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

/**
 * Remote JDBC implementation of {@code Constants} functionality.
 * 
 * @author René Jahn
 */
interface RemoteConstants
{
    static final String ACTION = "action";
    static final String ID = "id";
    static final String URL = "url";
    static final String JDBC_URL = "jdbcUrl";
    static final String HTTP_REQUEST_TIMEOUT = "httpRequestTimeout";
    static final String USER = "user";
    static final String PASSWORD = "password";
    static final String PROPERTIES = "properties";
    static final String SQL = "sql";
    static final String INDEX = "index";
    static final String VALUE = "value";
    static final String VALUES = "values";
    static final String TYPE = "type";
    static final String FETCH = "fetch";
    static final String SUCCESS = "success";
    static final String RESULT = "result";
    static final String ROWS = "rows";
    static final String END = "end";
    static final String ERROR = "error";
    static final String SESSION_ID = "sessionId";
    static final String BYTES = "bytes";
    static final String POSITION = "position";
    static final String STREAM_KIND = "streamKind";
    static final String OPERATIONS = "operations";
    static final String TEXT = "text";
    
    static final String CONNECT = "connect";
    static final String CLOSE_SESSION = "closeSession";
    static final String CREATE_EXECUTOR = "createExecutor";
    static final String CLOSE = "close";
    static final String CREATE_STATEMENT = "createStatement";
    static final String PREPARE_STATEMENT = "prepareStatement";
    static final String SET_PARAMETER = "setParameter";
    static final String EXECUTE = "execute";
    static final String EXECUTE_QUERY = "executeQuery";
    static final String EXECUTE_UPDATE = "executeUpdate";
    static final String FETCH_ROWS = "fetchRows";
    static final String GET_OBJECT = "getObject";
    static final String GET_META_DATA = "getMetaData";
    static final String GET_COLUMN_INFO = "getColumnInfo";
    static final String GET_RESULTSET_METADATA_INFO = "getResultSetMetaDataInfo";
    static final String GET_DATABASE_META_DATA = "getDatabaseMetaData";
    static final String COMMIT = "commit";
    static final String ROLLBACK = "rollback";
    static final String SET_AUTO_COMMIT = "setAutoCommit";
    static final String GET_AUTO_COMMIT = "getAutoCommit";
    static final String SET_READ_ONLY = "setReadOnly";
    static final String IS_READ_ONLY = "isReadOnly";
    static final String SET_ISOLATION = "setTransactionIsolation";
    static final String GET_ISOLATION = "getTransactionIsolation";
    static final String GENERIC_CALL = "genericCall";
    static final String INTERFACE = "interface";
    static final String METHOD = "method";
    static final String PARAMETER_TYPES = "parameterTypes";
}
