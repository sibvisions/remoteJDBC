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
import java.net.URL;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.NClob;
import java.sql.PreparedStatement;
import java.sql.Ref;
import java.sql.RowId;
import java.sql.SQLException;
import java.sql.SQLType;
import java.sql.SQLXML;
import java.util.Calendar;

import com.sibvisions.rjdbc.server.JdbcContext;

/**
 * Handles the to sql date operation for the remote JDBC resource.
 * 
 * @author René Jahn
 */
public final class PreparedStatementDelegate extends JdbcDelegate
{
    /**
     * Creates a new {@code PreparedStatementDelegate} instance.
     *
     * @param pContext the context
     */
    public PreparedStatementDelegate(JdbcContext pContext)
    {
        super(pContext);
    }

    /**
     * Handles the "to sql date" operation for the remote JDBC resource.
     *
     * @param pValue the value to convert
     * @return the resulting JDBC value
     */
    private static java.sql.Date toSqlDate(Object pValue)
    {
        if (pValue == null)
        {
            return null;
        }

        if (pValue instanceof java.sql.Date && !(pValue instanceof java.sql.Timestamp))
        {
            return (java.sql.Date) pValue;
        }

        if (pValue instanceof java.util.Date)
        {
            return new java.sql.Date(((java.util.Date) pValue).getTime());
        }
        
        throw new IllegalArgumentException("Expected date value but got " + pValue.getClass().getName());
    }

    /**
     * Handles the "to sql time" operation for the remote JDBC resource.
     *
     * @param pValue the value to convert
     * @return the resulting JDBC value
     */
    private static java.sql.Time toSqlTime(Object pValue)
    {
        if (pValue == null)
        {
            return null;
        }

        if (pValue instanceof java.sql.Time)
        {
            return (java.sql.Time) pValue;
        }

        if (pValue instanceof java.util.Date)
        {
            return new java.sql.Time(((java.util.Date) pValue).getTime());
        }
        
        throw new IllegalArgumentException("Expected time value but got " + pValue.getClass().getName());
    }

    /**
     * Handles the to sql timestamp operation for the remote JDBC resource.
     *
     * @param pValue the value to convert
     * @return the resulting JDBC value
     */
    private static java.sql.Timestamp toSqlTimestamp(Object pValue)
    {
        if (pValue == null)
        {
            return null;
        }

        if (pValue instanceof java.sql.Timestamp)
        {
            return (java.sql.Timestamp) pValue;
        }

        if (pValue instanceof java.util.Date)
        {
            return new java.sql.Timestamp(((java.util.Date) pValue).getTime());
        }
        
        throw new IllegalArgumentException("Expected timestamp value but got " + pValue.getClass().getName());
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
    @SuppressWarnings("deprecation")
	public Object call(long pId, String pSignature, Object[] pArgs) throws SQLException
    {
        switch (pSignature)
        {
            case "addBatch()":
                ((PreparedStatement)context.statement(pId)).addBatch();

                return null;
            case "clearParameters()":
                ((PreparedStatement)context.statement(pId)).clearParameters();

                return null;
            case "execute()": return remoteValue(((PreparedStatement)context.statement(pId)).execute());
            case "executeLargeUpdate()": return remoteValue(((PreparedStatement)context.statement(pId)).executeLargeUpdate());
            case "executeQuery()": return remoteValue(((PreparedStatement)context.statement(pId)).executeQuery());
            case "executeUpdate()": return remoteValue(((PreparedStatement)context.statement(pId)).executeUpdate());
            case "getMetaData()": return remoteValue(((PreparedStatement)context.statement(pId)).getMetaData());
            case "getParameterMetaData()": return remoteValue(((PreparedStatement)context.statement(pId)).getParameterMetaData());
            case "setArray(int,Array)":
                ((PreparedStatement)context.statement(pId)).setArray((int)pArgs[0], (Array)pArgs[1]);

                return null;
            case "setAsciiStream(int,InputStream)":
                ((PreparedStatement)context.statement(pId)).setAsciiStream((int)pArgs[0], (InputStream)pArgs[1]);

                return null;
            case "setAsciiStream(int,InputStream,int)":
                ((PreparedStatement)context.statement(pId)).setAsciiStream((int)pArgs[0], (InputStream)pArgs[1], (int)pArgs[2]);

                return null;
            case "setAsciiStream(int,InputStream,long)":
                ((PreparedStatement)context.statement(pId)).setAsciiStream((int)pArgs[0], (InputStream)pArgs[1], (long)pArgs[2]);

                return null;
            case "setBigDecimal(int,BigDecimal)":
                ((PreparedStatement)context.statement(pId)).setBigDecimal((int)pArgs[0], (BigDecimal)pArgs[1]);

                return null;
            case "setBinaryStream(int,InputStream)":
                ((PreparedStatement)context.statement(pId)).setBinaryStream((int)pArgs[0], (InputStream)pArgs[1]);

                return null;
            case "setBinaryStream(int,InputStream,int)":
                ((PreparedStatement)context.statement(pId)).setBinaryStream((int)pArgs[0], (InputStream)pArgs[1], (int)pArgs[2]);

                return null;
            case "setBinaryStream(int,InputStream,long)":
                ((PreparedStatement)context.statement(pId)).setBinaryStream((int)pArgs[0], (InputStream)pArgs[1], (long)pArgs[2]);

                return null;
            case "setBlob(int,Blob)":
                ((PreparedStatement)context.statement(pId)).setBlob((int)pArgs[0], (Blob)pArgs[1]);

                return null;
            case "setBlob(int,InputStream)":
                ((PreparedStatement)context.statement(pId)).setBlob((int)pArgs[0], (InputStream)pArgs[1]);

                return null;
            case "setBlob(int,InputStream,long)":
                ((PreparedStatement)context.statement(pId)).setBlob((int)pArgs[0], (InputStream)pArgs[1], (long)pArgs[2]);

                return null;
            case "setBoolean(int,boolean)":
                ((PreparedStatement)context.statement(pId)).setBoolean((int)pArgs[0], (boolean)pArgs[1]);

                return null;
            case "setByte(int,byte)":
                ((PreparedStatement)context.statement(pId)).setByte((int)pArgs[0], (byte)pArgs[1]);

                return null;
            case "setBytes(int,byte[])":
                ((PreparedStatement)context.statement(pId)).setBytes((int)pArgs[0], (byte[]) pArgs[1]);

                return null;
            case "setCharacterStream(int,Reader)":
                ((PreparedStatement)context.statement(pId)).setCharacterStream((int)pArgs[0], (Reader)pArgs[1]);

                return null;
            case "setCharacterStream(int,Reader,int)":
                ((PreparedStatement)context.statement(pId)).setCharacterStream((int)pArgs[0], (Reader)pArgs[1], (int)pArgs[2]);

                return null;
            case "setCharacterStream(int,Reader,long)":
                ((PreparedStatement)context.statement(pId)).setCharacterStream((int)pArgs[0], (Reader)pArgs[1], (long)pArgs[2]);

                return null;
            case "setClob(int,Clob)":
                ((PreparedStatement)context.statement(pId)).setClob((int)pArgs[0], (Clob)pArgs[1]);

                return null;
            case "setClob(int,Reader)":
                ((PreparedStatement)context.statement(pId)).setClob((int)pArgs[0], (Reader)pArgs[1]);

                return null;
            case "setClob(int,Reader,long)":
                ((PreparedStatement)context.statement(pId)).setClob((int)pArgs[0], (Reader)pArgs[1], (long)pArgs[2]);

                return null;
            case "setDate(int,Date)":
                ((PreparedStatement)context.statement(pId)).setDate((int)pArgs[0], toSqlDate(pArgs[1]));

                return null;
            case "setDate(int,Date,Calendar)":
                ((PreparedStatement)context.statement(pId)).setDate((int)pArgs[0], toSqlDate(pArgs[1]), (Calendar)pArgs[2]);

                return null;
            case "setDouble(int,double)":
                ((PreparedStatement)context.statement(pId)).setDouble((int)pArgs[0], (double)pArgs[1]);

                return null;
            case "setFloat(int,float)":
                ((PreparedStatement)context.statement(pId)).setFloat((int)pArgs[0], (float)pArgs[1]);

                return null;
            case "setInt(int,int)":
                ((PreparedStatement)context.statement(pId)).setInt((int)pArgs[0], (int)pArgs[1]);

                return null;
            case "setLong(int,long)":
                ((PreparedStatement)context.statement(pId)).setLong((int)pArgs[0], (long)pArgs[1]);

                return null;
            case "setNCharacterStream(int,Reader)":
                ((PreparedStatement)context.statement(pId)).setNCharacterStream((int)pArgs[0], (Reader)pArgs[1]);

                return null;
            case "setNCharacterStream(int,Reader,long)":
                ((PreparedStatement)context.statement(pId)).setNCharacterStream((int)pArgs[0], (Reader)pArgs[1], (long)pArgs[2]);

                return null;
            case "setNClob(int,NClob)":
                ((PreparedStatement)context.statement(pId)).setNClob((int)pArgs[0], (NClob)pArgs[1]);

                return null;
            case "setNClob(int,Reader)":
                ((PreparedStatement)context.statement(pId)).setNClob((int)pArgs[0], (Reader)pArgs[1]);

                return null;
            case "setNClob(int,Reader,long)":
                ((PreparedStatement)context.statement(pId)).setNClob((int)pArgs[0], (Reader)pArgs[1], (long)pArgs[2]);

                return null;
            case "setNString(int,String)":
                ((PreparedStatement)context.statement(pId)).setNString((int)pArgs[0], (String)pArgs[1]);

                return null;
            case "setNull(int,int)":
                ((PreparedStatement)context.statement(pId)).setNull((int)pArgs[0], (int)pArgs[1]);

                return null;
            case "setNull(int,int,String)":
                ((PreparedStatement)context.statement(pId)).setNull((int)pArgs[0], (int)pArgs[1], (String)pArgs[2]);

                return null;
            case "setObject(int,Object)":
                ((PreparedStatement)context.statement(pId)).setObject((int)pArgs[0], (Object)pArgs[1]);

                return null;
            case "setObject(int,Object,SQLType)":
                ((PreparedStatement)context.statement(pId)).setObject((int)pArgs[0], (Object)pArgs[1], (SQLType)pArgs[2]);

                return null;
            case "setObject(int,Object,SQLType,int)":
                ((PreparedStatement)context.statement(pId)).setObject((int)pArgs[0], (Object)pArgs[1], (SQLType)pArgs[2], (int)pArgs[3]);

                return null;
            case "setObject(int,Object,int)":
                ((PreparedStatement)context.statement(pId)).setObject((int)pArgs[0], (Object)pArgs[1], (int)pArgs[2]);

                return null;
            case "setObject(int,Object,int,int)":
                ((PreparedStatement)context.statement(pId)).setObject((int)pArgs[0], (Object)pArgs[1], (int)pArgs[2], (int)pArgs[3]);

                return null;
            case "setRef(int,Ref)":
                ((PreparedStatement)context.statement(pId)).setRef((int)pArgs[0], (Ref)pArgs[1]);

                return null;
            case "setRowId(int,RowId)":
                ((PreparedStatement)context.statement(pId)).setRowId((int)pArgs[0], (RowId)pArgs[1]);

                return null;
            case "setSQLXML(int,SQLXML)":
                ((PreparedStatement)context.statement(pId)).setSQLXML((int)pArgs[0], (SQLXML)pArgs[1]);

                return null;
            case "setShort(int,short)":
                ((PreparedStatement)context.statement(pId)).setShort((int)pArgs[0], (short)pArgs[1]);

                return null;
            case "setString(int,String)":
                ((PreparedStatement)context.statement(pId)).setString((int)pArgs[0], (String)pArgs[1]);

                return null;
            case "setTime(int,Time)":
                ((PreparedStatement)context.statement(pId)).setTime((int)pArgs[0], toSqlTime(pArgs[1]));

                return null;
            case "setTime(int,Time,Calendar)":
                ((PreparedStatement)context.statement(pId)).setTime((int)pArgs[0], toSqlTime(pArgs[1]), (Calendar)pArgs[2]);

                return null;
            case "setTimestamp(int,Timestamp)":
                ((PreparedStatement)context.statement(pId)).setTimestamp((int)pArgs[0], toSqlTimestamp(pArgs[1]));

                return null;
            case "setTimestamp(int,Timestamp,Calendar)":
                ((PreparedStatement)context.statement(pId)).setTimestamp((int)pArgs[0], toSqlTimestamp(pArgs[1]), (Calendar)pArgs[2]);

                return null;
            case "setURL(int,URL)":
                ((PreparedStatement)context.statement(pId)).setURL((int)pArgs[0], (URL)pArgs[1]);

                return null;
            case "setUnicodeStream(int,InputStream,int)":
                ((PreparedStatement)context.statement(pId)).setUnicodeStream((int)pArgs[0], (InputStream)pArgs[1], (int)pArgs[2]);

                return null;
                
            default:
                // PreparedStatement inherits all Statement methods. Reuse the
                // typed Statement delegate for those methods instead of
                // duplicating the inherited API here.

                return new StatementDelegate(context).call(pId, pSignature, pArgs);
        }
    }
}
