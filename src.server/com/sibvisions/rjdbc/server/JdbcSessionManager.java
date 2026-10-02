/*
 * Copyright (C) 2026 SIB Visions GmbH
 *
 * This file is part of RemoteJDBC.
 *
 * RemoteJDBC is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * RemoteJDBC is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with RemoteJDBC. If not, see <https://www.gnu.org/licenses/>.
 */
package com.sibvisions.rjdbc.server;

import java.security.SecureRandom;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.sibvisions.util.log.LoggerFactory;
import com.sibvisions.util.type.StringUtil;

/**
 * Provides the {@code jdbc session manager} functionality.
 * 
 * @author René Jahn
 */
final class JdbcSessionManager
{
    private static final SecureRandom RANDOM = new SecureRandom();

    private final Map<Long, JdbcSession> sessions = new ConcurrentHashMap<Long, JdbcSession>();
	
    private ScheduledExecutorService cleanupExecutor;
    
    private final JdbcSecurity security;
    
    private final String allowedJdbcUrls;
    
    private final String jdbcUrl;
    private final String jdbcUsername;
    private final String jdbcPassword;
    private final String environment;    
    
    private final long idleTimeout;
    private final long clobPrefetchSize;
    
    /**
     * Creates a new {@code JdbcSessionManager} instance.
     *
     * @param pAllowedJdbcUrls the allowed client jdbc urls
     * @param pJdbcUrl the server jdbc url
     * @param pJdbcUsername the server jdbc username
     * @param pJdbcPassword the server jdbc password
     * @param pIdleTimeout the idle timeout in millis
     * @param pClobPrefetchSize the clob pre-fetch size
     * @param pEnvironment the server environment
     * @param pSecurity the security configuration
     */
    JdbcSessionManager(String pAllowedJdbcUrls, String pJdbcUrl, String pJdbcUsername, String pJdbcPassword,
                       String pIdleTimeout, String pClobPrefetchSize, String pEnvironment, JdbcSecurity pSecurity)
    {
        allowedJdbcUrls = pAllowedJdbcUrls;
        jdbcUrl = pJdbcUrl;
        jdbcUsername = pJdbcUsername;
        jdbcPassword = pJdbcPassword;
        environment = StringUtil.isEmpty(pEnvironment) ? JdbcSecurity.ENVIRONMENT_DEV : pEnvironment.trim();
        
        idleTimeout = toLong(pIdleTimeout, JdbcContext.DEFAULT_IDLE_TIMEOUT, "idleTimeout");
        clobPrefetchSize = Math.max(0, toLong(pClobPrefetchSize, JdbcContext.DEFAULT_CLOB_PREFETCH_SIZE, "clobPrefetchSize"));
        
        security = pSecurity;

        if (idleTimeout > 0)
        {
            cleanupExecutor = Executors.newSingleThreadScheduledExecutor(r ->
            {
                Thread thread = new Thread(r, "rjdbc-idle-cleanup");
                thread.setDaemon(true);

                return thread;
            });

            cleanupExecutor.scheduleWithFixedDelay(() -> closeIfIdleExpired(), 1, 1, TimeUnit.MINUTES);
        }
    }

    /**
     * Returns the server security configuration.
     *
     * @return the security configuration
     */
    JdbcSecurity getSecurity()
    {
        return security;
    }
    
    /**
     * Returns the environment.
     * 
     * @return the environment
     */
    String getEnvironment()
    {
    	return environment;
    }
    
    /**
     * Converts given value to long.
     * 
     * @param pValue the value
     * @param pDefault the default value if {@code value} is empty
     * @param pName the name of the value
     * @return the converted {@code value} or {@code default}
     */
    private static long toLong(String pValue, long pDefault, String pName)
    {
    	if (StringUtil.isEmpty(pValue))
    	{
    		return pDefault;
    	}
    	
    	try
    	{
    		long value = Long.parseLong(pValue.trim());
    		
    		return Math.max(0, value);
    	}
    	catch (Exception e)
    	{
    		LoggerFactory.getInstance(JdbcSessionManager.class).error("Invalid value '", pName, "'", e);
    		
    		return pDefault;
    	}
    }
    
    /**
     * Creates a new JDBC session.
     * 
     * @return the new session
     */
    JdbcSession create()
    {
        JdbcContext context = new JdbcContext(allowedJdbcUrls, jdbcUrl, jdbcUsername, jdbcPassword, idleTimeout, clobPrefetchSize, environment);
        JdbcSession session;
        
        long id;

        do
        {
            id = RANDOM.nextLong() & Long.MAX_VALUE;
            
            session = new JdbcSession(id, context);
        }
        while (id == 0 || sessions.putIfAbsent(id, session) != null);

        return session;
    }
    
    /**
     * Returns a session for the given identifier without starting a request.
     *
     * @param pId the remote resource identifier
     * @return the session
     * @throws JdbcSessionExpiredException if the session does not exist
     */
    JdbcSession get(long pId) throws JdbcSessionExpiredException
    {
        JdbcSession session = sessions.get(pId);

        if (session == null)
        {
            throw new JdbcSessionExpiredException(pId);
        }

        return session;
    }    

    /**
     * Handles the begin request operation for the remote JDBC resource.
     *
     * @param pId the remote resource identifier
     * @return the resulting JDBC value
     * @throws SQLException if the JDBC operation cannot be completed
     */
    JdbcSession beginRequest(long pId) throws SQLException
    {
        JdbcSession session = get(pId);
        
        synchronized (session)
        {
            if (sessions.get(pId) != session)
            {
                throw new JdbcSessionExpiredException(pId);
            }

            // The cleanup and close operations use the same lock, so a session
            // cannot be closed between lookup and beginRequest()
            session.getContext().beginRequest();

            return session;
        }
    }
    
    JdbcSession beginRequest(long pId, long pSequence) throws SQLException
    {
        JdbcSession session = get(pId);

        synchronized (session)
        {
            if (sessions.get(pId) != session)
            {
                throw new JdbcSessionExpiredException(pId);
            }

            if (!session.acceptRequestSequence(pSequence))
            {
                throw new SecurityException("Invalid remote JDBC request sequence");
            }

            // The cleanup and close operations use the same lock, so a session
            // cannot be closed between sequence validation and beginRequest().
            session.getContext().beginRequest();

            return session;
        }
    }    

    /**
     * Closes specific session and releases its associated resources.
     *
     * @param pId the remote resource identifier
     */
    void close(long pId)
    {
        JdbcSession session = sessions.get(pId);

        if (session != null)
        {
            boolean removed;

            synchronized (session)
            {
                removed = sessions.remove(pId, session);
            }

            if (removed)
            {
            	session.close();
            }
        }
    }

    /**
     * Closes sessions if idle expired and releases its associated resources.
     */
    private void closeIfIdleExpired()
    {
        for (Map.Entry<Long, JdbcSession> entry : sessions.entrySet())
        {
            JdbcSession session = entry.getValue();
            
            boolean removed;

            synchronized (session)
            {
                removed = session.getContext().isIdleExpired() && sessions.remove(entry.getKey(), session);
            }

            if (removed)
            {
            	session.close();
            }
        }
    }

    /**
     * Closes all sessions and releases its associated resources.
     */
    private void closeAll()
    {
    	try
    	{
	        for (JdbcSession session : sessions.values())
	        {
	        	try
	        	{
	        		session.close();
	        	}
	        	catch (Exception ignore)
	        	{
	        	}
	        }
    	}
    	finally
    	{
    		sessions.clear();
    	}
    }
    
    /**
     * Disposes this instance and closes all sessions.
     */
    void dispose()
    {
    	try
    	{
	        if (cleanupExecutor != null)
	        {
	            cleanupExecutor.shutdownNow();
	        }
    	}
    	finally
    	{
    		closeAll();
    	}
    }
}
