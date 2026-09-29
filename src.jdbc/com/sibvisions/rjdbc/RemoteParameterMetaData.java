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

import java.sql.ParameterMetaData;
import java.sql.SQLException;

/**
 * Remote JDBC implementation of {@code ParameterMetaData} functionality.
 */
public class RemoteParameterMetaData implements ParameterMetaData
{
    protected final RemoteClient client;
    
    protected final long id;

    /**
     * Creates a new {@code RemoteParameterMetaData} instance.
     *
     * @param pClient the client
     * @param pId the remote resource identifier
     */
    public RemoteParameterMetaData(RemoteClient pClient, long pId)
    {
        client = pClient; 
        id = pId;
    }

    /** {@inheritDoc} */
    @Override
    public <T> T unwrap(Class<T> pIface) throws SQLException
    {
        return RemoteUtil.unwrapLocal(this, pIface);
    }

    /** {@inheritDoc} */
    @Override
    public boolean isWrapperFor(Class<?> pIface) throws SQLException
    {
        return RemoteUtil.isWrapperForLocal(this, pIface);
    }

    /** {@inheritDoc} */
    @Override
    public String toString()
    {
        return "RemoteParameterMetaData[" + id + "]";
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
    public int getParameterCount() throws SQLException
    {
        return (int)RemoteUtil.invoke(client, id, ParameterMetaData.class, "getParameterCount", new Class<?>[]{}, new Object[]{}, int.class);
    }

    /** {@inheritDoc} */
    @Override
    public int isNullable(int pParameterIndex) throws SQLException
    {
        return (int)RemoteUtil.invoke(client, id, ParameterMetaData.class, "isNullable", new Class<?>[]{int.class}, new Object[]{pParameterIndex}, int.class);
    }

    /** {@inheritDoc} */
    @Override
    public boolean isSigned(int pParameterIndex) throws SQLException
    {
        return (boolean)RemoteUtil.invoke(client, id, ParameterMetaData.class, "isSigned", new Class<?>[]{int.class}, new Object[]{pParameterIndex}, boolean.class);
    }

    /** {@inheritDoc} */
    @Override
    public int getPrecision(int pParameterIndex) throws SQLException
    {
        return (int)RemoteUtil.invoke(client, id, ParameterMetaData.class, "getPrecision", new Class<?>[]{int.class}, new Object[]{pParameterIndex}, int.class);
    }

    /** {@inheritDoc} */
    @Override
    public int getScale(int pParameterIndex) throws SQLException
    {
        return (int)RemoteUtil.invoke(client, id, ParameterMetaData.class, "getScale", new Class<?>[]{int.class}, new Object[]{pParameterIndex}, int.class);
    }

    /** {@inheritDoc} */
    @Override
    public int getParameterType(int pParameterIndex) throws SQLException
    {
        return (int)RemoteUtil.invoke(client, id, ParameterMetaData.class, "getParameterType", new Class<?>[]{int.class}, new Object[]{pParameterIndex}, int.class);
    }

    /** {@inheritDoc} */
    @Override
    public String getParameterTypeName(int pParameterIndex) throws SQLException
    {
        return (String)RemoteUtil.invoke(client, id, ParameterMetaData.class, "getParameterTypeName", new Class<?>[]{int.class}, new Object[]{pParameterIndex}, String.class);
    }

    /** {@inheritDoc} */
    @Override
    public String getParameterClassName(int pParameterIndex) throws SQLException
    {
        return (String)RemoteUtil.invoke(client, id, ParameterMetaData.class, "getParameterClassName", new Class<?>[]{int.class}, new Object[]{pParameterIndex}, String.class);
    }

    /** {@inheritDoc} */
    @Override
    public int getParameterMode(int pParameterIndex) throws SQLException
    {
        return (int)RemoteUtil.invoke(client, id, ParameterMetaData.class, "getParameterMode", new Class<?>[]{int.class}, new Object[]{pParameterIndex}, int.class);
    }
}
