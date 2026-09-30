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
 * Server-side delegate for {@code ResultSetMetaData} JDBC operations.
 * 
 * @author René Jahn
 */
public final class ResultSetMetaDataDelegate extends JdbcDelegate
{
    /**
     * Creates a new {@code ResultSetMetaDataDelegate} instance.
     *
     * @param pContext the context
     */
    public ResultSetMetaDataDelegate(JdbcContext pContext)
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
            case "getCatalogName(int)": return remoteValue(context.resultSetMetadata(pId).getCatalogName((int)pArgs[0]));
            case "getColumnClassName(int)": return remoteValue(context.resultSetMetadata(pId).getColumnClassName((int)pArgs[0]));
            case "getColumnCount()": return remoteValue(context.resultSetMetadata(pId).getColumnCount());
            case "getColumnDisplaySize(int)": return remoteValue(context.resultSetMetadata(pId).getColumnDisplaySize((int)pArgs[0]));
            case "getColumnLabel(int)": return remoteValue(context.resultSetMetadata(pId).getColumnLabel((int)pArgs[0]));
            case "getColumnName(int)": return remoteValue(context.resultSetMetadata(pId).getColumnName((int)pArgs[0]));
            case "getColumnType(int)": return remoteValue(context.resultSetMetadata(pId).getColumnType((int)pArgs[0]));
            case "getColumnTypeName(int)": return remoteValue(context.resultSetMetadata(pId).getColumnTypeName((int)pArgs[0]));
            case "getPrecision(int)": return remoteValue(context.resultSetMetadata(pId).getPrecision((int)pArgs[0]));
            case "getScale(int)": return remoteValue(context.resultSetMetadata(pId).getScale((int)pArgs[0]));
            case "getSchemaName(int)": return remoteValue(context.resultSetMetadata(pId).getSchemaName((int)pArgs[0]));
            case "getTableName(int)": return remoteValue(context.resultSetMetadata(pId).getTableName((int)pArgs[0]));
            case "isAutoIncrement(int)": return remoteValue(context.resultSetMetadata(pId).isAutoIncrement((int)pArgs[0]));
            case "isCaseSensitive(int)": return remoteValue(context.resultSetMetadata(pId).isCaseSensitive((int)pArgs[0]));
            case "isCurrency(int)": return remoteValue(context.resultSetMetadata(pId).isCurrency((int)pArgs[0]));
            case "isDefinitelyWritable(int)": return remoteValue(context.resultSetMetadata(pId).isDefinitelyWritable((int)pArgs[0]));
            case "isNullable(int)": return remoteValue(context.resultSetMetadata(pId).isNullable((int)pArgs[0]));
            case "isReadOnly(int)": return remoteValue(context.resultSetMetadata(pId).isReadOnly((int)pArgs[0]));
            case "isSearchable(int)": return remoteValue(context.resultSetMetadata(pId).isSearchable((int)pArgs[0]));
            case "isSigned(int)": return remoteValue(context.resultSetMetadata(pId).isSigned((int)pArgs[0]));
            case "isWritable(int)": return remoteValue(context.resultSetMetadata(pId).isWritable((int)pArgs[0]));
            
            default: throw new SQLFeatureNotSupportedException("Unsupported JDBC method: " + pSignature);
        }
    }
}
