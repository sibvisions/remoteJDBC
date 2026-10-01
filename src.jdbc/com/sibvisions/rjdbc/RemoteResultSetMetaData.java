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

import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/** 
 * Client-side ResultSetMetaData with one-shot metadata snapshot.
 *  
 * @author René Jahn
 */
public class RemoteResultSetMetaData implements ResultSetMetaData
{
    protected final RemoteClient client;
    
    protected final Class<?> remoteInterface;
    
    private volatile Object[][] columns;

    protected final long id;
    
    private volatile int count = -1;

    
    /**
     * Creates a new {@code RemoteResultSetMetaData} instance.
     *
     * @param pClient the client
     * @param pId the remote resource identifier
     */
    public RemoteResultSetMetaData(RemoteClient pClient, long pId)
    {
        this(pClient, pId, ResultSetMetaData.class);
    }

    /**
     * Creates a new {@code RemoteResultSetMetaData} instance.
     *
     * @param pClient the client
     * @param pId the remote resource identifier
     * @param pRemoteInterface the remote interface
     */
    protected RemoteResultSetMetaData(RemoteClient pClient, long pId, Class<?> pRemoteInterface)
    {
        client = pClient;
        id = pId;
        
        remoteInterface = pRemoteInterface;
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
        return remoteInterface.getSimpleName() + "[" + id + "]";
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode()
    {
        return Long.hashCode(id);
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(Object pObj)
    {
        return this == pObj;
    }

    /** {@inheritDoc} */
    @Override
    public int getColumnCount() throws SQLException
    {
        load(); return count;
    }

    /** {@inheritDoc} */
    @Override
    public boolean isAutoIncrement(int pColumn) throws SQLException
    {
        return bool(pColumn, 0);
    }

    /** {@inheritDoc} */
    @Override
    public boolean isCaseSensitive(int pColumn) throws SQLException
    {
        return bool(pColumn, 1);
    }

    /** {@inheritDoc} */
    @Override
    public boolean isSearchable(int pColumn) throws SQLException
    {
        return bool(pColumn, 2);
    }

    /** {@inheritDoc} */
    @Override
    public boolean isCurrency(int pColumn) throws SQLException
    {
        return bool(pColumn, 3);
    }

    /** {@inheritDoc} */
    @Override
    public int isNullable(int pColumn) throws SQLException
    {
        return integer(pColumn, 4);
    }

    /** {@inheritDoc} */
    @Override
    public boolean isSigned(int pColumn) throws SQLException
    {
        return bool(pColumn, 5);
    }

    /** {@inheritDoc} */
    @Override
    public int getColumnDisplaySize(int pColumn) throws SQLException
    {
        return integer(pColumn, 6);
    }

    /** {@inheritDoc} */
    @Override
    public String getColumnLabel(int pColumn) throws SQLException
    {
        return string(pColumn, 7);
    }

    /** {@inheritDoc} */
    @Override
    public String getColumnName(int pColumn) throws SQLException
    {
        return string(pColumn, 8);
    }

    /** {@inheritDoc} */
    @Override
    public String getSchemaName(int pColumn) throws SQLException
    {
        return string(pColumn, 9);
    }

    /** {@inheritDoc} */
    @Override
    public int getPrecision(int pColumn) throws SQLException
    {
        return integer(pColumn, 10);
    }

    /** {@inheritDoc} */
    @Override
    public int getScale(int pColumn) throws SQLException
    {
        return integer(pColumn, 11);
    }

    /** {@inheritDoc} */
    @Override
    public String getTableName(int pColumn) throws SQLException
    {
        return string(pColumn, 12);
    }

    /** {@inheritDoc} */
    @Override
    public String getCatalogName(int pColumn) throws SQLException
    {
        return string(pColumn, 13);
    }

    /** {@inheritDoc} */
    @Override
    public int getColumnType(int pColumn) throws SQLException
    {
        return integer(pColumn, 14);
    }

    /** {@inheritDoc} */
    @Override
    public String getColumnTypeName(int pColumn) throws SQLException
    {
        return string(pColumn, 15);
    }

    /** {@inheritDoc} */
    @Override
    public boolean isReadOnly(int pColumn) throws SQLException
    {
        return bool(pColumn, 16);
    }

    /** {@inheritDoc} */
    @Override
    public boolean isWritable(int pColumn) throws SQLException
    {
        return bool(pColumn, 17);
    }

    /** {@inheritDoc} */
    @Override
    public boolean isDefinitelyWritable(int pColumn) throws SQLException
    {
        return bool(pColumn, 18);
    }

    /** {@inheritDoc} */
    @Override
    public String getColumnClassName(int pColumn) throws SQLException
    {
        return string(pColumn, 19);
    }

	/**
	 * Handles the load operation for the remote JDBC resource.
	 * 
	 * @throws SQLException if the operation fails
	 */
    private void load() throws SQLException
    {
        if (columns != null)
        {
            return;
        }
        
        synchronized (this)
        {
            if (columns != null)
            {
                return;
            }
            
            Map<String, Object> request = new HashMap<String, Object>();
            request.put(RemoteConstants.ACTION, RemoteConstants.GET_RESULTSET_METADATA_INFO);
            request.put(RemoteConstants.ID, id);
            
            Map<String, Object> result = client.call(request);
            
            count = ((Number)result.get("count")).intValue();
            Object raw = result.get("columns");

            if (raw instanceof Object[][]) columns = (Object[][])raw;
            
            else if (raw instanceof Object[])
            {
                Object[] values = (Object[])raw;
                
                columns = new Object[values.length][];
                
                for (int i = 0; i < values.length; i++) 
                {
                	columns[i] = values[i] instanceof Object[] ? (Object[])values[i] : new Object[0];
                }
            }
            else
            {
                columns = new Object[0][0];
            }

            if (count != columns.length)
            {
                throw new SQLException("Invalid ResultSetMetaData snapshot");
            }
        }
    }

    /**
     * Reads a column value from the current remote result-set row.
     *
     * @param pIndex the index
     * @param pSlot the slot
     * @return the resulting JDBC value
     * @throws SQLException if the JDBC operation cannot be completed
     */
    private Object value(int pIndex, int pSlot) throws SQLException
    {
        load();

        if (pIndex < 1 || pIndex > count)
        {
            throw new SQLException("Invalid column index: " + pIndex);
        }

        return columns[pIndex - 1][pSlot];
    }

    /**
     * Handles the boolean operation for the remote JDBC resource.
     *
     * @param pIndex the index
     * @param pSlot the slot
     * @return the resulting JDBC value
     * @throws SQLException if the JDBC operation cannot be completed
     */
    private boolean bool(int pIndex, int pSlot) throws SQLException
    {
        return ((Boolean)value(pIndex, pSlot)).booleanValue();
    }

    /**
     * Handles the integer operation for the remote JDBC resource.
     *
     * @param pIndex the index
     * @param pSlot the slot
     * @return the resulting JDBC value
     * @throws SQLException if the JDBC operation cannot be completed
     */
    private int integer(int pIndex, int pSlot) throws SQLException
    {
        return ((Number)value(pIndex, pSlot)).intValue();
    }

    /**
     * Handles the string operation for the remote JDBC resource.
     *
     * @param pIndex the index
     * @param pSlot the slot
     * @return the resulting JDBC value
     * @throws SQLException if the JDBC operation cannot be completed
     */
    private String string(int pIndex, int pSlot) throws SQLException
    {
        Object v = value(pIndex, pSlot); 
        
        return v == null ? null : String.valueOf(v);
    }
}
