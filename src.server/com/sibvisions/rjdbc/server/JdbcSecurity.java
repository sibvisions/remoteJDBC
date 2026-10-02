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

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Enumeration;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import com.sibvisions.util.type.ResourceUtil;
import com.sibvisions.util.type.StringUtil;

/**
 * Provides optional authentication and encryption for the remote JDBC protocol.
 *
 * @author René Jahn
 */
final class JdbcSecurity
{
	public static final String ENVIRONMENT_DEV = "development";
	public static final String ENVIRONMENT_TEST = "test";
	public static final String ENVIRONMENT_PROD = "production";
	
    private static final String RSA_TRANSFORMATION = "RSA/ECB/OAEPWithSHA-256AndMGF1Padding";
    private static final String AES_TRANSFORMATION = "AES/GCM/NoPadding";
    private static final String HMAC_ALGORITHM = "HmacSHA256";
    
    private static final int NONCE_SIZE = 12;
    private static final int GCM_TAG_SIZE = 128;
    
    private final static int SECURITY_PROTOCOL_VERSION = 1;

    private static final SecureRandom RANDOM = new SecureRandom();

    private final PrivateKey privateKey;

    private final byte[] token;
    

    /**
     * Creates a new optional server security configuration.
     *
     * @param pKeyStore the optional PKCS#12 key store
     * @param pKeyStorePassword the key store password
     * @param pKeyAlias the private key alias, or {@code null}
     * @param pToken the optional authentication token
     * @throws Exception if the key store cannot be loaded
     */
    JdbcSecurity(String pKeyStore, String pKeyStorePassword, String pKeyAlias, String pToken) throws Exception
    {
        privateKey = loadPrivateKey(pKeyStore, pKeyStorePassword, pKeyAlias);
        token = StringUtil.isEmpty(pToken) ? null : pToken.getBytes(StandardCharsets.UTF_8);
    }

    /**
     * Returns whether payload encryption is configured.
     *
     * @return {@code true} if a private key is configured
     */
    boolean isEncryptionEnabled()
    {
        return privateKey != null;
    }

    /**
     * Returns whether token authentication is configured.
     *
     * @return {@code true} if a token is configured
     */
    boolean isAuthenticationEnabled()
    {
        return token != null;
    }

    /**
     * Authenticates an optional clear-text connection token.
     *
     * @param pToken the client token, or {@code null}
     * @param pEnvironment the environment
     */
    void authenticateToken(String pToken, String pEnvironment)
    {
        byte[] suppliedToken = pToken == null ? null : pToken.getBytes(StandardCharsets.UTF_8);

        try
        {
            if (token == null)
            {
                if (suppliedToken != null && suppliedToken.length > 0)
                {
                    throw new SecurityException("Remote JDBC authentication with token is not configured");
                }
            }
            else if (isProdEnvironment(pEnvironment))
            {
            	//in prod, a token is required
            	if (StringUtil.isEmpty(pToken))
            	{
            		throw new SecurityException("Remote JDBC authentication without token failed");
            	}
            	
            	if (!MessageDigest.isEqual(token, suppliedToken))
            	{
            		throw new SecurityException("Remote JDBC authentication with token failed");
            	}
            }
            else if (!StringUtil.isEmpty(pToken) && !MessageDigest.isEqual(token, suppliedToken))
            {
            	//not in prod, but token available -> checked
        		throw new SecurityException("Remote JDBC authentication with token failed");
            }
        }
        finally
        {
            if (suppliedToken != null)
            {
                Arrays.fill(suppliedToken, (byte)0);
            }
        }
    }

    /**
     * Decrypts and verifies encrypted connection initialization data.
     *
     * @param pHandshake the encrypted connection initialization data
     * @param pEnvironment the environment
     * @return the authenticated connection data
     * @throws Exception if encryption or authentication fails
     */
    Authentication authenticate(byte[] pHandshake, String pEnvironment) throws Exception
    {
        if (privateKey == null)
        {
            throw new SecurityException("Remote JDBC encryption is not configured");
        }

        DataInputStream in = new DataInputStream(new ByteArrayInputStream(pHandshake));
        int version = in.readUnsignedByte();

        if (version != SECURITY_PROTOCOL_VERSION)
        {
            throw new SecurityException("Unsupported remote JDBC security protocol");
        }

        int encryptedKeyLength = in.readInt();

        if (encryptedKeyLength <= 0 || encryptedKeyLength > 8192)
        {
            throw new SecurityException("Invalid remote JDBC authentication data");
        }

        byte[] encryptedKey = null;
        byte[] nonce = null;
        byte[] encrypted = null;
        byte[] handshakeKey = null;
        byte[] plain = null;
        byte[] suppliedToken = null;
                
        try
        {
        	encryptedKey = new byte[encryptedKeyLength];
        	in.readFully(encryptedKey);

        	nonce = new byte[NONCE_SIZE];
        	in.readFully(nonce);

        	int encryptedLength = in.readInt();

	        if (encryptedLength <= GCM_TAG_SIZE / 8 || encryptedLength > 16 * 1024 * 1024)
	        {
	            throw new SecurityException("Invalid remote JDBC authentication data");
	        }

	        encrypted = new byte[encryptedLength];
	        in.readFully(encrypted);

	        handshakeKey = decryptRsa(encryptedKey);

            plain = decryptAes(handshakeKey, nonce, encrypted, createHandshakeAad());
            
            DataInputStream handshake = new DataInputStream(new ByteArrayInputStream(plain));

            int suppliedTokenLength = handshake.readInt();

            if (suppliedTokenLength < 0 || suppliedTokenLength > 4096)
            {
                throw new SecurityException("Invalid remote JDBC authentication data");
            }

            suppliedToken = new byte[suppliedTokenLength];
            handshake.readFully(suppliedToken);

            if (token == null)
            {
                if (suppliedTokenLength > 0)
                {
                    throw new SecurityException("Remote JDBC authentication with token is not configured");
                }
            }
            else if (isProdEnvironment(pEnvironment))
            {
            	//in prod, a token is required
            	
            	if (suppliedTokenLength == 0)
            	{
            		throw new SecurityException("Remote JDBC authentication without token failed");
            	}
            	
            	if (!MessageDigest.isEqual(token, suppliedToken))
            	{
            		throw new SecurityException("Remote JDBC authentication with token failed");
            	}
            }
            else if (suppliedTokenLength > 0 && !MessageDigest.isEqual(token, suppliedToken))
            {
            	//not in prod, but token available -> checked
        		throw new SecurityException("Remote JDBC authentication with token failed");
            }

            int connectionKeyLength = handshake.readInt();

            if (connectionKeyLength != 32)
            {
                throw new SecurityException("Invalid remote JDBC connection key");
            }

            byte[] connectionKey = new byte[connectionKeyLength];
            handshake.readFully(connectionKey);

            int payloadLength = handshake.readInt();

            if (payloadLength <= 0 || payloadLength > 16 * 1024 * 1024)
            {
                Arrays.fill(connectionKey, (byte)0);
                
                throw new SecurityException("Invalid remote JDBC connection payload");
            }

            byte[] payload = new byte[payloadLength];
            handshake.readFully(payload);

            return new Authentication(connectionKey, payload);
        }
        finally
        {
        	unset(suppliedToken);
        	unset(nonce);
        	unset(handshakeKey);
        	unset(encryptedKey);
        	unset(encrypted);
        	unset(plain);
        }
    }

    /**
     * Unsets all bytes of given array.
     * 
     * @param pArray the array
     */
    private final void unset(byte[] pArray)
    {
    	if (pArray != null)
    	{
            Arrays.fill(pArray, (byte)0);
    	}
    }

    /**
     * Creates authenticated data for a request or response.
     *
     * @param pSessionId the session id
     * @param pSequence the sequence number
     * @param pDirection the direction
     * @return the authenticated data
     */
    byte[] createAad(long pSessionId, long pSequence, String pDirection)
    {
        return ("1:" + pSessionId + ":" + pSequence + ":" + pDirection).getBytes(StandardCharsets.UTF_8);
    }
    
    /**
     * Creates authenticated data for the encrypted connection initialization.
     *
     * @return the authenticated data
     */
    private byte[] createHandshakeAad()
    {
        return "rjdbc:connect:1".getBytes(StandardCharsets.UTF_8);
    }

    /**
     * Decrypts AES-GCM data.
     *
     * @param pKey the encryption key
     * @param pNonce the nonce
     * @param pData the encrypted data
     * @param pAad the authenticated data
     * @return the decrypted data
     * @throws Exception if decryption fails
     */
    byte[] decrypt(byte[] pKey, byte[] pNonce, byte[] pData, byte[] pAad) throws Exception
    {
        Cipher cipher = Cipher.getInstance(AES_TRANSFORMATION);
        cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(pKey, "AES"), new GCMParameterSpec(GCM_TAG_SIZE, pNonce));
        cipher.updateAAD(pAad);

        return cipher.doFinal(pData);
    }

    /**
     * Encrypts AES-GCM data.
     *
     * @param pKey the encryption key
     * @param pNonce the nonce
     * @param pData the data
     * @param pAad the authenticated data
     * @return the encrypted data
     * @throws Exception if encryption fails
     */
    byte[] encrypt(byte[] pKey, byte[] pNonce, byte[] pData, byte[] pAad) throws Exception
    {
        Cipher cipher = Cipher.getInstance(AES_TRANSFORMATION);
        cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(pKey, "AES"), new GCMParameterSpec(GCM_TAG_SIZE, pNonce));
        cipher.updateAAD(pAad);

        return cipher.doFinal(pData);
    }

    /**
     * Creates a fresh nonce.
     *
     * @return the nonce
     */
    byte[] createNonce()
    {
        byte[] nonce = new byte[NONCE_SIZE];
        
        RANDOM.nextBytes(nonce);

        return nonce;
    }

    /**
     * Derives a direction-specific key.
     *
     * @param pConnectionKey the connection key
     * @param pLabel the direction label
     * @return the derived key
     * @throws Exception if key derivation fails
     */
    byte[] deriveKey(byte[] pConnectionKey, String pLabel) throws Exception
    {
        Mac mac = Mac.getInstance(HMAC_ALGORITHM);
        mac.init(new SecretKeySpec(pConnectionKey, "HmacSHA256"));

        return mac.doFinal(pLabel.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Loads the configured private key.
     *
     * @param pKeyStore the optional key store
     * @param pPassword the key store password
     * @param pAlias the optional key alias
     * @return the private key or {@code null}
     * @throws Exception if the configured key cannot be loaded
     */
    private PrivateKey loadPrivateKey(String pKeyStore, String pPassword, String pAlias) throws Exception
    {
        if (StringUtil.isEmpty(pKeyStore))
        {
            return null;
        }

        KeyStore keyStore = KeyStore.getInstance("PKCS12");
        
        char[] password = pPassword == null ? new char[0] : pPassword.toCharArray();

        try (InputStream in = ResourceUtil.getResourceAsStream(pKeyStore))
        {
            keyStore.load(in, password);
        }
        finally
        {
            Arrays.fill(password, '\0');
        }

        String alias = pAlias;

        if (alias == null || alias.trim().isEmpty())
        {
            Enumeration<String> aliases = keyStore.aliases();

            while (aliases.hasMoreElements())
            {
                String candidate = aliases.nextElement();

                if (keyStore.isKeyEntry(candidate))
                {
                    alias = candidate;
                    
                    break;
                }
            }
        }

        if (alias == null || !keyStore.isKeyEntry(alias))
        {
            throw new IllegalArgumentException("No private key found in security key store");
        }

        char[] keyPassword = pPassword == null ? new char[0] : pPassword.toCharArray();
        java.security.Key key;

        try
        {
            key = keyStore.getKey(alias, keyPassword);
        }
        finally
        {
            Arrays.fill(keyPassword, '\0');
        }

        if (!(key instanceof PrivateKey))
        {
            throw new IllegalArgumentException("Security key store entry is not a private key");
        }

        return (PrivateKey)key;
    }

    /**
     * Decrypts an RSA-OAEP encrypted value.
     *
     * @param pData the encrypted value
     * @return the decrypted value
     * @throws Exception if decryption fails
     */
    private byte[] decryptRsa(byte[] pData) throws Exception
    {
        Cipher cipher = Cipher.getInstance(RSA_TRANSFORMATION);
        cipher.init(Cipher.DECRYPT_MODE, privateKey);

        return cipher.doFinal(pData);
    }

    /**
     * Decrypts AES-GCM data.
     *
     * @param pKey the key
     * @param pNonce the nonce
     * @param pData the data
     * @param pAad the authenticated data
     * @return the plaintext
     * @throws Exception if decryption fails
     */
    private byte[] decryptAes(byte[] pKey, byte[] pNonce, byte[] pData, byte[] pAad) throws Exception
    {
        Cipher cipher = Cipher.getInstance(AES_TRANSFORMATION);
        cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(pKey, "AES"), new GCMParameterSpec(GCM_TAG_SIZE, pNonce));
        cipher.updateAAD(pAad);

        return cipher.doFinal(pData);
    }
    
    /**
     * Gets whether given environment is a production environment.
     * 
     * @param pEnvironment the environment
     * @return {@code true} if production environment
     */
    static boolean isProdEnvironment(String pEnvironment)
    {
    	return ENVIRONMENT_PROD.equalsIgnoreCase(pEnvironment);
    }
    
    /**
     * Contains the authenticated connection key and connect payload.
     */
    static final class Authentication
    {
        private final byte[] connectionKey;
        private final byte[] payload;

        Authentication(byte[] pConnectionKey, byte[] pPayload)
        {
            connectionKey = pConnectionKey;
            payload = pPayload;
        }

        byte[] getConnectionKey()
        {
            return connectionKey;
        }

        byte[] getPayload()
        {
            return payload;
        }
    }    
}
