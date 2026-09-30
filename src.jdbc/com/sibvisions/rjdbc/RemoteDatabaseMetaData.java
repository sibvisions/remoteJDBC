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
package com.sibvisions.rjdbc;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.RowIdLifetime;
import java.sql.SQLException;

/**
 * Remote JDBC implementation of {@code DatabaseMetaData} functionality.
 * 
 * @author René Jahn
 */
public class RemoteDatabaseMetaData implements DatabaseMetaData
{
    protected final RemoteClient client;
    
    protected final Class<?> remoteInterface;

    protected final long id;
    

    /**
     * Creates a new {@code RemoteDatabaseMetaData} instance.
     *
     * @param pClient the client
     * @param pId the remote resource identifier
     */
    public RemoteDatabaseMetaData(RemoteClient pClient, long pId)
    {
        this(pClient, pId, DatabaseMetaData.class);
    }

    /**
     * Creates a new {@code RemoteDatabaseMetaData} instance.
     *
     * @param pClient the client
     * @param pId the remote resource identifier
     * @param pRemoteInterface the remote interface
     */
    protected RemoteDatabaseMetaData(RemoteClient pClient, long pId, Class<?> pRemoteInterface)
    {
        client = pClient;
        id = pId;
        
        remoteInterface = pRemoteInterface;
    }

    /** {@inheritDoc} */
    @Override
    public <T> T unwrap(Class<T> pIface) throws SQLException
    {
        return RemoteUtil.unwrapLocal(this, pIface);
    }

    /** {@inheritDoc} */
    @Override
    public boolean isWrapperFor(Class<?> pIface) throws SQLException
    {
        return RemoteUtil.isWrapperForLocal(this, pIface);
    }

    /** {@inheritDoc} */
    @Override
    public String toString()
    {
        return remoteInterface.getSimpleName() + "[" + id + "]";
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode()
    {
        return Long.hashCode(id);
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(Object pObj)
    {
        return this == pObj;
    }

    /** {@inheritDoc} */
    @Override
    public boolean allProceduresAreCallable() throws SQLException
    {
        return (boolean)RemoteUtil.invoke(client, id, remoteInterface, "allProceduresAreCallable", new Class<?>[]{}, new Object[]{}, boolean.class);
    }

    /** {@inheritDoc} */
    @Override
    public boolean allTablesAreSelectable() throws SQLException
    {
        return (boolean)RemoteUtil.invoke(client, id, remoteInterface, "allTablesAreSelectable", new Class<?>[]{}, new Object[]{}, boolean.class);
    }

    /** {@inheritDoc} */
    @Override
    public String getURL() throws SQLException
    {
        return (String)RemoteUtil.invoke(client, id, remoteInterface, "getURL", new Class<?>[]{}, new Object[]{}, String.class);
    }

    /** {@inheritDoc} */
    @Override
    public String getUserName() throws SQLException
    {
        return (String)RemoteUtil.invoke(client, id, remoteInterface, "getUserName", new Class<?>[]{}, new Object[]{}, String.class);
    }

    /** {@inheritDoc} */
    @Override
    public boolean isReadOnly() throws SQLException
    {
        return (boolean)RemoteUtil.invoke(client, id, remoteInterface, "isReadOnly", new Class<?>[]{}, new Object[]{}, boolean.class);
    }

    /** {@inheritDoc} */
    @Override
    public boolean nullsAreSortedHigh() throws SQLException
    {
        return (boolean)RemoteUtil.invoke(client, id, remoteInterface, "nullsAreSortedHigh", new Class<?>[]{}, new Object[]{}, boolean.class);
    }

    /** {@inheritDoc} */
    @Override
    public boolean nullsAreSortedLow() throws SQLException
    {
        return (boolean)RemoteUtil.invoke(client, id, remoteInterface, "nullsAreSortedLow", new Class<?>[]{}, new Object[]{}, boolean.class);
    }

    /** {@inheritDoc} */
    @Override
    public boolean nullsAreSortedAtStart() throws SQLException
    {
        return (boolean)RemoteUtil.invoke(client, id, remoteInterface, "nullsAreSortedAtStart", new Class<?>[]{}, new Object[]{}, boolean.class);
    }

    /** {@inheritDoc} */
    @Override
    public boolean nullsAreSortedAtEnd() throws SQLException
    {
        return (boolean)RemoteUtil.invoke(client, id, remoteInterface, "nullsAreSortedAtEnd", new Class<?>[]{}, new Object[]{}, boolean.class);
    }

    /** {@inheritDoc} */
    @Override
    public String getDatabaseProductName() throws SQLException
    {
        return (String)RemoteUtil.invoke(client, id, remoteInterface, "getDatabaseProductName", new Class<?>[]{}, new Object[]{}, String.class);
    }

    /** {@inheritDoc} */
    @Override
    public String getDatabaseProductVersion() throws SQLException
    {
        return (String)RemoteUtil.invoke(client, id, remoteInterface, "getDatabaseProductVersion", new Class<?>[]{}, new Object[]{}, String.class);
    }

    /** {@inheritDoc} */
    @Override
    public String getDriverName() throws SQLException
    {
        return (String)RemoteUtil.invoke(client, id, remoteInterface, "getDriverName", new Class<?>[]{}, new Object[]{}, String.class);
    }

    /** {@inheritDoc} */
    @Override
    public String getDriverVersion() throws SQLException
    {
        return (String)RemoteUtil.invoke(client, id, remoteInterface, "getDriverVersion", new Class<?>[]{}, new Object[]{}, String.class);
    }

    /** {@inheritDoc} */
    @Override
    public int getDriverMajorVersion()
    {
        return (int)RemoteUtil.invokeUnchecked(client, id, remoteInterface, "getDriverMajorVersion", new Class<?>[]{}, new Object[]{}, int.class);
    }

    /** {@inheritDoc} */
    @Override
    public int getDriverMinorVersion()
    {
        return (int)RemoteUtil.invokeUnchecked(client, id, remoteInterface, "getDriverMinorVersion", new Class<?>[]{}, new Object[]{}, int.class);
    }

    /** {@inheritDoc} */
    @Override
    public boolean usesLocalFiles() throws SQLException
    {
        return (boolean)RemoteUtil.invoke(client, id, remoteInterface, "usesLocalFiles", new Class<?>[]{}, new Object[]{}, boolean.class);
    }

    /** {@inheritDoc} */
    @Override
    public boolean usesLocalFilePerTable() throws SQLException
    {
        return (boolean)RemoteUtil.invoke(client, id, remoteInterface, "usesLocalFilePerTable", new Class<?>[]{}, new Object[]{}, boolean.class);
    }

    /** {@inheritDoc} */
    @Override
    public boolean supportsMixedCaseIdentifiers() throws SQLException
    {
        return (boolean)RemoteUtil.invoke(client, id, remoteInterface, "supportsMixedCaseIdentifiers", new Class<?>[]{}, new Object[]{}, boolean.class);
    }

    /** {@inheritDoc} */
    @Override
    public boolean storesUpperCaseIdentifiers() throws SQLException
    {
        return (boolean)RemoteUtil.invoke(client, id, remoteInterface, "storesUpperCaseIdentifiers", new Class<?>[]{}, new Object[]{}, boolean.class);
    }

    /** {@inheritDoc} */
    @Override
    public boolean storesLowerCaseIdentifiers() throws SQLException
    {
        return (boolean)RemoteUtil.invoke(client, id, remoteInterface, "storesLowerCaseIdentifiers", new Class<?>[]{}, new Object[]{}, boolean.class);
    }

    /** {@inheritDoc} */
    @Override
    public boolean storesMixedCaseIdentifiers() throws SQLException
    {
        return (boolean)RemoteUtil.invoke(client, id, remoteInterface, "storesMixedCaseIdentifiers", new Class<?>[]{}, new Object[]{}, boolean.class);
    }

    /** {@inheritDoc} */
    @Override
    public boolean supportsMixedCaseQuotedIdentifiers() throws SQLException
    {
        return (boolean)RemoteUtil.invoke(client, id, remoteInterface, "supportsMixedCaseQuotedIdentifiers", new Class<?>[]{}, new Object[]{}, boolean.class);
    }

    /** {@inheritDoc} */
    @Override
    public boolean storesUpperCaseQuotedIdentifiers() throws SQLException
    {
        return (boolean)RemoteUtil.invoke(client, id, remoteInterface, "storesUpperCaseQuotedIdentifiers", new Class<?>[]{}, new Object[]{}, boolean.class);
    }

    /** {@inheritDoc} */
    @Override
    public boolean storesLowerCaseQuotedIdentifiers() throws SQLException
    {
        return (boolean)RemoteUtil.invoke(client, id, remoteInterface, "storesLowerCaseQuotedIdentifiers", new Class<?>[]{}, new Object[]{}, boolean.class);
    }

    /** {@inheritDoc} */
    @Override
    public boolean storesMixedCaseQuotedIdentifiers() throws SQLException
    {
        return (boolean)RemoteUtil.invoke(client, id, remoteInterface, "storesMixedCaseQuotedIdentifiers", new Class<?>[]{}, new Object[]{}, boolean.class);
    }

    /** {@inheritDoc} */
    @Override
    public String getIdentifierQuoteString() throws SQLException
    {
        return (String)RemoteUtil.invoke(client, id, remoteInterface, "getIdentifierQuoteString", new Class<?>[]{}, new Object[]{}, String.class);
    }

    /** {@inheritDoc} */
    @Override
    public String getSQLKeywords() throws SQLException
    {
        return (String)RemoteUtil.invoke(client, id, remoteInterface, "getSQLKeywords", new Class<?>[]{}, new Object[]{}, String.class);
    }

    /** {@inheritDoc} */
    @Override
    public String getNumericFunctions() throws SQLException
    {
        return (String)RemoteUtil.invoke(client, id, remoteInterface, "getNumericFunctions", new Class<?>[]{}, new Object[]{}, String.class);
    }

    /** {@inheritDoc} */
    @Override
    public String getStringFunctions() throws SQLException
    {
        return (String)RemoteUtil.invoke(client, id, remoteInterface, "getStringFunctions", new Class<?>[]{}, new Object[]{}, String.class);
    }

    /** {@inheritDoc} */
    @Override
    public String getSystemFunctions() throws SQLException
    {
        return (String)RemoteUtil.invoke(client, id, remoteInterface, "getSystemFunctions", new Class<?>[]{}, new Object[]{}, String.class);
    }

    /** {@inheritDoc} */
    @Override
    public String getTimeDateFunctions() throws SQLException
    {
        return (String)RemoteUtil.invoke(client, id, remoteInterface, "getTimeDateFunctions", new Class<?>[]{}, new Object[]{}, String.class);
    }

    /** {@inheritDoc} */
    @Override
    public String getSearchStringEscape() throws SQLException
    {
        return (String)RemoteUtil.invoke(client, id, remoteInterface, "getSearchStringEscape", new Class<?>[]{}, new Object[]{}, String.class);
    }

    /** {@inheritDoc} */
    @Override
    public String getExtraNameCharacters() throws SQLException
    {
        return (String)RemoteUtil.invoke(client, id, remoteInterface, "getExtraNameCharacters", new Class<?>[]{}, new Object[]{}, String.class);
    }

    /** {@inheritDoc} */
    @Override
    public boolean supportsAlterTableWithAddColumn() throws SQLException
    {
        return (boolean)RemoteUtil.invoke(client, id, remoteInterface, "supportsAlterTableWithAddColumn", new Class<?>[]{}, new Object[]{}, boolean.class);
    }

    /** {@inheritDoc} */
    @Override
    public boolean supportsAlterTableWithDropColumn() throws SQLException
    {
        return (boolean)RemoteUtil.invoke(client, id, remoteInterface, "supportsAlterTableWithDropColumn", new Class<?>[]{}, new Object[]{}, boolean.class);
    }

    /** {@inheritDoc} */
    @Override
    public boolean supportsColumnAliasing() throws SQLException
    {
        return (boolean)RemoteUtil.invoke(client, id, remoteInterface, "supportsColumnAliasing", new Class<?>[]{}, new Object[]{}, boolean.class);
    }

    /** {@inheritDoc} */
    @Override
    public boolean nullPlusNonNullIsNull() throws SQLException
    {
        return (boolean)RemoteUtil.invoke(client, id, remoteInterface, "nullPlusNonNullIsNull", new Class<?>[]{}, new Object[]{}, boolean.class);
    }

    /** {@inheritDoc} */
    @Override
    public boolean supportsConvert() throws SQLException
    {
        return (boolean)RemoteUtil.invoke(client, id, remoteInterface, "supportsConvert", new Class<?>[]{}, new Object[]{}, boolean.class);
    }

    /** {@inheritDoc} */
    @Override
    public boolean supportsConvert(int pFromType, int pToType) throws SQLException
    {
        return (boolean)RemoteUtil.invoke(client, id, remoteInterface, "supportsConvert", new Class<?>[]{int.class, int.class}, new Object[]{pFromType, pToType}, boolean.class);
    }

    /** {@inheritDoc} */
    @Override
    public boolean supportsTableCorrelationNames() throws SQLException
    {
        return (boolean)RemoteUtil.invoke(client, id, remoteInterface, "supportsTableCorrelationNames", new Class<?>[]{}, new Object[]{}, boolean.class);
    }

    /** {@inheritDoc} */
    @Override
    public boolean supportsDifferentTableCorrelationNames() throws SQLException
    {
        return (boolean)RemoteUtil.invoke(client, id, remoteInterface, "supportsDifferentTableCorrelationNames", new Class<?>[]{}, new Object[]{}, boolean.class);
    }

    /** {@inheritDoc} */
    @Override
    public boolean supportsExpressionsInOrderBy() throws SQLException
    {
        return (boolean)RemoteUtil.invoke(client, id, remoteInterface, "supportsExpressionsInOrderBy", new Class<?>[]{}, new Object[]{}, boolean.class);
    }

    /** {@inheritDoc} */
    @Override
    public boolean supportsOrderByUnrelated() throws SQLException
    {
        return (boolean)RemoteUtil.invoke(client, id, remoteInterface, "supportsOrderByUnrelated", new Class<?>[]{}, new Object[]{}, boolean.class);
    }

    /** {@inheritDoc} */
    @Override
    public boolean supportsGroupBy() throws SQLException
    {
        return (boolean)RemoteUtil.invoke(client, id, remoteInterface, "supportsGroupBy", new Class<?>[]{}, new Object[]{}, boolean.class);
    }

    /** {@inheritDoc} */
    @Override
    public boolean supportsGroupByUnrelated() throws SQLException
    {
        return (boolean)RemoteUtil.invoke(client, id, remoteInterface, "supportsGroupByUnrelated", new Class<?>[]{}, new Object[]{}, boolean.class);
    }

    /** {@inheritDoc} */
    @Override
    public boolean supportsGroupByBeyondSelect() throws SQLException
    {
        return (boolean)RemoteUtil.invoke(client, id, remoteInterface, "supportsGroupByBeyondSelect", new Class<?>[]{}, new Object[]{}, boolean.class);
    }

    /** {@inheritDoc} */
    @Override
    public boolean supportsLikeEscapeClause() throws SQLException
    {
        return (boolean)RemoteUtil.invoke(client, id, remoteInterface, "supportsLikeEscapeClause", new Class<?>[]{}, new Object[]{}, boolean.class);
    }

    /** {@inheritDoc} */
    @Override
    public boolean supportsMultipleResultSets() throws SQLException
    {
        return (boolean)RemoteUtil.invoke(client, id, remoteInterface, "supportsMultipleResultSets", new Class<?>[]{}, new Object[]{}, boolean.class);
    }

    /** {@inheritDoc} */
    @Override
    public boolean supportsMultipleTransactions() throws SQLException
    {
        return (boolean)RemoteUtil.invoke(client, id, remoteInterface, "supportsMultipleTransactions", new Class<?>[]{}, new Object[]{}, boolean.class);
    }

    /** {@inheritDoc} */
    @Override
    public boolean supportsNonNullableColumns() throws SQLException
    {
        return (boolean)RemoteUtil.invoke(client, id, remoteInterface, "supportsNonNullableColumns", new Class<?>[]{}, new Object[]{}, boolean.class);
    }

    /** {@inheritDoc} */
    @Override
    public boolean supportsMinimumSQLGrammar() throws SQLException
    {
        return (boolean)RemoteUtil.invoke(client, id, remoteInterface, "supportsMinimumSQLGrammar", new Class<?>[]{}, new Object[]{}, boolean.class);
    }

    /** {@inheritDoc} */
    @Override
    public boolean supportsCoreSQLGrammar() throws SQLException
    {
        return (boolean)RemoteUtil.invoke(client, id, remoteInterface, "supportsCoreSQLGrammar", new Class<?>[]{}, new Object[]{}, boolean.class);
    }

    /** {@inheritDoc} */
    @Override
    public boolean supportsExtendedSQLGrammar() throws SQLException
    {
        return (boolean)RemoteUtil.invoke(client, id, remoteInterface, "supportsExtendedSQLGrammar", new Class<?>[]{}, new Object[]{}, boolean.class);
    }

    /** {@inheritDoc} */
    @Override
    public boolean supportsANSI92EntryLevelSQL() throws SQLException
    {
        return (boolean)RemoteUtil.invoke(client, id, remoteInterface, "supportsANSI92EntryLevelSQL", new Class<?>[]{}, new Object[]{}, boolean.class);
    }

    /** {@inheritDoc} */
    @Override
    public boolean supportsANSI92IntermediateSQL() throws SQLException
    {
        return (boolean)RemoteUtil.invoke(client, id, remoteInterface, "supportsANSI92IntermediateSQL", new Class<?>[]{}, new Object[]{}, boolean.class);
    }

    /** {@inheritDoc} */
    @Override
    public boolean supportsANSI92FullSQL() throws SQLException
    {
        return (boolean)RemoteUtil.invoke(client, id, remoteInterface, "supportsANSI92FullSQL", new Class<?>[]{}, new Object[]{}, boolean.class);
    }

    /** {@inheritDoc} */
    @Override
    public boolean supportsIntegrityEnhancementFacility() throws SQLException
    {
        return (boolean)RemoteUtil.invoke(client, id, remoteInterface, "supportsIntegrityEnhancementFacility", new Class<?>[]{}, new Object[]{}, boolean.class);
    }

    /** {@inheritDoc} */
    @Override
    public boolean supportsOuterJoins() throws SQLException
    {
        return (boolean)RemoteUtil.invoke(client, id, remoteInterface, "supportsOuterJoins", new Class<?>[]{}, new Object[]{}, boolean.class);
    }

    /** {@inheritDoc} */
    @Override
    public boolean supportsFullOuterJoins() throws SQLException
    {
        return (boolean)RemoteUtil.invoke(client, id, remoteInterface, "supportsFullOuterJoins", new Class<?>[]{}, new Object[]{}, boolean.class);
    }

    /** {@inheritDoc} */
    @Override
    public boolean supportsLimitedOuterJoins() throws SQLException
    {
        return (boolean)RemoteUtil.invoke(client, id, remoteInterface, "supportsLimitedOuterJoins", new Class<?>[]{}, new Object[]{}, boolean.class);
    }

    /** {@inheritDoc} */
    @Override
    public String getSchemaTerm() throws SQLException
    {
        return (String)RemoteUtil.invoke(client, id, remoteInterface, "getSchemaTerm", new Class<?>[]{}, new Object[]{}, String.class);
    }

    /** {@inheritDoc} */
    @Override
    public String getProcedureTerm() throws SQLException
    {
        return (String)RemoteUtil.invoke(client, id, remoteInterface, "getProcedureTerm", new Class<?>[]{}, new Object[]{}, String.class);
    }

    /** {@inheritDoc} */
    @Override
    public String getCatalogTerm() throws SQLException
    {
        return (String)RemoteUtil.invoke(client, id, remoteInterface, "getCatalogTerm", new Class<?>[]{}, new Object[]{}, String.class);
    }

    /** {@inheritDoc} */
    @Override
    public boolean isCatalogAtStart() throws SQLException
    {
        return (boolean)RemoteUtil.invoke(client, id, remoteInterface, "isCatalogAtStart", new Class<?>[]{}, new Object[]{}, boolean.class);
    }

    /** {@inheritDoc} */
    @Override
    public String getCatalogSeparator() throws SQLException
    {
        return (String)RemoteUtil.invoke(client, id, remoteInterface, "getCatalogSeparator", new Class<?>[]{}, new Object[]{}, String.class);
    }

    /** {@inheritDoc} */
    @Override
    public boolean supportsSchemasInDataManipulation() throws SQLException
    {
        return (boolean)RemoteUtil.invoke(client, id, remoteInterface, "supportsSchemasInDataManipulation", new Class<?>[]{}, new Object[]{}, boolean.class);
    }

    /** {@inheritDoc} */
    @Override
    public boolean supportsSchemasInProcedureCalls() throws SQLException
    {
        return (boolean)RemoteUtil.invoke(client, id, remoteInterface, "supportsSchemasInProcedureCalls", new Class<?>[]{}, new Object[]{}, boolean.class);
    }

    /** {@inheritDoc} */
    @Override
    public boolean supportsSchemasInTableDefinitions() throws SQLException
    {
        return (boolean)RemoteUtil.invoke(client, id, remoteInterface, "supportsSchemasInTableDefinitions", new Class<?>[]{}, new Object[]{}, boolean.class);
    }

    /** {@inheritDoc} */
    @Override
    public boolean supportsSchemasInIndexDefinitions() throws SQLException
    {
        return (boolean)RemoteUtil.invoke(client, id, remoteInterface, "supportsSchemasInIndexDefinitions", new Class<?>[]{}, new Object[]{}, boolean.class);
    }

    /** {@inheritDoc} */
    @Override
    public boolean supportsSchemasInPrivilegeDefinitions() throws SQLException
    {
        return (boolean)RemoteUtil.invoke(client, id, remoteInterface, "supportsSchemasInPrivilegeDefinitions", new Class<?>[]{}, new Object[]{}, boolean.class);
    }

    /** {@inheritDoc} */
    @Override
    public boolean supportsCatalogsInDataManipulation() throws SQLException
    {
        return (boolean)RemoteUtil.invoke(client, id, remoteInterface, "supportsCatalogsInDataManipulation", new Class<?>[]{}, new Object[]{}, boolean.class);
    }

    /** {@inheritDoc} */
    @Override
    public boolean supportsCatalogsInProcedureCalls() throws SQLException
    {
        return (boolean)RemoteUtil.invoke(client, id, remoteInterface, "supportsCatalogsInProcedureCalls", new Class<?>[]{}, new Object[]{}, boolean.class);
    }

    /** {@inheritDoc} */
    @Override
    public boolean supportsCatalogsInTableDefinitions() throws SQLException
    {
        return (boolean)RemoteUtil.invoke(client, id, remoteInterface, "supportsCatalogsInTableDefinitions", new Class<?>[]{}, new Object[]{}, boolean.class);
    }

    /** {@inheritDoc} */
    @Override
    public boolean supportsCatalogsInIndexDefinitions() throws SQLException
    {
        return (boolean)RemoteUtil.invoke(client, id, remoteInterface, "supportsCatalogsInIndexDefinitions", new Class<?>[]{}, new Object[]{}, boolean.class);
    }

    /** {@inheritDoc} */
    @Override
    public boolean supportsCatalogsInPrivilegeDefinitions() throws SQLException
    {
        return (boolean)RemoteUtil.invoke(client, id, remoteInterface, "supportsCatalogsInPrivilegeDefinitions", new Class<?>[]{}, new Object[]{}, boolean.class);
    }

    /** {@inheritDoc} */
    @Override
    public boolean supportsPositionedDelete() throws SQLException
    {
        return (boolean)RemoteUtil.invoke(client, id, remoteInterface, "supportsPositionedDelete", new Class<?>[]{}, new Object[]{}, boolean.class);
    }

    /** {@inheritDoc} */
    @Override
    public boolean supportsPositionedUpdate() throws SQLException
    {
        return (boolean)RemoteUtil.invoke(client, id, remoteInterface, "supportsPositionedUpdate", new Class<?>[]{}, new Object[]{}, boolean.class);
    }

    /** {@inheritDoc} */
    @Override
    public boolean supportsSelectForUpdate() throws SQLException
    {
        return (boolean)RemoteUtil.invoke(client, id, remoteInterface, "supportsSelectForUpdate", new Class<?>[]{}, new Object[]{}, boolean.class);
    }

    /** {@inheritDoc} */
    @Override
    public boolean supportsStoredProcedures() throws SQLException
    {
        return (boolean)RemoteUtil.invoke(client, id, remoteInterface, "supportsStoredProcedures", new Class<?>[]{}, new Object[]{}, boolean.class);
    }

    /** {@inheritDoc} */
    @Override
    public boolean supportsSubqueriesInComparisons() throws SQLException
    {
        return (boolean)RemoteUtil.invoke(client, id, remoteInterface, "supportsSubqueriesInComparisons", new Class<?>[]{}, new Object[]{}, boolean.class);
    }

    /** {@inheritDoc} */
    @Override
    public boolean supportsSubqueriesInExists() throws SQLException
    {
        return (boolean)RemoteUtil.invoke(client, id, remoteInterface, "supportsSubqueriesInExists", new Class<?>[]{}, new Object[]{}, boolean.class);
    }

    /** {@inheritDoc} */
    @Override
    public boolean supportsSubqueriesInIns() throws SQLException
    {
        return (boolean)RemoteUtil.invoke(client, id, remoteInterface, "supportsSubqueriesInIns", new Class<?>[]{}, new Object[]{}, boolean.class);
    }

    /** {@inheritDoc} */
    @Override
    public boolean supportsSubqueriesInQuantifieds() throws SQLException
    {
        return (boolean)RemoteUtil.invoke(client, id, remoteInterface, "supportsSubqueriesInQuantifieds", new Class<?>[]{}, new Object[]{}, boolean.class);
    }

    /** {@inheritDoc} */
    @Override
    public boolean supportsCorrelatedSubqueries() throws SQLException
    {
        return (boolean)RemoteUtil.invoke(client, id, remoteInterface, "supportsCorrelatedSubqueries", new Class<?>[]{}, new Object[]{}, boolean.class);
    }

    /** {@inheritDoc} */
    @Override
    public boolean supportsUnion() throws SQLException
    {
        return (boolean)RemoteUtil.invoke(client, id, remoteInterface, "supportsUnion", new Class<?>[]{}, new Object[]{}, boolean.class);
    }

    /** {@inheritDoc} */
    @Override
    public boolean supportsUnionAll() throws SQLException
    {
        return (boolean)RemoteUtil.invoke(client, id, remoteInterface, "supportsUnionAll", new Class<?>[]{}, new Object[]{}, boolean.class);
    }

    /** {@inheritDoc} */
    @Override
    public boolean supportsOpenCursorsAcrossCommit() throws SQLException
    {
        return (boolean)RemoteUtil.invoke(client, id, remoteInterface, "supportsOpenCursorsAcrossCommit", new Class<?>[]{}, new Object[]{}, boolean.class);
    }

    /** {@inheritDoc} */
    @Override
    public boolean supportsOpenCursorsAcrossRollback() throws SQLException
    {
        return (boolean)RemoteUtil.invoke(client, id, remoteInterface, "supportsOpenCursorsAcrossRollback", new Class<?>[]{}, new Object[]{}, boolean.class);
    }

    /** {@inheritDoc} */
    @Override
    public boolean supportsOpenStatementsAcrossCommit() throws SQLException
    {
        return (boolean)RemoteUtil.invoke(client, id, remoteInterface, "supportsOpenStatementsAcrossCommit", new Class<?>[]{}, new Object[]{}, boolean.class);
    }

    /** {@inheritDoc} */
    @Override
    public boolean supportsOpenStatementsAcrossRollback() throws SQLException
    {
        return (boolean)RemoteUtil.invoke(client, id, remoteInterface, "supportsOpenStatementsAcrossRollback", new Class<?>[]{}, new Object[]{}, boolean.class);
    }

    /** {@inheritDoc} */
    @Override
    public int getMaxBinaryLiteralLength() throws SQLException
    {
        return (int)RemoteUtil.invoke(client, id, remoteInterface, "getMaxBinaryLiteralLength", new Class<?>[]{}, new Object[]{}, int.class);
    }

    /** {@inheritDoc} */
    @Override
    public int getMaxCharLiteralLength() throws SQLException
    {
        return (int)RemoteUtil.invoke(client, id, remoteInterface, "getMaxCharLiteralLength", new Class<?>[]{}, new Object[]{}, int.class);
    }

    /** {@inheritDoc} */
    @Override
    public int getMaxColumnNameLength() throws SQLException
    {
        return (int)RemoteUtil.invoke(client, id, remoteInterface, "getMaxColumnNameLength", new Class<?>[]{}, new Object[]{}, int.class);
    }

    /** {@inheritDoc} */
    @Override
    public int getMaxColumnsInGroupBy() throws SQLException
    {
        return (int)RemoteUtil.invoke(client, id, remoteInterface, "getMaxColumnsInGroupBy", new Class<?>[]{}, new Object[]{}, int.class);
    }

    /** {@inheritDoc} */
    @Override
    public int getMaxColumnsInIndex() throws SQLException
    {
        return (int)RemoteUtil.invoke(client, id, remoteInterface, "getMaxColumnsInIndex", new Class<?>[]{}, new Object[]{}, int.class);
    }

    /** {@inheritDoc} */
    @Override
    public int getMaxColumnsInOrderBy() throws SQLException
    {
        return (int)RemoteUtil.invoke(client, id, remoteInterface, "getMaxColumnsInOrderBy", new Class<?>[]{}, new Object[]{}, int.class);
    }

    /** {@inheritDoc} */
    @Override
    public int getMaxColumnsInSelect() throws SQLException
    {
        return (int)RemoteUtil.invoke(client, id, remoteInterface, "getMaxColumnsInSelect", new Class<?>[]{}, new Object[]{}, int.class);
    }

    /** {@inheritDoc} */
    @Override
    public int getMaxColumnsInTable() throws SQLException
    {
        return (int)RemoteUtil.invoke(client, id, remoteInterface, "getMaxColumnsInTable", new Class<?>[]{}, new Object[]{}, int.class);
    }

    /** {@inheritDoc} */
    @Override
    public int getMaxConnections() throws SQLException
    {
        return (int)RemoteUtil.invoke(client, id, remoteInterface, "getMaxConnections", new Class<?>[]{}, new Object[]{}, int.class);
    }

    /** {@inheritDoc} */
    @Override
    public int getMaxCursorNameLength() throws SQLException
    {
        return (int)RemoteUtil.invoke(client, id, remoteInterface, "getMaxCursorNameLength", new Class<?>[]{}, new Object[]{}, int.class);
    }

    /** {@inheritDoc} */
    @Override
    public int getMaxIndexLength() throws SQLException
    {
        return (int)RemoteUtil.invoke(client, id, remoteInterface, "getMaxIndexLength", new Class<?>[]{}, new Object[]{}, int.class);
    }

    /** {@inheritDoc} */
    @Override
    public int getMaxSchemaNameLength() throws SQLException
    {
        return (int)RemoteUtil.invoke(client, id, remoteInterface, "getMaxSchemaNameLength", new Class<?>[]{}, new Object[]{}, int.class);
    }

    /** {@inheritDoc} */
    @Override
    public int getMaxProcedureNameLength() throws SQLException
    {
        return (int)RemoteUtil.invoke(client, id, remoteInterface, "getMaxProcedureNameLength", new Class<?>[]{}, new Object[]{}, int.class);
    }

    /** {@inheritDoc} */
    @Override
    public int getMaxCatalogNameLength() throws SQLException
    {
        return (int)RemoteUtil.invoke(client, id, remoteInterface, "getMaxCatalogNameLength", new Class<?>[]{}, new Object[]{}, int.class);
    }

    /** {@inheritDoc} */
    @Override
    public int getMaxRowSize() throws SQLException
    {
        return (int)RemoteUtil.invoke(client, id, remoteInterface, "getMaxRowSize", new Class<?>[]{}, new Object[]{}, int.class);
    }

    /** {@inheritDoc} */
    @Override
    public boolean doesMaxRowSizeIncludeBlobs() throws SQLException
    {
        return (boolean)RemoteUtil.invoke(client, id, remoteInterface, "doesMaxRowSizeIncludeBlobs", new Class<?>[]{}, new Object[]{}, boolean.class);
    }

    /** {@inheritDoc} */
    @Override
    public int getMaxStatementLength() throws SQLException
    {
        return (int)RemoteUtil.invoke(client, id, remoteInterface, "getMaxStatementLength", new Class<?>[]{}, new Object[]{}, int.class);
    }

    /** {@inheritDoc} */
    @Override
    public int getMaxStatements() throws SQLException
    {
        return (int)RemoteUtil.invoke(client, id, remoteInterface, "getMaxStatements", new Class<?>[]{}, new Object[]{}, int.class);
    }

    /** {@inheritDoc} */
    @Override
    public int getMaxTableNameLength() throws SQLException
    {
        return (int)RemoteUtil.invoke(client, id, remoteInterface, "getMaxTableNameLength", new Class<?>[]{}, new Object[]{}, int.class);
    }

    /** {@inheritDoc} */
    @Override
    public int getMaxTablesInSelect() throws SQLException
    {
        return (int)RemoteUtil.invoke(client, id, remoteInterface, "getMaxTablesInSelect", new Class<?>[]{}, new Object[]{}, int.class);
    }

    /** {@inheritDoc} */
    @Override
    public int getMaxUserNameLength() throws SQLException
    {
        return (int)RemoteUtil.invoke(client, id, remoteInterface, "getMaxUserNameLength", new Class<?>[]{}, new Object[]{}, int.class);
    }

    /** {@inheritDoc} */
    @Override
    public int getDefaultTransactionIsolation() throws SQLException
    {
        return (int)RemoteUtil.invoke(client, id, remoteInterface, "getDefaultTransactionIsolation", new Class<?>[]{}, new Object[]{}, int.class);
    }

    /** {@inheritDoc} */
    @Override
    public boolean supportsTransactions() throws SQLException
    {
        return (boolean)RemoteUtil.invoke(client, id, remoteInterface, "supportsTransactions", new Class<?>[]{}, new Object[]{}, boolean.class);
    }

    /** {@inheritDoc} */
    @Override
    public boolean supportsTransactionIsolationLevel(int pType) throws SQLException
    {
        return (boolean)RemoteUtil.invoke(client, id, remoteInterface, "supportsTransactionIsolationLevel", new Class<?>[]{int.class}, new Object[]{pType}, boolean.class);
    }

    /** {@inheritDoc} */
    @Override
    public boolean supportsDataDefinitionAndDataManipulationTransactions() throws SQLException
    {
        return (boolean)RemoteUtil.invoke(client, id, remoteInterface, "supportsDataDefinitionAndDataManipulationTransactions", new Class<?>[]{}, new Object[]{}, boolean.class);
    }

    /** {@inheritDoc} */
    @Override
    public boolean supportsDataManipulationTransactionsOnly() throws SQLException
    {
        return (boolean)RemoteUtil.invoke(client, id, remoteInterface, "supportsDataManipulationTransactionsOnly", new Class<?>[]{}, new Object[]{}, boolean.class);
    }

    /** {@inheritDoc} */
    @Override
    public boolean dataDefinitionCausesTransactionCommit() throws SQLException
    {
        return (boolean)RemoteUtil.invoke(client, id, remoteInterface, "dataDefinitionCausesTransactionCommit", new Class<?>[]{}, new Object[]{}, boolean.class);
    }

    /** {@inheritDoc} */
    @Override
    public boolean dataDefinitionIgnoredInTransactions() throws SQLException
    {
        return (boolean)RemoteUtil.invoke(client, id, remoteInterface, "dataDefinitionIgnoredInTransactions", new Class<?>[]{}, new Object[]{}, boolean.class);
    }

    /** {@inheritDoc} */
    @Override
    public ResultSet getProcedures(String pCatalog, String pSchemaPattern, String pProcedureNamePattern) throws SQLException
    {
        return (ResultSet)RemoteUtil.invoke(client, id, remoteInterface, "getProcedures", new Class<?>[]{String.class, String.class, String.class}, new Object[]{pCatalog, pSchemaPattern, pProcedureNamePattern}, ResultSet.class);
    }

    /** {@inheritDoc} */
    @Override
    public ResultSet getProcedureColumns(String pCatalog, String pSchemaPattern, String pProcedureNamePattern, String pColumnNamePattern) throws SQLException
    {
        return (ResultSet)RemoteUtil.invoke(client, id, remoteInterface, "getProcedureColumns", new Class<?>[]{String.class, String.class, String.class, String.class}, new Object[]{pCatalog, pSchemaPattern, pProcedureNamePattern, pColumnNamePattern}, ResultSet.class);
    }

    /** {@inheritDoc} */
    @Override
    public ResultSet getTables(String pCatalog, String pSchemaPattern, String pTableNamePattern, String[] pTypes) throws SQLException
    {
        return (ResultSet)RemoteUtil.invoke(client, id, remoteInterface, "getTables", new Class<?>[]{String.class, String.class, String.class, String[].class}, new Object[]{pCatalog, pSchemaPattern, pTableNamePattern, pTypes}, ResultSet.class);
    }

    /** {@inheritDoc} */
    @Override
    public ResultSet getSchemas() throws SQLException
    {
        return (ResultSet)RemoteUtil.invoke(client, id, remoteInterface, "getSchemas", new Class<?>[]{}, new Object[]{}, ResultSet.class);
    }

    /** {@inheritDoc} */
    @Override
    public ResultSet getCatalogs() throws SQLException
    {
        return (ResultSet)RemoteUtil.invoke(client, id, remoteInterface, "getCatalogs", new Class<?>[]{}, new Object[]{}, ResultSet.class);
    }

    /** {@inheritDoc} */
    @Override
    public ResultSet getTableTypes() throws SQLException
    {
        return (ResultSet)RemoteUtil.invoke(client, id, remoteInterface, "getTableTypes", new Class<?>[]{}, new Object[]{}, ResultSet.class);
    }

    /** {@inheritDoc} */
    @Override
    public ResultSet getColumns(String pCatalog, String pSchemaPattern, String pTableNamePattern, String pColumnNamePattern) throws SQLException
    {
        return (ResultSet)RemoteUtil.invoke(client, id, remoteInterface, "getColumns", new Class<?>[]{String.class, String.class, String.class, String.class}, new Object[]{pCatalog, pSchemaPattern, pTableNamePattern, pColumnNamePattern}, ResultSet.class);
    }

    /** {@inheritDoc} */
    @Override
    public ResultSet getColumnPrivileges(String pCatalog, String pSchema, String pTable, String pColumnNamePattern) throws SQLException
    {
        return (ResultSet)RemoteUtil.invoke(client, id, remoteInterface, "getColumnPrivileges", new Class<?>[]{String.class, String.class, String.class, String.class}, new Object[]{pCatalog, pSchema, pTable, pColumnNamePattern}, ResultSet.class);
    }

    /** {@inheritDoc} */
    @Override
    public ResultSet getTablePrivileges(String pCatalog, String pSchemaPattern, String pTableNamePattern) throws SQLException
    {
        return (ResultSet)RemoteUtil.invoke(client, id, remoteInterface, "getTablePrivileges", new Class<?>[]{String.class, String.class, String.class}, new Object[]{pCatalog, pSchemaPattern, pTableNamePattern}, ResultSet.class);
    }

    /** {@inheritDoc} */
    @Override
    public ResultSet getBestRowIdentifier(String pCatalog, String pSchema, String pTable, int pScope, boolean pNullable) throws SQLException
    {
        return (ResultSet)RemoteUtil.invoke(client, id, remoteInterface, "getBestRowIdentifier", new Class<?>[]{String.class, String.class, String.class, int.class, boolean.class}, new Object[]{pCatalog, pSchema, pTable, pScope, pNullable}, ResultSet.class);
    }

    /** {@inheritDoc} */
    @Override
    public ResultSet getVersionColumns(String pCatalog, String pSchema, String pTable) throws SQLException
    {
        return (ResultSet)RemoteUtil.invoke(client, id, remoteInterface, "getVersionColumns", new Class<?>[]{String.class, String.class, String.class}, new Object[]{pCatalog, pSchema, pTable}, ResultSet.class);
    }

    /** {@inheritDoc} */
    @Override
    public ResultSet getPrimaryKeys(String pCatalog, String pSchema, String pTable) throws SQLException
    {
        return (ResultSet)RemoteUtil.invoke(client, id, remoteInterface, "getPrimaryKeys", new Class<?>[]{String.class, String.class, String.class}, new Object[]{pCatalog, pSchema, pTable}, ResultSet.class);
    }

    /** {@inheritDoc} */
    @Override
    public ResultSet getImportedKeys(String pCatalog, String pSchema, String pTable) throws SQLException
    {
        return (ResultSet)RemoteUtil.invoke(client, id, remoteInterface, "getImportedKeys", new Class<?>[]{String.class, String.class, String.class}, new Object[]{pCatalog, pSchema, pTable}, ResultSet.class);
    }

    /** {@inheritDoc} */
    @Override
    public ResultSet getExportedKeys(String pCatalog, String pSchema, String pTable) throws SQLException
    {
        return (ResultSet)RemoteUtil.invoke(client, id, remoteInterface, "getExportedKeys", new Class<?>[]{String.class, String.class, String.class}, new Object[]{pCatalog, pSchema, pTable}, ResultSet.class);
    }

    /** {@inheritDoc} */
    @Override
    public ResultSet getCrossReference(String pParentCatalog, String pParentSchema, String pParentTable, String pForeignCatalog, String pForeignSchema, String pForeignTable) throws SQLException
    {
        return (ResultSet)RemoteUtil.invoke(client, id, remoteInterface, "getCrossReference", new Class<?>[]{String.class, String.class, String.class, String.class, String.class, String.class}, new Object[]{pParentCatalog, pParentSchema, pParentTable, pForeignCatalog, pForeignSchema, pForeignTable}, ResultSet.class);
    }

    /** {@inheritDoc} */
    @Override
    public ResultSet getTypeInfo() throws SQLException
    {
        return (ResultSet)RemoteUtil.invoke(client, id, remoteInterface, "getTypeInfo", new Class<?>[]{}, new Object[]{}, ResultSet.class);
    }

    /** {@inheritDoc} */
    @Override
    public ResultSet getIndexInfo(String pCatalog, String pSchema, String pTable, boolean pUnique, boolean pApproximate) throws SQLException
    {
        return (ResultSet)RemoteUtil.invoke(client, id, remoteInterface, "getIndexInfo", new Class<?>[]{String.class, String.class, String.class, boolean.class, boolean.class}, new Object[]{pCatalog, pSchema, pTable, pUnique, pApproximate}, ResultSet.class);
    }

    /** {@inheritDoc} */
    @Override
    public boolean supportsResultSetType(int pType) throws SQLException
    {
        return (boolean)RemoteUtil.invoke(client, id, remoteInterface, "supportsResultSetType", new Class<?>[]{int.class}, new Object[]{pType}, boolean.class);
    }

    /** {@inheritDoc} */
    @Override
    public boolean supportsResultSetConcurrency(int pType, int pConcurrency) throws SQLException
    {
        return (boolean)RemoteUtil.invoke(client, id, remoteInterface, "supportsResultSetConcurrency", new Class<?>[]{int.class, int.class}, new Object[]{pType, pConcurrency}, boolean.class);
    }

    /** {@inheritDoc} */
    @Override
    public boolean ownUpdatesAreVisible(int pType) throws SQLException
    {
        return (boolean)RemoteUtil.invoke(client, id, remoteInterface, "ownUpdatesAreVisible", new Class<?>[]{int.class}, new Object[]{pType}, boolean.class);
    }

    /** {@inheritDoc} */
    @Override
    public boolean ownDeletesAreVisible(int pType) throws SQLException
    {
        return (boolean)RemoteUtil.invoke(client, id, remoteInterface, "ownDeletesAreVisible", new Class<?>[]{int.class}, new Object[]{pType}, boolean.class);
    }

    /** {@inheritDoc} */
    @Override
    public boolean ownInsertsAreVisible(int pType) throws SQLException
    {
        return (boolean)RemoteUtil.invoke(client, id, remoteInterface, "ownInsertsAreVisible", new Class<?>[]{int.class}, new Object[]{pType}, boolean.class);
    }

    /** {@inheritDoc} */
    @Override
    public boolean othersUpdatesAreVisible(int pType) throws SQLException
    {
        return (boolean)RemoteUtil.invoke(client, id, remoteInterface, "othersUpdatesAreVisible", new Class<?>[]{int.class}, new Object[]{pType}, boolean.class);
    }

    /** {@inheritDoc} */
    @Override
    public boolean othersDeletesAreVisible(int pType) throws SQLException
    {
        return (boolean)RemoteUtil.invoke(client, id, remoteInterface, "othersDeletesAreVisible", new Class<?>[]{int.class}, new Object[]{pType}, boolean.class);
    }

    /** {@inheritDoc} */
    @Override
    public boolean othersInsertsAreVisible(int pType) throws SQLException
    {
        return (boolean)RemoteUtil.invoke(client, id, remoteInterface, "othersInsertsAreVisible", new Class<?>[]{int.class}, new Object[]{pType}, boolean.class);
    }

    /** {@inheritDoc} */
    @Override
    public boolean updatesAreDetected(int pType) throws SQLException
    {
        return (boolean)RemoteUtil.invoke(client, id, remoteInterface, "updatesAreDetected", new Class<?>[]{int.class}, new Object[]{pType}, boolean.class);
    }

    /** {@inheritDoc} */
    @Override
    public boolean deletesAreDetected(int pType) throws SQLException
    {
        return (boolean)RemoteUtil.invoke(client, id, remoteInterface, "deletesAreDetected", new Class<?>[]{int.class}, new Object[]{pType}, boolean.class);
    }

    /** {@inheritDoc} */
    @Override
    public boolean insertsAreDetected(int pType) throws SQLException
    {
        return (boolean)RemoteUtil.invoke(client, id, remoteInterface, "insertsAreDetected", new Class<?>[]{int.class}, new Object[]{pType}, boolean.class);
    }

    /** {@inheritDoc} */
    @Override
    public boolean supportsBatchUpdates() throws SQLException
    {
        return (boolean)RemoteUtil.invoke(client, id, remoteInterface, "supportsBatchUpdates", new Class<?>[]{}, new Object[]{}, boolean.class);
    }

    /** {@inheritDoc} */
    @Override
    public ResultSet getUDTs(String pCatalog, String pSchemaPattern, String pTypeNamePattern, int[] pTypes) throws SQLException
    {
        return (ResultSet)RemoteUtil.invoke(client, id, remoteInterface, "getUDTs", new Class<?>[]{String.class, String.class, String.class, int[].class}, new Object[]{pCatalog, pSchemaPattern, pTypeNamePattern, pTypes}, ResultSet.class);
    }

    /** {@inheritDoc} */
    @Override
    public Connection getConnection() throws SQLException
    {
        return (Connection)RemoteUtil.invoke(client, id, remoteInterface, "getConnection", new Class<?>[]{}, new Object[]{}, Connection.class);
    }

    /** {@inheritDoc} */
    @Override
    public boolean supportsSavepoints() throws SQLException
    {
        return (boolean)RemoteUtil.invoke(client, id, remoteInterface, "supportsSavepoints", new Class<?>[]{}, new Object[]{}, boolean.class);
    }

    /** {@inheritDoc} */
    @Override
    public boolean supportsNamedParameters() throws SQLException
    {
        return (boolean)RemoteUtil.invoke(client, id, remoteInterface, "supportsNamedParameters", new Class<?>[]{}, new Object[]{}, boolean.class);
    }

    /** {@inheritDoc} */
    @Override
    public boolean supportsMultipleOpenResults() throws SQLException
    {
        return (boolean)RemoteUtil.invoke(client, id, remoteInterface, "supportsMultipleOpenResults", new Class<?>[]{}, new Object[]{}, boolean.class);
    }

    /** {@inheritDoc} */
    @Override
    public boolean supportsGetGeneratedKeys() throws SQLException
    {
        return (boolean)RemoteUtil.invoke(client, id, remoteInterface, "supportsGetGeneratedKeys", new Class<?>[]{}, new Object[]{}, boolean.class);
    }

    /** {@inheritDoc} */
    @Override
    public ResultSet getSuperTypes(String pCatalog, String pSchemaPattern, String pTypeNamePattern) throws SQLException
    {
        return (ResultSet)RemoteUtil.invoke(client, id, remoteInterface, "getSuperTypes", new Class<?>[]{String.class, String.class, String.class}, new Object[]{pCatalog, pSchemaPattern, pTypeNamePattern}, ResultSet.class);
    }

    /** {@inheritDoc} */
    @Override
    public ResultSet getSuperTables(String pCatalog, String pSchemaPattern, String pTableNamePattern) throws SQLException
    {
        return (ResultSet)RemoteUtil.invoke(client, id, remoteInterface, "getSuperTables", new Class<?>[]{String.class, String.class, String.class}, new Object[]{pCatalog, pSchemaPattern, pTableNamePattern}, ResultSet.class);
    }

    /** {@inheritDoc} */
    @Override
    public ResultSet getAttributes(String pCatalog, String pSchemaPattern, String pTypeNamePattern, String pAttributeNamePattern) throws SQLException
    {
        return (ResultSet)RemoteUtil.invoke(client, id, remoteInterface, "getAttributes", new Class<?>[]{String.class, String.class, String.class, String.class}, new Object[]{pCatalog, pSchemaPattern, pTypeNamePattern, pAttributeNamePattern}, ResultSet.class);
    }

    /** {@inheritDoc} */
    @Override
    public boolean supportsResultSetHoldability(int pHoldability) throws SQLException
    {
        return (boolean)RemoteUtil.invoke(client, id, remoteInterface, "supportsResultSetHoldability", new Class<?>[]{int.class}, new Object[]{pHoldability}, boolean.class);
    }

    /** {@inheritDoc} */
    @Override
    public int getResultSetHoldability() throws SQLException
    {
        return (int)RemoteUtil.invoke(client, id, remoteInterface, "getResultSetHoldability", new Class<?>[]{}, new Object[]{}, int.class);
    }

    /** {@inheritDoc} */
    @Override
    public int getDatabaseMajorVersion() throws SQLException
    {
        return (int)RemoteUtil.invoke(client, id, remoteInterface, "getDatabaseMajorVersion", new Class<?>[]{}, new Object[]{}, int.class);
    }

    /** {@inheritDoc} */
    @Override
    public int getDatabaseMinorVersion() throws SQLException
    {
        return (int)RemoteUtil.invoke(client, id, remoteInterface, "getDatabaseMinorVersion", new Class<?>[]{}, new Object[]{}, int.class);
    }

    /** {@inheritDoc} */
    @Override
    public int getJDBCMajorVersion() throws SQLException
    {
        return (int)RemoteUtil.invoke(client, id, remoteInterface, "getJDBCMajorVersion", new Class<?>[]{}, new Object[]{}, int.class);
    }

    /** {@inheritDoc} */
    @Override
    public int getJDBCMinorVersion() throws SQLException
    {
        return (int)RemoteUtil.invoke(client, id, remoteInterface, "getJDBCMinorVersion", new Class<?>[]{}, new Object[]{}, int.class);
    }

    /** {@inheritDoc} */
    @Override
    public int getSQLStateType() throws SQLException
    {
        return (int)RemoteUtil.invoke(client, id, remoteInterface, "getSQLStateType", new Class<?>[]{}, new Object[]{}, int.class);
    }

    /** {@inheritDoc} */
    @Override
    public boolean locatorsUpdateCopy() throws SQLException
    {
        return (boolean)RemoteUtil.invoke(client, id, remoteInterface, "locatorsUpdateCopy", new Class<?>[]{}, new Object[]{}, boolean.class);
    }

    /** {@inheritDoc} */
    @Override
    public boolean supportsStatementPooling() throws SQLException
    {
        return (boolean)RemoteUtil.invoke(client, id, remoteInterface, "supportsStatementPooling", new Class<?>[]{}, new Object[]{}, boolean.class);
    }

    /** {@inheritDoc} */
    @Override
    public RowIdLifetime getRowIdLifetime() throws SQLException
    {
        return (RowIdLifetime)RemoteUtil.invoke(client, id, remoteInterface, "getRowIdLifetime", new Class<?>[]{}, new Object[]{}, RowIdLifetime.class);
    }

    /** {@inheritDoc} */
    @Override
    public ResultSet getSchemas(String pCatalog, String pSchemaPattern) throws SQLException
    {
        return (ResultSet)RemoteUtil.invoke(client, id, remoteInterface, "getSchemas", new Class<?>[]{String.class, String.class}, new Object[]{pCatalog, pSchemaPattern}, ResultSet.class);
    }

    /** {@inheritDoc} */
    @Override
    public boolean supportsStoredFunctionsUsingCallSyntax() throws SQLException
    {
        return (boolean)RemoteUtil.invoke(client, id, remoteInterface, "supportsStoredFunctionsUsingCallSyntax", new Class<?>[]{}, new Object[]{}, boolean.class);
    }

    /** {@inheritDoc} */
    @Override
    public boolean autoCommitFailureClosesAllResultSets() throws SQLException
    {
        return (boolean)RemoteUtil.invoke(client, id, remoteInterface, "autoCommitFailureClosesAllResultSets", new Class<?>[]{}, new Object[]{}, boolean.class);
    }

    /** {@inheritDoc} */
    @Override
    public ResultSet getClientInfoProperties() throws SQLException
    {
        return (ResultSet)RemoteUtil.invoke(client, id, remoteInterface, "getClientInfoProperties", new Class<?>[]{}, new Object[]{}, ResultSet.class);
    }

    /** {@inheritDoc} */
    @Override
    public ResultSet getFunctions(String pCatalog, String pSchemaPattern, String pFunctionNamePattern) throws SQLException
    {
        return (ResultSet)RemoteUtil.invoke(client, id, remoteInterface, "getFunctions", new Class<?>[]{String.class, String.class, String.class}, new Object[]{pCatalog, pSchemaPattern, pFunctionNamePattern}, ResultSet.class);
    }

    /** {@inheritDoc} */
    @Override
    public ResultSet getFunctionColumns(String pCatalog, String pSchemaPattern, String pFunctionNamePattern, String pColumnNamePattern) throws SQLException
    {
        return (ResultSet)RemoteUtil.invoke(client, id, remoteInterface, "getFunctionColumns", new Class<?>[]{String.class, String.class, String.class, String.class}, new Object[]{pCatalog, pSchemaPattern, pFunctionNamePattern, pColumnNamePattern}, ResultSet.class);
    }

    /** {@inheritDoc} */
    @Override
    public ResultSet getPseudoColumns(String pCatalog, String pSchemaPattern, String pTableNamePattern, String pColumnNamePattern) throws SQLException
    {
        return (ResultSet)RemoteUtil.invoke(client, id, remoteInterface, "getPseudoColumns", new Class<?>[]{String.class, String.class, String.class, String.class}, new Object[]{pCatalog, pSchemaPattern, pTableNamePattern, pColumnNamePattern}, ResultSet.class);
    }

    /** {@inheritDoc} */
    @Override
    public boolean generatedKeyAlwaysReturned() throws SQLException
    {
        return (boolean)RemoteUtil.invoke(client, id, remoteInterface, "generatedKeyAlwaysReturned", new Class<?>[]{}, new Object[]{}, boolean.class);
    }

    /** {@inheritDoc} */
    @Override
    public long getMaxLogicalLobSize() throws SQLException
    {
        return (long)RemoteUtil.invoke(client, id, remoteInterface, "getMaxLogicalLobSize", new Class<?>[]{}, new Object[]{}, long.class);
    }

    /** {@inheritDoc} */
    @Override
    public boolean supportsRefCursors() throws SQLException
    {
        return (boolean)RemoteUtil.invoke(client, id, remoteInterface, "supportsRefCursors", new Class<?>[]{}, new Object[]{}, boolean.class);
    }

    /** {@inheritDoc} */
    @Override
    public boolean supportsSharding() throws SQLException
    {
        return (boolean)RemoteUtil.invoke(client, id, remoteInterface, "supportsSharding", new Class<?>[]{}, new Object[]{}, boolean.class);
    }
}
