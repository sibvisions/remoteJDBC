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

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Initializes the database objects and state required by the test class.
 */
public class DatabaseMetaDataTest
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
            dropTable(statement);
            
            statement.executeUpdate(
            "CREATE TABLE TEST_DATABASE_METADATA (" +
            "ID NUMBER(10) PRIMARY KEY, " +
            "NAME VARCHAR2(100))");
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
	 * Removes the test table when it exists so the next test can start from a clean
	 * state.
	 * 
	 * @param statement the statement
	 */
    private void dropTable(Statement statement)
    {
        try
        {
            statement.executeUpdate("DROP TABLE TEST_DATABASE_METADATA");
        }
        catch (Exception ignored)
        {
        }
    }

	/**
	 * Verifies the JDBC behavior for basic database meta data.
	 * 
	 * @throws Exception if the operation fails
	 */
    @Test
    public void testBasicDatabaseMetaData() throws Exception
    {
        try (Connection connection = TestConnection.create())
        {
            DatabaseMetaData metadata = connection.getMetaData();

            assertNotNull(metadata);
            assertNotNull(metadata.getDatabaseProductName());
            assertFalse(metadata.getDatabaseProductName().isEmpty());
            assertNotNull(metadata.getDatabaseProductVersion());
            assertNotNull(metadata.getDriverName());
            assertFalse(metadata.getDriverName().isEmpty());
            assertNotNull(metadata.getDriverVersion());
            assertFalse(metadata.getDriverVersion().isEmpty());

            assertTrue(metadata.getDriverMajorVersion() >= 0);
            assertTrue(metadata.getDriverMinorVersion() >= 0);
            assertNotNull(metadata.getIdentifierQuoteString());
            assertNotNull(metadata.getSQLKeywords());
        }
    }

	/**
	 * Verifies the JDBC behavior for tables metadata.
	 * 
	 * @throws Exception if the operation fails
	 */
    @Test
    public void testTablesMetadata() throws Exception
    {
        try (Connection connection = TestConnection.create())
        {
            DatabaseMetaData metadata = connection.getMetaData();

            try (ResultSet resultSet = metadata.getTables(null, null, "TEST_DATABASE_METADATA", new String[] {"TABLE"}))
            {
                assertTrue(resultSet.next());
                assertEquals("TEST_DATABASE_METADATA", resultSet.getString("TABLE_NAME"));
                assertEquals("TABLE", resultSet.getString("TABLE_TYPE"));
                assertFalse(resultSet.next());
            }
        }
    }

	/**
	 * Verifies the JDBC behavior for columns metadata.
	 * 
	 * @throws Exception if the operation fails
	 */
    @Test
    public void testColumnsMetadata() throws Exception
    {
        try (Connection connection = TestConnection.create())
        {
            DatabaseMetaData metadata = connection.getMetaData();

            try (ResultSet resultSet = metadata.getColumns(null, null, "TEST_DATABASE_METADATA", null))
            {
                assertTrue(resultSet.next());
                assertEquals("ID", resultSet.getString("COLUMN_NAME"));
                assertTrue(resultSet.getInt("DATA_TYPE") != 0);
                assertEquals("NUMBER", resultSet.getString("TYPE_NAME"));

                assertTrue(resultSet.next());
                assertEquals("NAME", resultSet.getString("COLUMN_NAME"));
                assertTrue(resultSet.getInt("DATA_TYPE") != 0);
                assertEquals("VARCHAR2", resultSet.getString("TYPE_NAME"));

                assertFalse(resultSet.next());
            }
        }
    }

	/**
	 * Verifies the JDBC behavior for primary keys metadata.
	 * 
	 * @throws Exception if the operation fails
	 */
    @Test
    public void testPrimaryKeysMetadata() throws Exception
    {
        try (Connection connection = TestConnection.create())
        {
            DatabaseMetaData metadata = connection.getMetaData();

            try (ResultSet resultSet = metadata.getPrimaryKeys(null, null, "TEST_DATABASE_METADATA"))
            {
                assertTrue(resultSet.next());
                assertEquals("ID", resultSet.getString("COLUMN_NAME"));
                assertNotNull(resultSet.getString("PK_NAME"));
                assertFalse(resultSet.next());
            }
        }
    }

	/**
	 * Verifies the JDBC behavior for basic metadata.
	 * 
	 * @throws Exception if the operation fails
	 */
    @Test
    public void testBasicMetadata() throws Exception
    {
        try (Connection oracle = TestConnection.createOracle();
        	 Connection remote = TestConnection.create())
        {
            DatabaseMetaData oracleMeta = oracle.getMetaData();
            DatabaseMetaData remoteMeta = remote.getMetaData();

            assertEquals(oracleMeta.getDatabaseProductName(), remoteMeta.getDatabaseProductName());
            assertEquals(oracleMeta.getDatabaseProductVersion(), remoteMeta.getDatabaseProductVersion());
            assertEquals(oracleMeta.getDriverName(), remoteMeta.getDriverName());
            assertEquals(oracleMeta.getDriverVersion(), remoteMeta.getDriverVersion());
            assertEquals(oracleMeta.getIdentifierQuoteString(), remoteMeta.getIdentifierQuoteString());
            assertEquals(oracleMeta.getCatalogSeparator(), remoteMeta.getCatalogSeparator());
            assertEquals(oracleMeta.getCatalogTerm(), remoteMeta.getCatalogTerm());
            assertEquals(oracleMeta.getSchemaTerm(), remoteMeta.getSchemaTerm());
        }
    }

	/**
	 * Verifies the JDBC behavior for capability metadata.
	 * 
	 * @throws Exception if the operation fails
	 */
    @Test
    public void testCapabilityMetadata() throws Exception
    {
        try (Connection oracle = TestConnection.createOracle();
             Connection remote = TestConnection.create())
        {
            DatabaseMetaData oracleMeta = oracle.getMetaData();
            DatabaseMetaData remoteMeta = remote.getMetaData();

            assertEquals(oracleMeta.supportsTransactions(), remoteMeta.supportsTransactions());
            assertEquals(oracleMeta.supportsBatchUpdates(), remoteMeta.supportsBatchUpdates());
            assertEquals(oracleMeta.supportsSavepoints(), remoteMeta.supportsSavepoints());
            assertEquals(oracleMeta.supportsStoredProcedures(), remoteMeta.supportsStoredProcedures());
            assertEquals(oracleMeta.supportsMultipleResultSets(), remoteMeta.supportsMultipleResultSets());
            assertEquals(oracleMeta.supportsGetGeneratedKeys(), remoteMeta.supportsGetGeneratedKeys());
            assertEquals(oracleMeta.supportsNamedParameters(), remoteMeta.supportsNamedParameters());

            assertTrue(remoteMeta.getMaxConnections() >= 0);
        }
    }

	/**
	 * Verifies the JDBC behavior for get tables.
	 * 
	 * @throws Exception if the operation fails
	 */
    @Test
    public void testGetTables() throws Exception
    {
        try (Connection oracle = TestConnection.createOracle();
             Connection remote = TestConnection.create())
        {
            DatabaseMetaData oracleMeta = oracle.getMetaData();
            DatabaseMetaData remoteMeta = remote.getMetaData();

            try (ResultSet oracleResult = oracleMeta.getTables(null, null, "%", new String[] {"TABLE"});
            	 ResultSet remoteResult = remoteMeta.getTables(null, null, "%", new String[] {"TABLE"}))
            {
                ResultSetMetaData oracleRsMeta = oracleResult.getMetaData();
                ResultSetMetaData remoteRsMeta = remoteResult.getMetaData();

                assertEquals(oracleRsMeta.getColumnCount(), remoteRsMeta.getColumnCount());

                for (int i = 1; i <= oracleRsMeta.getColumnCount(); i++)
                {
                    assertEquals(oracleRsMeta.getColumnName(i), remoteRsMeta.getColumnName(i));
                    assertEquals(oracleRsMeta.getColumnType(i), remoteRsMeta.getColumnType(i));
                    assertEquals(oracleRsMeta.getColumnTypeName(i), remoteRsMeta.getColumnTypeName(i));
                }
                
                while (oracleResult.next())
                {
                    assertTrue(remoteResult.next());

                    assertEquals(oracleResult.getString("TABLE_CAT"), remoteResult.getString("TABLE_CAT"));
                    assertEquals(oracleResult.getString("TABLE_SCHEM"), remoteResult.getString("TABLE_SCHEM"));
                    assertEquals(oracleResult.getString("TABLE_NAME"), remoteResult.getString("TABLE_NAME"));
                    assertEquals(oracleResult.getString("TABLE_TYPE"), remoteResult.getString("TABLE_TYPE"));
                }

                // Remote result must not contain additional rows.
                assertFalse(remoteResult.next());
            }
        }
    }

	/**
	 * Verifies the JDBC behavior for get columns.
	 * 
	 * @throws Exception if the operation fails
	 */
    @Test
    public void testGetColumns() throws Exception
    {
        try (Connection oracle = TestConnection.createOracle();
             Connection remote = TestConnection.create())
        {
            DatabaseMetaData oracleMeta = oracle.getMetaData();
            DatabaseMetaData remoteMeta = remote.getMetaData();

            try (ResultSet oracleResult = oracleMeta.getColumns(null, null, "%", "%");
                 ResultSet remoteResult = remoteMeta.getColumns(null, null, "%", "%"))
            {
                ResultSetMetaData oracleRsMeta = oracleResult.getMetaData();
                ResultSetMetaData remoteRsMeta = remoteResult.getMetaData();

                assertEquals(oracleRsMeta.getColumnCount(), remoteRsMeta.getColumnCount());

                for (int i = 1; i <= oracleRsMeta.getColumnCount(); i++)
                {
                    assertEquals(oracleRsMeta.getColumnName(i), remoteRsMeta.getColumnName(i));
                    assertEquals(oracleRsMeta.getColumnType(i), remoteRsMeta.getColumnType(i));
                    assertEquals(oracleRsMeta.getColumnTypeName(i), remoteRsMeta.getColumnTypeName(i));
                }
                
                while (oracleResult.next())
                {
                    assertTrue(remoteResult.next());

                    assertEquals(oracleResult.getString("TABLE_SCHEM"), remoteResult.getString("TABLE_SCHEM"));
                    assertEquals(oracleResult.getString("TABLE_NAME"), remoteResult.getString("TABLE_NAME"));
                    assertEquals(oracleResult.getString("COLUMN_NAME"), remoteResult.getString("COLUMN_NAME"));
                    assertEquals(oracleResult.getInt("DATA_TYPE"), remoteResult.getInt("DATA_TYPE"));
                    assertEquals(oracleResult.getString("TYPE_NAME"), remoteResult.getString("TYPE_NAME"));
                    assertEquals(oracleResult.getInt("ORDINAL_POSITION"), remoteResult.getInt("ORDINAL_POSITION"));
                }

                // Remote result must not contain additional rows.
                assertFalse(remoteResult.next());
            }
        }
    }

	/**
	 * Verifies the JDBC behavior for get primary keys.
	 * 
	 * @throws Exception if the operation fails
	 */
    @Test
    public void testGetPrimaryKeys() throws Exception
    {
        try (Connection oracle = TestConnection.createOracle();
        	 Connection remote = TestConnection.create())
        {
            DatabaseMetaData oracleMeta = oracle.getMetaData();
            DatabaseMetaData remoteMeta = remote.getMetaData();

            try (ResultSet oracleResult = oracleMeta.getPrimaryKeys(null, null, "%");
            	 ResultSet remoteResult = remoteMeta.getPrimaryKeys(null, null, "%"))
            {
                ResultSetMetaData oracleRsMeta = oracleResult.getMetaData();
                ResultSetMetaData remoteRsMeta = remoteResult.getMetaData();

                assertEquals(oracleRsMeta.getColumnCount(), remoteRsMeta.getColumnCount());

                for (int i = 1; i <= oracleRsMeta.getColumnCount(); i++)
                {
                    assertEquals(oracleRsMeta.getColumnName(i), remoteRsMeta.getColumnName(i));
                    assertEquals(oracleRsMeta.getColumnType(i), remoteRsMeta.getColumnType(i));
                    assertEquals(oracleRsMeta.getColumnTypeName(i), remoteRsMeta.getColumnTypeName(i));
                }
                
                while (oracleResult.next())
                {
                    assertTrue(remoteResult.next());

                    assertEquals(oracleResult.getString("TABLE_SCHEM"),remoteResult.getString("TABLE_SCHEM"));
                    assertEquals(oracleResult.getString("TABLE_NAME"), remoteResult.getString("TABLE_NAME"));
                    assertEquals(oracleResult.getString("COLUMN_NAME"), remoteResult.getString("COLUMN_NAME"));
                    assertEquals(oracleResult.getShort("KEY_SEQ"), remoteResult.getShort("KEY_SEQ"));
                    assertEquals(oracleResult.getString("PK_NAME"), remoteResult.getString("PK_NAME"));
                }
                
                assertFalse(remoteResult.next());
            }
        }
    }

	/**
	 * Verifies the JDBC behavior for get index info.
	 * 
	 * @throws Exception if the operation fails
	 */
    @Test
    public void testGetIndexInfo() throws Exception
    {
        try (Connection oracle = TestConnection.createOracle();
        	 Connection remote = TestConnection.create())
        {
            DatabaseMetaData oracleMeta = oracle.getMetaData();
            DatabaseMetaData remoteMeta = connection.getMetaData();

            String tableName = null;

            try (ResultSet tables = oracleMeta.getTables(null, null, "%", new String[] {"TABLE"}))
            {
                while (tables.next())
                {
                    String candidate = tables.getString("TABLE_NAME");

                    if (candidate == null)
                    {
                        continue;
                    }
                    
                    try (ResultSet test = oracleMeta.getIndexInfo(null, null, candidate, false, false))
                    {
                        // Oracle actually accepts the table for getIndexInfo().
                        tableName = candidate;
                        
                        break;
                    }
                    catch (SQLException ex)
                    {
                        // Skip Oracle-internal/inaccessible tables.
                    }
                }
            }
            
            assertNotNull("No table suitable for getIndexInfo() found", tableName);

            try (ResultSet oracleResult = oracleMeta.getIndexInfo(null, null, tableName, false, false);
            	 ResultSet remoteResult = remoteMeta.getIndexInfo(null, null, tableName, false, false))
            {
                while (oracleResult.next())
                {
                    assertTrue("rjdbc returns too few records", remoteResult.next());

                    assertEquals(oracleResult.getString("TABLE_SCHEM"), remoteResult.getString("TABLE_SCHEM"));
                    assertEquals(oracleResult.getString("TABLE_NAME"), remoteResult.getString("TABLE_NAME"));
                    assertEquals(oracleResult.getBoolean("NON_UNIQUE"), remoteResult.getBoolean("NON_UNIQUE"));
                    assertEquals(oracleResult.getString("INDEX_QUALIFIER"), remoteResult.getString("INDEX_QUALIFIER"));
                    assertEquals(oracleResult.getString("INDEX_NAME"), remoteResult.getString("INDEX_NAME"));
                    assertEquals(oracleResult.getShort("TYPE"), remoteResult.getShort("TYPE"));
                    assertEquals(oracleResult.getShort("ORDINAL_POSITION"), remoteResult.getShort("ORDINAL_POSITION"));
                    assertEquals(oracleResult.getString("COLUMN_NAME"), remoteResult.getString("COLUMN_NAME"));
                    assertEquals(oracleResult.getString("ASC_OR_DESC"), remoteResult.getString("ASC_OR_DESC"));
                    assertEquals(oracleResult.getInt("CARDINALITY"), remoteResult.getInt("CARDINALITY"));
                    assertEquals(oracleResult.getInt("PAGES"), remoteResult.getInt("PAGES"));
                    assertEquals(oracleResult.getString("FILTER_CONDITION"), remoteResult.getString("FILTER_CONDITION"));
                }
                
                assertFalse("rjdbc returns additional IndexInfo rows.", remoteResult.next());
            }
        }
    }

	/**
	 * Verifies the JDBC behavior for get imported keys.
	 * 
	 * @throws Exception if the operation fails
	 */
    @Test
    public void testGetImportedKeys() throws Exception
    {
        try (Connection oracle = TestConnection.createOracle();
        Connection remote = TestConnection.create())
        {
            DatabaseMetaData oracleMeta = oracle.getMetaData();
            DatabaseMetaData remoteMeta = remote.getMetaData();

            String tableName = null;

            try (ResultSet tables = oracleMeta.getTables(null, null, "%", new String[] {"TABLE"}))
            {
                while (tables.next())
                {
                    String candidate = tables.getString("TABLE_NAME");

                    if (candidate == null)
                    {
                        continue;
                    }
                    
                    try (ResultSet keys = oracleMeta.getImportedKeys(null, null, candidate))
                    {
                        if (keys.next())
                        {
                            tableName = candidate;
                            
                            break;
                        }
                    }
                    catch (SQLException ex)
                    {
                        // Skip inaccessible Oracle system tables.
                    }
                }
            }
            assertNotNull("No table with foreign keys found", tableName);

            try (ResultSet oracleResult = oracleMeta.getImportedKeys(null, null, tableName);
                 ResultSet remoteResult = remoteMeta.getImportedKeys(null, null, tableName))
            {
                while (oracleResult.next())
                {
                    assertTrue("rjdbc returns too few records", remoteResult.next());

                    assertEquals(oracleResult.getString("PKTABLE_CAT"), remoteResult.getString("PKTABLE_CAT"));
                    assertEquals(oracleResult.getString("PKTABLE_SCHEM"), remoteResult.getString("PKTABLE_SCHEM"));
                    assertEquals(oracleResult.getString("PKTABLE_NAME"), remoteResult.getString("PKTABLE_NAME"));
                    assertEquals(oracleResult.getString("PKCOLUMN_NAME"), remoteResult.getString("PKCOLUMN_NAME"));
                    assertEquals(oracleResult.getString("FKTABLE_CAT"), remoteResult.getString("FKTABLE_CAT"));
                    assertEquals(oracleResult.getString("FKTABLE_SCHEM"), remoteResult.getString("FKTABLE_SCHEM"));
                    assertEquals(oracleResult.getString("FKTABLE_NAME"), remoteResult.getString("FKTABLE_NAME"));
                    assertEquals(oracleResult.getString("FKCOLUMN_NAME"), remoteResult.getString("FKCOLUMN_NAME"));
                    assertEquals(oracleResult.getShort("KEY_SEQ"), remoteResult.getShort("KEY_SEQ"));
                    assertEquals(oracleResult.getShort("UPDATE_RULE"), remoteResult.getShort("UPDATE_RULE"));
                    assertEquals(oracleResult.getShort("DELETE_RULE"), remoteResult.getShort("DELETE_RULE"));
                    assertEquals(oracleResult.getString("FK_NAME"), remoteResult.getString("FK_NAME"));
                    assertEquals(oracleResult.getString("PK_NAME"), remoteResult.getString("PK_NAME"));
                    assertEquals(oracleResult.getShort("DEFERRABILITY"), remoteResult.getShort("DEFERRABILITY"));
                }
                
                assertFalse("rjdbc returns additional Imported-Key records",remoteResult.next());
            }
        }
    }

	/**
	 * Verifies the JDBC behavior for get exported keys.
	 * 
	 * @throws Exception if the operation fails
	 */
    @Test
    public void testGetExportedKeys() throws Exception
    {
        try (Connection oracle = TestConnection.createOracle();
             Connection remote = TestConnection.create())
        {
            DatabaseMetaData oracleMeta = oracle.getMetaData();
            DatabaseMetaData remoteMeta = remote.getMetaData();

            String tableName = null;

            try (ResultSet tables = oracleMeta.getTables(null, null, "%", new String[] {"TABLE"}))
            {
                while (tables.next())
                {
                    String candidate = tables.getString("TABLE_NAME");

                    if (candidate == null)
                    {
                        continue;
                    }
                    
                    try (ResultSet keys = oracleMeta.getExportedKeys(null, null, candidate))
                    {
                        if (keys.next())
                        {
                            tableName = candidate;
                            
                            break;
                        }
                    }
                    catch (SQLException ex)
                    {
                        // Skip inaccessible Oracle system tables.
                    }
                }
            }
            assertNotNull("No table with an exported foreign key found.", tableName);

            try (ResultSet oracleResult = oracleMeta.getExportedKeys(null, null, tableName);
            	 ResultSet remoteResult = remoteMeta.getExportedKeys(null, null, tableName))
            {
                while (oracleResult.next())
                {
                    assertTrue("rjdbc returns too few records", remoteResult.next());

                    assertEquals(oracleResult.getString("PKTABLE_CAT"), remoteResult.getString("PKTABLE_CAT"));
                    assertEquals(oracleResult.getString("PKTABLE_SCHEM"), remoteResult.getString("PKTABLE_SCHEM"));
                    assertEquals(oracleResult.getString("PKTABLE_NAME"), remoteResult.getString("PKTABLE_NAME"));
                    assertEquals(oracleResult.getString("PKCOLUMN_NAME"), remoteResult.getString("PKCOLUMN_NAME"));
                    assertEquals(oracleResult.getString("FKTABLE_CAT"), remoteResult.getString("FKTABLE_CAT"));
                    assertEquals(oracleResult.getString("FKTABLE_SCHEM"), remoteResult.getString("FKTABLE_SCHEM"));
                    assertEquals(oracleResult.getString("FKTABLE_NAME"), remoteResult.getString("FKTABLE_NAME"));
                    assertEquals(oracleResult.getString("FKCOLUMN_NAME"), remoteResult.getString("FKCOLUMN_NAME"));
                    assertEquals(oracleResult.getShort("KEY_SEQ"), remoteResult.getShort("KEY_SEQ"));
                    assertEquals(oracleResult.getShort("UPDATE_RULE"), remoteResult.getShort("UPDATE_RULE"));
                    assertEquals(oracleResult.getShort("DELETE_RULE"), remoteResult.getShort("DELETE_RULE"));
                    assertEquals(oracleResult.getString("FK_NAME"), remoteResult.getString("FK_NAME"));
                    assertEquals(oracleResult.getString("PK_NAME"), remoteResult.getString("PK_NAME"));
                    assertEquals(oracleResult.getShort("DEFERRABILITY"), remoteResult.getShort("DEFERRABILITY"));
                }
                
                assertFalse("rjdbc returns additional Exported-Key records", remoteResult.next());
            }
        }
    }

	/**
	 * Verifies the JDBC behavior for get best row identifier.
	 * 
	 * @throws Exception if the operation fails
	 */
    @Test
    public void testGetBestRowIdentifier() throws Exception
    {
        try (Connection oracle = TestConnection.createOracle();
             Connection remote = TestConnection.create())
        {
            DatabaseMetaData oracleMeta = oracle.getMetaData();
            DatabaseMetaData remoteMeta = remote.getMetaData();

            String tableName = null;

            try (ResultSet tables = oracleMeta.getTables(null, null, "%", new String[] {"TABLE"}))
            {
                while (tables.next())
                {
                    String candidate = tables.getString("TABLE_NAME");

                    if (candidate == null)
                    {
                        continue;
                    }
                    
                    try (ResultSet rows = oracleMeta.getBestRowIdentifier(null, null, candidate, DatabaseMetaData.bestRowSession, true))
                    {
                        if (rows.next())
                        {
                            tableName = candidate;
                            
                            break;
                        }
                    }
                    catch (SQLException ex)
                    {
                        // Skip unsupported or inaccessible Oracle system tables.
                    }
                }
            }
            
            if (tableName == null)
            {
                // Oracle returns no BestRowIdentifier -> stop here
                return;
            }
            
            try (ResultSet oracleResult = oracleMeta.getBestRowIdentifier(null, null, tableName, DatabaseMetaData.bestRowSession, true);
                 ResultSet remoteResult = remoteMeta.getBestRowIdentifier(null, null, tableName, DatabaseMetaData.bestRowSession, true))
            {
                while (oracleResult.next())
                {
                    assertTrue("rJdbc returns too few records", remoteResult.next());

                    assertEquals(oracleResult.getShort("SCOPE"), remoteResult.getShort("SCOPE"));
                    assertEquals(oracleResult.getString("COLUMN_NAME"), remoteResult.getString("COLUMN_NAME"));
                    assertEquals(oracleResult.getShort("DATA_TYPE"), remoteResult.getShort("DATA_TYPE"));
                    assertEquals(oracleResult.getString("TYPE_NAME"), remoteResult.getString("TYPE_NAME"));
                    assertEquals(oracleResult.getInt("COLUMN_SIZE"), remoteResult.getInt("COLUMN_SIZE"));
                    assertEquals(oracleResult.getInt("BUFFER_LENGTH"), remoteResult.getInt("BUFFER_LENGTH"));
                    assertEquals(oracleResult.getInt("DECIMAL_DIGITS"), remoteResult.getInt("DECIMAL_DIGITS"));
                    assertEquals(oracleResult.getShort("PSEUDO_COLUMN"), remoteResult.getShort("PSEUDO_COLUMN"));
                }
                assertFalse("rjdbc returns additional BestRowIdentifier records", remoteResult.next());
            }
        }
    }

	/**
	 * Verifies the JDBC behavior for get version columns.
	 * 
	 * @throws Exception if the operation fails
	 */
    @Test
    public void testGetVersionColumns() throws Exception
    {
        try (Connection oracle = TestConnection.createOracle();
        	 Connection remote = TestConnection.create())
        {
            DatabaseMetaData oracleMeta = oracle.getMetaData();
            DatabaseMetaData remoteMeta = remote.getMetaData();

            String tableName = null;

            try (ResultSet tables = oracleMeta.getTables(null, null, "%", new String[] {"TABLE"}))
            {
                while (tables.next())
                {
                    String candidate = tables.getString("TABLE_NAME");

                    if (candidate == null)
                    {
                        continue;
                    }
                    
                    try (ResultSet rows = oracleMeta.getVersionColumns(null, null, candidate))
                    {
                        if (rows.next())
                        {
                            tableName = candidate;
                            
                            break;
                        }
                    }
                    catch (SQLException ex)
                    {
                        // Skip inaccessible Oracle system tables.
                    }
                }
            }
            
            if (tableName == null)
            {
                // Oracle returns no version columns for accessible tables.
                return;
            }
            
            try (ResultSet oracleResult = oracleMeta.getVersionColumns(null, null, tableName);
                 ResultSet remoteResult = remoteMeta.getVersionColumns(null, null, tableName))
            {
                while (oracleResult.next())
                {
                    assertTrue("rjdbc returns too few records",remoteResult.next());

                    assertEquals(oracleResult.getShort("SCOPE"), remoteResult.getShort("SCOPE"));
                    assertEquals(oracleResult.getString("COLUMN_NAME"), remoteResult.getString("COLUMN_NAME"));
                    assertEquals(oracleResult.getShort("DATA_TYPE"), remoteResult.getShort("DATA_TYPE"));
                    assertEquals(oracleResult.getString("TYPE_NAME"), remoteResult.getString("TYPE_NAME"));
                    assertEquals(oracleResult.getInt("COLUMN_SIZE"), remoteResult.getInt("COLUMN_SIZE"));
                    assertEquals(oracleResult.getInt("BUFFER_LENGTH"), remoteResult.getInt("BUFFER_LENGTH"));
                    assertEquals(oracleResult.getInt("DECIMAL_DIGITS"), remoteResult.getInt("DECIMAL_DIGITS"));
                    assertEquals(oracleResult.getShort("PSEUDO_COLUMN"), remoteResult.getShort("PSEUDO_COLUMN"));
                }
                
                assertFalse("rjdbc returns additional VersionColumn records", remoteResult.next());
            }
        }
    }

	/**
	 * Verifies the JDBC behavior for get type info.
	 * 
	 * @throws Exception if the operation fails
	 */
    @Test
    public void testGetTypeInfo() throws Exception
    {
        try (Connection oracle = TestConnection.createOracle();
             Connection remote = TestConnection.create())
        {
            DatabaseMetaData oracleMeta = oracle.getMetaData();
            DatabaseMetaData remoteMeta = remote.getMetaData();

            try (ResultSet oracleResult = oracleMeta.getTypeInfo();
            	 ResultSet remoteResult = remoteMeta.getTypeInfo())
            {
                while (oracleResult.next())
                {
                    assertTrue("rjdbc returns too few TypeInfo records", remoteResult.next());

                    assertEquals(oracleResult.getString("TYPE_NAME"), remoteResult.getString("TYPE_NAME"));
                    assertEquals(oracleResult.getShort("DATA_TYPE"), remoteResult.getShort("DATA_TYPE"));
                    assertEquals(oracleResult.getInt("PRECISION"), remoteResult.getInt("PRECISION"));
                    assertEquals(oracleResult.getString("LITERAL_PREFIX"), remoteResult.getString("LITERAL_PREFIX"));
                    assertEquals(oracleResult.getString("LITERAL_SUFFIX"), remoteResult.getString("LITERAL_SUFFIX"));
                    assertEquals(oracleResult.getString("CREATE_PARAMS"), remoteResult.getString("CREATE_PARAMS"));
                    assertEquals(oracleResult.getShort("NULLABLE"), remoteResult.getShort("NULLABLE"));
                    assertEquals(oracleResult.getBoolean("CASE_SENSITIVE"), remoteResult.getBoolean("CASE_SENSITIVE"));
                    assertEquals(oracleResult.getShort("SEARCHABLE"), remoteResult.getShort("SEARCHABLE"));
                    assertEquals(oracleResult.getBoolean("UNSIGNED_ATTRIBUTE"), remoteResult.getBoolean("UNSIGNED_ATTRIBUTE"));
                    assertEquals(oracleResult.getBoolean("FIXED_PREC_SCALE"), remoteResult.getBoolean("FIXED_PREC_SCALE"));
                    assertEquals(oracleResult.getBoolean("AUTO_INCREMENT"), remoteResult.getBoolean("AUTO_INCREMENT"));
                    assertEquals(oracleResult.getString("LOCAL_TYPE_NAME"), remoteResult.getString("LOCAL_TYPE_NAME"));
                    assertEquals(oracleResult.getShort("MINIMUM_SCALE"), remoteResult.getShort("MINIMUM_SCALE"));
                    assertEquals(oracleResult.getShort("MAXIMUM_SCALE"), remoteResult.getShort("MAXIMUM_SCALE"));
                }
                
                assertFalse("rjdbc returns additional TypeInfo records", remoteResult.next());
            }
        }
    }

	/**
	 * Verifies the JDBC behavior for get procedures.
	 * 
	 * @throws Exception if the operation fails
	 */
    @Test
    public void testGetProcedures() throws Exception
    {
        try (Connection oracle = TestConnection.createOracle();
             Connection remote = TestConnection.create())
        {
            DatabaseMetaData oracleMeta = oracle.getMetaData();
            DatabaseMetaData remoteMeta = remote.getMetaData();

            String procedureName = null;

            try (ResultSet procedures = oracleMeta.getProcedures(null, null, "%"))
            {
                if (procedures.next())
                {
                    procedureName = procedures.getString("PROCEDURE_NAME");
                }
            }
            
            if (procedureName == null)
            {
                return;
            }
            
            try (ResultSet oracleResult = oracleMeta.getProcedures(null, null, procedureName);
                 ResultSet remoteResult = remoteMeta.getProcedures(null, null, procedureName))
            {
                while (oracleResult.next())
                {
                    assertTrue("rjdbc returns too few Procedure records", remoteResult.next());

                    assertEquals(oracleResult.getString("PROCEDURE_CAT"), remoteResult.getString("PROCEDURE_CAT"));
                    assertEquals(oracleResult.getString("PROCEDURE_SCHEM"), remoteResult.getString("PROCEDURE_SCHEM"));
                    assertEquals(oracleResult.getString("PROCEDURE_NAME"), remoteResult.getString("PROCEDURE_NAME"));
                    assertEquals(oracleResult.getString("REMARKS"), remoteResult.getString("REMARKS"));
                    assertEquals(oracleResult.getShort("PROCEDURE_TYPE"), remoteResult.getShort("PROCEDURE_TYPE"));
                    assertEquals(oracleResult.getString("SPECIFIC_NAME"), remoteResult.getString("SPECIFIC_NAME"));
                }
                
                assertFalse("rjdbc returns additional Procedure records", remoteResult.next());
            }
        }
    }
}
