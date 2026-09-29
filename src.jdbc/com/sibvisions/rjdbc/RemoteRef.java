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

import java.sql.Ref;
import java.sql.SQLException;
import java.util.Map;

/**
 * Remote JDBC implementation of {@code Ref} functionality.
 */
public class RemoteRef implements Ref
{
    protected final RemoteClient client;
    
    protected final long id;

    /**
     * Creates a new {@code RemoteRef} instance.
     *
     * @param pClient the client
     * @param pId the remote resource identifier
     */
    public RemoteRef(RemoteClient pClient, long pId)
    {
        client = pClient; id = pId;
    }

    /** {@inheritDoc} */
    @Override
    public String toString()
    {
        return "RemoteRef[" + id + "]";
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
    public String getBaseTypeName() throws SQLException
    {
        return (String)RemoteUtil.invoke(client, id, Ref.class, "getBaseTypeName", new Class<?>[]{}, new Object[]{}, String.class);
    }

    /** {@inheritDoc} */
    @Override
    public Object getObject(Map<String,Class<?>> pMap) throws SQLException
    {
        return (Object)RemoteUtil.invoke(client, id, Ref.class, "getObject", new Class<?>[]{Map.class}, new Object[]{pMap}, Object.class);
    }

    /** {@inheritDoc} */
    @Override
    public Object getObject() throws SQLException
    {
        return (Object)RemoteUtil.invoke(client, id, Ref.class, "getObject", new Class<?>[]{}, new Object[]{}, Object.class);
    }

    /** {@inheritDoc} */
    @Override
    public void setObject(Object pValue) throws SQLException
    {
        RemoteUtil.invoke(client, id, Ref.class, "setObject", new Class<?>[]{Object.class}, new Object[]{pValue}, void.class);
    }
}
