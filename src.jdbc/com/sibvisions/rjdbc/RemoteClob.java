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
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.sql.Clob;
import java.sql.SQLException;

/**
 * Remote JDBC implementation of {@code Clob} functionality.
 * 
 * @author René Jahn
 */
public class RemoteClob implements Clob
{
    protected final RemoteClient client;
    
    protected final long id;
    
    private String cachedValue;
    
    private final boolean prefetched;
    private boolean valueLoaded;
    private boolean freed;
    

    /**
     * Creates a new {@code RemoteClob} instance.
     *
     * @param pClient the client
     * @param pId the remote resource identifier
     */
    public RemoteClob(RemoteClient pClient, long pId)
    {
        this(pClient, pId, null, false);
    }

    /**
     * Creates a new {@code RemoteClob} instance.
     *
     * @param pClient the client
     * @param pId the remote resource identifier
     * @param pValue the value to convert
     * @param pValueLoaded the value loaded
     */
    public RemoteClob(RemoteClient pClient, long pId, String pValue, boolean pValueLoaded)
    {
        client = pClient;
        id = pId;
        
        cachedValue = pValue;
        
        valueLoaded = pValueLoaded;
        prefetched = pValueLoaded;
    }

    /** {@inheritDoc} */
    @Override
    public String toString()
    {
        return "RemoteClob[" + id + "]";
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode()
    {
        return Long.hashCode(id);
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(Object pObject)
    {
        return this == pObject;
    }

    /** {@inheritDoc} */
    @Override
    public long length() throws SQLException
    {
        ensureOpen();

        if (valueLoaded)
        {
            return cachedValue.length();
        }

        return (long)RemoteUtil.invoke(client, id, remoteInterface(), "length", new Class<?>[]{}, new Object[]{}, long.class);
    }

    /** {@inheritDoc} */
    @Override
    public String getSubString(long pPosition, int pLength) throws SQLException
    {
        ensureOpen();

        if (valueLoaded)
        {
            if (pPosition < 1 || pLength < 0 || pPosition - 1 > cachedValue.length())
            {
                throw new SQLException("Invalid Clob position or length");
            }

            long end = pPosition - 1L + pLength;

            if (end > cachedValue.length())
            {
                throw new SQLException("Invalid Clob position or length");

            }

            return cachedValue.substring((int)pPosition - 1, (int)end);
        }

        return (String)RemoteUtil.invoke(client, id, remoteInterface(), "getSubString", new Class<?>[]{long.class, int.class}, new Object[]{pPosition, pLength}, String.class);
    }

    /** {@inheritDoc} */
    @Override
    public Reader getCharacterStream() throws SQLException
    {
        ensureOpen();

        if (valueLoaded)
        {
            return new StringReader(cachedValue);
        }

        return (Reader)RemoteUtil.invoke(client, id, remoteInterface(), "getCharacterStream", new Class<?>[]{}, new Object[]{}, Reader.class);
    }

    /** {@inheritDoc} */
    @Override
    public InputStream getAsciiStream() throws SQLException
    {
        ensureOpen();

        if (valueLoaded)
        {
            return new ByteArrayInputStream(cachedValue.getBytes(StandardCharsets.US_ASCII));
        }

        return (InputStream)RemoteUtil.invoke(client, id, remoteInterface(), "getAsciiStream", new Class<?>[]{}, new Object[]{}, InputStream.class);
    }

    /** {@inheritDoc} */
    @Override
    public long position(String pSearchstr, long pStart) throws SQLException
    {
        ensureOpen();

        return (long)RemoteUtil.invoke(client, id, remoteInterface(), "position", new Class<?>[]{String.class, long.class}, new Object[]{pSearchstr, pStart}, long.class);
    }

    /** {@inheritDoc} */
    @Override
    public long position(Clob pSearchstr, long pStart) throws SQLException
    {
        ensureOpen();

        return (long)RemoteUtil.invoke(client, id, remoteInterface(), "position", new Class<?>[]{Clob.class, long.class}, new Object[]{pSearchstr, pStart}, long.class);
    }

    /** {@inheritDoc} */
    @Override
    public int setString(long pPosition, String pStr) throws SQLException
    {
        ensureOpen();
        
        int result = (int)RemoteUtil.invoke(client, id, remoteInterface(), "setString", new Class<?>[]{long.class, String.class}, new Object[]{pPosition, pStr}, int.class);
        
        invalidateCache();

        return result;
    }

    /** {@inheritDoc} */
    @Override
    public int setString(long pPosition, String pStr, int pOffset, int pLength) throws SQLException
    {
        ensureOpen();
        
        int result = (int)RemoteUtil.invoke(client, id, remoteInterface(), "setString", new Class<?>[]{long.class, String.class, int.class, int.class}, new Object[]{pPosition, pStr, pOffset, pLength}, int.class);
        
        invalidateCache();

        return result;
    }

    /** {@inheritDoc} */
    @Override
    public OutputStream setAsciiStream(long pPosition) throws SQLException
    {
        ensureOpen();
        
        OutputStream stream = (OutputStream)RemoteUtil.invoke(client, id, remoteInterface(), "setAsciiStream", new Class<?>[]{long.class}, new Object[]{pPosition}, OutputStream.class);
        
        invalidateCache();

        return stream; 
    }

    /** {@inheritDoc} */
    @Override
    public Writer setCharacterStream(long pPosition) throws SQLException
    {
        ensureOpen();
        
        Writer w = (Writer)RemoteUtil.invoke(client, id, remoteInterface(), "setCharacterStream", new Class<?>[]{long.class}, new Object[]{pPosition}, Writer.class); 
        
        invalidateCache();

        return w; 
    }

    /** {@inheritDoc} */
    @Override
    public void truncate(long pLength) throws SQLException
    {
        ensureOpen();
        
        RemoteUtil.invoke(client, id, remoteInterface(), "truncate", new Class<?>[]{long.class}, new Object[]{pLength}, void.class);
        
        invalidateCache();
    }

    /** {@inheritDoc} */
    @Override
    public void free() throws SQLException
    {
        if (freed)
        {
            return;
        }
        
        // A prefetched CLOB was transferred completely with fetchRows(). The
        // server-side object is not needed for any further read operation, so
        // free() must remain entirely local and must not create an HTTPS request.

        if (!prefetched)
        {
            RemoteUtil.invoke(client, id, remoteInterface(), "free", new Class<?>[]{}, new Object[]{}, void.class);
        }
        
        cachedValue = null;
        valueLoaded = false;
        
        freed = true;
    }

    /** {@inheritDoc} */
    @Override
    public Reader getCharacterStream(long pPos, long pLength) throws SQLException
    {
        ensureOpen();

        if (valueLoaded)
        {
            if (pPos < 1 || pLength < 0 || pPos - 1 > cachedValue.length() || pPos - 1L + pLength > cachedValue.length())
            {
                throw new SQLException("Invalid Clob position or length [" + pPos + ", " + pLength + " : " + cachedValue.length() + "]");
            }

            return new StringReader(cachedValue.substring((int)pPos - 1, (int)(pPos - 1L + pLength)));
        }

        return (Reader)RemoteUtil.invoke(client, id, remoteInterface(), "getCharacterStream", new Class<?>[]{long.class, long.class}, new Object[]{pPos, pLength}, Reader.class);
    }

    /**
     * Sets cached value.
     *
     * @param pValue the value to convert
     */
    void setCachedValue(String pValue)
    {
        cachedValue = pValue;
        valueLoaded = true;
    }

	/**
	 * Handles the remote interface operation for the remote JDBC resource.
	 * 
	 * @return the resulting JDBC value
	 */
	protected Class<?> remoteInterface()
    {
        return Clob.class;
    }

	/**
	 * Handles the stream prefix operation for the remote JDBC resource.
	 * 
	 * @return the resulting JDBC value
	 */
	protected String streamPrefix()
    {
        return "clob";
    }

	/**
	 * Checks that the remote JDBC resource is still open.
	 * 
	 * @throws SQLException if the clob is already freed
	 */
	private void ensureOpen() throws SQLException
    {
        if (freed)
        {
            throw new SQLException("Clob is already freed");
        }
    }

	/**
	 * Handles the invalidate cache operation for the remote JDBC resource.
	 */
	private void invalidateCache()
    {
        cachedValue = null;
        valueLoaded = false;
    }
}
