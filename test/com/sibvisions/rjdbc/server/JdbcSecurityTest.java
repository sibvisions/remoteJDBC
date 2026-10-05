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

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;

import com.sibvisions.rjdbc.RemoteSecurityWrapper;
import com.sibvisions.rjdbc.TestConnection;
import com.sibvisions.util.type.ResourceUtil;

/**
 * Tests server-side authentication and session sequence handling.
 *
 * @author René Jahn
 */
public class JdbcSecurityTest
{
    /**
     * Verifies that the configured token is required for connection authentication.
     *
     * @throws Exception if the security operation fails
     */
    @Test
    public void testAuthenticationToken() throws Exception
    {
        Path keyStore = copyKeyStore();
        
        JdbcSecurity security = new JdbcSecurity(keyStore.toString(), 
        										 TestConnection.TEST_SERVER_PRIVATE_KEY_PASSWORD, TestConnection.TEST_SERVER_PRIVATE_KEY_ALIAS, "a-token", null);
        
        byte[] connectionKey = RemoteSecurityWrapper.createConnectionKey();
        byte[] payload = "connect".getBytes(StandardCharsets.UTF_8);

        try
        {
            byte[] encrypted = RemoteSecurityWrapper.encryptHandshake(loadCertificate().getPublicKey(),
                                                                      "a-token",
                                                                      connectionKey,
                                                                      payload);
            JdbcSecurity.Authentication authentication = security.authenticate(encrypted, JdbcSecurity.ENVIRONMENT_DEV);

            Assert.assertArrayEquals(connectionKey, authentication.getConnectionKey());
            Assert.assertArrayEquals(payload, authentication.getPayload());
        }
        finally
        {
            Arrays.fill(connectionKey, (byte)0);
            Arrays.fill(payload, (byte)0);
            Files.deleteIfExists(keyStore);
        }
    }


    /**
     * Verifies that encrypted connection data can be used without token authentication.
     *
     * @throws Exception if the security operation fails
     */
    @Test
    public void testOptionalAuthenticationToken() throws Exception
    {
        Path keyStore = copyKeyStore();
        
        JdbcSecurity security = new JdbcSecurity(keyStore.toString(), TestConnection.TEST_SERVER_PRIVATE_KEY_PASSWORD, TestConnection.TEST_SERVER_PRIVATE_KEY_ALIAS, null, null);
        
        byte[] connectionKey = RemoteSecurityWrapper.createConnectionKey();
        byte[] payload = "connect".getBytes(StandardCharsets.UTF_8);

        try
        {
            byte[] encrypted = RemoteSecurityWrapper.encryptHandshake(loadCertificate().getPublicKey(),
                                                               		  null,
                                                               		  connectionKey,
                                                               		  payload);
            JdbcSecurity.Authentication authentication = security.authenticate(encrypted, JdbcSecurity.ENVIRONMENT_DEV);

            Assert.assertArrayEquals(connectionKey, authentication.getConnectionKey());
            Assert.assertArrayEquals(payload, authentication.getPayload());
        }
        finally
        {
            Arrays.fill(connectionKey, (byte)0);
            Arrays.fill(payload, (byte)0);
            Files.deleteIfExists(keyStore);
        }
    }

    /**
     * Verifies that an incorrect token is rejected during connection authentication.
     *
     * @throws Exception if the security operation fails
     */
    @Test
    public void testWrongAuthenticationToken() throws Exception
    {
        Path keyStore = copyKeyStore();
        JdbcSecurity security = new JdbcSecurity(keyStore.toString(), TestConnection.TEST_SERVER_PRIVATE_KEY_PASSWORD, TestConnection.TEST_SERVER_PRIVATE_KEY_ALIAS, "a-token", null);
        
        byte[] connectionKey = RemoteSecurityWrapper.createConnectionKey();
        byte[] payload = "connect".getBytes(StandardCharsets.UTF_8);

        try
        {
            byte[] encrypted = RemoteSecurityWrapper.encryptHandshake(loadCertificate().getPublicKey(),
                                                               		  "wrong-token",
                                                               		  connectionKey,
                                                               		  payload);

            try
            {
                security.authenticate(encrypted, JdbcSecurity.ENVIRONMENT_DEV);
                
                Assert.fail("An incorrect authentication token must be rejected");
            }
            catch (SecurityException expected)
            {
                // expected
            }
        }
        finally
        {
            Arrays.fill(connectionKey, (byte)0);
            Arrays.fill(payload, (byte)0);
            Files.deleteIfExists(keyStore);
        }
    }

    /**
     * Verifies that changing encrypted connection data is rejected.
     *
     * @throws Exception if the security operation fails
     */
    @Test
    public void testTamperedAuthenticationData() throws Exception
    {
        Path keyStore = copyKeyStore();
        JdbcSecurity security = new JdbcSecurity(keyStore.toString(), TestConnection.TEST_SERVER_PRIVATE_KEY_PASSWORD, TestConnection.TEST_SERVER_PRIVATE_KEY_ALIAS, "a-token", null);
        
        byte[] connectionKey = RemoteSecurityWrapper.createConnectionKey();
        byte[] payload = "connect".getBytes(StandardCharsets.UTF_8);

        try
        {
            byte[] encrypted = RemoteSecurityWrapper.encryptHandshake(loadCertificate().getPublicKey(),
                                                               		  "a-token",
                                                               		  connectionKey,
                                                               		  payload);
            encrypted[encrypted.length - 1] ^= 1;

            try
            {
                security.authenticate(encrypted, JdbcSecurity.ENVIRONMENT_DEV);
                
                Assert.fail("Tampered authentication data must be rejected");
            }
            catch (Exception expected)
            {
                // Expected result.
            }
        }
        finally
        {
            Arrays.fill(connectionKey, (byte)0);
            Arrays.fill(payload, (byte)0);
            
            Files.deleteIfExists(keyStore);
        }
    }

    /**
     * Loads the test certificate.
     *
     * @return the certificate
     * @throws Exception if the certificate cannot be loaded
     */
    private X509Certificate loadCertificate() throws Exception
    {
        try (InputStream in = ResourceUtil.getResourceAsStream(TestConnection.TEST_CLIENT_PUBLIC_KEY))
        {
            Assert.assertNotNull("Missing security test certificate", in);

            CertificateFactory factory = CertificateFactory.getInstance("X.509");

            return (X509Certificate)factory.generateCertificate(in);
        }
    }

    /**
     * Copies the test key store to a temporary file.
     *
     * @return the temporary key store path
     * @throws Exception if the key store cannot be copied
     */
    private Path copyKeyStore() throws Exception
    {
        Path target = Files.createTempFile("rjdbc-server-test-copy-", ".p12");

        try (InputStream in = ResourceUtil.getResourceAsStream(TestConnection.TEST_SERVER_PRIVATE_KEY))
        {
            Assert.assertNotNull("Missing security test key store", in);
            
            Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
        }

        return target;
    }
}
