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
}
