# Remote JDBC

Remote JDBC is a lightweight **Type-3 remote JDBC driver** for Java 11+.

It allows a Java application to use the standard `java.sql` API while the actual JDBC connection, statements, result sets and JDBC resources live on a remote server.

The client communicates with the server over HTTP using a compact binary request/response format based on JVx `UniversalSerializer`.



## Features

### JDBC driver

- Standard `java.sql.Driver`
- Automatic driver registration through `META-INF/services/java.sql.Driver`
- JDBC URL prefix:
  ```text
  jdbc:rjdbc:
  ```
- Java 11+
- Standard `DriverManager` integration
- Driver properties and connection properties
- `Connection.isValid(...)`
- Connection/Session lifecycle handling

Example:

```java
Connection connection = DriverManager.getConnection(
    "jdbc:rjdbc:https://server.example/app/jdbc",
    "user",
    "password"
);
```



## Remote architecture

Remote JDBC separates the JDBC client from the actual database connection:

```text
Application
    |
    | standard java.sql API
    v
rjdbc Driver
    |
    | HTTP POST (binary UniversalSerializer payload)
    v
rjdbc Servlet
    |
    | JDBC API
    v
Database
```

The client does not need a JDBC driver for the target database.

The target JDBC driver is installed on the application server and is used by the server-side JDBC implementation.

### Sessions

Each remote connection gets its own server-side session.

A session contains its own:

- JDBC `Connection`
- `Statement` / `PreparedStatement` / `CallableStatement`
- `ResultSet`
- metadata objects
- LOB objects
- SQLXML
- arrays / structs
- refs
- row IDs
- savepoints
- executors

Session IDs are generated server-side and are required for subsequent requests.

Sessions are isolated from each other and can be cleaned up after an idle timeout.



# Supported JDBC API

Remote JDBC contains client and server implementations for the following JDBC object types:

- `Connection`
- `Statement`
- `PreparedStatement`
- `CallableStatement`
- `ResultSet`
- `ResultSetMetaData`
- `DatabaseMetaData`
- `ParameterMetaData`
- `Blob`
- `Clob`
- `NClob`
- `SQLXML`
- `Array`
- `Struct`
- `Ref`
- `RowId`
- `Savepoint`

The implementation uses concrete remote JDBC classes rather than Java dynamic proxies.

Unsupported or not-yet-implemented JDBC operations are reported using JDBC exceptions such as `SQLFeatureNotSupportedException` where appropriate.



## Connection

Implemented and tested functionality includes:

- opening and closing connections
- `isClosed()`
- `isValid()`
- auto-commit
- commit / rollback
- read-only mode
- transaction isolation
- catalog
- schema
- holdability
- client information
- network timeout
- native SQL
- savepoints
- executor / abort handling
- request lifecycle
- wrapper support
- closed-connection state handling

Example:

```java
try (Connection connection =
         DriverManager.getConnection(
             "jdbc:rjdbc:https://server.example/app/jdbc",
             "user",
             "password")) {

    connection.setAutoCommit(false);

    // JDBC operations...

    connection.commit();
}
```



# Statements

## Statement

Supports the normal JDBC statement lifecycle, including:

- `executeQuery`
- `executeUpdate`
- `execute`
- result retrieval
- generated-key related JDBC APIs where supported by the underlying driver
- fetch size / related statement configuration
- max rows
- max field size
- query timeout
- warnings
- poolable state
- close-on-completion
- result-set handling
- batch execution
- multiple-result handling
- wrapper support
- lifecycle and closed-state checks

## PreparedStatement

Supports:

- parameter binding
- primitive parameter types
- `String`
- `BigDecimal`
- binary values
- dates / times / timestamps
- streams
- readers / writers
- LOB parameters
- parameter metadata
- batch execution
- prepared queries
- prepared updates
- prepared `execute`
- large-update APIs where supported
- parameter reuse
- parameter clearing
- lifecycle handling

`BigDecimal` values retain their scale through the remote serialization path. Tests compare decimal values numerically where the underlying database is allowed to return a different scale.

## CallableStatement

The remote implementation supports the JDBC `CallableStatement` API through the same remote delegate mechanism.



# ResultSet

Result sets are handled remotely with a **client-side row cache and block fetching**.

Instead of transferring the entire result set at once, rows are fetched in blocks.

This provides:

- remote row fetching
- reduced request frequency
- support for large result sets
- client-side cursor state
- correct handling of end-of-result detection
- local `wasNull()` handling
- column access by index and label
- result-set metadata
- updateable result-set operations where supported
- cursor navigation

Navigation covered by the implementation/tests includes:

- `next()`
- `previous()`
- `first()`
- `last()`
- `absolute()`
- `relative()`
- `beforeFirst()`
- `afterLast()`
- `isBeforeFirst()`
- `isAfterLast()`
- `isFirst()`
- `isLast()`
- `getRow()`

Empty result sets have explicit cursor state handling so that empty results are neither incorrectly reported as before-first nor after-last.

The implementation also handles the difference between the server-side JDBC cursor and the client-side row cache when navigating backwards or repositioning the cursor.



# ResultSet updates

The remote result-set implementation supports JDBC update operations where the underlying database/result set supports them, including the update/refresh lifecycle.

Client-side row state is refreshed after remote row updates so that subsequent getters observe the updated values.



# Streaming and LOBs

RJdbc includes support for remote JDBC stream and LOB operations.

Supported JDBC types include:

- `Blob`
- `Clob`
- `NClob`
- `SQLXML`

The implementation contains remote stream/reader/writer handling and explicit lifecycle management.

CLOB prefetching can be configured on the server.

Default:

```text
clobPrefetchSize = 102400
```

The value is measured in characters and CLOBs with more than configured characters will be loaded on-demand. 



# Special JDBC types

Remote representations are implemented for:

- `Array`
- `Struct`
- `Ref`
- `RowId`
- `Savepoint`
- `SQLXML`
- `Blob`
- `Clob`
- `NClob`

Their server-side JDBC resources are tracked by the session context and released with the corresponding remote object/session lifecycle.



# Metadata

## ResultSetMetaData

Remote result-set metadata includes the normal JDBC metadata contract and column information.

The implementation transfers metadata information over the remote protocol rather than exposing the server-side JDBC object directly.

## DatabaseMetaData

Database metadata is available through:

```java
DatabaseMetaData metaData = connection.getMetaData();
```

The implementation covers a broad range of JDBC metadata operations, including:

- database/product information
- driver information
- JDBC capabilities
- tables
- columns
- primary keys
- indexes
- imported keys
- exported keys
- best-row identifiers
- version columns
- type information
- procedures
- wrapper support
- connection association

Metadata remains session-local.



# Transactions

Remote transactions use the server-side JDBC connection.

Supported operations include:

```java
connection.setAutoCommit(false);
connection.commit();
connection.rollback();
```

Also supported/tested:

- transaction isolation
- savepoints
- rollback to savepoint
- release savepoint
- read-only state

Transaction state belongs to the individual remote session and is not shared between independent client connections.



# Batch execution

Remote JDBC supports JDBC batch execution for statements and prepared statements.

Prepared batches are sent to the server and executed using the server-side JDBC connection.

Large batch APIs are also represented where supported by the implementation.



# Exception handling

Remote JDBC exceptions are returned through the protocol and reconstructed on the client side as `SQLException` instances.

The implementation preserves JDBC exception information where available, including:

- exception message
- SQL state
- vendor error code
- chained exceptions where supplied by the server/JDBC driver

Communication failures are converted into `SQLException` and mark the remote session as broken.

For example, an HTTP/session expiration results in a client-side error such as:

```text
Remote JDBC connection is no longer available
```

A session expiration is distinguished from a normal JDBC operation failure.



# Connection and session failure handling

The client detects communication failures and prevents further normal operations on a broken remote session.

The server also supports session expiration:

```text
idleTimeout
```

The default idle timeout (in millis) is:

```text
1800000 (30 minutes)
```

Active requests are protected from idle-session cleanup.

The server closes tracked JDBC resources when a session is explicitly closed, expires, or the servlet is destroyed.



# Security

The server can restrict which database URLs a client is allowed to request.

Use the servlet parameter:

```text
allowedJdbcUrls
```

A client-supplied JDBC URL is accepted only when it matches the configured allow-list.

Alternatively, the server can provide a fixed JDBC URL through:

```text
jdbcUrl
```

This makes it possible to avoid exposing arbitrary database URLs to clients.

### Recommended deployment

For production:

- use HTTPS, if not use `privateKey`
- use production `environment` (client JDBC settings (url, user, password, properties) will be ignored
- use `token` instead of username and password
- define pre-configured JDBC url and credentials
- configure a strict `allowedJdbcUrls` policy or a fixed server-side `jdbcUrl`
- do not expose unrestricted database URLs to untrusted clients
- use database credentials appropriate for the application
- deploy behind the normal application/web-server authentication and authorization mechanisms
- restrict network access to the rjdbc endpoint

Remote JDBC itself is a JDBC transport layer; authentication/authorization policy should be provided by the surrounding deployment.



# Server configuration

The server is exposed as a servlet:

```text
com.sibvisions.rjdbc.server.JdbcServlet
```

Supported servlet initialization parameters include:

| Parameter | Default | Description |
|---|---:|---|
| `environment` | development | System environment (production, test, development, ...) |
| `allowedJdbcUrls` | empty | Comma-separated allow-list for client-supplied JDBC URLs |
| `jdbcUrl` | empty | Server-side JDBC URL used when the client does not provide one |
| `jdbcUsername` | empty | Server-side JDBC user name |
| `jdbcPassword` | empty | Server-side JDBC password |
| `token` | empty | Token for authentication to avoid username/password |
| `privateKey` | empty | Private key resource (file, classpath) |
| `privateKeyPassword` | empty | Private key password |
| `privateKeyAlias` | empty | Private key password |
| `idleTimeout` | `180000` (30 min) | Session idle timeout in millis; `0` disables expiration |
| `clobPrefetchSize` | `102400` (10MB) | Maximum CLOB length prefetched during row fetching |

Example:

```xml
<servlet>
    <servlet-name>rjdbc</servlet-name>
    <servlet-class>com.sibvisions.rjdbc.server.JdbcServlet</servlet-class>

    <!--
	<init-param>
	    <param-name>environment</param-name>
	    <param-value>development</param-value>
	</init-param>

	<init-param>
	    <param-name>allowedJdbcUrls</param-name>
	    <param-value>*</param-value>
	</init-param>
	
    <init-param>
        <param-name>jdbcUrl</param-name>
        <param-value>jdbc:oracle:thin:@localhost:1521/XE</param-value>
    </init-param>
	
	<init-param>
	    <param-name>jdbcUrl</param-name>
	    <param-value></param-value>
	</init-param>    

	<init-param>
	    <param-name>jdbcUsername</param-name>
	    <param-value></param-value>
	</init-param>
	
	<init-param>
	    <param-name>jdbcPassword</param-name>
	    <param-value></param-value>
	</init-param>
    

	<init-param>
	    <param-name>token</param-name>
	    <param-value></param-value>
	</init-param>
	
	<init-param>
	    <param-name>privateKey</param-name>
	    <param-value></param-value>
	</init-param>
	
	<init-param>
	    <param-name>privateKeyPassword</param-name>
	    <param-value></param-value>
	</init-param>
	
	<init-param>
	    <param-name>privateKeyAlias</param-name>
	    <param-value></param-value>
	</init-param>	

    <init-param>
        <param-name>idleTimeout</param-name>
        <param-value></param-value>
    </init-param>

    <init-param>
        <param-name>clobPrefetchSize</param-name>
        <param-value></param-value>
    </init-param>
    -->
</servlet>

<servlet-mapping>
    <servlet-name>rjdbc</servlet-name>
    <url-pattern>/jdbc/*</url-pattern>
</servlet-mapping>
```

The server uses the `javax.servlet` API in the current implementation. For Jakarta Servlet containers such as newer Tomcat versions, the servlet imports need to be adapted to `jakarta.servlet`.



# Protocol

The current protocol intentionally stays small.

Requests and responses are serialized Java objects using:

```text
com.sibvisions.rad.remote.UniversalSerializer
```

The transport uses HTTP POST with:

```text
Content-Type: application/octet-stream
Accept: application/octet-stream
```

The logical protocol consists of maps containing fields such as:

```text
id
sessionId
action
sql
value
values
result
rows
error
success
```

JDBC resources are represented by server-side numeric IDs.

There is no separate shared DTO model required for JDBC objects.



# Project structure

```text
src.jdbc/
    com/sibvisions/rjdbc/
        Remote JDBC driver

src.server/
    com/sibvisions/rjdbc/server/
		Remote JDBC server

test/
    com/sibvisions/rjdbc/
        JDBC contract and regression tests
```

The server-side delegates isolate JDBC resource handling from the HTTP transport.



# Dependencies

The project requires:

- Java 11 or newer
- JVx serialization support containing:
  ```text
  com.sibvisions.rad.remote.UniversalSerializer
  ```
- Servlet API on the server side

For the current servlet implementation:

```text
javax.servlet-api
```

For Jakarta Servlet containers, adapt the servlet API/imports accordingly.

The target database JDBC driver is required **on the application server**, not on the client-side.



# Building

The project can be compiled with ANT (build.xml).

The JDBC driver service registration is located at:

```text
src.jdbc/META-INF/services/java.sql.Driver
```



# Client usage

A normal JDBC application does not need to know whether the database is local or remote.

Example:

```java
Class.forName("com.sibvisions.rjdbc.RemoteDriver");

try (Connection connection =
         DriverManager.getConnection(
             "jdbc:rjdbc:https://server.example/app/jdbc",
             "user",
             "password");
     PreparedStatement statement =
         connection.prepareStatement(
             "select id, name from customer where id = ?");
) {
    statement.setInt(1, 42);

    try (ResultSet resultSet = statement.executeQuery()) {
        while (resultSet.next()) {
            System.out.println(
                resultSet.getInt("id") + " " +
                resultSet.getString("name")
            );
        }
    }
}
```

Because the driver is registered through the Java service-provider mechanism, explicit `Class.forName(...)` is normally not necessary when the driver JAR is correctly installed.



# Testing

The project has an extensive JDBC regression suite.

Covered areas include:

- Driver contract
- Connection contract
- connection lifecycle
- `isValid`
- Statement lifecycle
- PreparedStatement lifecycle
- CallableStatement
- ResultSet navigation
- empty ResultSets
- ResultSet cursor state
- updatable ResultSets
- ResultSet metadata
- DatabaseMetaData
- NULL handling
- JDBC type conversions
- `BigDecimal`
- batch processing
- LOBs
- NClob
- SQLXML
- wrappers
- resource lifecycle
- session isolation
- concurrent independent connections
- concurrent metadata operations
- concurrent connection state changes
- streaming
- exception transparency

The tests are designed to compare remote JDBC behavior with normal JDBC behavior where this is meaningful and to verify remote-specific contracts such as session isolation and lifecycle handling.



# Design goals

The implementation focuses on:

1. **Standard JDBC API**

   Applications should use normal `java.sql` interfaces.

2. **Small remote protocol**

   JDBC objects are represented by IDs and method calls rather than a large custom object model.

3. **Server-side JDBC execution**

   Database-specific JDBC drivers remain on the server.

4. **Session isolation**

   Each remote connection owns its own JDBC session and resources.

5. **Block-based ResultSet fetching**

   Large result sets do not need to be transferred in a single response.

6. **Explicit lifecycle management**

   Remote resources are tracked and closed on the client and server.

7. **Database independence**

   The rjdbc layer itself does not depend on Oracle, PostgreSQL, MySQL, etc. Database-specific behavior comes from the JDBC driver installed on the server.



# Current limitations

Remote JDBC is **not a complete JDBC 4.x/4.3 compliance implementation**.

In particular:

- `Driver.jdbcCompliant()` returns `false`.
- Behavior ultimately depends on the JDBC driver and database used by the server.
- Network latency is inherent in remote JDBC operations.
- The current protocol is based on serialization and should therefore be deployed only in trusted, controlled environments with appropriate transport security.
- The current server servlet uses the `javax.servlet` API.
- Performance characteristics depend heavily on fetch size, database latency, network latency and the server-side JDBC driver.

The goal is broad practical JDBC compatibility, not a claim of complete JDBC specification compliance.



# License

[GNU General Public License, Version 3.0](https://www.gnu.org/licenses/gpl-3.0.en.html)
