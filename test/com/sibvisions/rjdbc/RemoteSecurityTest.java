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

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Arrays;

import javax.crypto.AEADBadTagException;

import org.junit.Assert;
import org.junit.Test;

/**
 * Tests the cryptographic operations used by the remote JDBC protocol.
 *
 * @author René Jahn
 */
public class RemoteSecurityTest
{
    /**
     * Verifies that encrypted connection initialization data can be decrypted
     * and that the payload is protected by AES-GCM authentication.
     *
     * @throws Exception if the security operation fails
     */
    @Test
    public void testEncryptedConnectionInitialization() throws Exception
    {
        X509Certificate certificate = loadCertificate();
        
        byte[] connectionKey = RemoteSecurity.createConnectionKey();
        byte[] payload = "connect-payload".getBytes(StandardCharsets.UTF_8);

        try
        {
            byte[] encrypted = RemoteSecurity.encryptHandshake(certificate.getPublicKey(),
                                                               "test-token",
                                                               connectionKey,
                                                               payload);

            Assert.assertTrue(encrypted.length > payload.length);
        }
        finally
        {
            Arrays.fill(connectionKey, (byte)0);
            Arrays.fill(payload, (byte)0);
        }
    }

    /**
     * Verifies that changing authenticated data invalidates AES-GCM data.
     *
     * @throws Exception if the security operation fails
     */
    @Test
    public void testAuthenticatedDataCannotBeChanged() throws Exception
    {
        byte[] key = RemoteSecurity.createConnectionKey();
        byte[] nonce = RemoteSecurity.createNonce();
        byte[] data = "payload".getBytes(StandardCharsets.UTF_8);
        byte[] aad = "aad".getBytes(StandardCharsets.UTF_8);

        try
        {
            byte[] encrypted = RemoteSecurity.encryptAes(key, nonce, data, aad);

            try
            {
                RemoteSecurity.decryptAes(key,
                                          nonce,
                                          encrypted,
                                          "changed-aad".getBytes(StandardCharsets.UTF_8));
                
                Assert.fail("Changed authenticated data must be rejected");
            }
            catch (AEADBadTagException expected)
            {
                // expected
            }
        }
        finally
        {
            Arrays.fill(key, (byte)0);
            Arrays.fill(nonce, (byte)0);
            Arrays.fill(data, (byte)0);
            Arrays.fill(aad, (byte)0);
        }
    }

    /**
     * Verifies that encrypted data cannot be decrypted with another key.
     *
     * @throws Exception if the security operation fails
     */
    @Test
    public void testWrongKeyIsRejected() throws Exception
    {
        byte[] key = RemoteSecurity.createConnectionKey();
        byte[] wrongKey = RemoteSecurity.createConnectionKey();
        byte[] nonce = RemoteSecurity.createNonce();
        byte[] data = "payload".getBytes(StandardCharsets.UTF_8);

        try
        {
            byte[] encrypted = RemoteSecurity.encryptAes(key, nonce, data, null);

            try
            {
                RemoteSecurity.decryptAes(wrongKey, nonce, encrypted, null);
                Assert.fail("A payload encrypted with another key must be rejected");
            }
            catch (AEADBadTagException expected)
            {
                // expected
            }
        }
        finally
        {
            Arrays.fill(key, (byte)0);
            Arrays.fill(wrongKey, (byte)0);
            Arrays.fill(nonce, (byte)0);
            Arrays.fill(data, (byte)0);
        }
    }

    /**
     * Loads the test certificate used for the security protocol tests.
     *
     * @return the test certificate
     * @throws Exception if the certificate cannot be loaded
     */
    private X509Certificate loadCertificate() throws Exception
    {
        try (InputStream in = getClass().getResourceAsStream(TestConnection.TEST_CLIENT_PUBLIC_KEY))
        {
            Assert.assertNotNull("Missing security test certificate", in);

            CertificateFactory factory = CertificateFactory.getInstance("X.509");

            return (X509Certificate)factory.generateCertificate(in);
        }
    }
}
