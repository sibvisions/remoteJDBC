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
import java.io.OutputStream;
import java.sql.Blob;
import java.sql.SQLException;

/**
 * Remote JDBC implementation of {@code Blob} functionality.
 * 
 * @author René Jahn
 */
public class RemoteBlob implements Blob
{
    protected final RemoteClient client;
    
    protected final long id;
    
    private boolean freed;
    

    /**
     * Creates a new {@code RemoteBlob} instance.
     *
     * @param pClient the client
     * @param pId the remote resource identifier
     */
    public RemoteBlob(RemoteClient pClient, long pId)
    {
        client = pClient; 
        id = pId;
    }

    /** {@inheritDoc} */
    @Override
    public String toString()
    {
        return "RemoteBlob[" + id + "]";
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
    	
        return (long)RemoteUtil.invoke(client, id, Blob.class, "length", new Class<?>[]{}, new Object[]{}, long.class);
    }

    /** {@inheritDoc} */
    @Override
    public byte[] getBytes(long pPosition, int pLength) throws SQLException
    {
    	ensureOpen();
    	
        return (byte[]) RemoteUtil.invoke(client, id, Blob.class, "getBytes", new Class<?>[]{long.class, int.class}, new Object[]{pPosition, pLength}, byte[].class);
    }

    /** {@inheritDoc} */
    @Override
    public InputStream getBinaryStream() throws SQLException
    {
    	ensureOpen();
    	
        return (InputStream)RemoteUtil.invoke(client, id, Blob.class, "getBinaryStream", new Class<?>[]{}, new Object[]{}, InputStream.class);
    }

    /** {@inheritDoc} */
    @Override
    public long position(byte[] pPattern, long pStart) throws SQLException
    {
    	ensureOpen();
    	
        return (long)RemoteUtil.invoke(client, id, Blob.class, "position", new Class<?>[]{byte[].class, long.class}, new Object[]{pPattern, pStart}, long.class);
    }

    /** {@inheritDoc} */
    @Override
    public long position(Blob pPattern, long pStart) throws SQLException
    {
    	ensureOpen();
    	
        return (long)RemoteUtil.invoke(client, id, Blob.class, "position", new Class<?>[]{Blob.class, long.class}, new Object[]{pPattern, pStart}, long.class);
    }

    /** {@inheritDoc} */
    @Override
    public int setBytes(long pPosition, byte[] pBytes) throws SQLException
    {
    	ensureOpen();
    	
        return (int)RemoteUtil.invoke(client, id, Blob.class, "setBytes", new Class<?>[]{long.class, byte[].class}, new Object[]{pPosition, pBytes}, int.class);
    }

    /** {@inheritDoc} */
    @Override
    public int setBytes(long pPosition, byte[] pBytes, int pOffset, int pLength) throws SQLException
    {
    	ensureOpen();
    	
        return (int)RemoteUtil.invoke(client, id, Blob.class, "setBytes", new Class<?>[]{long.class, byte[].class, int.class, int.class}, new Object[]{pPosition, pBytes, pOffset, pLength}, int.class);
    }

    /** {@inheritDoc} */
    @Override
    public OutputStream setBinaryStream(long pPosition) throws SQLException
    {
    	ensureOpen();
    	
        return (OutputStream)RemoteUtil.invoke(client, id, Blob.class, "setBinaryStream", new Class<?>[]{long.class}, new Object[]{pPosition}, OutputStream.class);
    }

    /** {@inheritDoc} */
    @Override
    public void truncate(long pLength) throws SQLException
    {
    	ensureOpen();
    	
        RemoteUtil.invoke(client, id, Blob.class, "truncate", new Class<?>[]{long.class}, new Object[]{pLength}, void.class);
    }

    /** {@inheritDoc} */
    @Override
    public void free() throws SQLException
    {
        if (freed)
        {
            return;

        }
    	
        RemoteUtil.invoke(client, id, Blob.class, "free", new Class<?>[]{}, new Object[]{}, void.class);
        
        freed = true;
    }

    /** {@inheritDoc} */
    @Override
    public InputStream getBinaryStream(long pPos, long pLength) throws SQLException
    {
    	ensureOpen();
    	
        return (InputStream)RemoteUtil.invoke(client, id, Blob.class, "getBinaryStream", new Class<?>[]{long.class, long.class}, new Object[]{pPos, pLength}, InputStream.class);
    }
    
    /**
     * Checks that the remote JDBC resource is still open.
     * 
     * @throws SQLException if the blob is already freed
     */
    private void ensureOpen() throws SQLException
    {
        if (freed)
        {
            throw new SQLException("Blog is already freed");
        }
    }
}
