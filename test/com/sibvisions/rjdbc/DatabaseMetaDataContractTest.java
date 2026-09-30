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
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import org.junit.Test;

/**
 * Verifies the JDBC behavior for database metadata.
 * 
 * @author René Jahn
 */
public class DatabaseMetaDataContractTest
{
	/**
	 * Verifies the JDBC behavior for basic properties.
	 * 
	 * @throws Exception if the operation fails
	 */
    @Test
    public void testBasicProperties() throws Exception
    {
        try (Connection connection = TestConnection.create())
        {
            DatabaseMetaData meta = connection.getMetaData();

            assertNotNull(meta.getDatabaseProductName());
            assertNotNull(meta.getDatabaseProductVersion());
            assertNotNull(meta.getDriverName());
            assertNotNull(meta.getDriverVersion());

            assertNotNull(meta.getURL());
            assertNotNull(meta.getUserName());

            assertNotNull(meta.getIdentifierQuoteString());
            assertNotNull(meta.getCatalogSeparator());
            assertNotNull(meta.getCatalogTerm());
            assertNotNull(meta.getSchemaTerm());

            assertNotNull(meta.getSearchStringEscape());
            assertNotNull(meta.getExtraNameCharacters());
        }
    }

	/**
	 * Verifies the JDBC behavior for version information.
	 * 
	 * @throws Exception if the operation fails
	 */
    @Test
    public void testVersionInformation() throws Exception
    {
        try (Connection connection = TestConnection.create())
        {
            DatabaseMetaData meta = connection.getMetaData();

            assertTrue(meta.getDatabaseMajorVersion() >= 0);
            assertTrue(meta.getDatabaseMinorVersion() >= 0);
            assertTrue(meta.getJDBCMajorVersion() >= 0);
            assertTrue(meta.getJDBCMinorVersion() >= 0);

            assertNotNull(meta.getSQLStateType());
        }
    }

	/**
	 * Verifies the JDBC behavior for function information.
	 * 
	 * @throws Exception if the operation fails
	 */
    @Test
    public void testFunctionInformation() throws Exception
    {
        try (Connection connection = TestConnection.create())
        {
            DatabaseMetaData meta = connection.getMetaData();

            assertNotNull(meta.getSQLKeywords());
            assertNotNull(meta.getNumericFunctions());
            assertNotNull(meta.getStringFunctions());
            assertNotNull(meta.getSystemFunctions());
            assertNotNull(meta.getTimeDateFunctions());
        }
    }

	/**
	 * Verifies the JDBC behavior for type info.
	 * 
	 * @throws Exception if the operation fails
	 */
    @Test
    public void testTypeInfo() throws Exception
    {
        try (Connection connection = TestConnection.create())
        {
            DatabaseMetaData meta = connection.getMetaData();

            try (ResultSet result = meta.getTypeInfo())
            {
                assertNotNull(result);
            }
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
            DatabaseMetaData meta = connection.getMetaData();

            try (ResultSet result = meta.getTables(null, null, null, null))
            {
                assertNotNull(result);
                assertNotNull(result.getMetaData());
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
            DatabaseMetaData meta = connection.getMetaData();

            try (ResultSet result = meta.getColumns(null, null, null, null))
            {
                assertNotNull(result);
                assertNotNull(result.getMetaData());
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
            DatabaseMetaData meta = connection.getMetaData();

            try (ResultSet result = meta.getPrimaryKeys(null, null, null))
            {
                assertNotNull(result);
                assertNotNull(result.getMetaData());
            }
        }
    }

	/**
	 * Verifies the JDBC behavior for imported keys metadata.
	 * 
	 * @throws Exception if the operation fails
	 */
    @Test
    public void testImportedKeysMetadata() throws Exception
    {
        try (Connection connection = TestConnection.create())
        {
            DatabaseMetaData meta = connection.getMetaData();

            try (ResultSet result = meta.getImportedKeys(null, null, null))
            {
                assertNotNull(result);
                assertNotNull(result.getMetaData());
            }
        }
    }

	/**
	 * Verifies the JDBC behavior for exported keys metadata.
	 * 
	 * @throws Exception if the operation fails
	 */
    @Test
    public void testExportedKeysMetadata() throws Exception
    {
        try (Connection connection = TestConnection.create())
        {
            DatabaseMetaData meta = connection.getMetaData();

            try (ResultSet result = meta.getExportedKeys(null, null, null))
            {
                assertNotNull(result);
                assertNotNull(result.getMetaData());
            }
        }
    }

	/**
	 * Verifies the JDBC behavior for index metadata.
	 * 
	 * @throws Exception if the operation fails
	 */
    @Test
    public void testIndexMetadata() throws Exception
    {
        try (Connection connection = TestConnection.create())
        {
            DatabaseMetaData meta = connection.getMetaData();

            String tableName = null;

            try (ResultSet tables = meta.getTables(null, null, "%", new String[] {"TABLE"}))
            {
                while (tables.next())
                {
                    String candidate = tables.getString("TABLE_NAME");

                    if (candidate == null)
                    {
                        continue;
                    }
                    
                    try (ResultSet indexes = meta.getIndexInfo(null, null, candidate, false, false))
                    {
                        tableName = candidate;
                        
                        break;
                    }
                    catch (SQLException ex)
                    {
                        // The table is rejected by the DBMS for getIndexInfo()
                        // -> try the next table.
                    }
                }
            }
            if (tableName == null)
            {
                return;
            }
            
            try (ResultSet result = meta.getIndexInfo(null, null, tableName, false, false))
            {
                assertNotNull(result);
                assertNotNull(result.getMetaData());

                assertEquals(
                "TABLE_CAT",
                result.getMetaData().getColumnName(1));

                assertEquals(
                "TABLE_SCHEM",
                result.getMetaData().getColumnName(2));

                assertEquals(
                "TABLE_NAME",
                result.getMetaData().getColumnName(3));

                assertEquals(
                "NON_UNIQUE",
                result.getMetaData().getColumnName(4));

                assertEquals(
                "INDEX_QUALIFIER",
                result.getMetaData().getColumnName(5));

                assertEquals(
                "INDEX_NAME",
                result.getMetaData().getColumnName(6));

                assertEquals(
                "TYPE",
                result.getMetaData().getColumnName(7));

                assertEquals(
                "ORDINAL_POSITION",
                result.getMetaData().getColumnName(8));

                assertEquals(
                "COLUMN_NAME",
                result.getMetaData().getColumnName(9));

                assertEquals(
                "ASC_OR_DESC",
                result.getMetaData().getColumnName(10));

                assertEquals(
                "CARDINALITY",
                result.getMetaData().getColumnName(11));

                assertEquals(
                "PAGES",
                result.getMetaData().getColumnName(12));

                assertEquals(
                "FILTER_CONDITION",
                result.getMetaData().getColumnName(13));
            }
        }
    }

	/**
	 * Verifies the JDBC behavior for best row identifier metadata.
	 * 
	 * @throws Exception if the operation fails
	 */
    @Test
    public void testBestRowIdentifierMetadata() throws Exception
    {
        try (Connection connection = TestConnection.create())
        {
            DatabaseMetaData meta = connection.getMetaData();

            try (ResultSet result = meta.getBestRowIdentifier(null, null, null,DatabaseMetaData.bestRowSession,true))
            {
                assertNotNull(result);
                assertNotNull(result.getMetaData());
            }
        }
    }

	/**
	 * Verifies the JDBC behavior for version columns metadata.
	 * 
	 * @throws Exception if the operation fails
	 */
    @Test
    public void testVersionColumnsMetadata() throws Exception
    {
        try (Connection connection = TestConnection.create())
        {
            DatabaseMetaData meta = connection.getMetaData();

            try (ResultSet result = meta.getVersionColumns(null, null, null))
            {
                assertNotNull(result);
                assertNotNull(result.getMetaData());
            }
        }
    }

	/**
	 * Verifies the JDBC behavior for procedures metadata.
	 * 
	 * @throws Exception if the operation fails
	 */
    @Test
    public void testProceduresMetadata() throws Exception
    {
        try (Connection connection = TestConnection.create())
        {
            DatabaseMetaData meta = connection.getMetaData();

            try (ResultSet result = meta.getProcedures(null, null, null))
            {
                assertNotNull(result);
                assertNotNull(result.getMetaData());
            }
        }
    }

	/**
	 * Verifies the JDBC behavior for procedure columns metadata.
	 * 
	 * @throws Exception if the operation fails
	 */
    @Test
    public void testProcedureColumnsMetadata() throws Exception
    {
        try (Connection connection = TestConnection.create())
        {
            DatabaseMetaData meta = connection.getMetaData();

            try (ResultSet result = meta.getProcedureColumns(null, null, null, null))
            {
                assertNotNull(result);
                assertNotNull(result.getMetaData());
            }
        }
    }

	/**
	 * Verifies the JDBC behavior for functions metadata.
	 * 
	 * @throws Exception if the operation fails
	 */
    @Test
    public void testFunctionsMetadata() throws Exception
    {
        try (Connection connection = TestConnection.create())
        {
            DatabaseMetaData meta = connection.getMetaData();

            try (ResultSet result = meta.getFunctions(null, null, null))
            {
                assertNotNull(result);
                assertNotNull(result.getMetaData());
            }
        }
    }

	/**
	 * Verifies the JDBC behavior for function columns metadata.
	 * 
	 * @throws Exception if the operation fails
	 */
    @Test
    public void testFunctionColumnsMetadata() throws Exception
    {
        try (Connection connection = TestConnection.create())
        {
            DatabaseMetaData meta = connection.getMetaData();

            try (ResultSet result = meta.getFunctionColumns(null, null, null, null))
            {
                assertNotNull(result);
                assertNotNull(result.getMetaData());
            }
        }
    }

	/**
	 * Verifies the JDBC behavior for schemas metadata.
	 * 
	 * @throws Exception if the operation fails
	 */
    @Test
    public void testSchemasMetadata() throws Exception
    {
        try (Connection connection = TestConnection.create())
        {
            DatabaseMetaData meta = connection.getMetaData();

            try (ResultSet result = meta.getSchemas())
            {
                assertNotNull(result);
                assertNotNull(result.getMetaData());
            }
        }
    }

	/**
	 * Verifies the JDBC behavior for catalogs metadata.
	 * 
	 * @throws Exception if the operation fails
	 */
    @Test
    public void testCatalogsMetadata() throws Exception
    {
        try (Connection connection = TestConnection.create())
        {
            DatabaseMetaData meta = connection.getMetaData();

            try (ResultSet result = meta.getCatalogs())
            {
                assertNotNull(result);
                assertNotNull(result.getMetaData());
            }
        }
    }

	/**
	 * Verifies the JDBC behavior for table types metadata.
	 * 
	 * @throws Exception if the operation fails
	 */
    @Test
    public void testTableTypesMetadata() throws Exception
    {
        try (Connection connection = TestConnection.create())
        {
            DatabaseMetaData meta = connection.getMetaData();

            try (ResultSet result = meta.getTableTypes())
            {
                assertNotNull(result);
                assertNotNull(result.getMetaData());
            }
        }
    }

	/**
	 * Verifies the JDBC behavior for attributes metadata.
	 * 
	 * @throws Exception if the operation fails
	 */
    @Test
    public void testAttributesMetadata() throws Exception
    {
        try (Connection connection = TestConnection.create())
        {
            DatabaseMetaData meta = connection.getMetaData();

            try
            {
                ResultSet rs = meta.getAttributes(null, null, null, null);

                assertNotNull(rs);

                ResultSetMetaData rsMeta = rs.getMetaData();
                assertEquals(19, rsMeta.getColumnCount());

                assertEquals("TYPE_CAT", rsMeta.getColumnName(1));
                assertEquals("TYPE_SCHEM", rsMeta.getColumnName(2));
                assertEquals("TYPE_NAME", rsMeta.getColumnName(3));
                assertEquals("ATTR_NAME", rsMeta.getColumnName(4));
                assertEquals("DATA_TYPE", rsMeta.getColumnName(5));
                assertEquals("ATTR_TYPE_NAME", rsMeta.getColumnName(6));
                assertEquals("ATTR_SIZE", rsMeta.getColumnName(7));
                assertEquals("DECIMAL_DIGITS", rsMeta.getColumnName(8));
                assertEquals("NUM_PREC_RADIX", rsMeta.getColumnName(9));
                assertEquals("NULLABLE", rsMeta.getColumnName(10));
                assertEquals("REMARKS", rsMeta.getColumnName(11));
                assertEquals("ATTR_DEF", rsMeta.getColumnName(12));
                assertEquals("SQL_DATA_TYPE", rsMeta.getColumnName(13));
                assertEquals("SQL_DATETIME_SUB", rsMeta.getColumnName(14));
                assertEquals("CHAR_OCTET_LENGTH", rsMeta.getColumnName(15));
                assertEquals("ORDINAL_POSITION", rsMeta.getColumnName(16));
                assertEquals("IS_NULLABLE", rsMeta.getColumnName(17));
                assertEquals("SCOPE_CATALOG", rsMeta.getColumnName(18));
                assertEquals("SCOPE_SCHEMA", rsMeta.getColumnName(19));

                rs.close();
            }
            catch (SQLException e)
            {
                // Oracle does not support getAttributes().
                // Therefore, "not supported" is a valid result.
                assertTrue(e.getMessage() == null
                || e.getMessage().toLowerCase().contains("nicht unterstützt")
                || e.getMessage().toLowerCase().contains("unsupported"));
            }
        }
    }

	/**
	 * Verifies the JDBC behavior for super types metadata.
	 * 
	 * @throws Exception if the operation fails
	 */
    @Test
    public void testSuperTypesMetadata() throws Exception
    {
        try (Connection connection = TestConnection.create())
        {
            DatabaseMetaData meta = connection.getMetaData();

            try
            {
                ResultSet rs = meta.getSuperTypes(null, null, null);

                assertNotNull(rs);

                ResultSetMetaData md = rs.getMetaData();
                assertEquals(6, md.getColumnCount());

                assertEquals("TYPE_CAT", md.getColumnName(1));
                assertEquals("TYPE_SCHEM", md.getColumnName(2));
                assertEquals("TYPE_NAME", md.getColumnName(3));
                assertEquals("SUPERTYPE_CAT", md.getColumnName(4));
                assertEquals("SUPERTYPE_SCHEM", md.getColumnName(5));
                assertEquals("SUPERTYPE_NAME", md.getColumnName(6));

                rs.close();
            }
            catch (SQLException e)
            {
                assertTrue(e.getMessage() == null
                || e.getMessage().toLowerCase().contains("nicht unterstützt")
                || e.getMessage().toLowerCase().contains("unsupported"));
            }
        }
    }

	/**
	 * Verifies the JDBC behavior for super tables metadata.
	 * 
	 * @throws Exception if the operation fails
	 */
    @Test
    public void testSuperTablesMetadata() throws Exception
    {
        try (Connection connection = TestConnection.create())
        {
            DatabaseMetaData meta = connection.getMetaData();

            try
            {
                try (ResultSet rs = meta.getSuperTables(null, null, null))
                {
	                assertNotNull(rs);
	
	                ResultSetMetaData md = rs.getMetaData();
	                assertEquals(10, md.getColumnCount());
	
	                assertEquals("TYPE_CAT", md.getColumnName(1));
	                assertEquals("TYPE_SCHEM", md.getColumnName(2));
	                assertEquals("TYPE_NAME", md.getColumnName(3));
	                assertEquals("TABLE_CAT", md.getColumnName(4));
	                assertEquals("TABLE_SCHEM", md.getColumnName(5));
	                assertEquals("TABLE_NAME", md.getColumnName(6));
	                assertEquals("SUPERTABLE_NAME", md.getColumnName(7));
                }
            }
            catch (SQLException e)
            {
                assertTrue(e.getMessage() == null
                || e.getMessage().toLowerCase().contains("nicht unterstützt")
                || e.getMessage().toLowerCase().contains("unsupported"));
            }
        }
    }

	/**
	 * Verifies the JDBC behavior for ud ts metadata.
	 * 
	 * @throws Exception if the operation fails
	 */
    @Test
    public void testUDTsMetadata() throws Exception
    {
        try (Connection connection = TestConnection.create())
        {
            DatabaseMetaData meta = connection.getMetaData();

            try (ResultSet result = meta.getUDTs(null, null, null, null))
            {
                assertNotNull(result);
                assertNotNull(result.getMetaData());
            }
        }
    }

	/**
	 * Verifies the JDBC behavior for client info properties metadata.
	 * 
	 * @throws Exception if the operation fails
	 */
    @Test
    public void testClientInfoPropertiesMetadata() throws Exception
    {
        try (Connection connection = TestConnection.create())
        {
            DatabaseMetaData meta = connection.getMetaData();

            try (ResultSet result = meta.getClientInfoProperties())
            {
                assertNotNull(result);
                assertNotNull(result.getMetaData());
            }
        }
    }

	/**
	 * Verifies the JDBC behavior for pseudo columns metadata.
	 * 
	 * @throws Exception if the operation fails
	 */
    @Test
    public void testPseudoColumnsMetadata() throws Exception
    {
        try (Connection connection = TestConnection.create())
        {
            DatabaseMetaData meta = connection.getMetaData();

            try (ResultSet result = meta.getPseudoColumns(null, null, null, null))
            {
                assertNotNull(result);
                assertNotNull(result.getMetaData());
            }
        }
    }

	/**
	 * Verifies the JDBC behavior for generated keys metadata.
	 * 
	 * @throws Exception if the operation fails
	 */
    @Test
    public void testGeneratedKeysMetadata() throws Exception
    {
        try (Connection connection = TestConnection.create())
        {
            DatabaseMetaData meta = connection.getMetaData();

            assertNotNull(meta.getResultSetHoldability());
            assertTrue(meta.getResultSetHoldability() >= 0);

            assertNotNull(meta.getMaxConnections());
            assertTrue(meta.getMaxConnections() >= 0);
        }
    }

	/**
	 * Verifies the JDBC behavior for capability methods.
	 * 
	 * @throws Exception if the operation fails
	 */
    @Test
    public void testCapabilityMethods() throws Exception
    {
        try (Connection connection = TestConnection.create())
        {
            DatabaseMetaData meta = connection.getMetaData();

            meta.allProceduresAreCallable();
            meta.allTablesAreSelectable();
            meta.isReadOnly();
            meta.nullsAreSortedHigh();
            meta.nullsAreSortedLow();
            meta.nullsAreSortedAtStart();
            meta.nullsAreSortedAtEnd();

            meta.usesLocalFiles();
            meta.usesLocalFilePerTable();
            meta.supportsMixedCaseIdentifiers();
            meta.storesUpperCaseIdentifiers();
            meta.storesLowerCaseIdentifiers();
            meta.storesMixedCaseIdentifiers();
            meta.supportsMixedCaseQuotedIdentifiers();
            meta.storesUpperCaseQuotedIdentifiers();
            meta.storesLowerCaseQuotedIdentifiers();
            meta.storesMixedCaseQuotedIdentifiers();

            meta.supportsAlterTableWithAddColumn();
            meta.supportsAlterTableWithDropColumn();
            meta.supportsColumnAliasing();
            meta.nullPlusNonNullIsNull();
            meta.supportsConvert();
            meta.supportsTableCorrelationNames();
            meta.supportsDifferentTableCorrelationNames();
            meta.supportsExpressionsInOrderBy();
            meta.supportsOrderByUnrelated();
            meta.supportsGroupBy();
            meta.supportsGroupByUnrelated();
            meta.supportsGroupByBeyondSelect();
            meta.supportsLikeEscapeClause();
            meta.supportsMultipleResultSets();
            meta.supportsMultipleTransactions();
            meta.supportsNonNullableColumns();
            meta.supportsMinimumSQLGrammar();
            meta.supportsCoreSQLGrammar();
            meta.supportsExtendedSQLGrammar();
            meta.supportsANSI92EntryLevelSQL();
            meta.supportsANSI92IntermediateSQL();
            meta.supportsANSI92FullSQL();
            meta.supportsIntegrityEnhancementFacility();
            meta.supportsOuterJoins();
            meta.supportsFullOuterJoins();
            meta.supportsLimitedOuterJoins();
            meta.isCatalogAtStart();
            meta.supportsSchemasInDataManipulation();
            meta.supportsSchemasInProcedureCalls();
            meta.supportsSchemasInTableDefinitions();
            meta.supportsSchemasInIndexDefinitions();
            meta.supportsSchemasInPrivilegeDefinitions();
            meta.supportsCatalogsInDataManipulation();
            meta.supportsCatalogsInProcedureCalls();
            meta.supportsCatalogsInTableDefinitions();
            meta.supportsCatalogsInIndexDefinitions();
            meta.supportsCatalogsInPrivilegeDefinitions();
            meta.supportsPositionedDelete();
            meta.supportsPositionedUpdate();
            meta.supportsSelectForUpdate();
            meta.supportsStoredProcedures();
            meta.supportsSubqueriesInComparisons();
            meta.supportsSubqueriesInExists();
            meta.supportsSubqueriesInIns();
            meta.supportsSubqueriesInQuantifieds();
            meta.supportsCorrelatedSubqueries();
            meta.supportsUnion();
            meta.supportsUnionAll();
        }
    }

	/**
	 * Verifies the JDBC behavior for transaction capabilities.
	 * 
	 * @throws Exception if the operation fails
	 */
    @Test
    public void testTransactionCapabilities() throws Exception
    {
        try (Connection connection = TestConnection.create())
        {
            DatabaseMetaData meta = connection.getMetaData();

            meta.supportsTransactions();

            assertTrue(meta.getDefaultTransactionIsolation() >= 0);

            meta.supportsTransactionIsolationLevel(Connection.TRANSACTION_NONE);
            meta.supportsTransactionIsolationLevel(Connection.TRANSACTION_READ_UNCOMMITTED);
            meta.supportsTransactionIsolationLevel(Connection.TRANSACTION_READ_COMMITTED);
            meta.supportsTransactionIsolationLevel(Connection.TRANSACTION_REPEATABLE_READ);
            meta.supportsTransactionIsolationLevel(Connection.TRANSACTION_SERIALIZABLE);

            meta.supportsDataDefinitionAndDataManipulationTransactions();
            meta.supportsDataManipulationTransactionsOnly();
            meta.dataDefinitionCausesTransactionCommit();
            meta.dataDefinitionIgnoredInTransactions();
        }
    }

	/**
	 * Verifies the JDBC behavior for result set capabilities.
	 * 
	 * @throws Exception if the operation fails
	 */
    @Test
    public void testResultSetCapabilities() throws Exception
    {
        try (Connection connection = TestConnection.create())
        {
            DatabaseMetaData meta = connection.getMetaData();

            meta.supportsResultSetType(ResultSet.TYPE_FORWARD_ONLY);
            meta.supportsResultSetType(ResultSet.TYPE_SCROLL_INSENSITIVE);
            meta.supportsResultSetType(ResultSet.TYPE_SCROLL_SENSITIVE);

            meta.supportsResultSetConcurrency(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            meta.supportsResultSetConcurrency(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            meta.supportsResultSetConcurrency(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);

            meta.ownUpdatesAreVisible(ResultSet.TYPE_FORWARD_ONLY);
            meta.ownDeletesAreVisible(ResultSet.TYPE_FORWARD_ONLY);
            meta.ownInsertsAreVisible(ResultSet.TYPE_FORWARD_ONLY);

            meta.othersUpdatesAreVisible(ResultSet.TYPE_FORWARD_ONLY);
            meta.othersDeletesAreVisible(ResultSet.TYPE_FORWARD_ONLY);
            meta.othersInsertsAreVisible(ResultSet.TYPE_FORWARD_ONLY);

            meta.updatesAreDetected(ResultSet.TYPE_FORWARD_ONLY);
            meta.deletesAreDetected(ResultSet.TYPE_FORWARD_ONLY);
            meta.insertsAreDetected(ResultSet.TYPE_FORWARD_ONLY);
        }
    }

	/**
	 * Verifies the JDBC behavior for limits.
	 * 
	 * @throws Exception if the operation fails
	 */
    @Test
    public void testLimits() throws Exception
    {
        try (Connection connection = TestConnection.create())
        {
            DatabaseMetaData meta = connection.getMetaData();

            assertTrue(meta.getMaxBinaryLiteralLength() >= 0);
            assertTrue(meta.getMaxCharLiteralLength() >= 0);
            assertTrue(meta.getMaxColumnNameLength() >= 0);
            assertTrue(meta.getMaxColumnsInGroupBy() >= 0);
            assertTrue(meta.getMaxColumnsInIndex() >= 0);
            assertTrue(meta.getMaxColumnsInOrderBy() >= 0);
            assertTrue(meta.getMaxColumnsInSelect() >= 0);
            assertTrue(meta.getMaxColumnsInTable() >= 0);
            assertTrue(meta.getMaxConnections() >= 0);
            assertTrue(meta.getMaxCursorNameLength() >= 0);
            assertTrue(meta.getMaxIndexLength() >= 0);
            assertTrue(meta.getMaxProcedureNameLength() >= 0);
            assertTrue(meta.getMaxRowSize() >= 0);
            assertTrue(meta.getMaxSchemaNameLength() >= 0);
            assertTrue(meta.getMaxStatementLength() >= 0);
            assertTrue(meta.getMaxStatements() >= 0);
            assertTrue(meta.getMaxTableNameLength() >= 0);
            assertTrue(meta.getMaxTablesInSelect() >= 0);
            assertTrue(meta.getMaxUserNameLength() >= 0);
        }
    }

	/**
	 * Verifies the JDBC behavior for wrapper contract.
	 * 
	 * @throws Exception if the operation fails
	 */
    @Test
    public void testWrapperContract() throws Exception
    {
        try (Connection connection = TestConnection.create())
        {
            DatabaseMetaData meta = connection.getMetaData();

            assertTrue(meta.isWrapperFor(DatabaseMetaData.class));
            assertSame(meta, meta.unwrap(DatabaseMetaData.class));
        }
    }
}
