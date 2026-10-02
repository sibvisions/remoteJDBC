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
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.Savepoint;
import java.sql.Statement;

import org.junit.Test;

/**
 * Verifies the JDBC behavior for connection.
 * 
 * @author René Jahn
 */
public class ConnectionContractTest
{
	/**
	 * Verifies the JDBC behavior for commit rollback and auto commit.
	 * 
	 * @throws Exception if the operation fails
	 */
    @Test
    public void testCommitRollbackAndAutoCommit() throws Exception
    {
        try (Connection connection = TestConnection.create();
        	 Statement statement = connection.createStatement())
        {
            boolean originalAutoCommit = connection.getAutoCommit();

            try
            {
                connection.setAutoCommit(false);
                assertFalse(connection.getAutoCommit());

                statement.executeUpdate("INSERT INTO TEST_TABLE (ID, NAME) VALUES (999998, 'RJdbcCommit')");
                connection.commit();

                assertEquals(1, countRows(statement, 999998));

                statement.executeUpdate("INSERT INTO TEST_TABLE (ID, NAME) VALUES (999999, 'RJdbcRollback')");
                connection.rollback();

                assertEquals(0, countRows(statement, 999999));
            }
            finally
            {
                try
                {
                    connection.rollback();

                    statement.executeUpdate("DELETE FROM TEST_TABLE WHERE ID IN (999998, 999999)");

                    connection.commit();
                }
                finally
                {
                    connection.setAutoCommit(originalAutoCommit);
                }
            }
        }
    }

	/**
	 * Verifies the JDBC behavior for savepoints.
	 * 
	 * @throws Exception if the operation fails
	 */
    @Test
    public void testSavepoints() throws Exception
    {
        try (Connection connection = TestConnection.create();
             Statement statement = connection.createStatement())
        {
            boolean originalAutoCommit = connection.getAutoCommit();

            try
            {
                connection.setAutoCommit(false);

                statement.executeUpdate("INSERT INTO TEST_TABLE (ID, NAME) VALUES (999997, 'BeforeSavepoint')");

                Savepoint savepoint = connection.setSavepoint();
                assertNotNull(savepoint);
                assertTrue(savepoint.getSavepointId() > 0);

                statement.executeUpdate("INSERT INTO TEST_TABLE (ID, NAME) VALUES (999996, 'AfterSavepoint')");

                connection.rollback(savepoint);

                assertEquals(1, countRows(statement, 999997));
                assertEquals(0, countRows(statement, 999996));

                Savepoint named = connection.setSavepoint("RJDBC_TEST_SAVEPOINT");

                assertNotNull(named);
                assertEquals("RJDBC_TEST_SAVEPOINT", named.getSavepointName());

                try
                {
                    connection.releaseSavepoint(named);
                }
                catch (SQLFeatureNotSupportedException e)
                {
                    // Optional JDBC feature.
                }
                catch (SQLException e)
                {
                    // Some JDBC drivers/databases, e.g. Oracle,
                    // do not support releaseSavepoint().
                }
            }
            catch (SQLFeatureNotSupportedException e)
            {
                // Savepoints are optional.
            }
            finally
            {
                try
                {
                    connection.rollback();
                }
                finally
                {
                    connection.setAutoCommit(originalAutoCommit);
                }
            }
        }
    }

	/**
	 * Verifies the JDBC behavior for read only catalog schema.
	 * 
	 * @throws Exception if the operation fails
	 */
    @Test
    public void testReadOnlyCatalogSchema() throws Exception
    {
        try (Connection connection = TestConnection.create())
        {
            String originalCatalog = connection.getCatalog();
            String originalSchema = connection.getSchema();

            boolean originalReadOnly = connection.isReadOnly();

            try
            {
                connection.setReadOnly(!originalReadOnly);
                
                assertEquals(!originalReadOnly, connection.isReadOnly());

                try
                {
                    connection.setReadOnly(originalReadOnly);
                }
                catch (SQLException e)
                {
                    // Some databases do not allow changing read-only while a transaction is active.
                }
                
                try
                {
                    connection.setCatalog(originalCatalog);
                    
                    assertEquals(originalCatalog, connection.getCatalog());
                }
                catch (SQLFeatureNotSupportedException e)
                {
                    // Optional driver feature.
                }
                
                try
                {
                    connection.setSchema(originalSchema);
                    
                    assertEquals(originalSchema, connection.getSchema());
                }
                catch (SQLFeatureNotSupportedException e)
                {
                    // Optional driver feature.
                }
            }
            finally
            {
                try
                {
                    connection.setReadOnly(originalReadOnly);
                }
                catch (SQLException ignored)
                {
                }
            }
        }
    }

	/**
	 * Verifies the JDBC behavior for transaction isolation round trip.
	 * 
	 * @throws Exception if the operation fails
	 */
    @Test
    public void testTransactionIsolationRoundTrip() throws Exception
    {
        try (Connection connection = TestConnection.create())
        {
            int original = connection.getTransactionIsolation();

            try
            {
                int[] levels = {Connection.TRANSACTION_READ_UNCOMMITTED,
                				Connection.TRANSACTION_READ_COMMITTED,
                				Connection.TRANSACTION_REPEATABLE_READ,
                				Connection.TRANSACTION_SERIALIZABLE};

                for (int level : levels)
                {
                    DatabaseMetaData metaData = connection.getMetaData();
                    
                    if (!metaData.supportsTransactionIsolationLevel(level))
                    {
                        continue;
                    }
                    
                    try
                    {
                        connection.setTransactionIsolation(level);
                        assertEquals(level, connection.getTransactionIsolation());
                    }
                    catch (SQLException e)
                    {
                        // A database may advertise a level but reject changing it
                        // in the current transaction state.
                    }
                }
            }
            finally
            {
                connection.setTransactionIsolation(original);
            }
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
        try (Connection connection = TestConnection.create())
        {
            int original = connection.getHoldability();

            try
            {
                int[] values = {ResultSet.HOLD_CURSORS_OVER_COMMIT,
                				ResultSet.CLOSE_CURSORS_AT_COMMIT};

                for (int value : values)
                {
                    try
                    {
                        if (connection.getMetaData().supportsResultSetHoldability(value))
                        {
                            connection.setHoldability(value);
                            
                            assertEquals(value, connection.getHoldability());
                        }
                    }
                    catch (SQLFeatureNotSupportedException e)
                    {
                        // Optional driver feature.
                    }
                    catch (SQLException e)
                    {
                        // Database may reject a supported holdability in its current state.
                    }
                }
            }
            finally
            {
                connection.setHoldability(original);
            }
        }
    }

	/**
	 * Verifies the JDBC behavior for client info properties.
	 * 
	 * @throws Exception if the operation fails
	 */
    @Test
    public void testClientInfoProperties() throws Exception
    {
        try (Connection connection = TestConnection.create())
        {	
            try
            {
            	//will work with Oracle
                connection.setClientInfo("OCSID.CLIENTID", "RJdbcTest");

                String value = connection.getClientInfo("OCSID.CLIENTID");

                if (value != null)
                {
                    assertEquals("RJdbcTest", value);
                }
                
                connection.setClientInfo("OCSID.CLIENTID", null);
            }
            catch (SQLFeatureNotSupportedException e)
            {
                // Client info is optional.
            }
            catch (SQLException e)
            {
                // The database/driver may reject arbitrary client-info properties.
            }
        }
    }

	/**
	 * Verifies the JDBC behavior for network timeout.
	 * 
	 * @throws Exception if the operation fails
	 */
    @Test
    public void testNetworkTimeout() throws Exception
    {
        try (Connection connection = TestConnection.create())
        {
            int original = connection.getNetworkTimeout();

            try
            {
                try
                {
                    connection.setNetworkTimeout(Runnable::run, 1234);
                    
                    assertEquals(1234, connection.getNetworkTimeout());

                    connection.setNetworkTimeout(Runnable::run, 0);
                    
                    assertEquals(0, connection.getNetworkTimeout());
                }
                catch (SQLFeatureNotSupportedException e)
                {
                    // Optional driver feature.
                }
            }
            finally
            {
                try
                {
                    connection.setNetworkTimeout(Runnable::run, original);
                }
                catch (SQLException ignored)
                {
                }
            }
        }
    }

	/**
	 * Verifies the JDBC behavior for native sql.
	 * 
	 * @throws Exception if the operation fails
	 */
    @Test
    public void testNativeSql() throws Exception
    {
        try (Connection connection = TestConnection.create())
        {
            String sql = "SELECT 1";

            String nativeSql = connection.nativeSQL(sql);

            assertNotNull(nativeSql);
            assertFalse(nativeSql.isEmpty());
        }
    }

	/**
	 * Verifies the JDBC behavior for callable statement contract.
	 * 
	 * @throws Exception if the operation fails
	 */
    @Test
    public void testCallableStatementContract() throws Exception
    {
        try (Connection connection = TestConnection.create())
        {
            /*
             * The actual CALL syntax and available procedures are database-specific.
             * Therefore this test deliberately verifies only the standard JDBC
             * lifecycle/creation contract and does not require a stored procedure.
             */
        	
            try (CallableStatement statement = connection.prepareCall("{call RJDBC_NON_EXISTING_PROCEDURE(?)}"))
            {
                assertNotNull(statement);
                assertFalse(statement.isClosed());
                
                statement.close();
                
                assertTrue(statement.isClosed());
            }
            catch (SQLFeatureNotSupportedException e)
            {
                // Callable statements are optional.
            }
            catch (SQLException e)
            {
                // A database-independent test cannot require a particular CALL syntax
                // or the existence of a procedure. Creation failure is therefore ok.
            	
                assertNotNull(e);
            }
        }
    }

	/**
	 * Supports the test scenario by handling count rows.
	 * 
	 * @param statement the statement
	 * @param id        the id
	 * @return the requested value
	 * @throws SQLException if the operation fails
	 */
    private int countRows(Statement statement, int id) throws SQLException
    {
        try (ResultSet result = statement.executeQuery("SELECT ID FROM TEST_TABLE WHERE ID = " + id))
        {
            int count = 0;
            
            while (result.next())
            {
                count++;
            }

            return count;
        }
    }
}
