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
package com.sibvisions.rjdbc.server.delegate;

import java.sql.Array;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Arrays;
import java.util.Map;

import javax.sql.rowset.CachedRowSet;
import javax.sql.rowset.RowSetMetaDataImpl;

/**
 * JDBC Array adapter for database-specific vector types which are not exposed
 * by the server-side JDBC driver as java.sql.Array.
 *
 * This is deliberately a bean-style POJO so it can also be handled by the
 * UniversalSerializer if the object is ever serialized directly. The JDBC
 * transport normally exposes it through the existing typed JDBC-object
 * protocol, however.
 *
 * Array elements are stored as Object[] rather than primitive arrays.
 * 
 * @author René Jahn
 */
class JdbcArrayAdapter implements Array
{
    private Object[] values;
    
    private String baseTypeName;
    
    private int baseType;
    

    /** 
     * Required constructor for serialization. 
     */
    public JdbcArrayAdapter()
    {
    }

    /**
     * Creates a new {@code JdbcArrayAdapter} instance.
     *
     * @param pValues the values
     * @param pBaseTypeName the base type name
     * @param pBaseType the base type
     */
    public JdbcArrayAdapter(Object[] pValues, String pBaseTypeName, int pBaseType)
    {
        values = pValues == null ? new Object[0] : pValues.clone();
        
        baseTypeName = pBaseTypeName;
        baseType = pBaseType;
    }

    /** {@inheritDoc} */
    @Override
    public String getBaseTypeName() throws SQLException
    {
        return baseTypeName;
    }

    /** {@inheritDoc} */
    @Override
    public int getBaseType() throws SQLException
    {
        return baseType;
    }

    /** {@inheritDoc} */
    @Override
    public Object getArray() throws SQLException
    {
        return values().clone();
    }

    /** {@inheritDoc} */
    @Override
    public Object getArray(Map<String, Class<?>> pMap) throws SQLException
    {
        return getArray();
    }

    /** {@inheritDoc} */
    @Override
    public Object getArray(long pIndex, int pCount) throws SQLException
    {
        Object[] source = values();
        
        int from = checkedSlice(pIndex, pCount, source.length);

        return Arrays.copyOfRange(source, from, from + pCount);
    }

    /** {@inheritDoc} */
    @Override
    public Object getArray(long pIndex, int pCount, Map<String, Class<?>> pMap) throws SQLException
    {
        return getArray(pIndex, pCount);
    }

    /** {@inheritDoc} */
    @Override
    public ResultSet getResultSet() throws SQLException
    {
        return getResultSet(1, values().length);
    }

    /** {@inheritDoc} */
    @Override
    public ResultSet getResultSet(Map<String, Class<?>> pMap) throws SQLException
    {
        return getResultSet();
    }

    /** {@inheritDoc} */
    @Override
    public ResultSet getResultSet(long pIndex, int pCount) throws SQLException
    {
        Object[] source = values();
        
        int from = checkedSlice(pIndex, pCount, source.length);

        RowSetMetaDataImpl meta = new RowSetMetaDataImpl();
        meta.setColumnCount(2);
        meta.setColumnType(1, Types.INTEGER);
        meta.setColumnTypeName(1, "INTEGER");
        meta.setColumnName(1, "INDEX");
        meta.setColumnLabel(1, "INDEX");
        meta.setNullable(1, java.sql.ResultSetMetaData.columnNoNulls);
        meta.setColumnType(2, baseType);
        meta.setColumnTypeName(2, baseTypeName);
        meta.setColumnName(2, "VALUE");
        meta.setColumnLabel(2, "VALUE");
        meta.setNullable(2, java.sql.ResultSetMetaData.columnNullable);

        CachedRowSet result = javax.sql.rowset.RowSetProvider.newFactory().createCachedRowSet();
        result.setMetaData(meta);

        for (int i = 0; i < pCount; i++)
        {
            result.moveToInsertRow();
            result.updateInt(1, (int)pIndex + i);
            result.updateObject(2, source[from + i]);
            result.insertRow();
            result.moveToCurrentRow();
        }
        
        result.beforeFirst();

        return result;
    }

    /** {@inheritDoc} */
    @Override
    public ResultSet getResultSet(long pIndex, int pCount, Map<String, Class<?>> pMap) throws SQLException
    {
        return getResultSet(pIndex, pCount);
    }

    /** {@inheritDoc} */
    @Override
    public void free() throws SQLException
    {
        values = null;
    }
    
    /**
     * Handles the from vector operation for the remote JDBC resource.
     *
     * @param pValue the value to convert
     * @param pTypeName the type name
     * @return the resulting JDBC value
     * @throws SQLException if the JDBC operation cannot be completed
     */
    static Array fromVector(Object pValue, String pTypeName) throws SQLException
    {
        if (pValue == null)
        {
            return null;
        }

        String text = String.valueOf(pValue).trim();
        String normalizedTypeName = pTypeName == null ? "" : pTypeName.trim();

        if (text.isEmpty())
        {
            return new JdbcArrayAdapter(new Object[0], baseTypeName(normalizedTypeName), baseType(normalizedTypeName));
        }

        String[] parts = text.split("\\s+");
        Object[] result = new Object[parts.length];
        
        boolean smallInt = "int2vector".equalsIgnoreCase(normalizedTypeName);
        
        for (int i = 0; i < parts.length; i++)
        {
            try
            {
                result[i] = smallInt ? Short.valueOf(parts[i]) : Long.valueOf(parts[i]);
            }
            catch (NumberFormatException e)
            {
                throw new SQLException("Invalid " + normalizedTypeName + " value: " + text, e);
            }
        }

        return new JdbcArrayAdapter(result, baseTypeName(normalizedTypeName), baseType(normalizedTypeName));
    }    

    /**
     * Returns values.
     * 
     * @return the resulting JDBC value
     * @throws SQLException if the JDBC operation cannot be completed
     */
    Object[] getValues() throws SQLException
    {
        return values().clone();
    }

    /**
     * Sets values.
     *
     * @param pValues the values
     */
    void setValues(Object[] pValues)
    {
        values = pValues == null ? new Object[0] : pValues.clone();
    }

    /**
     * Sets base type name.
     *
     * @param pBaseTypeName the base type name
     */
    void setBaseTypeName(String pBaseTypeName)
    {
        baseTypeName = pBaseTypeName;
    }

    /**
     * Sets base type.
     *
     * @param pBaseType the base type
     */
    void setBaseType(int pBaseType)
    {
        baseType = pBaseType;
    }

    /**
     * Handles the base type name operation for the remote JDBC resource.
     *
     * @param pTypeName the type name
     * @return the resulting JDBC value
     */
    private static String baseTypeName(String pTypeName)
    {
        return "int2vector".equalsIgnoreCase(pTypeName) ? "int2" : "oid";
    }

    /**
     * Handles the base type operation for the remote JDBC resource.
     *
     * @param pTypeName the type name
     * @return the resulting JDBC value
     */
    private static int baseType(String pTypeName)
    {
        return "int2vector".equalsIgnoreCase(pTypeName) ? Types.SMALLINT : Types.BIGINT;
    }

    /**
     * Handles the values operation for the remote JDBC resource.
     * 
     * @return the resulting JDBC value
     * @throws SQLException if the JDBC operation cannot be completed
     */
	private Object[] values() throws SQLException
    {
        if (values == null)
        {
            throw new SQLException("Array has been freed");
        }

        return values;
    }

    /**
     * Checks slice for the current JDBC operation.
     *
     * @param pIndex the index
     * @param pCount the count
     * @param pLength the length
     * @return the resulting JDBC value
     * @throws SQLException if the JDBC operation cannot be completed
     */
	private static int checkedSlice(long pIndex, int pCount, int pLength) throws SQLException
    {
        if (pIndex < 1 || pIndex > pLength || pIndex > Integer.MAX_VALUE)
        {
            throw new SQLException("Invalid array index: " + pIndex);
        }

        if (pCount < 0)
        {
            throw new SQLException("Invalid array count: " + pCount);
        }

        long end = pIndex - 1L + pCount;

        if (end > pLength)
        {
            throw new SQLException("Array slice exceeds array length: index=" + pIndex + ", count=" + pCount);

        }

        return (int)pIndex - 1;
    }
}
