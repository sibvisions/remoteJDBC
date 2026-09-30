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
import java.sql.Savepoint;
import java.sql.ShardingKey;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executor;

import com.sibvisions.rjdbc.server.JdbcContext;

/**
 * Server-side delegate for {@code Connection} JDBC operations.
 * 
 * @author René Jahn
 */
public final class ConnectionDelegate extends JdbcDelegate
{
    /**
     * Creates a new {@code ConnectionDelegate} instance.
     *
     * @param pContext the context
     */
    public ConnectionDelegate(JdbcContext pContext)
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
    @SuppressWarnings({ "unchecked", "rawtypes" })
	public Object call(long pId, String pSignature, Object[] pArgs) throws SQLException
    {
        switch (pSignature)
        {
            case "abort(Executor)":
                context.connection(pId).abort((Executor)pArgs[0]);

                return null;
            case "beginRequest()":
                context.connection(pId).beginRequest();

                return null;
            case "clearWarnings()":
                context.connection(pId).clearWarnings();

                return null;
            case "close()":
                context.connection(pId).close();

                return null;
            case "commit()":
                context.connection(pId).commit();

                return null;
            case "createArrayOf(String,Object[])":

                return remoteValue(context.connection(pId).createArrayOf((String)pArgs[0], (Object[])pArgs[1]));
            case "createBlob()":

                return remoteValue(context.connection(pId).createBlob());
            case "createClob()":

                return remoteValue(context.connection(pId).createClob());
            case "createNClob()":

                return remoteValue(context.connection(pId).createNClob());
            case "createSQLXML()":

                return remoteValue(context.connection(pId).createSQLXML());
            case "createStatement()":

                return remoteValue(context.connection(pId).createStatement());
            case "createStatement(int,int)":

                return remoteValue(context.connection(pId).createStatement((int)pArgs[0], (int)pArgs[1]));
            case "createStatement(int,int,int)":

                return remoteValue(context.connection(pId).createStatement((int)pArgs[0], (int)pArgs[1], (int)pArgs[2]));
            case "createStruct(String,Object[])":

                return remoteValue(context.connection(pId).createStruct((String)pArgs[0], (Object[])pArgs[1]));
            case "endRequest()":
                context.connection(pId).endRequest();

                return null;
            case "getAutoCommit()":

                return remoteValue(context.connection(pId).getAutoCommit());
            case "getCatalog()":

                return remoteValue(context.connection(pId).getCatalog());
            case "getClientInfo()":

                return remoteValue(context.connection(pId).getClientInfo());
            case "getClientInfo(String)":

                return remoteValue(context.connection(pId).getClientInfo((String)pArgs[0]));
            case "getHoldability()":

                return remoteValue(context.connection(pId).getHoldability());
            case "getMetaData()":

                return remoteValue(context.connection(pId).getMetaData());
            case "getNetworkTimeout()":

                return remoteValue(context.connection(pId).getNetworkTimeout());
            case "getSchema()":

                return remoteValue(context.connection(pId).getSchema());
            case "getTransactionIsolation()":

                return remoteValue(context.connection(pId).getTransactionIsolation());
            case "getTypeMap()":

                return remoteValue(context.connection(pId).getTypeMap());
            case "getWarnings()":

                return remoteValue(context.connection(pId).getWarnings());
            case "isClosed()":

                return remoteValue(context.connection(pId).isClosed());
            case "isReadOnly()":

                return remoteValue(context.connection(pId).isReadOnly());
            case "isValid(int)":

                return remoteValue(context.connection(pId).isValid((int)pArgs[0]));
            case "nativeSQL(String)":

                return remoteValue(context.connection(pId).nativeSQL((String)pArgs[0]));
            case "prepareCall(String)":

                return remoteValue(context.connection(pId).prepareCall((String)pArgs[0]));
            case "prepareCall(String,int,int)":

                return remoteValue(context.connection(pId).prepareCall((String)pArgs[0], (int)pArgs[1], (int)pArgs[2]));
            case "prepareCall(String,int,int,int)":

                return remoteValue(context.connection(pId).prepareCall((String)pArgs[0], (int)pArgs[1], (int)pArgs[2], (int)pArgs[3]));
            case "prepareStatement(String)":

                return remoteValue(context.connection(pId).prepareStatement((String)pArgs[0]));
            case "prepareStatement(String,String[])":

                return remoteValue(context.connection(pId).prepareStatement((String)pArgs[0], (String[])pArgs[1]));
            case "prepareStatement(String,int)":

                return remoteValue(context.connection(pId).prepareStatement((String)pArgs[0], (int)pArgs[1]));
            case "prepareStatement(String,int,int)":

                return remoteValue(context.connection(pId).prepareStatement((String)pArgs[0], (int)pArgs[1], (int)pArgs[2]));
            case "prepareStatement(String,int,int,int)":

                return remoteValue(context.connection(pId).prepareStatement((String)pArgs[0], (int)pArgs[1], (int)pArgs[2], (int)pArgs[3]));
            case "prepareStatement(String,int[])":

                return remoteValue(context.connection(pId).prepareStatement((String)pArgs[0], (int[]) pArgs[1]));
            case "releaseSavepoint(Savepoint)":
                context.connection(pId).releaseSavepoint((Savepoint)pArgs[0]);
                
                long savepointId = context.idFor(pArgs[0]);

                if (savepointId != 0)
                {
                    context.removeSavepoint(savepointId);

                }

                return null;
            case "rollback()":
                context.connection(pId).rollback();

                return null;
            case "rollback(Savepoint)":
                context.connection(pId).rollback((Savepoint)pArgs[0]);

                return null;
            case "setAutoCommit(boolean)":
                context.connection(pId).setAutoCommit((boolean)pArgs[0]);

                return null;
            case "setCatalog(String)":
                context.connection(pId).setCatalog((String)pArgs[0]);

                return null;
            case "setClientInfo(Properties)":
                context.connection(pId).setClientInfo((Properties)pArgs[0]);

                return null;
            case "setClientInfo(String,String)":
                context.connection(pId).setClientInfo((String)pArgs[0], (String)pArgs[1]);

                return null;
            case "setHoldability(int)":
                context.connection(pId).setHoldability((int)pArgs[0]);

                return null;
            case "setNetworkTimeout(Executor,int)":
                context.connection(pId).setNetworkTimeout((Executor)pArgs[0], (int)pArgs[1]);

                return null;
            case "setReadOnly(boolean)":
                context.connection(pId).setReadOnly((boolean)pArgs[0]);

                return null;
            case "setSavepoint()":

                return remoteValue(context.connection(pId).setSavepoint());
            case "setSavepoint(String)":

                return remoteValue(context.connection(pId).setSavepoint((String)pArgs[0]));
            case "setSchema(String)":
                context.connection(pId).setSchema((String)pArgs[0]);

                return null;
            case "setShardingKey(ShardingKey)":
                context.connection(pId).setShardingKey((ShardingKey)pArgs[0]);

                return null;
            case "setShardingKey(ShardingKey,ShardingKey)":
                context.connection(pId).setShardingKey((ShardingKey)pArgs[0], (ShardingKey)pArgs[1]);

                return null;
            case "setShardingKeyIfValid(ShardingKey,ShardingKey,int)":

                return remoteValue(context.connection(pId).setShardingKeyIfValid((ShardingKey)pArgs[0], (ShardingKey)pArgs[1], (int)pArgs[2]));
            case "setShardingKeyIfValid(ShardingKey,int)":

                return remoteValue(context.connection(pId).setShardingKeyIfValid((ShardingKey)pArgs[0], (int)pArgs[1]));
            case "setTransactionIsolation(int)":
                context.connection(pId).setTransactionIsolation((int)pArgs[0]);

                return null;
            case "setTypeMap(Map)":
                context.connection(pId).setTypeMap((Map)pArgs[0]);

                return null;
                
            default:
                throw new SQLFeatureNotSupportedException("Unsupported JDBC method: " + pSignature);
        }
    }
}
