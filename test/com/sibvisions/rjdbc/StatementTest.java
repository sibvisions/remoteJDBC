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
import static org.junit.Assert.fail;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.junit.Before;
import org.junit.Test;

/**
 * Initializes the database objects and state required by the test class.
 */
public class StatementTest
{
    private static final String TABLE = "TEST_TABLE";

    /**
     * Initializes the database objects and state required by the test class.
     *
     * @throws Exception if the operation fails
     */
    @Before
    public void setUp() throws Exception
    {
    	/*
         CREATE TABLE TEST_TABLE (
           ID   INTEGER PRIMARY KEY,
           NAME VARCHAR(100)
           );
         
         or
         
         CREATE TABLE TEST_TABLE (
           ID   NUMBER(10) PRIMARY KEY,
           NAME VARCHAR2(100)
         );
        */

        try (Connection connection = TestConnection.create();
             Statement statement = connection.createStatement())
        {
            statement.executeUpdate("DELETE FROM " + TABLE);
        }
    }

    /**
     * Verifies the JDBC behavior for generated keys.
     *
     * @throws Exception if the operation fails
     */
    @Test
    public void testGeneratedKeys() throws Exception
    {
        try (Connection connection = TestConnection.create();
             Statement statement = connection.createStatement())
        {
            statement.executeUpdate("INSERT INTO " + TABLE + " (ID, NAME) VALUES (10001, 'Generated')", Statement.RETURN_GENERATED_KEYS);

            try (ResultSet keys = statement.getGeneratedKeys())
            {
                assertNotNull(keys);

                // For a table with an explicitly assigned ID, there does not have to be
                // a generated key.
                if (keys.next())
                {
                    assertNotNull(keys.getObject(1));
                }
            }
        }
    }

    /**
     * Verifies the JDBC behavior for execute return generated keys.
     *
     * @throws Exception if the operation fails
     */
    @Test
    public void testExecuteReturnGeneratedKeys() throws Exception
    {
        try (Connection connection = TestConnection.create();
             Statement statement = connection.createStatement())
        {
            boolean result = statement.execute("INSERT INTO " + TABLE + " (ID, NAME) VALUES (10002, 'Execute')", Statement.RETURN_GENERATED_KEYS);

            assertFalse(result);

            assertTrue(statement.getUpdateCount() >= 0);

            try (ResultSet keys = statement.getGeneratedKeys())
            {
                assertNotNull(keys);
            }
        }
    }

    /**
     * Verifies the JDBC behavior for execute large update.
     *
     * @throws Exception if the operation fails
     */
    @Test
    public void testExecuteLargeUpdate() throws Exception
    {
        try (Connection connection = TestConnection.create();
             Statement statement = connection.createStatement())
        {
            statement.executeUpdate("INSERT INTO " + TABLE + " (ID, NAME) VALUES (10003, 'Before')");

            long count = statement.executeLargeUpdate("UPDATE " + TABLE + " SET NAME = 'After' WHERE ID = 10003");

            assertEquals(1L, count);

            try (ResultSet rs = statement.executeQuery("SELECT NAME FROM " + TABLE + " WHERE ID = 10003"))
            {
                assertTrue(rs.next());
                assertEquals("After", rs.getString(1));
                assertFalse(rs.next());
            }
        }
    }

    /**
     * Verifies the JDBC behavior for execute batch.
     *
     * @throws Exception if the operation fails
     */
    @Test
    public void testExecuteBatch() throws Exception
    {
        try (Connection connection = TestConnection.create();
             Statement statement = connection.createStatement())
        {
            statement.addBatch("INSERT INTO " + TABLE + " (ID, NAME) VALUES (10008, 'A')");
            statement.addBatch("INSERT INTO " + TABLE + " (ID, NAME) VALUES (10009, 'B')");

            int[] counts = statement.executeBatch();

            assertNotNull(counts);
            
            assertEquals(2, counts.length);
            assertEquals(1, counts[0]);
            assertEquals(1, counts[1]);

            try (ResultSet rs = statement.executeQuery("SELECT ID, NAME FROM " + TABLE + " WHERE ID IN (10008, 10009) ORDER BY ID"))
            {
                assertTrue(rs.next());
                
                assertEquals(10008, rs.getInt(1));
                assertEquals("A", rs.getString(2));
                
                assertTrue(rs.next());
                
                assertEquals(10009, rs.getInt(1));
                assertEquals("B", rs.getString(2));
                
                assertFalse(rs.next());
            }
        }
    }

    /**
     * Verifies the JDBC behavior for clear batch.
     *
     * @throws Exception if the operation fails
     */
    @Test
    public void testClearBatch() throws Exception
    {
        try (Connection connection = TestConnection.create();
             Statement statement = connection.createStatement())
        {
            statement.addBatch("INSERT INTO " + TABLE + " (ID, NAME) VALUES (10010, 'A')");
            statement.clearBatch();

            int[] counts = statement.executeBatch();

            assertNotNull(counts);
            assertEquals(0, counts.length);

            try (ResultSet rs = statement.executeQuery("SELECT ID FROM " + TABLE + " WHERE ID = 10010"))
            {
                assertFalse(rs.next());
            }
        }
    }

    /**
     * Verifies the JDBC behavior for execute large batch.
     *
     * @throws Exception if the operation fails
     */
    @Test
    public void testExecuteLargeBatch() throws Exception
    {
        try (Connection connection = TestConnection.create();
             Statement statement = connection.createStatement())
        {
            statement.addBatch("INSERT INTO " + TABLE + " (ID, NAME) VALUES (10004, 'A')");
            statement.addBatch("INSERT INTO " + TABLE + " (ID, NAME) VALUES (10005, 'B')");

            long[] counts = statement.executeLargeBatch();

            assertNotNull(counts);
            assertEquals(2, counts.length);

            for (long count : counts)
            {
                assertTrue(count >= 0 
                		   || count == Statement.SUCCESS_NO_INFO 
                		   || count == Statement.EXECUTE_FAILED);
            }
        }
    }

    /**
     * Verifies the JDBC behavior for get more results.
     *
     * @throws Exception if the operation fails
     */
    @Test
    public void testGetMoreResults() throws Exception
    {
        try (Connection connection = TestConnection.create();
             Statement statement = connection.createStatement())
        {
            statement.executeUpdate("INSERT INTO " + TABLE + " (ID, NAME) VALUES (10006, 'MoreResults')");

            boolean hasResultSet = statement.execute("SELECT ID, NAME FROM " + TABLE + " WHERE ID = 10006");

            assertTrue(hasResultSet);

            try (ResultSet rs = statement.getResultSet())
            {
                assertNotNull(rs);
                assertTrue(rs.next());
                assertEquals(10006, rs.getInt(1));
            }
            
            assertFalse(statement.getMoreResults(Statement.CLOSE_CURRENT_RESULT));
        }
    }

    /**
     * Verifies the JDBC behavior for close on completion.
     *
     * @throws Exception if the operation fails
     */
    @Test
    public void testCloseOnCompletion() throws Exception
    {
        try (Connection connection = TestConnection.create();
             Statement statement = connection.createStatement())
        {
            statement.closeOnCompletion();

            assertTrue(statement.isCloseOnCompletion());

            statement.executeUpdate("INSERT INTO " + TABLE + " (ID, NAME) VALUES (10007, 'Close')");

            try (ResultSet rs = statement.executeQuery("SELECT ID FROM " + TABLE + " WHERE ID = 10007"))
            {
                assertTrue(rs.next());
            }
            
            assertTrue(statement.isClosed());
        }
    }

    /**
     * Verifies the JDBC behavior for statement close lifecycle.
     *
     * @throws Exception if the operation fails
     */
    @Test
    public void testStatementCloseLifecycle() throws Exception
    {
        Connection connection = TestConnection.create();
        Statement statement = connection.createStatement();

        try
        {
            assertFalse(statement.isClosed());

            statement.close();
            
            assertTrue(statement.isClosed());

            try
            {
                statement.executeQuery("SELECT 1 FROM DUAL");
                
                fail("Expected SQLException after Statement.close()");
            }
            catch (java.sql.SQLException expected)
            {
                // expected
            }
            statement.close();
            
            assertTrue(statement.isClosed());
        }
        finally
        {
            connection.close();
        }
    }

    /**
     * Verifies the JDBC behavior for connection close lifecycle.
     *
     * @throws Exception if the operation fails
     */
    @Test
    public void testConnectionCloseLifecycle() throws Exception
    {
        try (Connection connection = TestConnection.create())
        {
	        try (Statement statement = connection.createStatement();
	        	 PreparedStatement preparedStatement = connection.prepareStatement("SELECT 1 FROM DUAL"))
	        {
		        assertFalse(connection.isClosed());
		        assertFalse(statement.isClosed());
		        assertFalse(preparedStatement.isClosed());
		
		        connection.close();
		
		        assertTrue(connection.isClosed());
		        assertTrue(statement.isClosed());
		        assertTrue(preparedStatement.isClosed());
		
		        connection.close();
		        
		        assertTrue(connection.isClosed());
		
		        try
		        {
		            statement.executeQuery("SELECT 1 FROM DUAL");
		            
		            fail("Expected SQLException after Connection.close()");
		        }
		        catch (SQLException expected)
		        {
		        }
		        
		        try
		        {
		            preparedStatement.executeQuery();
		            
		            fail("Expected SQLException after Connection.close()");
		        }
		        catch (SQLException expected)
		        {
		        }
	        }
        }
    }
    
	/**
	 * Verifies that closing a ResultSet after its Connection has been closed is harmless.
	 */
	@Test
	public void testResultSetCloseAfterConnectionClose() throws Exception
	{
	    try (Connection connection = TestConnection.create();
	         Statement statement = connection.createStatement();
	         ResultSet resultSet = statement.executeQuery("SELECT 1 FROM DUAL"))
	    {
	        assertFalse(resultSet.isClosed());
	
	        connection.close();
	
	        assertTrue(resultSet.isClosed());
	
	        resultSet.close();
	
	        assertTrue(resultSet.isClosed());
	    }
	}    

    /**
     * Verifies the JDBC behavior for connection is valid lifecycle.
     *
     * @throws Exception if the operation fails
     */
    @Test
    public void testConnectionIsValidLifecycle() throws Exception
    {
        try (Connection connection = TestConnection.create())
        {
            assertTrue(connection.isValid(2));

            connection.close();

            assertFalse(connection.isValid(2));
        }
    }

    /**
     * Verifies the JDBC behavior for max rows.
     *
     * @throws Exception if the operation fails
     */
    @Test
    public void testMaxRows() throws Exception
    {
        try (Connection connection = TestConnection.create();
             Statement statement = connection.createStatement())
        {
            statement.executeUpdate("INSERT INTO " + TABLE + " (ID, NAME) VALUES (10008, 'A')");
            statement.executeUpdate("INSERT INTO " + TABLE + " (ID, NAME) VALUES (10009, 'B')");

            statement.setMaxRows(1);

            assertEquals(1, statement.getMaxRows());

            try (ResultSet rs = statement.executeQuery("SELECT ID FROM " + TABLE + " WHERE ID IN (10008, 10009) ORDER BY ID"))
            {
                int rows = 0;

                while (rs.next())
                {
                    rows++;
                }
                
                assertEquals(1, rows);
            }
        }
    }

    /**
     * Verifies the JDBC behavior for large max rows.
     *
     * @throws Exception if the operation fails
     */
    @Test
    public void testLargeMaxRows() throws Exception
    {
        try (Connection connection = TestConnection.create();
             Statement statement = connection.createStatement())
        {
            statement.setLargeMaxRows(1);

            assertEquals(1L, statement.getLargeMaxRows());
        }
    }

    /**
     * Verifies the JDBC behavior for fetch size and direction.
     *
     * @throws Exception if the operation fails
     */
    @Test
    public void testFetchSizeAndDirection() throws Exception
    {
        try (Connection connection = TestConnection.create();
             Statement statement = connection.createStatement())
        {
            statement.setFetchSize(50);

            assertEquals(50, statement.getFetchSize());

            statement.setFetchDirection(ResultSet.FETCH_FORWARD);

            assertEquals(ResultSet.FETCH_FORWARD, statement.getFetchDirection());
        }
    }

    /**
     * Verifies the JDBC behavior for query timeout and cancel.
     *
     * @throws Exception if the operation fails
     */
    @Test
    public void testQueryTimeoutAndCancel() throws Exception
    {
        try (Connection connection = TestConnection.create();
             Statement statement = connection.createStatement())
        {
            statement.setQueryTimeout(10);

            assertEquals(10, statement.getQueryTimeout());

            statement.cancel();
        }
    }

    /**
     * Verifies the JDBC behavior for enquote literal.
     *
     * @throws Exception if the operation fails
     */
    @Test
    public void testEnquoteLiteral() throws Exception
    {
        try (Connection connection = TestConnection.create();
             Statement statement = connection.createStatement())
        {
            String value = statement.enquoteLiteral("Hello");

            assertNotNull(value);
            assertTrue(value.length() >= 2);
        }
    }

    /**
     * Verifies the JDBC behavior for enquote identifier.
     *
     * @throws Exception if the operation fails
     */
    @Test
    public void testEnquoteIdentifier() throws Exception
    {
        try (Connection connection = TestConnection.create();
             Statement statement = connection.createStatement())
        {
            String identifier = statement.enquoteIdentifier("TEST_COLUMN", false);

            assertNotNull(identifier);
            assertFalse(identifier.isEmpty());

            assertTrue(statement.isSimpleIdentifier("TEST_COLUMN"));
        }
    }

    /**
     * Verifies the JDBC behavior for enquote n char literal.
     *
     * @throws Exception if the operation fails
     */
    @Test
    public void testEnquoteNCharLiteral() throws Exception
    {
        try (Connection connection = TestConnection.create();
             Statement statement = connection.createStatement())
        {
            String value = statement.enquoteNCharLiteral("Hello");

            assertNotNull(value);
            assertTrue(value.length() >= 2);
        }
    }

    /**
     * Verifies the JDBC behavior for execute query and execute update state.
     *
     * @throws Exception if the operation fails
     */
    @Test
    public void testExecuteQueryAndExecuteUpdateState() throws Exception
    {
        try (Connection connection = TestConnection.create();
             Statement statement = connection.createStatement())
        {
            assertEquals(1, statement.executeUpdate("INSERT INTO " + TABLE + " (ID, NAME) VALUES (10018, 'State')"));

            assertNull(statement.getResultSet());
            assertEquals(1, statement.getUpdateCount());

            try (ResultSet rs = statement.executeQuery("SELECT ID FROM " + TABLE + " WHERE ID = 10018"))
            {
                assertNotNull(statement.getResultSet());
                assertEquals(-1, statement.getUpdateCount());

                assertTrue(rs.next());
                assertEquals(10018, rs.getInt(1));
            }
        }
    }

    /**
     * Verifies the JDBC behavior for execute state transitions.
     *
     * @throws Exception if the operation fails
     */
    @Test
    public void testExecuteStateTransitions() throws Exception
    {
        try (Connection connection = TestConnection.create();
             Statement statement = connection.createStatement())
        {
            statement.executeUpdate("INSERT INTO " + TABLE + " (ID, NAME) VALUES (10019, 'ExecuteState')");

            assertTrue(statement.execute("SELECT ID FROM " + TABLE + " WHERE ID = 10019"));
            assertNotNull(statement.getResultSet());

            assertFalse(statement.execute("UPDATE " + TABLE + " SET NAME = 'ExecuteState2' WHERE ID = 10019"));
            assertNull(statement.getResultSet());
            assertEquals(1, statement.getUpdateCount());
        }
    }

    /**
     * Verifies the JDBC behavior for get more results modes.
     *
     * @throws Exception if the operation fails
     */
    @Test
    public void testGetMoreResultsModes() throws Exception
    {
        try (Connection connection = TestConnection.create();
             Statement statement = connection.createStatement())
        {
            statement.execute("SELECT ID FROM " + TABLE + " WHERE ID = -1");

            assertFalse(statement.getMoreResults(Statement.CLOSE_CURRENT_RESULT));
            assertFalse(statement.getMoreResults(Statement.CLOSE_ALL_RESULTS));
            assertFalse(statement.getMoreResults(Statement.KEEP_CURRENT_RESULT));
        }
    }

    /**
     * Verifies the JDBC behavior for statement close closes current result set.
     *
     * @throws Exception if the operation fails
     */
    @Test
    public void testStatementCloseClosesCurrentResultSet() throws Exception
    {
        try (Connection connection = TestConnection.create())
        {
		    try (Statement statement = connection.createStatement())
		    {
			    ResultSet rs = statement.executeQuery("SELECT 1 FROM DUAL");
			
			    assertFalse(statement.isClosed());
			    statement.close();
			
			    assertTrue(statement.isClosed());
			    assertTrue(rs.isClosed());
		    }
        }
    }

    /**
     * Verifies the JDBC behavior for max field size and poolable.
     *
     * @throws Exception if the operation fails
     */
    @Test
    public void testMaxFieldSizeAndPoolable() throws Exception
    {
        try (Connection connection = TestConnection.create();
             Statement statement = connection.createStatement())
        {
            statement.setMaxFieldSize(1024);
            assertEquals(1024, statement.getMaxFieldSize());

            statement.setPoolable(true);
            assertTrue(statement.isPoolable());

            statement.setPoolable(false);
            assertFalse(statement.isPoolable());
        }
    }
}
