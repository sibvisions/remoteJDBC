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

import java.sql.Array;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

/**
 * Remote JDBC implementation of {@code Array} functionality.
 */
public class RemoteArray implements java.sql.Array
{
    protected final RemoteClient client;
    
    protected final long id;

    private Object array;
    
    private boolean arrayLoaded;
    private boolean freed;

    /**
     * Creates a new {@code RemoteArray} instance.
     *
     * @param pClient the client
     * @param pId the remote resource identifier
     */
    public RemoteArray(RemoteClient pClient, long pId)
    {
        this(pClient, pId, null, false);
    }

    /**
     * Creates a new {@code RemoteArray} instance.
     *
     * @param pClient the client
     * @param pId the remote resource identifier
     * @param pArray the array
     * @param pArrayLoaded the array loaded
     */
    public RemoteArray(RemoteClient pClient, long pId, Object pArray, boolean pArrayLoaded)
    {
        client = pClient;
        id = pId;
        
        array = pArray;
        arrayLoaded = pArrayLoaded;
    }

    /** {@inheritDoc} */
    @Override
    public String toString()
    {
        return "RemoteArray[" + id + "]";
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
    	ensureOpen();
    	
        return (String)RemoteUtil.invoke(client, id, Array.class, "getBaseTypeName", new Class<?>[]{}, new Object[]{}, String.class);
    }

    /** {@inheritDoc} */
    @Override
    public int getBaseType() throws SQLException
    {
    	ensureOpen();
    	
        return (int)RemoteUtil.invoke(client, id, Array.class, "getBaseType", new Class<?>[]{}, new Object[]{}, int.class);
    }

    /** {@inheritDoc} */
    @Override
    public Object getArray() throws SQLException
    {
        ensureOpen();

        if (!arrayLoaded)
        {
            array = RemoteUtil.invoke(client, id, java.sql.Array.class, "getArray", new Class<?>[]{}, new Object[]{}, Object.class);
            arrayLoaded = true;
        }

        return array;
    }

    /** {@inheritDoc} */
    @Override
    public Object getArray(Map<String,Class<?>> pMap) throws SQLException
    {
        ensureOpen();

        return getArray();
    }

    /** {@inheritDoc} */
    @Override
    public Object getArray(long pIndex, int pCount) throws SQLException
    {
        ensureOpen();

        return slice(getArray(), pIndex, pCount);
    }

    /** {@inheritDoc} */
    @Override
    public Object getArray(long pIndex, int pCount, Map<String,Class<?>> pMap) throws SQLException
    {
        ensureOpen();

        return slice(getArray(), pIndex, pCount);
    }

    /** {@inheritDoc} */
    @Override
    public ResultSet getResultSet() throws SQLException
    {
        ensureOpen();

        return (ResultSet)RemoteUtil.invoke(client, id, Array.class, "getResultSet", new Class<?>[]{}, new Object[]{}, ResultSet.class);
    }

    /** {@inheritDoc} */
    @Override
    public ResultSet getResultSet(Map<String,Class<?>> pMap) throws SQLException
    {
        ensureOpen();

        return (ResultSet)RemoteUtil.invoke(client, id, Array.class, "getResultSet", new Class<?>[]{Map.class}, new Object[]{pMap}, ResultSet.class);
    }

    /** {@inheritDoc} */
    @Override
    public ResultSet getResultSet(long pIndex, int pCount) throws SQLException
    {
        ensureOpen();

        return (ResultSet)RemoteUtil.invoke(client, id, Array.class, "getResultSet", new Class<?>[]{long.class, int.class}, new Object[]{pIndex, pCount}, ResultSet.class);
    }

    /** {@inheritDoc} */
    @Override
    public ResultSet getResultSet(long pIndex, int pCount, Map<String,Class<?>> pMap) throws SQLException
    {
        ensureOpen();

        return (ResultSet)RemoteUtil.invoke(client, id, Array.class, "getResultSet", new Class<?>[]{long.class, int.class, Map.class}, new Object[]{pIndex, pCount, pMap}, ResultSet.class);
    }

    /** {@inheritDoc} */
    @Override
    public void free() throws SQLException
    {
        if (!freed)
        {
            RemoteUtil.invoke(client, id, java.sql.Array.class, "free", new Class<?>[]{}, new Object[]{}, void.class);
            
            array = null;
            arrayLoaded = false;
            freed = true;
        }
    }

    /**
     * Sets cached array.
     *
     * @param pArray the array
     */
    void setCachedArray(Object pArray)
    {
        array = pArray;
        this.arrayLoaded = true;
    }

	/**
	 * Checks that the remote JDBC resource is still open.
	 * 
	 * @throws SQLException if array has been freed
	 */
    private void ensureOpen() throws SQLException
    {
        if (freed)
        {
            throw new SQLException("Array has been freed");
        }
    }

    /**
     * Handles the slice operation for the remote JDBC resource.
     *
     * @param pSource the source
     * @param pIndex the index
     * @param pCount the count
     * @return the resulting JDBC value
     * @throws SQLException if the JDBC operation cannot be completed
     */
    private Object slice(Object pSource, long pIndex, int pCount) throws SQLException
    {
        if (pIndex < 1 || pCount < 0)
        {
            throw new SQLException("Invalid array index/count: " + pIndex + "/" + pCount);
        }

        if (pSource == null)
        {
            return null;
        }

        int length = java.lang.reflect.Array.getLength(pSource);
        long start = pIndex - 1;

        if (start > length || start + pCount > length)
        {
            throw new SQLException("Invalid array index/count: " + pIndex + "/" + pCount + " for length " + length);
        }

        Object result = java.lang.reflect.Array.newInstance(pSource.getClass().getComponentType(), pCount);
        
        for (int i = 0; i < pCount; i++)
        {
            java.lang.reflect.Array.set(result, i, java.lang.reflect.Array.get(pSource, (int)start + i));
        }

        return result;
    }
}
