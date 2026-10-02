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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import com.sibvisions.util.type.FileUtil;
import com.sibvisions.util.type.ResourceUtil;

/**
 * Provides cryptographic operations used by the remote JDBC protocol.
 *
 * @author René Jahn
 */
final class RemoteSecurity
{
    private static final String RSA_TRANSFORMATION = "RSA/ECB/OAEPWithSHA-256AndMGF1Padding";
    private static final String AES_TRANSFORMATION = "AES/GCM/NoPadding";
    private static final String HMAC_ALGORITHM = "HmacSHA256";

    static final int CONNECTION_KEY_SIZE = 32;
    static final int NONCE_SIZE = 12;
    static final int GCM_TAG_SIZE = 128;
    
    private final static int SECURITY_PROTOCOL_VERSION = 1;

    private static final SecureRandom RANDOM = new SecureRandom();

    
    private RemoteSecurity()
    {
    }

    /**
     * Creates a cryptographically secure random connection key.
     *
     * @return the connection key
     */
    static byte[] createConnectionKey()
    {
        byte[] key = new byte[CONNECTION_KEY_SIZE];
        
        RANDOM.nextBytes(key);

        return key;
    }

    /**
     * Loads an X.509 certificate from a file.
     *
     * @param pPath the certificate path
     * @return the public key
     * @throws GeneralSecurityException if the certificate cannot be read
     * @throws IOException if the certificate file cannot be read
     */
    static PublicKey loadPublicKey(String pPath) throws GeneralSecurityException, 
    													IOException
    {
        byte[] data = FileUtil.getContent(ResourceUtil.getResourceAsStream(pPath));

        try
        {
            CertificateFactory factory = CertificateFactory.getInstance("X.509");
            X509Certificate certificate = (X509Certificate)factory.generateCertificate(new ByteArrayInputStream(data));

            return certificate.getPublicKey();
        }
        catch (GeneralSecurityException e)
        {
        	//X.509 format -> error
        	if (new String(data, StandardCharsets.US_ASCII).contains("BEGIN CERTIFICATE"))
        	{
        		throw e;
        	}
        	
            KeyFactory factory = KeyFactory.getInstance("RSA");

            return factory.generatePublic(new X509EncodedKeySpec(Base64.getDecoder().decode(data)));
        }
    }

    /**
     * Creates the encrypted connection initialization data.
     *
     * @param pPublicKey the server public key
     * @param pToken the configured authentication token
     * @param pConnectionKey the connection key
     * @param pPayload the serialized connect request
     * @return the encrypted connection initialization data
     * @throws GeneralSecurityException if encryption fails
     * @throws IOException if the data cannot be encoded
     */
    static byte[] encryptHandshake(PublicKey pPublicKey, String pToken, byte[] pConnectionKey, byte[] pPayload) throws GeneralSecurityException, 
    																												   IOException
    {
        byte[] handshakeKey = createConnectionKey();
        byte[] nonce = createNonce();
        byte[] plain = encodeHandshake(pToken, pConnectionKey, pPayload);
        byte[] encrypted = encryptAes(handshakeKey, nonce, plain, createHandshakeAad());
        byte[] encryptedKey = encryptRsa(pPublicKey, handshakeKey);

        try
        {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            
            try (DataOutputStream out = new DataOutputStream(bos))
            {
            	//version
	            out.writeByte(SECURITY_PROTOCOL_VERSION);
	            
	            out.writeInt(encryptedKey.length);
	            out.write(encryptedKey);
	            out.write(nonce);
	            out.writeInt(encrypted.length);
	            out.write(encrypted);
            }
            
            return bos.toByteArray();
        }
        finally
        {
            Arrays.fill(handshakeKey, (byte)0);
            Arrays.fill(nonce, (byte)0);
            Arrays.fill(plain, (byte)0);
            Arrays.fill(encrypted, (byte)0);
            Arrays.fill(encryptedKey, (byte)0);
        }
    }

    /**
     * Creates authenticated data for a session request or response.
     *
     * @param pSessionId the session id
     * @param pSequence the sequence number
     * @param pDirection the direction label
     * @return the authenticated data
     */
    static byte[] createAad(long pSessionId, long pSequence, String pDirection)
    {
        return ("1:" + pSessionId + ":" + pSequence + ":" + pDirection).getBytes(StandardCharsets.UTF_8);
    }
    
    /**
     * Creates authenticated data for the encrypted connection initialization.
     *
     * @return the authenticated data
     */
    static byte[] createHandshakeAad()
    {
        return "rjdbc:connect:1".getBytes(StandardCharsets.UTF_8);
    }

    /**
     * Encrypts data with AES-GCM.
     *
     * @param pKey the AES key
     * @param pNonce the unique nonce
     * @param pData the data
     * @param pAad the additional authenticated data
     * @return the encrypted data
     * @throws GeneralSecurityException if encryption fails
     */
    static byte[] encryptAes(byte[] pKey, byte[] pNonce, byte[] pData, byte[] pAad) throws GeneralSecurityException
    {
        Cipher cipher = Cipher.getInstance(AES_TRANSFORMATION);
        cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(pKey, "AES"), new GCMParameterSpec(GCM_TAG_SIZE, pNonce));

        if (pAad != null)
        {
            cipher.updateAAD(pAad);
        }

        return cipher.doFinal(pData);
    }

    /**
     * Decrypts data with AES-GCM.
     *
     * @param pKey the AES key
     * @param pNonce the nonce
     * @param pData the encrypted data
     * @param pAad the additional authenticated data
     * @return the decrypted data
     * @throws GeneralSecurityException if decryption fails
     */
    static byte[] decryptAes(byte[] pKey, byte[] pNonce, byte[] pData, byte[] pAad) throws GeneralSecurityException
    {
        Cipher cipher = Cipher.getInstance(AES_TRANSFORMATION);
        cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(pKey, "AES"), new GCMParameterSpec(GCM_TAG_SIZE, pNonce));

        if (pAad != null)
        {
            cipher.updateAAD(pAad);
        }

        return cipher.doFinal(pData);
    }

    /**
     * Creates a fresh request nonce.
     *
     * @return the nonce
     */
    static byte[] createNonce()
    {
        byte[] nonce = new byte[NONCE_SIZE];
        
        RANDOM.nextBytes(nonce);

        return nonce;
    }

    /**
     * Derives a direction-specific AES key from the connection key.
     *
     * @param pConnectionKey the connection key
     * @param pLabel the direction label
     * @return the derived key
     * @throws GeneralSecurityException if key derivation fails
     */
    static byte[] deriveKey(byte[] pConnectionKey, String pLabel) throws GeneralSecurityException
    {
        Mac mac = Mac.getInstance(HMAC_ALGORITHM);
        mac.init(new SecretKeySpec(pConnectionKey, "HmacSHA256"));

        return mac.doFinal(pLabel.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Encrypts the small handshake key with RSA-OAEP.
     *
     * @param pPublicKey the public key
     * @param pData the data
     * @return the encrypted data
     * @throws GeneralSecurityException if encryption fails
     */
    private static byte[] encryptRsa(PublicKey pPublicKey, byte[] pData) throws GeneralSecurityException
    {
        Cipher cipher = Cipher.getInstance(RSA_TRANSFORMATION);
        cipher.init(Cipher.ENCRYPT_MODE, pPublicKey);

        return cipher.doFinal(pData);
    }

    /**
     * Encodes handshake data before symmetric encryption.
     *
     * @param pToken the authentication token
     * @param pConnectionKey the connection key
     * @return the encoded handshake
     * @throws IOException if encoding fails
     */
    private static byte[] encodeHandshake(String pToken, byte[] pConnectionKey, byte[] pPayload) throws IOException
    {
        byte[] token = pToken == null ? new byte[0] : pToken.getBytes(StandardCharsets.UTF_8);

        try
        {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            
            try (DataOutputStream out = new DataOutputStream(bos))
            {
	            out.writeInt(token.length);
	            out.write(token);
	            out.writeInt(pConnectionKey.length);
	            out.write(pConnectionKey);
	            out.writeInt(pPayload.length);
	            out.write(pPayload);
            }
            
            return bos.toByteArray();
        }
        finally
        {
            Arrays.fill(token, (byte)0);
        }
    }
}
