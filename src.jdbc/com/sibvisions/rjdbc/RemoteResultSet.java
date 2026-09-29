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
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Date;
import java.sql.NClob;
import java.sql.Ref;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.RowId;
import java.sql.SQLException;
import java.sql.SQLType;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

/**
 * Remote JDBC implementation of {@code ResultSet} functionality.
 */
public class RemoteResultSet implements ResultSet
{
    protected final RemoteClient client;
    
    protected final long id;
    
    protected final Class<?> remoteInterface;

    private ResultSetMetaData metaDataCache;

    /** client-side row cache. A block is fetched with one remote request. */
    private Object[][] rowCache = new Object[0][];

    private int rowCacheIndex = -1;
    private int rowNumber = 0;
    private int fetchSize = 1000;

    /** true after a fetch has established that this ResultSet contains no rows. */
    private boolean emptyResultSet;
    private boolean endOfRows;
    private boolean closed;
    private boolean lastWasNull;
    private boolean onInsertRow;
    private boolean serverUpdatePositioned;

    /**
     * Creates a new {@code RemoteResultSet} instance.
     *
     * @param pClient the client
     * @param pId the remote resource identifier
     */
    public RemoteResultSet(RemoteClient pClient, long pId)
    {
        this(pClient, pId, ResultSet.class);
    }

    /**
     * Creates a new {@code RemoteResultSet} instance.
     *
     * @param pClient the client
     * @param pId the remote resource identifier
     * @param pRemoteInterface the remote interface
     */
    protected RemoteResultSet(RemoteClient pClient, long pId, Class<?> pRemoteInterface)
    {
        client = pClient;
        id = pId;
        
        remoteInterface = pRemoteInterface;
    }

    /** {@inheritDoc} */
    @Override
    public <T> T unwrap(Class<T> pIface) throws SQLException
    {
        ensureOpen();

        return RemoteUtil.unwrapLocal(this, pIface);
    }

    /** {@inheritDoc} */
    @Override
    public boolean isWrapperFor(Class<?> pIface) throws SQLException
    {
        ensureOpen();

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
    public boolean next() throws SQLException
    {
        ensureOpen();

        if (rowCacheIndex + 1 < rowCache.length)
        {
            rowCacheIndex++;
            rowNumber++;
            
            serverUpdatePositioned = false;
            lastWasNull = false;

            return true;
        }

        if (endOfRows)
        {
            rowCacheIndex = rowCache.length;
            rowNumber = 0;
            
            lastWasNull = false;

            return false;
        }

        int requestedFetchSize = fetchSize > 0 ? fetchSize : 1000;

        Map<String,Object> req = new HashMap<String,Object>();
        req.put(RemoteConstants.ACTION, RemoteConstants.FETCH_ROWS);
        req.put(RemoteConstants.ID, id);
        req.put("fetch", requestedFetchSize);

        Map<String,Object> response = client.call(req);
        Object rows = response.get("rows");

        if (!(rows instanceof Object[][]))
        {
            throw new SQLException("Invalid fetchRows response: " + rows);

        }
        rowCache = (Object[][])rows;

        if (rowCache.length == 0)
        {
            emptyResultSet = true;
            // An empty ResultSet is neither before-first nor after-last.
            // Keep the logical cursor outside the empty cache so the
            // position flags remain false while next() continues to return
            // false.
            rowCacheIndex = -1;
            rowNumber = 0;
            
            endOfRows = true;
            lastWasNull = false;

            return false;
        }
        
        emptyResultSet = false;
        rowCacheIndex = 0;
        rowNumber = rowNumber == 0 ? 1 : rowNumber + 1;
        serverUpdatePositioned = false;
        lastWasNull = false;

        Object end = response.get("endOfRows");
        
        endOfRows = end instanceof Boolean ? ((Boolean)end).booleanValue() : rowCache.length < requestedFetchSize;

        return true;
    }

    /** {@inheritDoc} */
    @Override
    public void close() throws SQLException
    {
        if (closed)
        {
            return;

        }
        
        RemoteUtil.invoke(client, id, remoteInterface, "close", new Class<?>[]{}, new Object[]{}, void.class);
        
        rowCache = new Object[0][];
        rowCacheIndex = 0;
        rowNumber = 0;
        endOfRows = true;
        lastWasNull = false;
        closed = true;
    }

    /** {@inheritDoc} */
    @Override
    public boolean wasNull() throws SQLException
    {
        ensureOpen();

        return lastWasNull;
    }

    /** {@inheritDoc} */
    @Override
    public String getString(int pColumnIndex) throws SQLException
    {
        Object value = value(pColumnIndex);

        if (value == null)
        {
            return null;
        }

        if (value instanceof String)
        {
            return (String)value;
        }

        if (value instanceof Clob)
        {
            Clob clob = (Clob)value;

            return clob.getSubString(1, (int)Math.min(Integer.MAX_VALUE, clob.length()));
        }

        return String.valueOf(value);
    }

    /** {@inheritDoc} */
    @Override
    public boolean getBoolean(int pColumnIndex) throws SQLException
    {
        return asBoolean(value(pColumnIndex));
    }

    /** {@inheritDoc} */
    @Override
    public byte getByte(int pColumnIndex) throws SQLException
    {
        return asNumber(value(pColumnIndex)).byteValue();
    }

    /** {@inheritDoc} */
    @Override
    public short getShort(int pColumnIndex) throws SQLException
    {
        return asNumber(value(pColumnIndex)).shortValue();
    }

    /** {@inheritDoc} */
    @Override
    public int getInt(int pColumnIndex) throws SQLException
    {
        return asNumber(value(pColumnIndex)).intValue();
    }

    /** {@inheritDoc} */
    @Override
    public long getLong(int pColumnIndex) throws SQLException
    {
        return asNumber(value(pColumnIndex)).longValue();
    }

    /** {@inheritDoc} */
    @Override
    public float getFloat(int pColumnIndex) throws SQLException
    {
        return asNumber(value(pColumnIndex)).floatValue();
    }

    /** {@inheritDoc} */
    @Override
    public double getDouble(int pColumnIndex) throws SQLException
    {
        return asNumber(value(pColumnIndex)).doubleValue();
    }

    /** {@inheritDoc} */
    @Override
    public BigDecimal getBigDecimal(int pColumnIndex, int pScale) throws SQLException
    {
        BigDecimal value = getBigDecimal(pColumnIndex);

        return value == null ? null : value.setScale(pScale, RoundingMode.HALF_UP);
    }

    /** {@inheritDoc} */
    @Override
    public byte[] getBytes(int pColumnIndex) throws SQLException
    {
        Object value = value(pColumnIndex);

        if (value == null)
        {
            return null;
        }

        if (value instanceof byte[])
        {
            return (byte[]) value;
        }

        if (value instanceof String)
        {
            return ((String)value).getBytes(java.nio.charset.StandardCharsets.UTF_8);

        }
        
        throw new SQLException("Column " + pColumnIndex + " is not convertible to bytes: " + value.getClass().getName());
    }

    /** {@inheritDoc} */
    @Override
    public Date getDate(int pColumnIndex) throws SQLException
    {
        Object value = value(pColumnIndex);

        if (value == null)
        {
            return null;
        }

        if (value instanceof Date)
        {
            return (Date)value;
        }

        if (value instanceof java.util.Date)
        {
            return new Date(((java.util.Date) value).getTime());
        }
        
        throw new SQLException("Column " + pColumnIndex + " is not a date: " + value.getClass().getName());
    }

    /** {@inheritDoc} */
    @Override
    public Time getTime(int pColumnIndex) throws SQLException
    {
        Object value = value(pColumnIndex);

        if (value == null)
        {
            return null;
        }

        if (value instanceof Time)
        {
            return (Time)value;
        }

        if (value instanceof java.util.Date)
        {
            return new Time(((java.util.Date) value).getTime());
        }
        
        throw new SQLException("Column " + pColumnIndex + " is not a time: " + value.getClass().getName());
    }

    /** {@inheritDoc} */
    @Override
    public Timestamp getTimestamp(int pColumnIndex) throws SQLException
    {
        Object value = value(pColumnIndex);

        if (value == null)
        {
            return null;
        }

        if (value instanceof Timestamp)
        {
            return (Timestamp)value;
        }

        if (value instanceof java.util.Date)
        {
            return new Timestamp(((java.util.Date) value).getTime());
        }
        
        throw new SQLException("Column " + pColumnIndex + " is not a timestamp: " + value.getClass().getName());
    }

    /** {@inheritDoc} */
    @Override
    public InputStream getAsciiStream(int pColumnIndex) throws SQLException
    {
        Object value = value(pColumnIndex);

        if (value == null)
        {
            return null;
        }

        if (value instanceof byte[])
        {
            return new ByteArrayInputStream((byte[]) value);
        }

        if (value instanceof String)
        {
            return new ByteArrayInputStream(((String)value).getBytes(StandardCharsets.US_ASCII));
        }

        if (value instanceof Clob)
        {
            return ((Clob)value).getAsciiStream();
        }
        
        throw new SQLException("Column " + pColumnIndex + " cannot be read as ASCII stream: " + value.getClass().getName());
    }

    /** {@inheritDoc} */
    @Override
    public InputStream getUnicodeStream(int pColumnIndex) throws SQLException
    {
        Object value = value(pColumnIndex);

        if (value == null)
        {
            return null;
        }

        if (value instanceof byte[])
        {
            return new ByteArrayInputStream((byte[]) value);
        }

        if (value instanceof String)
        {
            return new ByteArrayInputStream(((String)value).getBytes(StandardCharsets.UTF_8));
        }

        if (value instanceof Clob)
        {
            long length = ((Clob)value).length();

            if (length > Integer.MAX_VALUE)
            {
                throw new SQLException("Clob is too large");

            }

            return new ByteArrayInputStream(((Clob)value).getSubString(1, (int)length).getBytes(StandardCharsets.UTF_8));
        }
        
        throw new SQLException("Column " + pColumnIndex + " cannot be read as Unicode stream: " + value.getClass().getName());
    }

    /** {@inheritDoc} */
    @Override
    public InputStream getBinaryStream(int pColumnIndex) throws SQLException
    {
        Object value = value(pColumnIndex);

        if (value == null)
        {
            return null;
        }

        if (value instanceof byte[])
        {
            return new ByteArrayInputStream((byte[]) value);
        }

        if (value instanceof String)
        {
            return new ByteArrayInputStream(((String)value).getBytes(StandardCharsets.UTF_8));
        }

        if (value instanceof Blob)
        {
            return ((Blob)value).getBinaryStream();
        }
        
        throw new SQLException("Column " + pColumnIndex + " cannot be read as binary stream: " + value.getClass().getName());
    }

    /** {@inheritDoc} */
    @Override
    public String getString(String pColumnLabel) throws SQLException
    {
        return getString(columnIndex(pColumnLabel));
    }

    /** {@inheritDoc} */
    @Override
    public boolean getBoolean(String pColumnLabel) throws SQLException
    {
        return getBoolean(columnIndex(pColumnLabel));
    }

    /** {@inheritDoc} */
    @Override
    public byte getByte(String pColumnLabel) throws SQLException
    {
        return getByte(columnIndex(pColumnLabel));
    }

    /** {@inheritDoc} */
    @Override
    public short getShort(String pColumnLabel) throws SQLException
    {
        return getShort(columnIndex(pColumnLabel));
    }

    /** {@inheritDoc} */
    @Override
    public int getInt(String pColumnLabel) throws SQLException
    {
        return getInt(columnIndex(pColumnLabel));
    }

    /** {@inheritDoc} */
    @Override
    public long getLong(String pColumnLabel) throws SQLException
    {
        return getLong(columnIndex(pColumnLabel));
    }

    /** {@inheritDoc} */
    @Override
    public float getFloat(String pColumnLabel) throws SQLException
    {
        return getFloat(columnIndex(pColumnLabel));
    }

    /** {@inheritDoc} */
    @Override
    public double getDouble(String pColumnLabel) throws SQLException
    {
        return getDouble(columnIndex(pColumnLabel));
    }

    /** {@inheritDoc} */
    @Override
    public BigDecimal getBigDecimal(String pColumnLabel, int pScale) throws SQLException
    {
        return getBigDecimal(columnIndex(pColumnLabel), pScale);
    }

    /** {@inheritDoc} */
    @Override
    public byte[] getBytes(String pColumnLabel) throws SQLException
    {
        return getBytes(columnIndex(pColumnLabel));
    }

    /** {@inheritDoc} */
    @Override
    public Date getDate(String pColumnLabel) throws SQLException
    {
        return getDate(columnIndex(pColumnLabel));
    }

    /** {@inheritDoc} */
    @Override
    public Time getTime(String pColumnLabel) throws SQLException
    {
        return getTime(columnIndex(pColumnLabel));
    }

    /** {@inheritDoc} */
    @Override
    public Timestamp getTimestamp(String pColumnLabel) throws SQLException
    {
        return getTimestamp(columnIndex(pColumnLabel));
    }

    /** {@inheritDoc} */
    @Override
    public InputStream getAsciiStream(String pColumnLabel) throws SQLException
    {
        return getAsciiStream(columnIndex(pColumnLabel));
    }

    /** {@inheritDoc} */
    @Override
    public InputStream getUnicodeStream(String pColumnLabel) throws SQLException
    {
        return getUnicodeStream(columnIndex(pColumnLabel));
    }

    /** {@inheritDoc} */
    @Override
    public InputStream getBinaryStream(String pColumnLabel) throws SQLException
    {
        return getBinaryStream(columnIndex(pColumnLabel));
    }

    /** {@inheritDoc} */
    @Override
    public SQLWarning getWarnings() throws SQLException
    {
        ensureOpen();

        return (SQLWarning)RemoteUtil.invoke(client, id, remoteInterface, "getWarnings", new Class<?>[]{}, new Object[]{}, SQLWarning.class);
    }

    /** {@inheritDoc} */
    @Override
    public void clearWarnings() throws SQLException
    {
        ensureOpen();
        RemoteUtil.invoke(client, id, remoteInterface, "clearWarnings", new Class<?>[]{}, new Object[]{}, void.class);
    }

    /** {@inheritDoc} */
    @Override
    public String getCursorName() throws SQLException
    {
        ensureOpen();

        return (String)RemoteUtil.invoke(client, id, remoteInterface, "getCursorName", new Class<?>[]{}, new Object[]{}, String.class);
    }

    /** {@inheritDoc} */
    @Override
    public synchronized ResultSetMetaData getMetaData() throws SQLException
    {
        ensureOpen();

        if (metaDataCache == null)
        {
            metaDataCache = (ResultSetMetaData)RemoteUtil.invoke(client, id, remoteInterface, "getMetaData", new Class<?>[]{}, new Object[]{}, ResultSetMetaData.class);
            
            populateColumnCache(metaDataCache);
        }

        return metaDataCache;
    }

    /** {@inheritDoc} */
    @Override
    public Object getObject(int pColumnIndex) throws SQLException
    {
        return value(pColumnIndex);
    }

    /** {@inheritDoc} */
    @Override
    public Object getObject(String pColumnLabel) throws SQLException
    {
        return value(columnIndex(pColumnLabel));
    }

    /** {@inheritDoc} */
    @Override
    public int findColumn(String pColumnLabel) throws SQLException
    {
        return columnIndex(pColumnLabel);
    }

    /** {@inheritDoc} */
    @Override
    public Reader getCharacterStream(int pColumnIndex) throws SQLException
    {
        Object value = value(pColumnIndex);

        if (value == null)
        {
            return null;
        }

        if (value instanceof String)
        {
            return new StringReader((String)value);
        }

        if (value instanceof Clob)
        {
            return ((Clob)value).getCharacterStream();
        }
        
        throw new SQLException("Column " + pColumnIndex + " cannot be read as character stream: " + value.getClass().getName());
    }

    /** {@inheritDoc} */
    @Override
    public Reader getCharacterStream(String pColumnLabel) throws SQLException
    {
        return getCharacterStream(columnIndex(pColumnLabel));
    }

    /** {@inheritDoc} */
    @Override
    public BigDecimal getBigDecimal(int pColumnIndex) throws SQLException
    {
        return (BigDecimal)value(pColumnIndex);
    }

    /** {@inheritDoc} */
    @Override
    public BigDecimal getBigDecimal(String pColumnLabel) throws SQLException
    {
        return getBigDecimal(columnIndex(pColumnLabel));
    }

    /** {@inheritDoc} */
    @Override
    public boolean isBeforeFirst() throws SQLException
    {
        ensureOpen();
        
        // JDBC specifies false for an empty ResultSet. At construction time
        // we do not yet know whether the query returned any rows, so load the
        // first block once while keeping the logical cursor before the first
        // row. This lets next() consume the cached first row normally.

        if (rowNumber == 0 
        	&& rowCacheIndex < 0 
        	&& rowCache.length == 0 
        	&& !endOfRows)
        {
            boolean hasRow = next();

            if (hasRow)
            {
                rowCacheIndex = -1;
                rowNumber = 0;

                lastWasNull = false;
            }
        }

        return rowNumber == 0 && rowCacheIndex < 0 && rowCache.length > 0;
    }

    /** {@inheritDoc} */
    @Override
    public boolean isAfterLast() throws SQLException
    {
        ensureOpen();
        
        // An empty ResultSet is neither before-first nor after-last.
        // endOfRows alone is not sufficient because the initial fetch of an
        // empty ResultSet also sets it.

        if (emptyResultSet)
        {
            return false;

        }

        return endOfRows && rowCacheIndex >= rowCache.length;
    }

    /** {@inheritDoc} */
    @Override
    public boolean isFirst() throws SQLException
    {
        ensureOpen();

        return rowNumber == 1 && rowCacheIndex >= 0 && rowCacheIndex < rowCache.length;
    }

    /** {@inheritDoc} */
    @Override
    public boolean isLast() throws SQLException
    {
        ensureOpen();

        return rowCacheIndex >= 0 && rowCacheIndex < rowCache.length && endOfRows;
    }

    /** {@inheritDoc} */
    @Override
    public void beforeFirst() throws SQLException
    {
        ensureOpen();
        
        RemoteUtil.invoke(client, id, remoteInterface, "beforeFirst", new Class<?>[]{}, new Object[]{}, void.class);
        
        invalidateRows();
    }

    /** {@inheritDoc} */
    @Override
    public void afterLast() throws SQLException
    {
        ensureOpen();
        
        RemoteUtil.invoke(client, id, remoteInterface, "afterLast", new Class<?>[]{}, new Object[]{}, void.class);
        
        rowCache = new Object[0][];
        rowCacheIndex = 0;
        rowNumber = 0;
        endOfRows = true;
        lastWasNull = false;
    }

    /** {@inheritDoc} */
    @Override
    public boolean first() throws SQLException
    {
        ensureOpen();
        
        boolean result = (boolean)RemoteUtil.invoke(client, id, remoteInterface, "first", new Class<?>[]{}, new Object[]{}, boolean.class);

        return refreshCurrentRow(result);
    }

    /** {@inheritDoc} */
    @Override
    public boolean last() throws SQLException
    {
        ensureOpen();
        
        boolean result = (boolean)RemoteUtil.invoke(client, id, remoteInterface, "last", new Class<?>[]{}, new Object[]{}, boolean.class);

        return refreshCurrentRow(result);
    }

    /** {@inheritDoc} */
    @Override
    public int getRow() throws SQLException
    {
        ensureOpen();

        return rowNumber;
    }

    /** {@inheritDoc} */
    @Override
    public boolean absolute(int pRow) throws SQLException
    {
        ensureOpen();
        
        boolean result = (boolean)RemoteUtil.invoke(client, id, remoteInterface, "absolute", new Class<?>[]{int.class}, new Object[]{pRow}, boolean.class);

        return refreshCurrentRow(result);
    }

    /** {@inheritDoc} */
    @Override
    public boolean relative(int pRows) throws SQLException
    {
        ensureOpen();
        
        boolean result = (boolean)RemoteUtil.invoke(client, id, remoteInterface, "relative", new Class<?>[]{int.class}, new Object[]{pRows}, boolean.class);

        return refreshCurrentRow(result);
    }

    /** {@inheritDoc} */
    @Override
    public boolean previous() throws SQLException
    {
        ensureOpen();
        
        // The server-side cursor may already be beyond the complete client
        // cache because fetchRows() reads a whole block (plus one look-ahead
        // row). Move inside the local cache without touching the server.

        if (rowCacheIndex > 0 && rowCacheIndex < rowCache.length)
        {
            rowCacheIndex--;
            rowNumber--;
            
            lastWasNull = false;

            return true;
        }

        // At the first cached row, the server cursor is not necessarily on
        // this row anymore. Reposition explicitly to the preceding logical
        // row instead of calling server-side previous().

        if (rowCacheIndex == 0 && rowNumber > 1)
        {
            boolean result = (boolean)RemoteUtil.invoke(client, id, remoteInterface, "absolute", new Class<?>[]{int.class}, new Object[]{rowNumber - 1}, boolean.class);

            return refreshCurrentRow(result);
        }

        if (rowCacheIndex == 0 && rowNumber == 1)
        {
            RemoteUtil.invoke(client, id, remoteInterface, "beforeFirst", new Class<?>[]{}, new Object[]{}, void.class);
            
            invalidateRows();

            return false;
        }

        // If we are after-last (including after an empty/failed traversal),
        // the server cursor is positioned consistently and can move back to
        // the final row.
        boolean result = (boolean)RemoteUtil.invoke(client, id, remoteInterface, "previous", new Class<?>[]{}, new Object[]{}, boolean.class);

        return refreshCurrentRow(result);
    }

    /** {@inheritDoc} */
    @Override
    public void setFetchDirection(int pDirection) throws SQLException
    {
        ensureOpen();
        
        RemoteUtil.invoke(client, id, remoteInterface, "setFetchDirection", new Class<?>[]{int.class}, new Object[]{pDirection}, void.class);
    }

    /** {@inheritDoc} */
    @Override
    public int getFetchDirection() throws SQLException
    {
        ensureOpen();

        return (int)RemoteUtil.invoke(client, id, remoteInterface, "getFetchDirection", new Class<?>[]{}, new Object[]{}, int.class);
    }

    /** {@inheritDoc} */
    @Override
    public void setFetchSize(int pRows) throws SQLException
    {
        ensureOpen();

        if (pRows < 0)
        {
            throw new SQLException("fetchSize must not be negative");
        }
        
        fetchSize = pRows == 0 ? 100 : pRows;
    }

    /** {@inheritDoc} */
    @Override
    public int getFetchSize() throws SQLException
    {
        ensureOpen();

        return fetchSize;
    }

    /** {@inheritDoc} */
    @Override
    public int getType() throws SQLException
    {
        ensureOpen();

        return (int)RemoteUtil.invoke(client, id, remoteInterface, "getType", new Class<?>[]{}, new Object[]{}, int.class);
    }

    /** {@inheritDoc} */
    @Override
    public int getConcurrency() throws SQLException
    {
        ensureOpen();

        return (int)RemoteUtil.invoke(client, id, remoteInterface, "getConcurrency", new Class<?>[]{}, new Object[]{}, int.class);
    }

    /** {@inheritDoc} */
    @Override
    public boolean rowUpdated() throws SQLException
    {
        ensureOpen();

        return (boolean)RemoteUtil.invoke(client, id, remoteInterface, "rowUpdated", new Class<?>[]{}, new Object[]{}, boolean.class);
    }

    /** {@inheritDoc} */
    @Override
    public boolean rowInserted() throws SQLException
    {
        ensureOpen();

        return (boolean)RemoteUtil.invoke(client, id, remoteInterface, "rowInserted", new Class<?>[]{}, new Object[]{}, boolean.class);
    }

    /** {@inheritDoc} */
    @Override
    public boolean rowDeleted() throws SQLException
    {
        ensureOpen();

        return (boolean)RemoteUtil.invoke(client, id, remoteInterface, "rowDeleted", new Class<?>[]{}, new Object[]{}, boolean.class);
    }

    /** {@inheritDoc} */
    @Override
    public void updateNull(int pColumnIndex) throws SQLException
    {
        invokeCurrentRowUpdate("updateNull", new Class<?>[]{int.class}, new Object[]{pColumnIndex});
    }

    /** {@inheritDoc} */
    @Override
    public void updateBoolean(int pColumnIndex, boolean pValue) throws SQLException
    {
        invokeCurrentRowUpdate("updateBoolean", new Class<?>[]{int.class, boolean.class}, new Object[]{pColumnIndex, pValue});
    }

    /** {@inheritDoc} */
    @Override
    public void updateByte(int pColumnIndex, byte pValue) throws SQLException
    {
        invokeCurrentRowUpdate("updateByte", new Class<?>[]{int.class, byte.class}, new Object[]{pColumnIndex, pValue});
    }

    /** {@inheritDoc} */
    @Override
    public void updateShort(int pColumnIndex, short pValue) throws SQLException
    {
        invokeCurrentRowUpdate("updateShort", new Class<?>[]{int.class, short.class}, new Object[]{pColumnIndex, pValue});
    }

    /** {@inheritDoc} */
    @Override
    public void updateInt(int pColumnIndex, int pValue) throws SQLException
    {
        invokeCurrentRowUpdate("updateInt", new Class<?>[]{int.class, int.class}, new Object[]{pColumnIndex, pValue});
    }

    /** {@inheritDoc} */
    @Override
    public void updateLong(int pColumnIndex, long pValue) throws SQLException
    {
        invokeCurrentRowUpdate("updateLong", new Class<?>[]{int.class, long.class}, new Object[]{pColumnIndex, pValue});
    }

    /** {@inheritDoc} */
    @Override
    public void updateFloat(int pColumnIndex, float pValue) throws SQLException
    {
        invokeCurrentRowUpdate("updateFloat", new Class<?>[]{int.class, float.class}, new Object[]{pColumnIndex, pValue});
    }

    /** {@inheritDoc} */
    @Override
    public void updateDouble(int pColumnIndex, double pValue) throws SQLException
    {
        invokeCurrentRowUpdate("updateDouble", new Class<?>[]{int.class, double.class}, new Object[]{pColumnIndex, pValue});
    }

    /** {@inheritDoc} */
    @Override
    public void updateBigDecimal(int pColumnIndex, BigDecimal pValue) throws SQLException
    {
        invokeCurrentRowUpdate("updateBigDecimal", new Class<?>[]{int.class, BigDecimal.class}, new Object[]{pColumnIndex, pValue});
    }

    /** {@inheritDoc} */
    @Override
    public void updateString(int pColumnIndex, String pValue) throws SQLException
    {
        invokeCurrentRowUpdate("updateString", new Class<?>[]{int.class, String.class}, new Object[]{pColumnIndex, pValue});
    }

    /** {@inheritDoc} */
    @Override
    public void updateBytes(int pColumnIndex, byte[] pValue) throws SQLException
    {
        invokeCurrentRowUpdate("updateBytes", new Class<?>[]{int.class, byte[].class}, new Object[]{pColumnIndex, pValue});
    }

    /** {@inheritDoc} */
    @Override
    public void updateDate(int pColumnIndex, Date pValue) throws SQLException
    {
        invokeCurrentRowUpdate("updateDate", new Class<?>[]{int.class, Date.class}, new Object[]{pColumnIndex, pValue});
    }

    /** {@inheritDoc} */
    @Override
    public void updateTime(int pColumnIndex, Time pValue) throws SQLException
    {
        invokeCurrentRowUpdate("updateTime", new Class<?>[]{int.class, Time.class}, new Object[]{pColumnIndex, pValue});
    }

    /** {@inheritDoc} */
    @Override
    public void updateTimestamp(int pColumnIndex, Timestamp pValue) throws SQLException
    {
        invokeCurrentRowUpdate("updateTimestamp", new Class<?>[]{int.class, Timestamp.class}, new Object[]{pColumnIndex, pValue});
    }

    /** {@inheritDoc} */
    @Override
    public void updateAsciiStream(int pColumnIndex, InputStream pValue, int pLength) throws SQLException
    {
        invokeCurrentRowUpdate("updateAsciiStream", new Class<?>[]{int.class, InputStream.class, int.class}, new Object[]{pColumnIndex, pValue, pLength});
    }

    /** {@inheritDoc} */
    @Override
    public void updateBinaryStream(int pColumnIndex, InputStream pValue, int pLength) throws SQLException
    {
        invokeCurrentRowUpdate("updateBinaryStream", new Class<?>[]{int.class, InputStream.class, int.class}, new Object[]{pColumnIndex, pValue, pLength});
    }

    /** {@inheritDoc} */
    @Override
    public void updateCharacterStream(int pColumnIndex, Reader pValue, int pLength) throws SQLException
    {
        invokeCurrentRowUpdate("updateCharacterStream", new Class<?>[]{int.class, Reader.class, int.class}, new Object[]{pColumnIndex, pValue, pLength});
    }

    /** {@inheritDoc} */
    @Override
    public void updateObject(int pColumnIndex, Object pValue, int pScaleOrLength) throws SQLException
    {
        invokeCurrentRowUpdate("updateObject", new Class<?>[]{int.class, Object.class, int.class}, new Object[]{pColumnIndex, pValue, pScaleOrLength});
    }

    /** {@inheritDoc} */
    @Override
    public void updateObject(int pColumnIndex, Object pValue) throws SQLException
    {
        invokeCurrentRowUpdate("updateObject", new Class<?>[]{int.class, Object.class}, new Object[]{pColumnIndex, pValue});
    }

    /** {@inheritDoc} */
    @Override
    public void updateNull(String pColumnLabel) throws SQLException
    {
        invokeCurrentRowUpdate("updateNull", new Class<?>[]{String.class}, new Object[]{pColumnLabel});
    }

    /** {@inheritDoc} */
    @Override
    public void updateBoolean(String pColumnLabel, boolean pValue) throws SQLException
    {
        invokeCurrentRowUpdate("updateBoolean", new Class<?>[]{String.class, boolean.class}, new Object[]{pColumnLabel, pValue});
    }

    /** {@inheritDoc} */
    @Override
    public void updateByte(String pColumnLabel, byte pValue) throws SQLException
    {
        invokeCurrentRowUpdate("updateByte", new Class<?>[]{String.class, byte.class}, new Object[]{pColumnLabel, pValue});
    }

    /** {@inheritDoc} */
    @Override
    public void updateShort(String pColumnLabel, short pValue) throws SQLException
    {
        invokeCurrentRowUpdate("updateShort", new Class<?>[]{String.class, short.class}, new Object[]{pColumnLabel, pValue});
    }

    /** {@inheritDoc} */
    @Override
    public void updateInt(String pColumnLabel, int pValue) throws SQLException
    {
        invokeCurrentRowUpdate("updateInt", new Class<?>[]{String.class, int.class}, new Object[]{pColumnLabel, pValue});
    }

    /** {@inheritDoc} */
    @Override
    public void updateLong(String pColumnLabel, long pValue) throws SQLException
    {
        invokeCurrentRowUpdate("updateLong", new Class<?>[]{String.class, long.class}, new Object[]{pColumnLabel, pValue});
    }

    /** {@inheritDoc} */
    @Override
    public void updateFloat(String pColumnLabel, float pValue) throws SQLException
    {
        invokeCurrentRowUpdate("updateFloat", new Class<?>[]{String.class, float.class}, new Object[]{pColumnLabel, pValue});
    }

    /** {@inheritDoc} */
    @Override
    public void updateDouble(String pColumnLabel, double pValue) throws SQLException
    {
        invokeCurrentRowUpdate("updateDouble", new Class<?>[]{String.class, double.class}, new Object[]{pColumnLabel, pValue});
    }

    /** {@inheritDoc} */
    @Override
    public void updateBigDecimal(String pColumnLabel, BigDecimal pValue) throws SQLException
    {
        invokeCurrentRowUpdate("updateBigDecimal", new Class<?>[]{String.class, BigDecimal.class}, new Object[]{pColumnLabel, pValue});
    }

    /** {@inheritDoc} */
    @Override
    public void updateString(String pColumnLabel, String pValue) throws SQLException
    {
        invokeCurrentRowUpdate("updateString", new Class<?>[]{String.class, String.class}, new Object[]{pColumnLabel, pValue});
    }

    /** {@inheritDoc} */
    @Override
    public void updateBytes(String pColumnLabel, byte[] pValue) throws SQLException
    {
        invokeCurrentRowUpdate("updateBytes", new Class<?>[]{String.class, byte[].class}, new Object[]{pColumnLabel, pValue});
    }

    /** {@inheritDoc} */
    @Override
    public void updateDate(String pColumnLabel, Date pValue) throws SQLException
    {
        invokeCurrentRowUpdate("updateDate", new Class<?>[]{String.class, Date.class}, new Object[]{pColumnLabel, pValue});
    }

    /** {@inheritDoc} */
    @Override
    public void updateTime(String pColumnLabel, Time pValue) throws SQLException
    {
        invokeCurrentRowUpdate("updateTime", new Class<?>[]{String.class, Time.class}, new Object[]{pColumnLabel, pValue});
    }

    /** {@inheritDoc} */
    @Override
    public void updateTimestamp(String pColumnLabel, Timestamp pValue) throws SQLException
    {
        invokeCurrentRowUpdate("updateTimestamp", new Class<?>[]{String.class, Timestamp.class}, new Object[]{pColumnLabel, pValue});
    }

    /** {@inheritDoc} */
    @Override
    public void updateAsciiStream(String pColumnLabel, InputStream pValue, int pLength) throws SQLException
    {
        invokeCurrentRowUpdate("updateAsciiStream", new Class<?>[]{String.class, InputStream.class, int.class}, new Object[]{pColumnLabel, pValue, pLength});
    }

    /** {@inheritDoc} */
    @Override
    public void updateBinaryStream(String pColumnLabel, InputStream pValue, int pLength) throws SQLException
    {
        invokeCurrentRowUpdate("updateBinaryStream", new Class<?>[]{String.class, InputStream.class, int.class}, new Object[]{pColumnLabel, pValue, pLength});
    }

    /** {@inheritDoc} */
    @Override
    public void updateCharacterStream(String pColumnLabel, Reader pValue, int pLength) throws SQLException
    {
        invokeCurrentRowUpdate("updateCharacterStream", new Class<?>[]{String.class, Reader.class, int.class}, new Object[]{pColumnLabel, pValue, pLength});
    }

    /** {@inheritDoc} */
    @Override
    public void updateObject(String pColumnLabel, Object pValue, int pScaleOrLength) throws SQLException
    {
        invokeCurrentRowUpdate("updateObject", new Class<?>[]{String.class, Object.class, int.class}, new Object[]{pColumnLabel, pValue, pScaleOrLength});
    }

    /** {@inheritDoc} */
    @Override
    public void updateObject(String pColumnLabel, Object pValue) throws SQLException
    {
        invokeCurrentRowUpdate("updateObject", new Class<?>[]{String.class, Object.class}, new Object[]{pColumnLabel, pValue});
    }

    /** {@inheritDoc} */
    @Override
    public void insertRow() throws SQLException
    {
        ensureOpen();
        
        RemoteUtil.invoke(client, id, remoteInterface, "insertRow", new Class<?>[]{}, new Object[]{}, void.class);
    }

    /** {@inheritDoc} */
    @Override
    public void updateRow() throws SQLException
    {
        invokeCurrentRowUpdate("updateRow", new Class<?>[]{}, new Object[]{});
        
        refreshCurrentRow(true);
    }

    /** {@inheritDoc} */
    @Override
    public void deleteRow() throws SQLException
    {
        ensureOpen();

        if (!onInsertRow && rowNumber > 0)
        {
            RemoteUtil.invoke(client, id, remoteInterface, "absolute", new Class<?>[]{int.class}, new Object[]{rowNumber}, boolean.class);
        }
        
        RemoteUtil.invoke(client, id, remoteInterface, "deleteRow", new Class<?>[]{}, new Object[]{}, void.class);
    }

    /** {@inheritDoc} */
    @Override
    public void refreshRow() throws SQLException
    {
        ensureOpen();

        if (!onInsertRow && rowNumber > 0)
        {
            RemoteUtil.invoke(client, id, remoteInterface, "absolute", new Class<?>[]{int.class}, new Object[]{rowNumber}, boolean.class);
        }
        
        RemoteUtil.invoke(client, id, remoteInterface, "refreshRow", new Class<?>[]{}, new Object[]{}, void.class);
        
        refreshCurrentRow(true);
    }

    /** {@inheritDoc} */
    @Override
    public void cancelRowUpdates() throws SQLException
    {
        ensureOpen();

        if (!onInsertRow && rowNumber > 0)
        {
            RemoteUtil.invoke(client, id, remoteInterface, "absolute", new Class<?>[]{int.class}, new Object[]{rowNumber}, boolean.class);
        }
        
        RemoteUtil.invoke(client, id, remoteInterface, "cancelRowUpdates", new Class<?>[]{}, new Object[]{}, void.class);
    }

    /** {@inheritDoc} */
    @Override
    public void moveToInsertRow() throws SQLException
    {
        ensureOpen();
        
        RemoteUtil.invoke(client, id, remoteInterface, "moveToInsertRow", new Class<?>[]{}, new Object[]{}, void.class);
        
        onInsertRow = true;
    }

    /** {@inheritDoc} */
    @Override
    public void moveToCurrentRow() throws SQLException
    {
        ensureOpen();
        
        RemoteUtil.invoke(client, id, remoteInterface, "moveToCurrentRow", new Class<?>[]{}, new Object[]{}, void.class);
        
        onInsertRow = false;
    }

    /** {@inheritDoc} */
    @Override
    public Statement getStatement() throws SQLException
    {
        ensureOpen();

        return (Statement)RemoteUtil.invoke(client, id, remoteInterface, "getStatement", new Class<?>[]{}, new Object[]{}, Statement.class);
    }

    /** {@inheritDoc} */
    @Override
    public Object getObject(int pColumnIndex, Map<String, Class<?>> pMap) throws SQLException
    {
        // The row cache already contains the JDBC value. Do not call the server
        // ResultSet again because fetchRows() has advanced its cursor.

        return value(pColumnIndex);
    }

    /** {@inheritDoc} */
    @Override
    public Ref getRef(int pColumnIndex) throws SQLException
    {
        return (Ref)value(pColumnIndex);
    }

    /** {@inheritDoc} */
    @Override
    public Blob getBlob(int pColumnIndex) throws SQLException
    {
        return (Blob)value(pColumnIndex);
    }

    /** {@inheritDoc} */
    @Override
    public Clob getClob(int pColumnIndex) throws SQLException
    {
        Object raw = value(pColumnIndex);

        if (raw == null || raw instanceof Clob)
        {
            return (Clob)raw;
        }
        
        throw new SQLException("Column " + pColumnIndex + " is not a Clob: " + raw.getClass().getName());
    }

    /** {@inheritDoc} */
    @Override
    public Array getArray(int pColumnIndex) throws SQLException
    {
        return (Array)value(pColumnIndex);
    }

    /** {@inheritDoc} */
    @Override
    public Object getObject(String pColumnLabel, Map<String, Class<?>> pMap) throws SQLException
    {
        return getObject(columnIndex(pColumnLabel), pMap);
    }

    /** {@inheritDoc} */
    @Override
    public Ref getRef(String pColumnLabel) throws SQLException
    {
        return getRef(columnIndex(pColumnLabel));
    }

    /** {@inheritDoc} */
    @Override
    public Blob getBlob(String pColumnLabel) throws SQLException
    {
        return getBlob(columnIndex(pColumnLabel));
    }

    /** {@inheritDoc} */
    @Override
    public Clob getClob(String pColumnLabel) throws SQLException
    {
        return getClob(columnIndex(pColumnLabel));
    }

    /** {@inheritDoc} */
    @Override
    public Array getArray(String pColumnLabel) throws SQLException
    {
        return getArray(columnIndex(pColumnLabel));
    }

    /** {@inheritDoc} */
    @Override
    public Date getDate(int pColumnIndex, Calendar pCalendar) throws SQLException
    {
        Object value = value(pColumnIndex);

        if (value == null)
        {
            return null;
        }

        if (value instanceof Date)
        {
            return new Date(((Date)value).getTime());
        }

        if (value instanceof java.util.Date)
        {
            return new Date(((java.util.Date) value).getTime());
        }
        
        throw new SQLException("Column " + pColumnIndex + " is not a date: " + value.getClass().getName());
    }

    /** {@inheritDoc} */
    @Override
    public Date getDate(String pColumnLabel, Calendar pCalendar) throws SQLException
    {
        return getDate(columnIndex(pColumnLabel), pCalendar);
    }

    /** {@inheritDoc} */
    @Override
    public Time getTime(int pColumnIndex, Calendar pCalendar) throws SQLException
    {
        Object value = value(pColumnIndex);

        if (value == null)
        {
            return null;
        }

        if (value instanceof Time)
        {
            return new Time(((Time)value).getTime());
        }

        if (value instanceof java.util.Date)
        {
            return new Time(((java.util.Date) value).getTime());
        }
        
        throw new SQLException("Column " + pColumnIndex + " is not a time: " + value.getClass().getName());
    }

    /** {@inheritDoc} */
    @Override
    public Time getTime(String pColumnLabel, Calendar pCalendar) throws SQLException
    {
        return getTime(columnIndex(pColumnLabel), pCalendar);
    }

    /** {@inheritDoc} */
    @Override
    public Timestamp getTimestamp(int pColumnIndex, Calendar pCalendar) throws SQLException
    {
        Object value = value(pColumnIndex);

        if (value == null)
        {
            return null;
        }

        if (value instanceof Timestamp)
        {
            return new Timestamp(((Timestamp)value).getTime());
        }

        if (value instanceof java.util.Date)
        {
            return new Timestamp(((java.util.Date) value).getTime());
        }
        
        throw new SQLException("Column " + pColumnIndex + " is not a timestamp: " + value.getClass().getName());
    }

    /** {@inheritDoc} */
    @Override
    public Timestamp getTimestamp(String pColumnLabel, Calendar pCalendar) throws SQLException
    {
        return getTimestamp(columnIndex(pColumnLabel), pCalendar);
    }

    /** {@inheritDoc} */
    @Override
    public URL getURL(int pColumnIndex) throws SQLException
    {
        Object raw = value(pColumnIndex);

        if (raw == null || raw instanceof URL)
        {
            return (URL)raw;
        }
        
        try
        {
            return new URL(String.valueOf(raw));
        }
        catch (Exception e)
        {
            throw new SQLException("Column " + pColumnIndex + " is not a valid URL", e);
        }

    }

    /** {@inheritDoc} */
    @Override
    public URL getURL(String pColumnLabel) throws SQLException
    {
        return getURL(columnIndex(pColumnLabel));
    }

    /** {@inheritDoc} */
    @Override
    public void updateRef(int pColumnIndex, Ref pValue) throws SQLException
    {
        invokeCurrentRowUpdate("updateRef", new Class<?>[]{int.class, Ref.class}, new Object[]{pColumnIndex, pValue});
    }

    /** {@inheritDoc} */
    @Override
    public void updateRef(String pColumnLabel, Ref pValue) throws SQLException
    {
        invokeCurrentRowUpdate("updateRef", new Class<?>[]{String.class, Ref.class}, new Object[]{pColumnLabel, pValue});
    }

    /** {@inheritDoc} */
    @Override
    public void updateBlob(int pColumnIndex, Blob pValue) throws SQLException
    {
        invokeCurrentRowUpdate("updateBlob", new Class<?>[]{int.class, Blob.class}, new Object[]{pColumnIndex, pValue});
    }

    /** {@inheritDoc} */
    @Override
    public void updateBlob(String pColumnLabel, Blob pValue) throws SQLException
    {
        invokeCurrentRowUpdate("updateBlob", new Class<?>[]{String.class, Blob.class}, new Object[]{pColumnLabel, pValue});
    }

    /** {@inheritDoc} */
    @Override
    public void updateClob(int pColumnIndex, Clob pValue) throws SQLException
    {
        invokeCurrentRowUpdate("updateClob", new Class<?>[]{int.class, Clob.class}, new Object[]{pColumnIndex, pValue});
    }

    /** {@inheritDoc} */
    @Override
    public void updateClob(String pColumnLabel, Clob pValue) throws SQLException
    {
        invokeCurrentRowUpdate("updateClob", new Class<?>[]{String.class, Clob.class}, new Object[]{pColumnLabel, pValue});
    }

    /** {@inheritDoc} */
    @Override
    public void updateArray(int pColumnIndex, Array pValue) throws SQLException
    {
        invokeCurrentRowUpdate("updateArray", new Class<?>[]{int.class, Array.class}, new Object[]{pColumnIndex, pValue});
    }

    /** {@inheritDoc} */
    @Override
    public void updateArray(String pColumnLabel, Array pValue) throws SQLException
    {
        invokeCurrentRowUpdate("updateArray", new Class<?>[]{String.class, Array.class}, new Object[]{pColumnLabel, pValue});
    }

    /** {@inheritDoc} */
    @Override
    public RowId getRowId(int pColumnIndex) throws SQLException
    {
        return (RowId)value(pColumnIndex);
    }

    /** {@inheritDoc} */
    @Override
    public RowId getRowId(String pColumnLabel) throws SQLException
    {
        return getRowId(columnIndex(pColumnLabel));
    }

    /** {@inheritDoc} */
    @Override
    public void updateRowId(int pColumnIndex, RowId pValue) throws SQLException
    {
        invokeCurrentRowUpdate("updateRowId", new Class<?>[]{int.class, RowId.class}, new Object[]{pColumnIndex, pValue});
    }

    /** {@inheritDoc} */
    @Override
    public void updateRowId(String pColumnLabel, RowId pValue) throws SQLException
    {
        invokeCurrentRowUpdate("updateRowId", new Class<?>[]{String.class, RowId.class}, new Object[]{pColumnLabel, pValue});
    }

    /** {@inheritDoc} */
    @Override
    public int getHoldability() throws SQLException
    {
        ensureOpen();

        return (int)RemoteUtil.invoke(client, id, remoteInterface, "getHoldability", new Class<?>[]{}, new Object[]{}, int.class);
    }

    /** {@inheritDoc} */
    @Override
    public boolean isClosed() throws SQLException
    {
        if (closed || client.isSessionClosed() || client.isSessionBroken())
        {
            return true;

        }

        return (boolean)RemoteUtil.invoke(client, id, remoteInterface, "isClosed", new Class<?>[]{}, new Object[]{}, boolean.class);
    }

    /** {@inheritDoc} */
    @Override
    public void updateNString(int pColumnIndex, String pValue) throws SQLException
    {
        invokeCurrentRowUpdate("updateNString", new Class<?>[]{int.class, String.class}, new Object[]{pColumnIndex, pValue});
    }

    /** {@inheritDoc} */
    @Override
    public void updateNString(String pColumnLabel, String pValue) throws SQLException
    {
        invokeCurrentRowUpdate("updateNString", new Class<?>[]{String.class, String.class}, new Object[]{pColumnLabel, pValue});
    }

    /** {@inheritDoc} */
    @Override
    public void updateNClob(int pColumnIndex, NClob pValue) throws SQLException
    {
        invokeCurrentRowUpdate("updateNClob", new Class<?>[]{int.class, NClob.class}, new Object[]{pColumnIndex, pValue});
    }

    /** {@inheritDoc} */
    @Override
    public void updateNClob(String pColumnLabel, NClob pValue) throws SQLException
    {
        invokeCurrentRowUpdate("updateNClob", new Class<?>[]{String.class, NClob.class}, new Object[]{pColumnLabel, pValue});
    }

    /** {@inheritDoc} */
    @Override
    public NClob getNClob(int pColumnIndex) throws SQLException
    {
        Object raw = value(pColumnIndex);

        if (raw == null || raw instanceof NClob)
        {
            return (NClob)raw;
        }
        
        throw new SQLException("Column " + pColumnIndex + " is not an NClob: " + raw.getClass().getName());
    }

    /** {@inheritDoc} */
    @Override
    public NClob getNClob(String pColumnLabel) throws SQLException
    {
        return getNClob(columnIndex(pColumnLabel));
    }

    /** {@inheritDoc} */
    @Override
    public SQLXML getSQLXML(int pColumnIndex) throws SQLException
    {
        Object raw = value(pColumnIndex);

        return (SQLXML)raw;
    }

    /** {@inheritDoc} */
    @Override
    public SQLXML getSQLXML(String pColumnLabel) throws SQLException
    {
        return getSQLXML(columnIndex(pColumnLabel));
    }

    /** {@inheritDoc} */
    @Override
    public void updateSQLXML(int pColumnIndex, SQLXML pValue) throws SQLException
    {
        invokeCurrentRowUpdate("updateSQLXML", new Class<?>[]{int.class, SQLXML.class}, new Object[]{pColumnIndex, pValue});
    }

    /** {@inheritDoc} */
    @Override
    public void updateSQLXML(String pColumnLabel, SQLXML pValue) throws SQLException
    {
        invokeCurrentRowUpdate("updateSQLXML", new Class<?>[]{String.class, SQLXML.class}, new Object[]{pColumnLabel, pValue});
    }

    /** {@inheritDoc} */
    @Override
    public String getNString(int pColumnIndex) throws SQLException
    {
        return getString(pColumnIndex);
    }

    /** {@inheritDoc} */
    @Override
    public String getNString(String pColumnLabel) throws SQLException
    {
        return getNString(columnIndex(pColumnLabel));
    }

    /** {@inheritDoc} */
    @Override
    public Reader getNCharacterStream(int pColumnIndex) throws SQLException
    {
        Object value = value(pColumnIndex);

        if (value == null)
        {
            return null;
        }

        if (value instanceof String)
        {
            return new StringReader((String)value);
        }

        if (value instanceof NClob)
        {
            return ((NClob)value).getCharacterStream();
        }

        if (value instanceof Clob)
        {
            return ((Clob)value).getCharacterStream();
        }
        
        throw new SQLException("Column " + pColumnIndex + " cannot be read as N character stream: " + value.getClass().getName());
    }

    /** {@inheritDoc} */
    @Override
    public Reader getNCharacterStream(String pColumnLabel) throws SQLException
    {
        return getNCharacterStream(columnIndex(pColumnLabel));
    }

    /** {@inheritDoc} */
    @Override
    public void updateNCharacterStream(int pColumnIndex, Reader pValue, long pLength) throws SQLException
    {
        invokeCurrentRowUpdate("updateNCharacterStream", new Class<?>[]{int.class, Reader.class, long.class}, new Object[]{pColumnIndex, pValue, pLength});
    }

    /** {@inheritDoc} */
    @Override
    public void updateNCharacterStream(String pColumnLabel, Reader pValue, long pLength) throws SQLException
    {
        invokeCurrentRowUpdate("updateNCharacterStream", new Class<?>[]{String.class, Reader.class, long.class}, new Object[]{pColumnLabel, pValue, pLength});
    }

    /** {@inheritDoc} */
    @Override
    public void updateAsciiStream(int pColumnIndex, InputStream pValue, long pLength) throws SQLException
    {
        invokeCurrentRowUpdate("updateAsciiStream", new Class<?>[]{int.class, InputStream.class, long.class}, new Object[]{pColumnIndex, pValue, pLength});
    }

    /** {@inheritDoc} */
    @Override
    public void updateBinaryStream(int pColumnIndex, InputStream pValue, long pLength) throws SQLException
    {
        invokeCurrentRowUpdate("updateBinaryStream", new Class<?>[]{int.class, InputStream.class, long.class}, new Object[]{pColumnIndex, pValue, pLength});
    }

    /** {@inheritDoc} */
    @Override
    public void updateCharacterStream(int pColumnIndex, Reader pValue, long pLength) throws SQLException
    {
        invokeCurrentRowUpdate("updateCharacterStream", new Class<?>[]{int.class, Reader.class, long.class}, new Object[]{pColumnIndex, pValue, pLength});
    }

    /** {@inheritDoc} */
    @Override
    public void updateAsciiStream(String pColumnLabel, InputStream pValue, long pLength) throws SQLException
    {
        invokeCurrentRowUpdate("updateAsciiStream", new Class<?>[]{String.class, InputStream.class, long.class}, new Object[]{pColumnLabel, pValue, pLength});
    }

    /** {@inheritDoc} */
    @Override
    public void updateBinaryStream(String pColumnLabel, InputStream pValue, long pLength) throws SQLException
    {
        invokeCurrentRowUpdate("updateBinaryStream", new Class<?>[]{String.class, InputStream.class, long.class}, new Object[]{pColumnLabel, pValue, pLength});
    }

    /** {@inheritDoc} */
    @Override
    public void updateCharacterStream(String pColumnLabel, Reader pValue, long pLength) throws SQLException
    {
        invokeCurrentRowUpdate("updateCharacterStream", new Class<?>[]{String.class, Reader.class, long.class}, new Object[]{pColumnLabel, pValue, pLength});
    }

    /** {@inheritDoc} */
    @Override
    public void updateBlob(int pColumnIndex, InputStream pValue, long pLength) throws SQLException
    {
        invokeCurrentRowUpdate("updateBlob", new Class<?>[]{int.class, InputStream.class, long.class}, new Object[]{pColumnIndex, pValue, pLength});
    }

    /** {@inheritDoc} */
    @Override
    public void updateBlob(String pColumnLabel, InputStream pValue, long pLength) throws SQLException
    {
        invokeCurrentRowUpdate("updateBlob", new Class<?>[]{String.class, InputStream.class, long.class}, new Object[]{pColumnLabel, pValue, pLength});
    }

    /** {@inheritDoc} */
    @Override
    public void updateClob(int pColumnIndex, Reader pValue, long pLength) throws SQLException
    {
        invokeCurrentRowUpdate("updateClob", new Class<?>[]{int.class, Reader.class, long.class}, new Object[]{pColumnIndex, pValue, pLength});
    }

    /** {@inheritDoc} */
    @Override
    public void updateClob(String pColumnLabel, Reader pValue, long pLength) throws SQLException
    {
        invokeCurrentRowUpdate("updateClob", new Class<?>[]{String.class, Reader.class, long.class}, new Object[]{pColumnLabel, pValue, pLength});
    }

    /** {@inheritDoc} */
    @Override
    public void updateNClob(int pColumnIndex, Reader pValue, long pLength) throws SQLException
    {
        invokeCurrentRowUpdate("updateNClob", new Class<?>[]{int.class, Reader.class, long.class}, new Object[]{pColumnIndex, pValue, pLength});
    }

    /** {@inheritDoc} */
    @Override
    public void updateNClob(String pColumnLabel, Reader pValue, long pLength) throws SQLException
    {
        invokeCurrentRowUpdate("updateNClob", new Class<?>[]{String.class, Reader.class, long.class}, new Object[]{pColumnLabel, pValue, pLength});
    }

    /** {@inheritDoc} */
    @Override
    public void updateNCharacterStream(int pColumnIndex, Reader pValue) throws SQLException
    {
        invokeCurrentRowUpdate("updateNCharacterStream", new Class<?>[]{int.class, Reader.class}, new Object[]{pColumnIndex, pValue});
    }

    /** {@inheritDoc} */
    @Override
    public void updateNCharacterStream(String pColumnLabel, Reader pValue) throws SQLException
    {
        invokeCurrentRowUpdate("updateNCharacterStream", new Class<?>[]{String.class, Reader.class}, new Object[]{pColumnLabel, pValue});
    }

    /** {@inheritDoc} */
    @Override
    public void updateAsciiStream(int pColumnIndex, InputStream pValue) throws SQLException
    {
        invokeCurrentRowUpdate("updateAsciiStream", new Class<?>[]{int.class, InputStream.class}, new Object[]{pColumnIndex, pValue});
    }

    /** {@inheritDoc} */
    @Override
    public void updateBinaryStream(int pColumnIndex, InputStream pValue) throws SQLException
    {
        invokeCurrentRowUpdate("updateBinaryStream", new Class<?>[]{int.class, InputStream.class}, new Object[]{pColumnIndex, pValue});
    }

    /** {@inheritDoc} */
    @Override
    public void updateCharacterStream(int pColumnIndex, Reader pValue) throws SQLException
    {
        invokeCurrentRowUpdate("updateCharacterStream", new Class<?>[]{int.class, Reader.class}, new Object[]{pColumnIndex, pValue});
    }

    /** {@inheritDoc} */
    @Override
    public void updateAsciiStream(String pColumnLabel, InputStream pValue) throws SQLException
    {
        invokeCurrentRowUpdate("updateAsciiStream", new Class<?>[]{String.class, InputStream.class}, new Object[]{pColumnLabel, pValue});
    }

    /** {@inheritDoc} */
    @Override
    public void updateBinaryStream(String pColumnLabel, InputStream pValue) throws SQLException
    {
        invokeCurrentRowUpdate("updateBinaryStream", new Class<?>[]{String.class, InputStream.class}, new Object[]{pColumnLabel, pValue});
    }

    /** {@inheritDoc} */
    @Override
    public void updateCharacterStream(String pColumnLabel, Reader pValue) throws SQLException
    {
        invokeCurrentRowUpdate("updateCharacterStream", new Class<?>[]{String.class, Reader.class}, new Object[]{pColumnLabel, pValue});
    }

    /** {@inheritDoc} */
    @Override
    public void updateBlob(int pColumnIndex, InputStream pValue) throws SQLException
    {
        invokeCurrentRowUpdate("updateBlob", new Class<?>[]{int.class, InputStream.class}, new Object[]{pColumnIndex, pValue});
    }

    /** {@inheritDoc} */
    @Override
    public void updateBlob(String pColumnLabel, InputStream pValue) throws SQLException
    {
        invokeCurrentRowUpdate("updateBlob", new Class<?>[]{String.class, InputStream.class}, new Object[]{pColumnLabel, pValue});
    }

    /** {@inheritDoc} */
    @Override
    public void updateClob(int pColumnIndex, Reader pValue) throws SQLException
    {
        invokeCurrentRowUpdate("updateClob", new Class<?>[]{int.class, Reader.class}, new Object[]{pColumnIndex, pValue});
    }

    /** {@inheritDoc} */
    @Override
    public void updateClob(String pColumnLabel, Reader pValue) throws SQLException
    {
        invokeCurrentRowUpdate("updateClob", new Class<?>[]{String.class, Reader.class}, new Object[]{pColumnLabel, pValue});
    }

    /** {@inheritDoc} */
    @Override
    public void updateNClob(int pColumnIndex, Reader pValue) throws SQLException
    {
        invokeCurrentRowUpdate("updateNClob", new Class<?>[]{int.class, Reader.class}, new Object[]{pColumnIndex, pValue});
    }

    /** {@inheritDoc} */
    @Override
    public void updateNClob(String pColumnLabel, Reader pValue) throws SQLException
    {
        invokeCurrentRowUpdate("updateNClob", new Class<?>[]{String.class, Reader.class}, new Object[]{pColumnLabel, pValue});
    }

    /** {@inheritDoc} */
    @Override
    public <T> T getObject(int pColumnIndex, Class<T> pType) throws SQLException
    {
        return convertObject(value(pColumnIndex), pType, pColumnIndex);
    }

    /** {@inheritDoc} */
    @Override
    public <T> T getObject(String pColumnLabel, Class<T> pType) throws SQLException
    {
        return getObject(columnIndex(pColumnLabel), pType);
    }

    /** {@inheritDoc} */
    @Override
    public void updateObject(int pColumnIndex, Object pValue, SQLType pSqlType, int pScaleOrLength) throws SQLException
    {
        invokeCurrentRowUpdate("updateObject", new Class<?>[]{int.class, Object.class, SQLType.class, int.class}, new Object[]{pColumnIndex, pValue, pSqlType, pScaleOrLength});
    }

    /** {@inheritDoc} */
    @Override
    public void updateObject(String pColumnLabel, Object pValue, SQLType pSqlType, int pScaleOrLength) throws SQLException
    {
        invokeCurrentRowUpdate("updateObject", new Class<?>[]{String.class, Object.class, SQLType.class, int.class}, new Object[]{pColumnLabel, pValue, pSqlType, pScaleOrLength});
    }

    /** {@inheritDoc} */
    @Override
    public void updateObject(int pColumnIndex, Object pValue, SQLType pSqlType) throws SQLException
    {
        invokeCurrentRowUpdate("updateObject", new Class<?>[]{int.class, Object.class, SQLType.class}, new Object[]{pColumnIndex, pValue, pSqlType});
    }

    /** {@inheritDoc} */
    @Override
    public void updateObject(String pColumnLabel, Object pValue, SQLType pSqlType) throws SQLException
    {
        invokeCurrentRowUpdate("updateObject", new Class<?>[]{String.class, Object.class, SQLType.class}, new Object[]{pColumnLabel, pValue, pSqlType});
    }
    private final Map<String,Integer> columnCache = new HashMap<>();

    /**
     * Clears the cached rows and resets the current row position.
     */
	private void invalidateRows()
    {
        rowCache = new Object[0][];
        
        rowCacheIndex = -1;
        rowNumber = 0;
        
        endOfRows = false;
        emptyResultSet = false;
        lastWasNull = false;
        onInsertRow = false;
        serverUpdatePositioned = false;
    }

    /**
     * Builds the cached column-name lookup from the result-set metadata.
     *
     * @param pMetaData the result-set metadata
     * @throws SQLException if the operation fails
     */
	private void populateColumnCache(ResultSetMetaData pMetaData) throws SQLException
    {
		String label;
		String name;
		
        for (int i = 1, cnt = pMetaData.getColumnCount(); i <= cnt; i++)
        {
            label = pMetaData.getColumnLabel(i);
            name = pMetaData.getColumnName(i);

            if (label != null && !columnCache.containsKey(label))
            {
                columnCache.put(label, i);
            }

            if (name != null && !columnCache.containsKey(name))
            {
                columnCache.put(name, i);
            }
        }
    }

    /**
     * Resolves a column label to its JDBC column index.
     *
     * @param pColumnLabel the column label
     * @return the resulting JDBC value
     * @throws SQLException if the JDBC operation cannot be completed
     */
	private int columnIndex(String pColumnLabel) throws SQLException
    {
        ensureOpen();
        
        Integer cached = columnCache.get(pColumnLabel);

        if (cached != null)
        {
            return cached;
        }
        
        populateColumnCache(getMetaData());
        
        cached = columnCache.get(pColumnLabel);

        if (cached != null)
        {
            return cached;
        }

        // Column labels returned by database drivers may differ in case from
        // the label supplied by the caller. Keep the lookup local and avoid
        // going back to the server-side ResultSet, which may already have
        // been consumed by fetchRows().
        
        for (Map.Entry<String,Integer> entry : columnCache.entrySet())
        {
            if (entry.getKey() != null && entry.getKey().equalsIgnoreCase(pColumnLabel))
            {
                return entry.getValue();
            }
        }
        
        throw new SQLException("Column not found: " + pColumnLabel);
    }

    /**
     * Checks that the remote JDBC resource is still open.
     * @throws SQLException if the operation fails
     */
	private void ensureOpen() throws SQLException
    {
        if (closed)
        {
            throw new SQLException("ResultSet is closed");
        }
    }

    /**
     * Reads a column value from the current remote result-set row.
     *
     * @param pColumnIndex the column index
     * @return the resulting JDBC value
     * @throws SQLException if the JDBC operation cannot be completed
     */
	private Object value(int pColumnIndex) throws SQLException
    {
        ensureOpen();

        if (rowCacheIndex < 0 || rowCacheIndex >= rowCache.length)
        {
            throw new SQLException("Invalid cursor position");
        }

        if (pColumnIndex < 1 || pColumnIndex > rowCache[rowCacheIndex].length)
        {
            throw new SQLException("Invalid column index: " + pColumnIndex);
        }

        Object raw = rowCache[rowCacheIndex][pColumnIndex - 1];
        
        lastWasNull = raw == null;

        return RemoteUtil.adapt(client, raw, Object.class);
    }

    /**
     * Converts a returned value to a numeric JDBC value.
     *
     * @param pValue the value to convert
     * @return the resulting JDBC value
     * @throws SQLException if the JDBC operation cannot be completed
     */
	private static Number asNumber(Object pValue) throws SQLException
    {
        if (pValue == null)
        {
            return Integer.valueOf(0);
        }

        if (pValue instanceof Number)
        {
            return (Number)pValue;
        }
        
        try
        {
            return new BigDecimal(String.valueOf(pValue));
        }
        catch (NumberFormatException e)
        {
            throw new SQLException("Value is not numeric: " + pValue, e);
        }
    }

    /**
     * Converts a returned value to a boolean JDBC value.
     *
     * @param pValue the value to convert
     * @return the resulting JDBC value
     * @throws SQLException if the JDBC operation cannot be completed
     */
	private static boolean asBoolean(Object pValue) throws SQLException
    {
        if (pValue == null)
        {
            return false;
        }

        if (pValue instanceof Boolean)
        {
            return (Boolean)pValue;
        }

        if (pValue instanceof Number)
        {
            return ((Number)pValue).doubleValue() != 0;
        }

        return Boolean.parseBoolean(String.valueOf(pValue));
    }

    /**
     * Refreshes the cached representation of the current result-set row.
     *
     * @param pPositioned the positioned
     * @return the resulting JDBC value
     * @throws SQLException if the JDBC operation cannot be completed
     */
	private boolean refreshCurrentRow(boolean pPositioned) throws SQLException
    {
        invalidateRows();

        if (!pPositioned)
        {
            boolean afterLast = (boolean)RemoteUtil.invoke(client, id, remoteInterface, "isAfterLast", new Class<?>[]{}, new Object[]{}, boolean.class);

            if (afterLast)
            {
                rowCacheIndex = 0;
                endOfRows = true;
            }

            return false;
        }

        int currentRow = (int)RemoteUtil.invoke(client, id, remoteInterface, "getRow", new Class<?>[]{}, new Object[]{}, int.class);
        
        boolean knownLast = (boolean)RemoteUtil.invoke(client, id, remoteInterface, "isLast", new Class<?>[]{}, new Object[]{}, boolean.class);

        Map<String,Object> request = new HashMap<String,Object>();
        request.put(RemoteConstants.ACTION, "fetchCurrentRow");
        request.put(RemoteConstants.ID, id);

        Map<String,Object> response = client.call(request);
        
        Object rows = response.get("rows");

        if (!(rows instanceof Object[][]))
        {
            throw new SQLException("Invalid fetchCurrentRow response");
        }

        rowCache = (Object[][])rows;

        if (rowCache.length != 1)
        {
            throw new SQLException("Expected exactly one current row");

        }
        
        rowCacheIndex = 0;
        rowNumber = currentRow;
        
        endOfRows = knownLast;
        lastWasNull = false;

        return true;
    }

    /**
     * Handles the invoke current row update operation for the remote JDBC resource.
     *
     * @param pMethod the method
     * @param pParameterTypes the parameter types
     * @param pArgs the method arguments
     * @throws SQLException if the operation fails
     */
	private void invokeCurrentRowUpdate(String pMethod, Class<?>[] pParameterTypes, Object[] pArgs) throws SQLException
    {
        ensureOpen();
        
        // fetchRows() may have advanced the server cursor beyond the logical
        // client row. Position it once before the first updateXXX() call.
        // Do NOT reposition before updateRow(): that would discard the
        // server-side update buffer on some JDBC drivers.

        if (!onInsertRow 
        	&& rowNumber > 0 
        	&& !serverUpdatePositioned
        	&& !"updateRow".equals(pMethod))
        {
            RemoteUtil.invoke(client, id, remoteInterface, "absolute", new Class<?>[]{int.class}, new Object[]{rowNumber}, boolean.class);
            
            serverUpdatePositioned = true;

        }
        
        RemoteUtil.invoke(client, id, remoteInterface, pMethod, pParameterTypes, pArgs, void.class);
    }

    /**
     * Handles the suppress warnings operation for the remote JDBC resource.
     *
     * @param pValue the value to convert
     * @param pType the requested Java type
     * @param pColumnIndex the column index
     * @return the resulting JDBC value
     * @throws SQLException if the JDBC operation cannot be completed
     */
	@SuppressWarnings("unchecked")
    private <T> T convertObject(Object pValue, Class<T> pType, int pColumnIndex) throws SQLException
    {
        if (pValue == null)
        {
            return null;
        }

        if (pType == null)
        {
            throw new SQLException("Target type must not be null");
        }

        if (pType.isInstance(pValue))
        {
            return (T)pValue;
        }

        if (pType == String.class)
        {
            return (T)String.valueOf(pValue);
        }

        if (pType == BigDecimal.class && pValue instanceof Number)
        {
            if (pValue instanceof BigDecimal)
            {
                return (T)pValue;
            }

            return (T)new BigDecimal(pValue.toString());
        }

        if (pType == Boolean.class || pType == boolean.class)
        {
            if (pValue instanceof Boolean)
            {
                return (T)pValue;
            }

            if (pValue instanceof Number)
            {
                return (T)Boolean.valueOf(((Number)pValue).intValue() != 0);
            }
        }

        if (Number.class.isAssignableFrom(pType) || pType.isPrimitive())
        {
            if (pValue instanceof Number)
            {
                Number number = (Number)pValue;

                if (pType == Byte.class || pType == byte.class)
                {
                    return (T)Byte.valueOf(number.byteValue());
                }

                if (pType == Short.class || pType == short.class)
                {
                    return (T)Short.valueOf(number.shortValue());
                }

                if (pType == Integer.class || pType == int.class)
                {
                    return (T)Integer.valueOf(number.intValue());
                }

                if (pType == Long.class || pType == long.class)
                {
                    return (T)Long.valueOf(number.longValue());
                }

                if (pType == Float.class || pType == float.class)
                {
                    return (T)Float.valueOf(number.floatValue());
                }

                if (pType == Double.class || pType == double.class)
                {
                    return (T)Double.valueOf(number.doubleValue());
                }
            }
        }

        if (pType == byte[].class && pValue instanceof String)
        {
            return (T)((String)pValue).getBytes(java.nio.charset.StandardCharsets.UTF_8);
        }
        
        throw new SQLException("Column " + pColumnIndex + " cannot be converted to " + pType.getName() + ": " + pValue.getClass().getName());
    }
}
