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
package com.sibvisions.rjdbc;

import java.sql.SQLException;
import java.sql.Struct;
import java.util.Map;

/**
 * Remote JDBC implementation of {@code Struct} functionality.
 * 
 * @author René Jahn
 */
public class RemoteStruct implements Struct
{
    protected final RemoteClient client;
    
    protected final long id;
    

    /**
     * Creates a new {@code RemoteStruct} instance.
     *
     * @param pClient the client
     * @param pId the remote resource identifier
     */
    public RemoteStruct(RemoteClient pClient, long pId)
    {
        client = pClient; 
        id = pId;
    }

    /** {@inheritDoc} */
    @Override
    public String toString()
    {
        return "RemoteStruct[" + id + "]";
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
    public String getSQLTypeName() throws SQLException
    {
        return (String)RemoteUtil.invoke(client, id, Struct.class, "getSQLTypeName", new Class<?>[] {}, new Object[] {}, String.class);
    }

    /** {@inheritDoc} */
    @Override
    public Object[] getAttributes() throws SQLException
    {
        return (Object[])RemoteUtil.invoke(client, id, Struct.class, "getAttributes", new Class<?>[] {}, new Object[] {}, Object[].class);
    }

    /** {@inheritDoc} */
    @Override
    public Object[] getAttributes(Map<String,Class<?>> pMap) throws SQLException
    {
        return (Object[])RemoteUtil.invoke(client, id, Struct.class, "getAttributes", new Class<?>[] {Map.class}, new Object[] {pMap}, Object[].class);
    }
}
