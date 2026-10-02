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

import java.util.Arrays;

/**
 * Provides the {@code jdbc session} functionality.
 * 
 * @author René Jahn
 */
final class JdbcSession
{
    private final long id;
    
    private final JdbcContext context;
    
    private final JdbcDispatcher dispatcher;
    
    private byte[] connectionKey;

    private long requestSequence;

    private long responseSequence;    
    

    /**
     * Creates a new {@code JdbcSession} instance.
     *
     * @param pId the remote resource identifier
     * @param pContext the context
     */
    JdbcSession(long pId, JdbcContext pContext)
    {
        id = pId;
        context = pContext;
        
        dispatcher = new JdbcDispatcher(pContext);
    }

    /**
     * Returns id.
     * 
     * @return the resulting JDBC value
     */
    long getId()
    {
        return id;
    }

    /**
     * Returns context.
     * 
     * @return the resulting JDBC value
     */
    JdbcContext getContext()
    {
        return context;
    }

    /**
     * Returns dispatcher.
     * @return the resulting JDBC value
     */
    JdbcDispatcher getDispatcher()
    {
        return dispatcher;
    }

    void close()
    {
		clearConnectionKey();
		
		context.closeAll();
    }
    
    /**
     * Sets the connection key for the authenticated session.
     *
     * @param pConnectionKey the connection key
     */
    synchronized void setConnectionKey(byte[] pConnectionKey)
    {
        clearConnectionKey();
        
        connectionKey = pConnectionKey;
        requestSequence = 0;
        responseSequence = 0;
    }

    /**
     * Returns the connection key.
     *
     * @return the connection key
     */
    synchronized byte[] getConnectionKey()
    {
        return connectionKey;
    }

    /**
     * Accepts the next client request sequence.
     *
     * @param pSequence the sequence
     * @return {@code true} if the sequence is new
     */
    synchronized boolean acceptRequestSequence(long pSequence)
    {
        if (pSequence != requestSequence + 1)
        {
            return false;
        }

        requestSequence = pSequence;

        return true;
    }

    /**
     * Returns the next server response sequence.
     *
     * @return the next response sequence
     */
    synchronized long nextResponseSequence()
    {
        return ++responseSequence;
    }

    /**
     * Clears the connection key.
     */
    private synchronized void clearConnectionKey()
    {
        if (connectionKey != null)
        {
            Arrays.fill(connectionKey, (byte)0);
            
            connectionKey = null;
        }
    }    
}
