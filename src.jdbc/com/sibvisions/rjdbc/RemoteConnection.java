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

import java.sql.Array;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.NClob;
import java.sql.PreparedStatement;
import java.sql.SQLClientInfoException;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Savepoint;
import java.sql.ShardingKey;
import java.sql.Statement;
import java.sql.Struct;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executor;

/**
 * Establishes the requested JDBC connection and registers it in the remote session.
 * 
 * @author René Jahn
 */
public class RemoteConnection implements Connection
{
    protected final RemoteClient client;
    
    protected final long id;
    
    protected final Class<?> remoteInterface;
    
    private volatile boolean closed;
    

    /**
     * Establishes a connection to the remote JDBC endpoint.
     *
     * @param pClient the client
     * @param pEndpoint the endpoint
     * @param pProperties the properties
     * @return the resulting JDBC value
     * @throws SQLException if the JDBC operation cannot be completed
     */
    static Connection connect(RemoteClient pClient, String pEndpoint, Properties pProperties) throws SQLException
    {
        Map<String,Object> req = new HashMap<>();
        req.put(RemoteConstants.ACTION, RemoteConstants.CONNECT);
        req.put(RemoteConstants.URL, pEndpoint);
        
        String jdbcUrl = pProperties.getProperty(RemoteConstants.JDBC_URL);

        if (jdbcUrl != null && !jdbcUrl.trim().isEmpty())
        {
            req.put(RemoteConstants.JDBC_URL, jdbcUrl);

        }
        
        Map<String,Object> remoteProperties = new HashMap<>();
        
        for (String name : pProperties.stringPropertyNames())
        {
            if (!RemoteConstants.JDBC_URL.equals(name)
                && !RemoteConstants.HTTP_REQUEST_TIMEOUT.equals(name))
            {
                remoteProperties.put(name, pProperties.getProperty(name));
            }
        }
        
        req.put(RemoteConstants.PROPERTIES, remoteProperties);
        
        Map<String,Object> response = pClient.call(req);
        
        long id;
        
        Object result = response.get(RemoteConstants.RESULT);

        if (result instanceof Map)
        {
            Map<?,?> connectionInfo = (Map<?,?>)result;
            
            id = RemoteUtil.number(connectionInfo.get(RemoteConstants.ID));
        }
        else
        {
            // Backward-compatible with a server that still returns the raw connection id.
            id = RemoteUtil.number(result);

        }

        return new RemoteConnection(pClient, id);
    }

    /**
     * Creates a new {@code RemoteConnection} instance.
     *
     * @param pClient the client
     * @param pId the remote resource identifier
     */
    public RemoteConnection(RemoteClient pClient, long pId)
    {
        this(pClient, pId, Connection.class);
    }

    /**
     * Creates a new {@code RemoteConnection} instance.
     *
     * @param pClient the client
     * @param pId the remote resource identifier
     * @param pRemoteInterface the remote interface
     */
    protected RemoteConnection(RemoteClient pClient, long pId, Class<?> pRemoteInterface)
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
    public Statement createStatement() throws SQLException
    {
    	ensureOpen();
    	
        return (Statement)RemoteUtil.invoke(client, id, remoteInterface, "createStatement", new Class<?>[]{}, new Object[]{}, Statement.class);
    }

    /** {@inheritDoc} */
    @Override
    public PreparedStatement prepareStatement(String pSql) throws SQLException
    {
    	ensureOpen();
    	
        return (PreparedStatement)RemoteUtil.invoke(client, id, remoteInterface, "prepareStatement", new Class<?>[]{String.class}, new Object[]{pSql}, PreparedStatement.class);
    }

    /** {@inheritDoc} */
    @Override
    public CallableStatement prepareCall(String pSql) throws SQLException
    {
    	ensureOpen();

    	return (CallableStatement)RemoteUtil.invoke(client, id, remoteInterface, "prepareCall", new Class<?>[]{String.class}, new Object[]{pSql}, CallableStatement.class);
    }

    /** {@inheritDoc} */
    @Override
    public String nativeSQL(String pSql) throws SQLException
    {
    	ensureOpen();
    	
        return (String)RemoteUtil.invoke(client, id, remoteInterface, "nativeSQL", new Class<?>[]{String.class}, new Object[]{pSql}, String.class);
    }

    /** {@inheritDoc} */
    @Override
    public void setAutoCommit(boolean pAutoCommit) throws SQLException
    {
    	ensureOpen();
    	
        RemoteUtil.invoke(client, id, remoteInterface, "setAutoCommit", new Class<?>[]{boolean.class}, new Object[]{pAutoCommit}, void.class);
    }

    /** {@inheritDoc} */
    @Override
    public boolean getAutoCommit() throws SQLException
    {
    	ensureOpen();
    	
        return (boolean)RemoteUtil.invoke(client, id, remoteInterface, "getAutoCommit", new Class<?>[]{}, new Object[]{}, boolean.class);
    }

    /** {@inheritDoc} */
    @Override
    public void commit() throws SQLException
    {
    	ensureOpen();
    	
        RemoteUtil.invoke(client, id, remoteInterface, "commit", new Class<?>[]{}, new Object[]{}, void.class);
    }

    /** {@inheritDoc} */
    @Override
    public void rollback() throws SQLException
    {
    	ensureOpen();
    	
        RemoteUtil.invoke(client, id, remoteInterface, "rollback", new Class<?>[]{}, new Object[]{}, void.class);
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
            if (remoteInterface == Connection.class)
            {
                client.closeSession();
            }
            else
            {
                RemoteUtil.invoke(client, id, remoteInterface, "close", new Class<?>[]{}, new Object[]{}, void.class);
            }

        }
        finally
        {
            closed = true;
        }

    }

    /** {@inheritDoc} */
    @Override
    public boolean isClosed() throws SQLException
    {
        if (closed || client.isSessionBroken())
        {
            return true;

        }

        return (boolean)RemoteUtil.invoke(client, id, remoteInterface, "isClosed", new Class<?>[]{}, new Object[]{}, boolean.class);
    }

    /** {@inheritDoc} */
    @Override
    public DatabaseMetaData getMetaData() throws SQLException
    {
    	ensureOpen();
    	
        return (DatabaseMetaData)RemoteUtil.invoke(client, id, remoteInterface, "getMetaData", new Class<?>[]{}, new Object[]{}, DatabaseMetaData.class);
    }

    /** {@inheritDoc} */
    @Override
    public void setReadOnly(boolean pReadOnly) throws SQLException
    {
    	ensureOpen();
    	
        RemoteUtil.invoke(client, id, remoteInterface, "setReadOnly", new Class<?>[]{boolean.class}, new Object[]{pReadOnly}, void.class);
    }

    /** {@inheritDoc} */
    @Override
    public boolean isReadOnly() throws SQLException
    {
    	ensureOpen();
    	
        return (boolean)RemoteUtil.invoke(client, id, remoteInterface, "isReadOnly", new Class<?>[]{}, new Object[]{}, boolean.class);
    }

    /** {@inheritDoc} */
    @Override
    public void setCatalog(String pCatalog) throws SQLException
    {
    	ensureOpen();
    	
        RemoteUtil.invoke(client, id, remoteInterface, "setCatalog", new Class<?>[]{String.class}, new Object[]{pCatalog}, void.class);
    }

    /** {@inheritDoc} */
    @Override
    public String getCatalog() throws SQLException
    {
    	ensureOpen();
    	
        return (String)RemoteUtil.invoke(client, id, remoteInterface, "getCatalog", new Class<?>[]{}, new Object[]{}, String.class);
    }

    /** {@inheritDoc} */
    @Override
    public void setTransactionIsolation(int pLevel) throws SQLException
    {
    	ensureOpen();
    	
        RemoteUtil.invoke(client, id, remoteInterface, "setTransactionIsolation", new Class<?>[]{int.class}, new Object[]{pLevel}, void.class);
    }

    /** {@inheritDoc} */
    @Override
    public int getTransactionIsolation() throws SQLException
    {
    	ensureOpen();
    	
        return (int)RemoteUtil.invoke(client, id, remoteInterface, "getTransactionIsolation", new Class<?>[]{}, new Object[]{}, int.class);
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
    public Statement createStatement(int pResultSetType, int pResultSetConcurrency) throws SQLException
    {
    	ensureOpen();
    	
        return (Statement)RemoteUtil.invoke(client, id, remoteInterface, "createStatement", new Class<?>[]{int.class, int.class}, new Object[]{pResultSetType, pResultSetConcurrency}, Statement.class);
    }

    /** {@inheritDoc} */
    @Override
    public PreparedStatement prepareStatement(String pSql, int pResultSetType, int pResultSetConcurrency) throws SQLException
    {
    	ensureOpen();
    	
        return (PreparedStatement)RemoteUtil.invoke(client, id, remoteInterface, "prepareStatement", new Class<?>[]{String.class, int.class, int.class}, new Object[]{pSql, pResultSetType, pResultSetConcurrency}, PreparedStatement.class);
    }

    /** {@inheritDoc} */
    @Override
    public CallableStatement prepareCall(String pSql, int pResultSetType, int pResultSetConcurrency) throws SQLException
    {
    	ensureOpen();
    	
        return (CallableStatement)RemoteUtil.invoke(client, id, remoteInterface, "prepareCall", new Class<?>[]{String.class, int.class, int.class}, new Object[]{pSql, pResultSetType, pResultSetConcurrency}, CallableStatement.class);
    }

    /** {@inheritDoc} */
    @SuppressWarnings("unchecked")
	@Override
    public Map<String,Class<?>> getTypeMap() throws SQLException
    {
    	ensureOpen();
    	
        return (Map<String,Class<?>>)RemoteUtil.invoke(client, id, remoteInterface, "getTypeMap", new Class<?>[]{}, new Object[]{}, Map.class);
    }

    /** {@inheritDoc} */
    @Override
    public void setTypeMap(Map<String,Class<?>> pMap) throws SQLException
    {
    	ensureOpen();
    	
        RemoteUtil.invoke(client, id, remoteInterface, "setTypeMap", new Class<?>[]{Map.class}, new Object[]{pMap}, void.class);
    }

    /** {@inheritDoc} */
    @Override
    public void setHoldability(int pHoldability) throws SQLException
    {
    	ensureOpen();
    	
        RemoteUtil.invoke(client, id, remoteInterface, "setHoldability", new Class<?>[]{int.class}, new Object[]{pHoldability}, void.class);
    }

    /** {@inheritDoc} */
    @Override
    public int getHoldability() throws SQLException
    {
    	ensureOpen();
    	
        return (int)RemoteUtil.invoke(client, id, remoteInterface, "getHoldability", new Class<?>[]{}, new Object[]{}, int.class);
    }

    /** {@inheritDoc} */
    @Override
    public Savepoint setSavepoint() throws SQLException
    {
    	ensureOpen();
    	
        return (Savepoint)RemoteUtil.invoke(client, id, remoteInterface, "setSavepoint", new Class<?>[]{}, new Object[]{}, Savepoint.class);
    }

    /** {@inheritDoc} */
    @Override
    public Savepoint setSavepoint(String pName) throws SQLException
    {
    	ensureOpen();
    	
        return (Savepoint)RemoteUtil.invoke(client, id, remoteInterface, "setSavepoint", new Class<?>[]{String.class}, new Object[]{pName}, Savepoint.class);
    }

    /** {@inheritDoc} */
    @Override
    public void rollback(Savepoint pSavepoint) throws SQLException
    {
    	ensureOpen();
    	
        RemoteUtil.invoke(client, id, remoteInterface, "rollback", new Class<?>[]{Savepoint.class}, new Object[]{pSavepoint}, void.class);
    }

    /** {@inheritDoc} */
    @Override
    public void releaseSavepoint(Savepoint pSavepoint) throws SQLException
    {
    	ensureOpen();
    	
        RemoteUtil.invoke(client, id, remoteInterface, "releaseSavepoint", new Class<?>[]{Savepoint.class}, new Object[]{pSavepoint}, void.class);
    }

    /** {@inheritDoc} */
    @Override
    public Statement createStatement(int pResultSetType, int pResultSetConcurrency, int pResultSetHoldability) throws SQLException
    {
    	ensureOpen();
    	
        return (Statement)RemoteUtil.invoke(client, id, remoteInterface, "createStatement", new Class<?>[]{int.class, int.class, int.class}, new Object[]{pResultSetType, pResultSetConcurrency, pResultSetHoldability}, Statement.class);
    }

    /** {@inheritDoc} */
    @Override
    public PreparedStatement prepareStatement(String pSql, int pResultSetType, int pResultSetConcurrency, int pResultSetHoldability) throws SQLException
    {
    	ensureOpen();
    	
        return (PreparedStatement)RemoteUtil.invoke(client, id, remoteInterface, "prepareStatement", new Class<?>[]{String.class, int.class, int.class, int.class}, new Object[]{pSql, pResultSetType, pResultSetConcurrency, pResultSetHoldability}, PreparedStatement.class);
    }

    /** {@inheritDoc} */
    @Override
    public CallableStatement prepareCall(String pSql, int pResultSetType, int pResultSetConcurrency, int pResultSetHoldability) throws SQLException
    {
    	ensureOpen();
    	
        return (CallableStatement)RemoteUtil.invoke(client, id, remoteInterface, "prepareCall", new Class<?>[]{String.class, int.class, int.class, int.class}, new Object[]{pSql, pResultSetType, pResultSetConcurrency, pResultSetHoldability}, CallableStatement.class);
    }

    /** {@inheritDoc} */
    @Override
    public PreparedStatement prepareStatement(String pSql, int pResultSetType) throws SQLException
    {
    	ensureOpen();
    	
        return (PreparedStatement)RemoteUtil.invoke(client, id, remoteInterface, "prepareStatement", new Class<?>[]{String.class, int.class}, new Object[]{pSql, pResultSetType}, PreparedStatement.class);
    }

    /** {@inheritDoc} */
    @Override
    public PreparedStatement prepareStatement(String pSql, int[] pColumnIndexes) throws SQLException
    {
    	ensureOpen();
    	
        return (PreparedStatement)RemoteUtil.invoke(client, id, remoteInterface, "prepareStatement", new Class<?>[]{String.class, int[].class}, new Object[]{pSql, pColumnIndexes}, PreparedStatement.class);
    }

    /** {@inheritDoc} */
    @Override
    public PreparedStatement prepareStatement(String pSql, String[] pColumnNames) throws SQLException
    {
    	ensureOpen();
    	
        return (PreparedStatement)RemoteUtil.invoke(client, id, remoteInterface, "prepareStatement", new Class<?>[]{String.class, String[].class}, new Object[]{pSql, pColumnNames}, PreparedStatement.class);
    }

    /** {@inheritDoc} */
    @Override
    public Clob createClob() throws SQLException
    {
    	ensureOpen();
    	
        return (Clob)RemoteUtil.invoke(client, id, remoteInterface, "createClob", new Class<?>[]{}, new Object[]{}, Clob.class);
    }

    /** {@inheritDoc} */
    @Override
    public Blob createBlob() throws SQLException
    {
    	ensureOpen();
    	
        return (Blob)RemoteUtil.invoke(client, id, remoteInterface, "createBlob", new Class<?>[]{}, new Object[]{}, Blob.class);
    }

    /** {@inheritDoc} */
    @Override
    public NClob createNClob() throws SQLException
    {
    	ensureOpen();
    	
        return (NClob)RemoteUtil.invoke(client, id, remoteInterface, "createNClob", new Class<?>[]{}, new Object[]{}, NClob.class);
    }

    /** {@inheritDoc} */
    @Override
    public SQLXML createSQLXML() throws SQLException
    {
    	ensureOpen();
    	
        return (SQLXML)RemoteUtil.invoke(client, id, remoteInterface, "createSQLXML", new Class<?>[]{}, new Object[]{}, SQLXML.class);
    }

    /** {@inheritDoc} */
    @Override
    public boolean isValid(int pTimeout) throws SQLException
    {
        if (pTimeout < 0)
        {
            throw new SQLException("Timeout value must be >= 0");
        }

        if (closed || client.isSessionClosed() || client.isSessionBroken())
        {
            return false;

        }
        try
        {
            return (boolean)RemoteUtil.invoke(client, id, remoteInterface, "isValid", new Class<?>[]{int.class}, new Object[]{pTimeout}, boolean.class);
        }
        catch (SQLException e)
        {
            return false;
        }

    }

    /** {@inheritDoc} */
    @Override
    public void setClientInfo(String pName, String pValue) throws SQLClientInfoException
    {
		ensureOpenClientInfo();
    	
        RemoteUtil.invokeClientInfo(client, id, remoteInterface, "setClientInfo", new Class<?>[]{String.class, String.class}, new Object[]{pName, pValue}, void.class);
    }

    /** {@inheritDoc} */
    @Override
    public void setClientInfo(Properties pClientInfo) throws SQLClientInfoException
    {
    	ensureOpenClientInfo();
    	
        RemoteUtil.invokeClientInfo(client, id, remoteInterface, "setClientInfo", new Class<?>[]{Properties.class}, new Object[]{pClientInfo}, void.class);
    }

    /** {@inheritDoc} */
    @Override
    public String getClientInfo(String pName) throws SQLException
    {
    	ensureOpen();
    	
        return (String)RemoteUtil.invoke(client, id, remoteInterface, "getClientInfo", new Class<?>[]{String.class}, new Object[]{pName}, String.class);
    }

    /** {@inheritDoc} */
    @Override
    public Properties getClientInfo() throws SQLException
    {
    	ensureOpen();
    	
        return (Properties)RemoteUtil.invoke(client, id, remoteInterface, "getClientInfo", new Class<?>[]{}, new Object[]{}, Properties.class);
    }

    /** {@inheritDoc} */
    @Override
    public Array createArrayOf(String pTypeName, Object[] pElements) throws SQLException
    {
    	ensureOpen();
    	
        return (Array)RemoteUtil.invoke(client, id, remoteInterface, "createArrayOf", new Class<?>[]{String.class, Object[].class}, new Object[]{pTypeName, pElements}, Array.class);
    }

    /** {@inheritDoc} */
    @Override
    public Struct createStruct(String pTypeName, Object[] pAttributes) throws SQLException
    {
    	ensureOpen();
    	
        return (Struct)RemoteUtil.invoke(client, id, remoteInterface, "createStruct", new Class<?>[]{String.class, Object[].class}, new Object[]{pTypeName, pAttributes}, Struct.class);
    }

    /** {@inheritDoc} */
    @Override
    public void setSchema(String pSchema) throws SQLException
    {
    	ensureOpen();
    	
        RemoteUtil.invoke(client, id, remoteInterface, "setSchema", new Class<?>[]{String.class}, new Object[]{pSchema}, void.class);
    }

    /** {@inheritDoc} */
    @Override
    public String getSchema() throws SQLException
    {
    	ensureOpen();
    	
        return (String)RemoteUtil.invoke(client, id, remoteInterface, "getSchema", new Class<?>[]{}, new Object[]{}, String.class);
    }

    /** {@inheritDoc} */
    @Override
    public void abort(Executor pExecutor) throws SQLException
    {
    	ensureOpen();
    	
        Executor remoteExecutor = pExecutor;

        if (!(pExecutor instanceof RemoteExecutor))
        {
            remoteExecutor = createRemoteExecutor();
        }
        
        RemoteUtil.invoke(client, id, remoteInterface, "abort", new Class<?>[]{Executor.class}, new Object[]{remoteExecutor}, void.class);
    }

    /** {@inheritDoc} */
    @Override
    public void setNetworkTimeout(Executor pExecutor, int pMilliseconds) throws SQLException
    {
    	ensureOpen();
    	
        Executor remoteExecutor = pExecutor;

        if (!(pExecutor instanceof RemoteExecutor))
        {
            remoteExecutor = createRemoteExecutor();
        }
        
        RemoteUtil.invoke(client, id, remoteInterface, "setNetworkTimeout", new Class<?>[]{Executor.class, int.class}, new Object[]{remoteExecutor, pMilliseconds}, void.class);
    }

    /** {@inheritDoc} */
    @Override
    public int getNetworkTimeout() throws SQLException
    {
    	ensureOpen();
    	
        return (int)RemoteUtil.invoke(client, id, remoteInterface, "getNetworkTimeout", new Class<?>[]{}, new Object[]{}, int.class);
    }

    /** {@inheritDoc} */
    @Override
    public void beginRequest() throws SQLException
    {
    	ensureOpen();
    	
        RemoteUtil.invoke(client, id, remoteInterface, "beginRequest", new Class<?>[]{}, new Object[]{}, void.class);
    }

    /** {@inheritDoc} */
    @Override
    public void endRequest() throws SQLException
    {
    	ensureOpen();
    	
        RemoteUtil.invoke(client, id, remoteInterface, "endRequest", new Class<?>[]{}, new Object[]{}, void.class);
    }

    /** {@inheritDoc} */
    @Override
    public boolean setShardingKeyIfValid(ShardingKey pShardingKey, ShardingKey pSuperShardingKey, int pTimeout) throws SQLException
    {
    	ensureOpen();
    	
        return (boolean)RemoteUtil.invoke(client, id, remoteInterface, "setShardingKeyIfValid", new Class<?>[]{ShardingKey.class, ShardingKey.class, int.class}, new Object[]{pShardingKey, pSuperShardingKey, pTimeout}, boolean.class);
    }

    /** {@inheritDoc} */
    @Override
    public boolean setShardingKeyIfValid(ShardingKey pShardingKey, int pTimeout) throws SQLException
    {
    	ensureOpen();
    	
        return (boolean)RemoteUtil.invoke(client, id, remoteInterface, "setShardingKeyIfValid", new Class<?>[]{ShardingKey.class, int.class}, new Object[]{pShardingKey, pTimeout}, boolean.class);
    }

    /** {@inheritDoc} */
    @Override
    public void setShardingKey(ShardingKey pShardingKey, ShardingKey pSuperShardingKey) throws SQLException
    {
    	ensureOpen();
    	
        RemoteUtil.invoke(client, id, remoteInterface, "setShardingKey", new Class<?>[]{ShardingKey.class, ShardingKey.class}, new Object[]{pShardingKey, pSuperShardingKey}, void.class);
    }

    /** {@inheritDoc} */
    @Override
    public void setShardingKey(ShardingKey pShardingKey) throws SQLException
    {
    	ensureOpen();
    	
        RemoteUtil.invoke(client, id, remoteInterface, "setShardingKey", new Class<?>[]{ShardingKey.class}, new Object[]{pShardingKey}, void.class);
    }

    /**
     * Checks that the remote connection is still open.
     * 
     * @throws SQLException if the connection is closed
     */
    private void ensureOpen() throws SQLException
    {
        if (closed)
        {
            throw new SQLException("Connection is closed");
        }
    }
    
    /**
     * Checks that the remote connection is still open.
     * 
     * @throws SQLClientInfoException if the connection is closed
     */
    private void ensureOpenClientInfo() throws SQLClientInfoException
    {
    	if (closed)
    	{
    		throw new SQLClientInfoException();
    	}
    }
    
	/**
	 * Handles the create remote executor operation for the remote JDBC resource.
	 * 
	 * @return the resulting JDBC value
	 * @throws SQLException if the JDBC operation cannot be completed
	 */
    public RemoteExecutor createRemoteExecutor() throws SQLException
    {
        Map<String,Object> request = new HashMap<>();
        request.put(RemoteConstants.ACTION, RemoteConstants.CREATE_EXECUTOR);
        request.put(RemoteConstants.ID, id);
        
        Object value = client.call(request).get(RemoteConstants.RESULT);

        return new RemoteExecutor(client, RemoteUtil.number(value), java.util.concurrent.ForkJoinPool.commonPool());
    }
}
