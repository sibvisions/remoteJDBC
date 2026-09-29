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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Initializes the database objects and state required by the test class.
 */
public class ResultSetTest
{
    private static final String QUERY = "SELECT ID, NAME, INT_VALUE, LONG_VALUE, DECIMAL_VALUE, " +
                                               "DATE_VALUE, TIME_VALUE, TEXT_VALUE, NULL_VALUE " +
                                        "FROM TEST_RESULTSET ORDER BY ID";

    private Connection connection;

    /**
     * Initializes the database objects and state required by the test class.
      *
     * @throws Exception if the operation fails
     */
    @Before
    public void setUp() throws Exception
    {
        connection = TestConnection.create();

        try (Statement statement = connection.createStatement())
        {
            dropTable(statement);

            statement.executeUpdate(
            "CREATE TABLE TEST_RESULTSET (" +
            "ID NUMBER(10) PRIMARY KEY, " +
            "NAME VARCHAR2(100), " +
            "INT_VALUE NUMBER(10), " +
            "LONG_VALUE NUMBER(19), " +
            "DECIMAL_VALUE NUMBER(20,5), " +
            "DATE_VALUE DATE, " +
            "TIME_VALUE TIMESTAMP, " +
            "TEXT_VALUE CLOB, " +
            "BLOB_VALUE BLOB, " +
            "NULL_VALUE VARCHAR2(100))"
            );

            statement.executeUpdate(
            "INSERT INTO TEST_RESULTSET " +
            "(ID, NAME, INT_VALUE, LONG_VALUE, DECIMAL_VALUE, DATE_VALUE, TIME_VALUE, TEXT_VALUE, NULL_VALUE) " +
            "VALUES " +
            "(1, 'ONE', 10, 10000000000, 12345.67890, " +
            "DATE '2024-01-02', TIMESTAMP '2024-01-02 12:34:56', " +
            "'Hello World', NULL)"
            );

            statement.executeUpdate(
            "INSERT INTO TEST_RESULTSET " +
            "(ID, NAME, INT_VALUE, LONG_VALUE, DECIMAL_VALUE, DATE_VALUE, TIME_VALUE, TEXT_VALUE, NULL_VALUE) " +
            "VALUES " +
            "(2, 'TWO', 20, 20000000000, 98765.43210, " +
            "DATE '2024-02-03', TIMESTAMP '2024-02-03 23:45:12', " +
            "'Second Row', 'NOT NULL')"
            );

            statement.executeUpdate(
            "INSERT INTO TEST_RESULTSET " +
            "(ID, NAME, INT_VALUE, LONG_VALUE, DECIMAL_VALUE, DATE_VALUE, TIME_VALUE, TEXT_VALUE, NULL_VALUE) " +
            "VALUES " +
            "(3, 'THREE', 30, 30000000000, 1.25000, " +
            "DATE '2024-03-04', TIMESTAMP '2024-03-04 01:02:03', " +
            "'Third Row', NULL)"
            );
        }
    }

    /**
     * Releases the database objects and resets the state created by the test class.
      *
     * @throws Exception if the operation fails
     */
    @After
    public void tearDown() throws Exception
    {
        if (connection != null)
        {
            try
            {
                try (Statement statement = connection.createStatement())
                {
                    dropTable(statement);
                }
            }
            finally
            {
                connection.close();
            }
        }
    }

    /**
     * Removes the test table when it exists so the next test can start from a clean state.
      *
     * @param statement the statement
     */
    private void dropTable(Statement statement)
    {
        try
        {
            statement.executeUpdate("DROP TABLE TEST_RESULTSET");
        }
        catch (Exception ignored)
        {
        }
    }

    /**
     * Creates result set for use by the tests.
      *
     * @return the requested value
     * @throws Exception if the operation fails
     */
    private ResultSet createResultSet() throws Exception
    {
        Statement statement = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);

        return statement.executeQuery(QUERY);
    }

    /**
     * Verifies the JDBC behavior for next.
      *
     * @throws Exception if the operation fails
     */
    @Test
    public void testNext() throws Exception
    {
        try (ResultSet resultSet = createResultSet())
        {
            assertTrue(resultSet.next());
            assertEquals(1, resultSet.getInt("ID"));

            assertTrue(resultSet.next());
            assertEquals(2, resultSet.getInt("ID"));

            assertTrue(resultSet.next());
            assertEquals(3, resultSet.getInt("ID"));

            assertFalse(resultSet.next());
        }
    }

    /**
     * Verifies the JDBC behavior for previous.
      *
     * @throws Exception if the operation fails
     */
    @Test
    public void testPrevious() throws Exception
    {
        try (ResultSet resultSet = createResultSet())
        {
            assertTrue(resultSet.last());
            assertEquals(3, resultSet.getInt("ID"));

            assertTrue(resultSet.previous());
            assertEquals(2, resultSet.getInt("ID"));

            assertTrue(resultSet.previous());
            assertEquals(1, resultSet.getInt("ID"));

            assertFalse(resultSet.previous());
        }
    }

    /**
     * Verifies the JDBC behavior for first and last.
      *
     * @throws Exception if the operation fails
     */
    @Test
    public void testFirstAndLast() throws Exception
    {
        try (ResultSet resultSet = createResultSet())
        {
            assertTrue(resultSet.first());
            assertEquals(1, resultSet.getInt("ID"));

            assertTrue(resultSet.last());
            assertEquals(3, resultSet.getInt("ID"));
        }
    }

    /**
     * Verifies the JDBC behavior for absolute.
      *
     * @throws Exception if the operation fails
     */
    @Test
    public void testAbsolute() throws Exception
    {
        try (ResultSet resultSet = createResultSet())
        {
            assertTrue(resultSet.absolute(2));
            assertEquals(2, resultSet.getInt("ID"));

            assertTrue(resultSet.absolute(-1));
            assertEquals(3, resultSet.getInt("ID"));

            assertFalse(resultSet.absolute(100));
            assertTrue(resultSet.isAfterLast());
        }
    }

    /**
     * Verifies the JDBC behavior for relative.
      *
     * @throws Exception if the operation fails
     */
    @Test
    public void testRelative() throws Exception
    {
        try (ResultSet resultSet = createResultSet())
        {
            assertTrue(resultSet.first());
            assertTrue(resultSet.relative(2));

            assertEquals(3, resultSet.getInt("ID"));

            assertTrue(resultSet.relative(-1));
            assertEquals(2, resultSet.getInt("ID"));
        }
    }

    /**
     * Verifies the JDBC behavior for get row.
      *
     * @throws Exception if the operation fails
     */
    @Test
    public void testGetRow() throws Exception
    {
        try (ResultSet resultSet = createResultSet())
        {
            assertEquals(0, resultSet.getRow());

            assertTrue(resultSet.next());
            assertEquals(1, resultSet.getRow());

            assertTrue(resultSet.next());
            assertEquals(2, resultSet.getRow());
        }
    }

    /**
     * Verifies the JDBC behavior for position flags.
      *
     * @throws Exception if the operation fails
     */
    @Test
    public void testPositionFlags() throws Exception
    {
        try (ResultSet resultSet = createResultSet())
        {
            assertTrue(resultSet.isBeforeFirst());
            assertFalse(resultSet.isFirst());
            assertFalse(resultSet.isAfterLast());
            assertFalse(resultSet.isLast());

            resultSet.first();

            assertTrue(resultSet.isFirst());
            assertFalse(resultSet.isBeforeFirst());

            resultSet.last();

            assertTrue(resultSet.isLast());

            resultSet.next();

            assertTrue(resultSet.isAfterLast());
        }
    }

    /**
     * Verifies the JDBC behavior for get int.
      *
     * @throws Exception if the operation fails
     */
    @Test
    public void testGetInt() throws Exception
    {
        try (ResultSet resultSet = createResultSet())
        {
            resultSet.next();

            assertEquals(1, resultSet.getInt(1));
            assertEquals(10, resultSet.getInt(3));
        }
    }

    /**
     * Verifies the JDBC behavior for get long.
      *
     * @throws Exception if the operation fails
     */
    @Test
    public void testGetLong() throws Exception
    {
        try (ResultSet resultSet = createResultSet())
        {
            resultSet.next();

            assertEquals(10000000000L, resultSet.getLong("LONG_VALUE"));
        }
    }

    /**
     * Verifies the JDBC behavior for get string.
      *
     * @throws Exception if the operation fails
     */
    @Test
    public void testGetString() throws Exception
    {
        try (ResultSet resultSet = createResultSet())
        {
            resultSet.next();

            assertEquals("ONE", resultSet.getString("NAME"));
            assertEquals("ONE", resultSet.getString(2));
        }
    }

    /**
     * Verifies the JDBC behavior for get boolean.
      *
     * @throws Exception if the operation fails
     */
    @Test
    public void testGetBoolean() throws Exception
    {
        try (ResultSet resultSet = createResultSet())
        {
            assertTrue(resultSet.next());

            // ID = 1, INT_VALUE = 10
            assertTrue(resultSet.getBoolean("INT_VALUE"));
            assertFalse(resultSet.wasNull());

            assertTrue(resultSet.next());

            // ID = 2, NULL_VALUE = 'NOT NULL'
            assertFalse(resultSet.getBoolean("NULL_VALUE"));
            assertFalse(resultSet.wasNull());

            assertTrue(resultSet.next());

            // ID = 3, NULL_VALUE = NULL
            assertFalse(resultSet.getBoolean("NULL_VALUE"));
            assertTrue(resultSet.wasNull());
        }
    }

    /**
     * Verifies the JDBC behavior for get byte.
      *
     * @throws Exception if the operation fails
     */
    @Test
    public void testGetByte() throws Exception
    {
        try (ResultSet resultSet = createResultSet())
        {
            resultSet.next();

            assertEquals((byte) 10, resultSet.getByte("INT_VALUE"));
        }
    }

    /**
     * Verifies the JDBC behavior for get short.
      *
     * @throws Exception if the operation fails
     */
    @Test
    public void testGetShort() throws Exception
    {
        try (ResultSet resultSet = createResultSet())
        {
            resultSet.next();

            assertEquals((short) 10, resultSet.getShort("INT_VALUE"));
        }
    }

    /**
     * Verifies the JDBC behavior for get float.
      *
     * @throws Exception if the operation fails
     */
    @Test
    public void testGetFloat() throws Exception
    {
        try (ResultSet resultSet = createResultSet())
        {
            resultSet.next();

            assertEquals(10.0f, resultSet.getFloat("INT_VALUE"), 0.001f);
        }
    }

    /**
     * Verifies the JDBC behavior for get double.
      *
     * @throws Exception if the operation fails
     */
    @Test
    public void testGetDouble() throws Exception
    {
        try (ResultSet resultSet = createResultSet())
        {
            resultSet.next();

            assertEquals(10.0d, resultSet.getDouble("INT_VALUE"), 0.001d);
        }
    }

    /**
     * Verifies the JDBC behavior for get big decimal.
      *
     * @throws Exception if the operation fails
     */
    @Test
    public void testGetBigDecimal() throws Exception
    {
        try (ResultSet resultSet = createResultSet())
        {
            resultSet.next();

            BigDecimal value = resultSet.getBigDecimal("DECIMAL_VALUE");

            assertEquals(new BigDecimal("12345.6789"), value);
        }
    }

    /**
     * Verifies the JDBC behavior for get big decimal by index.
      *
     * @throws Exception if the operation fails
     */
    @Test
    public void testGetBigDecimalByIndex() throws Exception
    {
        try (ResultSet rs = createResultSet())
        {
            assertTrue(rs.next());

            assertEquals(new BigDecimal("12345.6789"), rs.getBigDecimal(5));
        }
    }

    /**
     * Verifies the JDBC behavior for read same row multiple times.
      *
     * @throws Exception if the operation fails
     */
    @Test
    public void testReadSameRowMultipleTimes() throws Exception
    {
        try (ResultSet rs = createResultSet())
        {
            assertTrue(rs.next());

            assertEquals(new BigDecimal("12345.6789"), rs.getBigDecimal("DECIMAL_VALUE"));
            assertEquals(new BigDecimal("12345.6789"), rs.getBigDecimal("DECIMAL_VALUE"));
            assertEquals("12345.6789", rs.getString("DECIMAL_VALUE"));
        }
    }

    /**
     * Verifies the JDBC behavior for get date.
      *
     * @throws Exception if the operation fails
     */
    @Test
    public void testGetDate() throws Exception
    {
        try (ResultSet resultSet = createResultSet())
        {
            resultSet.next();

            Date value = resultSet.getDate("DATE_VALUE");

            assertNotNull(value);
            
            assertEquals(Date.valueOf("2024-01-02"), value);
        }
    }

    /**
     * Verifies the JDBC behavior for get time.
      *
     * @throws Exception if the operation fails
     */
    @Test
    public void testGetTime() throws Exception
    {
        try (ResultSet resultSet = createResultSet())
        {
            resultSet.next();

            Time value = resultSet.getTime("TIME_VALUE");

            assertNotNull(value);
        }
    }

    /**
     * Verifies the JDBC behavior for get timestamp.
      *
     * @throws Exception if the operation fails
     */
    @Test
    public void testGetTimestamp() throws Exception
    {
        try (ResultSet resultSet = createResultSet())
        {
            resultSet.next();

            Timestamp value = resultSet.getTimestamp("TIME_VALUE");

            assertNotNull(value);
            assertEquals(Timestamp.valueOf("2024-01-02 12:34:56"), value);
        }
    }

    /**
     * Verifies the JDBC behavior for get object.
      *
     * @throws Exception if the operation fails
     */
    @Test
    public void testGetObject() throws Exception
    {
        try (ResultSet resultSet = createResultSet())
        {
            resultSet.next();

            assertEquals(1, ((Number) resultSet.getObject("ID")).intValue());
            assertEquals("ONE", resultSet.getObject("NAME"));
        }
    }

    /**
     * Verifies the JDBC behavior for get object by class.
      *
     * @throws Exception if the operation fails
     */
    @Test
    public void testGetObjectByClass() throws Exception
    {
        try (ResultSet resultSet = createResultSet())
        {
            resultSet.next();

            Integer id = resultSet.getObject("ID", Integer.class);
            String name = resultSet.getObject("NAME", String.class);

            assertEquals(Integer.valueOf(1), id);
            assertEquals("ONE", name);
        }
    }

    /**
     * Verifies the JDBC behavior for find column.
      *
     * @throws Exception if the operation fails
     */
    @Test
    public void testFindColumn() throws Exception
    {
        try (ResultSet resultSet = createResultSet())
        {
            assertEquals(1, resultSet.findColumn("ID"));
            assertEquals(2, resultSet.findColumn("NAME"));
            assertEquals(3, resultSet.findColumn("INT_VALUE"));

            // Oracle typically returns metadata in upper-case
            assertEquals(2, resultSet.findColumn("name"));
        }
    }

    /**
     * Verifies the JDBC behavior for was null.
      *
     * @throws Exception if the operation fails
     */
    @Test
    public void testWasNull() throws Exception
    {
        try (ResultSet resultSet = createResultSet())
        {
            resultSet.next();

            assertNull(resultSet.getObject("NULL_VALUE"));
            assertTrue(resultSet.wasNull());

            assertEquals("ONE", resultSet.getString("NAME"));
            assertFalse(resultSet.wasNull());
        }
    }

    /**
     * Verifies the JDBC behavior for null value conversions.
      *
     * @throws Exception if the operation fails
     */
    @Test
    public void testNullValueConversions() throws Exception
    {
        try (ResultSet resultSet = createResultSet())
        {
            assertTrue(resultSet.next());
            assertTrue(resultSet.next());
            assertTrue(resultSet.next());

            assertEquals(0, resultSet.getInt("NULL_VALUE"));
            assertTrue(resultSet.wasNull());

            assertEquals(0L, resultSet.getLong("NULL_VALUE"));
            assertTrue(resultSet.wasNull());

            assertNull(resultSet.getBigDecimal("NULL_VALUE"));
            assertTrue(resultSet.wasNull());

            assertNull(resultSet.getString("NULL_VALUE"));
            assertTrue(resultSet.wasNull());

            assertNull(resultSet.getObject("NULL_VALUE"));
            assertTrue(resultSet.wasNull());
        }
    }

    /**
     * Verifies the JDBC behavior for get meta data.
      *
     * @throws Exception if the operation fails
     */
    @Test
    public void testGetMetaData() throws Exception
    {
        try (ResultSet resultSet = createResultSet())
        {
            ResultSetMetaData metadata = resultSet.getMetaData();

            assertEquals(9, metadata.getColumnCount());
            assertEquals("ID", metadata.getColumnName(1));
            assertEquals("NAME", metadata.getColumnName(2));
            assertEquals("INT_VALUE", metadata.getColumnName(3));

            assertTrue(metadata.getColumnType(1) != Types.NULL);
            assertNotNull(metadata.getColumnTypeName(1));
        }
    }

    /**
     * Verifies the JDBC behavior for result set meta data details.
      *
     * @throws Exception if the operation fails
     */
    @Test
    public void testResultSetMetaDataDetails() throws Exception
    {
        try (Connection oracle = TestConnection.createOracle();
	         Connection remote = TestConnection.create();
	         Statement oracleStatement = oracle.createStatement();
	         Statement remoteStatement = remote.createStatement();
	         ResultSet oracleResult = oracleStatement.executeQuery(QUERY);
	         ResultSet remoteResult = remoteStatement.executeQuery(QUERY))
        {
            ResultSetMetaData oracleMeta = oracleResult.getMetaData();
            ResultSetMetaData remoteMeta = remoteResult.getMetaData();

            assertEquals(oracleMeta.getColumnCount(), remoteMeta.getColumnCount());

            for (int i = 1; i <= oracleMeta.getColumnCount(); i++)
            {
                assertEquals(oracleMeta.getColumnLabel(i), remoteMeta.getColumnLabel(i));
                assertEquals(oracleMeta.getColumnName(i), remoteMeta.getColumnName(i));
                assertEquals(oracleMeta.isCurrency(i), remoteMeta.isCurrency(i));
                assertEquals(oracleMeta.isReadOnly(i), remoteMeta.isReadOnly(i));
                assertEquals(oracleMeta.isWritable(i), remoteMeta.isWritable(i));
                assertEquals(oracleMeta.isDefinitelyWritable(i), remoteMeta.isDefinitelyWritable(i));
                assertEquals(oracleMeta.getColumnType(i), remoteMeta.getColumnType(i));
                assertEquals(oracleMeta.getColumnTypeName(i), remoteMeta.getColumnTypeName(i));
                assertEquals(oracleMeta.getPrecision(i), remoteMeta.getPrecision(i));
                assertEquals(oracleMeta.getScale(i), remoteMeta.getScale(i));
                assertEquals(oracleMeta.getColumnClassName(i), remoteMeta.getColumnClassName(i));
            }
        }
    }

    /**
     * Verifies the JDBC behavior for result set meta data invalid column.
      *
     * @throws Exception if the operation fails
     */
    @Test(expected = java.sql.SQLException.class)
    public void testResultSetMetaDataInvalidColumn() throws Exception
    {
        try (ResultSet resultSet = createResultSet())
        {
            resultSet.getMetaData().getColumnName(0);
        }
    }

    /**
     * Verifies the JDBC behavior for get character stream.
      *
     * @throws Exception if the operation fails
     */
    @Test
    public void testGetCharacterStream() throws Exception
    {
        try (ResultSet resultSet = createResultSet())
        {
            resultSet.next();

            try (Reader reader = resultSet.getCharacterStream("TEXT_VALUE"))
            {
                assertNotNull(reader);

                char[] buffer = new char[100];
                int length = reader.read(buffer);

                assertTrue(length > 0);

                String value = new String(buffer, 0, length);
                
                assertTrue(value.contains("Hello World"));
            }
        }
    }

    /**
     * Verifies the JDBC behavior for get clob.
      *
     * @throws Exception if the operation fails
     */
    @Test
    public void testGetClob() throws Exception
    {
        try (ResultSet resultSet = createResultSet())
        {
            resultSet.next();

            Clob clob = resultSet.getClob("TEXT_VALUE");

            assertNotNull(clob);
            assertTrue(clob.length() >= 11);
            
            assertEquals("Hello",clob.getSubString(1, 5));
        }
    }

    /**
     * Verifies the JDBC behavior for get ascii stream.
      *
     * @throws Exception if the operation fails
     */
    @Test
    public void testGetAsciiStream() throws Exception
    {
        try (ResultSet resultSet = createResultSet())
        {
            resultSet.next();

            try (InputStream stream = resultSet.getAsciiStream("TEXT_VALUE"))
            {
                assertNotNull(stream);

                byte[] buffer = new byte[100];
                int length = stream.read(buffer);

                assertTrue(length > 0);

                String value = new String(buffer, 0, length);

                assertTrue(value.contains("Hello World"));
            }
        }
    }

    /**
     * Verifies the JDBC behavior for get bytes.
      *
     * @throws Exception if the operation fails
     */
    @Test
    public void testGetBytes() throws Exception
    {
        try (ResultSet resultSet = createResultSet())
        {
            resultSet.next();

            Object value = resultSet.getObject("NAME");

            assertNotNull(value);

            byte[] bytes = resultSet.getBytes("NAME");

            assertNotNull(bytes);
            assertTrue(bytes.length > 0);
        }
    }

    /**
     * Verifies the JDBC behavior for get warnings.
      *
     * @throws Exception if the operation fails
     */
    @Test
    public void testGetWarnings() throws Exception
    {
        try (ResultSet resultSet = createResultSet())
        {
            assertNull(resultSet.getWarnings());

            resultSet.clearWarnings();

            assertNull(resultSet.getWarnings());
        }
    }

    /**
     * Verifies the JDBC behavior for fetch size.
      *
     * @throws Exception if the operation fails
     */
    @Test
    public void testFetchSize() throws Exception
    {
        try (ResultSet resultSet = createResultSet())
        {
            resultSet.setFetchSize(2);

            assertEquals(2, resultSet.getFetchSize());

            assertTrue(resultSet.next());
            assertTrue(resultSet.next());
            assertTrue(resultSet.next());
            assertEquals(3, resultSet.getInt("ID"));
        }
    }

    /**
     * Verifies the JDBC behavior for fetch direction.
      *
     * @throws Exception if the operation fails
     */
    @Test
    public void testFetchDirection() throws Exception
    {
        try (ResultSet resultSet = createResultSet())
        {
            resultSet.setFetchDirection(ResultSet.FETCH_FORWARD);

            assertEquals(ResultSet.FETCH_FORWARD, resultSet.getFetchDirection());
        }
    }

    /**
     * Verifies the JDBC behavior for type and concurrency.
      *
     * @throws Exception if the operation fails
     */
    @Test
    public void testTypeAndConcurrency() throws Exception
    {
        try (ResultSet resultSet = createResultSet())
        {
            assertEquals(ResultSet.TYPE_SCROLL_INSENSITIVE, resultSet.getType());

            assertEquals(ResultSet.CONCUR_READ_ONLY, resultSet.getConcurrency());
        }
    }

    /**
     * Verifies the JDBC behavior for holdability.
      *
     * @throws Exception if the operation fails
     */
    @Test
    public void testHoldability() throws Exception
    {
        try (ResultSet resultSet = createResultSet())
        {
            assertEquals(connection.getHoldability(), resultSet.getHoldability());
        }
    }

    /**
     * Verifies the JDBC behavior for is closed.
      *
     * @throws Exception if the operation fails
     */
    @Test
    public void testIsClosed() throws Exception
    {
        ResultSet resultSet = createResultSet();

        assertFalse(resultSet.isClosed());

        resultSet.close();

        assertTrue(resultSet.isClosed());
    }

    /**
     * Verifies the JDBC behavior for close is idempotent.
      *
     * @throws Exception if the operation fails
     */
    @Test
    public void testCloseIsIdempotent() throws Exception
    {
        ResultSet resultSet = createResultSet();

        resultSet.close();
        resultSet.close();

        assertTrue(resultSet.isClosed());
    }

    /**
     * Verifies the JDBC behavior for result set close lifecycle.
      *
     * @throws Exception if the operation fails
     */
    @Test
    public void testResultSetCloseLifecycle() throws Exception
    {
        ResultSet resultSet = createResultSet();

        try
        {
            assertFalse(resultSet.isClosed());

            resultSet.close();
            assertTrue(resultSet.isClosed());

            try
            {
                resultSet.next();
                
                Assert.fail("Expected SQLException after ResultSet.close()");
            }
            catch (java.sql.SQLException expected)
            {
                // expected
            }
            
            resultSet.close();
            
            assertTrue(resultSet.isClosed());
        }
        finally
        {
            resultSet.close();
        }
    }

    /**
     * Verifies the JDBC behavior for get statement.
      *
     * @throws Exception if the operation fails
     */
    @Test
    public void testGetStatement() throws Exception
    {
        Statement statement = connection.createStatement();

        try
        {
            ResultSet resultSet = statement.executeQuery("SELECT ID FROM TEST_RESULTSET ORDER BY ID");

            try
            {
                assertNotNull(resultSet.getStatement());
            }
            finally
            {
                resultSet.close();
            }
        }
        finally
        {
            statement.close();
        }
    }

    /**
     * Verifies the JDBC behavior for get statement lifecycle.
      *
     * @throws Exception if the operation fails
     */
    @Test
    public void testGetStatementLifecycle() throws Exception
    {
        Statement statement = connection.createStatement();

        ResultSet resultSet = statement.executeQuery("SELECT ID FROM TEST_RESULTSET ORDER BY ID");

        resultSet.close();

        assertFalse(statement.isClosed());

        statement.close();

        assertTrue(statement.isClosed());
    }

    /**
     * Verifies the JDBC behavior for relative navigation across fetch blocks.
      *
     * @throws Exception if the operation fails
     */
    @Test
    public void testRelativeNavigationAcrossFetchBlocks() throws Exception
    {
        try (ResultSet resultSet = createResultSet())
        {
            resultSet.setFetchSize(1);

            assertTrue(resultSet.first());
            assertEquals(1, resultSet.getInt("ID"));

            assertTrue(resultSet.relative(1));
            assertEquals(2, resultSet.getInt("ID"));

            assertTrue(resultSet.relative(1));
            assertEquals(3, resultSet.getInt("ID"));

            assertTrue(resultSet.relative(-2));
            assertEquals(1, resultSet.getInt("ID"));
        }
    }

    /**
     * Verifies the JDBC behavior for absolute across fetch blocks.
      *
     * @throws Exception if the operation fails
     */
    @Test
    public void testAbsoluteAcrossFetchBlocks() throws Exception
    {
        try (ResultSet resultSet = createResultSet())
        {
            resultSet.setFetchSize(1);

            assertTrue(resultSet.absolute(3));
            assertEquals(3, resultSet.getInt("ID"));

            assertTrue(resultSet.absolute(1));
            assertEquals(1, resultSet.getInt("ID"));
        }
    }

    /**
     * Verifies the JDBC behavior for get clob repeatedly.
      *
     * @throws Exception if the operation fails
     */
    @Test
    public void testGetClobRepeatedly() throws Exception
    {
        try (ResultSet resultSet = createResultSet())
        {
            resultSet.next();

            Clob first = resultSet.getClob("TEXT_VALUE");
            Clob second = resultSet.getClob("TEXT_VALUE");

            assertNotNull(first);
            assertNotNull(second);

            assertEquals(first.length(), second.length());
        }
    }

    /**
     * Verifies the JDBC behavior for null clob.
      *
     * @throws Exception if the operation fails
     */
    @Test
    public void testNullClob() throws Exception
    {
        try (ResultSet resultSet = createResultSet())
        {
            resultSet.next();

            assertNull(resultSet.getClob("NULL_VALUE"));
            assertTrue(resultSet.wasNull());
        }
    }

    /**
     * Verifies the JDBC behavior for get binary stream.
      *
     * @throws Exception if the operation fails
     */
    @Test
    public void testGetBinaryStream() throws Exception
    {
        //The table intentionally contains no BLOB test data
        //This test first verifies JDBC behavior for NULL
    	
        try (ResultSet resultSet = createResultSet())
        {
            resultSet.next();

            InputStream stream = resultSet.getBinaryStream("NULL_VALUE");

            assertNull(stream);
            assertTrue(resultSet.wasNull());
        }
    }

    /**
     * Verifies the JDBC behavior for get n character stream.
      *
     * @throws Exception if the operation fails
     */
    @Test
    public void testGetNCharacterStream() throws Exception
    {
        try (ResultSet resultSet = createResultSet())
        {
            resultSet.next();

            //VARCHAR2 is not necessarily treated as NCHAR by the Oracle driver. 
            //Therefore, this test only verifies NULL behavior.
            
            assertNull(resultSet.getNCharacterStream("NULL_VALUE"));
            assertTrue(resultSet.wasNull());
        }
    }

    /**
     * Verifies the JDBC behavior for get n clob.
      *
     * @throws Exception if the operation fails
     */
    @Test
    public void testGetNClob() throws Exception
    {
        try (ResultSet resultSet = createResultSet())
        {
            resultSet.next();

            //NULL is unambiguous for the VARCHAR2 test value
            assertNull(resultSet.getNClob("NULL_VALUE"));
            assertTrue(resultSet.wasNull());
        }
    }

    /**
     * Verifies the JDBC behavior for wrapper.
      *
     * @throws Exception if the operation fails
     */
    @Test
    public void testWrapper() throws Exception
    {
        try (ResultSet resultSet = createResultSet())
        {
            assertTrue(resultSet.isWrapperFor(ResultSet.class));
            assertNotNull(resultSet.unwrap(ResultSet.class));
        }
    }

    /**
     * Verifies the JDBC behavior for get object map form.
      *
     * @throws Exception if the operation fails
     */
    @Test
    public void testGetObjectMapForm() throws Exception
    {
        try (ResultSet resultSet = createResultSet())
        {
            resultSet.next();

            java.util.Map<String, Class<?>> map = new java.util.HashMap<>();

            map.put("ID", Integer.class);
            map.put("NAME", String.class);

            Object id = resultSet.getObject("ID", map);

            assertNotNull(id);
        }
    }

    /**
     * Verifies the JDBC behavior for multiple rows with mixed getters.
      *
     * @throws Exception if the operation fails
     */
    @Test
    public void testMultipleRowsWithMixedGetters() throws Exception
    {
        try (ResultSet resultSet = createResultSet())
        {
            assertTrue(resultSet.next());

            assertEquals(1, resultSet.getInt("ID"));
            assertEquals("ONE", resultSet.getString("NAME"));

            assertTrue(resultSet.next());

            assertEquals(2, resultSet.getInt("ID"));
            assertEquals("TWO", resultSet.getString("NAME"));

            assertTrue(resultSet.next());

            assertEquals(3, resultSet.getInt("ID"));
            assertEquals("THREE", resultSet.getString("NAME"));
        }
    }

    /**
     * Verifies the JDBC behavior for after last navigation.
      *
     * @throws Exception if the operation fails
     */
    @Test
    public void testAfterLastNavigation() throws Exception
    {
        try (ResultSet resultSet = createResultSet())
        {
            resultSet.afterLast();

            assertTrue(resultSet.isAfterLast());
            assertEquals(0, resultSet.getRow());

            assertTrue(resultSet.previous());
            assertEquals(3, resultSet.getInt("ID"));
        }
    }

    /**
     * Verifies the JDBC behavior for before first navigation.
      *
     * @throws Exception if the operation fails
     */
    @Test
    public void testBeforeFirstNavigation() throws Exception
    {
        try (ResultSet resultSet = createResultSet())
        {
            resultSet.first();
            resultSet.beforeFirst();

            assertTrue(resultSet.isBeforeFirst());
            assertEquals(0, resultSet.getRow());

            assertTrue(resultSet.next());
            assertEquals(1, resultSet.getInt("ID"));
        }
    }

    /**
     * Verifies the JDBC behavior for last then next.
      *
     * @throws Exception if the operation fails
     */
    @Test
    public void testLastThenNext() throws Exception
    {
        try (ResultSet resultSet = createResultSet())
        {
            assertTrue(resultSet.last());
            assertEquals(3, resultSet.getInt("ID"));

            assertFalse(resultSet.next());
            assertTrue(resultSet.isAfterLast());
        }
    }

    /**
     * Verifies the JDBC behavior for previous within and across fetched blocks.
      *
     * @throws Exception if the operation fails
     */
    @Test
    public void testPreviousWithinAndAcrossFetchedBlocks() throws Exception
    {
        try (ResultSet resultSet = createResultSet())
        {
            resultSet.setFetchSize(1);

            assertTrue(resultSet.next());
            assertEquals(1, resultSet.getInt("ID"));

            //First fetch contains a look-ahead row, so row 2 is still local
            
            assertTrue(resultSet.next());
            assertEquals(2, resultSet.getInt("ID"));
            assertTrue(resultSet.previous());
            assertEquals(1, resultSet.getInt("ID"));

            //Move into the next fetched block
            //previous() must still target logical row 2 although the server cursor is already ahead
            
            assertTrue(resultSet.next());
            assertEquals(2, resultSet.getInt("ID"));
            assertTrue(resultSet.next());
            assertEquals(3, resultSet.getInt("ID"));

            assertTrue(resultSet.previous());
            assertEquals(2, resultSet.getInt("ID"));

            assertTrue(resultSet.previous());
            assertEquals(1, resultSet.getInt("ID"));

            assertFalse(resultSet.previous());
            assertTrue(resultSet.isBeforeFirst());
            assertEquals(0, resultSet.getRow());
        }
    }

    /**
     * Verifies the JDBC behavior for empty result set position flags.
      *
     * @throws Exception if the operation fails
     */
    @Test
    public void testEmptyResultSetPositionFlags() throws Exception
    {
        try (Statement statement = connection.createStatement(
	         ResultSet.TYPE_SCROLL_INSENSITIVE,
	         ResultSet.CONCUR_READ_ONLY);
	         ResultSet resultSet = statement.executeQuery("SELECT ID, NAME FROM TEST_RESULTSET WHERE 1 = 0"))
        {
            assertFalse(resultSet.isBeforeFirst());
            assertFalse(resultSet.isAfterLast());
            assertFalse(resultSet.next());
            assertFalse(resultSet.isBeforeFirst());
            assertFalse(resultSet.isAfterLast());
            assertEquals(0, resultSet.getRow());
        }
    }

    /**
     * Verifies the JDBC behavior for first then previous.
      *
     * @throws Exception if the operation fails
     */
    @Test
    public void testFirstThenPrevious() throws Exception
    {
        try (ResultSet resultSet = createResultSet())
        {
            assertTrue(resultSet.first());
            assertEquals(1, resultSet.getInt("ID"));

            assertFalse(resultSet.previous());
            assertTrue(resultSet.isBeforeFirst());
            assertEquals(0, resultSet.getRow());

            // Calling next() from beforeFirst must return the first row again.
            assertTrue(resultSet.next());
            assertEquals(1, resultSet.getInt("ID"));
        }
    }

    /**
     * Verifies the JDBC behavior for fetch block does not break current row.
      *
     * @throws Exception if the operation fails
     */
    @Test
    public void testFetchBlockDoesNotBreakCurrentRow() throws Exception
    {
        try (ResultSet rs = createResultSet())
        {
            rs.setFetchSize(1000);

            assertTrue(rs.next());

            //The server cursor may already have advanced further in the meantime.
            //These accesses must still return the first client-side row.
            assertEquals(1, rs.getRow());
            assertFalse(rs.isAfterLast());

            assertEquals(new BigDecimal("12345.6789"), rs.getBigDecimal("DECIMAL_VALUE"));
        }
    }

    /**
     * Verifies the JDBC behavior for updatable result set across fetch block.
      *
     * @throws Exception if the operation fails
     */
    @Test
    public void testUpdatableResultSetAcrossFetchBlock() throws Exception
    {
        int id;
        String originalName;

        try (Statement statement = connection.createStatement(
	         ResultSet.TYPE_SCROLL_INSENSITIVE,
	         ResultSet.CONCUR_UPDATABLE);
	         ResultSet resultSet = statement.executeQuery("SELECT ID, NAME, INT_VALUE FROM TEST_RESULTSET"))
        {
            resultSet.setFetchSize(1);
            
            assertTrue(resultSet.next());

            id = resultSet.getInt("ID");
            originalName = resultSet.getString("NAME");
            
            String changedName = originalName + "_UPDATED";

            //fetchRows() has already advanced the server cursor past the logical client row.
            //updateRow() must still update this row
            
            resultSet.updateString("NAME", changedName);
            resultSet.updateRow();

            assertEquals(changedName, resultSet.getString("NAME"));

            try (Statement verify = connection.createStatement();
                 ResultSet check = verify.executeQuery("SELECT NAME FROM TEST_RESULTSET WHERE ID = " + id))
            {
                assertTrue(check.next());
                assertEquals(changedName, check.getString(1));
            }
            
            resultSet.updateString("NAME", originalName);
            resultSet.updateRow();
        }
    }
}
