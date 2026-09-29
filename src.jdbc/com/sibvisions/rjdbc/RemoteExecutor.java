/*
 * Copyright (C)2026 SIB Visions GmbH
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

package com.sibvisions.rjdbc;

import java.util.concurrent.Executor;

/**
 * Client-side handle for a server-side Executor belonging to the JDBC session.
 * The local execute method remains a normal Executor operation; JDBC methods
 * such as Connection.abort use the remote id when the executor crosses the wire.
 */
public final class RemoteExecutor implements Executor
{
    protected final RemoteClient client;
    
    protected final long id;
    
    private final Executor localExecutor;

    /**
     * Creates a new {@code RemoteExecutor} instance.
     *
     * @param pClient the client
     * @param pId the remote resource identifier
     * @param pLocalExecutor the local executor
     */
    RemoteExecutor(RemoteClient pClient, long pId, Executor pLocalExecutor)
    {
        client = pClient;
        id = pId;
        
        localExecutor = pLocalExecutor;
    }

    /** {@inheritDoc} */
    @Override
    public void execute(Runnable pCommand)
    {
        if (pCommand == null)
        {
            throw new NullPointerException("command");

        }
        localExecutor.execute(pCommand);
    }

    /** {@inheritDoc} */
    @Override
    public String toString()
    {
        return "RemoteExecutor[" + id + "]";
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode()
    {
        return Long.hashCode(id);
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(Object pObject)
    {
        return this == pObject;
    }
}
