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
package com.sibvisions.rjdbc;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * Remote JDBC implementation of {@code Writer} functionality.
 * 
 * @author René Jahn
 */
final class RemoteWriter extends Writer
{
    private final RemoteClient client;
    
    private final StringWriter buffer = new StringWriter();
    
    private final Map<String,Object> descriptor;
    
    private boolean closed;
    

    /**
     * Creates a new {@code RemoteWriter} instance.
     *
     * @param pClient the client
     * @param pDescriptor the descriptor
     */
    RemoteWriter(RemoteClient pClient, Map<String,Object> pDescriptor)
    {
        client = pClient; 
        
        descriptor = pDescriptor;
    }

    /** {@inheritDoc} */
    @Override
    public void write(char[] pChars, int pOffset, int pLength) throws IOException
    {
    	ensureOpen();

    	buffer.write(pChars, pOffset, pLength);
    }

    /** {@inheritDoc} */
    @Override
    public void write(String pValue) throws IOException
    {
    	ensureOpen();
    	
        buffer.write(pValue, 0, pValue.length());
    }

    /** {@inheritDoc} */
    @Override
    public void flush()
    {
    }

    /** {@inheritDoc} */
    @Override
    public void close() throws IOException
    {
        if (closed)
        {
            return;
        }
        
        closed = true;
        
        if (client.isSessionClosed() || client.isSessionBroken())
        {
        	return;
        }
        
        Map<String,Object> request = new HashMap<>();
        request.put(RemoteConstants.ACTION, "writeStream");
        request.put(RemoteConstants.ID, descriptor.get("id"));
        request.put(RemoteConstants.STREAM_KIND, descriptor.get("streamKind"));
        request.put(RemoteConstants.POSITION, descriptor.get("position"));
        request.put(RemoteConstants.TEXT, buffer.toString());
        
        try
        {
            client.call(request);
        }
        catch (SQLException e)
        {
            throw new IOException(e);
        }

    }  
    
    /**
     * Checks that the writer is still open.
     * 
     * @throws IOException if the writer is closed
     */
    private void ensureOpen() throws IOException
    {
        if (closed || client.isSessionClosed() || client.isSessionBroken())
        {
            throw new IOException("Writer is closed");
        }
    }     
}
