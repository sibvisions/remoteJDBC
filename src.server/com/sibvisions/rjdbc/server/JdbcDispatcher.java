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
package com.sibvisions.rjdbc.server;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.sql.Array;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.NClob;
import java.sql.ParameterMetaData;
import java.sql.PreparedStatement;
import java.sql.Ref;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.RowId;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.SQLXML;
import java.sql.Savepoint;
import java.sql.Statement;
import java.sql.Struct;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.ServiceLoader;

import com.sibvisions.rjdbc.server.delegate.ArrayDelegate;
import com.sibvisions.rjdbc.server.delegate.BlobDelegate;
import com.sibvisions.rjdbc.server.delegate.CallableStatementDelegate;
import com.sibvisions.rjdbc.server.delegate.ClobDelegate;
import com.sibvisions.rjdbc.server.delegate.ConnectionDelegate;
import com.sibvisions.rjdbc.server.delegate.DatabaseMetaDataDelegate;
import com.sibvisions.rjdbc.server.delegate.JdbcDelegate;
import com.sibvisions.rjdbc.server.delegate.NClobDelegate;
import com.sibvisions.rjdbc.server.delegate.ParameterMetaDataDelegate;
import com.sibvisions.rjdbc.server.delegate.PreparedStatementDelegate;
import com.sibvisions.rjdbc.server.delegate.RefDelegate;
import com.sibvisions.rjdbc.server.delegate.ResultSetDelegate;
import com.sibvisions.rjdbc.server.delegate.ResultSetMetaDataDelegate;
import com.sibvisions.rjdbc.server.delegate.RowIdDelegate;
import com.sibvisions.rjdbc.server.delegate.SQLXMLDelegate;
import com.sibvisions.rjdbc.server.delegate.SavepointDelegate;
import com.sibvisions.rjdbc.server.delegate.StatementDelegate;
import com.sibvisions.rjdbc.server.delegate.StructDelegate;
import com.sibvisions.util.log.Log;

/**
 * Provides the {@code jdbc dispatcher} functionality.
 * 
 * @author René Jahn
 */
final class JdbcDispatcher
{
    private static final int MAX_FETCH_ROWS = 10000;
    private static final int MAX_BATCH_SIZE = 10000;

    private final JdbcContext context;
    
    private final Map<String, JdbcDelegate> delegates = new HashMap<String, JdbcDelegate>();
    

    /**
     * Creates a new {@code JdbcDispatcher} instance.
     *
     * @param pContext the context
     */
    JdbcDispatcher(JdbcContext pContext)
    {
        context = pContext;
        
        delegates.put(Connection.class.getName(), new ConnectionDelegate(pContext));
        delegates.put(Statement.class.getName(), new StatementDelegate(pContext));
        delegates.put(PreparedStatement.class.getName(), new PreparedStatementDelegate(pContext));
        delegates.put(CallableStatement.class.getName(), new CallableStatementDelegate(pContext));
        delegates.put(ResultSet.class.getName(), new ResultSetDelegate(pContext));
        delegates.put(ResultSetMetaData.class.getName(), new ResultSetMetaDataDelegate(pContext));
        delegates.put(DatabaseMetaData.class.getName(), new DatabaseMetaDataDelegate(pContext));
        delegates.put(Blob.class.getName(), new BlobDelegate(pContext));
        delegates.put(Clob.class.getName(), new ClobDelegate(pContext));
        delegates.put(NClob.class.getName(), new NClobDelegate(pContext));
        delegates.put(SQLXML.class.getName(), new SQLXMLDelegate(pContext));
        delegates.put(Array.class.getName(), new ArrayDelegate(pContext));
        delegates.put(Struct.class.getName(), new StructDelegate(pContext));
        delegates.put(ParameterMetaData.class.getName(), new ParameterMetaDataDelegate(pContext));
        delegates.put(Ref.class.getName(), new RefDelegate(pContext));
        delegates.put(RowId.class.getName(), new RowIdDelegate(pContext));
        delegates.put(Savepoint.class.getName(), new SavepointDelegate(pContext));
    }

    /**
     * Executes through the remote JDBC protocol.
     *
     * @param pRequest the request
     * @return the resulting JDBC value
     */
    Map<String,Object> execute(Map<String,Object> pRequest)
    {
        try
        {
            return success(dispatch(pRequest));
        }
        catch (Throwable e)
        {
            return error(e);
        }
    }

    /**
     * Dispatches the request to the JDBC operation identified by the request data.
     *
     * @param pPayload the request data
     * @return the resulting JDBC value
     * @throws Exception if the operation fails
     */
    private Object dispatch(Map<String, Object> pPayload) throws Exception
    {
    	Log.debug(JdbcDispatcher.class, pPayload);
    	
        String action = (String)pPayload.get("action");

        if ("connect".equals(action))
        {
            return connect(pPayload);
        }

        if ("closeSession".equals(action))
        {
            throw new SQLFeatureNotSupportedException("closeSession must be handled by JdbcHttpHandler");
        }

        if ("createExecutor".equals(action))
        {
            long connectionId = number(pPayload.get("id"));
            
            context.connection(connectionId);

            return context.addExecutor();
        }

        long id = number(pPayload.get("id"));
        
        switch (action)
        {
            case "close": 
            	context.close(id); 
            	
            	return null;
            	
            case "createStatement": return context.addStatement(context.connection(id).createStatement());
            case "prepareStatement": return context.addStatement(context.connection(id).prepareStatement((String)pPayload.get("sql")));
            case "setParameter":
                ((PreparedStatement)context.statement(id)).setObject(number(pPayload.get("index"), int.class), pPayload.get("value"));

                return null;
                
            case "executeQuery":
            {
                Statement statement = context.statement(id);
                ResultSet rs = statement instanceof PreparedStatement ? ((PreparedStatement)statement).executeQuery() : statement.executeQuery((String)pPayload.get("sql"));

                return context.addResultSet(rs);
            }
            case "executeUpdate":
            {
                Statement statement = context.statement(id);

                if (statement instanceof PreparedStatement)
                {
                    return ((PreparedStatement)statement).executeUpdate();
                }
                
                String sql = (String)pPayload.get("sql");

                if (sql == null)
                {
                    throw new SQLException("SQL is required for a Statement");
                }

                return statement.executeUpdate(sql);
            }
            case "execute":
            {
                Statement statement = context.statement(id);

                if (statement instanceof PreparedStatement)
                {
                    return ((PreparedStatement)statement).execute();
                }
                
                String sql = (String)pPayload.get("sql");

                if (sql == null)
                {
                    throw new SQLException("SQL is required for a Statement");
                }

                return statement.execute(sql);
            }
            case "fetchRows": return fetchRows(context.resultSet(id), ((Number)pPayload.get("fetch")).intValue());
            case "fetchCurrentRow": return fetchCurrentRow(context.resultSet(id));
            case "getObject": return context.resultSet(id).getObject(((Number)pPayload.get("index")).intValue());
            case "getMetaData": return context.addResultSetMetadata(context.resultSet(id).getMetaData());
            case "getColumnInfo": return getColumnInfo(context.resultSet(id));
            case "getResultSetMetaDataInfo": return getResultSetMetaDataInfo(context.resultSetMetadata(id));
            case "getDatabaseMetaData": return context.addDatabaseMetadata(context.connection(id).getMetaData());
            case "commit": 
            	context.connection(id).commit(); 
            	
            	return null;
            	
            case "rollback": 
            	context.connection(id).rollback(); 
            	
            	return null;
            case "setAutoCommit": 
            	context.connection(id).setAutoCommit((Boolean)pPayload.get("value")); 
            	
            	return null;
            	
            case "getAutoCommit": return context.connection(id).getAutoCommit();
            case "setReadOnly": 
            	context.connection(id).setReadOnly((Boolean)pPayload.get("value")); 
            	
            	return null;
            	
            case "isReadOnly": return context.connection(id).isReadOnly();
            case "setTransactionIsolation": 
            	context.connection(id).setTransactionIsolation(((Number)pPayload.get("value")).intValue()); 
            	
            	return null;
            	
            case "getTransactionIsolation": return context.connection(id).getTransactionIsolation();
            case "genericCall": return directCall(pPayload);
            case "executePreparedBatch": return executePreparedBatch(pPayload);
            case "executePreparedLargeBatch": return executePreparedLargeBatch(pPayload);
            case "executePreparedQuery": return executePreparedQuery(pPayload);
            case "executePreparedUpdate": return executePreparedUpdate(pPayload);
            case "executePrepared": return executePrepared(pPayload);
            case "executePreparedLargeUpdate": return executePreparedLargeUpdate(pPayload);
            case "writeStream": return writeStream(pPayload);
                
            default: throw new SQLFeatureNotSupportedException("Unknown action: " + action);
        }

    }

    /**
     * Invokes a JDBC method using the method and parameter information in the request.
     *
     * @param pRequest the request
     * @return the resulting JDBC value
     * @throws SQLException if the JDBC operation cannot be completed
     */
    private Object directCall(Map<String,Object> pRequest) throws SQLException
    {
        long id = number(pRequest.get("id"));
        
        String interfaceName = (String)pRequest.get("interface");
        String method = (String)pRequest.get("method");
        String[] parameterTypes = stringArray(pRequest.get("parameterTypes"));
        String signature = method + "(" + normalizedParameterTypes(parameterTypes) + ")";

        Object[] args = decodeArguments(stringArray(pRequest.get("parameterTypes")), objectArray(pRequest.get("value")));
        
        JdbcDelegate delegate = delegates.get(interfaceName);

        if (delegate == null)
        {
            throw new SQLFeatureNotSupportedException("Unknown JDBC interface: " + interfaceName);
        }

        return delegate.call(id, signature, args);
    }

    /**
     * Builds the normalized parameter-type signature used for remote method dispatch.
     *
     * @param pParameterTypes the parameter types
     * @return the resulting JDBC value
     */
    private static String normalizedParameterTypes(String[] pParameterTypes)
    {
        StringBuilder result = new StringBuilder();
        
        for (int i = 0; i < pParameterTypes.length; i++)
        {
            if (i > 0)
            {
                result.append(",");
            }
            
            result.append(normalizeParameterType(pParameterTypes[i]));
        }

        return result.toString();
    }

    /**
     * Normalizes a Java parameter type for remote method dispatch.
     *
     * @param pType the requested Java type
     * @return the resulting JDBC value
     */
    private static String normalizeParameterType(String pType)
    {
        if (pType == null)
        {
            return "null";
        }

        if (pType.startsWith("["))
        {
            int dimensions = 0;
            
            while (dimensions < pType.length() && pType.charAt(dimensions) == '[') 
            {
            	dimensions++;
            }
            
            String base = pType.substring(dimensions);

            if (base.length() == 1)
            {
                switch (base.charAt(0))
                {
                    case 'Z': base = "boolean"; break;
                    case 'B': base = "byte"; break;
                    case 'C': base = "char"; break;
                    case 'S': base = "short"; break;
                    case 'I': base = "int"; break;
                    case 'J': base = "long"; break;
                    case 'F': base = "float"; break;
                    case 'D': base = "double"; break;
                    
                    default: break;
                }
            }
            else if (base.startsWith("L") && base.endsWith(";"))
            {
                base = base.substring(1, base.length() - 1);
            }

            return normalizeParameterType(base) + "[]".repeat(dimensions);
        }

        int dot = pType.lastIndexOf('.');

        return dot >= 0 ? pType.substring(dot + 1) : pType;
    }

    /**
     * Applies operations to given prepared statement identified by given id.
     *  
     * @param pId the statement identifier
     * @param pOperationsValue the operations value
     * @throws SQLException if apply fails
     */
    private void applyPreparedOperations(long pId, Object pOperationsValue) throws SQLException
    {
        if (!(pOperationsValue instanceof Iterable))
        {
            throw new SQLException("Prepared statement operations must be a collection");
        }

        PreparedStatement statement = (PreparedStatement)context.statement(pId);
        statement.clearParameters();
        
        for (Object operationValue : (Iterable<?>)pOperationsValue)
        {
            if (!(operationValue instanceof Map))
            {
                throw new SQLException("Prepared statement operation must be a Map");
            }

            Map<?, ?> operation = (Map<?, ?>)operationValue;
            
            String method = String.valueOf(operation.get("method"));
            String[] parameterTypes = stringArray(operation.get("parameterTypes"));
            String signature = method + "(" + normalizedParameterTypes(parameterTypes) + ")";

            Object[] args = objectArray(operation.get("value"));
            Object[] decoded = decodeArguments(parameterTypes, args);
            
            JdbcDelegate delegate = delegates.get(
                    statement instanceof CallableStatement
                            ? CallableStatement.class.getName()
                            : PreparedStatement.class.getName());

            if (delegate == null)
            {
                throw new SQLException("PreparedStatement delegate missing");
            }
            
            delegate.call(pId, signature, decoded);
        }
    }

    /**
     * Executes prepared query through the remote JDBC protocol.
     *
     * @param pRequest the request
     * @return the resulting JDBC value
     * @throws SQLException if the JDBC operation cannot be completed
     */
    private Object executePreparedQuery(Map<String,Object> pRequest) throws SQLException
    {
        long id = number(pRequest.get("id"));
        
        applyPreparedOperations(id, pRequest.get("operations"));

        return context.addResultSet(((PreparedStatement)context.statement(id)).executeQuery());
    }

    /**
     * Executes prepared update through the remote JDBC protocol.
     *
     * @param pRequest the request
     * @return the resulting JDBC value
     * @throws SQLException if the JDBC operation cannot be completed
     */
    private Object executePreparedUpdate(Map<String,Object> pRequest) throws SQLException
    {
        long id = number(pRequest.get("id"));
        
        applyPreparedOperations(id, pRequest.get("operations"));

        return ((PreparedStatement)context.statement(id)).executeUpdate();
    }

    /**
     * Executes prepared through the remote JDBC protocol.
     *
     * @param pRequest the request
     * @return the resulting JDBC value
     * @throws SQLException if the JDBC operation cannot be completed
     */
    private Object executePrepared(Map<String,Object> pRequest) throws SQLException
    {
        long id = number(pRequest.get("id"));
        
        applyPreparedOperations(id, pRequest.get("operations"));

        return ((PreparedStatement)context.statement(id)).execute();
    }

    /**
     * Executes prepared large update through the remote JDBC protocol.
     *
     * @param pRequest the request
     * @return the resulting JDBC value
     * @throws SQLException if the JDBC operation cannot be completed
     */
    private Object executePreparedLargeUpdate(Map<String,Object> pRequest) throws SQLException
    {
        long id = number(pRequest.get("id"));
        
        applyPreparedOperations(id, pRequest.get("operations"));

        return ((PreparedStatement)context.statement(id)).executeLargeUpdate();
    }

    /**
     * Executes prepared batch through the remote JDBC protocol.
     *
     * @param pRequest the request
     * @return the resulting JDBC value
     * @throws SQLException if the JDBC operation cannot be completed
     */
    private Object executePreparedBatch(Map<String,Object> pRequest) throws SQLException
    {
        long id = number(pRequest.get("id"));
        
        PreparedStatement statement = (PreparedStatement)context.statement(id);
        
        Object rowsValue = pRequest.get("rows");

        if (!(rowsValue instanceof Iterable))
        {
            throw new SQLException("Prepared batch rows must be a collection");
        }
        
        statement.clearBatch();
        
        int batchSize = 0;

        for (Object rowValue : (Iterable<?>)rowsValue)
        {
            if (++batchSize > MAX_BATCH_SIZE)
            {
                statement.clearBatch();
                
                throw new SQLException("Prepared batch exceeds maximum size of " + MAX_BATCH_SIZE);
            }

            applyPreparedOperations(id, rowValue);
            
            statement.addBatch();
        }

        return statement.executeBatch();
    }

    /**
     * Executes prepared large batch through the remote JDBC protocol.
     *
     * @param pRequest the request
     * @return the resulting JDBC value
     * @throws SQLException if the JDBC operation cannot be completed
     */
	private Object executePreparedLargeBatch(Map<String,Object> pRequest) throws SQLException
    {
        long id = number(pRequest.get("id"));
        
        PreparedStatement statement = (PreparedStatement)context.statement(id);
        
        Object rowsValue = pRequest.get("rows");

        if (!(rowsValue instanceof Iterable))
        {
            throw new SQLException("Prepared batch rows must be a collection");
        }
        
        statement.clearBatch();
        
        int batchSize = 0;

        for (Object rowValue : (Iterable<?>)rowsValue)
        {
            if (++batchSize > MAX_BATCH_SIZE)
            {
                statement.clearBatch();
                
                throw new SQLException("Prepared batch exceeds maximum size of " + MAX_BATCH_SIZE);
            }

            applyPreparedOperations(id, rowValue);
            
            statement.addBatch();
        }

        return statement.executeLargeBatch();
    }

    /**
     * Writes stream data to the corresponding remote JDBC value.
     *
     * @param pRequest the request
     * @return the resulting JDBC value
     * @throws SQLException if the JDBC operation cannot be completed
     */
	private Object writeStream(Map<String,Object> pRequest) throws SQLException
    {
        long id = number(pRequest.get("id"));
        
        String text = (String)pRequest.get("text");
        String kind = (String)pRequest.get("streamKind");

        Object position = pRequest.get("position");
        
        byte[] bytes = (byte[]) pRequest.get("bytes");
        
        
        try
        {
            if ("blob".equals(kind))
            {
                OutputStream stream = context.blob(id).setBinaryStream(((Number)position).longValue());

                if (bytes != null)
                {
                    stream.write(bytes);
                }
                
                stream.close();

                return null;
            }

            if ("clobAscii".equals(kind) || "nclobAscii".equals(kind))
            {
                OutputStream stream = ("nclobAscii".equals(kind)
            		? context.nclob(id).setAsciiStream(((Number)position).longValue())
            		: context.clob(id).setAsciiStream(((Number)position).longValue())
        		);

                if (bytes != null)
                {
                    stream.write(bytes);
                }
                
                stream.close();

                return null;
            }

            if ("clobCharacter".equals(kind) || "nclobCharacter".equals(kind))
            {
                Writer stream = ("nclobCharacter".equals(kind)
	                ? context.nclob(id).setCharacterStream(((Number)position).longValue())
	                : context.clob(id).setCharacterStream(((Number)position).longValue())
                );

                if (text != null)
                {
                    stream.write(text);
                }
                
                stream.close();

                return null;
            }

            if ("sqlxmlBinary".equals(kind))
            {
                OutputStream stream = context.sqlxml(id).setBinaryStream();

                if (bytes != null)
                {
                    stream.write(bytes);
                }
                
                stream.close();

                return null;
            }

            if ("sqlxmlCharacter".equals(kind))
            {
                Writer stream = context.sqlxml(id).setCharacterStream();

                if (text != null)
                {
                    stream.write(text);
                }
                
                stream.close();

                return null;
            }
        }
        catch (IOException e)
        {
            throw new SQLException(e);
        }
        
        throw new SQLFeatureNotSupportedException("Unknown remote stream: " + kind);
    }

    /**
     * Decodes protocol arguments into Java values for a remote JDBC method invocation.
     *
     * @param pParameterTypes the parameter types
     * @param pArgs the method arguments
     * @return the resulting JDBC value
     * @throws SQLException if the JDBC operation cannot be completed
     */
	private Object[] decodeArguments(String[] pParameterTypes, Object[] pArgs) throws SQLException
    {
        Object[] decoded = new Object[pArgs.length];
        
        for (int i = 0; i < pArgs.length; i++)
        {
            String type = pParameterTypes[i];

            Object value = pArgs[i];

            if (value == null)
            {
                decoded[i] = null; 
            }
            else
            {
	            if (InputStream.class.getName().equals(type))
	            {
	                decoded[i] = new java.io.ByteArrayInputStream((byte[]) value);
	            }
	            else if (Reader.class.getName().equals(type))
	            {
	                decoded[i] = new java.io.StringReader((String)value);
	            }
	            else if (Blob.class.getName().equals(type))
	            {
	                decoded[i] = context.blob(number(value));
	            }
	            else if (Clob.class.getName().equals(type))
	            {
	                decoded[i] = context.clob(number(value));
	            }
	            else if (NClob.class.getName().equals(type))
	            {
	                decoded[i] = context.nclob(number(value));
	            }
	            else if (SQLXML.class.getName().equals(type))
	            {
	                decoded[i] = context.sqlxml(number(value));
	            }
	            else if (Array.class.getName().equals(type))
	            {
	                decoded[i] = context.array(number(value));
	            }
	            else if (Struct.class.getName().equals(type))
	            {
	                decoded[i] = context.struct(number(value));
	            }
	            else if (ParameterMetaData.class.getName().equals(type))
	            {
	                decoded[i] = context.parameterMetaData(number(value));
	            }
	            else if (Ref.class.getName().equals(type))
	            {
	                decoded[i] = context.ref(number(value));
	            }
	            else if (RowId.class.getName().equals(type))
	            {
	                decoded[i] = context.rowId(number(value));
	            }
	            else if (Savepoint.class.getName().equals(type))
	            {
	                decoded[i] = context.savepoint(number(value));
	            }
	            else if (java.util.concurrent.Executor.class.getName().equals(type))
	            {
	                decoded[i] = context.executor(number(value));
	            }
	            else
	            {
	                decoded[i] = value;
	            }
            }

        }

        return decoded;
    }

    /**
     * Establishes the requested JDBC connection and registers it in the remote session.
     *
     * @param pPayload the request data
     * @return the resulting JDBC value
     * @throws SQLException if the JDBC operation cannot be completed
     */
	private Map<String,Object> connect(Map<String,Object> pPayload) throws SQLException
    {
        String url = (String)pPayload.get("jdbcUrl");
        
        //sent from client
        if (url != null && !url.trim().isEmpty())
        {
            if (!context.isJdbcUrlAllowed(url))
            {
                throw new SQLException("JDBC URL is not allowed: " + url);
            }
        }
        else
        {
            url = context.getJdbcUrl();

            if (url == null || url.isEmpty())
            {
                throw new SQLException("No JDBC URL configured on the client or server");
            }
        }
        
ServiceLoader<Driver> drivers = ServiceLoader.load(Driver.class, Thread.currentThread().getContextClassLoader());

for (Driver driver : drivers)
{
    //already registered
}
        

        Properties properties = new Properties();
        
        Object value = pPayload.get("properties");

        if (value instanceof Map)
        {
            Map<?,?> values = (Map<?,?>)value;
            
            for (Map.Entry<?,?> entry : values.entrySet()) 
            {
            	if (entry.getKey() != null && entry.getValue() != null)
            	{
            		properties.put(String.valueOf(entry.getKey()), String.valueOf(entry.getValue()));
            	}
            }
        }
        
        long connectionId = context.addConnection(DriverManager.getConnection(url, properties));
        
        Map<String,Object> result = new HashMap<>();
        result.put("id", connectionId);

        return result;
    }

    /**
     * Extracts column metadata required by the remote result-set protocol.
     *
     * @param pResultSet the JDBC result set
     * @return the resulting JDBC value
     * @throws SQLException if the JDBC operation cannot be completed
     */
	private Map<String,Object> getColumnInfo(ResultSet pResultSet) throws SQLException
    {
        ResultSetMetaData metaData = pResultSet.getMetaData();
        
        int count = metaData.getColumnCount();

        if (count < 0)
        {
            throw new SQLException("Invalid ResultSetMetaData column count: " + count);
        }
        
        String[] labels = new String[count];
        
        String[] names = new String[count];
        for (int i = 1; i <= count; i++)
        {
            labels[i - 1] = metaData.getColumnLabel(i);
            names[i - 1] = metaData.getColumnName(i);
        }

        Map<String,Object> result = new HashMap<>();
        result.put("count", count);
        result.put("labels", labels);
        result.put("names", names);

        return result;
    }

    /**
     * Extracts result-set metadata into the protocol representation sent to the client.
     *
     * @param pMetaData the result-set metadata
     * @return the resulting JDBC value
     * @throws SQLException if the JDBC operation cannot be completed
     */
	private Map<String,Object> getResultSetMetaDataInfo(ResultSetMetaData pMetaData) throws SQLException
    {
        int count = pMetaData.getColumnCount();

        if (count < 0)
        {
            throw new SQLException("Invalid ResultSetMetaData column count: " + count);
        }
        
        Object[][] columns = new Object[count][];
        
        for (int i = 1; i <= count; i++)
        {
            columns[i - 1] = new Object[] 
    		{
	            pMetaData.isAutoIncrement(i),
	            pMetaData.isCaseSensitive(i),
	            pMetaData.isSearchable(i),
	            pMetaData.isCurrency(i),
	            pMetaData.isNullable(i),
	            pMetaData.isSigned(i),
	            pMetaData.getColumnDisplaySize(i),
	            pMetaData.getColumnLabel(i),
	            pMetaData.getColumnName(i),
	            pMetaData.getSchemaName(i),
	            pMetaData.getPrecision(i),
	            pMetaData.getScale(i),
	            pMetaData.getTableName(i),
	            pMetaData.getCatalogName(i),
	            pMetaData.getColumnType(i),
	            pMetaData.getColumnTypeName(i),
	            pMetaData.isReadOnly(i),
	            pMetaData.isWritable(i),
	            pMetaData.isDefinitelyWritable(i),
	            pMetaData.getColumnClassName(i)
	        };
        }

        Map<String,Object> result = new HashMap<>();
        result.put("count", count);
        result.put("columns", columns);

        return result;
    }

    /**
     * Reads the current result-set row and converts it to the remote protocol representation.
     *
     * @param pResultSet the JDBC result set
     * @return the resulting JDBC value
     * @throws SQLException if the JDBC operation cannot be completed
     */
	private Map<String,Object> fetchCurrentRow(ResultSet pResultSet) throws SQLException
    {
        ResultSetMetaData metadata = pResultSet.getMetaData();
        
        int columns = metadata.getColumnCount();
        
        Object[] row;
        
        if (columns >= 0)
        {
	        int[] sqlTypes = new int[columns];
	        
	        String[] typeNames = new String[columns];
	
	        for (int i = 1; i <= columns; i++)
	        {
	            sqlTypes[i - 1] = metadata.getColumnType(i);
	            typeNames[i - 1] = metadata.getColumnTypeName(i);
	        }
	
	        ResultSetDelegate delegate = new ResultSetDelegate(context);
	        
	        row = new Object[columns];
	        
	        for (int i = 1; i <= columns; i++)
	        {
	            row[i - 1] = delegate.remoteResultSetValue(pResultSet, i, pResultSet.getObject(i), sqlTypes[i - 1], typeNames[i - 1]);
	        }
        }
        else
        {
        	if (pResultSet.next())
        	{
        		throw new SQLException("Invalid ResultSetMetaData: getColumnCount() returned " + columns + " although the ResultSet contains a row");
        	}
        	else
        	{
        		row = null;
        	}
        }
        
        Map<String,Object> result = new HashMap<>();
        result.put("rows", row != null ? new Object[][] {row} : new Object[1][0]);

        return result;
    }

    /**
     * Fetches up to the requested number of rows from the result set for transmission to the client.
     *
     * @param pResultSet the JDBC result set
     * @param pMax the maximum number of rows
     * @return the resulting JDBC value
     * @throws SQLException if the JDBC operation cannot be completed
     */
	private Map<String,Object> fetchRows(ResultSet pResultSet, int pMax) throws SQLException
    {
		int fetch = Math.min(Math.max(1, pMax), MAX_FETCH_ROWS);

		ResultSetMetaData metadata = pResultSet.getMetaData();
        
        int columns = metadata.getColumnCount();

        List<Object[]> rows;
        
		// Some JDBC drivers may return a synthetic empty ResultSet from
		// executeQuery() for statements that do not produce a tabular
		// result (like ALTER SESSION SET ... in Oracle). 
		// Such ResultSets may report a negative column count.
        //
		// We don't build column metadata in this case.
        if (columns >= 0)
        {
			rows = new ArrayList<>(fetch + 1);
        
	        int[] sqlTypes = new int[columns];
	        
	        String[] typeNames = new String[columns];
	
	        for (int i = 1; i <= columns; i++)
	        {
	            sqlTypes[i - 1] = metadata.getColumnType(i);
	            typeNames[i - 1] = metadata.getColumnTypeName(i);
	        }
	
	        ResultSetDelegate delegate = new ResultSetDelegate(context);
	
	        // Read one extra row. This lets the client know whether the current
	        // block contains the end of the ResultSet without requiring a second
	        // network round-trip. The extra row is kept in the client cache and
	        // is therefore not lost.
	        while (rows.size() <= fetch && pResultSet.next())
	        {
	            Object[] row = new Object[columns];
	            
	            for (int i=1; i<=columns; i++)
	            {
	                row[i - 1] = delegate.remoteResultSetValue(pResultSet, i, pResultSet.getObject(i), sqlTypes[i - 1], typeNames[i - 1]);
	            }
	            
	            rows.add(row);
	        }
        }
        else
        {
        	if (pResultSet.next())
        	{
        		throw new SQLException("Invalid ResultSetMetaData: getColumnCount() returned " + columns + " although the ResultSet contains a row");
        	}
        	else
        	{
        		rows = null;	
        	}
        }

        Map<String,Object> result = new HashMap<>();
        result.put("rows", rows != null ? rows.toArray(new Object[rows.size()][]) : new Object[0][]);
        result.put("endOfRows", rows == null || rows.size() <= fetch);

        return result;
    }

    /**
     * Converts a protocol value to a string array.
     *
     * @param pValue the value to convert
     * @return the resulting JDBC value
     */
	private static String[] stringArray(Object pValue)
    {
        if (pValue == null)
        {
            return new String[0];
        }
        else if (pValue instanceof String[])
        {
            return (String[])pValue;
        }
        
        Object[] values = (Object[])pValue;
        String[] result = new String[values.length];
        
        for (int i=0; i<values.length; i++) 
        {
        	result[i] = String.valueOf(values[i]);
        }

        return result;
    }

    /**
     * Converts a protocol value to an object array.
     *
     * @param pValue the value to convert
     * @return the resulting JDBC value
     */
	private static Object[] objectArray(Object pValue)
    {
        return pValue == null ? new Object[0] : (Object[])pValue;
    }

    /**
     * Converts a protocol value to a numeric value.
     *
     * @param pValue the value to convert
     * @return the resulting JDBC value
     */
	private static long number(Object pValue)
    {
        return ((Number)pValue).longValue();
    }

    /**
     * Converts a protocol value to a numeric value.
     *
     * @param pValue the value to convert
     * @param pType the requested Java type
     * @return the resulting JDBC value
     */
	private static int number(Object pValue, Class<?> pType)
    {
        return ((Number)pValue).intValue();
    }

    /**
     * Builds the successful response envelope for a remote JDBC operation.
     *
     * @param pValue the value to convert
     * @return the resulting JDBC value
     */
	@SuppressWarnings("unchecked")
	private static Map<String,Object> success(Object pValue)
    {
        Map<String,Object> result = new HashMap<>();
        result.put("success", true); 
        result.put("result", pValue);

        if (pValue instanceof Map)
        {
        	result.putAll((Map<String,Object>)pValue);
        }

        return result;
    }

    /**
     * Builds the error response envelope for a failed remote JDBC operation.
     *
     * @param pCause the exception to encode
     * @return the resulting JDBC value
     */
	private static Map<String,Object> error(Throwable pCause)
    {
        Map<String,Object> result = new HashMap<>();
        result.put("success", false); 
        result.put("error", pCause instanceof SQLException ? pCause : new SQLException(pCause));

        Log.debug(JdbcDispatcher.class, pCause);
        
        return result;
    }
}
