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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
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
import java.sql.SQLClientInfoException;
import java.sql.SQLException;
import java.sql.SQLXML;
import java.sql.Savepoint;
import java.sql.Statement;
import java.sql.Struct;
import java.util.HashMap;
import java.util.Map;

import com.sibvisions.util.type.CommonUtil;

/**
 * Utility class for remote JDBC implementation functionality.
 * 
 * @author René Jahn
 */
final class RemoteUtil
{
    /**
     * Creates a new {@code RemoteUtil} instance.
     */
    private RemoteUtil()
    {
    }

	/**
	 * Handles the invoke operation for the remote JDBC resource.
	 *
	 * @param pClient          the client
	 * @param pId              the remote resource identifier
	 * @param pRemoteInterface the remote interface
	 * @param pMethod          the method
	 * @param pParameterTypes  the parameter types
	 * @param pArgs            the method arguments
	 * @param pReturnType      the return type
	 * @return the resulting JDBC value
	 * @throws SQLException if the JDBC operation cannot be completed
	 */
	static Object invoke(RemoteClient pClient, long pId, Class<?> pRemoteInterface,
					     String pMethod, Class<?>[] pParameterTypes, Object[] pArgs,
					     Class<?> pReturnType) throws SQLException
    {
        String[] names = new String[pParameterTypes.length];
        
        for (int i=0; i<pParameterTypes.length; i++) 
        {
        	names[i] = pParameterTypes[i].getName();
        }

        Map<String,Object> req = new HashMap<String,Object>();
        req.put(RemoteConstants.ACTION, RemoteConstants.GENERIC_CALL);
        req.put(RemoteConstants.ID, pId);
        req.put(RemoteConstants.INTERFACE, pRemoteInterface.getName());
        req.put(RemoteConstants.METHOD, pMethod);
        req.put(RemoteConstants.PARAMETER_TYPES, names);
        req.put(RemoteConstants.VALUE, encodeArguments(pArgs));

        Object value = pClient.call(req).get(RemoteConstants.RESULT);

        return adapt(pClient, value, pReturnType);
    }

    /**
     * Handles the invoke and record operation for the remote JDBC resource.
     *
     * @param pClient the client
     * @param pId the remote resource identifier
     * @param pRemoteInterface the remote interface
     * @param pMethod the method
     * @param pParameterTypes the parameter types
     * @param pArgs the method arguments
     * @param pReturnType the return type
     * @param pRecorder the recorder
     * @return the resulting JDBC value
     * @throws SQLException if the JDBC operation cannot be completed
     */
	static Object invokeAndRecord(RemoteClient pClient, long pId, Class<?> pRemoteInterface,
							      String pMethod, Class<?>[] pParameterTypes, Object[] pArgs,
							      Class<?> pReturnType, java.util.List<Map<String,Object>> pRecorder) throws SQLException
    {
        String[] names = new String[pParameterTypes.length];
    
        for (int i = 0; i < pParameterTypes.length; i++) 
    	{
        	names[i] = pParameterTypes[i].getName();
    	}
        
        Object[] encoded = encodeArguments(pArgs);
        
        Map<String,Object> operation = new HashMap<String,Object>();
        operation.put(RemoteConstants.METHOD, pMethod);
        operation.put(RemoteConstants.PARAMETER_TYPES, names);
        operation.put(RemoteConstants.VALUE, encoded);
        
        pRecorder.add(operation);

        Map<String,Object> req = new HashMap<String,Object>();
        req.put(RemoteConstants.ACTION, RemoteConstants.GENERIC_CALL);
        req.put(RemoteConstants.ID, pId);
        req.put(RemoteConstants.INTERFACE, pRemoteInterface.getName());
        req.put(RemoteConstants.METHOD, pMethod);
        req.put(RemoteConstants.PARAMETER_TYPES, names);
        req.put(RemoteConstants.VALUE, encoded);

        Object value = pClient.call(req).get(RemoteConstants.RESULT);

        return adapt(pClient, value, pReturnType);}

    /**
     * Handles the invoke unchecked operation for the remote JDBC resource.
     *
     * @param pClient the client
     * @param pId the remote resource identifier
     * @param pRemoteInterface the remote interface
     * @param pMethod the method
     * @param pParameterTypes the parameter types
     * @param pArgs the method arguments
     * @param pReturnType the return type
     * @return the resulting JDBC value
     */
	static Object invokeUnchecked(RemoteClient pClient, long pId, Class<?> pRemoteInterface,
							      String pMethod, Class<?>[] pParameterTypes, Object[] pArgs,
							      Class<?> pReturnType)
    {
        try
        {
            return invoke(pClient, pId, pRemoteInterface, pMethod, pParameterTypes, pArgs, pReturnType);
        }
        catch (SQLException e)
        {
            throw new IllegalStateException("Remote JDBC call failed: " + pMethod, e);
        }
    }

    /**
     * Handles the invoke client info operation for the remote JDBC resource.
     *
     * @param pClient the client
     * @param pId the remote resource identifier
     * @param pRemoteInterface the remote interface
     * @param pMethod the method
     * @param pParameterTypes the parameter types
     * @param pArgs the method arguments
     * @param pReturnType the return type
     * @return the resulting JDBC value
     * @throws SQLClientInfoException if the operation fails
     */
	static Object invokeClientInfo(RemoteClient pClient, long pId, Class<?> pRemoteInterface,
								   String pMethod, Class<?>[] pParameterTypes, Object[] pArgs,
								   Class<?> pReturnType) throws SQLClientInfoException
    {
        try
        {
            return invoke(pClient, pId, pRemoteInterface, pMethod, pParameterTypes, pArgs, pReturnType);
        }
        catch (SQLClientInfoException e)
        {
            throw e;
        }
        catch (SQLException e)
        {
            SQLClientInfoException x = new SQLClientInfoException();
            x.initCause(e);
            throw x;
        }
    }

    /**
     * Handles the adapt operation for the remote JDBC resource.
     *
     * @param pClient the client
     * @param pValue the value to convert
     * @param pReturnType the return type
     * @return the resulting JDBC value
     * @throws SQLException if the JDBC operation cannot be completed
     */
	@SuppressWarnings("unchecked")
	static Object adapt(RemoteClient pClient, Object pValue, Class<?> pReturnType) throws SQLException
    {
        if (pValue == null)
        {
            return null;
        }

        if (pValue instanceof Map)
        {
            Map<?, ?> object = (Map<?, ?>)pValue;
            
            Object type = object.get("jdbcObject");

            if (type != null)
            {
                long id = number(object.get("id"));
                
                String name = String.valueOf(type);

                if ("Connection".equals(name))
                {
                    return new RemoteConnection(pClient, id);
                }

                if ("Statement".equals(name))
                {
                    return new RemoteStatement(pClient, id);
                }

                if ("PreparedStatement".equals(name))
                {
                    return new RemotePreparedStatement(pClient, id);
                }

                if ("CallableStatement".equals(name))
                {
                    return new RemoteCallableStatement(pClient, id);
                }

                if ("ResultSet".equals(name))
                {
                    return new RemoteResultSet(pClient, id);
                }

                if ("ResultSetMetaData".equals(name))
                {
                    return new RemoteResultSetMetaData(pClient, id);
                }

                if ("DatabaseMetaData".equals(name))
                {
                    return new RemoteDatabaseMetaData(pClient, id);
                }

                if ("Blob".equals(name))
                {
                    return new RemoteBlob(pClient, id);
                }

                if ("Clob".equals(name))
                {
                    Object cachedClob = object.get("clob");

                    if (object.containsKey("clob"))
                    {
                        return new RemoteClob(pClient, id, (String)cachedClob, true);
                    }

                    return new RemoteClob(pClient, id);
                }

                if ("NClob".equals(name))
                {
                    Object cachedNClob = object.get("clob");

                    if (object.containsKey("clob"))
                    {
                        return new RemoteNClob(pClient, id, (String)cachedNClob, true);
                    }

                    return new RemoteNClob(pClient, id);
                }

                if ("SQLXML".equals(name))
                {
                    Object node = object.get("xmlNode");

                    if (node instanceof com.sibvisions.util.xml.XmlNode)
                    {
                        return new RemoteSQLXML(pClient, id, (com.sibvisions.util.xml.XmlNode) node, true);
                    }

                    return new RemoteSQLXML(pClient, id);
                }

                if ("Array".equals(name))
                {
                    Object cachedArray = object.get("array");

                    if (object.containsKey("array"))
                    {
                        return new RemoteArray(pClient, id, cachedArray, true);
                    }

                    return new RemoteArray(pClient, id);
                }

                if ("Struct".equals(name))
                {
                    return new RemoteStruct(pClient, id);
                }

                if ("ParameterMetaData".equals(name))
                {
                    return new RemoteParameterMetaData(pClient, id);
                }

                if ("Ref".equals(name))
                {
                    return new RemoteRef(pClient, id);
                }

                if ("RowId".equals(name))
                {
                    return new RemoteRowId(pClient, id);
                }

                if ("Savepoint".equals(name))
                {
                    return new RemoteSavepoint(pClient, id);
                }

                if ("Executor".equals(name))
                {
                    return new RemoteExecutor(pClient, id, java.util.concurrent.ForkJoinPool.commonPool());
                }
            }
        }

        if (pReturnType == Connection.class)
        {
            return new RemoteConnection(pClient, number(pValue));
        }

        if (pReturnType == Statement.class)
        {
            return new RemoteStatement(pClient, number(pValue));
        }

        if (pReturnType == PreparedStatement.class)
        {
            return new RemotePreparedStatement(pClient, number(pValue));
        }

        if (pReturnType == CallableStatement.class)
        {
            return new RemoteCallableStatement(pClient, number(pValue));
        }

        if (pReturnType == ResultSet.class)
        {
            return new RemoteResultSet(pClient, number(pValue));
        }

        if (pReturnType == ResultSetMetaData.class)
        {
            return new RemoteResultSetMetaData(pClient, number(pValue));
        }

        if (pReturnType == DatabaseMetaData.class)
        {
            return new RemoteDatabaseMetaData(pClient, number(pValue));
        }

        if (pReturnType == Blob.class)
        {
            return new RemoteBlob(pClient, number(pValue));
        }

        if (pReturnType == Clob.class)
        {
            return new RemoteClob(pClient, number(pValue));
        }

        if (pReturnType == NClob.class)
        {
            return new RemoteNClob(pClient, number(pValue));
        }

        if (pReturnType == SQLXML.class)
        {
            return new RemoteSQLXML(pClient, number(pValue));
        }

        if (pReturnType == Array.class)
        {
            return new RemoteArray(pClient, number(pValue));
        }

        if (pReturnType == Struct.class)
        {
            return new RemoteStruct(pClient, number(pValue));
        }

        if (pReturnType == ParameterMetaData.class)
        {
            return new RemoteParameterMetaData(pClient, number(pValue));
        }

        if (pReturnType == Ref.class)
        {
            return new RemoteRef(pClient, number(pValue));
        }

        if (pReturnType == RowId.class)
        {
            return new RemoteRowId(pClient, number(pValue));
        }

        if (pReturnType == Savepoint.class)
        {
            return new RemoteSavepoint(pClient, number(pValue));
        }

        if (pReturnType == java.util.concurrent.Executor.class)
        {
            return new RemoteExecutor(pClient, number(pValue), java.util.concurrent.ForkJoinPool.commonPool());
        }

        if (pReturnType == InputStream.class && pValue instanceof byte[])
        {
            return new ByteArrayInputStream((byte[]) pValue);
        }

        if (pReturnType == Reader.class && pValue instanceof String)
        {
            return new StringReader((String)pValue);
        }

        if (pReturnType == OutputStream.class && pValue instanceof Map)
        {
            return new RemoteOutputStream(pClient, (Map<String,Object>)pValue);
        }

        if (pReturnType == Writer.class && pValue instanceof Map)
        {
            return new RemoteWriter(pClient, (Map<String,Object>)pValue);
        }

        return pValue;
    }

    /**
     * Encodes arguments for batch for transport through the remote JDBC protocol.
     *
     * @param pArgs the method arguments
     * @return the resulting JDBC value
     * @throws SQLException if the JDBC operation cannot be completed
     */
	static Object[] encodeArgumentsForBatch(Object[] pArgs) throws SQLException
    {
        return encodeArguments(pArgs);
    }

    /**
     * Encodes arguments for transport through the remote JDBC protocol.
     *
     * @param pArgs the method arguments
     * @return the resulting JDBC value
     * @throws SQLException if the JDBC operation cannot be completed
     */
	private static Object[] encodeArguments(Object[] pArgs) throws SQLException
    {
        if (pArgs == null || pArgs.length == 0)
        {
            return new Object[0];

        }
        Object[] encoded = new Object[pArgs.length];
        
        for (int i = 0; i < pArgs.length; i++)
        {
        	encoded[i] = encodeArgument(pArgs[i]);
        }

        return encoded;
    }

    /**
     * Encodes argument for transport through the remote JDBC protocol.
     *
     * @param pValue the value to convert
     * @return the resulting JDBC value
     * @throws SQLException if the JDBC operation cannot be completed
     */
	private static Object encodeArgument(Object pValue) throws SQLException
    {
        if (pValue == null)
        {
            return null;
        }

        if (pValue instanceof RemoteConnection)
        {
            return ((RemoteConnection)pValue).id;
        }

        if (pValue instanceof RemoteCallableStatement)
        {
            return ((RemoteCallableStatement)pValue).id;
        }

        if (pValue instanceof RemotePreparedStatement)
        {
            return ((RemotePreparedStatement)pValue).id;
        }

        if (pValue instanceof RemoteStatement)
        {
            return ((RemoteStatement)pValue).id;
        }

        if (pValue instanceof RemoteResultSet)
        {
            return ((RemoteResultSet)pValue).id;
        }

        if (pValue instanceof RemoteResultSetMetaData)
        {
            return ((RemoteResultSetMetaData)pValue).id;
        }

        if (pValue instanceof RemoteDatabaseMetaData)
        {
            return ((RemoteDatabaseMetaData)pValue).id;
        }

        if (pValue instanceof RemoteBlob)
        {
            return ((RemoteBlob)pValue).id;
        }

        if (pValue instanceof RemoteClob)
        {
            return ((RemoteClob)pValue).id;
        }

        if (pValue instanceof RemoteNClob)
        {
            return ((RemoteNClob)pValue).id;
        }

        if (pValue instanceof RemoteSQLXML)
        {
            return ((RemoteSQLXML)pValue).id;
        }

        if (pValue instanceof RemoteArray)
        {
            return ((RemoteArray)pValue).id;
        }

        if (pValue instanceof RemoteStruct)
        {
            return ((RemoteStruct)pValue).id;
        }

        if (pValue instanceof RemoteParameterMetaData)
        {
            return ((RemoteParameterMetaData)pValue).id;
        }

        if (pValue instanceof RemoteRef)
        {
            return ((RemoteRef)pValue).id;
        }

        if (pValue instanceof RemoteRowId)
        {
            return ((RemoteRowId)pValue).id;
        }

        if (pValue instanceof RemoteSavepoint)
        {
            return ((RemoteSavepoint)pValue).id;
        }

        if (pValue instanceof RemoteExecutor)
        {
            return ((RemoteExecutor)pValue).id;
        }

        if (pValue instanceof InputStream)
        {
            return readBytes((InputStream)pValue);
        }

        if (pValue instanceof Reader)
        {
            return readChars((Reader)pValue);
        }

        return pValue;
    }

    /**
     * Reads bytes from the remote JDBC resource.
     *
     * @param pInput the input
     * @return the resulting JDBC value
     * @throws SQLException if the JDBC operation cannot be completed
     */
	private static byte[] readBytes(InputStream pInput) throws SQLException
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
        finally
        {
        	CommonUtil.close(pInput);
        }
    }

    /**
     * Reads chars from the remote JDBC resource.
     *
     * @param pReader the reader
     * @return the resulting JDBC value
     * @throws SQLException if the JDBC operation cannot be completed
     */
	private static String readChars(Reader pReader) throws SQLException
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
        catch (java.io.IOException e)
        {
            throw new SQLException(e);
        }
        finally
        {
        	CommonUtil.close(pReader);
        }
    }

    /**
     * Converts a protocol value to a numeric value.
     *
     * @param pValue the value to convert
     * @return the resulting JDBC value
     * @throws SQLException if the JDBC operation cannot be completed
     */
	static long number(Object pValue) throws SQLException
    {
        if (!(pValue instanceof Number))
        {
            throw new SQLException("Expected remote JDBC object id, got: " + pValue);
        }

        return ((Number)pValue).longValue();
    }

    /**
     * Handles the unwrap local operation for the remote JDBC resource.
     *
     * @param pSelf the self
     * @param pIface the iface
     * @return the resulting JDBC value
     * @throws SQLException if the JDBC operation cannot be completed
     */
	static <T> T unwrapLocal(Object pSelf, Class<T> pIface) throws SQLException
    {
        if (pIface.isInstance(pSelf))
        {
            return pIface.cast(pSelf);
        }
        
        throw new SQLException("Not a wrapper for " + pIface.getName());
    }

    /**
     * Returns whether wrapper for local.
     *
     * @param pSelf the self
     * @param pIface the iface
     * @return whether the condition is true
     */
    static boolean isWrapperForLocal(Object pSelf, Class<?> pIface)
    {
        return pIface.isInstance(pSelf);
    }
}
