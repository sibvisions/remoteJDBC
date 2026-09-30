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
import static org.junit.Assert.assertTrue;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Types;
import java.sql.Statement;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Initializes the database objects and state required by the test class.
 */
public class CallableStatementTest
{
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
            statement.executeUpdate(
            "CREATE OR REPLACE PROCEDURE RJDBC_TEST_PROC(" +
            "  P_IN IN NUMBER," +
            "  P_OUT OUT NUMBER," +
            "  P_INOUT IN OUT NUMBER" +
            ") AS " +
            "BEGIN " +
            "  P_OUT := P_IN * 2; " +
            "  P_INOUT := P_INOUT + 1; " +
            "END;"
            );

            statement.executeUpdate(
            "CREATE OR REPLACE FUNCTION RJDBC_TEST_FUNC(" +
            "  P_VALUE IN NUMBER" +
            ") RETURN NUMBER AS " +
            "BEGIN " +
            "  RETURN P_VALUE * 3; " +
            "END;"
            );

            statement.executeUpdate(
            "CREATE TABLE RJDBC_TEST_BATCH (" +
            "  ID NUMBER PRIMARY KEY," +
            "  NAME VARCHAR2(100)" +
            ")"
            );

            statement.executeUpdate(
            "CREATE OR REPLACE PROCEDURE RJDBC_TEST_BATCH_PROC(" +
            "  P_ID IN NUMBER," +
            "  P_NAME IN VARCHAR2" +
            ") AS " +
            "BEGIN " +
            "  INSERT INTO RJDBC_TEST_BATCH(ID, NAME) VALUES (P_ID, P_NAME); " +
            "END;"
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
    	if (connection != null && !connection.isClosed())
    	{
	        try
	        {
	            try (Statement statement = connection.createStatement())
	            {
	                statement.executeUpdate("DROP PROCEDURE RJDBC_TEST_PROC");
	                statement.executeUpdate("DROP FUNCTION RJDBC_TEST_FUNC");
	                statement.executeUpdate("DROP PROCEDURE RJDBC_TEST_BATCH_PROC");
	                statement.executeUpdate("DROP TABLE RJDBC_TEST_BATCH");
	            }
	        }
	        finally
	        {
                connection.close();
	        }
    	}
    }

	/**
	 * Verifies that named callable parameters are preserved between batch rows.
	 *
	 * @throws Exception if the operation fails
	 */
    @Test
    public void testNamedParametersInBatch() throws Exception
    {
        try (CallableStatement statement = connection.prepareCall("{call RJDBC_TEST_BATCH_PROC(?, ?)}"))
        {
            statement.setInt("P_ID", 1);
            statement.setString("P_NAME", "First");
            statement.addBatch();

            statement.setInt("P_ID", 2);
            statement.addBatch();

            int[] updateCounts = statement.executeBatch();

            assertEquals(2, updateCounts.length);
        }

        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(
                     "SELECT ID, NAME FROM RJDBC_TEST_BATCH ORDER BY ID"))
        {
            assertTrue(resultSet.next());
            assertEquals(1, resultSet.getInt("ID"));
            assertEquals("First", resultSet.getString("NAME"));

            assertTrue(resultSet.next());
            assertEquals(2, resultSet.getInt("ID"));
            assertEquals("First", resultSet.getString("NAME"));

            assertFalse(resultSet.next());
        }
    }
    
    /**
     * Verifis that named callable parameters are preserved between large batch rows.
     * 
     * @throws Exception if the operation fails
     */
	@Test
	public void testNamedParametersInLargeBatch() throws Exception
	{
	    try (CallableStatement statement = connection.prepareCall("{call RJDBC_TEST_BATCH_PROC(?, ?)}"))
	    {
	        statement.setInt("P_ID", 1);
	        statement.setString("P_NAME", "First");
	        statement.addBatch();
	
	        statement.setInt("P_ID", 2);
	        statement.addBatch();
	
	        long[] updateCounts = statement.executeLargeBatch();
	
	        assertNotNull(updateCounts);
	        assertEquals(2, updateCounts.length);
	    }
	
	    try (Statement statement = connection.createStatement();
	         ResultSet resultSet = statement.executeQuery("SELECT ID, NAME FROM RJDBC_TEST_BATCH ORDER BY ID"))
	    {
	        assertTrue(resultSet.next());
	        assertEquals(1, resultSet.getInt("ID"));
	        assertEquals("First", resultSet.getString("NAME"));
	
	        assertTrue(resultSet.next());
	        assertEquals(2, resultSet.getInt("ID"));
	        assertEquals("First", resultSet.getString("NAME"));
	
	        assertFalse(resultSet.next());
	    }
	}    
	
	/**
	 * Verifies clear parameters in batch.
	 * 
	 * @throws Exception if the operation fails
	 */
	@Test
	public void testNamedParametersClearParametersInBatch() throws Exception
	{
	    try (CallableStatement statement = connection.prepareCall(
	            "{call RJDBC_TEST_BATCH_PROC(?, ?)}"))
	    {
	        statement.setInt("P_ID", 1);
	        statement.setString("P_NAME", "First");
	        statement.addBatch();
	
	        statement.clearParameters();
	
	        statement.setInt("P_ID", 2);
	        statement.setString("P_NAME", "Second");
	        statement.addBatch();
	
	        int[] updateCounts = statement.executeBatch();
	
	        assertEquals(2, updateCounts.length);
	    }
	
	    try (Statement statement = connection.createStatement();
	         ResultSet resultSet = statement.executeQuery(
	                 "SELECT ID, NAME FROM RJDBC_TEST_BATCH ORDER BY ID"))
	    {
	        assertTrue(resultSet.next());
	        assertEquals(1, resultSet.getInt("ID"));
	        assertEquals("First", resultSet.getString("NAME"));
	
	        assertTrue(resultSet.next());
	        assertEquals(2, resultSet.getInt("ID"));
	        assertEquals("Second", resultSet.getString("NAME"));
	
	        assertFalse(resultSet.next());
	    }
	}	
	
	@Test
	public void testNamedParametersClearBatch() throws Exception
	{
	    try (CallableStatement statement = connection.prepareCall("{call RJDBC_TEST_BATCH_PROC(?, ?)}"))
	    {
	        statement.setInt("P_ID", 1);
	        statement.setString("P_NAME", "First");
	        statement.addBatch();
	
	        statement.clearBatch();
	
	        //Parameters should still be set
	        statement.setInt("P_ID", 2);
	        statement.addBatch();
	
	        int[] updateCounts = statement.executeBatch();
	
	        assertEquals(1, updateCounts.length);
	    }
	
	    try (Statement statement = connection.createStatement();
	         ResultSet resultSet = statement.executeQuery("SELECT ID, NAME FROM RJDBC_TEST_BATCH ORDER BY ID"))
	    {
	        assertTrue(resultSet.next());
	        assertEquals(2, resultSet.getInt("ID"));
	        assertEquals("First", resultSet.getString("NAME"));
	
	        assertFalse(resultSet.next());
	    }
	}	

	/**
	 * Verifies the JDBC behavior for in and out parameter.
	 * 
	 * @throws Exception if the operation fails
	 */
    @Test
    public void testInAndOutParameter() throws Exception
    {
        try (CallableStatement statement = connection.prepareCall("{call RJDBC_TEST_PROC(?, ?, ?)}"))
        {
            statement.setInt(1, 5);
            statement.registerOutParameter(2, Types.NUMERIC);
            statement.setInt(3, 10);
            statement.registerOutParameter(3, Types.NUMERIC);

            statement.execute();

            assertEquals(10, statement.getInt(2));
            assertEquals(11, statement.getInt(3));
            assertFalse(statement.wasNull());
        }
    }

	/**
	 * Verifies the JDBC behavior for out parameter as object.
	 * 
	 * @throws Exception if the operation fails
	 */
    @Test
    public void testOutParameterAsObject() throws Exception
    {
        try (CallableStatement statement = connection.prepareCall("{call RJDBC_TEST_PROC(?, ?, ?)}"))
        {
            statement.setInt(1, 7);
            statement.registerOutParameter(2, Types.NUMERIC);
            statement.setInt(3, 20);
            statement.registerOutParameter(3, Types.NUMERIC);

            statement.execute();

            Object value = statement.getObject(2);

            assertNotNull(value);
            assertEquals(14, ((Number) value).intValue());
        }
    }

	/**
	 * Verifies the JDBC behavior for in out parameter.
	 * 
	 * @throws Exception if the operation fails
	 */
    @Test
    public void testInOutParameter() throws Exception
    {
        try (CallableStatement statement = connection.prepareCall("{call RJDBC_TEST_PROC(?, ?, ?)}"))
        {
            statement.setInt(1, 3);
            statement.registerOutParameter(2, Types.NUMERIC);

            statement.setInt(3, 41);
            statement.registerOutParameter(3, Types.NUMERIC);

            statement.execute();

            assertEquals(6, statement.getInt(2));
            assertEquals(42, statement.getInt(3));
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
        try (CallableStatement statement = connection.prepareCall("{call RJDBC_TEST_PROC(?, ?, ?)}"))
        {
            statement.setInt(1, 4);
            statement.registerOutParameter(2, Types.NUMERIC);
            statement.setInt(3, 10);
            statement.registerOutParameter(3, Types.NUMERIC);

            statement.execute();

            statement.getObject(2);

            assertFalse(statement.wasNull());
        }
    }

	/**
	 * Verifies the JDBC behavior for function return value.
	 * 
	 * @throws Exception if the operation fails
	 */
    @Test
    public void testFunctionReturnValue() throws Exception
    {
        try (CallableStatement statement = connection.prepareCall("{? = call RJDBC_TEST_FUNC(?)}"))
        {
            statement.registerOutParameter(1, Types.NUMERIC);
            statement.setInt(2, 9);

            statement.execute();

            assertEquals(27, statement.getInt(1));
        }
    }

	/**
	 * Verifies the JDBC behavior for execute returns result flag.
	 * 
	 * @throws Exception if the operation fails
	 */
    @Test
    public void testExecuteReturnsResultFlag() throws Exception
    {
        try (CallableStatement statement = connection.prepareCall("{call RJDBC_TEST_PROC(?, ?, ?)}"))
        {
            statement.setInt(1, 5);
            statement.registerOutParameter(2, Types.NUMERIC);
            statement.setInt(3, 10);
            statement.registerOutParameter(3, Types.NUMERIC);

            boolean result = statement.execute();

            assertFalse(result);
            assertEquals(10, statement.getInt(2));
            assertEquals(11, statement.getInt(3));
        }
    }

	/**
	 * Verifies the JDBC behavior for clear parameters.
	 * 
	 * @throws Exception if the operation fails
	 */
    @Test
    public void testClearParameters() throws Exception
    {
        try (CallableStatement statement = connection.prepareCall("{call RJDBC_TEST_PROC(?, ?, ?)}"))
        {
            statement.setInt(1, 5);
            statement.registerOutParameter(2, Types.NUMERIC);
            statement.setInt(3, 10);
            statement.registerOutParameter(3, Types.NUMERIC);

            statement.clearParameters();

            statement.setInt(1, 8);
            statement.registerOutParameter(2, Types.NUMERIC);
            statement.setInt(3, 20);
            statement.registerOutParameter(3, Types.NUMERIC);

            statement.execute();

            assertEquals(16, statement.getInt(2));
            assertEquals(21, statement.getInt(3));
        }
    }

	/**
	 * Verifies the JDBC behavior for result set after callable statement.
	 * 
	 * @throws Exception if the operation fails
	 */
    @Test
    public void testResultSetAfterCallableStatement() throws Exception
    {
        try (CallableStatement statement = connection.prepareCall("BEGIN " +
        														  "  OPEN ? FOR SELECT 1 AS ID, 'TEST' AS NAME FROM DUAL; " +
        														  "END;"))
        {
            statement.registerOutParameter(1, Types.REF_CURSOR);

            statement.execute();

            try (ResultSet resultSet = statement.getObject(1, ResultSet.class))
            {
                assertTrue(resultSet.next());
                assertEquals(1, resultSet.getInt("ID"));
                assertEquals("TEST", resultSet.getString("NAME"));
                assertFalse(resultSet.next());
            }
        }
    }

	/**
	 * Verifies the JDBC behavior for close.
	 * 
	 * @throws Exception if the operation fails
	 */
    @Test
    public void testClose() throws Exception
    {
        CallableStatement statement = connection.prepareCall("{call RJDBC_TEST_PROC(?, ?, ?)}");

        statement.setInt(1, 5);
        statement.registerOutParameter(2, Types.NUMERIC);
        statement.setInt(3, 10);
        statement.registerOutParameter(3, Types.NUMERIC);

        statement.close();

        assertTrue(statement.isClosed());
    }
}
