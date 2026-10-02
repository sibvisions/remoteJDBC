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

import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.DriverPropertyInfo;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;

import org.junit.Assert;
import org.junit.Test;

/**
 * Tests secure-only handling of client supplied JDBC connection information.
 *
 * @author René Jahn
 */
public class JdbcContextSecurityTest
{
    /**
     * Verifies that the production environment uses the server JDBC configuration.
     *
     * @throws Exception if the test driver cannot be registered
     */
    @Test
    public void testProductionUsesServerConnectionInformation() throws Exception
    {
        CapturingDriver driver = new CapturingDriver();
        
        DriverManager.registerDriver(driver);

        try
        {
            JdbcContext context = new JdbcContext("jdbc:test:%", "jdbc:test:server", "server", "server-password", 0, 0, JdbcSecurity.ENVIRONMENT_PROD);
            JdbcDispatcher dispatcher = new JdbcDispatcher(context);

            java.util.Map<String,Object> response = dispatcher.execute(createConnectRequest("jdbc:test:client", "client-value"));

            Assert.assertEquals(Boolean.TRUE, response.get("success"));
            Assert.assertEquals("jdbc:test:server", driver.url);
            Assert.assertFalse(driver.properties.containsKey("clientProperty"));
        }
        finally
        {
            DriverManager.deregisterDriver(driver);
        }
    }

    /**
     * Verifies that the test environment keeps the existing client configuration behavior.
     *
     * @throws Exception if the test driver cannot be registered
     */
    @Test
    public void testTestEnvironmentUsesClientConnectionInformation() throws Exception
    {
        CapturingDriver driver = new CapturingDriver();
        
        DriverManager.registerDriver(driver);

        try
        {
            JdbcContext context = new JdbcContext("jdbc:test:*", "jdbc:test:server", null, null, 0, 0, "test");
            JdbcDispatcher dispatcher = new JdbcDispatcher(context);

            java.util.Map<String,Object> response = dispatcher.execute(createConnectRequest("jdbc:test:client", "client-value"));

            Assert.assertEquals(Boolean.TRUE, response.get("success"));
            Assert.assertEquals("jdbc:test:client", driver.url);
            Assert.assertEquals("client-value", driver.properties.getProperty("clientProperty"));
        }
        finally
        {
            DriverManager.deregisterDriver(driver);
        }
    }

    /**
     * Creates a connect request for the test driver.
     *
     * @param pUrl the client JDBC URL
     * @param pValue the client property value
     * @return the connect request
     */
    private Map<String,Object> createConnectRequest(String pUrl, String pValue)
    {
        Map<String, Object> request = new HashMap<String, Object>();
        request.put("action", "connect");
        request.put("jdbcUrl", pUrl);

        Properties properties = new Properties();
        properties.setProperty("clientProperty", pValue);
        request.put("properties", properties);

        return request;
    }

    /**
     * JDBC test driver that records the connection parameters supplied by the dispatcher.
     */
    private static final class CapturingDriver implements Driver
    {
        private String url;
        private Properties properties;

        @Override
        public Connection connect(String pUrl, Properties pProperties) throws SQLException
        {
            url = pUrl;
            properties = new Properties();
            properties.putAll(pProperties);

            return (Connection)Proxy.newProxyInstance(Connection.class.getClassLoader(),
                                                         new Class<?>[] {Connection.class},
                                                         (pProxy, pMethod, pArgs) ->
                                                         {
                                                             if ("isClosed".equals(pMethod.getName()))
                                                             {
                                                                 return Boolean.FALSE;
                                                             }

                                                             if ("close".equals(pMethod.getName()))
                                                             {
                                                                 return null;
                                                             }

                                                             throw new SQLFeatureNotSupportedException();
                                                         });
        }

        @Override
        public boolean acceptsURL(String pUrl)
        {
            return pUrl != null && pUrl.startsWith("jdbc:test:");
        }

        @Override
        public DriverPropertyInfo[] getPropertyInfo(String pUrl, Properties pProperties)
        {
            return new DriverPropertyInfo[0];
        }

        @Override
        public int getMajorVersion()
        {
            return 1;
        }

        @Override
        public int getMinorVersion()
        {
            return 0;
        }

        @Override
        public boolean jdbcCompliant()
        {
            return false;
        }

        @Override
        public Logger getParentLogger() throws SQLFeatureNotSupportedException
        {
            throw new SQLFeatureNotSupportedException();
        }
    }
}
