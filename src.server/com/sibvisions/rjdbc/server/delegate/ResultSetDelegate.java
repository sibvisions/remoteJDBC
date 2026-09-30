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
package com.sibvisions.rjdbc.server.delegate;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.NClob;
import java.sql.Ref;
import java.sql.RowId;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.SQLType;
import java.sql.SQLXML;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Map;

import com.sibvisions.rjdbc.server.JdbcContext;

/**
 * Server-side delegate for {@code ResultSet} JDBC operations.
 * 
 * @author René Jahn
 */
public final class ResultSetDelegate extends JdbcDelegate
{
    /**
     * Creates a new {@code ResultSetDelegate} instance.
     *
     * @param pContext the context
     */
    public ResultSetDelegate(JdbcContext pContext)
    {
        super(pContext);
    }

    /**
     * Handles the call operation for the remote JDBC resource.
     *
     * @param pId the remote resource identifier
     * @param pSignature the signature
     * @param pArgs the method arguments
     * @return the resulting JDBC value
     * @throws SQLException if the JDBC operation cannot be completed
     */
    @SuppressWarnings({ "deprecation", "unchecked", "rawtypes" })
	public Object call(long pId, String pSignature, Object[] pArgs) throws SQLException
    {
        switch (pSignature)
        {
            case "absolute(int)": return remoteValue(context.resultSet(pId).absolute((int)pArgs[0]));
            case "afterLast()":
                context.resultSet(pId).afterLast();

                return null;
            case "beforeFirst()":
                context.resultSet(pId).beforeFirst();

                return null;
            case "cancelRowUpdates()":
                context.resultSet(pId).cancelRowUpdates();

                return null;
            case "clearWarnings()":
                context.resultSet(pId).clearWarnings();

                return null;
            case "close()":
                context.resultSet(pId).close();

                return null;
            case "deleteRow()":
                context.resultSet(pId).deleteRow();

                return null;
            case "findColumn(String)": return remoteValue(context.resultSet(pId).findColumn((String)pArgs[0]));
            case "first()": return remoteValue(context.resultSet(pId).first());
            case "getArray(String)": return remoteValue(context.resultSet(pId).getArray((String)pArgs[0]));
            case "getArray(int)": return remoteValue(context.resultSet(pId).getArray((int)pArgs[0]));
            case "getAsciiStream(String)": return remoteValue(context.resultSet(pId).getAsciiStream((String)pArgs[0]));
            case "getAsciiStream(int)": return remoteValue(context.resultSet(pId).getAsciiStream((int)pArgs[0]));
            case "getBigDecimal(String)": return remoteValue(context.resultSet(pId).getBigDecimal((String)pArgs[0]));
            case "getBigDecimal(String,int)": return remoteValue(context.resultSet(pId).getBigDecimal((String)pArgs[0], (int)pArgs[1]));
            case "getBigDecimal(int)": return remoteValue(context.resultSet(pId).getBigDecimal((int)pArgs[0]));
            case "getBigDecimal(int,int)": return remoteValue(context.resultSet(pId).getBigDecimal((int)pArgs[0], (int)pArgs[1]));
            case "getBinaryStream(String)": return remoteValue(context.resultSet(pId).getBinaryStream((String)pArgs[0]));
            case "getBinaryStream(int)": return remoteValue(context.resultSet(pId).getBinaryStream((int)pArgs[0]));
            case "getBlob(String)": return remoteValue(context.resultSet(pId).getBlob((String)pArgs[0]));
            case "getBlob(int)": return remoteValue(context.resultSet(pId).getBlob((int)pArgs[0]));
            case "getBoolean(String)": return remoteValue(context.resultSet(pId).getBoolean((String)pArgs[0]));
            case "getBoolean(int)": return remoteValue(context.resultSet(pId).getBoolean((int)pArgs[0]));
            case "getByte(String)": return remoteValue(context.resultSet(pId).getByte((String)pArgs[0]));
            case "getByte(int)": return remoteValue(context.resultSet(pId).getByte((int)pArgs[0]));
            case "getBytes(String)": return remoteValue(context.resultSet(pId).getBytes((String)pArgs[0]));
            case "getBytes(int)": return remoteValue(context.resultSet(pId).getBytes((int)pArgs[0]));
            case "getCharacterStream(String)": return remoteValue(context.resultSet(pId).getCharacterStream((String)pArgs[0]));
            case "getCharacterStream(int)": return remoteValue(context.resultSet(pId).getCharacterStream((int)pArgs[0]));
            case "getClob(String)": return remoteValue(context.resultSet(pId).getClob((String)pArgs[0]));
            case "getClob(int)": return remoteValue(context.resultSet(pId).getClob((int)pArgs[0]));
            case "getConcurrency()": return remoteValue(context.resultSet(pId).getConcurrency());
            case "getCursorName()": return remoteValue(context.resultSet(pId).getCursorName());
            case "getDate(String)": return remoteValue(context.resultSet(pId).getDate((String)pArgs[0]));
            case "getDate(String,Calendar)": return remoteValue(context.resultSet(pId).getDate((String)pArgs[0], (Calendar)pArgs[1]));
            case "getDate(int)": return remoteValue(context.resultSet(pId).getDate((int)pArgs[0]));
            case "getDate(int,Calendar)": return remoteValue(context.resultSet(pId).getDate((int)pArgs[0], (Calendar)pArgs[1]));
            case "getDouble(String)": return remoteValue(context.resultSet(pId).getDouble((String)pArgs[0]));
            case "getDouble(int)": return remoteValue(context.resultSet(pId).getDouble((int)pArgs[0]));
            case "getFetchDirection()": return remoteValue(context.resultSet(pId).getFetchDirection());
            case "getFetchSize()": return remoteValue(context.resultSet(pId).getFetchSize());
            case "getFloat(String)": return remoteValue(context.resultSet(pId).getFloat((String)pArgs[0]));
            case "getFloat(int)": return remoteValue(context.resultSet(pId).getFloat((int)pArgs[0]));
            case "getHoldability()": return remoteValue(context.resultSet(pId).getHoldability());
            case "getInt(String)": return remoteValue(context.resultSet(pId).getInt((String)pArgs[0]));
            case "getInt(int)": return remoteValue(context.resultSet(pId).getInt((int)pArgs[0]));
            case "getLong(String)": return remoteValue(context.resultSet(pId).getLong((String)pArgs[0]));
            case "getLong(int)": return remoteValue(context.resultSet(pId).getLong((int)pArgs[0]));
            case "getMetaData()": return remoteValue(context.resultSet(pId).getMetaData());
            case "getNCharacterStream(String)": return remoteValue(context.resultSet(pId).getNCharacterStream((String)pArgs[0]));
            case "getNCharacterStream(int)": return remoteValue(context.resultSet(pId).getNCharacterStream((int)pArgs[0]));
            case "getNClob(String)": return remoteValue(context.resultSet(pId).getNClob((String)pArgs[0]));
            case "getNClob(int)": return remoteValue(context.resultSet(pId).getNClob((int)pArgs[0]));
            case "getNString(String)": return remoteValue(context.resultSet(pId).getNString((String)pArgs[0]));
            case "getNString(int)": return remoteValue(context.resultSet(pId).getNString((int)pArgs[0]));
            case "getObject(String)": return remoteResultSetValue(context.resultSet(pId), context.resultSet(pId).findColumn((String)pArgs[0]), context.resultSet(pId).getObject((String)pArgs[0]));
            case "getObject(String,Class)": return remoteResultSetValue(context.resultSet(pId), context.resultSet(pId).findColumn((String)pArgs[0]), context.resultSet(pId).getObject((String)pArgs[0], (Class)pArgs[1]));
            case "getObject(String,Map)": return remoteResultSetValue(context.resultSet(pId), context.resultSet(pId).findColumn((String)pArgs[0]), context.resultSet(pId).getObject((String)pArgs[0], (Map)pArgs[1]));
            case "getObject(int)": return remoteResultSetValue(context.resultSet(pId), ((Number)pArgs[0]).intValue(), context.resultSet(pId).getObject(((Number)pArgs[0]).intValue()));
            case "getObject(int,Class)": return remoteResultSetValue(context.resultSet(pId), ((Number)pArgs[0]).intValue(), context.resultSet(pId).getObject(((Number)pArgs[0]).intValue(), (Class)pArgs[1]));
            case "getObject(int,Map)": return remoteResultSetValue(context.resultSet(pId), ((Number)pArgs[0]).intValue(), context.resultSet(pId).getObject(((Number)pArgs[0]).intValue(), (Map)pArgs[1]));
            case "getRef(String)": return remoteValue(context.resultSet(pId).getRef((String)pArgs[0]));
            case "getRef(int)": return remoteValue(context.resultSet(pId).getRef((int)pArgs[0]));
            case "getRow()": return remoteValue(context.resultSet(pId).getRow());
            case "getRowId(String)": return remoteValue(context.resultSet(pId).getRowId((String)pArgs[0]));
            case "getRowId(int)": return remoteValue(context.resultSet(pId).getRowId((int)pArgs[0]));
            case "getSQLXML(String)": return remoteResultSetValue(context.resultSet(pId), context.resultSet(pId).findColumn((String)pArgs[0]));
            case "getSQLXML(int)": return remoteResultSetValue(context.resultSet(pId), (int)pArgs[0]);
            case "getShort(String)": return remoteValue(context.resultSet(pId).getShort((String)pArgs[0]));
            case "getShort(int)": return remoteValue(context.resultSet(pId).getShort((int)pArgs[0]));
            case "getStatement()": return remoteValue(context.resultSet(pId).getStatement());
            case "getString(String)": return remoteValue(context.resultSet(pId).getString((String)pArgs[0]));
            case "getString(int)": return remoteValue(context.resultSet(pId).getString((int)pArgs[0]));
            case "getTime(String)": return remoteValue(context.resultSet(pId).getTime((String)pArgs[0]));
            case "getTime(String,Calendar)": return remoteValue(context.resultSet(pId).getTime((String)pArgs[0], (Calendar)pArgs[1]));
            case "getTime(int)": return remoteValue(context.resultSet(pId).getTime((int)pArgs[0]));
            case "getTime(int,Calendar)": return remoteValue(context.resultSet(pId).getTime((int)pArgs[0], (Calendar)pArgs[1]));
            case "getTimestamp(String)": return remoteValue(context.resultSet(pId).getTimestamp((String)pArgs[0]));
            case "getTimestamp(String,Calendar)": return remoteValue(context.resultSet(pId).getTimestamp((String)pArgs[0], (Calendar)pArgs[1]));
            case "getTimestamp(int)": return remoteValue(context.resultSet(pId).getTimestamp((int)pArgs[0]));
            case "getTimestamp(int,Calendar)": return remoteValue(context.resultSet(pId).getTimestamp((int)pArgs[0], (Calendar)pArgs[1]));
            case "getType()": return remoteValue(context.resultSet(pId).getType());
            case "getURL(String)": return remoteValue(context.resultSet(pId).getURL((String)pArgs[0]));
            case "getURL(int)": return remoteValue(context.resultSet(pId).getURL((int)pArgs[0]));
            case "getUnicodeStream(String)": return remoteValue(context.resultSet(pId).getUnicodeStream((String)pArgs[0]));
            case "getUnicodeStream(int)": return remoteValue(context.resultSet(pId).getUnicodeStream((int)pArgs[0]));
            case "getWarnings()": return remoteValue(context.resultSet(pId).getWarnings());
            case "insertRow()":
                context.resultSet(pId).insertRow();

                return null;
            case "isAfterLast()": return remoteValue(context.resultSet(pId).isAfterLast());
            case "isBeforeFirst()": return remoteValue(context.resultSet(pId).isBeforeFirst());
            case "isClosed()": return remoteValue(context.resultSet(pId).isClosed());
            case "isFirst()": return remoteValue(context.resultSet(pId).isFirst());
            case "isLast()": return remoteValue(context.resultSet(pId).isLast());
            case "last()": return remoteValue(context.resultSet(pId).last());
            case "moveToCurrentRow()":
                context.resultSet(pId).moveToCurrentRow();

                return null;
            case "moveToInsertRow()":
                context.resultSet(pId).moveToInsertRow();

                return null;
            case "next()": return remoteValue(context.resultSet(pId).next());
            case "previous()": return remoteValue(context.resultSet(pId).previous());
            case "refreshRow()":
                context.resultSet(pId).refreshRow();

                return null;
            case "relative(int)": return remoteValue(context.resultSet(pId).relative((int)pArgs[0]));
            case "rowDeleted()": return remoteValue(context.resultSet(pId).rowDeleted());
            case "rowInserted()": return remoteValue(context.resultSet(pId).rowInserted());
            case "rowUpdated()": return remoteValue(context.resultSet(pId).rowUpdated());
            case "setFetchDirection(int)":
                context.resultSet(pId).setFetchDirection((int)pArgs[0]);

                return null;
            case "setFetchSize(int)":
                context.resultSet(pId).setFetchSize((int)pArgs[0]);

                return null;
            case "updateArray(String,Array)":
                context.resultSet(pId).updateArray((String)pArgs[0], (Array)pArgs[1]);

                return null;
            case "updateArray(int,Array)":
                context.resultSet(pId).updateArray((int)pArgs[0], (Array)pArgs[1]);

                return null;
            case "updateAsciiStream(String,InputStream)":
                context.resultSet(pId).updateAsciiStream((String)pArgs[0], (InputStream)pArgs[1]);

                return null;
            case "updateAsciiStream(String,InputStream,int)":
                context.resultSet(pId).updateAsciiStream((String)pArgs[0], (InputStream)pArgs[1], (int)pArgs[2]);

                return null;
            case "updateAsciiStream(String,InputStream,long)":
                context.resultSet(pId).updateAsciiStream((String)pArgs[0], (InputStream)pArgs[1], (long)pArgs[2]);

                return null;
            case "updateAsciiStream(int,InputStream)":
                context.resultSet(pId).updateAsciiStream((int)pArgs[0], (InputStream)pArgs[1]);

                return null;
            case "updateAsciiStream(int,InputStream,int)":
                context.resultSet(pId).updateAsciiStream((int)pArgs[0], (InputStream)pArgs[1], (int)pArgs[2]);

                return null;
            case "updateAsciiStream(int,InputStream,long)":
                context.resultSet(pId).updateAsciiStream((int)pArgs[0], (InputStream)pArgs[1], (long)pArgs[2]);

                return null;
            case "updateBigDecimal(String,BigDecimal)":
                context.resultSet(pId).updateBigDecimal((String)pArgs[0], (BigDecimal)pArgs[1]);

                return null;
            case "updateBigDecimal(int,BigDecimal)":
                context.resultSet(pId).updateBigDecimal((int)pArgs[0], (BigDecimal)pArgs[1]);

                return null;
            case "updateBinaryStream(String,InputStream)":
                context.resultSet(pId).updateBinaryStream((String)pArgs[0], (InputStream)pArgs[1]);

                return null;
            case "updateBinaryStream(String,InputStream,int)":
                context.resultSet(pId).updateBinaryStream((String)pArgs[0], (InputStream)pArgs[1], (int)pArgs[2]);

                return null;
            case "updateBinaryStream(String,InputStream,long)":
                context.resultSet(pId).updateBinaryStream((String)pArgs[0], (InputStream)pArgs[1], (long)pArgs[2]);

                return null;
            case "updateBinaryStream(int,InputStream)":
                context.resultSet(pId).updateBinaryStream((int)pArgs[0], (InputStream)pArgs[1]);

                return null;
            case "updateBinaryStream(int,InputStream,int)":
                context.resultSet(pId).updateBinaryStream((int)pArgs[0], (InputStream)pArgs[1], (int)pArgs[2]);

                return null;
            case "updateBinaryStream(int,InputStream,long)":
                context.resultSet(pId).updateBinaryStream((int)pArgs[0], (InputStream)pArgs[1], (long)pArgs[2]);

                return null;
            case "updateBlob(String,Blob)":
                context.resultSet(pId).updateBlob((String)pArgs[0], (Blob)pArgs[1]);

                return null;
            case "updateBlob(String,InputStream)":
                context.resultSet(pId).updateBlob((String)pArgs[0], (InputStream)pArgs[1]);

                return null;
            case "updateBlob(String,InputStream,long)":
                context.resultSet(pId).updateBlob((String)pArgs[0], (InputStream)pArgs[1], (long)pArgs[2]);

                return null;
            case "updateBlob(int,Blob)":
                context.resultSet(pId).updateBlob((int)pArgs[0], (Blob)pArgs[1]);

                return null;
            case "updateBlob(int,InputStream)":
                context.resultSet(pId).updateBlob((int)pArgs[0], (InputStream)pArgs[1]);

                return null;
            case "updateBlob(int,InputStream,long)":
                context.resultSet(pId).updateBlob((int)pArgs[0], (InputStream)pArgs[1], (long)pArgs[2]);

                return null;
            case "updateBoolean(String,boolean)":
                context.resultSet(pId).updateBoolean((String)pArgs[0], (boolean)pArgs[1]);

                return null;
            case "updateBoolean(int,boolean)":
                context.resultSet(pId).updateBoolean((int)pArgs[0], (boolean)pArgs[1]);

                return null;
            case "updateByte(String,byte)":
                context.resultSet(pId).updateByte((String)pArgs[0], (byte)pArgs[1]);

                return null;
            case "updateByte(int,byte)":
                context.resultSet(pId).updateByte((int)pArgs[0], (byte)pArgs[1]);

                return null;
            case "updateBytes(String,byte[])":
                context.resultSet(pId).updateBytes((String)pArgs[0], (byte[]) pArgs[1]);

                return null;
            case "updateBytes(int,byte[])":
                context.resultSet(pId).updateBytes((int)pArgs[0], (byte[]) pArgs[1]);

                return null;
            case "updateCharacterStream(String,Reader)":
                context.resultSet(pId).updateCharacterStream((String)pArgs[0], (Reader)pArgs[1]);

                return null;
            case "updateCharacterStream(String,Reader,int)":
                context.resultSet(pId).updateCharacterStream((String)pArgs[0], (Reader)pArgs[1], (int)pArgs[2]);

                return null;
            case "updateCharacterStream(String,Reader,long)":
                context.resultSet(pId).updateCharacterStream((String)pArgs[0], (Reader)pArgs[1], (long)pArgs[2]);

                return null;
            case "updateCharacterStream(int,Reader)":
                context.resultSet(pId).updateCharacterStream((int)pArgs[0], (Reader)pArgs[1]);

                return null;
            case "updateCharacterStream(int,Reader,int)":
                context.resultSet(pId).updateCharacterStream((int)pArgs[0], (Reader)pArgs[1], (int)pArgs[2]);

                return null;
            case "updateCharacterStream(int,Reader,long)":
                context.resultSet(pId).updateCharacterStream((int)pArgs[0], (Reader)pArgs[1], (long)pArgs[2]);

                return null;
            case "updateClob(String,Clob)":
                context.resultSet(pId).updateClob((String)pArgs[0], (Clob)pArgs[1]);

                return null;
            case "updateClob(String,Reader)":
                context.resultSet(pId).updateClob((String)pArgs[0], (Reader)pArgs[1]);

                return null;
            case "updateClob(String,Reader,long)":
                context.resultSet(pId).updateClob((String)pArgs[0], (Reader)pArgs[1], (long)pArgs[2]);

                return null;
            case "updateClob(int,Clob)":
                context.resultSet(pId).updateClob((int)pArgs[0], (Clob)pArgs[1]);

                return null;
            case "updateClob(int,Reader)":
                context.resultSet(pId).updateClob((int)pArgs[0], (Reader)pArgs[1]);

                return null;
            case "updateClob(int,Reader,long)":
                context.resultSet(pId).updateClob((int)pArgs[0], (Reader)pArgs[1], (long)pArgs[2]);

                return null;
            case "updateDate(String,Date)":
                context.resultSet(pId).updateDate((String)pArgs[0], (java.sql.Date) pArgs[1]);

                return null;
            case "updateDate(int,Date)":
                context.resultSet(pId).updateDate((int)pArgs[0], (java.sql.Date) pArgs[1]);

                return null;
            case "updateDouble(String,double)":
                context.resultSet(pId).updateDouble((String)pArgs[0], (double)pArgs[1]);

                return null;
            case "updateDouble(int,double)":
                context.resultSet(pId).updateDouble((int)pArgs[0], (double)pArgs[1]);

                return null;
            case "updateFloat(String,float)":
                context.resultSet(pId).updateFloat((String)pArgs[0], (float)pArgs[1]);

                return null;
            case "updateFloat(int,float)":
                context.resultSet(pId).updateFloat((int)pArgs[0], (float)pArgs[1]);

                return null;
            case "updateInt(String,int)":
                context.resultSet(pId).updateInt((String)pArgs[0], (int)pArgs[1]);

                return null;
            case "updateInt(int,int)":
                context.resultSet(pId).updateInt((int)pArgs[0], (int)pArgs[1]);

                return null;
            case "updateLong(String,long)":
                context.resultSet(pId).updateLong((String)pArgs[0], (long)pArgs[1]);

                return null;
            case "updateLong(int,long)":
                context.resultSet(pId).updateLong((int)pArgs[0], (long)pArgs[1]);

                return null;
            case "updateNCharacterStream(String,Reader)":
                context.resultSet(pId).updateNCharacterStream((String)pArgs[0], (Reader)pArgs[1]);

                return null;
            case "updateNCharacterStream(String,Reader,long)":
                context.resultSet(pId).updateNCharacterStream((String)pArgs[0], (Reader)pArgs[1], (long)pArgs[2]);

                return null;
            case "updateNCharacterStream(int,Reader)":
                context.resultSet(pId).updateNCharacterStream((int)pArgs[0], (Reader)pArgs[1]);

                return null;
            case "updateNCharacterStream(int,Reader,long)":
                context.resultSet(pId).updateNCharacterStream((int)pArgs[0], (Reader)pArgs[1], (long)pArgs[2]);

                return null;
            case "updateNClob(String,NClob)":
                context.resultSet(pId).updateNClob((String)pArgs[0], (NClob)pArgs[1]);

                return null;
            case "updateNClob(String,Reader)":
                context.resultSet(pId).updateNClob((String)pArgs[0], (Reader)pArgs[1]);

                return null;
            case "updateNClob(String,Reader,long)":
                context.resultSet(pId).updateNClob((String)pArgs[0], (Reader)pArgs[1], (long)pArgs[2]);

                return null;
            case "updateNClob(int,NClob)":
                context.resultSet(pId).updateNClob((int)pArgs[0], (NClob)pArgs[1]);

                return null;
            case "updateNClob(int,Reader)":
                context.resultSet(pId).updateNClob((int)pArgs[0], (Reader)pArgs[1]);

                return null;
            case "updateNClob(int,Reader,long)":
                context.resultSet(pId).updateNClob((int)pArgs[0], (Reader)pArgs[1], (long)pArgs[2]);

                return null;
            case "updateNString(String,String)":
                context.resultSet(pId).updateNString((String)pArgs[0], (String)pArgs[1]);

                return null;
            case "updateNString(int,String)":
                context.resultSet(pId).updateNString((int)pArgs[0], (String)pArgs[1]);

                return null;
            case "updateNull(String)":
                context.resultSet(pId).updateNull((String)pArgs[0]);

                return null;
            case "updateNull(int)":
                context.resultSet(pId).updateNull((int)pArgs[0]);

                return null;
            case "updateObject(String,Object)":
                context.resultSet(pId).updateObject((String)pArgs[0], (Object)pArgs[1]);

                return null;
            case "updateObject(String,Object,SQLType)":
                context.resultSet(pId).updateObject((String)pArgs[0], (Object)pArgs[1], (SQLType)pArgs[2]);

                return null;
            case "updateObject(String,Object,SQLType,int)":
                context.resultSet(pId).updateObject((String)pArgs[0], (Object)pArgs[1], (SQLType)pArgs[2], (int)pArgs[3]);

                return null;
            case "updateObject(String,Object,int)":
                context.resultSet(pId).updateObject((String)pArgs[0], (Object)pArgs[1], (int)pArgs[2]);

                return null;
            case "updateObject(int,Object)":
                context.resultSet(pId).updateObject((int)pArgs[0], (Object)pArgs[1]);

                return null;
            case "updateObject(int,Object,SQLType)":
                context.resultSet(pId).updateObject((int)pArgs[0], (Object)pArgs[1], (SQLType)pArgs[2]);

                return null;
            case "updateObject(int,Object,SQLType,int)":
                context.resultSet(pId).updateObject((int)pArgs[0], (Object)pArgs[1], (SQLType)pArgs[2], (int)pArgs[3]);

                return null;
            case "updateObject(int,Object,int)":
                context.resultSet(pId).updateObject((int)pArgs[0], (Object)pArgs[1], (int)pArgs[2]);

                return null;
            case "updateRef(String,Ref)":
                context.resultSet(pId).updateRef((String)pArgs[0], (Ref)pArgs[1]);

                return null;
            case "updateRef(int,Ref)":
                context.resultSet(pId).updateRef((int)pArgs[0], (Ref)pArgs[1]);

                return null;
            case "updateRow()":
                context.resultSet(pId).updateRow();

                return null;
            case "updateRowId(String,RowId)":
                context.resultSet(pId).updateRowId((String)pArgs[0], (RowId)pArgs[1]);

                return null;
            case "updateRowId(int,RowId)":
                context.resultSet(pId).updateRowId((int)pArgs[0], (RowId)pArgs[1]);

                return null;
            case "updateSQLXML(String,SQLXML)":
                context.resultSet(pId).updateSQLXML((String)pArgs[0], (SQLXML)pArgs[1]);

                return null;
            case "updateSQLXML(int,SQLXML)":
                context.resultSet(pId).updateSQLXML((int)pArgs[0], (SQLXML)pArgs[1]);

                return null;
            case "updateShort(String,short)":
                context.resultSet(pId).updateShort((String)pArgs[0], (short)pArgs[1]);

                return null;
            case "updateShort(int,short)":
                context.resultSet(pId).updateShort((int)pArgs[0], (short)pArgs[1]);

                return null;
            case "updateString(String,String)":
                context.resultSet(pId).updateString((String)pArgs[0], (String)pArgs[1]);

                return null;
            case "updateString(int,String)":
                context.resultSet(pId).updateString((int)pArgs[0], (String)pArgs[1]);

                return null;
            case "updateTime(String,Time)":
                context.resultSet(pId).updateTime((String)pArgs[0], (Time)pArgs[1]);

                return null;
            case "updateTime(int,Time)":
                context.resultSet(pId).updateTime((int)pArgs[0], (Time)pArgs[1]);

                return null;
            case "updateTimestamp(String,Timestamp)":
                context.resultSet(pId).updateTimestamp((String)pArgs[0], (Timestamp)pArgs[1]);

                return null;
            case "updateTimestamp(int,Timestamp)":
                context.resultSet(pId).updateTimestamp((int)pArgs[0], (Timestamp)pArgs[1]);

                return null;
            case "wasNull()": return remoteValue(context.resultSet(pId).wasNull());
                
            default: throw new SQLFeatureNotSupportedException("Unsupported JDBC method: " + pSignature);
        }
    }
}
