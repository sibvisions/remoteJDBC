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

import java.sql.RowId;

/**
 * Remote JDBC implementation of {@code RowId} functionality.
 * 
 * @author René Jahn
 */
public class RemoteRowId implements RowId
{
    protected final RemoteClient client;
    
    protected final long id;
    

    /**
     * Creates a new {@code RemoteRowId} instance.
     *
     * @param pClient the client
     * @param pId the remote resource identifier
     */
    public RemoteRowId(RemoteClient pClient, long pId)
    {
        client = pClient; 
        id = pId;
    }

    /** {@inheritDoc} */
    @Override
    public String toString()
    {
        return "RemoteRowId[" + id + "]";
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
    public byte[] getBytes()
    {
        return (byte[])RemoteUtil.invokeUnchecked(client, id, RowId.class, "getBytes", new Class<?>[]{}, new Object[]{}, byte[].class);
    }
}
