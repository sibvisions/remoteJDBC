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

import java.sql.SQLException;
import java.sql.Savepoint;

/**
 * Remote JDBC implementation of {@code Savepoint} functionality.
 */
public class RemoteSavepoint implements Savepoint
{
    protected final RemoteClient client;
    
    protected final long id;

    /**
     * Creates a new {@code RemoteSavepoint} instance.
     *
     * @param pClient the client
     * @param pId the remote resource identifier
     */
    public RemoteSavepoint(RemoteClient pClient, long pId)
    {
        client = pClient; 
        id = pId;
    }

    /** {@inheritDoc} */
    @Override
    public String toString()
    {
        return "RemoteSavepoint[" + id + "]";
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

    /** {@inheritDoc} */
    @Override
    public int getSavepointId() throws SQLException
    {
        return (int)RemoteUtil.invoke(client, id, Savepoint.class, "getSavepointId", new Class<?>[]{}, new Object[]{}, int.class);
    }

    /** {@inheritDoc} */
    @Override
    public String getSavepointName() throws SQLException
    {
        return (String)RemoteUtil.invoke(client, id, Savepoint.class, "getSavepointName", new Class<?>[]{}, new Object[]{}, String.class);
    }
}
