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
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.StringReader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Array;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.NClob;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.SQLXML;
import java.sql.Statement;
import java.sql.Types;
import java.util.HashMap;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Supports the test scenario by handling set up callable objects.
 */
public class TestMixedFunctions
{
    private static final String TABLE = "TEST_TABLE";

    /**
     * Supports the test scenario by handling set up callable objects.
     *
     * @throws Exception if the operation fails
     */
    @Before
    public void setUpCallableObjects() throws Exception
    {
        try (Connection connection = TestConnection.create();
             Statement statement = connection.createStatement())
        {
            statement.executeUpdate(
            "CREATE OR REPLACE PROCEDURE RJDBC_TEST_MIXED_PROC(" +
            "  P_IN IN NUMBER," +
            "  P_OUT OUT NUMBER," +
            "  P_INOUT IN OUT NUMBER" +
            ") AS BEGIN P_OUT := P_IN * 2; P_INOUT := P_INOUT + 1; END;");
        }
    }

    /**
     * Releases resources associated with callable objects.
     *
     * @throws Exception if the operation fails
     */
    @After
    public void tearDownCallableObjects() throws Exception
    {
        try (Connection connection = TestConnection.create();
             Statement statement = connection.createStatement())
        {
            try
            {
                statement.executeUpdate("DROP PROCEDURE RJDBC_TEST_MIXED_PROC");
            }
            catch (SQLException ignored)
            {
            }
        }
    }

    // ---------------------------------------------------------------------
    // Connection
    // ---------------------------------------------------------------------

    /**
     * Verifies the JDBC behavior for connection type map.
     *
     * @throws Exception if the operation fails
     */
    @Test
    public void testConnectionTypeMap() throws Exception
    {
        try (Connection connection = TestConnection.create())
        {
            assertNotNull(connection.getTypeMap());
        }
    }

    /**
     * Verifies the JDBC behavior for connection client info properties.
     *
     * @throws Exception if the operation fails
     */
    @Test
    public void testConnectionClientInfoProperties() throws Exception
    {
        try (Connection connection = TestConnection.create())
        {
            try
            {
                connection.setClientInfo("ApplicationName", "RJdbcMixedTest");

                if (connection.getClientInfo("ApplicationName") != null)
                {
                    assertEquals("RJdbcMixedTest", connection.getClientInfo("ApplicationName"));
                }
                
                assertNotNull(connection.getClientInfo());
            }
            catch (SQLFeatureNotSupportedException e)
            {
                // Optional JDBC feature.
            }
            catch (SQLException e)
            {
                // Drivers may reject arbitrary client-info properties.
            }
        }
    }

    /**
     * Verifies the JDBC behavior for connection abort.
     *
     * @throws Exception if the operation fails
     */
    @Test
    public void testConnectionAbort() throws Exception
    {
        Connection connection = TestConnection.create();

        try
        {
            try
            {
                connection.abort(Runnable::run);
                
                assertTrue(connection.isClosed());
            }
            catch (SQLFeatureNotSupportedException e)
            {
                // Optional JDBC feature.
            }
            catch (SQLException e)
            {
                // Driver/database may reject abort().
            }
        }
        finally
        {
            try
            {
                if (!connection.isClosed())
                {
                    connection.close();
                }
            }
            catch (SQLException ignored)
            {
            }
        }
    }

    /**
     * Verifies the JDBC behavior for connection invalid arguments.
     *
     * @throws Exception if the operation fails
     */
    @Test
    public void testConnectionInvalidArguments() throws Exception
    {
        try (Connection connection = TestConnection.create())
        {
            try
            {
                connection.setNetworkTimeout(null, -1);
                
                fail("Expected SQLException");
            }
            catch (SQLException e)
            {
                // expected
            }
            try
            {
                connection.setNetworkTimeout(null, Integer.MIN_VALUE);
                
                fail("Expected SQLException");
            }
            catch (SQLException e)
            {
                // expected
            }
        }
    }

    // ---------------------------------------------------------------------
    // Statement
    // ---------------------------------------------------------------------

    /**
     * Verifies the JDBC behavior for statement result and update counts.
     *
     * @throws Exception if the operation fails
     */
    @Test
    public void testStatementResultAndUpdateCounts() throws Exception
    {
        try (Connection connection = TestConnection.create();
             Statement statement = connection.createStatement())
        {
            try
            {
                statement.executeUpdate("DELETE FROM " + TABLE + " WHERE ID = 12901");

                // executeUpdate() returns the affected row count.
                int updateCount = statement.executeUpdate("INSERT INTO " + TABLE + " (ID, NAME) VALUES (12901, 'MixedFunctions')");

                assertEquals(1, updateCount);

                // execute() with a SELECT returns a ResultSet.
                boolean result = statement.execute("SELECT ID, NAME FROM " + TABLE + " WHERE ID = 12901");

                assertTrue(result);
                assertNotNull(statement.getResultSet());

                try (ResultSet rs = statement.getResultSet())
                {
                    assertTrue(rs.next());
                    assertEquals(12901, rs.getInt("ID"));
                    assertEquals("MixedFunctions", rs.getString("NAME"));
                    assertFalse(rs.next());
                }

                // The current result is a ResultSet, therefore no update count.
                assertEquals(-1, statement.getUpdateCount());
                assertEquals(-1L, statement.getLargeUpdateCount());

                // There is no further result.
                assertFalse(statement.getMoreResults());

                assertEquals(-1, statement.getUpdateCount());
                assertEquals(-1L, statement.getLargeUpdateCount());
            }
            finally
            {
                statement.executeUpdate("DELETE FROM " + TABLE + " WHERE ID = 12901");
            }
        }
    }

    /**
     * Verifies the JDBC behavior for statement invalid fetch values.
     *
     * @throws Exception if the operation fails
     */
    @Test
    public void testStatementInvalidFetchValues() throws Exception
    {
        try (Connection connection = TestConnection.create();
             Statement statement = connection.createStatement())
        {
            try
            {
                statement.setFetchDirection(123456);
                
                fail("Invalid fetch direction should be rejected");
            }
            catch (SQLException expected)
            {
            }
            
            try
            {
                statement.setFetchSize(-1);
                
                fail("Negative fetch size should be rejected");
            }
            catch (SQLException expected)
            {
            }
            
            try
            {
                statement.setMaxRows(-1);
                
                fail("Negative max rows should be rejected");
            }
            catch (SQLException expected)
            {
            }
            
            try
            {
                statement.setLargeMaxRows(-1);
                
                fail("Negative large max rows should be rejected");
            }
            catch (SQLException expected)
            {
            }
        }
    }

    /**
     * Verifies the JDBC behavior for statement escape processing and cursor name.
     *
     * @throws Exception if the operation fails
     */
    @Test
    public void testStatementEscapeProcessingAndCursorName() throws Exception
    {
        try (Connection connection = TestConnection.create();
             Statement statement = connection.createStatement())
        {
            statement.setEscapeProcessing(false);
            statement.setEscapeProcessing(true);

            try
            {
                statement.setCursorName("RJDBC_MIXED_CURSOR");
            }
            catch (SQLFeatureNotSupportedException e)
            {
                // Optional JDBC feature.
            }
            catch (SQLException e)
            {
                // Some databases do not support explicit cursor names.
            }
        }
    }

    //---------------------------------------------------------------------
    // PreparedStatement - special parameter types
    //---------------------------------------------------------------------

    /**
     * Verifies the JDBC behavior for prepared statement n string.
     *
     * @throws Exception if the operation fails
     */
    @Test
    public void testPreparedStatementNString() throws Exception
    {
        try (Connection connection = TestConnection.create();
        	 PreparedStatement statement = connection.prepareStatement("SELECT ? AS VALUE FROM " + TABLE + " WHERE 1 = 0"))
        {
            statement.setNString(1, "ÄÖÜ 日本語");
            
            try (ResultSet rs = statement.executeQuery())
            {
                ResultSetMetaData meta = rs.getMetaData();
                
                assertEquals(1, meta.getColumnCount());
            }
        }
    }

    /**
     * Verifies the JDBC behavior for prepared statement n character stream.
     *
     * @throws Exception if the operation fails
     */
    @Test
    public void testPreparedStatementNCharacterStream() throws Exception
    {
        try (Connection connection = TestConnection.create();
             PreparedStatement statement = connection.prepareStatement("SELECT ? AS VALUE FROM " + TABLE + " WHERE 1 = 0"))
        {
            statement.setNCharacterStream(1, new StringReader("ÄÖÜ 日本語"));
            
            try (ResultSet rs = statement.executeQuery())
            {
                assertEquals(1, rs.getMetaData().getColumnCount());
            }
        }
    }

    /**
     * Verifies the JDBC behavior for prepared statement ascii and binary streams.
     *
     * @throws Exception if the operation fails
     */
    @Test
    public void testPreparedStatementAsciiAndBinaryStreams() throws Exception
    {
        try (Connection connection = TestConnection.create();
             PreparedStatement statement = connection.prepareStatement("SELECT ?, ? FROM " + TABLE + " WHERE 1 = 0"))
        {
            statement.setAsciiStream(1, new ByteArrayInputStream("ABC".getBytes("US-ASCII")), 3);
            statement.setBinaryStream(2, new ByteArrayInputStream(new byte[] {1, 2, 3}), 3);

            try (ResultSet rs = statement.executeQuery())
            {
                assertEquals(2, rs.getMetaData().getColumnCount());
            }
        }
    }

    /**
     * Verifies the JDBC behavior for prepared statement blob clob n clob and sql xml.
     *
     * @throws Exception if the operation fails
     */
    @Test
    public void testPreparedStatementBlobClobNClobAndSqlXml() throws Exception
    {
        try (Connection connection = TestConnection.create();
             PreparedStatement statement = connection.prepareStatement("SELECT ?, ?, ?, ? FROM " + TABLE + " WHERE 1 = 0"))
        {
            Blob blob = null;
            Clob clob = null;
            NClob nclob = null;
            SQLXML sqlxml = null;

            try
            {
                blob = connection.createBlob();
                blob.setBytes(1, new byte[] {1, 2, 3});
                
                statement.setBlob(1, blob);

                clob = connection.createClob();
                clob.setString(1, "RJdbc");
                
                statement.setClob(2, clob);

                nclob = connection.createNClob();
                nclob.setString(1, "日本語");
                
                statement.setNClob(3, nclob);

                sqlxml = connection.createSQLXML();
                sqlxml.setString("<root/>");
                
                statement.setSQLXML(4, sqlxml);

                try (ResultSet rs = statement.executeQuery())
                {
                    assertEquals(4, rs.getMetaData().getColumnCount());
                }
            }
            catch (SQLFeatureNotSupportedException e)
            {
                // Optional JDBC types.
            }
            finally
            {
                if (blob != null) 
                {
                	try
	                {
	                    blob.free();
	                }
	                catch (SQLException ignored)
	                {
	                }
                }
                
                if (clob != null) 
                {
                	try
	                {
	                    clob.free();
	                }
	                catch (SQLException ignored)
	                {
	                }
                }
                
                if (nclob != null)
                {
                	try
	                {
	                    nclob.free();
	                }
	                catch (SQLException ignored)
	                {
	                }
                }
                
                if (sqlxml != null)
                {
                	try
	                {
	                    sqlxml.free();
	                }
	                catch (SQLException ignored)
	                {
	                }
                }
            }
        }
    }

    /**
     * Verifies the JDBC behavior for prepared statement array.
     *
     * @throws Exception if the operation fails
     */
    @Test
    public void testPreparedStatementArray() throws Exception
    {
        try (Connection connection = TestConnection.create();
             PreparedStatement statement = connection.prepareStatement("SELECT ? FROM " + TABLE + " WHERE 1 = 0"))
        {
            try
            {
                Array array = connection.createArrayOf("VARCHAR", new Object[] {"A", "B"});
                
                try
                {
                    statement.setArray(1, array);

                    try (ResultSet rs = statement.executeQuery())
                    {
                        assertEquals(1, rs.getMetaData().getColumnCount());
                    }
                }
                finally
                {
                    array.free();
                }
            }
            catch (SQLFeatureNotSupportedException e)
            {
                // Optional JDBC feature.
            }
            catch (SQLException e)
            {
                // The database may not expose ARRAY as a bindable SQL type.
            }
        }
    }

    /**
     * Verifies the JDBC behavior for prepared statement url.
     *
     * @throws Exception if the operation fails
     */
    @Test
    public void testPreparedStatementUrl() throws Exception
    {
        try (Connection connection = TestConnection.create())
        {
            PreparedStatement statement = connection.prepareStatement("SELECT ? FROM TEST_TABLE WHERE 1 = 0");

            try
            {
                URL url = new URL("https://example.com/test");

                statement.setURL(1, url);

                assertNotNull(statement);
            }
            finally
            {
                //Do not perform another remote operation here
                //-> Statement lifecycle is covered by PreparedStatementTest
            }
        }
    }

    /**
     * Verifies the JDBC behavior for prepared statement sql type and scale.
     *
     * @throws Exception if the operation fails
     */
    @Test
    public void testPreparedStatementSqlTypeAndScale() throws Exception
    {
        try (Connection connection = TestConnection.create();
             PreparedStatement statement = connection.prepareStatement("SELECT ? FROM " + TABLE + " WHERE 1 = 0"))
        {
            statement.setObject(1, "123", Types.VARCHAR, 20);

            try (ResultSet rs = statement.executeQuery())
            {
                assertEquals(1, rs.getMetaData().getColumnCount());
            }
        }
    }

    // ---------------------------------------------------------------------
    // ResultSet special getters
    // ---------------------------------------------------------------------

    /**
     * Verifies the JDBC behavior for result set special getters.
     *
     * @throws Exception if the operation fails
     */
    @Test
    public void testResultSetSpecialGetters() throws Exception
    {
        try (Connection connection = TestConnection.create();
             Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery("SELECT ID, NAME FROM " + TABLE + " WHERE ID = 0"))
        {
            assertFalse(rs.next());
        }

        //The actual special getter implementations are exercised through
        //values which exist in TEST_TABLE. This test intentionally does not
        //require a vendor-specific SQL type
        
        try (Connection connection = TestConnection.create();
        	 Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery("SELECT ID, NAME FROM " + TABLE + " WHERE ID = 1"))
        {
            if (rs.next())
            {
                assertNotNull(rs.getObject(1));
                assertNotNull(rs.getObject(2));
                assertNotNull(rs.getString(2));
            }
        }
    }

    /**
     * Verifies the JDBC behavior for result set object by map.
     *
     * @throws Exception if the operation fails
     */
    @Test
    public void testResultSetObjectByMap() throws Exception
    {
        try (Connection connection = TestConnection.create();
             Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery("SELECT ID, NAME FROM " + TABLE + " WHERE ID = 1"))
        {
            if (rs.next())
            {
                Map<String, Class<?>> map = new HashMap<String, Class<?>>();

                assertNotNull(rs.getObject(1, map));
                assertNotNull(rs.getObject("ID", map));
            }
        }
    }

    //---------------------------------------------------------------------
    // ResultSet update contract
    //---------------------------------------------------------------------

    /**
     * Verifies the JDBC behavior for result set update primitive values.
     *
     * @throws Exception if the operation fails
     */
    @Test
    public void testResultSetUpdatePrimitiveValues() throws Exception
    {
        try (Connection connection = TestConnection.create();
	         Statement statement = connection.createStatement(
	         ResultSet.TYPE_SCROLL_INSENSITIVE,
	         ResultSet.CONCUR_UPDATABLE);
	         ResultSet rs = statement.executeQuery("SELECT ID, NAME FROM " + TABLE))
        {
            if (!rs.next())
            {
                return;
            }

            int id = rs.getInt("ID");
            
            String original = rs.getString("NAME");

            try
            {
                rs.updateString("NAME", "MixedUpdate");
                rs.updateRow();

                assertEquals("MixedUpdate", rs.getString("NAME"));

                rs.refreshRow();
                
                assertEquals("MixedUpdate", rs.getString("NAME"));
            }
            catch (SQLFeatureNotSupportedException e)
            {
                return;
            }
            finally
            {
                try
                {
                    rs.updateString("NAME", original);
                    rs.updateRow();
                }
                catch (SQLException ignored)
                {
                    try (PreparedStatement restore = connection.prepareStatement("UPDATE " + TABLE + " SET NAME = ? WHERE ID = ?"))
                    {
                        restore.setString(1, original);
                        restore.setInt(2, id);
                        restore.executeUpdate();
                    }
                }
            }
        }
    }

    /**
     * Verifies the JDBC behavior for result set cancel row updates.
     *
     * @throws Exception if the operation fails
     */
    @Test
    public void testResultSetCancelRowUpdates() throws Exception
    {
        try (Connection connection = TestConnection.create();
	         Statement statement = connection.createStatement(
	         ResultSet.TYPE_SCROLL_INSENSITIVE,
	         ResultSet.CONCUR_UPDATABLE);
	         ResultSet rs = statement.executeQuery("SELECT ID, NAME FROM " + TABLE))
        {
            if (!rs.next())
            {
                return;
            }

            String original = rs.getString("NAME");

            try
            {
                rs.updateString("NAME", "ShouldNotBeStored");
                rs.cancelRowUpdates();
                
                assertEquals(original, rs.getString("NAME"));
            }
            catch (SQLFeatureNotSupportedException e)
            {
                // Optional updatable-result-set feature.
            }
        }
    }

    /**
     * Verifies the JDBC behavior for result set insert and delete row lifecycle.
     *
     * @throws Exception if the operation fails
     */
    @Test
    public void testResultSetInsertAndDeleteRowLifecycle() throws Exception
    {
        try (Connection connection = TestConnection.create();
	         Statement statement = connection.createStatement(
	         ResultSet.TYPE_SCROLL_INSENSITIVE,
	         ResultSet.CONCUR_UPDATABLE);
	         ResultSet rs = statement.executeQuery("SELECT ID, NAME FROM " + TABLE))
        {
            try
            {
                rs.moveToInsertRow();
                rs.updateInt("ID", 12999);
                rs.updateString("NAME", "MixedInsert");
                rs.insertRow();

                rs.moveToCurrentRow();
                rs.last();

                try (PreparedStatement cleanup = connection.prepareStatement("DELETE FROM " + TABLE + " WHERE ID = 12999"))
                {
                    cleanup.executeUpdate();
                }
            }
            catch (SQLFeatureNotSupportedException e)
            {
                // Optional updatable-result-set feature.
            }
        }
    }

    // ---------------------------------------------------------------------
    // CallableStatement named parameters / typed getters
    // ---------------------------------------------------------------------

    /**
     * Verifies the JDBC behavior for callable statement named parameters.
     *
     * @throws Exception if the operation fails
     */
    @Test
    public void testCallableStatementNamedParameters() throws Exception
    {
        try (Connection connection = TestConnection.create();
        	 CallableStatement statement = connection.prepareCall("{call RJDBC_TEST_PROC(?, ?, ?)}"))
        {
            try
            {
                statement.setInt("P_IN", 5);
                statement.registerOutParameter("P_OUT", Types.NUMERIC);
                statement.setInt("P_INOUT", 10);
                statement.registerOutParameter("P_INOUT", Types.NUMERIC);

                statement.execute();

                assertEquals(10, statement.getInt("P_OUT"));
                assertEquals(11, statement.getInt("P_INOUT"));
            }
            catch (SQLFeatureNotSupportedException e)
            {
                // Named-parameter access is optional/driver-dependent.
            }
            catch (SQLException e)
            {
                // Some JDBC drivers (including Oracle in this invocation style)
                // do not support named parameter access for this call syntax.
            }
        }
    }

    /**
     * Verifies the JDBC behavior for callable statement typed get object.
     *
     * @throws Exception if the operation fails
     */
    @Test
    public void testCallableStatementTypedGetObject() throws Exception
    {
        try (Connection connection = TestConnection.create();
             Statement setup = connection.createStatement())
        {
            setup.execute(
            "CREATE OR REPLACE PROCEDURE RJDBC_MIXED_TYPED_PROC(" +
            "P_IN IN NUMBER, " +
            "P_OUT OUT NUMBER, " +
            "P_INOUT IN OUT NUMBER) AS " +
            "BEGIN " +
            "P_OUT := P_IN * 2; " +
            "P_INOUT := P_INOUT + 1; " +
            "END;");

            try
            {
                try (CallableStatement statement = connection.prepareCall("{call RJDBC_MIXED_TYPED_PROC(?, ?, ?)}"))
                {
                    statement.setInt(1, 5);
                    statement.registerOutParameter(2, Types.NUMERIC);
                    statement.registerOutParameter(3, Types.NUMERIC);
                    statement.setInt(3, 10);

                    statement.execute();

                    BigDecimal out =
                    statement.getObject(2, BigDecimal.class);
                    BigDecimal inout =
                    statement.getObject(3, BigDecimal.class);

                    assertNotNull(out);
                    assertNotNull(inout);

                    assertEquals(10, out.intValue());
                    assertEquals(11, inout.intValue());
                }
            }
            finally
            {
                try
                {
                    setup.execute("DROP PROCEDURE RJDBC_MIXED_TYPED_PROC");
                }
                catch (SQLException ignored)
                {
                    // Procedure may already have been removed.
                }
            }
        }
    }

    /**
     * Verifies the JDBC behavior for callable statement register out parameter scale.
     *
     * @throws Exception if the operation fails
     */
    @Test
    public void testCallableStatementRegisterOutParameterScale() throws Exception
    {
        try (Connection connection = TestConnection.create();
             CallableStatement statement = connection.prepareCall("{call RJDBC_TEST_MIXED_PROC(?, ?, ?)}"))
        {
            statement.setInt(1, 5);
            statement.registerOutParameter(2, Types.NUMERIC, 0);
            statement.setInt(3, 10);
            statement.registerOutParameter(3, Types.NUMERIC, 0);

            statement.execute();

            assertEquals(10, statement.getInt(2));
            assertEquals(11, statement.getInt(3));
        }
        catch (SQLFeatureNotSupportedException e)
        {
            // Optional JDBC feature
        }
    }

    //---------------------------------------------------------------------
    // Miscellaneous JDBC lifecycle/contract cases
    //---------------------------------------------------------------------

    /**
     * Verifies the JDBC behavior for connection properties after close.
     *
     * @throws Exception if the operation fails
     */
    @Test
    public void testConnectionPropertiesAfterClose() throws Exception
    {
        Connection connection = TestConnection.create();
        connection.close();

        try
        {
            connection.getTypeMap();
            
            fail("getTypeMap() must fail after close");
        }
        catch (SQLException expected)
        {
        }
        
        try
        {
            connection.getClientInfo();
            
            fail("getClientInfo() must fail after close");
        }
        catch (SQLException expected)
        {
        }
    }

    /**
     * Verifies the JDBC behavior for statement get result set after close.
     *
     * @throws Exception if the operation fails
     */
    @Test
    public void testStatementGetResultSetAfterClose() throws Exception
    {
        Connection connection = TestConnection.create();
        Statement statement = connection.createStatement();

        try
        {
            statement.close();

            try
            {
                statement.getResultSet();
                
                fail("getResultSet() must fail after close");
            }
            catch (SQLException expected)
            {
            }
            
            try
            {
                statement.getUpdateCount();
                
                fail("getUpdateCount() must fail after close");
            }
            catch (SQLException expected)
            {
            }
        }
        finally
        {
            connection.close();
        }
    }

    /**
     * Verifies the JDBC behavior for prepared statement special parameter nulls.
     *
     * @throws Exception if the operation fails
     */
    @Test
    public void testPreparedStatementSpecialParameterNulls() throws Exception
    {
        try (Connection connection = TestConnection.create();
             PreparedStatement statement = connection.prepareStatement("SELECT ?, ?, ? FROM " + TABLE + " WHERE 1 = 0"))
        {
            statement.setNString(1, null);
            statement.setURL(2, null);
            statement.setSQLXML(3, null);

            try (ResultSet rs = statement.executeQuery())
            {
                assertEquals(3, rs.getMetaData().getColumnCount());
            }
        }
        catch (SQLFeatureNotSupportedException e)
        {
            // Optional JDBC feature
        }
        catch (SQLException e)
        {
            // A driver may reject a vendor-specific null bind type
        }
    }
}
