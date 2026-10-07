<p align="right">
  <img src="design/png/logo_remoteJDBC.png" alt="Logo">
</p>
<br>

**Remote JDBC** is a Type-3 remote JDBC driver for Java 11+.

It allows a Java application to use the standard `java.sql` API while the
actual JDBC connection and JDBC resources are maintained on a remote server.
The client communicates with the server over HTTP(S) using a compact binary
protocol based on JVx `UniversalSerializer`.

The database-specific JDBC driver is installed on the server. The client does
not need the JDBC driver of the target database.

## Why Remote JDBC?

Remote JDBC is useful when a client cannot or should not connect directly to a
database, for example:

- the database is only reachable from a protected server network
- a database should not be exposed to client networks
- an application server should own the database credentials
- existing Java applications should continue to use the standard JDBC API
- tools such as DBeaver should access a database through a JDBC-over-HTTP
  endpoint

No application-specific database API is required. Applications continue to
use `java.sql.Connection`, `PreparedStatement`, `ResultSet`, and the other
standard JDBC interfaces.

## Architecture

```text
+------------------------+
| Java application       |
| DBeaver / JDBC client  |
+-----------+------------+
            |
            | jdbc:rjdbc:https://...
            | HTTP POST
            v
+------------------------+
| Remote JDBC client     |
| JDBC Driver            |
+-----------+------------+
            |
            | HTTP(S)
            | binary protocol
            v
+------------------------+
| Servlet container      |
| JdbcServlet            |
+-----------+------------+
            |
            | JDBC
            v
+------------------------+
| Target database        |
| + database JDBC driver |
+------------------------+
```

A remote JDBC `Connection` represents a server-side session. The session owns
the corresponding server-side JDBC connection and the JDBC resources created
from it.

## Features

### JDBC driver

- Standard `java.sql.Driver`
- Automatic driver registration through
  `META-INF/services/java.sql.Driver`
- JDBC URL prefix: `jdbc:rjdbc:`
- Java 11+
- Standard `DriverManager` integration
- Connection properties
- `Connection.isValid(...)`
- Connection/session lifecycle handling

### Remote JDBC API

The implementation provides remote representations for:

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

The implementation uses concrete remote JDBC classes rather than Java
dynamic proxies.

Unsupported JDBC operations are reported using JDBC exceptions such as
`SQLFeatureNotSupportedException` where appropriate.

### Result sets

Result sets use client-side caching and block fetching.

Instead of transferring an entire result set in one response, rows are
transferred in blocks. This reduces network round trips and allows large
result sets to be processed without one huge response.

The implementation covers, among other things:

- `next()`
- `previous()`
- `first()`
- `last()`
- `absolute()`
- `relative()`
- `beforeFirst()`
- `afterLast()`
- cursor-state methods
- column access by index and label
- `wasNull()`
- ResultSet metadata
- updateable result sets where supported by the underlying JDBC driver

The server limits a single remote fetch to 10,000 rows.

### Statements and prepared statements

Supported functionality includes:

- `executeQuery`
- `executeUpdate`
- `execute`
- generated keys where supported by the underlying driver
- fetch size
- max rows
- max field size
- query timeout
- warnings
- poolable state
- close-on-completion
- multiple results
- batch execution
- wrapper support
- parameter binding
- parameter metadata
- streams and readers
- LOB parameters
- large-update APIs where supported

Prepared batches are executed against the server-side JDBC connection.

The server limits a prepared batch to 10,000 entries.

### Transactions

Transactions belong to the individual remote session and therefore to the
corresponding server-side JDBC connection.

```java
connection.setAutoCommit(false);

try {
    // JDBC operations...
    connection.commit();
}
catch (SQLException e) {
    connection.rollback();
    throw e;
}
```

Supported transaction functionality includes:

- auto-commit
- commit
- rollback
- transaction isolation
- read-only state
- savepoints
- rollback to savepoint
- release savepoint

The actual behavior ultimately depends on the target database and its JDBC
driver.

### LOBs and streams

Remote support is provided for:

- `Blob`
- `Clob`
- `NClob`
- `SQLXML`
- binary streams
- character streams
- readers
- writers

CLOB prefetching can be configured on the server.

The default is:

```text
clobPrefetchSize = 102400
```

The value is measured in **characters**, not bytes and not megabytes.

CLOB data exceeding the configured prefetch size is loaded on demand.

### Metadata

`DatabaseMetaData` and `ResultSetMetaData` are transferred over the remote
protocol.

The implementation covers a broad range of metadata operations, including:

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

## Quick start

### 1. Build Remote JDBC

The project can be built with Apache Ant using:

```text
build.xml
```

The JDBC driver service registration is located at:

```text
src.jdbc/META-INF/services/java.sql.Driver
```

### 2. Deploy the server

Deploy the server servlet to a servlet container such as Tomcat.

The servlet class is:

```text
com.sibvisions.rjdbc.server.JdbcServlet
```

A minimal servlet mapping is:

```xml
<servlet>
    <servlet-name>rjdbc</servlet-name>
    <servlet-class>com.sibvisions.rjdbc.server.JdbcServlet</servlet-class>
</servlet>

<servlet-mapping>
    <servlet-name>rjdbc</servlet-name>
    <url-pattern>/jdbc/*</url-pattern>
</servlet-mapping>
```

The current server implementation uses the `javax.servlet` API.

### 3. Install the database JDBC driver on the server

The JDBC driver for the target database must be available to the server-side
application.

For example:

```text
Tomcat
  |
  +-- Remote JDBC servlet
  |
  +-- Oracle JDBC driver
```

The Oracle, PostgreSQL, MySQL, SQL Server, etc. JDBC driver is **not** needed
by the Remote JDBC client merely to establish the remote connection.

### 4. Configure the server

For a production deployment, configure the database connection on the
server:

```xml
<init-param>
    <param-name>environment</param-name>
    <param-value>production</param-value>
</init-param>

<init-param>
    <param-name>jdbcUrl</param-name>
    <param-value>jdbc:oracle:thin:@dbserver:1521/ORCL</param-value>
</init-param>

<init-param>
    <param-name>jdbcUsername</param-name>
    <param-value>application_user</param-value>
</init-param>

<init-param>
    <param-name>jdbcPassword</param-name>
    <param-value>change-me</param-value>
</init-param>

<init-param>
    <param-name>token</param-name>
    <param-value>change-me-to-a-strong-random-token</param-value>
</init-param>
```

optional

```xml
<init-param>
    <param-name>tokenManager</param-name>
    <param-value>c.s.r.s.CustomTokenManager</param-value>
</init-param>
```

Do not store real production credentials or tokens in source control.

### 5. Connect from Java

```java
try (Connection connection =
         DriverManager.getConnection(
             "jdbc:rjdbc:https://server.example/app/jdbc",
             "user",
             "password")) {

    try (PreparedStatement statement =
             connection.prepareStatement(
                 "select id, name from customer where id = ?")) {

        statement.setInt(1, 42);

        try (ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                System.out.println(
                    resultSet.getInt("id") + " " +
                    resultSet.getString("name"));
            }
        }
    }
}
```

The driver is normally registered automatically through the Java service
provider mechanism. `Class.forName(...)` is therefore normally not required.

If explicit registration is required, the driver class is:

```text
com.sibvisions.rjdbc.RemoteDriver
```

## JDBC URL

A Remote JDBC URL starts with:

```text
jdbc:rjdbc:
```

Example:

```text
jdbc:rjdbc:https://server.example/app/jdbc
```

The URL after `jdbc:rjdbc:` is the HTTP(S) endpoint of the Remote JDBC
servlet.

The target database JDBC URL is **not** appended to the Remote JDBC URL.

The target database URL is selected by the server:

- in production, from the server configuration
- in non-production environments, optionally from a client-supplied JDBC URL
  that passes the server's `allowedJdbcUrls` policy

## Client connection properties

Remote JDBC uses JDBC connection properties for remote-specific options.

For example, the authentication token is supplied as the `token` property
when token authentication is used.

A client must not assume that the username/password arguments of
`DriverManager.getConnection(...)` are database credentials in every server
configuration. In production the server owns the database credentials.

## Server configuration

The server is exposed as:

```text
com.sibvisions.rjdbc.server.JdbcServlet
```

Supported servlet initialization parameters are:

| Parameter | Default | Description |
|---|---:|---|
| `environment` | `development` | Server environment. `production` activates the production rules. |
| `allowedJdbcUrls` | empty | Allow-list for client-supplied JDBC URLs. |
| `jdbcUrl` | empty | Server-side JDBC URL. Required in production. |
| `jdbcUsername` | empty | Server-side JDBC username. Required in production. |
| `jdbcPassword` | empty | Server-side JDBC password. Required in production. |
| `token` | empty | Optional token configuration. In production a token must be configured and supplied by the client. |
| `tokenManager` | empty | Optional token manager (full class name). In production a token must be configured and supplied by the client. |
| `privateKey` | empty | Optional PKCS#12 key-store resource containing the server private key. Enables Remote JDBC protocol encryption. |
| `privateKeyPassword` | empty | Password used to open the PKCS#12 key store/private-key entry. |
| `privateKeyAlias` | empty | Optional private-key alias. If omitted, the first private-key entry is selected. |
| `idleTimeout` | `1800000` | Session idle timeout in **milliseconds**. `1800000` = 30 minutes. `0` disables expiration. |
| `clobPrefetchSize` | `102400` | Maximum CLOB prefetch size in **characters**. |

### `idleTimeout`

The unit is milliseconds.

Default:

```text
1800000 ms = 30 minutes
```

Example:

```xml
<init-param>
    <param-name>idleTimeout</param-name>
    <param-value>1800000</param-value>
</init-param>
```

A value of `0` disables idle expiration.

Active requests are protected from idle-session cleanup.

### `clobPrefetchSize`

The value is a number of **characters**.

Default:

```text
102400 characters
```

It is not a size in MB.

## Development mode

Development mode is useful when the client needs to select the target JDBC
URL.

Example:

```xml
<init-param>
    <param-name>environment</param-name>
    <param-value>development</param-value>
</init-param>

<init-param>
    <param-name>allowedJdbcUrls</param-name>
    <param-value>jdbc:oracle:thin:@localhost:1521/XE</param-value>
</init-param>
```

The client can then provide the allowed database JDBC URL.

Do not expose an unrestricted client-JDBC-URL configuration to untrusted
networks.

## Production mode

Set:

```xml
<init-param>
    <param-name>environment</param-name>
    <param-value>production</param-value>
</init-param>
```

Production mode requires:

- a server-side `jdbcUrl`
- a server-side `jdbcUsername`
- a server-side `jdbcPassword`
- a configured authentication `token` or `tokenManager

The client cannot select the target JDBC URL in production.

Client JDBC URL, username, password and client JDBC properties are not used to
override the server-side production database configuration.

The production connection therefore follows this model:

```text
Remote JDBC client
        |
        | authentication token
        v
Remote JDBC servlet
        |
        | fixed URL + fixed credentials
        v
Target database
```

This is the recommended configuration when the Remote JDBC endpoint is used
by untrusted or semi-trusted clients.

## Security

Remote JDBC provides two protocol-level security mechanisms:

1. token authentication;
2. optional application-level encryption.

They are separate mechanisms.

### Token authentication

The server can be configured with:

```xml
<init-param>
    <param-name>token</param-name>
    <param-value>...</param-value>
</init-param>
```

The client supplies the token using the JDBC connection property:

```text
token
```

Token comparison uses a constant-time comparison.

In production, token authentication is mandatory. A production server without
a configured token rejects connection initialization.

In non-production environments:

- if no token is configured, no token is required
- if a token is configured, a supplied token is checked
- an incorrect supplied token is rejected

Token authentication by itself does **not** encrypt HTTP traffic.

Therefore:

```text
HTTP + token
```

provides authentication, but not transport confidentiality.

Use HTTPS when the HTTP connection itself must be protected.

### Application-level protocol encryption

Remote JDBC can additionally encrypt the protocol payload.

Configure the server with a PKCS#12 key store:

```xml
<init-param>
    <param-name>privateKey</param-name>
    <param-value>rjdbc-appserver.p12</param-value>
</init-param>

<init-param>
    <param-name>privateKeyPassword</param-name>
    <param-value>...</param-value>
</init-param>

<init-param>
    <param-name>privateKeyAlias</param-name>
    <param-value>rjdbc</param-value>
</init-param>
```

The server loads a private key from the PKCS#12 key store.

The protocol security implementation uses:

- RSA-OAEP with SHA-256 for the handshake key
- AES-GCM for encrypted payloads
- HMAC-SHA-256 for direction-specific key derivation
- random nonces
- per-connection keys
- authenticated data containing the protocol version, session ID,
  sequence number and direction

The private key remains on the server.

The application-level protocol encryption protects the Remote JDBC payload.
It is not TLS and does not turn an HTTP endpoint into an HTTPS endpoint.

### HTTPS

For an Internet-facing deployment, HTTPS is strongly recommended.

Recommended architecture:

```text
Remote JDBC client
       |
       | HTTPS
       v
Reverse proxy / Tomcat
       |
       | Remote JDBC protocol
       v
Database
```

HTTPS protects the HTTP transport itself, including the Remote JDBC binary
payload, authentication token and other request data.

Protocol encryption can additionally be enabled when an independent
application-level encryption boundary is desired.

### Important security distinction

The following are different:

| Configuration | Authentication | HTTP transport encryption | Remote protocol encryption |
|---|---|---|---|
| HTTP only | No | No | No |
| HTTP + token | Yes | No | No |
| HTTPS | Depends on deployment | Yes | No |
| HTTPS + token | Yes | Yes | No |
| HTTPS + token + `privateKey` | Yes | Yes | Yes |

For production deployments, HTTPS plus token authentication and a fixed
server-side database configuration is the normal recommendation.

### Database URL restrictions

The server can restrict client-selected JDBC URLs with:

```text
allowedJdbcUrls
```

It is a semicolon separated list with support for wildcard (*).

A client-supplied URL is accepted only when it matches the configured
allow-list.

This is useful in development or controlled multi-database deployments.

For production, a fixed server-side `jdbcUrl` is preferable.

Do not use a broad allow-list as a substitute for authentication.

### Credentials

When a client-selected JDBC URL is used, the client configuration is used
for the selected database connection.

When the server-side JDBC configuration is used, the server's
`jdbcUsername` and `jdbcPassword` are applied.

In production the server-side credentials are mandatory.

## Sessions and resource lifecycle

Each remote JDBC connection gets its own server-side session.

A session tracks server-side JDBC resources such as:

- `Connection`
- `Statement`
- `PreparedStatement`
- `CallableStatement`
- `ResultSet`
- metadata objects
- `Blob`
- `Clob`
- `NClob`
- `SQLXML`
- `Array`
- `Struct`
- `Ref`
- `RowId`
- `Savepoint`
- executors

Session IDs are generated on the server.

Resources are released when the corresponding remote resource is closed and
when the complete session is closed.

The server also cleans up tracked resources when:

- a session expires
- a connection/session is explicitly closed
- the servlet is destroyed

The default idle timeout is 30 minutes.

## Protocol

Remote JDBC uses HTTP POST with:

```text
Content-Type: application/octet-stream
Accept: application/octet-stream
```

The payload is serialized using:

```text
com.sibvisions.rad.remote.UniversalSerializer
```

The protocol represents remote JDBC resources using server-side IDs.

The logical request/response data contains fields such as:

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

The protocol is intentionally small and keeps JDBC objects on the server
instead of trying to serialize the complete JDBC object graph.

## Exception handling

Remote JDBC exceptions are returned through the protocol and reconstructed on
the client as `SQLException` instances.

Where available, the implementation preserves:

- message
- SQL state
- vendor error code
- chained exceptions

Communication failures are converted into `SQLException` and the affected
remote session is treated as broken.

A session expiration is distinguished from a normal JDBC operation failure.

## Supported database drivers

Remote JDBC itself is database-independent.

The target database JDBC driver is loaded on the server.

The server contains explicit driver initialization for commonly used JDBC URL
prefixes, including:

- Oracle
- DB2
- IBM i / AS/400
- Derby
- jTDS SQL Server
- Microsoft SQL Server
- MySQL
- PostgreSQL
- EDB
- HSQLDB
- SQLite
- H2
- MariaDB

For other JDBC drivers, the server can use Java's `ServiceLoader` mechanism.

The actual supported functionality is ultimately determined by the JDBC driver
and database installed on the server.

## Tomcat deployment

Remote JDBC is suitable for servlet containers such as Tomcat.

The current servlet implementation uses:

```text
javax.servlet.*
```

Therefore the current source targets the `javax.servlet` API.

If deploying to a Jakarta Servlet-only container, use built `jakarta` binary.

For a Tomcat deployment, the important pieces are:

```text
Tomcat
|
+-- Remote JDBC web application
|   |
|   +-- JdbcServlet
|   +-- Remote JDBC server classes
|
+-- JVx serialization dependency
|
+-- target database JDBC driver
```

The database must be reachable from the Tomcat/server host, not from the
Remote JDBC client.

## DBeaver and other JDBC tools

Remote JDBC can be used by JDBC clients that support custom JDBC drivers.

The important distinction is:

```text
DBeaver
   |
   | Remote JDBC driver
   |
   v
Remote JDBC servlet
   |
   | Oracle/PostgreSQL/etc. JDBC driver
   |
   v
Database
```

DBeaver does not need direct network access to the database.

It needs network access to the Remote JDBC HTTP(S) endpoint.

For an Oracle database, for example, the Oracle JDBC driver belongs on the
Remote JDBC server. The DBeaver client uses the Remote JDBC driver and the
`jdbc:rjdbc:` URL.

## Troubleshooting

### `No suitable driver`

Check the JDBC URL.

The client URL must start with:

```text
jdbc:rjdbc:
```

For example:

```text
jdbc:rjdbc:https://server.example/app/jdbc
```

Do not configure the Remote JDBC client with the target database URL:

```text
jdbc:oracle:thin:@localhost:1521/XE
```

That is a server-side database URL.

If the error occurs on the server side, verify that the target JDBC driver is
installed and visible to the server class loader.

### Database driver not found on the server

Check:

1. the database JDBC JAR is installed on the server
2. the JAR is visible to the application/server class loader
3. the JDBC URL uses the correct database-specific prefix
4. the corresponding JDBC driver class is available

For Oracle, for example:

```text
jdbc:oracle:
```

causes the server to initialize:

```text
oracle.jdbc.OracleDriver
```

### Production connection rejected

Verify that all production settings are present:

```text
environment=production
jdbcUrl=<server-side JDBC URL>
jdbcUsername=<server-side DB user>
jdbcPassword=<server-side DB password>
token=<strong token> or tokenManager=<full qualfied class name>
```

The client must provide the configured Remote JDBC token.

### Client JDBC URL rejected

If the client is allowed to select the database URL, verify:

```text
allowedJdbcUrls
```

The supplied URL must match the configured allow-list.

For production, do not expect the client JDBC URL to be accepted. Production
uses the server-side `jdbcUrl`.

### Session unexpectedly expires

Check:

```text
idleTimeout
```

The unit is milliseconds.

For example:

```text
1800000 = 30 minutes
3600000 = 60 minutes
0       = disabled
```

### CLOB appears truncated or is fetched in parts

Check:

```text
clobPrefetchSize
```

The value is measured in characters.

Default:

```text
102400 characters
```

Larger CLOB values are fetched on demand.

## Performance considerations

Remote JDBC adds network latency to JDBC operations.

Applications should therefore avoid unnecessary request/response cycles.

Important factors include:

- network latency
- database latency
- ResultSet fetch size
- number of JDBC calls
- batch size
- LOB usage
- database driver behavior

Result sets are fetched in blocks and the server limits one fetch operation to
10000 rows.

Prepared batches are limited to 10000 entries.

For high-latency networks, batching and appropriate fetch sizes can have a
significant impact on performance.

## Testing

The project contains an extensive JDBC regression test suite.

Covered areas include:

- driver contract
- connection contract
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

The tests compare remote JDBC behavior with normal JDBC behavior where that is
meaningful and also verify Remote JDBC-specific session and lifecycle
contracts.

## Project structure

```text
src.jdbc/
    com/sibvisions/rjdbc/
        Remote JDBC client

src.server/
    com/sibvisions/rjdbc/server/
        Remote JDBC servlet/server

test/
    com/sibvisions/rjdbc/
        JDBC contract and regression tests

security/
    Security-related helpers
```

The server-side delegate classes isolate JDBC resource handling from the HTTP
transport.

## Dependencies

The project requires:

- Java 11 or newer
- JVx serialization support containing
  `com.sibvisions.rad.remote.UniversalSerializer`
  
  ```text
  <dependency>
      <groupId>com.sibvisions.jvx</groupId>
      <artifactId>jvxserialize</artifactId>
      <version>3.5.1</version>
  </dependency>
  ```
  
- Servlet API on the server side

The current servlet implementation uses:

```text
javax.servlet-api
```

The target database JDBC driver is required on the server, not on the
Remote JDBC client.

## Design goals

Remote JDBC focuses on:

1. **Standard JDBC API**

   Applications use normal `java.sql` interfaces.

2. **Small remote protocol**

   JDBC resources are represented by IDs and remote method calls rather than
   by a large custom object model.

3. **Server-side JDBC execution**

   Database-specific JDBC drivers remain on the server.

4. **Session isolation**

   Each remote connection owns its own JDBC session and resources.

5. **Block-based ResultSet fetching**

   Large result sets do not need to be transferred in a single response.

6. **Explicit resource lifecycle**

   Remote resources are tracked and released on both client and server.

7. **Database independence**

   Remote JDBC itself does not depend on Oracle, PostgreSQL, MySQL, or another
   specific database.

## Limitations

Remote JDBC aims for broad practical JDBC compatibility. It is not a claim of
complete JDBC 4.x/4.3 compliance.

Important limitations:

- `Driver.jdbcCompliant()` returns `false`
- behavior depends on the JDBC driver and database used on the server
- network latency is inherent in remote JDBC
- some JDBC operations are unsupported or depend on the underlying driver
- the current servlet implementation uses `javax.servlet`
- the protocol uses JVx serialization
- performance depends on fetch sizes, database latency, network latency and
  server-side JDBC driver behavior

The project should therefore be evaluated against the specific JDBC driver and
database features required by an application.

## Security checklist

For a production deployment:

- [ ] Use HTTPS
- [ ] Use `environment=production`
- [ ] Configure a fixed server-side `jdbcUrl`
- [ ] Configure server-side `jdbcUsername`
- [ ] Configure server-side `jdbcPassword`
- [ ] Configure a strong `token` or `tokenManager` 
- [ ] Keep the database credentials and token out of source control
- [ ] Restrict network access to the Remote JDBC endpoint
- [ ] Use least-privilege database credentials
- [ ] Do not expose unrestricted client JDBC URLs
- [ ] Protect the PKCS#12 private key if protocol encryption is enabled
- [ ] Use the servlet container/reverse proxy's normal authentication and
      authorization mechanisms where required

Remote JDBC is a JDBC transport layer. It should be deployed as part of a
properly secured application/server architecture.

## Maven

### Remote JDBC driver

```text
<dependency>
    <groupId>com.sibvisions.rjdbc</groupId>
    <artifactId>rjdbc-driver</artifactId>
    <version>1.12</version>
</dependency>
```

### javax.servlet API

```text
<dependency>
    <groupId>com.sibvisions.rjdbc</groupId>
    <artifactId>rjdbc-server-javax</artifactId>
    <version>1.12</version>
</dependency>
```
  
### jakarta.servlet API
  
```text
<dependency>
    <groupId>com.sibvisions.rjdbc</groupId>
    <artifactId>rjdbc-server-jakarta</artifactId>
    <version>1.12</version>
</dependency>
```

## License

Remote JDBC is licensed under the GNU General Public License, version 3.0
(GPL-3.0).

See [LICENSE](LICENSE).

