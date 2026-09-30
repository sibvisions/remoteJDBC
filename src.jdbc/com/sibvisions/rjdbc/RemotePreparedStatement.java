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

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Date;
import java.sql.NClob;
import java.sql.ParameterMetaData;
import java.sql.PreparedStatement;
import java.sql.Ref;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.RowId;
import java.sql.SQLException;
import java.sql.SQLType;
import java.sql.SQLXML;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Handles the invoke setter operation for the remote JDBC resource.
 */
public class RemotePreparedStatement extends RemoteStatement 
									 implements PreparedStatement
{
    private final Map<Object, Map<String,Object>> currentParameters = new LinkedHashMap<>();

    private final List<List<Map<String,Object>>> batchRows = new ArrayList<>();

	
    /**
     * Creates a new {@code RemotePreparedStatement} instance.
     *
     * @param pClient the client
     * @param pId the remote resource identifier
     */
    public RemotePreparedStatement(RemoteClient pClient, long pId)
    {
        super(pClient, pId, PreparedStatement.class);
    }

    /**
     * Creates a new {@code RemotePreparedStatement} instance.
     *
     * @param pClient the client
     * @param pId the remote resource identifier
     * @param pRemoteInterface the remote interface
     */
    protected RemotePreparedStatement(RemoteClient pClient, long pId, Class<?> pRemoteInterface)
    {
        super(pClient, pId, pRemoteInterface);
    }

    /** {@inheritDoc} */
    @Override
    public ResultSet executeQuery() throws SQLException
    {
        return (ResultSet)RemoteUtil.adapt(client, executePrepared("executePreparedQuery"), ResultSet.class);
    }

    /** {@inheritDoc} */
    @Override
    public int executeUpdate() throws SQLException
    {
        return ((Number)executePrepared("executePreparedUpdate")).intValue();
    }

    /** {@inheritDoc} */
    @Override
    public void setNull(int pParameterIndex, int pSqlType) throws SQLException
    {
        invokeSetter("setNull", new Class<?>[]{int.class, int.class}, new Object[]{pParameterIndex, pSqlType});
    }

    /** {@inheritDoc} */
    @Override
    public void setBoolean(int pParameterIndex, boolean pValue) throws SQLException
    {
        invokeSetter("setBoolean", new Class<?>[]{int.class, boolean.class}, new Object[]{pParameterIndex, pValue});
    }

    /** {@inheritDoc} */
    @Override
    public void setByte(int pParameterIndex, byte pValue) throws SQLException
    {
        invokeSetter("setByte", new Class<?>[]{int.class, byte.class}, new Object[]{pParameterIndex, pValue});
    }

    /** {@inheritDoc} */
    @Override
    public void setShort(int pParameterIndex, short pValue) throws SQLException
    {
        invokeSetter("setShort", new Class<?>[]{int.class, short.class}, new Object[]{pParameterIndex, pValue});
    }

    /** {@inheritDoc} */
    @Override
    public void setInt(int pParameterIndex, int pValue) throws SQLException
    {
        invokeSetter("setInt", new Class<?>[]{int.class, int.class}, new Object[]{pParameterIndex, pValue});
    }

    /** {@inheritDoc} */
    @Override
    public void setLong(int pParameterIndex, long pValue) throws SQLException
    {
        invokeSetter("setLong", new Class<?>[]{int.class, long.class}, new Object[]{pParameterIndex, pValue});
    }

    /** {@inheritDoc} */
    @Override
    public void setFloat(int pParameterIndex, float pValue) throws SQLException
    {
        invokeSetter("setFloat", new Class<?>[]{int.class, float.class}, new Object[]{pParameterIndex, pValue});
    }

    /** {@inheritDoc} */
    @Override
    public void setDouble(int pParameterIndex, double pValue) throws SQLException
    {
        invokeSetter("setDouble", new Class<?>[]{int.class, double.class}, new Object[]{pParameterIndex, pValue});
    }

    /** {@inheritDoc} */
    @Override
    public void setBigDecimal(int pParameterIndex, BigDecimal pValue) throws SQLException
    {
        invokeSetter("setBigDecimal", new Class<?>[]{int.class, BigDecimal.class}, new Object[]{pParameterIndex, pValue});
    }

    /** {@inheritDoc} */
    @Override
    public void setString(int pParameterIndex, String pValue) throws SQLException
    {
        invokeSetter("setString", new Class<?>[]{int.class, String.class}, new Object[]{pParameterIndex, pValue});
    }

    /** {@inheritDoc} */
    @Override
    public void setBytes(int pParameterIndex, byte[] pValue) throws SQLException
    {
        invokeSetter("setBytes", new Class<?>[]{int.class, byte[].class}, new Object[]{pParameterIndex, pValue});
    }

    /** {@inheritDoc} */
    @Override
    public void setDate(int pParameterIndex, Date pValue) throws SQLException
    {
        invokeSetter("setDate", new Class<?>[]{int.class, Date.class}, new Object[]{pParameterIndex, pValue});
    }

    /** {@inheritDoc} */
    @Override
    public void setTime(int pParameterIndex, Time pValue) throws SQLException
    {
        invokeSetter("setTime", new Class<?>[]{int.class, Time.class}, new Object[]{pParameterIndex, pValue});
    }

    /** {@inheritDoc} */
    @Override
    public void setTimestamp(int pParameterIndex, Timestamp pValue) throws SQLException
    {
        invokeSetter("setTimestamp", new Class<?>[]{int.class, Timestamp.class}, new Object[]{pParameterIndex, pValue});
    }

    /** {@inheritDoc} */
    @Override
    public void setAsciiStream(int pParameterIndex, InputStream pValue, int pLength) throws SQLException
    {
        invokeSetter("setAsciiStream", new Class<?>[]{int.class, InputStream.class, int.class}, new Object[]{pParameterIndex, pValue, pLength});
    }

    /** {@inheritDoc} */
    @Override
    public void setUnicodeStream(int pParameterIndex, InputStream pValue, int pLength) throws SQLException
    {
        invokeSetter("setUnicodeStream", new Class<?>[]{int.class, InputStream.class, int.class}, new Object[]{pParameterIndex, pValue, pLength});
    }

    /** {@inheritDoc} */
    @Override
    public void setBinaryStream(int pParameterIndex, InputStream pValue, int pLength) throws SQLException
    {
        invokeSetter("setBinaryStream", new Class<?>[]{int.class, InputStream.class, int.class}, new Object[]{pParameterIndex, pValue, pLength});
    }

    /** {@inheritDoc} */
    @Override
    public void clearParameters() throws SQLException
    {
    	ensureOpen();
    	
        currentParameters.clear();
    }

    /** {@inheritDoc} */
    @Override
    public void setObject(int pParameterIndex, Object pValue, int pTargetSqlType) throws SQLException
    {
        invokeSetter("setObject", new Class<?>[]{int.class, Object.class, int.class}, new Object[]{pParameterIndex, pValue, pTargetSqlType});
    }

    /** {@inheritDoc} */
    @Override
    public void setObject(int pParameterIndex, Object pValue) throws SQLException
    {
        invokeSetter("setObject", new Class<?>[]{int.class, Object.class}, new Object[]{pParameterIndex, pValue});
    }

    /** {@inheritDoc} */
    @Override
    public boolean execute() throws SQLException
    {
        return (Boolean)executePrepared("executePrepared");
    }

    /** {@inheritDoc} */
    @Override
    public void addBatch() throws SQLException
    {
    	ensureOpen();
    	
        batchRows.add(new ArrayList<>(currentParameters.values()));
    }

    /** {@inheritDoc} */
    @Override
    public void setCharacterStream(int pParameterIndex, Reader pValue, int pLength) throws SQLException
    {
        invokeSetter("setCharacterStream", new Class<?>[]{int.class, Reader.class, int.class}, new Object[]{pParameterIndex, pValue, pLength});
    }

    /** {@inheritDoc} */
    @Override
    public void setRef(int pParameterIndex, Ref pValue) throws SQLException
    {
        invokeSetter("setRef", new Class<?>[]{int.class, Ref.class}, new Object[]{pParameterIndex, pValue});
    }

    /** {@inheritDoc} */
    @Override
    public void setBlob(int pParameterIndex, Blob pValue) throws SQLException
    {
        invokeSetter("setBlob", new Class<?>[]{int.class, Blob.class}, new Object[]{pParameterIndex, pValue});
    }

    /** {@inheritDoc} */
    @Override
    public void setClob(int pParameterIndex, Clob pValue) throws SQLException
    {
        invokeSetter("setClob", new Class<?>[]{int.class, Clob.class}, new Object[]{pParameterIndex, pValue});
    }

    /** {@inheritDoc} */
    @Override
    public void setArray(int pParameterIndex, Array pValue) throws SQLException
    {
        invokeSetter("setArray", new Class<?>[]{int.class, Array.class}, new Object[]{pParameterIndex, pValue});
    }

    /** {@inheritDoc} */
    @Override
    public ResultSetMetaData getMetaData() throws SQLException
    {
    	ensureOpen();
    	
        return (ResultSetMetaData)RemoteUtil.invoke(client, id, remoteInterface, "getMetaData", new Class<?>[]{}, new Object[]{}, ResultSetMetaData.class);
    }

    /** {@inheritDoc} */
    @Override
    public void setDate(int pParameterIndex, Date pValue, Calendar pCalendar) throws SQLException
    {
        invokeSetter("setDate", new Class<?>[]{int.class, Date.class, Calendar.class}, new Object[]{pParameterIndex, pValue, pCalendar});
    }

    /** {@inheritDoc} */
    @Override
    public void setTime(int pParameterIndex, Time pValue, Calendar pCalendar) throws SQLException
    {
        invokeSetter("setTime", new Class<?>[]{int.class, Time.class, Calendar.class}, new Object[]{pParameterIndex, pValue, pCalendar});
    }

    /** {@inheritDoc} */
    @Override
    public void setTimestamp(int pParameterIndex, Timestamp pValue, Calendar pCalendar) throws SQLException
    {
        invokeSetter("setTimestamp", new Class<?>[]{int.class, Timestamp.class, Calendar.class}, new Object[]{pParameterIndex, pValue, pCalendar});
    }

    /** {@inheritDoc} */
    @Override
    public void setNull(int pParameterIndex, int pSqlType, String pTypeName) throws SQLException
    {
        invokeSetter("setNull", new Class<?>[]{int.class, int.class, String.class}, new Object[]{pParameterIndex, pSqlType, pTypeName});
    }

    /** {@inheritDoc} */
    @Override
    public void setURL(int pParameterIndex, URL pValue) throws SQLException
    {
        invokeSetter("setURL", new Class<?>[]{int.class, URL.class}, new Object[]{pParameterIndex, pValue});
    }

    /** {@inheritDoc} */
    @Override
    public ParameterMetaData getParameterMetaData() throws SQLException
    {
    	ensureOpen();
    	
        return (ParameterMetaData)RemoteUtil.invoke(client, id, remoteInterface, "getParameterMetaData", new Class<?>[]{}, new Object[]{}, ParameterMetaData.class);
    }

    /** {@inheritDoc} */
    @Override
    public void setRowId(int pParameterIndex, RowId pValue) throws SQLException
    {
        invokeSetter("setRowId", new Class<?>[]{int.class, RowId.class}, new Object[]{pParameterIndex, pValue});
    }

    /** {@inheritDoc} */
    @Override
    public void setNString(int pParameterIndex, String pValue) throws SQLException
    {
        invokeSetter("setNString", new Class<?>[]{int.class, String.class}, new Object[]{pParameterIndex, pValue});
    }

    /** {@inheritDoc} */
    @Override
    public void setNCharacterStream(int pParameterIndex, Reader pValue, long pLength) throws SQLException
    {
        invokeSetter("setNCharacterStream", new Class<?>[]{int.class, Reader.class, long.class}, new Object[]{pParameterIndex, pValue, pLength});
    }

    /** {@inheritDoc} */
    @Override
    public void setNClob(int pParameterIndex, NClob pValue) throws SQLException
    {
        invokeSetter("setNClob", new Class<?>[]{int.class, NClob.class}, new Object[]{pParameterIndex, pValue});
    }

    /** {@inheritDoc} */
    @Override
    public void setClob(int pParameterIndex, Reader pValue, long pLength) throws SQLException
    {
        invokeSetter("setClob", new Class<?>[]{int.class, Reader.class, long.class}, new Object[]{pParameterIndex, pValue, pLength});
    }

    /** {@inheritDoc} */
    @Override
    public void setBlob(int pParameterIndex, InputStream pValue, long pLength) throws SQLException
    {
        invokeSetter("setBlob", new Class<?>[]{int.class, InputStream.class, long.class}, new Object[]{pParameterIndex, pValue, pLength});
    }

    /** {@inheritDoc} */
    @Override
    public void setNClob(int pParameterIndex, Reader pValue, long pLength) throws SQLException
    {
        invokeSetter("setNClob", new Class<?>[]{int.class, Reader.class, long.class}, new Object[]{pParameterIndex, pValue, pLength});
    }

    /** {@inheritDoc} */
    @Override
    public void setSQLXML(int pParameterIndex, SQLXML pValue) throws SQLException
    {
        invokeSetter("setSQLXML", new Class<?>[]{int.class, SQLXML.class}, new Object[]{pParameterIndex, pValue});
    }

    /** {@inheritDoc} */
    @Override
    public void setObject(int pParameterIndex, Object pValue, int pTargetSqlType, int pScaleOrLength) throws SQLException
    {
        invokeSetter("setObject", new Class<?>[]{int.class, Object.class, int.class, int.class}, new Object[]{pParameterIndex, pValue, pTargetSqlType, pScaleOrLength});
    }

    /** {@inheritDoc} */
    @Override
    public void setAsciiStream(int pParameterIndex, InputStream pValue, long pLength) throws SQLException
    {
        invokeSetter("setAsciiStream", new Class<?>[]{int.class, InputStream.class, long.class}, new Object[]{pParameterIndex, pValue, pLength});
    }

    /** {@inheritDoc} */
    @Override
    public void setBinaryStream(int pParameterIndex, InputStream pValue, long pLength) throws SQLException
    {
        invokeSetter("setBinaryStream", new Class<?>[]{int.class, InputStream.class, long.class}, new Object[]{pParameterIndex, pValue, pLength});
    }

    /** {@inheritDoc} */
    @Override
    public void setCharacterStream(int pParameterIndex, Reader pValue, long pLength) throws SQLException
    {
        invokeSetter("setCharacterStream", new Class<?>[]{int.class, Reader.class, long.class}, new Object[]{pParameterIndex, pValue, pLength});
    }

    /** {@inheritDoc} */
    @Override
    public void setAsciiStream(int pParameterIndex, InputStream pValue) throws SQLException
    {
        invokeSetter("setAsciiStream", new Class<?>[]{int.class, InputStream.class}, new Object[]{pParameterIndex, pValue});
    }

    /** {@inheritDoc} */
    @Override
    public void setBinaryStream(int pParameterIndex, InputStream pValue) throws SQLException
    {
        invokeSetter("setBinaryStream", new Class<?>[]{int.class, InputStream.class}, new Object[]{pParameterIndex, pValue});
    }

    /** {@inheritDoc} */
    @Override
    public void setCharacterStream(int pParameterIndex, Reader pValue) throws SQLException
    {
        invokeSetter("setCharacterStream", new Class<?>[]{int.class, Reader.class}, new Object[]{pParameterIndex, pValue});
    }

    /** {@inheritDoc} */
    @Override
    public void setNCharacterStream(int pParameterIndex, Reader pValue) throws SQLException
    {
        invokeSetter("setNCharacterStream", new Class<?>[]{int.class, Reader.class}, new Object[]{pParameterIndex, pValue});
    }

    /** {@inheritDoc} */
    @Override
    public void setClob(int pParameterIndex, Reader pValue) throws SQLException
    {
        invokeSetter("setClob", new Class<?>[]{int.class, Reader.class}, new Object[]{pParameterIndex, pValue});
    }

    /** {@inheritDoc} */
    @Override
    public void setBlob(int pParameterIndex, InputStream pValue) throws SQLException
    {
        invokeSetter("setBlob", new Class<?>[]{int.class, InputStream.class}, new Object[]{pParameterIndex, pValue});
    }

    /** {@inheritDoc} */
    @Override
    public void setNClob(int pParameterIndex, Reader pValue) throws SQLException
    {
        invokeSetter("setNClob", new Class<?>[]{int.class, Reader.class}, new Object[]{pParameterIndex, pValue});
    }

    /** {@inheritDoc} */
    @Override
    public void setObject(int pParameterIndex, Object pValue, SQLType pSqlType, int pScaleOrLength) throws SQLException
    {
        invokeSetter("setObject", new Class<?>[]{int.class, Object.class, SQLType.class, int.class}, new Object[]{pParameterIndex, pValue, pSqlType, pScaleOrLength});
    }

    /** {@inheritDoc} */
    @Override
    public void setObject(int pParameterIndex, Object pValue, SQLType pSqlType) throws SQLException
    {
        invokeSetter("setObject", new Class<?>[]{int.class, Object.class, SQLType.class}, new Object[]{pParameterIndex, pValue, pSqlType});
    }

    /** {@inheritDoc} */
    @Override
    public void clearBatch() throws SQLException
    {
        batchRows.clear();
        
        super.clearBatch();
    }

    /** {@inheritDoc} */
    @Override
    public int[] executeBatch() throws SQLException
    {
    	ensureOpen();
    	
    	if (batchRows.isEmpty())
        {
            return super.executeBatch();
        }

        Map<String,Object> request = new HashMap<>();
        request.put(RemoteConstants.ACTION, "executePreparedBatch");
        request.put(RemoteConstants.ID, id);
        request.put(RemoteConstants.ROWS, new ArrayList<List<Map<String,Object>>>(batchRows));
        
        Object result = client.call(request).get(RemoteConstants.RESULT);
        
        batchRows.clear();

        return (int[]) result;
    }

    /** {@inheritDoc} */
    @Override
    public long executeLargeUpdate() throws SQLException
    {
        return ((Number)executePrepared("executePreparedLargeUpdate")).longValue();
    }

    /** {@inheritDoc} */
    @Override
    public long[] executeLargeBatch() throws SQLException
    {
    	ensureOpen();
    	
    	if (batchRows.isEmpty())
        {
            return super.executeLargeBatch();
        }

        Map<String,Object> request = new HashMap<>();
        request.put(RemoteConstants.ACTION, "executePreparedLargeBatch");
        request.put(RemoteConstants.ID, id);
        request.put(RemoteConstants.ROWS, new ArrayList<List<Map<String,Object>>>(batchRows));

        Object result = client.call(request).get(RemoteConstants.RESULT);
        
        batchRows.clear();

        return (long[])result;
    }

    /**
     * Invokes the appropriate JDBC setter method for the supplied parameter value.
     *
     * @param pMethod the method
     * @param pParameterTypes the parameter types
     * @param pArgs the method arguments
     * @throws SQLException if the JDBC operation cannot be completed
     */
    protected void invokeSetter(String pMethod, Class<?>[] pParameterTypes, Object[] pArgs) throws SQLException
    {
    	ensureOpen();
    	
    	String[] names = new String[pParameterTypes.length];
        
        for (int i = 0; i < pParameterTypes.length; i++)
        {
            names[i] = pParameterTypes[i].getName();
        }

        Map<String,Object> operation = new HashMap<String,Object>();
        operation.put(RemoteConstants.METHOD, pMethod);
        operation.put(RemoteConstants.PARAMETER_TYPES, names);
        operation.put(RemoteConstants.VALUE, encodeArgumentsForBatch(pArgs));
        
        if (pArgs.length == 0)
        {
            throw new SQLException("Prepared statement setter requires a parameter identifier");
        }

        currentParameters.put(pArgs[0], operation);
    }

    /**
     * Encodes arguments for batch for transport through the remote JDBC protocol.
     *
     * @param pArgs the method arguments
     * @return the resulting JDBC value
     * @throws SQLException if the JDBC operation cannot be completed
     */
	private Object[] encodeArgumentsForBatch(Object[] pArgs) throws SQLException
    {
        return RemoteUtil.encodeArgumentsForBatch(pArgs);
    }

    /**
     * Executes prepared through the remote JDBC protocol.
     *
     * @param pAction the action
     * @return the resulting JDBC value
     * @throws SQLException if the JDBC operation cannot be completed
     */
	private Object executePrepared(String pAction) throws SQLException
    {
    	ensureOpen();
		
        Map<String,Object> request = new HashMap<String,Object>();
        request.put(RemoteConstants.ACTION, pAction);
        request.put(RemoteConstants.ID, id);
        request.put(RemoteConstants.OPERATIONS, new ArrayList<Map<String,Object>>(currentParameters.values()));
        
        Object value = client.call(request).get(RemoteConstants.RESULT);

        return value;
    }
}
