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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;

/**
 * Remote JDBC implementation of {@code Statement} functionality.
 */
public class RemoteStatement implements Statement
{
    protected final RemoteClient client;
    
    protected final long id;
    
    protected final Class<?> remoteInterface;
    
    private volatile boolean closed;

    /**
     * Creates a new {@code RemoteStatement} instance.
     *
     * @param pClient the client
     * @param pId the remote resource identifier
     */
    public RemoteStatement(RemoteClient pClient, long pId)
    {
        this(pClient, pId, Statement.class);
    }

    /**
     * Creates a new {@code RemoteStatement} instance.
     *
     * @param pClient the client
     * @param pId the remote resource identifier
     * @param pRemoteInterface the remote interface
     */
    protected RemoteStatement(RemoteClient pClient, long pId, Class<?> pRemoteInterface)
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
    public ResultSet executeQuery(String pSql) throws SQLException
    {
    	ensureOpen();
    	
        return (ResultSet)RemoteUtil.invoke(client, id, remoteInterface, "executeQuery", new Class<?>[]{String.class}, new Object[]{pSql}, ResultSet.class);
    }

    /** {@inheritDoc} */
    @Override
    public int executeUpdate(String pSql) throws SQLException
    {
    	ensureOpen();

    	return (int)RemoteUtil.invoke(client, id, remoteInterface, "executeUpdate", new Class<?>[]{String.class}, new Object[]{pSql}, int.class);
    }

    /** {@inheritDoc} */
    @Override
    public void close() throws SQLException
    {
        if (closed)
        {
            return;

        }
        try
        {
            RemoteUtil.invoke(client, id, remoteInterface, "close", new Class<?>[]{}, new Object[]{}, void.class);
        }
        finally
        {
            closed = true;
        }

    }

    /** {@inheritDoc} */
    @Override
    public int getMaxFieldSize() throws SQLException
    {
    	ensureOpen();

    	return (int)RemoteUtil.invoke(client, id, remoteInterface, "getMaxFieldSize", new Class<?>[]{}, new Object[]{}, int.class);
    }

    /** {@inheritDoc} */
    @Override
    public void setMaxFieldSize(int pMax) throws SQLException
    {
    	ensureOpen();

    	RemoteUtil.invoke(client, id, remoteInterface, "setMaxFieldSize", new Class<?>[]{int.class}, new Object[]{pMax}, void.class);
    }

    /** {@inheritDoc} */
    @Override
    public int getMaxRows() throws SQLException
    {
    	ensureOpen();

    	return (int)RemoteUtil.invoke(client, id, remoteInterface, "getMaxRows", new Class<?>[]{}, new Object[]{}, int.class);
    }

    /** {@inheritDoc} */
    @Override
    public void setMaxRows(int pMax) throws SQLException
    {
    	ensureOpen();

    	RemoteUtil.invoke(client, id, remoteInterface, "setMaxRows", new Class<?>[]{int.class}, new Object[]{pMax}, void.class);
    }

    /** {@inheritDoc} */
    @Override
    public void setEscapeProcessing(boolean pEscapeProcessing) throws SQLException
    {
    	ensureOpen();
    	
    	RemoteUtil.invoke(client, id, remoteInterface, "setEscapeProcessing", new Class<?>[]{boolean.class}, new Object[]{pEscapeProcessing}, void.class);
    }

    /** {@inheritDoc} */
    @Override
    public int getQueryTimeout() throws SQLException
    {
    	ensureOpen();
    	
        return (int)RemoteUtil.invoke(client, id, remoteInterface, "getQueryTimeout", new Class<?>[]{}, new Object[]{}, int.class);
    }

    /** {@inheritDoc} */
    @Override
    public void setQueryTimeout(int pSeconds) throws SQLException
    {
    	ensureOpen();
    	
        RemoteUtil.invoke(client, id, remoteInterface, "setQueryTimeout", new Class<?>[]{int.class}, new Object[]{pSeconds}, void.class);
    }

    /** {@inheritDoc} */
    @Override
    public void cancel() throws SQLException
    {
    	ensureOpen();
    	
        RemoteUtil.invoke(client, id, remoteInterface, "cancel", new Class<?>[]{}, new Object[]{}, void.class);
    }

    /** {@inheritDoc} */
    @Override
    public SQLWarning getWarnings() throws SQLException
    {
    	ensureOpen();
    	
        return (SQLWarning)RemoteUtil.invoke(client, id, remoteInterface, "getWarnings", new Class<?>[]{}, new Object[]{}, SQLWarning.class);
    }

    /** {@inheritDoc} */
    @Override
    public void clearWarnings() throws SQLException
    {
    	ensureOpen();
    	
        RemoteUtil.invoke(client, id, remoteInterface, "clearWarnings", new Class<?>[]{}, new Object[]{}, void.class);
    }

    /** {@inheritDoc} */
    @Override
    public void setCursorName(String pName) throws SQLException
    {
    	ensureOpen();
    	
        RemoteUtil.invoke(client, id, remoteInterface, "setCursorName", new Class<?>[]{String.class}, new Object[]{pName}, void.class);
    }

    /** {@inheritDoc} */
    @Override
    public boolean execute(String pSql) throws SQLException
    {
    	ensureOpen();
    	
        return (boolean)RemoteUtil.invoke(client, id, remoteInterface, "execute", new Class<?>[]{String.class}, new Object[]{pSql}, boolean.class);
    }

    /** {@inheritDoc} */
    @Override
    public ResultSet getResultSet() throws SQLException
    {
    	ensureOpen();
    	
        return (ResultSet)RemoteUtil.invoke(client, id, remoteInterface, "getResultSet", new Class<?>[]{}, new Object[]{}, ResultSet.class);
    }

    /** {@inheritDoc} */
    @Override
    public int getUpdateCount() throws SQLException
    {
    	ensureOpen();
    	
        return (int)RemoteUtil.invoke(client, id, remoteInterface, "getUpdateCount", new Class<?>[]{}, new Object[]{}, int.class);
    }

    /** {@inheritDoc} */
    @Override
    public boolean getMoreResults() throws SQLException
    {
    	ensureOpen();
    	
        return (boolean)RemoteUtil.invoke(client, id, remoteInterface, "getMoreResults", new Class<?>[]{}, new Object[]{}, boolean.class);
    }

    /** {@inheritDoc} */
    @Override
    public void setFetchDirection(int pDirection) throws SQLException
    {
    	ensureOpen();
    	
        RemoteUtil.invoke(client, id, remoteInterface, "setFetchDirection", new Class<?>[]{int.class}, new Object[]{pDirection}, void.class);
    }

    /** {@inheritDoc} */
    @Override
    public int getFetchDirection() throws SQLException
    {
    	ensureOpen();
    	
        return (int)RemoteUtil.invoke(client, id, remoteInterface, "getFetchDirection", new Class<?>[]{}, new Object[]{}, int.class);
    }

    /** {@inheritDoc} */
    @Override
    public void setFetchSize(int pRows) throws SQLException
    {
    	ensureOpen();
    	
    	RemoteUtil.invoke(client, id, remoteInterface, "setFetchSize", new Class<?>[]{int.class}, new Object[]{pRows}, void.class);
    }

    /** {@inheritDoc} */
    @Override
    public int getFetchSize() throws SQLException
    {
    	ensureOpen();
    	
        return (int)RemoteUtil.invoke(client, id, remoteInterface, "getFetchSize", new Class<?>[]{}, new Object[]{}, int.class);
    }

    /** {@inheritDoc} */
    @Override
    public int getResultSetConcurrency() throws SQLException
    {
    	ensureOpen();
    	
        return (int)RemoteUtil.invoke(client, id, remoteInterface, "getResultSetConcurrency", new Class<?>[]{}, new Object[]{}, int.class);
    }

    /** {@inheritDoc} */
    @Override
    public int getResultSetType() throws SQLException
    {
    	ensureOpen();
    	
        return (int)RemoteUtil.invoke(client, id, remoteInterface, "getResultSetType", new Class<?>[]{}, new Object[]{}, int.class);
    }

    /** {@inheritDoc} */
    @Override
    public void addBatch(String pSql) throws SQLException
    {
    	ensureOpen();
    	
        RemoteUtil.invoke(client, id, remoteInterface, "addBatch", new Class<?>[]{String.class}, new Object[]{pSql}, void.class);
    }

    /** {@inheritDoc} */
    @Override
    public void clearBatch() throws SQLException
    {
    	ensureOpen();
    	
        RemoteUtil.invoke(client, id, remoteInterface, "clearBatch", new Class<?>[]{}, new Object[]{}, void.class);
    }

    /** {@inheritDoc} */
    @Override
    public int[] executeBatch() throws SQLException
    {
    	ensureOpen();
    	
        return (int[]) RemoteUtil.invoke(client, id, remoteInterface, "executeBatch", new Class<?>[]{}, new Object[]{}, int[].class);
    }

    /** {@inheritDoc} */
    @Override
    public Connection getConnection() throws SQLException
    {
    	ensureOpen();
    	
        return (Connection)RemoteUtil.invoke(client, id, remoteInterface, "getConnection", new Class<?>[]{}, new Object[]{}, Connection.class);
    }

    /** {@inheritDoc} */
    @Override
    public boolean getMoreResults(int pCurrent) throws SQLException
    {
    	ensureOpen();
    	
        return (boolean)RemoteUtil.invoke(client, id, remoteInterface, "getMoreResults", new Class<?>[]{int.class}, new Object[]{pCurrent}, boolean.class);
    }

    /** {@inheritDoc} */
    @Override
    public ResultSet getGeneratedKeys() throws SQLException
    {
    	ensureOpen();
    	
        return (ResultSet)RemoteUtil.invoke(client, id, remoteInterface, "getGeneratedKeys", new Class<?>[]{}, new Object[]{}, ResultSet.class);
    }

    /** {@inheritDoc} */
    @Override
    public int executeUpdate(String pSql, int pAutoGeneratedKeys) throws SQLException
    {
    	ensureOpen();
    	
        return (int)RemoteUtil.invoke(client, id, remoteInterface, "executeUpdate", new Class<?>[]{String.class, int.class}, new Object[]{pSql, pAutoGeneratedKeys}, int.class);
    }

    /** {@inheritDoc} */
    @Override
    public int executeUpdate(String pSql, int[] pColumnIndexes) throws SQLException
    {
    	ensureOpen();
    	
        return (int)RemoteUtil.invoke(client, id, remoteInterface, "executeUpdate", new Class<?>[]{String.class, int[].class}, new Object[]{pSql, pColumnIndexes}, int.class);
    }

    /** {@inheritDoc} */
    @Override
    public int executeUpdate(String pSql, String[] pColumnNames) throws SQLException
    {
    	ensureOpen();
    	
        return (int)RemoteUtil.invoke(client, id, remoteInterface, "executeUpdate", new Class<?>[]{String.class, String[].class}, new Object[]{pSql, pColumnNames}, int.class);
    }

    /** {@inheritDoc} */
    @Override
    public boolean execute(String pSql, int pAutoGeneratedKeys) throws SQLException
    {
    	ensureOpen();
    	
        return (boolean)RemoteUtil.invoke(client, id, remoteInterface, "execute", new Class<?>[]{String.class, int.class}, new Object[]{pSql, pAutoGeneratedKeys}, boolean.class);
    }

    /** {@inheritDoc} */
    @Override
    public boolean execute(String pSql, int[] pColumnIndexes) throws SQLException
    {
    	ensureOpen();
    	
        return (boolean)RemoteUtil.invoke(client, id, remoteInterface, "execute", new Class<?>[]{String.class, int[].class}, new Object[]{pSql, pColumnIndexes}, boolean.class);
    }

    /** {@inheritDoc} */
    @Override
    public boolean execute(String pSql, String[] pColumnNames) throws SQLException
    {
    	ensureOpen();
    	
        return (boolean)RemoteUtil.invoke(client, id, remoteInterface, "execute", new Class<?>[]{String.class, String[].class}, new Object[]{pSql, pColumnNames}, boolean.class);
    }

    /** {@inheritDoc} */
    @Override
    public int getResultSetHoldability() throws SQLException
    {
    	ensureOpen();
    	
        return (int)RemoteUtil.invoke(client, id, remoteInterface, "getResultSetHoldability", new Class<?>[]{}, new Object[]{}, int.class);
    }

    /** {@inheritDoc} */
    @Override
    public boolean isClosed() throws SQLException
    {
        if (closed || client.isSessionClosed() || client.isSessionBroken())
        {
            return true;

        }

        return (boolean)RemoteUtil.invoke(client, id, remoteInterface, "isClosed", new Class<?>[]{}, new Object[]{}, boolean.class);
    }

    /** {@inheritDoc} */
    @Override
    public void setPoolable(boolean pPoolable) throws SQLException
    {
    	ensureOpen();
    	
        RemoteUtil.invoke(client, id, remoteInterface, "setPoolable", new Class<?>[]{boolean.class}, new Object[]{pPoolable}, void.class);
    }

    /** {@inheritDoc} */
    @Override
    public boolean isPoolable() throws SQLException
    {
    	ensureOpen();
    	
        return (boolean)RemoteUtil.invoke(client, id, remoteInterface, "isPoolable", new Class<?>[]{}, new Object[]{}, boolean.class);
    }

    /** {@inheritDoc} */
    @Override
    public void closeOnCompletion() throws SQLException
    {
    	ensureOpen();
    	
        RemoteUtil.invoke(client, id, remoteInterface, "closeOnCompletion", new Class<?>[]{}, new Object[]{}, void.class);
    }

    /** {@inheritDoc} */
    @Override
    public boolean isCloseOnCompletion() throws SQLException
    {
    	ensureOpen();
    	
        return (boolean)RemoteUtil.invoke(client, id, remoteInterface, "isCloseOnCompletion", new Class<?>[]{}, new Object[]{}, boolean.class);
    }

    /** {@inheritDoc} */
    @Override
    public long getLargeUpdateCount() throws SQLException
    {
    	ensureOpen();
    	
        return (long)RemoteUtil.invoke(client, id, remoteInterface, "getLargeUpdateCount", new Class<?>[]{}, new Object[]{}, long.class);
    }

    /** {@inheritDoc} */
    @Override
    public void setLargeMaxRows(long pLargeMaxRows) throws SQLException
    {
    	ensureOpen();
    	
        RemoteUtil.invoke(client, id, remoteInterface, "setLargeMaxRows", new Class<?>[]{long.class}, new Object[]{pLargeMaxRows}, void.class);
    }

    /** {@inheritDoc} */
    @Override
    public long getLargeMaxRows() throws SQLException
    {
    	ensureOpen();
    	
        return (long)RemoteUtil.invoke(client, id, remoteInterface, "getLargeMaxRows", new Class<?>[]{}, new Object[]{}, long.class);
    }

    /** {@inheritDoc} */
    @Override
    public long[] executeLargeBatch() throws SQLException
    {
    	ensureOpen();
    	
        return (long[]) RemoteUtil.invoke(client, id, remoteInterface, "executeLargeBatch", new Class<?>[]{}, new Object[]{}, long[].class);
    }

    /** {@inheritDoc} */
    @Override
    public long executeLargeUpdate(String pSql) throws SQLException
    {
    	ensureOpen();
    	
        return (long)RemoteUtil.invoke(client, id, remoteInterface, "executeLargeUpdate", new Class<?>[]{String.class}, new Object[]{pSql}, long.class);
    }

    /** {@inheritDoc} */
    @Override
    public long executeLargeUpdate(String pSql, int pAutoGeneratedKeys) throws SQLException
    {
    	ensureOpen();
    	
        return (long)RemoteUtil.invoke(client, id, remoteInterface, "executeLargeUpdate", new Class<?>[]{String.class, int.class}, new Object[]{pSql, pAutoGeneratedKeys}, long.class);
    }

    /** {@inheritDoc} */
    @Override
    public long executeLargeUpdate(String pSql, int[] pColumnIndexes) throws SQLException
    {
    	ensureOpen();
    	
        return (long)RemoteUtil.invoke(client, id, remoteInterface, "executeLargeUpdate", new Class<?>[]{String.class, int[].class}, new Object[]{pSql, pColumnIndexes}, long.class);
    }

    /** {@inheritDoc} */
    @Override
    public long executeLargeUpdate(String pSql, String[] pColumnNames) throws SQLException
    {
    	ensureOpen();
    	
        return (long)RemoteUtil.invoke(client, id, remoteInterface, "executeLargeUpdate", new Class<?>[]{String.class, String[].class}, new Object[]{pSql, pColumnNames}, long.class);
    }

    /** {@inheritDoc} */
    @Override
    public String enquoteLiteral(String pVal) throws SQLException
    {
    	ensureOpen();
    	
        return (String)RemoteUtil.invoke(client, id, remoteInterface, "enquoteLiteral", new Class<?>[]{String.class}, new Object[]{pVal}, String.class);
    }

    /** {@inheritDoc} */
    @Override
    public String enquoteIdentifier(String pIdentifier, boolean pAlwaysQuote) throws SQLException
    {
    	ensureOpen();
    	
        return (String)RemoteUtil.invoke(client, id, remoteInterface, "enquoteIdentifier", new Class<?>[]{String.class, boolean.class}, new Object[]{pIdentifier, pAlwaysQuote}, String.class);
    }

    /** {@inheritDoc} */
    @Override
    public boolean isSimpleIdentifier(String pIdentifier) throws SQLException
    {
    	ensureOpen();
    	
        return (boolean)RemoteUtil.invoke(client, id, remoteInterface, "isSimpleIdentifier", new Class<?>[]{String.class}, new Object[]{pIdentifier}, boolean.class);
    }

    /** {@inheritDoc} */
    @Override
    public String enquoteNCharLiteral(String pVal) throws SQLException
    {
    	ensureOpen();
    	
        return (String)RemoteUtil.invoke(client, id, remoteInterface, "enquoteNCharLiteral", new Class<?>[]{String.class}, new Object[]{pVal}, String.class);
    }
    
    /**
     * Checks that the remote statement is still open.
     * 
     * @throws SQLException if the statement is closed
     */
    protected void ensureOpen() throws SQLException
    {
        if (closed)
        {
            throw new SQLException("Statement is closed");
        }
    }
}
