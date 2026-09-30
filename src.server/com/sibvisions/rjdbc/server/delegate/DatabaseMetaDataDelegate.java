/*
 * Copyright (C)2026 SIB Visions GmbH
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
package com.sibvisions.rjdbc.server.delegate;

import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;

import com.sibvisions.rjdbc.server.JdbcContext;

/**
 * Server-side delegate for {@code DatabaseMetaData} JDBC operations.
 * 
 * @author René Jahn
 */
public final class DatabaseMetaDataDelegate extends JdbcDelegate
{
    /**
     * Creates a new {@code DatabaseMetaDataDelegate} instance.
     *
     * @param pContext the context
     */
    public DatabaseMetaDataDelegate(JdbcContext pContext)
    {
        super(pContext);
    }

    /**
     * Handles the call operation for the remote JDBC resource.
     *
     * @param pId the remote resource identifier
     * @param pSignature the signature
     * @param pArgs the method arguments
     * @return the resulting JDBC value
     * @throws SQLException if the JDBC operation cannot be completed
     */
    public Object call(long pId, String pSignature, Object[] pArgs) throws SQLException
    {
        switch (pSignature)
        {
            case "allProceduresAreCallable()":

                return remoteValue(context.databaseMetadata(pId).allProceduresAreCallable());
            case "allTablesAreSelectable()":

                return remoteValue(context.databaseMetadata(pId).allTablesAreSelectable());
            case "autoCommitFailureClosesAllResultSets()":

                return remoteValue(context.databaseMetadata(pId).autoCommitFailureClosesAllResultSets());
            case "dataDefinitionCausesTransactionCommit()":

                return remoteValue(context.databaseMetadata(pId).dataDefinitionCausesTransactionCommit());
            case "dataDefinitionIgnoredInTransactions()":

                return remoteValue(context.databaseMetadata(pId).dataDefinitionIgnoredInTransactions());
            case "deletesAreDetected(int)":

                return remoteValue(context.databaseMetadata(pId).deletesAreDetected((int)pArgs[0]));
            case "doesMaxRowSizeIncludeBlobs()":

                return remoteValue(context.databaseMetadata(pId).doesMaxRowSizeIncludeBlobs());
            case "generatedKeyAlwaysReturned()":

                return remoteValue(context.databaseMetadata(pId).generatedKeyAlwaysReturned());
            case "getAttributes(String,String,String,String)":

                return remoteValue(context.databaseMetadata(pId).getAttributes((String)pArgs[0], (String)pArgs[1], (String)pArgs[2], (String)pArgs[3]));
            case "getBestRowIdentifier(String,String,String,int,boolean)":

                return remoteValue(context.databaseMetadata(pId).getBestRowIdentifier((String)pArgs[0], (String)pArgs[1], (String)pArgs[2], (int)pArgs[3], (boolean)pArgs[4]));
            case "getCatalogSeparator()":

                return remoteValue(context.databaseMetadata(pId).getCatalogSeparator());
            case "getCatalogTerm()":

                return remoteValue(context.databaseMetadata(pId).getCatalogTerm());
            case "getCatalogs()":

                return remoteValue(context.databaseMetadata(pId).getCatalogs());
            case "getClientInfoProperties()":

                return remoteValue(context.databaseMetadata(pId).getClientInfoProperties());
            case "getColumnPrivileges(String,String,String,String)":

                return remoteValue(context.databaseMetadata(pId).getColumnPrivileges((String)pArgs[0], (String)pArgs[1], (String)pArgs[2], (String)pArgs[3]));
            case "getColumns(String,String,String,String)":

                return remoteValue(context.databaseMetadata(pId).getColumns((String)pArgs[0], (String)pArgs[1], (String)pArgs[2], (String)pArgs[3]));
            case "getConnection()":

                return remoteValue(context.databaseMetadata(pId).getConnection());
            case "getCrossReference(String,String,String,String,String,String)":

                return remoteValue(context.databaseMetadata(pId).getCrossReference((String)pArgs[0], (String)pArgs[1], (String)pArgs[2], (String)pArgs[3], (String)pArgs[4], (String)pArgs[5]));
            case "getDatabaseMajorVersion()":

                return remoteValue(context.databaseMetadata(pId).getDatabaseMajorVersion());
            case "getDatabaseMinorVersion()":

                return remoteValue(context.databaseMetadata(pId).getDatabaseMinorVersion());
            case "getDatabaseProductName()":

                return remoteValue(context.databaseMetadata(pId).getDatabaseProductName());
            case "getDatabaseProductVersion()":

                return remoteValue(context.databaseMetadata(pId).getDatabaseProductVersion());
            case "getDefaultTransactionIsolation()":

                return remoteValue(context.databaseMetadata(pId).getDefaultTransactionIsolation());
            case "getDriverMajorVersion()":

                return remoteValue(context.databaseMetadata(pId).getDriverMajorVersion());
            case "getDriverMinorVersion()":

                return remoteValue(context.databaseMetadata(pId).getDriverMinorVersion());
            case "getDriverName()":

                return remoteValue(context.databaseMetadata(pId).getDriverName());
            case "getDriverVersion()":

                return remoteValue(context.databaseMetadata(pId).getDriverVersion());
            case "getExportedKeys(String,String,String)":

                return remoteValue(context.databaseMetadata(pId).getExportedKeys((String)pArgs[0], (String)pArgs[1], (String)pArgs[2]));
            case "getExtraNameCharacters()":

                return remoteValue(context.databaseMetadata(pId).getExtraNameCharacters());
            case "getFunctionColumns(String,String,String,String)":

                return remoteValue(context.databaseMetadata(pId).getFunctionColumns((String)pArgs[0], (String)pArgs[1], (String)pArgs[2], (String)pArgs[3]));
            case "getFunctions(String,String,String)":

                return remoteValue(context.databaseMetadata(pId).getFunctions((String)pArgs[0], (String)pArgs[1], (String)pArgs[2]));
            case "getIdentifierQuoteString()":

                return remoteValue(context.databaseMetadata(pId).getIdentifierQuoteString());
            case "getImportedKeys(String,String,String)":

                return remoteValue(context.databaseMetadata(pId).getImportedKeys((String)pArgs[0], (String)pArgs[1], (String)pArgs[2]));
            case "getIndexInfo(String,String,String,boolean,boolean)":

                return remoteValue(context.databaseMetadata(pId).getIndexInfo((String)pArgs[0], (String)pArgs[1], (String)pArgs[2], (boolean)pArgs[3], (boolean)pArgs[4]));
            case "getJDBCMajorVersion()":

                return remoteValue(context.databaseMetadata(pId).getJDBCMajorVersion());
            case "getJDBCMinorVersion()":

                return remoteValue(context.databaseMetadata(pId).getJDBCMinorVersion());
            case "getMaxBinaryLiteralLength()":

                return remoteValue(context.databaseMetadata(pId).getMaxBinaryLiteralLength());
            case "getMaxCatalogNameLength()":

                return remoteValue(context.databaseMetadata(pId).getMaxCatalogNameLength());
            case "getMaxCharLiteralLength()":

                return remoteValue(context.databaseMetadata(pId).getMaxCharLiteralLength());
            case "getMaxColumnNameLength()":

                return remoteValue(context.databaseMetadata(pId).getMaxColumnNameLength());
            case "getMaxColumnsInGroupBy()":

                return remoteValue(context.databaseMetadata(pId).getMaxColumnsInGroupBy());
            case "getMaxColumnsInIndex()":

                return remoteValue(context.databaseMetadata(pId).getMaxColumnsInIndex());
            case "getMaxColumnsInOrderBy()":

                return remoteValue(context.databaseMetadata(pId).getMaxColumnsInOrderBy());
            case "getMaxColumnsInSelect()":

                return remoteValue(context.databaseMetadata(pId).getMaxColumnsInSelect());
            case "getMaxColumnsInTable()":

                return remoteValue(context.databaseMetadata(pId).getMaxColumnsInTable());
            case "getMaxConnections()":

                return remoteValue(context.databaseMetadata(pId).getMaxConnections());
            case "getMaxCursorNameLength()":

                return remoteValue(context.databaseMetadata(pId).getMaxCursorNameLength());
            case "getMaxIndexLength()":

                return remoteValue(context.databaseMetadata(pId).getMaxIndexLength());
            case "getMaxLogicalLobSize()":

                return remoteValue(context.databaseMetadata(pId).getMaxLogicalLobSize());
            case "getMaxProcedureNameLength()":

                return remoteValue(context.databaseMetadata(pId).getMaxProcedureNameLength());
            case "getMaxRowSize()":

                return remoteValue(context.databaseMetadata(pId).getMaxRowSize());
            case "getMaxSchemaNameLength()":

                return remoteValue(context.databaseMetadata(pId).getMaxSchemaNameLength());
            case "getMaxStatementLength()":

                return remoteValue(context.databaseMetadata(pId).getMaxStatementLength());
            case "getMaxStatements()":

                return remoteValue(context.databaseMetadata(pId).getMaxStatements());
            case "getMaxTableNameLength()":

                return remoteValue(context.databaseMetadata(pId).getMaxTableNameLength());
            case "getMaxTablesInSelect()":

                return remoteValue(context.databaseMetadata(pId).getMaxTablesInSelect());
            case "getMaxUserNameLength()":

                return remoteValue(context.databaseMetadata(pId).getMaxUserNameLength());
            case "getNumericFunctions()":

                return remoteValue(context.databaseMetadata(pId).getNumericFunctions());
            case "getPrimaryKeys(String,String,String)":

                return remoteValue(context.databaseMetadata(pId).getPrimaryKeys((String)pArgs[0], (String)pArgs[1], (String)pArgs[2]));
            case "getProcedureColumns(String,String,String,String)":

                return remoteValue(context.databaseMetadata(pId).getProcedureColumns((String)pArgs[0], (String)pArgs[1], (String)pArgs[2], (String)pArgs[3]));
            case "getProcedureTerm()":

                return remoteValue(context.databaseMetadata(pId).getProcedureTerm());
            case "getProcedures(String,String,String)":

                return remoteValue(context.databaseMetadata(pId).getProcedures((String)pArgs[0], (String)pArgs[1], (String)pArgs[2]));
            case "getPseudoColumns(String,String,String,String)":

                return remoteValue(context.databaseMetadata(pId).getPseudoColumns((String)pArgs[0], (String)pArgs[1], (String)pArgs[2], (String)pArgs[3]));
            case "getResultSetHoldability()":

                return remoteValue(context.databaseMetadata(pId).getResultSetHoldability());
            case "getRowIdLifetime()":

                return remoteValue(context.databaseMetadata(pId).getRowIdLifetime());
            case "getSQLKeywords()":

                return remoteValue(context.databaseMetadata(pId).getSQLKeywords());
            case "getSQLStateType()":

                return remoteValue(context.databaseMetadata(pId).getSQLStateType());
            case "getSchemaTerm()":

                return remoteValue(context.databaseMetadata(pId).getSchemaTerm());
            case "getSchemas()":

                return remoteValue(context.databaseMetadata(pId).getSchemas());
            case "getSchemas(String,String)":

                return remoteValue(context.databaseMetadata(pId).getSchemas((String)pArgs[0], (String)pArgs[1]));
            case "getSearchStringEscape()":

                return remoteValue(context.databaseMetadata(pId).getSearchStringEscape());
            case "getStringFunctions()":

                return remoteValue(context.databaseMetadata(pId).getStringFunctions());
            case "getSuperTables(String,String,String)":

                return remoteValue(context.databaseMetadata(pId).getSuperTables((String)pArgs[0], (String)pArgs[1], (String)pArgs[2]));
            case "getSuperTypes(String,String,String)":

                return remoteValue(context.databaseMetadata(pId).getSuperTypes((String)pArgs[0], (String)pArgs[1], (String)pArgs[2]));
            case "getSystemFunctions()":

                return remoteValue(context.databaseMetadata(pId).getSystemFunctions());
            case "getTablePrivileges(String,String,String)":

                return remoteValue(context.databaseMetadata(pId).getTablePrivileges((String)pArgs[0], (String)pArgs[1], (String)pArgs[2]));
            case "getTableTypes()":

                return remoteValue(context.databaseMetadata(pId).getTableTypes());
            case "getTables(String,String,String,String[])":

                return remoteValue(context.databaseMetadata(pId).getTables((String)pArgs[0], (String)pArgs[1], (String)pArgs[2], (String[])pArgs[3]));
            case "getTimeDateFunctions()":

                return remoteValue(context.databaseMetadata(pId).getTimeDateFunctions());
            case "getTypeInfo()":

                return remoteValue(context.databaseMetadata(pId).getTypeInfo());
            case "getUDTs(String,String,String,int[])":

                return remoteValue(context.databaseMetadata(pId).getUDTs((String)pArgs[0], (String)pArgs[1], (String)pArgs[2], (int[]) pArgs[3]));
            case "getURL()":

                return remoteValue(context.databaseMetadata(pId).getURL());
            case "getUserName()":

                return remoteValue(context.databaseMetadata(pId).getUserName());
            case "getVersionColumns(String,String,String)":

                return remoteValue(context.databaseMetadata(pId).getVersionColumns((String)pArgs[0], (String)pArgs[1], (String)pArgs[2]));
            case "insertsAreDetected(int)":

                return remoteValue(context.databaseMetadata(pId).insertsAreDetected((int)pArgs[0]));
            case "isCatalogAtStart()":

                return remoteValue(context.databaseMetadata(pId).isCatalogAtStart());
            case "isReadOnly()":

                return remoteValue(context.databaseMetadata(pId).isReadOnly());
            case "locatorsUpdateCopy()":

                return remoteValue(context.databaseMetadata(pId).locatorsUpdateCopy());
            case "nullPlusNonNullIsNull()":

                return remoteValue(context.databaseMetadata(pId).nullPlusNonNullIsNull());
            case "nullsAreSortedAtEnd()":

                return remoteValue(context.databaseMetadata(pId).nullsAreSortedAtEnd());
            case "nullsAreSortedAtStart()":

                return remoteValue(context.databaseMetadata(pId).nullsAreSortedAtStart());
            case "nullsAreSortedHigh()":

                return remoteValue(context.databaseMetadata(pId).nullsAreSortedHigh());
            case "nullsAreSortedLow()":

                return remoteValue(context.databaseMetadata(pId).nullsAreSortedLow());
            case "othersDeletesAreVisible(int)":

                return remoteValue(context.databaseMetadata(pId).othersDeletesAreVisible((int)pArgs[0]));
            case "othersInsertsAreVisible(int)":

                return remoteValue(context.databaseMetadata(pId).othersInsertsAreVisible((int)pArgs[0]));
            case "othersUpdatesAreVisible(int)":

                return remoteValue(context.databaseMetadata(pId).othersUpdatesAreVisible((int)pArgs[0]));
            case "ownDeletesAreVisible(int)":

                return remoteValue(context.databaseMetadata(pId).ownDeletesAreVisible((int)pArgs[0]));
            case "ownInsertsAreVisible(int)":

                return remoteValue(context.databaseMetadata(pId).ownInsertsAreVisible((int)pArgs[0]));
            case "ownUpdatesAreVisible(int)":

                return remoteValue(context.databaseMetadata(pId).ownUpdatesAreVisible((int)pArgs[0]));
            case "storesLowerCaseIdentifiers()":

                return remoteValue(context.databaseMetadata(pId).storesLowerCaseIdentifiers());
            case "storesLowerCaseQuotedIdentifiers()":

                return remoteValue(context.databaseMetadata(pId).storesLowerCaseQuotedIdentifiers());
            case "storesMixedCaseIdentifiers()":

                return remoteValue(context.databaseMetadata(pId).storesMixedCaseIdentifiers());
            case "storesMixedCaseQuotedIdentifiers()":

                return remoteValue(context.databaseMetadata(pId).storesMixedCaseQuotedIdentifiers());
            case "storesUpperCaseIdentifiers()":

                return remoteValue(context.databaseMetadata(pId).storesUpperCaseIdentifiers());
            case "storesUpperCaseQuotedIdentifiers()":

                return remoteValue(context.databaseMetadata(pId).storesUpperCaseQuotedIdentifiers());
            case "supportsANSI92EntryLevelSQL()":

                return remoteValue(context.databaseMetadata(pId).supportsANSI92EntryLevelSQL());
            case "supportsANSI92FullSQL()":

                return remoteValue(context.databaseMetadata(pId).supportsANSI92FullSQL());
            case "supportsANSI92IntermediateSQL()":

                return remoteValue(context.databaseMetadata(pId).supportsANSI92IntermediateSQL());
            case "supportsAlterTableWithAddColumn()":

                return remoteValue(context.databaseMetadata(pId).supportsAlterTableWithAddColumn());
            case "supportsAlterTableWithDropColumn()":

                return remoteValue(context.databaseMetadata(pId).supportsAlterTableWithDropColumn());
            case "supportsBatchUpdates()":

                return remoteValue(context.databaseMetadata(pId).supportsBatchUpdates());
            case "supportsCatalogsInDataManipulation()":

                return remoteValue(context.databaseMetadata(pId).supportsCatalogsInDataManipulation());
            case "supportsCatalogsInIndexDefinitions()":

                return remoteValue(context.databaseMetadata(pId).supportsCatalogsInIndexDefinitions());
            case "supportsCatalogsInPrivilegeDefinitions()":

                return remoteValue(context.databaseMetadata(pId).supportsCatalogsInPrivilegeDefinitions());
            case "supportsCatalogsInProcedureCalls()":

                return remoteValue(context.databaseMetadata(pId).supportsCatalogsInProcedureCalls());
            case "supportsCatalogsInTableDefinitions()":

                return remoteValue(context.databaseMetadata(pId).supportsCatalogsInTableDefinitions());
            case "supportsColumnAliasing()":

                return remoteValue(context.databaseMetadata(pId).supportsColumnAliasing());
            case "supportsConvert()":

                return remoteValue(context.databaseMetadata(pId).supportsConvert());
            case "supportsConvert(int,int)":

                return remoteValue(context.databaseMetadata(pId).supportsConvert((int)pArgs[0], (int)pArgs[1]));
            case "supportsCoreSQLGrammar()":

                return remoteValue(context.databaseMetadata(pId).supportsCoreSQLGrammar());
            case "supportsCorrelatedSubqueries()":

                return remoteValue(context.databaseMetadata(pId).supportsCorrelatedSubqueries());
            case "supportsDataDefinitionAndDataManipulationTransactions()":

                return remoteValue(context.databaseMetadata(pId).supportsDataDefinitionAndDataManipulationTransactions());
            case "supportsDataManipulationTransactionsOnly()":

                return remoteValue(context.databaseMetadata(pId).supportsDataManipulationTransactionsOnly());
            case "supportsDifferentTableCorrelationNames()":

                return remoteValue(context.databaseMetadata(pId).supportsDifferentTableCorrelationNames());
            case "supportsExpressionsInOrderBy()":

                return remoteValue(context.databaseMetadata(pId).supportsExpressionsInOrderBy());
            case "supportsExtendedSQLGrammar()":

                return remoteValue(context.databaseMetadata(pId).supportsExtendedSQLGrammar());
            case "supportsFullOuterJoins()":

                return remoteValue(context.databaseMetadata(pId).supportsFullOuterJoins());
            case "supportsGetGeneratedKeys()":

                return remoteValue(context.databaseMetadata(pId).supportsGetGeneratedKeys());
            case "supportsGroupBy()":

                return remoteValue(context.databaseMetadata(pId).supportsGroupBy());
            case "supportsGroupByBeyondSelect()":

                return remoteValue(context.databaseMetadata(pId).supportsGroupByBeyondSelect());
            case "supportsGroupByUnrelated()":

                return remoteValue(context.databaseMetadata(pId).supportsGroupByUnrelated());
            case "supportsIntegrityEnhancementFacility()":

                return remoteValue(context.databaseMetadata(pId).supportsIntegrityEnhancementFacility());
            case "supportsLikeEscapeClause()":

                return remoteValue(context.databaseMetadata(pId).supportsLikeEscapeClause());
            case "supportsLimitedOuterJoins()":

                return remoteValue(context.databaseMetadata(pId).supportsLimitedOuterJoins());
            case "supportsMinimumSQLGrammar()":

                return remoteValue(context.databaseMetadata(pId).supportsMinimumSQLGrammar());
            case "supportsMixedCaseIdentifiers()":

                return remoteValue(context.databaseMetadata(pId).supportsMixedCaseIdentifiers());
            case "supportsMixedCaseQuotedIdentifiers()":

                return remoteValue(context.databaseMetadata(pId).supportsMixedCaseQuotedIdentifiers());
            case "supportsMultipleOpenResults()":

                return remoteValue(context.databaseMetadata(pId).supportsMultipleOpenResults());
            case "supportsMultipleResultSets()":

                return remoteValue(context.databaseMetadata(pId).supportsMultipleResultSets());
            case "supportsMultipleTransactions()":

                return remoteValue(context.databaseMetadata(pId).supportsMultipleTransactions());
            case "supportsNamedParameters()":

                return remoteValue(context.databaseMetadata(pId).supportsNamedParameters());
            case "supportsNonNullableColumns()":

                return remoteValue(context.databaseMetadata(pId).supportsNonNullableColumns());
            case "supportsOpenCursorsAcrossCommit()":

                return remoteValue(context.databaseMetadata(pId).supportsOpenCursorsAcrossCommit());
            case "supportsOpenCursorsAcrossRollback()":

                return remoteValue(context.databaseMetadata(pId).supportsOpenCursorsAcrossRollback());
            case "supportsOpenStatementsAcrossCommit()":

                return remoteValue(context.databaseMetadata(pId).supportsOpenStatementsAcrossCommit());
            case "supportsOpenStatementsAcrossRollback()":

                return remoteValue(context.databaseMetadata(pId).supportsOpenStatementsAcrossRollback());
            case "supportsOrderByUnrelated()":

                return remoteValue(context.databaseMetadata(pId).supportsOrderByUnrelated());
            case "supportsOuterJoins()":

                return remoteValue(context.databaseMetadata(pId).supportsOuterJoins());
            case "supportsPositionedDelete()":

                return remoteValue(context.databaseMetadata(pId).supportsPositionedDelete());
            case "supportsPositionedUpdate()":

                return remoteValue(context.databaseMetadata(pId).supportsPositionedUpdate());
            case "supportsRefCursors()":

                return remoteValue(context.databaseMetadata(pId).supportsRefCursors());
            case "supportsResultSetConcurrency(int,int)":

                return remoteValue(context.databaseMetadata(pId).supportsResultSetConcurrency((int)pArgs[0], (int)pArgs[1]));
            case "supportsResultSetHoldability(int)":

                return remoteValue(context.databaseMetadata(pId).supportsResultSetHoldability((int)pArgs[0]));
            case "supportsResultSetType(int)":

                return remoteValue(context.databaseMetadata(pId).supportsResultSetType((int)pArgs[0]));
            case "supportsSavepoints()":

                return remoteValue(context.databaseMetadata(pId).supportsSavepoints());
            case "supportsSchemasInDataManipulation()":

                return remoteValue(context.databaseMetadata(pId).supportsSchemasInDataManipulation());
            case "supportsSchemasInIndexDefinitions()":

                return remoteValue(context.databaseMetadata(pId).supportsSchemasInIndexDefinitions());
            case "supportsSchemasInPrivilegeDefinitions()":

                return remoteValue(context.databaseMetadata(pId).supportsSchemasInPrivilegeDefinitions());
            case "supportsSchemasInProcedureCalls()":

                return remoteValue(context.databaseMetadata(pId).supportsSchemasInProcedureCalls());
            case "supportsSchemasInTableDefinitions()":

                return remoteValue(context.databaseMetadata(pId).supportsSchemasInTableDefinitions());
            case "supportsSelectForUpdate()":

                return remoteValue(context.databaseMetadata(pId).supportsSelectForUpdate());
            case "supportsSharding()":

                return remoteValue(context.databaseMetadata(pId).supportsSharding());
            case "supportsStatementPooling()":

                return remoteValue(context.databaseMetadata(pId).supportsStatementPooling());
            case "supportsStoredFunctionsUsingCallSyntax()":

                return remoteValue(context.databaseMetadata(pId).supportsStoredFunctionsUsingCallSyntax());
            case "supportsStoredProcedures()":

                return remoteValue(context.databaseMetadata(pId).supportsStoredProcedures());
            case "supportsSubqueriesInComparisons()":

                return remoteValue(context.databaseMetadata(pId).supportsSubqueriesInComparisons());
            case "supportsSubqueriesInExists()":

                return remoteValue(context.databaseMetadata(pId).supportsSubqueriesInExists());
            case "supportsSubqueriesInIns()":

                return remoteValue(context.databaseMetadata(pId).supportsSubqueriesInIns());
            case "supportsSubqueriesInQuantifieds()":

                return remoteValue(context.databaseMetadata(pId).supportsSubqueriesInQuantifieds());
            case "supportsTableCorrelationNames()":

                return remoteValue(context.databaseMetadata(pId).supportsTableCorrelationNames());
            case "supportsTransactionIsolationLevel(int)":

                return remoteValue(context.databaseMetadata(pId).supportsTransactionIsolationLevel((int)pArgs[0]));
            case "supportsTransactions()":

                return remoteValue(context.databaseMetadata(pId).supportsTransactions());
            case "supportsUnion()":

                return remoteValue(context.databaseMetadata(pId).supportsUnion());
            case "supportsUnionAll()":

                return remoteValue(context.databaseMetadata(pId).supportsUnionAll());
            case "updatesAreDetected(int)":

                return remoteValue(context.databaseMetadata(pId).updatesAreDetected((int)pArgs[0]));
            case "usesLocalFilePerTable()":

                return remoteValue(context.databaseMetadata(pId).usesLocalFilePerTable());
            case "usesLocalFiles()":

                return remoteValue(context.databaseMetadata(pId).usesLocalFiles());
                
            default:
                throw new SQLFeatureNotSupportedException("Unsupported JDBC method: " + pSignature);
        }
    }
}
