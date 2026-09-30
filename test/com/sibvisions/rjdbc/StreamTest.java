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

import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Connection;

import org.junit.Test;

/**
 * Verifies the JDBC behavior for streaming.
 * 
 * @author René Jahn
 */
public class StreamTest
{
    /**
     * Verifies that an output stream can be closed after the connection
     * has already been closed.
     *
     * @throws Exception if the operation fails
     */
    @Test
    public void testOutputStreamCloseAfterConnectionClose() throws Exception
    {
        Connection connection = TestConnection.create();
        Blob blob = connection.createBlob();
        OutputStream stream = blob.setBinaryStream(1);

        connection.close();

        stream.close();
        stream.close();
    }

    /**
     * Verifies that a writer can be closed after the connection
     * has already been closed.
     *
     * @throws Exception if the operation fails
     */
    @Test
    public void testWriterCloseAfterConnectionClose() throws Exception
    {
        Connection connection = TestConnection.create();
        Clob clob = connection.createClob();
        Writer writer = clob.setCharacterStream(1);

        connection.close();

        writer.close();
        writer.close();
    }

    /**
     * Verifies that writing to an output stream after the connection
     * has been closed fails immediately.
     *
     * @throws Exception if the operation fails
     */
    @Test
    public void testOutputStreamWriteAfterConnectionClose() throws Exception
    {
        Connection connection = TestConnection.create();
        Blob blob = connection.createBlob();
        OutputStream stream = blob.setBinaryStream(1);

        connection.close();

        try
        {
            stream.write(1);
            
            fail("Expected IOException after Connection.close()");
        }
        catch (IOException expected)
        {
        }
    }

    /**
     * Verifies that writing to a writer after the connection has been
     * closed fails immediately.
     *
     * @throws Exception if the operation fails
     */
    @Test
    public void testWriterWriteAfterConnectionClose() throws Exception
    {
        Connection connection = TestConnection.create();
        Clob clob = connection.createClob();
        Writer writer = clob.setCharacterStream(1);

        connection.close();

        try
        {
            writer.write("test");
            fail("Expected IOException after Connection.close()");
        }
        catch (IOException expected)
        {
        }
    }
}
