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

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.NClob;
import java.sql.RowId;
import java.sql.SQLException;
import java.sql.SQLType;
import java.sql.SQLXML;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Map;

import com.sibvisions.rjdbc.server.JdbcContext;

/**
 * Server-side delegate for {@code CallableStatement} JDBC operations.
 * 
 * @author René Jahn
 */
public final class CallableStatementDelegate extends JdbcDelegate
{
    /**
     * Creates a new {@code CallableStatementDelegate} instance.
     *
     * @param pContext the context
     */
    public CallableStatementDelegate(JdbcContext pContext)
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
    @SuppressWarnings({ "deprecation", "rawtypes", "unchecked" })
	public Object call(long pId, String pSignature, Object[] pArgs) throws SQLException
    {
        switch (pSignature)
        {
            case "getArray(String)":

                return remoteValue(((CallableStatement)context.statement(pId)).getArray((String)pArgs[0]));
            case "getArray(int)":

                return remoteValue(((CallableStatement)context.statement(pId)).getArray((int)pArgs[0]));
            case "getBigDecimal(String)":

                return remoteValue(((CallableStatement)context.statement(pId)).getBigDecimal((String)pArgs[0]));
            case "getBigDecimal(int)":

                return remoteValue(((CallableStatement)context.statement(pId)).getBigDecimal((int)pArgs[0]));
            case "getBigDecimal(int,int)":

                return remoteValue(((CallableStatement)context.statement(pId)).getBigDecimal((int)pArgs[0], (int)pArgs[1]));
            case "getBlob(String)":

                return remoteValue(((CallableStatement)context.statement(pId)).getBlob((String)pArgs[0]));
            case "getBlob(int)":

                return remoteValue(((CallableStatement)context.statement(pId)).getBlob((int)pArgs[0]));
            case "getBoolean(String)":

                return remoteValue(((CallableStatement)context.statement(pId)).getBoolean((String)pArgs[0]));
            case "getBoolean(int)":

                return remoteValue(((CallableStatement)context.statement(pId)).getBoolean((int)pArgs[0]));
            case "getByte(String)":

                return remoteValue(((CallableStatement)context.statement(pId)).getByte((String)pArgs[0]));
            case "getByte(int)":

                return remoteValue(((CallableStatement)context.statement(pId)).getByte((int)pArgs[0]));
            case "getBytes(String)":

                return remoteValue(((CallableStatement)context.statement(pId)).getBytes((String)pArgs[0]));
            case "getBytes(int)":

                return remoteValue(((CallableStatement)context.statement(pId)).getBytes((int)pArgs[0]));
            case "getCharacterStream(String)":

                return remoteValue(((CallableStatement)context.statement(pId)).getCharacterStream((String)pArgs[0]));
            case "getCharacterStream(int)":

                return remoteValue(((CallableStatement)context.statement(pId)).getCharacterStream((int)pArgs[0]));
            case "getClob(String)":

                return remoteValue(((CallableStatement)context.statement(pId)).getClob((String)pArgs[0]));
            case "getClob(int)":

                return remoteValue(((CallableStatement)context.statement(pId)).getClob((int)pArgs[0]));
            case "getDate(String)":

                return remoteValue(((CallableStatement)context.statement(pId)).getDate((String)pArgs[0]));
            case "getDate(String,Calendar)":

                return remoteValue(((CallableStatement)context.statement(pId)).getDate((String)pArgs[0], (Calendar)pArgs[1]));
            case "getDate(int)":

                return remoteValue(((CallableStatement)context.statement(pId)).getDate((int)pArgs[0]));
            case "getDate(int,Calendar)":

                return remoteValue(((CallableStatement)context.statement(pId)).getDate((int)pArgs[0], (Calendar)pArgs[1]));
            case "getDouble(String)":

                return remoteValue(((CallableStatement)context.statement(pId)).getDouble((String)pArgs[0]));
            case "getDouble(int)":

                return remoteValue(((CallableStatement)context.statement(pId)).getDouble((int)pArgs[0]));
            case "getFloat(String)":

                return remoteValue(((CallableStatement)context.statement(pId)).getFloat((String)pArgs[0]));
            case "getFloat(int)":

                return remoteValue(((CallableStatement)context.statement(pId)).getFloat((int)pArgs[0]));
            case "getInt(String)":

                return remoteValue(((CallableStatement)context.statement(pId)).getInt((String)pArgs[0]));
            case "getInt(int)":

                return remoteValue(((CallableStatement)context.statement(pId)).getInt((int)pArgs[0]));
            case "getLong(String)":

                return remoteValue(((CallableStatement)context.statement(pId)).getLong((String)pArgs[0]));
            case "getLong(int)":

                return remoteValue(((CallableStatement)context.statement(pId)).getLong((int)pArgs[0]));
            case "getNCharacterStream(String)":

                return remoteValue(((CallableStatement)context.statement(pId)).getNCharacterStream((String)pArgs[0]));
            case "getNCharacterStream(int)":

                return remoteValue(((CallableStatement)context.statement(pId)).getNCharacterStream((int)pArgs[0]));
            case "getNClob(String)":

                return remoteValue(((CallableStatement)context.statement(pId)).getNClob((String)pArgs[0]));
            case "getNClob(int)":

                return remoteValue(((CallableStatement)context.statement(pId)).getNClob((int)pArgs[0]));
            case "getNString(String)":

                return remoteValue(((CallableStatement)context.statement(pId)).getNString((String)pArgs[0]));
            case "getNString(int)":

                return remoteValue(((CallableStatement)context.statement(pId)).getNString((int)pArgs[0]));
            case "getObject(String)":

                return remoteValue(((CallableStatement)context.statement(pId)).getObject((String)pArgs[0]));
            case "getObject(String,Class)":

                return remoteValue(((CallableStatement)context.statement(pId)).getObject((String)pArgs[0], (Class)pArgs[1]));
            case "getObject(String,Map)":

                return remoteValue(((CallableStatement)context.statement(pId)).getObject((String)pArgs[0], (Map)pArgs[1]));
            case "getObject(int)":

                return remoteValue(((CallableStatement)context.statement(pId)).getObject((int)pArgs[0]));
            case "getObject(int,Class)":

                return remoteValue(((CallableStatement)context.statement(pId)).getObject((int)pArgs[0], (Class)pArgs[1]));
            case "getObject(int,Map)":

                return remoteValue(((CallableStatement)context.statement(pId)).getObject((int)pArgs[0], (Map)pArgs[1]));
            case "getRef(String)":

                return remoteValue(((CallableStatement)context.statement(pId)).getRef((String)pArgs[0]));
            case "getRef(int)":

                return remoteValue(((CallableStatement)context.statement(pId)).getRef((int)pArgs[0]));
            case "getRowId(String)":

                return remoteValue(((CallableStatement)context.statement(pId)).getRowId((String)pArgs[0]));
            case "getRowId(int)":

                return remoteValue(((CallableStatement)context.statement(pId)).getRowId((int)pArgs[0]));
            case "getSQLXML(String)":

                return remoteValue(((CallableStatement)context.statement(pId)).getSQLXML((String)pArgs[0]));
            case "getSQLXML(int)":

                return remoteValue(((CallableStatement)context.statement(pId)).getSQLXML((int)pArgs[0]));
            case "getShort(String)":

                return remoteValue(((CallableStatement)context.statement(pId)).getShort((String)pArgs[0]));
            case "getShort(int)":

                return remoteValue(((CallableStatement)context.statement(pId)).getShort((int)pArgs[0]));
            case "getString(String)":

                return remoteValue(((CallableStatement)context.statement(pId)).getString((String)pArgs[0]));
            case "getString(int)":

                return remoteValue(((CallableStatement)context.statement(pId)).getString((int)pArgs[0]));
            case "getTime(String)":

                return remoteValue(((CallableStatement)context.statement(pId)).getTime((String)pArgs[0]));
            case "getTime(String,Calendar)":

                return remoteValue(((CallableStatement)context.statement(pId)).getTime((String)pArgs[0], (Calendar)pArgs[1]));
            case "getTime(int)":

                return remoteValue(((CallableStatement)context.statement(pId)).getTime((int)pArgs[0]));
            case "getTime(int,Calendar)":

                return remoteValue(((CallableStatement)context.statement(pId)).getTime((int)pArgs[0], (Calendar)pArgs[1]));
            case "getTimestamp(String)":

                return remoteValue(((CallableStatement)context.statement(pId)).getTimestamp((String)pArgs[0]));
            case "getTimestamp(String,Calendar)":

                return remoteValue(((CallableStatement)context.statement(pId)).getTimestamp((String)pArgs[0], (Calendar)pArgs[1]));
            case "getTimestamp(int)":

                return remoteValue(((CallableStatement)context.statement(pId)).getTimestamp((int)pArgs[0]));
            case "getTimestamp(int,Calendar)":

                return remoteValue(((CallableStatement)context.statement(pId)).getTimestamp((int)pArgs[0], (Calendar)pArgs[1]));
            case "getURL(String)":

                return remoteValue(((CallableStatement)context.statement(pId)).getURL((String)pArgs[0]));
            case "getURL(int)":

                return remoteValue(((CallableStatement)context.statement(pId)).getURL((int)pArgs[0]));
            case "registerOutParameter(String,SQLType)":
                ((CallableStatement)context.statement(pId)).registerOutParameter((String)pArgs[0], (SQLType)pArgs[1]);

                return null;
            case "registerOutParameter(String,SQLType,String)":
                ((CallableStatement)context.statement(pId)).registerOutParameter((String)pArgs[0], (SQLType)pArgs[1], (String)pArgs[2]);

                return null;
            case "registerOutParameter(String,SQLType,int)":
                ((CallableStatement)context.statement(pId)).registerOutParameter((String)pArgs[0], (SQLType)pArgs[1], (int)pArgs[2]);

                return null;
            case "registerOutParameter(String,int)":
                ((CallableStatement)context.statement(pId)).registerOutParameter((String)pArgs[0], (int)pArgs[1]);

                return null;
            case "registerOutParameter(String,int,String)":
                ((CallableStatement)context.statement(pId)).registerOutParameter((String)pArgs[0], (int)pArgs[1], (String)pArgs[2]);

                return null;
            case "registerOutParameter(String,int,int)":
                ((CallableStatement)context.statement(pId)).registerOutParameter((String)pArgs[0], (int)pArgs[1], (int)pArgs[2]);

                return null;
            case "registerOutParameter(int,SQLType)":
                ((CallableStatement)context.statement(pId)).registerOutParameter((int)pArgs[0], (SQLType)pArgs[1]);

                return null;
            case "registerOutParameter(int,SQLType,String)":
                ((CallableStatement)context.statement(pId)).registerOutParameter((int)pArgs[0], (SQLType)pArgs[1], (String)pArgs[2]);

                return null;
            case "registerOutParameter(int,SQLType,int)":
                ((CallableStatement)context.statement(pId)).registerOutParameter((int)pArgs[0], (SQLType)pArgs[1], (int)pArgs[2]);

                return null;
            case "registerOutParameter(int,int)":
                ((CallableStatement)context.statement(pId)).registerOutParameter((int)pArgs[0], (int)pArgs[1]);

                return null;
            case "registerOutParameter(int,int,String)":
                ((CallableStatement)context.statement(pId)).registerOutParameter((int)pArgs[0], (int)pArgs[1], (String)pArgs[2]);

                return null;
            case "registerOutParameter(int,int,int)":
                ((CallableStatement)context.statement(pId)).registerOutParameter((int)pArgs[0], (int)pArgs[1], (int)pArgs[2]);

                return null;
            case "setAsciiStream(String,InputStream)":
                ((CallableStatement)context.statement(pId)).setAsciiStream((String)pArgs[0], (InputStream)pArgs[1]);

                return null;
            case "setAsciiStream(String,InputStream,int)":
                ((CallableStatement)context.statement(pId)).setAsciiStream((String)pArgs[0], (InputStream)pArgs[1], (int)pArgs[2]);

                return null;
            case "setAsciiStream(String,InputStream,long)":
                ((CallableStatement)context.statement(pId)).setAsciiStream((String)pArgs[0], (InputStream)pArgs[1], (long)pArgs[2]);

                return null;
            case "setBigDecimal(String,BigDecimal)":
                ((CallableStatement)context.statement(pId)).setBigDecimal((String)pArgs[0], (BigDecimal)pArgs[1]);

                return null;
            case "setBinaryStream(String,InputStream)":
                ((CallableStatement)context.statement(pId)).setBinaryStream((String)pArgs[0], (InputStream)pArgs[1]);

                return null;
            case "setBinaryStream(String,InputStream,int)":
                ((CallableStatement)context.statement(pId)).setBinaryStream((String)pArgs[0], (InputStream)pArgs[1], (int)pArgs[2]);

                return null;
            case "setBinaryStream(String,InputStream,long)":
                ((CallableStatement)context.statement(pId)).setBinaryStream((String)pArgs[0], (InputStream)pArgs[1], (long)pArgs[2]);

                return null;
            case "setBlob(String,Blob)":
                ((CallableStatement)context.statement(pId)).setBlob((String)pArgs[0], (Blob)pArgs[1]);

                return null;
            case "setBlob(String,InputStream)":
                ((CallableStatement)context.statement(pId)).setBlob((String)pArgs[0], (InputStream)pArgs[1]);

                return null;
            case "setBlob(String,InputStream,long)":
                ((CallableStatement)context.statement(pId)).setBlob((String)pArgs[0], (InputStream)pArgs[1], (long)pArgs[2]);

                return null;
            case "setBoolean(String,boolean)":
                ((CallableStatement)context.statement(pId)).setBoolean((String)pArgs[0], (boolean)pArgs[1]);

                return null;
            case "setByte(String,byte)":
                ((CallableStatement)context.statement(pId)).setByte((String)pArgs[0], (byte)pArgs[1]);

                return null;
            case "setBytes(String,byte[])":
                ((CallableStatement)context.statement(pId)).setBytes((String)pArgs[0], (byte[]) pArgs[1]);

                return null;
            case "setCharacterStream(String,Reader)":
                ((CallableStatement)context.statement(pId)).setCharacterStream((String)pArgs[0], (Reader)pArgs[1]);

                return null;
            case "setCharacterStream(String,Reader,int)":
                ((CallableStatement)context.statement(pId)).setCharacterStream((String)pArgs[0], (Reader)pArgs[1], (int)pArgs[2]);

                return null;
            case "setCharacterStream(String,Reader,long)":
                ((CallableStatement)context.statement(pId)).setCharacterStream((String)pArgs[0], (Reader)pArgs[1], (long)pArgs[2]);

                return null;
            case "setClob(String,Clob)":
                ((CallableStatement)context.statement(pId)).setClob((String)pArgs[0], (Clob)pArgs[1]);

                return null;
            case "setClob(String,Reader)":
                ((CallableStatement)context.statement(pId)).setClob((String)pArgs[0], (Reader)pArgs[1]);

                return null;
            case "setClob(String,Reader,long)":
                ((CallableStatement)context.statement(pId)).setClob((String)pArgs[0], (Reader)pArgs[1], (long)pArgs[2]);

                return null;
            case "setDate(String,Date)":
                ((CallableStatement)context.statement(pId)).setDate((String)pArgs[0], (java.sql.Date) pArgs[1]);

                return null;
            case "setDate(String,Date,Calendar)":
                ((CallableStatement)context.statement(pId)).setDate((String)pArgs[0], (java.sql.Date) pArgs[1], (Calendar)pArgs[2]);

                return null;
            case "setDouble(String,double)":
                ((CallableStatement)context.statement(pId)).setDouble((String)pArgs[0], (double)pArgs[1]);

                return null;
            case "setFloat(String,float)":
                ((CallableStatement)context.statement(pId)).setFloat((String)pArgs[0], (float)pArgs[1]);

                return null;
            case "setInt(String,int)":
                ((CallableStatement)context.statement(pId)).setInt((String)pArgs[0], (int)pArgs[1]);

                return null;
            case "setLong(String,long)":
                ((CallableStatement)context.statement(pId)).setLong((String)pArgs[0], (long)pArgs[1]);

                return null;
            case "setNCharacterStream(String,Reader)":
                ((CallableStatement)context.statement(pId)).setNCharacterStream((String)pArgs[0], (Reader)pArgs[1]);

                return null;
            case "setNCharacterStream(String,Reader,long)":
                ((CallableStatement)context.statement(pId)).setNCharacterStream((String)pArgs[0], (Reader)pArgs[1], (long)pArgs[2]);

                return null;
            case "setNClob(String,NClob)":
                ((CallableStatement)context.statement(pId)).setNClob((String)pArgs[0], (NClob)pArgs[1]);

                return null;
            case "setNClob(String,Reader)":
                ((CallableStatement)context.statement(pId)).setNClob((String)pArgs[0], (Reader)pArgs[1]);

                return null;
            case "setNClob(String,Reader,long)":
                ((CallableStatement)context.statement(pId)).setNClob((String)pArgs[0], (Reader)pArgs[1], (long)pArgs[2]);

                return null;
            case "setNString(String,String)":
                ((CallableStatement)context.statement(pId)).setNString((String)pArgs[0], (String)pArgs[1]);

                return null;
            case "setNull(String,int)":
                ((CallableStatement)context.statement(pId)).setNull((String)pArgs[0], (int)pArgs[1]);

                return null;
            case "setNull(String,int,String)":
                ((CallableStatement)context.statement(pId)).setNull((String)pArgs[0], (int)pArgs[1], (String)pArgs[2]);

                return null;
            case "setObject(String,Object)":
                ((CallableStatement)context.statement(pId)).setObject((String)pArgs[0], (Object)pArgs[1]);

                return null;
            case "setObject(String,Object,SQLType)":
                ((CallableStatement)context.statement(pId)).setObject((String)pArgs[0], (Object)pArgs[1], (SQLType)pArgs[2]);

                return null;
            case "setObject(String,Object,SQLType,int)":
                ((CallableStatement)context.statement(pId)).setObject((String)pArgs[0], (Object)pArgs[1], (SQLType)pArgs[2], (int)pArgs[3]);

                return null;
            case "setObject(String,Object,int)":
                ((CallableStatement)context.statement(pId)).setObject((String)pArgs[0], (Object)pArgs[1], (int)pArgs[2]);

                return null;
            case "setObject(String,Object,int,int)":
                ((CallableStatement)context.statement(pId)).setObject((String)pArgs[0], (Object)pArgs[1], (int)pArgs[2], (int)pArgs[3]);

                return null;
            case "setRowId(String,RowId)":
                ((CallableStatement)context.statement(pId)).setRowId((String)pArgs[0], (RowId)pArgs[1]);

                return null;
            case "setSQLXML(String,SQLXML)":
                ((CallableStatement)context.statement(pId)).setSQLXML((String)pArgs[0], (SQLXML)pArgs[1]);

                return null;
            case "setShort(String,short)":
                ((CallableStatement)context.statement(pId)).setShort((String)pArgs[0], (short)pArgs[1]);

                return null;
            case "setString(String,String)":
                ((CallableStatement)context.statement(pId)).setString((String)pArgs[0], (String)pArgs[1]);

                return null;
            case "setTime(String,Time)":
                ((CallableStatement)context.statement(pId)).setTime((String)pArgs[0], (Time)pArgs[1]);

                return null;
            case "setTime(String,Time,Calendar)":
                ((CallableStatement)context.statement(pId)).setTime((String)pArgs[0], (Time)pArgs[1], (Calendar)pArgs[2]);

                return null;
            case "setTimestamp(String,Timestamp)":
                ((CallableStatement)context.statement(pId)).setTimestamp((String)pArgs[0], (Timestamp)pArgs[1]);

                return null;
            case "setTimestamp(String,Timestamp,Calendar)":
                ((CallableStatement)context.statement(pId)).setTimestamp((String)pArgs[0], (Timestamp)pArgs[1], (Calendar)pArgs[2]);

                return null;
            case "setURL(String,URL)":
                ((CallableStatement)context.statement(pId)).setURL((String)pArgs[0], (URL)pArgs[1]);

                return null;
            case "wasNull()":

                return remoteValue(((CallableStatement)context.statement(pId)).wasNull());
                
            default:
                //CallableStatement extends PreparedStatement. Reuse the typed PreparedStatement delegate for inherited methods; 
            	//it in turndelegates inherited Statement methods to StatementDelegate.

                return new PreparedStatementDelegate(context).call(pId, pSignature, pArgs);
        }
    }
}
