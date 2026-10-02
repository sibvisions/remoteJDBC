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
package com.sibvisions.rjdbc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

import org.junit.Assert;
import org.junit.Test;

/**
 * Verifies end-to-end remote JDBC communication through the configured HTTP endpoint.
 *
 * @author René Jahn
 */
public class CommunicationTest
{
    /**
     * Verifies that an unencrypted remote JDBC connection can execute a query.
     *
     * @throws Exception if the operation fails
     */
    @Test
    public void testUnencryptedCommunication() throws Exception
    {
        try (Connection connection = TestConnection.create())
        {
        	executeQuery(connection);
        }
    }

    /**
     * Verifies that a server certificate enables encrypted remote communication.
     *
     * @throws Exception if the operation fails
     */
    @Test
    public void testEncryptedCommunication() throws Exception
    {
    	//This Test will fail in production mode
    	
        Properties properties = createOracleProperties();
        properties.setProperty(RemoteConstants.SERVER_CERTIFICATE, TestConnection.TEST_CLIENT_PUBLIC_KEY);

        try (Connection connection = TestConnection.create(properties))
        {
        	executeQuery(connection);
        }
    }

    /**
     * Verifies that a configured authentication token can establish an unencrypted remote JDBC connection.
     *
     * @throws Exception if the operation fails
     */
    @Test
    public void testUnencryptedTokenAuthentication() throws Exception
    {
        Properties properties = createOracleProperties();
        properties.setProperty(RemoteConstants.TOKEN, TestConnection.TEST_TOKEN);

        try (Connection connection = TestConnection.create(properties))
        {
        	executeQuery(connection);
        }
    }

    /**
     * Verifies that encrypted and token authentication work together.
     *
     * @throws Exception if the operation fails
     */
    @Test
    public void testEncryptedTokenAuthentication() throws Exception
    {
    	//This Test will fail in production mode 
    	
        Properties properties = createOracleProperties();
        properties.setProperty(RemoteConstants.SERVER_CERTIFICATE, TestConnection.TEST_CLIENT_PUBLIC_KEY);
        properties.setProperty(RemoteConstants.TOKEN, "rjdbc-test-token");

        try (Connection connection = TestConnection.create(properties))
        {
        	executeQuery(connection);
        }
    }

    /**
     * Verifies that an invalid authentication token is rejected by the remote server.
     *
     * @throws Exception if the operation fails
     */
    @Test
    public void testInvalidTokenIsRejected() throws Exception
    {
        Properties properties = new Properties();
        properties.setProperty(RemoteConstants.TOKEN, TestConnection.TEST_TOKEN + "-invalid");

        try
        {
            TestConnection.create(properties);

            fail("An invalid authentication token must be rejected.");
        }
        catch (SQLException expected)
        {
        	Assert.assertEquals("Remote JDBC HTTP status: 401", expected.getMessage());
        }
    }

    /**
     * Creates remote JDBC properties for a connection that uses the client database configuration.
     *
     * @return the remote JDBC properties
     */
    private static Properties createOracleProperties()
    {
        Properties properties = new Properties();
        properties.put(RemoteConstants.USER, TestConnection.propDb.getProperty("oracle.db.user"));
        properties.put(RemoteConstants.PASSWORD, TestConnection.propDb.getProperty("oracle.db.pwd"));
        properties.put(RemoteConstants.JDBC_URL, TestConnection.propDb.getProperty("oracle.db.url"));

        return properties;
    }

    /**
     * Executes a simple query through the configured remote JDBC endpoint.
     *
     * @param pProperties the connection properties
     * @throws Exception if the operation fails
     */
    private static void executeQuery(Connection pConnection) throws Exception
    {
        try (PreparedStatement statement = pConnection.prepareStatement("SELECT 1 FROM DUAL");
             ResultSet result = statement.executeQuery())
        {
            assertTrue(result.next());
            assertEquals(1, result.getInt(1));
            assertTrue(!result.next());
        }
    }
}
