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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.sql.Array;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.NClob;
import java.sql.ParameterMetaData;
import java.sql.PreparedStatement;
import java.sql.Ref;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.RowId;
import java.sql.SQLException;
import java.sql.SQLXML;
import java.sql.Savepoint;
import java.sql.Statement;
import java.sql.Struct;
import java.sql.Types;
import java.util.HashMap;
import java.util.Map;

import com.sibvisions.rjdbc.server.JdbcContext;
import com.sibvisions.util.xml.XmlWorker;

/**
 * Handles the remote result set value operation for the remote JDBC resource.
 * 
 * @author René Jahn
 */
public abstract class JdbcDelegate
{
    protected final JdbcContext context;

    
    /**
     * Creates a new {@code JdbcDelegate} instance.
     *
     * @param pContext the context
     */
    public JdbcDelegate(JdbcContext pContext)
    {
        context = pContext;
    }

    /**
     * Handles the remote result set value operation for the remote JDBC resource.
     *
     * @param pId the remote resource identifier
     * @param pSignature the signature
     * @param pArgs the method arguments
     * @return the resulting JDBC value
     * @throws SQLException if the JDBC operation cannot be completed
     */
    public abstract Object call(long pId, String pSignature, Object[] pArgs) throws SQLException;

    /**
     * Converts a remote result-set value to the representation expected by the JDBC client.
     *
     * @param pResultSet the result set
     * @param pColumnIndex the column index
     * @return the resulting JDBC value
     * @throws SQLException if the JDBC operation cannot be completed
     */
    protected Object remoteResultSetValue(ResultSet pResultSet, int pColumnIndex) throws SQLException
    {
        return remoteResultSetValue(pResultSet, pResultSet.getMetaData(), pColumnIndex, pResultSet.getObject(pColumnIndex));
    }

    /**
     * Handles the remote result set value operation for the remote JDBC resource.
     *
     * @param pResultSet the result set
     * @param pColumnIndex the column index
     * @param pValue the value to convert
     * @return the resulting JDBC value
     * @throws SQLException if the JDBC operation cannot be completed
     */
    protected Object remoteResultSetValue(ResultSet pResultSet, int pColumnIndex, Object pValue) throws SQLException
    {
        return remoteResultSetValue(pResultSet, pResultSet.getMetaData(), pColumnIndex, pValue);
    }

    /**
     * Converts a remote result-set value using already retrieved result-set metadata.
     *
     * @param pResultSet the result set
     * @param pMetaData the result-set metadata
     * @param pColumnIndex the column index
     * @param pValue the value to convert
     * @return the resulting JDBC value
     * @throws SQLException if the JDBC operation cannot be completed
     */
    protected Object remoteResultSetValue(ResultSet pResultSet, ResultSetMetaData pMetaData, int pColumnIndex, Object pValue) throws SQLException
    {
        return remoteResultSetValue(pResultSet, pColumnIndex, pValue, pMetaData.getColumnType(pColumnIndex), pMetaData.getColumnTypeName(pColumnIndex));
    }

    /**
     * Converts a remote result-set value using pre-computed column metadata.
     *
     * @param pResultSet the result set
     * @param pColumnIndex the column index
     * @param pValue the value to convert
     * @param pSqlType the SQL type of the column
     * @param pTypeName the database type name of the column
     * @return the resulting JDBC value
     * @throws SQLException if the JDBC operation cannot be completed
     */
    public Object remoteResultSetValue(ResultSet pResultSet, int pColumnIndex, Object pValue, int pSqlType, String pTypeName) throws SQLException
    {
        int sqlType = pSqlType;
        
        String typeName = pTypeName;

        // Always transport standard JDBC temporal types. Some drivers (for
        // example Oracle) return vendor-specific temporal classes from
        // getObject(), which cannot be consumed by the generic client.

        if (pValue != null)
        {
            switch (sqlType)
            {
                case Types.DATE:
                    if (!(pValue instanceof java.sql.Date))
                    {
                    	pValue = pResultSet.getDate(pColumnIndex);
                    }
                    
                    break;
                case Types.TIME:
                    if (!(pValue instanceof java.sql.Time))
                    {
                    	pValue = pResultSet.getTime(pColumnIndex);

                    }
                    
                    break;
                case Types.TIMESTAMP:
                case Types.TIMESTAMP_WITH_TIMEZONE:
                    if (!(pValue instanceof java.sql.Timestamp))
                    {
                    	pValue = pResultSet.getTimestamp(pColumnIndex);

                    }
                    
                    break;
                    
                default:
                    break;
            }
        }

        if (isVectorType(typeName))
        {
            return JdbcArrayAdapter.fromVector(pValue, typeName);
        }

        if (pValue instanceof Array)
        {
            Array array = (Array)pValue;
            
            Map<String,Object> result = jdbcObject("Array", context.idFor(array));
            result.put("array", array.getArray());

            return result;
        }

        if (pValue instanceof NClob)
        {
            NClob nclob = (NClob)pValue;
            
            long length = nclob.length();

            if (context.getClobPrefetchSize() > 0
                && length <= context.getClobPrefetchSize()
                && length <= Integer.MAX_VALUE)
            {
                Map<String,Object> result = jdbcObject("NClob", context.idFor(nclob));
                result.put("clob", nclob.getSubString(1L, (int)length));

                return result;
            }
        }

        if (pValue instanceof Clob)
        {
            Clob clob = (Clob)pValue;
            
            long length = clob.length();

            if (context.getClobPrefetchSize() > 0
            	&& length <= context.getClobPrefetchSize()
            	&& length <= Integer.MAX_VALUE)
            {
                Map<String,Object> result = jdbcObject("Clob", context.idFor(clob));
                result.put("clob", clob.getSubString(1L, (int)length));

                return result;
            }

        }

        if (pValue instanceof SQLXML)
        {
            SQLXML sqlxml = (SQLXML)pValue;
            
            Map<String,Object> result = jdbcObject("SQLXML", context.idFor(sqlxml));
            String xml = sqlxml.getString();
            
            try
            {
                result.put("xmlNode", XmlWorker.readNode(new ByteArrayInputStream(xml.getBytes("UTF-8"))));
            }
            catch (Exception e)
            {
                throw new SQLException("Could not serialize SQLXML as XmlNode", e);
            }

            return result;
        }

        return remoteValue(pValue);
    }

    /**
     * Returns whether vector type.
     *
     * @param pTypeName the type name
     * @return whether the condition is true
     */
    protected static boolean isVectorType(String pTypeName)
    {
        return "oidvector".equalsIgnoreCase(pTypeName) || "int2vector".equalsIgnoreCase(pTypeName);
    }

    /**
     * Creates JDBC object definition for client.
     *
     * @param pValue the value to convert
     * @return the resulting JDBC value
     * @throws SQLException if the JDBC operation cannot be completed
     */
    protected Object remoteValue(Object pValue) throws SQLException
    {
        if (pValue == null)
        {
            return null;
        }

        if (pValue instanceof Connection)
        {
            return jdbcObject("Connection", context.idFor(pValue));
        }
        else if (pValue instanceof CallableStatement)
        {
            return jdbcObject("CallableStatement", context.idFor(pValue));
        }
        else if (pValue instanceof PreparedStatement)
        {
            return jdbcObject("PreparedStatement", context.idFor(pValue));
        }
        else if (pValue instanceof Statement)
        {
            return jdbcObject("Statement", context.idFor(pValue));
        }
        else if (pValue instanceof ResultSet)
        {
            return jdbcObject("ResultSet", context.idFor(pValue));
        }
        else if (pValue instanceof ResultSetMetaData)
        {
            return jdbcObject("ResultSetMetaData", context.idFor(pValue));
        }
        else if (pValue instanceof DatabaseMetaData)
        {
            return jdbcObject("DatabaseMetaData", context.idFor(pValue));
        }
        else if (pValue instanceof Blob)
        {
            return jdbcObject("Blob", context.idFor(pValue));
        }
        else if (pValue instanceof NClob)
        {
            return jdbcObject("NClob", context.idFor(pValue));
        }
        else if (pValue instanceof Clob)
        {
            return jdbcObject("Clob", context.idFor(pValue));
        }
        else if (pValue instanceof SQLXML)
        {
            return jdbcObject("SQLXML", context.idFor(pValue));
        }
        else if (pValue instanceof Array)
        {
            return jdbcObject("Array", context.idFor(pValue));
        }
        else if (pValue instanceof Struct)
        {
            return jdbcObject("Struct", context.idFor(pValue));
        }
        else if (pValue instanceof ParameterMetaData)
        {
            return jdbcObject("ParameterMetaData", context.idFor(pValue));
        }
        else if (pValue instanceof Ref)
        {
            return jdbcObject("Ref", context.idFor(pValue));
        }
        else if (pValue instanceof RowId)
        {
            return jdbcObject("RowId", context.idFor(pValue));
        }
        else if (pValue instanceof Savepoint)
        {
            return jdbcObject("Savepoint", context.idFor(pValue));
        }
        else if (pValue instanceof InputStream)
        {
            return readBytes((InputStream)pValue);
        }
        else if (pValue instanceof Reader)
        {
            return readChars((Reader)pValue);
        }
        else
        {
        	return pValue;
        }
    }

    /**
     * Converts a protocol value to a numeric value.
     *
     * @param pValue the value to convert
     * @return the resulting JDBC value
     */
    protected static long number(Object pValue)
    {
        if (pValue instanceof Number)
        {
            return ((Number)pValue).longValue();
        }

        if (pValue instanceof Map)
        {
            Object id = ((Map<?, ?>)pValue).get("id");

            if (id instanceof Number)
            {
                return ((Number)id).longValue();
            }
        }
        
        throw new IllegalArgumentException("Expected JDBC object id: " + pValue);
    }

    /**
     * Handles the jdbc object operation for the remote JDBC resource.
     *
     * @param pType the requested Java type
     * @param pId the remote resource identifier
     * @return the resulting JDBC value
     */
    protected static Map<String,Object> jdbcObject(String pType, long pId)
    {
        Map<String,Object> result = new HashMap<>();
        result.put("jdbcObject", pType);
        result.put("id", pId);

        return result;
    }

    /**
     * Handles the stream operation for the remote JDBC resource.
     *
     * @param pKind the kind
     * @param pId the remote resource identifier
     * @param pPosition the position
     * @return the resulting JDBC value
     */
    protected static Map<String,Object> stream(String pKind, long pId, Object pPosition)
    {
        Map<String,Object> result = new HashMap<>();
        result.put("streamKind", pKind);
        result.put("id", pId);

        if (pPosition != null)
        {
            result.put("position", pPosition);

        }

        return result;
    }

    /**
     * Converts a protocol value to an object array.
     *
     * @param pValue the value to convert
     * @return the resulting JDBC value
     */
    protected static Object[] objectArray(Object pValue)
    {
        return pValue == null ? new Object[0] : (Object[])pValue;
    }

    /**
     * Converts a protocol value to a string array.
     *
     * @param pValue the value to convert
     * @return the resulting JDBC value
     */
    protected static String[] stringArray(Object pValue)
    {
        if (pValue == null)
        {
            return new String[0];
        }

        if (pValue instanceof String[])
        {
            return (String[])pValue;
        }
        
        Object[] values = (Object[])pValue;
        String[] result = new String[values.length];
        
        for (int i=0; i < values.length; i++)
        {
        	result[i] = String.valueOf(values[i]);
        }

        return result;
    }

    /**
     * Handles the input stream operation for the remote JDBC resource.
     *
     * @param pValue the value to convert
     * @return the resulting JDBC value
     */
    protected static InputStream inputStream(Object pValue)
    {
        if (pValue instanceof InputStream)
        {
            return (InputStream)pValue;
        }

        if (pValue instanceof byte[])
        {
            return new ByteArrayInputStream((byte[])pValue);
        }

        return null;
    }

    /**
     * Reads r from the remote JDBC resource.
     *
     * @param pValue the value to convert
     * @return the resulting JDBC value
     */
    protected static Reader reader(Object pValue)
    {
        if (pValue instanceof Reader)
        {
            return (Reader)pValue;
        }

        if (pValue instanceof String)
        {
            return new StringReader((String)pValue);
        }

        return null;
    }

    /**
     * Reads bytes from the given input stream.
     *
     * @param pInput the input
     * @return the resulting JDBC value
     * @throws SQLException if reading fails
     */
    public static byte[] readBytes(InputStream pInput) throws SQLException
    {
        try
        {
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            
            byte[] buffer = new byte[8192];
            
            int count;
            
            while ((count = pInput.read(buffer)) >= 0)
            {
            	output.write(buffer, 0, count);
            }

            return output.toByteArray();
        }
        catch (IOException e)
        {
            throw new SQLException(e);
        }
    }

    /**
     * Reads chars from the given reader.
     *
     * @param pReader the reader
     * @return the resulting JDBC value
     * @throws SQLException if reading fails
     */
    public static String readChars(Reader pReader) throws SQLException
    {
        try
        {
            StringBuilder output = new StringBuilder();
            
            char[] buffer = new char[8192];
            
            int count;
            
            while ((count = pReader.read(buffer)) >= 0)
            {
            	output.append(buffer, 0, count);
            }

            return output.toString();
        }
        catch (IOException e)
        {
            throw new SQLException(e);
        }
    }
}
