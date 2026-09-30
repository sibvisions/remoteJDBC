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
 * Server-side delegate for {@code Statement} JDBC operations.
 * 
 * @author René Jahn
 */
public final class StatementDelegate extends JdbcDelegate
{
    /**
     * Creates a new {@code StatementDelegate} instance.
     *
     * @param pContext the context
     */
    public StatementDelegate(JdbcContext pContext)
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
            case "addBatch(String)":
                context.statement(pId).addBatch((String)pArgs[0]);

                return null;
            case "cancel()":
                context.statement(pId).cancel();

                return null;
            case "clearBatch()":
                context.statement(pId).clearBatch();

                return null;
            case "clearWarnings()":
                context.statement(pId).clearWarnings();

                return null;
            case "close()":
                context.statement(pId).close();

                return null;
            case "closeOnCompletion()":
                context.statement(pId).closeOnCompletion();

                return null;
            case "enquoteIdentifier(String,boolean)": return remoteValue(context.statement(pId).enquoteIdentifier((String)pArgs[0], (boolean)pArgs[1]));
            case "enquoteLiteral(String)": return remoteValue(context.statement(pId).enquoteLiteral((String)pArgs[0]));
            case "enquoteNCharLiteral(String)": return remoteValue(context.statement(pId).enquoteNCharLiteral((String)pArgs[0]));
            case "execute(String)": return remoteValue(context.statement(pId).execute((String)pArgs[0]));
            case "execute(String,String[])": return remoteValue(context.statement(pId).execute((String)pArgs[0], (String[])pArgs[1]));
            case "execute(String,int)": return remoteValue(context.statement(pId).execute((String)pArgs[0], (int)pArgs[1]));
            case "execute(String,int[])": return remoteValue(context.statement(pId).execute((String)pArgs[0], (int[]) pArgs[1]));
            case "executeBatch()": return remoteValue(context.statement(pId).executeBatch());
            case "executeLargeBatch()": return remoteValue(context.statement(pId).executeLargeBatch());
            case "executeLargeUpdate(String)": return remoteValue(context.statement(pId).executeLargeUpdate((String)pArgs[0]));
            case "executeLargeUpdate(String,String[])": return remoteValue(context.statement(pId).executeLargeUpdate((String)pArgs[0], (String[])pArgs[1]));
            case "executeLargeUpdate(String,int)": return remoteValue(context.statement(pId).executeLargeUpdate((String)pArgs[0], (int)pArgs[1]));
            case "executeLargeUpdate(String,int[])": return remoteValue(context.statement(pId).executeLargeUpdate((String)pArgs[0], (int[]) pArgs[1]));
            case "executeQuery(String)": return remoteValue(context.statement(pId).executeQuery((String)pArgs[0]));
            case "executeUpdate(String)": return remoteValue(context.statement(pId).executeUpdate((String)pArgs[0]));
            case "executeUpdate(String,String[])": return remoteValue(context.statement(pId).executeUpdate((String)pArgs[0], (String[])pArgs[1]));
            case "executeUpdate(String,int)": return remoteValue(context.statement(pId).executeUpdate((String)pArgs[0], (int)pArgs[1]));
            case "executeUpdate(String,int[])": return remoteValue(context.statement(pId).executeUpdate((String)pArgs[0], (int[]) pArgs[1]));
            case "getConnection()": return remoteValue(context.statement(pId).getConnection());
            case "getFetchDirection()": return remoteValue(context.statement(pId).getFetchDirection());
            case "getFetchSize()": return remoteValue(context.statement(pId).getFetchSize());
            case "getGeneratedKeys()": return remoteValue(context.statement(pId).getGeneratedKeys());
            case "getLargeMaxRows()": return remoteValue(context.statement(pId).getLargeMaxRows());
            case "getLargeUpdateCount()": return remoteValue(context.statement(pId).getLargeUpdateCount());
            case "getMaxFieldSize()": return remoteValue(context.statement(pId).getMaxFieldSize());
            case "getMaxRows()": return remoteValue(context.statement(pId).getMaxRows());
            case "getMoreResults()": return remoteValue(context.statement(pId).getMoreResults());
            case "getMoreResults(int)": return remoteValue(context.statement(pId).getMoreResults((int)pArgs[0]));
            case "getQueryTimeout()": return remoteValue(context.statement(pId).getQueryTimeout());
            case "getResultSet()": return remoteValue(context.statement(pId).getResultSet());
            case "getResultSetConcurrency()": return remoteValue(context.statement(pId).getResultSetConcurrency());
            case "getResultSetHoldability()": return remoteValue(context.statement(pId).getResultSetHoldability());
            case "getResultSetType()": return remoteValue(context.statement(pId).getResultSetType());
            case "getUpdateCount()": return remoteValue(context.statement(pId).getUpdateCount());
            case "getWarnings()": return remoteValue(context.statement(pId).getWarnings());
            case "isCloseOnCompletion()": return remoteValue(context.statement(pId).isCloseOnCompletion());
            case "isClosed()": return remoteValue(context.statement(pId).isClosed());
            case "isPoolable()": return remoteValue(context.statement(pId).isPoolable());
            case "isSimpleIdentifier(String)":return remoteValue(context.statement(pId).isSimpleIdentifier((String)pArgs[0]));
            case "setCursorName(String)":
                context.statement(pId).setCursorName((String)pArgs[0]);

                return null;
            case "setEscapeProcessing(boolean)":
                context.statement(pId).setEscapeProcessing((boolean)pArgs[0]);

                return null;
            case "setFetchDirection(int)":
                context.statement(pId).setFetchDirection((int)pArgs[0]);

                return null;
            case "setFetchSize(int)":
                context.statement(pId).setFetchSize((int)pArgs[0]);

                return null;
            case "setLargeMaxRows(long)":
                context.statement(pId).setLargeMaxRows((long)pArgs[0]);

                return null;
            case "setMaxFieldSize(int)":
                context.statement(pId).setMaxFieldSize((int)pArgs[0]);

                return null;
            case "setMaxRows(int)":
                context.statement(pId).setMaxRows((int)pArgs[0]);

                return null;
            case "setPoolable(boolean)":
                context.statement(pId).setPoolable((boolean)pArgs[0]);

                return null;
            case "setQueryTimeout(int)":
                context.statement(pId).setQueryTimeout((int)pArgs[0]);

                return null;
                
            default: throw new SQLFeatureNotSupportedException("Unsupported JDBC method: " + pSignature);
        }
    }
}
