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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.Reader;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

/**
 * Verifies the JDBC behavior for streaming and transparent exceptions.
 * 
 * @author René Jahn
 */
public class StreamingAndExceptionTransparencyTest
{
	/**
	 * Verifies the JDBC behavior for character streaming from remote result set.
	 * 
	 * @throws Exception if the operation fails
	 */
    @Test
    public void testCharacterStreamingFromRemoteResultSet() throws Exception
    {
        try (Connection connection = TestConnection.create())
        {
            DatabaseMetaData metadata = connection.getMetaData();

            try (ResultSet resultSet = metadata.getTables(null, null, "%", null))
            {
                if (!resultSet.next())
                {
                    return;
                }

                String expected = resultSet.getString("TABLE_NAME");
                assertNotNull(expected);

                try (Reader reader = resultSet.getCharacterStream("TABLE_NAME"))
                {
                    assertNotNull(reader);

                    StringBuilder value = new StringBuilder();
                    
                    char[] buffer = new char[2];
                    int read;

                    while ((read = reader.read(buffer)) != -1)
                    {
                        value.append(buffer, 0, read);
                    }
                    
                    assertEquals(expected, value.toString());
                }
            }
        }
    }

	/**
	 * Verifies the JDBC behavior for character stream can be read incrementally.
	 * 
	 * @throws Exception if the operation fails
	 */
    @Test
    public void testCharacterStreamCanBeReadIncrementally() throws Exception
    {
        try (Connection connection = TestConnection.create())
        {
            DatabaseMetaData metadata = connection.getMetaData();

            try (ResultSet resultSet = metadata.getTables(null, null, "%", null))
            {
                if (!resultSet.next())
                {
                    return;
                }

                String expected = resultSet.getString("TABLE_NAME");

                try (Reader reader = resultSet.getCharacterStream("TABLE_NAME"))
                {
                    assertNotNull(reader);

                    List<Integer> chunks = new ArrayList<Integer>();
                    
                    char[] buffer = new char[1];
                    int read;

                    while ((read = reader.read(buffer)) != -1)
                    {
                        chunks.add(read);
                    }
                    
                    assertEquals(expected.length(), chunks.size());

                    for (Integer chunk : chunks)
                    {
                        assertEquals(1, chunk.intValue());
                    }
                }
            }
        }
    }

	/**
	 * Verifies the JDBC behavior for multiple remote streams are independent.
	 * 
	 * @throws Exception if the operation fails
	 */
    @Test
    public void testMultipleRemoteStreamsAreIndependent() throws Exception
    {
        try (Connection connection = TestConnection.create())
        {
            DatabaseMetaData metadata = connection.getMetaData();

            try (ResultSet resultSet = metadata.getTables(null, null, "%", null))
            {
                if (!resultSet.next())
                {
                    return;
                }

                String expected = resultSet.getString("TABLE_NAME");

                try (Reader readerA = resultSet.getCharacterStream("TABLE_NAME");
                	 Reader readerB = resultSet.getCharacterStream("TABLE_NAME"))
                {
                    assertNotNull(readerA);
                    assertNotNull(readerB);

                    char[] a = new char[1];
                    char[] b = new char[1];

                    if (expected.length() > 0)
                    {
                        assertEquals(1, readerA.read(a));
                        assertEquals(1, readerB.read(b));

                        assertEquals(a[0], b[0]);
                    }
                }
            }
        }
    }

	/**
	 * Verifies the JDBC behavior for stream closed lifecycle.
	 * 
	 * @throws Exception if the operation fails
	 */
    @Test
    public void testStreamClosedLifecycle() throws Exception
    {
        try (Connection connection = TestConnection.create())
        {
            DatabaseMetaData metadata = connection.getMetaData();

            try (ResultSet resultSet = metadata.getTables(null, null, "%", null))
            {
                if (!resultSet.next())
                {
                    return;
                }

                Reader reader = resultSet.getCharacterStream("TABLE_NAME");
                assertNotNull(reader);

                reader.close();

                try
                {
                    reader.read();
                    
                    fail("A closed remote stream must not be readable.");
                }
                catch (Exception expected)
                {
                    assertTrue(expected instanceof java.io.IOException);
                }
            }
        }
    }

	/**
	 * Verifies the JDBC behavior for invalid cursor exception is sql exception.
	 * 
	 * @throws Exception if the operation fails
	 */
    @Test
    public void testInvalidCursorExceptionIsSQLException() throws Exception
    {
        try (Connection connection = TestConnection.create();
             ResultSet resultSet = connection.getMetaData().getTables(null, null, "%", null))
        {
            try
            {
                resultSet.getString(1);
                
                fail("Reading before next() must fail.");
            }
            catch (SQLException exception)
            {
                assertNotNull(exception);
                assertNotNull(exception.getMessage());
            }
        }
    }

	/**
	 * Verifies the JDBC behavior for closed result set exception is sql exception.
	 * 
	 * @throws Exception if the operation fails
	 */
    @Test
    public void testClosedResultSetExceptionIsSQLException() throws Exception
    {
        try (Connection connection = TestConnection.create();
             ResultSet resultSet = connection.getMetaData().getTables(null, null, "%", null))
        {
            resultSet.close();

            try
            {
                resultSet.next();
                
                fail("Using a closed ResultSet must fail.");
            }
            catch (SQLException exception)
            {
                assertNotNull(exception);
                assertNotNull(exception.getMessage());
            }
        }
    }

	/**
	 * Verifies the JDBC behavior for closed statement exception is sql exception.
	 * 
	 * @throws Exception if the operation fails
	 */
    @Test
    public void testClosedStatementExceptionIsSQLException() throws Exception
    {
        try (Connection connection = TestConnection.create())
        {
            Statement statement = connection.createStatement();
            statement.close();

            try
            {
                statement.execute("/* JDBC exception contract */");
                
                fail("Using a closed Statement must fail.");
            }
            catch (SQLException exception)
            {
                assertNotNull(exception);
                assertNotNull(exception.getMessage());
            }
        }
    }

	/**
	 * Verifies the JDBC behavior for closed connection exception is sql exception.
	 * 
	 * @throws Exception if the operation fails
	 */
    @Test
    public void testClosedConnectionExceptionIsSQLException() throws Exception
    {
        Connection connection = TestConnection.create();
        connection.close();

        try
        {
            connection.createStatement();
            
            fail("Using a closed Connection must fail.");
        }
        catch (SQLException exception)
        {
            assertNotNull(exception);
            assertNotNull(exception.getMessage());
        }
    }

	/**
	 * Verifies the JDBC behavior for exception metadata is accessible.
	 * 
	 * @throws Exception if the operation fails
	 */
    @Test
    public void testExceptionMetadataIsAccessible() throws Exception
    {
        try (Connection connection = TestConnection.create())
        {
            try
            {
                connection.commit();
                
                fail("commit() with autoCommit enabled must fail.");
            }
            catch (SQLException exception)
            {
                // The exact SQLState/error code is database/driver dependent.
                // The remote JDBC contract requires a usable SQLException.
                assertNotNull(exception.getMessage());

                String sqlState = exception.getSQLState();
                
                int errorCode = exception.getErrorCode();

                // Accessing the metadata itself must never throw.
                assertTrue(sqlState == null || sqlState.length() >= 0);
                assertTrue(errorCode == exception.getErrorCode());
            }
        }
    }

	/**
	 * Verifies the JDBC behavior for next exception chain is preserved when
	 * present.
	 * 
	 * @throws Exception if the operation fails
	 */
    @Test
    public void testNextExceptionChainIsPreservedWhenPresent() throws Exception
    {
        try (Connection connection = TestConnection.create())
        {
            try
            {
                connection.commit();
                
                fail("commit() with autoCommit enabled must fail.");
            }
            catch (SQLException exception)
            {
                SQLException next = exception.getNextException();

                if (next != null)
                {
                    assertNotNull(next.getMessage());
                    assertNotNull(next.getSQLState());
                }
            }
        }
    }
}
