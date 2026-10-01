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

import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.NClob;
import java.sql.ParameterMetaData;
import java.sql.Ref;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.RowId;
import java.sql.SQLException;
import java.sql.SQLXML;
import java.sql.Savepoint;
import java.sql.Statement;
import java.sql.Struct;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import com.sibvisions.util.type.StringUtil;

/**
 * Provides the {@code jdbc context} functionality.
 * 
 * @author René Jahn
 */
public final class JdbcContext
{
	//30 minutes
	public final static long DEFAULT_IDLE_TIMEOUT = 1800000;
	
	//characters
	public final static long DEFAULT_CLOB_PREFETCH_SIZE = 102400;

	
	private final AtomicLong ids = new AtomicLong(1);
    
    private ExecutorService executorService;
    
    private final Map<Long, Connection> connections = new ConcurrentHashMap<>();
    private final Map<Long, Statement> statements = new ConcurrentHashMap<>();
    private final Map<Long, ResultSet> resultSets = new ConcurrentHashMap<>();
    private final Map<Long, ResultSetMetaData> resultSetMetadata = new ConcurrentHashMap<>();
    private final Map<Long, DatabaseMetaData> databaseMetadata = new ConcurrentHashMap<>();
    private final Map<Long, Blob> blobs = new ConcurrentHashMap<>();
    private final Map<Long, Clob> clobs = new ConcurrentHashMap<>();
    private final Map<Long, NClob> nclobs = new ConcurrentHashMap<>();
    private final Map<Long, SQLXML> sqlxml = new ConcurrentHashMap<>();
    private final Map<Long, Array> arrays = new ConcurrentHashMap<>();
    private final Map<Long, Struct> structs = new ConcurrentHashMap<>();
    private final Map<Long, ParameterMetaData> parameterMetaData = new ConcurrentHashMap<>();
    private final Map<Long, Ref> refs = new ConcurrentHashMap<>();
    private final Map<Long, RowId> rowIds = new ConcurrentHashMap<>();
    private final Map<Long, Savepoint> savepoints = new ConcurrentHashMap<>();
    private final Map<Long, Executor> executors = new ConcurrentHashMap<>();
    private final Map<Object, Long> resourceIds = new IdentityHashMap<>();
    
    private final Object lifecycleLock = new Object();

    private final String[] allowedJdbcUrls;
    private final String jdbcUrl;
    
    private final long idleTimeoutMillis;
    private final long clobPrefetchSize;
    
    private long lastAccess = System.currentTimeMillis();
    
    private int activeRequests;
    

    /**
     * Creates a new {@code JdbcContext} instance.
     */
    JdbcContext()
    {
        this("", "", DEFAULT_IDLE_TIMEOUT, DEFAULT_CLOB_PREFETCH_SIZE);
    }

    /**
     * Creates a new {@code JdbcContext} instance.
     *
     * @param pAllowedJdbcUrls the allowed jdbc urls
     */
    JdbcContext(String pAllowedJdbcUrls)
    {
        this(pAllowedJdbcUrls, "", DEFAULT_IDLE_TIMEOUT, DEFAULT_CLOB_PREFETCH_SIZE);
    }

    /**
     * Creates a new {@code JdbcContext} instance.
     *
     * @param pAllowedJdbcUrls the allowed jdbc urls
     * @param pJdbcUrl the jdbc url
     */
    JdbcContext(String pAllowedJdbcUrls, String pJdbcUrl)
    {
        this(pAllowedJdbcUrls, pJdbcUrl, DEFAULT_IDLE_TIMEOUT, DEFAULT_CLOB_PREFETCH_SIZE);
    }

    /**
     * Creates a new {@code JdbcContext} instance.
     *
     * @param pAllowedJdbcUrls the allowed jdbc urls
     * @param pJdbcUrl the jdbc url
     * @param pIdleTimeout the idle timeout in millis
     */
    JdbcContext(String pAllowedJdbcUrls, String pJdbcUrl, long pIdleTimeout)
    {
        this(pAllowedJdbcUrls, pJdbcUrl, pIdleTimeout, DEFAULT_CLOB_PREFETCH_SIZE);
    }

    /**
     * Creates a new {@code JdbcContext} instance.
     *
     * @param pAllowedJdbcUrls the allowed jdbc urls
     * @param pJdbcUrl the jdbc url
     * @param pIdleTimeout the idle timeout in millis
     * @param pClobPrefetchSize the clob prefetch size
     */
    JdbcContext(String pAllowedJdbcUrls, String pJdbcUrl, long pIdleTimeout, long pClobPrefetchSize)
    {
        allowedJdbcUrls = pAllowedJdbcUrls == null ? new String[0] : pAllowedJdbcUrls.trim().split(",");
        
        jdbcUrl = pJdbcUrl == null ? "" : pJdbcUrl.trim();
        
        idleTimeoutMillis = pIdleTimeout <= 0 ? 0 : pIdleTimeout;
        clobPrefetchSize = Math.max(0, pClobPrefetchSize);
    }
    
    /**
     * Begins request.
     */
    void beginRequest()
    {
        synchronized (lifecycleLock)
        {
            activeRequests++;
            
            lastAccess = System.currentTimeMillis();
        }
    }

    /**
     * Ends request.
     */
    void endRequest()
    {
        synchronized (lifecycleLock)
        {
            if (activeRequests > 0)
            {
                activeRequests--;
            }
            
            lastAccess = System.currentTimeMillis();
        }
    }

    /**
     * Returns whether idle expired.
     * 
     * @return whether the condition is true
     */
    boolean isIdleExpired()
    {
        synchronized (lifecycleLock)
        {
            return idleTimeoutMillis > 0
                   && activeRequests == 0
                   && System.currentTimeMillis() - lastAccess >= idleTimeoutMillis;
        }
    }

    /**
     * Closes if idle expired and releases its associated resources.
     * 
     * @return the resulting JDBC value
     */
    boolean closeIfIdleExpired()
    {
        synchronized (lifecycleLock)
        {
            if (idleTimeoutMillis <= 0 
            	|| activeRequests != 0
            	|| System.currentTimeMillis() - lastAccess < idleTimeoutMillis)
            {
                return false;

            }
            
            closeAll();
            
            lastAccess = System.currentTimeMillis();

            return true;
        }
    }

    /**
     * Returns idle timeout minutes.
     * 
     * @return the resulting JDBC value
     */
    long getIdleTimeoutMinutes()
    {
        return idleTimeoutMillis <= 0 ? 0 : TimeUnit.MILLISECONDS.toMinutes(idleTimeoutMillis);
    }

    /**
     * Returns jdbc url.
     * 
     * @return the resulting JDBC value
     */
    public String getJdbcUrl()
    {
        return jdbcUrl;
    }

	/**
	 * Returns maximum CLOB length in characters that is pre-fetched with fetchRows().
	 * 
	 * @return the maximum CLOB pre-fetch size in characters 
	 */
    public long getClobPrefetchSize()
    {
        return clobPrefetchSize;
    }

    /**
     * Returns whether jdbc url is allowed.
     *
     * @param pJdbcUrl the jdbc url
     * @return whether the condition is true
     */
    public boolean isJdbcUrlAllowed(String pJdbcUrl)
    {
        if (pJdbcUrl == null || allowedJdbcUrls.length == 0)
        {
            return false;
        }
        
        for (String pattern : allowedJdbcUrls)
        {
            pattern = pattern.trim();

            if (!pattern.isEmpty() && StringUtil.like(pJdbcUrl, pattern))
            {
                return true;
            }
        }

        return false;
    }

    /**
     * Adds connection to the associated JDBC resource.
     *
     * @param pValue the value to convert
     * @return the resulting JDBC value
     */
    long addConnection(Connection pValue)
    {
        return addResource(connections, pValue);
    }

    /**
     * Adds statement to the associated JDBC resource.
     *
     * @param pValue the value to convert
     * @return the resulting JDBC value
     */
    long addStatement(Statement pValue)
    {
        return addResource(statements, pValue);
    }

    /**
     * Adds result set to the associated JDBC resource.
     *
     * @param pValue the value to convert
     * @return the resulting JDBC value
     */
    long addResultSet(ResultSet pValue)
    {
        return addResource(resultSets, pValue);
    }

    /**
     * Adds result set metadata to the associated JDBC resource.
     *
     * @param pValue the value to convert
     * @return the resulting JDBC value
     */
    long addResultSetMetadata(ResultSetMetaData pValue)
    {
        return addResource(resultSetMetadata, pValue);
    }

    /**
     * Adds database metadata to the associated JDBC resource.
     *
     * @param pValue the value to convert
     * @return the resulting JDBC value
     */
    long addDatabaseMetadata(DatabaseMetaData pValue)
    {
        return addResource(databaseMetadata, pValue);
    }

    /**
     * Adds blob to the associated JDBC resource.
     *
     * @param pValue the value to convert
     * @return the resulting JDBC value
     */
    long addBlob(Blob pValue)
    {
        return addResource(blobs, pValue);
    }

    /**
     * Adds clob to the associated JDBC resource.
     *
     * @param pValue the value to convert
     * @return the resulting JDBC value
     */
    long addClob(Clob pValue)
    {
        return addResource(clobs, pValue);
    }

    /**
     * Adds nclob to the associated JDBC resource.
     *
     * @param pValue the value to convert
     * @return the resulting JDBC value
     */
    long addNClob(NClob pValue)
    {
        return addResource(nclobs, pValue);
    }

    /**
     * Adds sqlxml to the associated JDBC resource.
     *
     * @param pValue the value to convert
     * @return the resulting JDBC value
     */
    long addSQLXML(SQLXML pValue)
    {
        return addResource(sqlxml, pValue);
    }

    /**
     * Adds array to the associated JDBC resource.
     *
     * @param pValue the value to convert
     * @return the resulting JDBC value
     */
    long addArray(Array pValue)
    {
        return addResource(arrays, pValue);
    }

    /**
     * Adds struct to the associated JDBC resource.
     *
     * @param pValue the value to convert
     * @return the resulting JDBC value
     */
    long addStruct(Struct pValue)
    {
        return addResource(structs, pValue);
    }

    /**
     * Adds parameter meta data to the associated JDBC resource.
     *
     * @param pValue the value to convert
     * @return the resulting JDBC value
     */
    long addParameterMetaData(ParameterMetaData pValue)
    {
        return addResource(parameterMetaData, pValue);
    }

    /**
     * Adds ref to the associated JDBC resource.
     *
     * @param pValue the value to convert
     * @return the resulting JDBC value
     */
    long addRef(Ref pValue)
    {
        return addResource(refs, pValue);
    }

    /**
     * Adds row id to the associated JDBC resource.
     *
     * @param pValue the value to convert
     * @return the resulting JDBC value
     */
    long addRowId(RowId pValue)
    {
        return addResource(rowIds, pValue);
    }

    /**
     * Adds savepoint to the associated JDBC resource.
     *
     * @param pValue the value to convert
     * @return the resulting JDBC value
     */
    long addSavepoint(Savepoint pValue)
    {
        return addResource(savepoints, pValue);
    }

    /**
     * Adds executor to the associated JDBC resource.
     * 
     * @return the resulting JDBC value
     */
    long addExecutor()
    {
        synchronized (lifecycleLock)
        {
            if (executorService == null || executorService.isShutdown())
            {
                executorService = Executors.newCachedThreadPool(r ->
                {
                    Thread thread = new Thread(r, "rjdbc-remote-executor");
                    thread.setDaemon(true);

                    return thread;
                });
            }

            synchronized (resourceIds)
            {
                Long existingId = resourceIds.get(executorService);

                if (existingId != null)
                {
                    return existingId;
                }

                long id = ids.getAndIncrement();

                executors.put(id, executorService);
                resourceIds.put(executorService, id);

                return id;
            }
        }
    }

    /**
     * Registers a JDBC resource and its reverse identifier mapping.
     *
     * @param pResources the resource registry
     * @param pValue the JDBC resource
     * @param <T> the JDBC resource type
     * @return the generated resource identifier
     */
    private <T> long addResource(Map<Long, T> pResources, T pValue)
    {
        synchronized (resourceIds)
        {
            Long existingId = resourceIds.get(pValue);

            if (existingId != null)
            {
                return existingId;
            }

            long id = ids.getAndIncrement();

            pResources.put(id, pValue);
            resourceIds.put(pValue, id);

            return id;
        }
    }

	/**
	 * Handles the id for operation for the remote JDBC resource.
	 *
	 * @param pValue the value to convert
	 * @return the resulting JDBC value
	 */
    public long idFor(Object pValue)
    {
        if (pValue == null)
        {
            return 0;
        }
            
        synchronized (resourceIds)
        {
            Long id = resourceIds.get(pValue);

            if (id != null)
            {
                return id;
            }
        }

        if (pValue instanceof Connection)
        {
            return addConnection((Connection)pValue);
        }

        if (pValue instanceof Statement)
        {
            return addStatement((Statement)pValue);
        }

        if (pValue instanceof ResultSet)
        {
            return addResultSet((ResultSet)pValue);
        }

        if (pValue instanceof ResultSetMetaData)
        {
            return addResultSetMetadata((ResultSetMetaData)pValue);
        }

        if (pValue instanceof DatabaseMetaData)
        {
            return addDatabaseMetadata((DatabaseMetaData)pValue);
        }

        if (pValue instanceof Blob)
        {
            return addBlob((Blob)pValue);
        }

        if (pValue instanceof NClob)
        {
            return addNClob((NClob)pValue);
        }

        if (pValue instanceof Clob)
        {
            return addClob((Clob)pValue);
        }

        if (pValue instanceof SQLXML)
        {
            return addSQLXML((SQLXML)pValue);
        }

        if (pValue instanceof Array)
        {
            return addArray((Array)pValue);
        }

        if (pValue instanceof Struct)
        {
            return addStruct((Struct)pValue);
        }

        if (pValue instanceof ParameterMetaData)
        {
            return addParameterMetaData((ParameterMetaData)pValue);
        }

        if (pValue instanceof Ref)
        {
            return addRef((Ref)pValue);
        }

        if (pValue instanceof RowId)
        {
            return addRowId((RowId)pValue);
        }

        if (pValue instanceof Savepoint)
        {
            return addSavepoint((Savepoint)pValue);
        }
        
        throw new IllegalArgumentException("Unsupported JDBC return type: " + pValue.getClass().getName());
    }

	/**
	 * Handles the result set metadata operation for the remote JDBC resource.
	 *
	 * @param pId the remote resource identifier
	 * @return the resulting JDBC value
	 * @throws SQLException if the JDBC operation cannot be completed
	 */
	public ResultSetMetaData resultSetMetadata(long pId) throws SQLException 
	{
		return get(resultSetMetadata, pId, "result set metadata");
	}

	/**
	 * Handles the database metadata operation for the remote JDBC resource.
	 *
	 * @param pId the remote resource identifier
	 * @return the resulting JDBC value
	 * @throws SQLException if the JDBC operation cannot be completed
	 */
	public DatabaseMetaData databaseMetadata(long pId) throws SQLException        
	{
		return get(databaseMetadata, pId, "database metadata");
    }

	/**
	 * Gets the connection for given identifier.
	 *
	 * @param pId the remote resource identifier
	 * @return the resulting JDBC value
	 * @throws SQLException if the JDBC operation cannot be completed
	 */
	public Connection connection(long pId) throws SQLException        
	{
		return get(connections, pId, "connection");
    }

	/**
	 * Gets the statement for given identifier.
	 *
	 * @param pId the remote resource identifier
	 * @return the resulting JDBC value
	 * @throws SQLException if the JDBC operation cannot be completed
	 */
	public Statement statement(long pId) throws SQLException
    {
		return get(statements, pId, "statement");
    }

	/**
	 * Gets the blob for given identifier.
	 *
	 * @param pId the remote resource identifier
	 * @return the resulting JDBC value
	 * @throws SQLException if the JDBC operation cannot be completed
	 */
	public Blob blob(long pId) throws SQLException
    {
        return get(blobs, pId, "blob");
    }

	/**
	 * Gets the clob for given identifier.
	 *
	 * @param pId the remote resource identifier
	 * @return the resulting JDBC value
	 * @throws SQLException if the JDBC operation cannot be completed
	 */
	public Clob clob(long pId) throws SQLException
    {
        return get(clobs, pId, "clob");
    }

	/**
	 * Gets the nclob for given identifier.
	 *
	 * @param pId the remote resource identifier
	 * @return the resulting JDBC value
	 * @throws SQLException if the JDBC operation cannot be completed
	 */
	public NClob nclob(long pId) throws SQLException
    {
        return get(nclobs, pId, "nclob");
    }

	/**
	 * Gets the sqlxml for given identifier.
	 *
	 * @param pId the remote resource identifier
	 * @return the resulting JDBC value
	 * @throws SQLException if the JDBC operation cannot be completed
	 */
	public SQLXML sqlxml(long pId) throws SQLException
    {
        return get(sqlxml, pId, "sqlxml");
    }

	/**
	 * Gets the array for given identifier.
	 *
	 * @param pId the remote resource identifier
	 * @return the resulting JDBC value
	 * @throws SQLException if the JDBC operation cannot be completed
	 */
	public Array array(long pId) throws SQLException
    {
        return get(arrays, pId, "array");
    }

	/**
	 * Gets the struct for given identifier.
	 *
	 * @param pId the remote resource identifier
	 * @return the resulting JDBC value
	 * @throws SQLException if the JDBC operation cannot be completed
	 */
	public Struct struct(long pId) throws SQLException
    {
        return get(structs, pId, "struct");
    }

	/**
	 * Gets the parameter metadata for given identifier.
	 *
	 * @param pId the remote resource identifier
	 * @return the resulting JDBC value
	 * @throws SQLException if the JDBC operation cannot be completed
	 */
	public ParameterMetaData parameterMetaData(long pId) throws SQLException
    {
        return get(parameterMetaData, pId, "parameter metadata");
    }

	/**
	 * Gets the ref for given identifier.
	 *
	 * @param pId the remote resource identifier
	 * @return the resulting JDBC value
	 * @throws SQLException if the JDBC operation cannot be completed
	 */
	public Ref ref(long pId) throws SQLException
    {
        return get(refs, pId, "ref");
    }

	/**
	 * Gets the rowid for given identifier.
	 *
	 * @param pId the remote resource identifier
	 * @return the resulting JDBC value
	 * @throws SQLException if the JDBC operation cannot be completed
	 */
	public RowId rowId(long pId) throws SQLException
    {
        return get(rowIds, pId, "row id");
    }

	/**
	 * Gets the savepoint for given identifier.
	 *
	 * @param pId the remote resource identifier
	 * @return the resulting JDBC value
	 * @throws SQLException if the JDBC operation cannot be completed
	 */
	public Savepoint savepoint(long pId) throws SQLException
    {
        return get(savepoints, pId, "savepoint");
    }

    /**
     * Removes savepoint.
     *
     * @param pId the remote resource identifier
     */
    public void removeSavepoint(long pId)
    {
    	removeResource(savepoints, pId);
    }

	/**
	 * Gets the executor for given identifier.
	 *
	 * @param pId the remote resource identifier
	 * @return the resulting JDBC value
	 * @throws SQLException if the JDBC operation cannot be completed
	 */
	public Executor executor(long pId) throws SQLException
    {
        return get(executors, pId, "executor");
    }
	
	/**
	 * Gets the result set for given identifier.
	 *
	 * @param pId the remote resource identifier
	 * @return the resulting JDBC value
	 * @throws SQLException if the JDBC operation cannot be completed
	 */
	public ResultSet resultSet(long pId) throws SQLException
    {
		return get(resultSets, pId, "result set");
    }

	/**
	 * Looks up a JDBC resource by its remote identifier.
	 *
	 * @param pMap  the map
	 * @param pId   the remote resource identifier
	 * @param pType the requested Java type
	 * @return the resulting JDBC value
	 * @throws SQLException if the JDBC operation cannot be completed
	 */
	private static <T> T get(Map<Long,T> pMap, long pId, String pType) throws SQLException
    {
        T value = pMap.get(pId);

        if (value == null)
        {
            throw new SQLException("Unknown " + pType + " id: " + pId);

        }

        return value;
    }

	/**
	 * Closes the JDBC resource and releases its associated resources.
	 *
	 * @param pId the remote resource identifier
	 * @throws SQLException if the operation fails
	 */
	void close(long pId) throws SQLException
    {
        Connection c = removeResource(connections, pId);

        if (c != null)
        {
            c.close(); 
            
            return;
        }

        Statement s = removeResource(statements, pId);

        if (s != null)
        {
            s.close(); 
            
            return;
        }

        ResultSet r = removeResource(resultSets, pId);

        if (r != null)
        {
            r.close(); return;
        }

        Object value = removeResource(blobs, pId);

        if (value != null)
        {
            quietFree(value);
            
            return;
        }
        
        value = removeResource(clobs, pId);

        if (value != null)
        {
            quietFree(value);
            
            return;
        }
        
        value = removeResource(nclobs, pId);

        if (value != null)
        {
            quietFree(value);
            
            return;
        }
        
        value = removeResource(sqlxml, pId);

        if (value != null)
        {
            quietFree(value);
            
            return;
        }
        
        value = removeResource(arrays, pId);

        if (value != null)
        {
            quietFree(value);
            
            return;
        }

        if (removeResource(structs, pId) != null)
        {
            return;
        }

        if (removeResource(parameterMetaData, pId) != null)
        {
            return;
        }

        if (removeResource(refs, pId) != null)
        {
            return;
        }

        if (removeResource(rowIds, pId) != null)
        {
            return;
        }

        if (removeResource(savepoints, pId) != null)
        {
            return;
        }

        if (removeResource(executors, pId) != null)
        {
            return;
        }
    }

    /**
     * Removes a JDBC resource and its reverse identifier mapping.
     *
     * @param pResources the resource registry
     * @param pId the remote resource identifier
     * @param <T> the JDBC resource type
     * @return the removed JDBC resource
     */
    private <T> T removeResource(Map<Long, T> pResources, long pId)
    {
        T value = pResources.remove(pId);

        if (value != null)
        {
            synchronized (resourceIds)
            {
                resourceIds.remove(value);
            }
        }

        return value;
    }	

	/**
	 * Closes all and releases its associated resources.
	 */
	void closeAll()        
	{
        resultSets.values().forEach(JdbcContext::quietClose);
        statements.values().forEach(JdbcContext::quietClose);
        connections.values().forEach(JdbcContext::quietClose);
        
        blobs.values().forEach(JdbcContext::quietFree);
        clobs.values().forEach(JdbcContext::quietFree);
        nclobs.values().forEach(JdbcContext::quietFree);
        sqlxml.values().forEach(JdbcContext::quietFree);
        arrays.values().forEach(JdbcContext::quietFree);
        
        resultSets.clear();
        statements.clear();
        connections.clear();
        resultSetMetadata.clear();
        databaseMetadata.clear();
        
        blobs.clear(); 
        clobs.clear(); 
        nclobs.clear(); 
        sqlxml.clear(); 
        arrays.clear();
        
        structs.clear(); 
        parameterMetaData.clear(); 
        refs.clear(); 
        rowIds.clear(); 
        savepoints.clear();
        
        executors.clear();
        
        synchronized (lifecycleLock)
        {
            synchronized (resourceIds)
            {
                resourceIds.clear();
            }

            if (executorService != null)
            {
                executorService.shutdownNow();
                executorService = null;
            }
        }
    }

	/**
	 * Closes a resource while suppressing exceptions.
	 *
	 * @param pC the resource to close
	 */
	private static void quietClose(AutoCloseable pCloseable)
    {
        try
        {
            pCloseable.close();
        }
        catch (Exception ignored)
        {
        }
    }

    /**
	 * Releases or Closes a JDBC resource while suppressing exceptions.
	 *
	 * @param pObject the object to free/close
	 */
	private static void quietFree(Object pObject)
    {
		if (pObject == null)
		{
			return;
		}
		
        try
        {
        	if (pObject instanceof AutoCloseable)
        	{
        		((AutoCloseable)pObject).close();
        	}
        	else if (pObject instanceof Blob)
            {
            	((Blob)pObject).free();
            }
            else if (pObject instanceof Clob)
            {
            	((Clob)pObject).free();
            }
            else if (pObject instanceof SQLXML) 
        	{
            	((SQLXML)pObject).free();
        	}
            else if (pObject instanceof Array)
            {
            	((Array)pObject).free();
            }
        }
        catch (Exception ignored)
        {
        }
    }
}
