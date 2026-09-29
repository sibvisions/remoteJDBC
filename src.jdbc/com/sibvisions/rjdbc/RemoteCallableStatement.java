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
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.Date;
import java.sql.NClob;
import java.sql.Ref;
import java.sql.RowId;
import java.sql.SQLException;
import java.sql.SQLType;
import java.sql.SQLXML;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Map;

/**
 * Remote JDBC implementation of {@code CallableStatement} functionality.
 */
public class RemoteCallableStatement extends RemotePreparedStatement 
									 implements CallableStatement
{
    /**
     * Creates a new {@code RemoteCallableStatement} instance.
     *
     * @param pClient the client
     * @param pId the remote resource identifier
     */
    public RemoteCallableStatement(RemoteClient pClient, long pId)
    {
        super(pClient, pId, CallableStatement.class);
    }

    /** {@inheritDoc} */
    @Override
    public void registerOutParameter(int pParameterIndex, int pSqlType) throws SQLException
    {
        RemoteUtil.invoke(client, id, remoteInterface, "registerOutParameter", new Class<?>[]{int.class, int.class}, new Object[]{pParameterIndex, pSqlType}, void.class);
    }

    /** {@inheritDoc} */
    @Override
    public void registerOutParameter(int pParameterIndex, int pSqlType, int pScale) throws SQLException
    {
        RemoteUtil.invoke(client, id, remoteInterface, "registerOutParameter", new Class<?>[]{int.class, int.class, int.class}, new Object[]{pParameterIndex, pSqlType, pScale}, void.class);
    }

    /** {@inheritDoc} */
    @Override
    public boolean wasNull() throws SQLException
    {
        return (boolean)RemoteUtil.invoke(client, id, remoteInterface, "wasNull", new Class<?>[]{}, new Object[]{}, boolean.class);
    }

    /** {@inheritDoc} */
    @Override
    public String getString(int pParameterIndex) throws SQLException
    {
        return (String)RemoteUtil.invoke(client, id, remoteInterface, "getString", new Class<?>[]{int.class}, new Object[]{pParameterIndex}, String.class);
    }

    /** {@inheritDoc} */
    @Override
    public boolean getBoolean(int pParameterIndex) throws SQLException
    {
        return (boolean)RemoteUtil.invoke(client, id, remoteInterface, "getBoolean", new Class<?>[]{int.class}, new Object[]{pParameterIndex}, boolean.class);
    }

    /** {@inheritDoc} */
    @Override
    public byte getByte(int pParameterIndex) throws SQLException
    {
        return (byte)RemoteUtil.invoke(client, id, remoteInterface, "getByte", new Class<?>[]{int.class}, new Object[]{pParameterIndex}, byte.class);
    }

    /** {@inheritDoc} */
    @Override
    public short getShort(int pParameterIndex) throws SQLException
    {
        return (short)RemoteUtil.invoke(client, id, remoteInterface, "getShort", new Class<?>[]{int.class}, new Object[]{pParameterIndex}, short.class);
    }

    /** {@inheritDoc} */
    @Override
    public int getInt(int pParameterIndex) throws SQLException
    {
        return (int)RemoteUtil.invoke(client, id, remoteInterface, "getInt", new Class<?>[]{int.class}, new Object[]{pParameterIndex}, int.class);
    }

    /** {@inheritDoc} */
    @Override
    public long getLong(int pParameterIndex) throws SQLException
    {
        return (long)RemoteUtil.invoke(client, id, remoteInterface, "getLong", new Class<?>[]{int.class}, new Object[]{pParameterIndex}, long.class);
    }

    /** {@inheritDoc} */
    @Override
    public float getFloat(int pParameterIndex) throws SQLException
    {
        return (float)RemoteUtil.invoke(client, id, remoteInterface, "getFloat", new Class<?>[]{int.class}, new Object[]{pParameterIndex}, float.class);
    }

    /** {@inheritDoc} */
    @Override
    public double getDouble(int pParameterIndex) throws SQLException
    {
        return (double)RemoteUtil.invoke(client, id, remoteInterface, "getDouble", new Class<?>[]{int.class}, new Object[]{pParameterIndex}, double.class);
    }

    /** {@inheritDoc} */
    @Override
    public BigDecimal getBigDecimal(int pParameterIndex, int pScale) throws SQLException
    {
        return (BigDecimal)RemoteUtil.invoke(client, id, remoteInterface, "getBigDecimal", new Class<?>[]{int.class, int.class}, new Object[]{pParameterIndex, pScale}, BigDecimal.class);
    }

    /** {@inheritDoc} */
    @Override
    public byte[] getBytes(int pParameterIndex) throws SQLException
    {
        return (byte[]) RemoteUtil.invoke(client, id, remoteInterface, "getBytes", new Class<?>[]{int.class}, new Object[]{pParameterIndex}, byte[].class);
    }

    /** {@inheritDoc} */
    @Override
    public Date getDate(int pParameterIndex) throws SQLException
    {
        return (Date)RemoteUtil.invoke(client, id, remoteInterface, "getDate", new Class<?>[]{int.class}, new Object[]{pParameterIndex}, Date.class);
    }

    /** {@inheritDoc} */
    @Override
    public Time getTime(int pParameterIndex) throws SQLException
    {
        return (Time)RemoteUtil.invoke(client, id, remoteInterface, "getTime", new Class<?>[]{int.class}, new Object[]{pParameterIndex}, Time.class);
    }

    /** {@inheritDoc} */
    @Override
    public Timestamp getTimestamp(int pParameterIndex) throws SQLException
    {
        return (Timestamp)RemoteUtil.invoke(client, id, remoteInterface, "getTimestamp", new Class<?>[]{int.class}, new Object[]{pParameterIndex}, Timestamp.class);
    }

    /** {@inheritDoc} */
    @Override
    public Object getObject(int pParameterIndex) throws SQLException
    {
        return (Object)RemoteUtil.invoke(client, id, remoteInterface, "getObject", new Class<?>[]{int.class}, new Object[]{pParameterIndex}, Object.class);
    }

    /** {@inheritDoc} */
    @Override
    public BigDecimal getBigDecimal(int pParameterIndex) throws SQLException
    {
        return (BigDecimal)RemoteUtil.invoke(client, id, remoteInterface, "getBigDecimal", new Class<?>[]{int.class}, new Object[]{pParameterIndex}, BigDecimal.class);
    }

    /** {@inheritDoc} */
    @Override
    public Object getObject(int pParameterIndex, Map<String, Class<?>> pMap) throws SQLException
    {
        return (Object)RemoteUtil.invoke(client, id, remoteInterface, "getObject", new Class<?>[]{int.class, Map.class}, new Object[]{pParameterIndex, pMap}, Object.class);
    }

    /** {@inheritDoc} */
    @Override
    public Ref getRef(int pParameterIndex) throws SQLException
    {
        return (Ref)RemoteUtil.invoke(client, id, remoteInterface, "getRef", new Class<?>[]{int.class}, new Object[]{pParameterIndex}, Ref.class);
    }

    /** {@inheritDoc} */
    @Override
    public Blob getBlob(int pParameterIndex) throws SQLException
    {
        return (Blob)RemoteUtil.invoke(client, id, remoteInterface, "getBlob", new Class<?>[]{int.class}, new Object[]{pParameterIndex}, Blob.class);
    }

    /** {@inheritDoc} */
    @Override
    public Clob getClob(int pParameterIndex) throws SQLException
    {
        return (Clob)RemoteUtil.invoke(client, id, remoteInterface, "getClob", new Class<?>[]{int.class}, new Object[]{pParameterIndex}, Clob.class);
    }

    /** {@inheritDoc} */
    @Override
    public Array getArray(int pParameterIndex) throws SQLException
    {
        return (Array)RemoteUtil.invoke(client, id, remoteInterface, "getArray", new Class<?>[]{int.class}, new Object[]{pParameterIndex}, Array.class);
    }

    /** {@inheritDoc} */
    @Override
    public Date getDate(int pParameterIndex, Calendar pCalendar) throws SQLException
    {
        return (Date)RemoteUtil.invoke(client, id, remoteInterface, "getDate", new Class<?>[]{int.class, Calendar.class}, new Object[]{pParameterIndex, pCalendar}, Date.class);
    }

    /** {@inheritDoc} */
    @Override
    public Time getTime(int pParameterIndex, Calendar pCalendar) throws SQLException
    {
        return (Time)RemoteUtil.invoke(client, id, remoteInterface, "getTime", new Class<?>[]{int.class, Calendar.class}, new Object[]{pParameterIndex, pCalendar}, Time.class);
    }

    /** {@inheritDoc} */
    @Override
    public Timestamp getTimestamp(int pParameterIndex, Calendar pCalendar) throws SQLException
    {
        return (Timestamp)RemoteUtil.invoke(client, id, remoteInterface, "getTimestamp", new Class<?>[]{int.class, Calendar.class}, new Object[]{pParameterIndex, pCalendar}, Timestamp.class);
    }

    /** {@inheritDoc} */
    @Override
    public void registerOutParameter(int pParameterIndex, int pSqlType, String pScale) throws SQLException
    {
        RemoteUtil.invoke(client, id, remoteInterface, "registerOutParameter", new Class<?>[]{int.class, int.class, String.class}, new Object[]{pParameterIndex, pSqlType, pScale}, void.class);
    }

    /** {@inheritDoc} */
    @Override
    public void registerOutParameter(String pParameterName, int pSqlType) throws SQLException
    {
        RemoteUtil.invoke(client, id, remoteInterface, "registerOutParameter", new Class<?>[]{String.class, int.class}, new Object[]{pParameterName, pSqlType}, void.class);
    }

    /** {@inheritDoc} */
    @Override
    public void registerOutParameter(String pParameterName, int pSqlType, int pScale) throws SQLException
    {
        RemoteUtil.invoke(client, id, remoteInterface, "registerOutParameter", new Class<?>[]{String.class, int.class, int.class}, new Object[]{pParameterName, pSqlType, pScale}, void.class);
    }

    /** {@inheritDoc} */
    @Override
    public void registerOutParameter(String pParameterName, int pSqlType, String pScale) throws SQLException
    {
        RemoteUtil.invoke(client, id, remoteInterface, "registerOutParameter", new Class<?>[]{String.class, int.class, String.class}, new Object[]{pParameterName, pSqlType, pScale}, void.class);
    }

    /** {@inheritDoc} */
    @Override
    public URL getURL(int pParameterIndex) throws SQLException
    {
        return (URL)RemoteUtil.invoke(client, id, remoteInterface, "getURL", new Class<?>[]{int.class}, new Object[]{pParameterIndex}, URL.class);
    }

    /** {@inheritDoc} */
    @Override
    public void setURL(String pParameterName, URL pValue) throws SQLException
    {
        RemoteUtil.invoke(client, id, remoteInterface, "setURL", new Class<?>[]{String.class, URL.class}, new Object[]{pParameterName, pValue}, void.class);
    }

    /** {@inheritDoc} */
    @Override
    public void setNull(String pParameterName, int pSqlType) throws SQLException
    {
        RemoteUtil.invoke(client, id, remoteInterface, "setNull", new Class<?>[]{String.class, int.class}, new Object[]{pParameterName, pSqlType}, void.class);
    }

    /** {@inheritDoc} */
    @Override
    public void setBoolean(String pParameterName, boolean pValue) throws SQLException
    {
        RemoteUtil.invoke(client, id, remoteInterface, "setBoolean", new Class<?>[]{String.class, boolean.class}, new Object[]{pParameterName, pValue}, void.class);
    }

    /** {@inheritDoc} */
    @Override
    public void setByte(String pParameterName, byte pValue) throws SQLException
    {
        RemoteUtil.invoke(client, id, remoteInterface, "setByte", new Class<?>[]{String.class, byte.class}, new Object[]{pParameterName, pValue}, void.class);
    }

    /** {@inheritDoc} */
    @Override
    public void setShort(String pParameterName, short pValue) throws SQLException
    {
        RemoteUtil.invoke(client, id, remoteInterface, "setShort", new Class<?>[]{String.class, short.class}, new Object[]{pParameterName, pValue}, void.class);
    }

    /** {@inheritDoc} */
    @Override
    public void setInt(String pParameterName, int pValue) throws SQLException
    {
        RemoteUtil.invoke(client, id, remoteInterface, "setInt", new Class<?>[]{String.class, int.class}, new Object[]{pParameterName, pValue}, void.class);
    }

    /** {@inheritDoc} */
    @Override
    public void setLong(String pParameterName, long pValue) throws SQLException
    {
        RemoteUtil.invoke(client, id, remoteInterface, "setLong", new Class<?>[]{String.class, long.class}, new Object[]{pParameterName, pValue}, void.class);
    }

    /** {@inheritDoc} */
    @Override
    public void setFloat(String pParameterName, float pValue) throws SQLException
    {
        RemoteUtil.invoke(client, id, remoteInterface, "setFloat", new Class<?>[]{String.class, float.class}, new Object[]{pParameterName, pValue}, void.class);
    }

    /** {@inheritDoc} */
    @Override
    public void setDouble(String pParameterName, double pValue) throws SQLException
    {
        RemoteUtil.invoke(client, id, remoteInterface, "setDouble", new Class<?>[]{String.class, double.class}, new Object[]{pParameterName, pValue}, void.class);
    }

    /** {@inheritDoc} */
    @Override
    public void setBigDecimal(String pParameterName, BigDecimal pValue) throws SQLException
    {
        RemoteUtil.invoke(client, id, remoteInterface, "setBigDecimal", new Class<?>[]{String.class, BigDecimal.class}, new Object[]{pParameterName, pValue}, void.class);
    }

    /** {@inheritDoc} */
    @Override
    public void setString(String pParameterName, String pValue) throws SQLException
    {
        RemoteUtil.invoke(client, id, remoteInterface, "setString", new Class<?>[]{String.class, String.class}, new Object[]{pParameterName, pValue}, void.class);
    }

    /** {@inheritDoc} */
    @Override
    public void setBytes(String pParameterName, byte[] pValue) throws SQLException
    {
        RemoteUtil.invoke(client, id, remoteInterface, "setBytes", new Class<?>[]{String.class, byte[].class}, new Object[]{pParameterName, pValue}, void.class);
    }

    /** {@inheritDoc} */
    @Override
    public void setDate(String pParameterName, Date pValue) throws SQLException
    {
        RemoteUtil.invoke(client, id, remoteInterface, "setDate", new Class<?>[]{String.class, Date.class}, new Object[]{pParameterName, pValue}, void.class);
    }

    /** {@inheritDoc} */
    @Override
    public void setTime(String pParameterName, Time pValue) throws SQLException
    {
        RemoteUtil.invoke(client, id, remoteInterface, "setTime", new Class<?>[]{String.class, Time.class}, new Object[]{pParameterName, pValue}, void.class);
    }

    /** {@inheritDoc} */
    @Override
    public void setTimestamp(String pParameterName, Timestamp pValue) throws SQLException
    {
        RemoteUtil.invoke(client, id, remoteInterface, "setTimestamp", new Class<?>[]{String.class, Timestamp.class}, new Object[]{pParameterName, pValue}, void.class);
    }

    /** {@inheritDoc} */
    @Override
    public void setAsciiStream(String pParameterName, InputStream pValue, int pLength) throws SQLException
    {
        RemoteUtil.invoke(client, id, remoteInterface, "setAsciiStream", new Class<?>[]{String.class, InputStream.class, int.class}, new Object[]{pParameterName, pValue, pLength}, void.class);
    }

    /** {@inheritDoc} */
    @Override
    public void setBinaryStream(String pParameterName, InputStream pValue, int pLength) throws SQLException
    {
        RemoteUtil.invoke(client, id, remoteInterface, "setBinaryStream", new Class<?>[]{String.class, InputStream.class, int.class}, new Object[]{pParameterName, pValue, pLength}, void.class);
    }

    /** {@inheritDoc} */
    @Override
    public void setObject(String pParameterName, Object pValue, int pTargetSqlType, int pScaleOrLength) throws SQLException
    {
        RemoteUtil.invoke(client, id, remoteInterface, "setObject", new Class<?>[]{String.class, Object.class, int.class, int.class}, new Object[]{pParameterName, pValue, pTargetSqlType, pScaleOrLength}, void.class);
    }

    /** {@inheritDoc} */
    @Override
    public void setObject(String pParameterName, Object pValue, int pTargetSqlType) throws SQLException
    {
        RemoteUtil.invoke(client, id, remoteInterface, "setObject", new Class<?>[]{String.class, Object.class, int.class}, new Object[]{pParameterName, pValue, pTargetSqlType}, void.class);
    }

    /** {@inheritDoc} */
    @Override
    public void setObject(String pParameterName, Object pValue) throws SQLException
    {
        RemoteUtil.invoke(client, id, remoteInterface, "setObject", new Class<?>[]{String.class, Object.class}, new Object[]{pParameterName, pValue}, void.class);
    }

    /** {@inheritDoc} */
    @Override
    public void setCharacterStream(String pParameterName, Reader pValue, int pLength) throws SQLException
    {
        RemoteUtil.invoke(client, id, remoteInterface, "setCharacterStream", new Class<?>[]{String.class, Reader.class, int.class}, new Object[]{pParameterName, pValue, pLength}, void.class);
    }

    /** {@inheritDoc} */
    @Override
    public void setDate(String pParameterName, Date pValue, Calendar pCalendar) throws SQLException
    {
        RemoteUtil.invoke(client, id, remoteInterface, "setDate", new Class<?>[]{String.class, Date.class, Calendar.class}, new Object[]{pParameterName, pValue, pCalendar}, void.class);
    }

    /** {@inheritDoc} */
    @Override
    public void setTime(String pParameterName, Time pValue, Calendar pCalendar) throws SQLException
    {
        RemoteUtil.invoke(client, id, remoteInterface, "setTime", new Class<?>[]{String.class, Time.class, Calendar.class}, new Object[]{pParameterName, pValue, pCalendar}, void.class);
    }

    /** {@inheritDoc} */
    @Override
    public void setTimestamp(String pParameterName, Timestamp pValue, Calendar pCalendar) throws SQLException
    {
        RemoteUtil.invoke(client, id, remoteInterface, "setTimestamp", new Class<?>[]{String.class, Timestamp.class, Calendar.class}, new Object[]{pParameterName, pValue, pCalendar}, void.class);
    }

    /** {@inheritDoc} */
    @Override
    public void setNull(String pParameterName, int pSqlType, String pTypeName) throws SQLException
    {
        RemoteUtil.invoke(client, id, remoteInterface, "setNull", new Class<?>[]{String.class, int.class, String.class}, new Object[]{pParameterName, pSqlType, pTypeName}, void.class);
    }

    /** {@inheritDoc} */
    @Override
    public String getString(String pParameterName) throws SQLException
    {
        return (String)RemoteUtil.invoke(client, id, remoteInterface, "getString", new Class<?>[]{String.class}, new Object[]{pParameterName}, String.class);
    }

    /** {@inheritDoc} */
    @Override
    public boolean getBoolean(String pParameterName) throws SQLException
    {
        return (boolean)RemoteUtil.invoke(client, id, remoteInterface, "getBoolean", new Class<?>[]{String.class}, new Object[]{pParameterName}, boolean.class);
    }

    /** {@inheritDoc} */
    @Override
    public byte getByte(String pParameterName) throws SQLException
    {
        return (byte)RemoteUtil.invoke(client, id, remoteInterface, "getByte", new Class<?>[]{String.class}, new Object[]{pParameterName}, byte.class);
    }

    /** {@inheritDoc} */
    @Override
    public short getShort(String pParameterName) throws SQLException
    {
        return (short)RemoteUtil.invoke(client, id, remoteInterface, "getShort", new Class<?>[]{String.class}, new Object[]{pParameterName}, short.class);
    }

    /** {@inheritDoc} */
    @Override
    public int getInt(String pParameterName) throws SQLException
    {
        return (int)RemoteUtil.invoke(client, id, remoteInterface, "getInt", new Class<?>[]{String.class}, new Object[]{pParameterName}, int.class);
    }

    /** {@inheritDoc} */
    @Override
    public long getLong(String pParameterName) throws SQLException
    {
        return (long)RemoteUtil.invoke(client, id, remoteInterface, "getLong", new Class<?>[]{String.class}, new Object[]{pParameterName}, long.class);
    }

    /** {@inheritDoc} */
    @Override
    public float getFloat(String pParameterName) throws SQLException
    {
        return (float)RemoteUtil.invoke(client, id, remoteInterface, "getFloat", new Class<?>[]{String.class}, new Object[]{pParameterName}, float.class);
    }

    /** {@inheritDoc} */
    @Override
    public double getDouble(String pParameterName) throws SQLException
    {
        return (double)RemoteUtil.invoke(client, id, remoteInterface, "getDouble", new Class<?>[]{String.class}, new Object[]{pParameterName}, double.class);
    }

    /** {@inheritDoc} */
    @Override
    public byte[] getBytes(String pParameterName) throws SQLException
    {
        return (byte[]) RemoteUtil.invoke(client, id, remoteInterface, "getBytes", new Class<?>[]{String.class}, new Object[]{pParameterName}, byte[].class);
    }

    /** {@inheritDoc} */
    @Override
    public Date getDate(String pParameterName) throws SQLException
    {
        return (Date)RemoteUtil.invoke(client, id, remoteInterface, "getDate", new Class<?>[]{String.class}, new Object[]{pParameterName}, Date.class);
    }

    /** {@inheritDoc} */
    @Override
    public Time getTime(String pParameterName) throws SQLException
    {
        return (Time)RemoteUtil.invoke(client, id, remoteInterface, "getTime", new Class<?>[]{String.class}, new Object[]{pParameterName}, Time.class);
    }

    /** {@inheritDoc} */
    @Override
    public Timestamp getTimestamp(String pParameterName) throws SQLException
    {
        return (Timestamp)RemoteUtil.invoke(client, id, remoteInterface, "getTimestamp", new Class<?>[]{String.class}, new Object[]{pParameterName}, Timestamp.class);
    }

    /** {@inheritDoc} */
    @Override
    public Object getObject(String pParameterName) throws SQLException
    {
        return (Object)RemoteUtil.invoke(client, id, remoteInterface, "getObject", new Class<?>[]{String.class}, new Object[]{pParameterName}, Object.class);
    }

    /** {@inheritDoc} */
    @Override
    public BigDecimal getBigDecimal(String pParameterName) throws SQLException
    {
        return (BigDecimal)RemoteUtil.invoke(client, id, remoteInterface, "getBigDecimal", new Class<?>[]{String.class}, new Object[]{pParameterName}, BigDecimal.class);
    }

    /** {@inheritDoc} */
    @Override
    public Object getObject(String pParameterName, Map<String, Class<?>> pMap) throws SQLException
    {
        return (Object)RemoteUtil.invoke(client, id, remoteInterface, "getObject", new Class<?>[]{String.class, Map.class}, new Object[]{pParameterName, pMap}, Object.class);
    }

    /** {@inheritDoc} */
    @Override
    public Ref getRef(String pParameterName) throws SQLException
    {
        return (Ref)RemoteUtil.invoke(client, id, remoteInterface, "getRef", new Class<?>[]{String.class}, new Object[]{pParameterName}, Ref.class);
    }

    /** {@inheritDoc} */
    @Override
    public Blob getBlob(String pParameterName) throws SQLException
    {
        return (Blob)RemoteUtil.invoke(client, id, remoteInterface, "getBlob", new Class<?>[]{String.class}, new Object[]{pParameterName}, Blob.class);
    }

    /** {@inheritDoc} */
    @Override
    public Clob getClob(String pParameterName) throws SQLException
    {
        return (Clob)RemoteUtil.invoke(client, id, remoteInterface, "getClob", new Class<?>[]{String.class}, new Object[]{pParameterName}, Clob.class);
    }

    /** {@inheritDoc} */
    @Override
    public Array getArray(String pParameterName) throws SQLException
    {
        return (Array)RemoteUtil.invoke(client, id, remoteInterface, "getArray", new Class<?>[]{String.class}, new Object[]{pParameterName}, Array.class);
    }

    /** {@inheritDoc} */
    @Override
    public Date getDate(String pParameterName, Calendar pCalendar) throws SQLException
    {
        return (Date)RemoteUtil.invoke(client, id, remoteInterface, "getDate", new Class<?>[]{String.class, Calendar.class}, new Object[]{pParameterName, pCalendar}, Date.class);
    }

    /** {@inheritDoc} */
    @Override
    public Time getTime(String pParameterName, Calendar pCalendar) throws SQLException
    {
        return (Time)RemoteUtil.invoke(client, id, remoteInterface, "getTime", new Class<?>[]{String.class, Calendar.class}, new Object[]{pParameterName, pCalendar}, Time.class);
    }

    /** {@inheritDoc} */
    @Override
    public Timestamp getTimestamp(String pParameterName, Calendar pCalendar) throws SQLException
    {
        return (Timestamp)RemoteUtil.invoke(client, id, remoteInterface, "getTimestamp", new Class<?>[]{String.class, Calendar.class}, new Object[]{pParameterName, pCalendar}, Timestamp.class);
    }

    /** {@inheritDoc} */
    @Override
    public URL getURL(String pParameterName) throws SQLException
    {
        return (URL)RemoteUtil.invoke(client, id, remoteInterface, "getURL", new Class<?>[]{String.class}, new Object[]{pParameterName}, URL.class);
    }

    /** {@inheritDoc} */
    @Override
    public RowId getRowId(int pParameterIndex) throws SQLException
    {
        return (RowId)RemoteUtil.invoke(client, id, remoteInterface, "getRowId", new Class<?>[]{int.class}, new Object[]{pParameterIndex}, RowId.class);
    }

    /** {@inheritDoc} */
    @Override
    public RowId getRowId(String pParameterName) throws SQLException
    {
        return (RowId)RemoteUtil.invoke(client, id, remoteInterface, "getRowId", new Class<?>[]{String.class}, new Object[]{pParameterName}, RowId.class);
    }

    /** {@inheritDoc} */
    @Override
    public void setRowId(String pParameterName, RowId pValue) throws SQLException
    {
        RemoteUtil.invoke(client, id, remoteInterface, "setRowId", new Class<?>[]{String.class, RowId.class}, new Object[]{pParameterName, pValue}, void.class);
    }

    /** {@inheritDoc} */
    @Override
    public void setNString(String pParameterName, String pValue) throws SQLException
    {
        RemoteUtil.invoke(client, id, remoteInterface, "setNString", new Class<?>[]{String.class, String.class}, new Object[]{pParameterName, pValue}, void.class);
    }

    /** {@inheritDoc} */
    @Override
    public void setNCharacterStream(String pParameterName, Reader pValue, long pLength) throws SQLException
    {
        RemoteUtil.invoke(client, id, remoteInterface, "setNCharacterStream", new Class<?>[]{String.class, Reader.class, long.class}, new Object[]{pParameterName, pValue, pLength}, void.class);
    }

    /** {@inheritDoc} */
    @Override
    public void setNClob(String pParameterName, NClob pValue) throws SQLException
    {
        RemoteUtil.invoke(client, id, remoteInterface, "setNClob", new Class<?>[]{String.class, NClob.class}, new Object[]{pParameterName, pValue}, void.class);
    }

    /** {@inheritDoc} */
    @Override
    public void setClob(String pParameterName, Reader pValue, long pLength) throws SQLException
    {
        RemoteUtil.invoke(client, id, remoteInterface, "setClob", new Class<?>[]{String.class, Reader.class, long.class}, new Object[]{pParameterName, pValue, pLength}, void.class);
    }

    /** {@inheritDoc} */
    @Override
    public void setBlob(String pParameterName, InputStream pValue, long pLength) throws SQLException
    {
        RemoteUtil.invoke(client, id, remoteInterface, "setBlob", new Class<?>[]{String.class, InputStream.class, long.class}, new Object[]{pParameterName, pValue, pLength}, void.class);
    }

    /** {@inheritDoc} */
    @Override
    public void setNClob(String pParameterName, Reader pValue, long pLength) throws SQLException
    {
        RemoteUtil.invoke(client, id, remoteInterface, "setNClob", new Class<?>[]{String.class, Reader.class, long.class}, new Object[]{pParameterName, pValue, pLength}, void.class);
    }

    /** {@inheritDoc} */
    @Override
    public NClob getNClob(int pParameterIndex) throws SQLException
    {
        return (NClob)RemoteUtil.invoke(client, id, remoteInterface, "getNClob", new Class<?>[]{int.class}, new Object[]{pParameterIndex}, NClob.class);
    }

    /** {@inheritDoc} */
    @Override
    public NClob getNClob(String pParameterName) throws SQLException
    {
        return (NClob)RemoteUtil.invoke(client, id, remoteInterface, "getNClob", new Class<?>[]{String.class}, new Object[]{pParameterName}, NClob.class);
    }

    /** {@inheritDoc} */
    @Override
    public void setSQLXML(String pParameterName, SQLXML pValue) throws SQLException
    {
        RemoteUtil.invoke(client, id, remoteInterface, "setSQLXML", new Class<?>[]{String.class, SQLXML.class}, new Object[]{pParameterName, pValue}, void.class);
    }

    /** {@inheritDoc} */
    @Override
    public SQLXML getSQLXML(int pParameterIndex) throws SQLException
    {
        return (SQLXML)RemoteUtil.invoke(client, id, remoteInterface, "getSQLXML", new Class<?>[]{int.class}, new Object[]{pParameterIndex}, SQLXML.class);
    }

    /** {@inheritDoc} */
    @Override
    public SQLXML getSQLXML(String pParameterName) throws SQLException
    {
        return (SQLXML)RemoteUtil.invoke(client, id, remoteInterface, "getSQLXML", new Class<?>[]{String.class}, new Object[]{pParameterName}, SQLXML.class);
    }

    /** {@inheritDoc} */
    @Override
    public String getNString(int pParameterIndex) throws SQLException
    {
        return (String)RemoteUtil.invoke(client, id, remoteInterface, "getNString", new Class<?>[]{int.class}, new Object[]{pParameterIndex}, String.class);
    }

    /** {@inheritDoc} */
    @Override
    public String getNString(String pParameterName) throws SQLException
    {
        return (String)RemoteUtil.invoke(client, id, remoteInterface, "getNString", new Class<?>[]{String.class}, new Object[]{pParameterName}, String.class);
    }

    /** {@inheritDoc} */
    @Override
    public Reader getNCharacterStream(int pParameterIndex) throws SQLException
    {
        return (Reader)RemoteUtil.invoke(client, id, remoteInterface, "getNCharacterStream", new Class<?>[]{int.class}, new Object[]{pParameterIndex}, Reader.class);
    }

    /** {@inheritDoc} */
    @Override
    public Reader getNCharacterStream(String pParameterName) throws SQLException
    {
        return (Reader)RemoteUtil.invoke(client, id, remoteInterface, "getNCharacterStream", new Class<?>[]{String.class}, new Object[]{pParameterName}, Reader.class);
    }

    /** {@inheritDoc} */
    @Override
    public Reader getCharacterStream(int pParameterIndex) throws SQLException
    {
        return (Reader)RemoteUtil.invoke(client, id, remoteInterface, "getCharacterStream", new Class<?>[]{int.class}, new Object[]{pParameterIndex}, Reader.class);
    }

    /** {@inheritDoc} */
    @Override
    public Reader getCharacterStream(String pParameterName) throws SQLException
    {
        return (Reader)RemoteUtil.invoke(client, id, remoteInterface, "getCharacterStream", new Class<?>[]{String.class}, new Object[]{pParameterName}, Reader.class);
    }

    /** {@inheritDoc} */
    @Override
    public void setBlob(String pParameterName, Blob pValue) throws SQLException
    {
        RemoteUtil.invoke(client, id, remoteInterface, "setBlob", new Class<?>[]{String.class, Blob.class}, new Object[]{pParameterName, pValue}, void.class);
    }

    /** {@inheritDoc} */
    @Override
    public void setClob(String pParameterName, Clob pValue) throws SQLException
    {
        RemoteUtil.invoke(client, id, remoteInterface, "setClob", new Class<?>[]{String.class, Clob.class}, new Object[]{pParameterName, pValue}, void.class);
    }

    /** {@inheritDoc} */
    @Override
    public void setAsciiStream(String pParameterName, InputStream pValue, long pLength) throws SQLException
    {
        RemoteUtil.invoke(client, id, remoteInterface, "setAsciiStream", new Class<?>[]{String.class, InputStream.class, long.class}, new Object[]{pParameterName, pValue, pLength}, void.class);
    }

    /** {@inheritDoc} */
    @Override
    public void setBinaryStream(String pParameterName, InputStream pValue, long pLength) throws SQLException
    {
        RemoteUtil.invoke(client, id, remoteInterface, "setBinaryStream", new Class<?>[]{String.class, InputStream.class, long.class}, new Object[]{pParameterName, pValue, pLength}, void.class);
    }

    /** {@inheritDoc} */
    @Override
    public void setCharacterStream(String pParameterName, Reader pValue, long pLength) throws SQLException
    {
        RemoteUtil.invoke(client, id, remoteInterface, "setCharacterStream", new Class<?>[]{String.class, Reader.class, long.class}, new Object[]{pParameterName, pValue, pLength}, void.class);
    }

    /** {@inheritDoc} */
    @Override
    public void setAsciiStream(String pParameterName, InputStream pValue) throws SQLException
    {
        RemoteUtil.invoke(client, id, remoteInterface, "setAsciiStream", new Class<?>[]{String.class, InputStream.class}, new Object[]{pParameterName, pValue}, void.class);
    }

    /** {@inheritDoc} */
    @Override
    public void setBinaryStream(String pParameterName, InputStream pValue) throws SQLException
    {
        RemoteUtil.invoke(client, id, remoteInterface, "setBinaryStream", new Class<?>[]{String.class, InputStream.class}, new Object[]{pParameterName, pValue}, void.class);
    }

    /** {@inheritDoc} */
    @Override
    public void setCharacterStream(String pParameterName, Reader pValue) throws SQLException
    {
        RemoteUtil.invoke(client, id, remoteInterface, "setCharacterStream", new Class<?>[]{String.class, Reader.class}, new Object[]{pParameterName, pValue}, void.class);
    }

    /** {@inheritDoc} */
    @Override
    public void setNCharacterStream(String pParameterName, Reader pValue) throws SQLException
    {
        RemoteUtil.invoke(client, id, remoteInterface, "setNCharacterStream", new Class<?>[]{String.class, Reader.class}, new Object[]{pParameterName, pValue}, void.class);
    }

    /** {@inheritDoc} */
    @Override
    public void setClob(String pParameterName, Reader pValue) throws SQLException
    {
        RemoteUtil.invoke(client, id, remoteInterface, "setClob", new Class<?>[]{String.class, Reader.class}, new Object[]{pParameterName, pValue}, void.class);
    }

    /** {@inheritDoc} */
    @Override
    public void setBlob(String pParameterName, InputStream pValue) throws SQLException
    {
        RemoteUtil.invoke(client, id, remoteInterface, "setBlob", new Class<?>[]{String.class, InputStream.class}, new Object[]{pParameterName, pValue}, void.class);
    }

    /** {@inheritDoc} */
    @Override
    public void setNClob(String pParameterName, Reader pValue) throws SQLException
    {
        RemoteUtil.invoke(client, id, remoteInterface, "setNClob", new Class<?>[]{String.class, Reader.class}, new Object[]{pParameterName, pValue}, void.class);
    }

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("unchecked")
    public <T> T getObject(int pParameterIndex, Class<T> pType) throws SQLException
    {
        return (T)RemoteUtil.invoke(client, id, remoteInterface, "getObject", new Class<?>[]{int.class, Class.class}, new Object[]{pParameterIndex, pType}, Object.class);
    }

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("unchecked")
    public <T> T getObject(String pParameterName, Class<T> pType) throws SQLException
    {
        return (T)RemoteUtil.invoke(client, id, remoteInterface, "getObject", new Class<?>[]{String.class, Class.class}, new Object[]{pParameterName, pType}, Object.class);
    }

    /** {@inheritDoc} */
    @Override
    public void setObject(String pParameterName, Object pValue, SQLType pSqlType, int pScaleOrLength) throws SQLException
    {
        RemoteUtil.invoke(client, id, remoteInterface, "setObject", new Class<?>[]{String.class, Object.class, SQLType.class, int.class}, new Object[]{pParameterName, pValue, pSqlType, pScaleOrLength}, void.class);
    }

    /** {@inheritDoc} */
    @Override
    public void setObject(String pParameterName, Object pValue, SQLType pSqlType) throws SQLException
    {
        RemoteUtil.invoke(client, id, remoteInterface, "setObject", new Class<?>[]{String.class, Object.class, SQLType.class}, new Object[]{pParameterName, pValue, pSqlType}, void.class);
    }

    /** {@inheritDoc} */
    @Override
    public void registerOutParameter(int pParameterIndex, SQLType pSqlType) throws SQLException
    {
        RemoteUtil.invoke(client, id, remoteInterface, "registerOutParameter", new Class<?>[]{int.class, SQLType.class}, new Object[]{pParameterIndex, pSqlType}, void.class);
    }

    /** {@inheritDoc} */
    @Override
    public void registerOutParameter(int pParameterIndex, SQLType pSqlType, int pScale) throws SQLException
    {
        RemoteUtil.invoke(client, id, remoteInterface, "registerOutParameter", new Class<?>[]{int.class, SQLType.class, int.class}, new Object[]{pParameterIndex, pSqlType, pScale}, void.class);
    }

    /** {@inheritDoc} */
    @Override
    public void registerOutParameter(int pParameterIndex, SQLType pSqlType, String pScale) throws SQLException
    {
        RemoteUtil.invoke(client, id, remoteInterface, "registerOutParameter", new Class<?>[]{int.class, SQLType.class, String.class}, new Object[]{pParameterIndex, pSqlType, pScale}, void.class);
    }

    /** {@inheritDoc} */
    @Override
    public void registerOutParameter(String pParameterIndex, SQLType pSqlType) throws SQLException
    {
        RemoteUtil.invoke(client, id, remoteInterface, "registerOutParameter", new Class<?>[]{String.class, SQLType.class}, new Object[]{pParameterIndex, pSqlType}, void.class);
    }

    /** {@inheritDoc} */
    @Override
    public void registerOutParameter(String pParameterIndex, SQLType pSqlType, int pScale) throws SQLException
    {
        RemoteUtil.invoke(client, id, remoteInterface, "registerOutParameter", new Class<?>[]{String.class, SQLType.class, int.class}, new Object[]{pParameterIndex, pSqlType, pScale}, void.class);
    }

    /** {@inheritDoc} */
    @Override
    public void registerOutParameter(String pParameterIndex, SQLType pSqlType, String pScale) throws SQLException
    {
        RemoteUtil.invoke(client, id, remoteInterface, "registerOutParameter", new Class<?>[]{String.class, SQLType.class, String.class}, new Object[]{pParameterIndex, pSqlType, pScale}, void.class);
    }
}
