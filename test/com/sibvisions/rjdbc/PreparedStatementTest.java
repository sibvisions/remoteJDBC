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

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.JDBCType;
import java.sql.ParameterMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;

import org.junit.Before;
import org.junit.Test;

/**
 * Verifies the JDBC behavior for prepared statement.
 * 
 * @author René Jahn
 */
public class PreparedStatementTest
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
        try (Connection connection = TestConnection.create();
        	 PreparedStatement statement = connection.prepareStatement("DELETE FROM " + TABLE))
        {
            statement.executeUpdate();
        }
    }

	/**
	 * Verifies the JDBC behavior for execute query.
	 * 
	 * @throws Exception if the operation fails
	 */
    @Test
    public void testExecuteQuery() throws Exception
    {
        try (Connection connection = TestConnection.create();
        	 PreparedStatement insert = connection.prepareStatement("INSERT INTO " + TABLE + " (ID, NAME) VALUES (?, ?)"))
        {
            insert.setInt(1, 11001);
            insert.setString(2, "Query");
            
            assertEquals(1, insert.executeUpdate());

            try (PreparedStatement select = connection.prepareStatement("SELECT ID, NAME FROM " + TABLE + " WHERE ID = ?"))
            {
                select.setInt(1, 11001);

                try (ResultSet rs = select.executeQuery())
                {
                    assertTrue(rs.next());
                    assertEquals(11001, rs.getInt("ID"));
                    assertEquals("Query", rs.getString("NAME"));
                    assertFalse(rs.next());
                }
            }
        }
    }

	/**
	 * Verifies the JDBC behavior for execute update.
	 * 
	 * @throws Exception if the operation fails
	 */
    @Test
    public void testExecuteUpdate() throws Exception
    {
        try (Connection connection = TestConnection.create();
             PreparedStatement statement = connection.prepareStatement("INSERT INTO " + TABLE + " (ID, NAME) VALUES (?, ?)"))
        {
            statement.setInt(1, 11002);
            statement.setString(2, "Insert");

            assertEquals(1, statement.executeUpdate());
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
             PreparedStatement insert = connection.prepareStatement("INSERT INTO " + TABLE + " (ID, NAME) VALUES (?, ?)"))
        {
            insert.setInt(1, 11003);
            insert.setString(2, "Before");
            insert.executeUpdate();
        }
        
        try (Connection connection = TestConnection.create();
             PreparedStatement statement = connection.prepareStatement("UPDATE " + TABLE + " SET NAME = ? WHERE ID = ?"))
        {
            statement.setString(1, "After");
            statement.setInt(2, 11003);

            assertEquals(1L, statement.executeLargeUpdate());
        }
    }

	/**
	 * Verifies the JDBC behavior for set object.
	 * 
	 * @throws Exception if the operation fails
	 */
    @Test
    public void testSetObject() throws Exception
    {
        try (Connection connection = TestConnection.create();
             PreparedStatement statement = connection.prepareStatement("INSERT INTO " + TABLE + " (ID, NAME) VALUES (?, ?)"))
        {
            statement.setObject(1, 11004, JDBCType.INTEGER);
            statement.setObject(2, "Object", JDBCType.VARCHAR);

            assertEquals(1, statement.executeUpdate());
        }
    }

	/**
	 * Verifies the JDBC behavior for set object with scale.
	 * 
	 * @throws Exception if the operation fails
	 */
    @Test
    public void testSetObjectWithScale() throws Exception
    {
        try (Connection connection = TestConnection.create();
        	 PreparedStatement statement = connection.prepareStatement("INSERT INTO " + TABLE + " (ID, NAME) VALUES (?, ?)"))
        {
            statement.setObject(1, 11005, JDBCType.INTEGER, 0);
            statement.setObject(2, "Scaled", JDBCType.VARCHAR, 100);

            assertEquals(1, statement.executeUpdate());
        }
    }

	/**
	 * Verifies the JDBC behavior for set null with sql type.
	 * 
	 * @throws Exception if the operation fails
	 */
    @Test
    public void testSetNullWithSqlType() throws Exception
    {
        try (Connection connection = TestConnection.create();
        	 PreparedStatement statement = connection.prepareStatement("INSERT INTO " + TABLE + " (ID, NAME) VALUES (?, ?)"))
        {
            statement.setObject(1, 11006, JDBCType.INTEGER);
            statement.setNull(2, java.sql.Types.VARCHAR);

            assertEquals(1, statement.executeUpdate());
        }
        
        try (Connection connection = TestConnection.create();
        	 PreparedStatement statement = connection.prepareStatement("SELECT NAME FROM " + TABLE + " WHERE ID = ?"))
        {
            statement.setInt(1, 11006);

            try (ResultSet rs = statement.executeQuery())
            {
                assertTrue(rs.next());
                assertNull(rs.getString(1));
                assertTrue(rs.wasNull());
            }
        }
    }

	/**
	 * Verifies the JDBC behavior for set object null.
	 * 
	 * @throws Exception if the operation fails
	 */
    @Test
    public void testSetObjectNull() throws Exception
    {
        try (Connection connection = TestConnection.create();
        	 PreparedStatement statement = connection.prepareStatement("INSERT INTO " + TABLE + " (ID, NAME) VALUES (?, ?)"))
        {
            statement.setInt(1, 11007);
            statement.setObject(2, null);

            assertEquals(1, statement.executeUpdate());
        }
    }

	/**
	 * Verifies the JDBC behavior for set object typed values.
	 * 
	 * @throws Exception if the operation fails
	 */
    @Test
    public void testSetObjectTypedValues() throws Exception
    {
        try (Connection connection = TestConnection.create();
        	 PreparedStatement statement = connection.prepareStatement("SELECT ? AS I, ? AS L, ? AS D, ? AS S FROM DUAL"))
        {
            statement.setObject(1, Integer.valueOf(42), JDBCType.INTEGER);
            statement.setObject(2, Long.valueOf(12345678901L), JDBCType.BIGINT);
            statement.setObject(3, new BigDecimal("123.45"), JDBCType.DECIMAL);
            statement.setObject(4, "typed", JDBCType.VARCHAR);

            try (ResultSet rs = statement.executeQuery())
            {
                assertTrue(rs.next());
                assertEquals(42, rs.getInt("I"));
                assertEquals(12345678901L, rs.getLong("L"));
                assertEquals(0, new BigDecimal("123.45").compareTo(rs.getBigDecimal("D")));
                assertEquals("typed", rs.getString("S"));
            }
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
        try (Connection connection = TestConnection.create();
        	 PreparedStatement statement = connection.prepareStatement("INSERT INTO " + TABLE + " (ID, NAME) VALUES (?, ?)"))
        {
            statement.setInt(1, 11008);
            statement.setString(2, "Old");

            statement.clearParameters();

            statement.setInt(1, 11008);
            statement.setString(2, "New");

            assertEquals(1, statement.executeUpdate());
        }
    }

	/**
	 * Verifies the JDBC behavior for overwrite parameter.
	 * 
	 * @throws Exception if the operation fails
	 */
    @Test
    public void testOverwriteParameter() throws Exception
    {
        try (Connection connection = TestConnection.create();
        	 PreparedStatement statement = connection.prepareStatement("INSERT INTO " + TABLE + " (ID, NAME) VALUES (?, ?)"))
        {
            statement.setInt(1, 11009);
            statement.setString(2, "First");
            statement.setString(2, "Second");

            assertEquals(1, statement.executeUpdate());
        }
        
        try (Connection connection = TestConnection.create();
             PreparedStatement statement = connection.prepareStatement("SELECT NAME FROM " + TABLE + " WHERE ID = ?"))
        {
            statement.setInt(1, 11009);

            try (ResultSet rs = statement.executeQuery())
            {
                assertTrue(rs.next());
                assertEquals("Second", rs.getString(1));
            }
        }
    }

	/**
	 * Verifies the JDBC behavior for date time timestamp.
	 * 
	 * @throws Exception if the operation fails
	 */
    @Test
    public void testDateTimeTimestamp() throws Exception
    {
        Date date = Date.valueOf("2025-01-15");
        Time time = Time.valueOf("12:34:56");
        Timestamp timestamp = Timestamp.valueOf("2025-01-15 12:34:56");

        try (Connection connection = TestConnection.create();
             PreparedStatement statement = connection.prepareStatement("SELECT ? AS D, ? AS T, ? AS TS FROM DUAL"))
        {
            statement.setDate(1, date);
            statement.setTime(2, time);
            statement.setTimestamp(3, timestamp);

            try (ResultSet rs = statement.executeQuery())
            {
                assertTrue(rs.next());

                assertEquals(date, rs.getDate(1));
                assertEquals(time, rs.getTime(2));
                assertEquals(timestamp, rs.getTimestamp(3));
            }
        }
    }

	/**
	 * Verifies the JDBC behavior for date time timestamp with calendar without
	 * calendar.
	 * 
	 * @throws Exception if the operation fails
	 */
    @Test
    public void testDateTimeTimestampWithCalendarWithoutCalendar() throws Exception
    {
        Calendar calendar = Calendar.getInstance();

        Date date = Date.valueOf("2025-02-10");
        Time time = Time.valueOf("10:20:30");
        Timestamp timestamp = Timestamp.valueOf("2025-02-10 10:20:30");

        try (Connection connection = TestConnection.create();
        	 PreparedStatement statement = connection.prepareStatement("SELECT ? AS D, ? AS T, ? AS TS FROM DUAL"))
        {
            statement.setDate(1, date, calendar);
            statement.setTime(2, time, calendar);
            statement.setTimestamp(3, timestamp, calendar);

            try (ResultSet rs = statement.executeQuery())
            {
                assertTrue(rs.next());

                assertEquals(date, rs.getDate(1));
                assertEquals(time, rs.getTime(2));
                assertEquals(timestamp, rs.getTimestamp(3));
            }
        }
    }

	/**
	 * Verifies the JDBC behavior for date time timestamp with calendar.
	 * 
	 * @throws Exception if the operation fails
	 */
    @Test
    public void testDateTimeTimestampWithCalendar() throws Exception
    {
        Calendar calendar = Calendar.getInstance();

        Date date = Date.valueOf("2025-02-10");
        Time time = Time.valueOf("10:20:30");
        Timestamp timestamp = Timestamp.valueOf("2025-02-10 10:20:30");

        try (Connection connection = TestConnection.create();
             PreparedStatement statement = connection.prepareStatement("SELECT ? AS D, ? AS T, ? AS TS FROM DUAL"))
        {
            statement.setDate(1, date, calendar);
            statement.setTime(2, time, calendar);
            statement.setTimestamp(3, timestamp, calendar);

            try (ResultSet rs = statement.executeQuery())
            {
                assertTrue(rs.next());

                assertEquals(date, rs.getDate(1, calendar));
                assertEquals(time, rs.getTime(2, calendar));
                assertEquals(timestamp, rs.getTimestamp(3, calendar));
            }
        }
    }

	/**
	 * Verifies the JDBC behavior for mixed parameter types.
	 * 
	 * @throws Exception if the operation fails
	 */
    @Test
    public void testMixedParameterTypes() throws Exception
    {
        try (Connection connection = TestConnection.create();
             PreparedStatement statement = connection.prepareStatement("INSERT INTO " + TABLE + " (ID, NAME) VALUES (?, ?)"))
        {
            statement.setObject(1, 11010, JDBCType.INTEGER);
            statement.setObject(2, "Mixed", JDBCType.VARCHAR);

            assertEquals(1, statement.executeUpdate());
        }
    }

	/**
	 * Verifies the JDBC behavior for batch.
	 * 
	 * @throws Exception if the operation fails
	 */
    @Test
    public void testBatch() throws Exception
    {
        try (Connection connection = TestConnection.create();
             PreparedStatement statement = connection.prepareStatement("INSERT INTO " + TABLE + " (ID, NAME) VALUES (?, ?)"))
        {
            statement.setInt(1, 11011);
            statement.setString(2, "A");
            statement.addBatch();

            statement.setInt(1, 11012);
            statement.setString(2, "B");
            statement.addBatch();

            int[] counts = statement.executeBatch();

            assertNotNull(counts);
            assertEquals(2, counts.length);
        }
    }

	/**
	 * Verifies the JDBC behavior for large batch.
	 * 
	 * @throws Exception if the operation fails
	 */
    @Test
	public void testLargeBatch() throws Exception
	{
	    try (Connection connection = TestConnection.create();
	         PreparedStatement statement = connection.prepareStatement("INSERT INTO " + TABLE + " (ID, NAME) VALUES (?, ?)"))
	    {
	        statement.setInt(1, 11013);
	        statement.setString(2, "A");
	        statement.addBatch();
	
	        statement.setInt(1, 11014);
	        statement.addBatch();
	
	        long[] counts = statement.executeLargeBatch();
	
	        assertNotNull(counts);
	        assertEquals(2, counts.length);
	    }
	
	    try (Connection connection = TestConnection.create();
	         PreparedStatement statement = connection.prepareStatement("SELECT ID, NAME FROM " + TABLE + " WHERE ID IN (?, ?) ORDER BY ID"))
	    {
	        statement.setInt(1, 11013);
	        statement.setInt(2, 11014);
	
	        try (ResultSet rs = statement.executeQuery())
	        {
	            assertTrue(rs.next());
	            assertEquals(11013, rs.getInt("ID"));
	            assertEquals("A", rs.getString("NAME"));
	
	            assertTrue(rs.next());
	            assertEquals(11014, rs.getInt("ID"));
	            assertEquals("A", rs.getString("NAME"));
	
	            assertFalse(rs.next());
	        }
	    }
	}
    
	/**
	 * Verifies the JDBC behavior for clear parameters after batch.
	 * 
	 * @throws Exception if the operation fails
	 */
    @Test
	public void testClearParametersAfterBatch() throws Exception
	{
	    try (Connection connection = TestConnection.create();
	         PreparedStatement statement = connection.prepareStatement("INSERT INTO TEST_TABLE (ID, NAME) VALUES (?, ?)"))
	    {
	        statement.setInt(1, 11015);
	        statement.setString(2, "A");
	        statement.addBatch();
	
	        statement.clearParameters();
	
	        statement.setInt(1, 11016);
	        statement.setString(2, "B");
	        statement.addBatch();
	
	        long[] counts = statement.executeLargeBatch();
	
	        assertNotNull(counts);
	        assertEquals(2, counts.length);
	    }
	
	    try (Connection connection = TestConnection.create();
	         PreparedStatement statement = connection.prepareStatement("SELECT ID, NAME FROM TEST_TABLE WHERE ID IN (?, ?) ORDER BY ID"))
	    {
	        statement.setInt(1, 11015);
	        statement.setInt(2, 11016);
	
	        try (ResultSet rs = statement.executeQuery())
	        {
	            assertTrue(rs.next());
	            assertEquals(11015, rs.getInt("ID"));
	            assertEquals("A", rs.getString("NAME"));
	
	            assertTrue(rs.next());
	            assertEquals(11016, rs.getInt("ID"));
	            assertEquals("B", rs.getString("NAME"));
	
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
	         PreparedStatement statement = connection.prepareStatement("INSERT INTO " + TABLE + " (ID, NAME) VALUES (?, ?)"))
	    {
	        statement.setInt(1, 11017);
	        statement.setString(2, "A");
	        statement.addBatch();
	
	        statement.clearBatch();
	
	        // Die aktuellen Parameter existieren weiterhin.
	        statement.setInt(1, 11018);
	        statement.addBatch();
	
	        long[] counts = statement.executeLargeBatch();
	
	        assertNotNull(counts);
	        assertEquals(1, counts.length);
	    }
	
	    try (Connection connection = TestConnection.create();
	         PreparedStatement statement = connection.prepareStatement("SELECT ID, NAME FROM " + TABLE + " WHERE ID IN (?, ?) ORDER BY ID"))
	    {
	        statement.setInt(1, 11017);
	        statement.setInt(2, 11018);
	
	        try (ResultSet rs = statement.executeQuery())
	        {
	            // 11017 wurde durch clearBatch() verworfen.
	            assertTrue(rs.next());
	            assertEquals(11018, rs.getInt("ID"));
	            assertEquals("A", rs.getString("NAME"));
	
	            assertFalse(rs.next());
	        }
	    }
	}	    
    
    /**
     * Verifies multiple changes of same parameter between two batch calls.
     * @throws Exception
     */
	@Test
	public void testLatestParameterValueIsCapturedByAddBatch() throws Exception
	{
	    try (Connection connection = TestConnection.create();
	    	 PreparedStatement statement = connection.prepareStatement("INSERT INTO " + TABLE + " (ID, NAME) VALUES (?, ?)"))
	    {
	        statement.setInt(1, 1);
	        statement.setString(2, "A");
	
	        // Change parameter 2 before addBatch again
	        statement.setString(2, "B");
	        statement.addBatch();
	
	        statement.setInt(1, 2);
	
	        // Again: parameter 2 before addBatch
	        statement.setString(2, "C");
	        statement.setString(2, "D");
	        statement.addBatch();
	
	        int[] updateCounts = statement.executeBatch();
	
	        assertNotNull(updateCounts);
	        assertEquals(2, updateCounts.length);
	    }
	
	    try (Connection connection = TestConnection.create();
	    	 PreparedStatement statement = connection.prepareStatement("SELECT ID, NAME FROM " + TABLE + " WHERE ID IN (?, ?) ORDER BY ID"))
	    {
	        statement.setInt(1, 1);
	        statement.setInt(2, 2);
	
	        try (ResultSet resultSet = statement.executeQuery())
	        {
	            assertTrue(resultSet.next());
	            assertEquals(1, resultSet.getInt("ID"));
	            assertEquals("B", resultSet.getString("NAME"));
	
	            assertTrue(resultSet.next());
	            assertEquals(2, resultSet.getInt("ID"));
	            assertEquals("D", resultSet.getString("NAME"));
	
	            assertFalse(resultSet.next());
	        }
	    }
	}    

	/**
	 * Verifies that repeated addBatch() calls create independent batch rows.
	 * 
	 * @throws Exception if the operation fails
	 */
	@Test
	public void testRepeatedAddBatchCreatesIndependentRows() throws Exception
	{
	    try (Connection connection = TestConnection.create();
	    	 PreparedStatement statement = connection.prepareStatement("INSERT INTO TEST_TABLE (ID, NAME) VALUES (?, ?)"))
	    {
	        statement.setInt(1, 11021);
	        statement.setString(2, "Same");
	        statement.addBatch();
	
	        statement.setInt(1, 11022);
	        statement.addBatch();
	
	        statement.setInt(1, 11023);
	        statement.addBatch();
	
	        int[] updateCounts = statement.executeBatch();
	
	        assertNotNull(updateCounts);
	        assertEquals(3, updateCounts.length);
	    }
	
	    try (Connection connection = TestConnection.create();
	    	 PreparedStatement statement = connection.prepareStatement("SELECT ID, NAME FROM TEST_TABLE WHERE ID IN (?, ?, ?) ORDER BY ID"))
	    {
	    	statement.setInt(1, 11021);
	        statement.setInt(2, 11022);
	        statement.setInt(3, 11023);
	
	        try (ResultSet resultSet = statement.executeQuery())
	        {
	            assertTrue(resultSet.next());
	            assertEquals(11021, resultSet.getInt("ID"));
	            assertEquals("Same", resultSet.getString("NAME"));
	
	            assertTrue(resultSet.next());
	            assertEquals(11022, resultSet.getInt("ID"));
	            assertEquals("Same", resultSet.getString("NAME"));
	
	            assertTrue(resultSet.next());
	            assertEquals(11023, resultSet.getInt("ID"));
	            assertEquals("Same", resultSet.getString("NAME"));
	
	            assertFalse(resultSet.next());
	        }
	    }
	}	
	
	/**
	 * Verifies that executing a batch twice does not execute the same rows again.
	 * 
	 * @throws Exception if the operation fails
	 */
	@Test
	public void testExecuteBatchTwice() throws Exception
	{
	    try (Connection connection = TestConnection.create();
	    	 PreparedStatement statement = connection.prepareStatement("INSERT INTO TEST_TABLE (ID, NAME) VALUES (?, ?)"))
	    {
	        statement.setInt(1, 11024);
	        statement.setString(2, "First");
	        statement.addBatch();
	
	        int[] firstCounts = statement.executeBatch();
	
	        assertNotNull(firstCounts);
	        assertEquals(1, firstCounts.length);
	
	        int[] secondCounts = statement.executeBatch();
	
	        assertNotNull(secondCounts);
	        assertEquals(0, secondCounts.length);
	    }
	}	
	
	/**
	 * Verifies the JDBC behavior for parameter meta data.
	 * 
	 * @throws Exception if the operation fails
	 */
    @Test
    public void testParameterMetaData() throws Exception
    {
        try (Connection connection = TestConnection.create();
             PreparedStatement statement = connection.prepareStatement("SELECT ID, NAME FROM " + TABLE + " WHERE ID = ?"))
        {
            ParameterMetaData metadata = statement.getParameterMetaData();

            assertNotNull(metadata);
            assertEquals(1, metadata.getParameterCount());
        }
    }

	/**
	 * Verifies the JDBC behavior for result set meta data.
	 * 
	 * @throws Exception if the operation fails
	 */
    @Test
    public void testResultSetMetaData() throws Exception
    {
        try (Connection connection = TestConnection.create();
             PreparedStatement statement = connection.prepareStatement("SELECT ID, NAME FROM " + TABLE + " WHERE ID = ?"))
        {
            statement.setInt(1, 11016);

            ResultSetMetaData metadata = statement.getMetaData();

            if (metadata == null)
            {
                statement.executeQuery();
                metadata = statement.getMetaData();
            }
            
            assertNotNull(metadata);
            assertEquals(2, metadata.getColumnCount());

            assertEquals("ID", metadata.getColumnName(1));
            assertEquals("NAME", metadata.getColumnName(2));
        }
    }

	/**
	 * Verifies the JDBC behavior for execute.
	 * 
	 * @throws Exception if the operation fails
	 */
    @Test
    public void testExecute() throws Exception
    {
        try (Connection connection = TestConnection.create();
	         PreparedStatement statement = connection.prepareStatement("SELECT ID FROM " + TABLE + " WHERE ID = ?"))
        {
            statement.setInt(1, 11017);

            // Create the record required by this test.
            try (PreparedStatement insert = connection.prepareStatement("INSERT INTO " + TABLE + " (ID, NAME) VALUES (?, ?)"))
            {
                insert.setInt(1, 11017);
                insert.setString(2, "Execute");
                insert.executeUpdate();
            }

            boolean hasResultSet = statement.execute();

            assertTrue(hasResultSet);

            try (ResultSet rs = statement.getResultSet())
            {
                assertNotNull(rs);
                assertTrue(rs.next());
                assertEquals(11017, rs.getInt(1));
            }
        }
    }

	/**
	 * Verifies the JDBC behavior for big decimal scale from oracle.
	 * 
	 * @throws Exception if the operation fails
	 */
    @Test
    public void testBigDecimalScaleFromOracle() throws Exception
    {
        try (Connection connection = TestConnection.create();
             PreparedStatement statement = connection.prepareStatement("SELECT CAST(12345.67890 AS NUMBER(20,5)) AS D, " +
            		 												   "CAST(1.2300 AS NUMBER(20,4)) AS D2, " +
            		 												   "CAST(1.0 AS NUMBER(20,1)) AS D3 FROM DUAL"))
        {
            try (ResultSet rs = statement.executeQuery())
            {
                assertTrue(rs.next());

                BigDecimal d = rs.getBigDecimal("D");
                BigDecimal d2 = rs.getBigDecimal("D2");
                BigDecimal d3 = rs.getBigDecimal("D3");

                assertNotNull(d);
                assertNotNull(d2);
                assertNotNull(d3);

                assertEquals(0, new BigDecimal("12345.67890").compareTo(d));
                assertEquals(0, new BigDecimal("1.2300").compareTo(d2));
                assertEquals(0, new BigDecimal("1.0").compareTo(d3));

                //Oracle returns 4 and not 5
                assertEquals(4, d.scale());
                assertEquals(2, d2.scale());
                assertEquals(0, d3.scale());
            }
        }
    }
}
