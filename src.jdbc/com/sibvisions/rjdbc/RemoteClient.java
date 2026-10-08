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
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.channels.ClosedChannelException;
import java.security.PublicKey;
import java.sql.SQLException;
import java.time.Duration;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import com.sibvisions.rad.remote.UniversalSerializer;
import com.sibvisions.util.log.LoggerFactory;
import com.sibvisions.util.type.ExceptionUtil;

/**
 * Handles communication for a remote JDBC resource.
 * 
 * @author René Jahn
 */
final class RemoteClient
{
    private static final String REQUEST_DIRECTION = "request";
    private static final String RESPONSE_DIRECTION = "response";	
	
	
    private final URI endpoint;
    
    private final HttpClient http;

    private final Duration requestTimeout;
    
    private final UniversalSerializer serializer = new UniversalSerializer();
    
    private final PublicKey serverPublicKey;

    private final String token;    
    
    private final AtomicLong requestSequence = new AtomicLong();

    private final AtomicLong responseSequence = new AtomicLong();    

    private volatile byte[] connectionKey;

    private volatile byte[] requestKey;

    private volatile byte[] responseKey;
    
    private volatile long sessionId;
    
    private volatile boolean sessionBroken;
    
    private volatile boolean connectedOnce;

    
    /**
     * Creates a new {@code RemoteClient} instance.
     *
     * @param pUrl the url
     * @param pRequestTimeoutMillis the HTTP request timeout in milliseconds
     * @param pServerPublicKey the server public key
     * @param pToken the authentication token 
     * @throws SQLException if the operation fails
     */
    RemoteClient(String pUrl, long pRequestTimeoutMillis, PublicKey pServerPublicKey, String pToken) throws SQLException
    {
        try
        {
            endpoint = URI.create(pUrl);
            requestTimeout = pRequestTimeoutMillis > 0 ? Duration.ofMillis(pRequestTimeoutMillis) : null;
            serverPublicKey = pServerPublicKey;
            token = pToken;
            
            http = HttpClient.newBuilder()
        				.version(HttpClient.Version.HTTP_1_1)
        				.build();
        }
        catch (RuntimeException e)
        {
            throw new SQLException("Invalid remote JDBC URL: " + pUrl, e);
        }
    }

    /**
     * Executes a remote JDBC request.
     *
     * @param pPayload the request payload
     * @return the resulting JDBC value
     * @throws SQLException if the JDBC operation cannot be completed
     */
    @SuppressWarnings("unchecked")
    synchronized Map<String, Object> call(Map<String, Object> pPayload) throws SQLException
    {
        byte[] newConnectionKey = null;

        boolean isConnect = RemoteConstants.CONNECT.equals(pPayload.get(RemoteConstants.ACTION));
        boolean connectionKeyCommitted = false;
        
        try
        {
            if (sessionBroken && !isConnect)
            {
            	if (!connectedOnce)
            	{
            		throw new SQLException("Remote JDBC connection can't be established (Server not available?)");
            	}
            	
        		connectedOnce = false;
            	
                throw new SQLException("Remote JDBC connection is no longer available");
            }

            if (!isConnect)
            {
                if (sessionId == 0)
                {
                    throw new SQLException("Remote JDBC session is not established");
                }
            
                if (serverPublicKey != null && connectionKey == null)
                {
                    throw new SQLException("Remote JDBC encrypted session is not established");
                }
                
                pPayload.put(RemoteConstants.SESSION_ID, sessionId);
            }

            byte[] payload = serialize(pPayload);
            byte[] requestBody;
            
            long sequence = 0;

            try
            {
                if (isConnect && serverPublicKey != null)
                {
                    newConnectionKey = RemoteSecurity.createConnectionKey();
                    
                    byte[] handshake = RemoteSecurity.encryptHandshake(serverPublicKey, token, newConnectionKey, payload);
                    byte[] connectMarker = serialize(createConnectMarker());
                    
                    requestBody = createConnectEnvelope(connectMarker, handshake);

                    Arrays.fill(handshake, (byte)0);
                    Arrays.fill(connectMarker, (byte)0);
                }
                else if (!isConnect && requestKey != null)
                {
                    sequence = requestSequence.incrementAndGet();
                    
                    byte[] nonce = RemoteSecurity.createNonce();
                    byte[] aad = RemoteSecurity.createAad(sessionId, sequence, REQUEST_DIRECTION);
                    byte[] encrypted = RemoteSecurity.encryptAes(requestKey, nonce, payload, aad);

                    requestBody = createEnvelope(sessionId, sequence, nonce, encrypted);
                }
                else
                {
                    requestBody = payload.clone();
                }
            }
            finally
            {
                Arrays.fill(payload, (byte)0);
            }
            

            HttpRequest.Builder requestBuilder = HttpRequest.newBuilder(endpoint)
                    .header("Content-Type", "application/octet-stream")
                    .header("Accept", "application/octet-stream")
                    .POST(HttpRequest.BodyPublishers.ofByteArray(requestBody));

            if (requestTimeout != null)
            {
                requestBuilder.timeout(requestTimeout);
            }

            HttpResponse<byte[]> response = http.send(requestBuilder.build(), HttpResponse.BodyHandlers.ofByteArray());

            if (response.statusCode() == HttpURLConnection.HTTP_GONE)
            {
                clearSecurityState();
                
                sessionBroken = true;

                throw new SQLException("Remote JDBC session expired or is no longer available");
            }

            if (response.statusCode() != HttpURLConnection.HTTP_OK)
            {
            	byte[] body = response.body();
            	
            	if (body != null)
            	{
            		LoggerFactory.getInstance(getClass()).error(new String(body));
            	}
            	
                throw new SQLException("Remote JDBC HTTP status: " + response.statusCode());
            }

            byte[] responsePayload;

            if (isConnect && serverPublicKey != null)
            {
                EncryptedEnvelope envelope = readEnvelope(response.body());

                if (envelope.sessionId == 0 || envelope.sequence != 1)
                {
                    throw new SQLException("Invalid remote JDBC connection response");
                }

                byte[] connectResponseKey = RemoteSecurity.deriveKey(newConnectionKey, RESPONSE_DIRECTION);

                try
                {
                    byte[] aad = RemoteSecurity.createAad(envelope.sessionId, envelope.sequence, RESPONSE_DIRECTION);
                    
                    responsePayload = RemoteSecurity.decryptAes(connectResponseKey, envelope.nonce, envelope.payload, aad);
                }
                finally
                {
                    Arrays.fill(connectResponseKey, (byte)0);
                }

                sessionId = envelope.sessionId;
            }
            else if (!isConnect && responseKey != null)
            {
                EncryptedEnvelope envelope = readEnvelope(response.body());

                if (envelope.sessionId != sessionId)
                {
                    throw new SQLException("Invalid remote JDBC response session");
                }

                if (envelope.sequence != responseSequence.get() + 1)
                {
                    throw new SQLException("Invalid remote JDBC response sequence");
                }

                byte[] aad = RemoteSecurity.createAad(sessionId, envelope.sequence, RESPONSE_DIRECTION);
                
                responsePayload = RemoteSecurity.decryptAes(responseKey, envelope.nonce, envelope.payload, aad);
                responseSequence.set(envelope.sequence);
            }
            else
            {
                responsePayload = response.body();
            }

            Object value = deserialize(responsePayload);

            if (!(value instanceof Map))
            {
                throw new SQLException("Invalid remote JDBC response");
            }

            Map<String, Object> result = (Map<String, Object>)value;
            
            Object returnedSessionId = result.get(RemoteConstants.SESSION_ID);

            if (returnedSessionId instanceof Number)
            {
                sessionId = ((Number)returnedSessionId).longValue();
            }

            Object success = result.get(RemoteConstants.SUCCESS);

            if (Boolean.FALSE.equals(success))
            {
                throw remoteException(result.get(RemoteConstants.ERROR));
            }
            
            if (isConnect)
            {
                connectionKey = newConnectionKey;
                
                if (serverPublicKey != null)
                {
	                requestKey = RemoteSecurity.deriveKey(connectionKey, REQUEST_DIRECTION);
	                responseKey = RemoteSecurity.deriveKey(connectionKey, RESPONSE_DIRECTION);
                }
                
                requestSequence.set(0);
                responseSequence.set(1);
                
                connectionKeyCommitted = true;
            }            
            
            connectedOnce = true;

            return result;
        }
        catch (SQLException e)
        {
        	unset(newConnectionKey, connectionKeyCommitted);
        	
            throw e;
        }
        catch (InterruptedException e)
        {
        	unset(newConnectionKey, connectionKeyCommitted);
        	
            Thread.currentThread().interrupt();
            
            throw new SQLException("Remote JDBC request interrupted", e);
        }
        catch (IOException e)
        {
        	unset(newConnectionKey, connectionKeyCommitted);
        	
            if (sessionId != 0)
            {
            	clearSecurityState();
                
                sessionBroken = true;
            }

            LoggerFactory.getInstance(getClass()).error(e);
            
        	if (!connectedOnce)
        	{
        		if (ExceptionUtil.getRootCause(e) instanceof ClosedChannelException)
        		{
	        		//no root cause here, because most tools will show the root-cause of the exception but it's 
	        		//just a connection exception
	        		throw new SQLException("Remote JDBC connection can't be established (Server not available?)");
        		}
        		else
        		{
        			throw new SQLException("Remote JDBC connection can't be established", e);
        		}
        	}
        	
    		connectedOnce = false;
            
            throw new SQLException("Remote JDBC connection is no longer available", e);
        }
        catch (Exception e)
        {
        	unset(newConnectionKey, connectionKeyCommitted);
        	
            throw new SQLException("Remote JDBC communication failed", e);
        }
    }
    
    /**
     * Unsets the given key if not commited.
     * 
     * @param pKey the key
     * @param pCommited the commited state
     */
    private final void unset(byte[] pKey, boolean pCommited)
    {
    	//unset memory (avoid reading with e.g. VisualVM)
        if (!pCommited && pKey != null)
        {
            Arrays.fill(pKey, (byte)0);
        }        	
    }

	/**
	 * Closes session and releases its associated resources.
	 * 
	 * @throws SQLException if the JDBC operation cannot be completed
	 */
	void closeSession() throws SQLException
    {
        if (sessionId == 0)
        {
        	clearSecurityState();
        	
        	connectedOnce = false;
            sessionBroken = false;

            return;
        }

        Map<String,Object> request = new HashMap<String,Object>();
        request.put(RemoteConstants.ACTION, RemoteConstants.CLOSE_SESSION);
        
        try
        {
            call(request);
        }
        finally
        {
        	clearSecurityState();
        	
        	connectedOnce = false;
            sessionBroken = false;
        }
    }

    /**
     * Returns whether the session is broken.
     *
     * @return {@code true} if the session is broken
     */
    boolean isSessionBroken()
    {
        return sessionBroken;
    }

   /**
     * Returns whether the session is closed.
     *
     * @return {@code true} if the session is closed
     */
    boolean isSessionClosed()
    {
        return sessionId == 0;
    }

    /**
     * Serializes an object.
     *
     * @param pObject the object
     * @return the serialized object
     * @throws Exception if serialization fails
     */
	private byte[] serialize(Object pObject) throws Exception
    {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);
        
        serializer.write(dos, pObject);
        
        dos.flush();

        return bos.toByteArray();
    }

    /**
     * Deserializes an object.
     *
     * @param pBytes the bytes
     * @return the deserialized object
     * @throws Exception if deserialization fails
     */
	private Object deserialize(byte[] pBytes) throws Exception
    {
        DataInputStream dis = new DataInputStream(new ByteArrayInputStream(pBytes));

        return serializer.read(dis);
    }

	/**
	 * Converts a remote error into an SQL exception.
	 *
	 * @param pError the error
	 * @return the SQL exception
	 */
	private SQLException remoteException(Object pError)
    {
        if (pError instanceof SQLException)
        {
            return (SQLException)pError;
        }

        return new SQLException(String.valueOf(pError));
    }
	
    /**
     * Creates the unencrypted marker used to identify a connection request.
     *
     * @return the connection marker
     */
    private Map<String,Object> createConnectMarker()
    {
        Map<String,Object> marker = new HashMap<>();
        marker.put(RemoteConstants.ACTION, RemoteConstants.CONNECT);

        return marker;
    }

    /**
     * Creates a connection envelope.
     *
     * @param pPayload the serialized request
     * @param pHandshake the encrypted handshake
     * @return the envelope
     * @throws IOException if encoding fails
     */
    private byte[] createConnectEnvelope(byte[] pPayload, byte[] pHandshake) throws IOException
    {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        
        try (DataOutputStream out = new DataOutputStream(bos))
        {
	        out.writeInt(RemoteConstants.SECURITY_MAGIC);
	        out.writeByte(RemoteConstants.SECURITY_CONNECT);
	        out.writeInt(pPayload.length);
	        out.write(pPayload);
	        out.writeInt(pHandshake.length);
	        out.write(pHandshake);
        }

        return bos.toByteArray();
    }

    /**
     * Creates an encrypted request envelope.
     *
     * @param pSessionId the session id
     * @param pSequence the sequence
     * @param pNonce the nonce
     * @param pPayload the encrypted payload
     * @return the envelope
     * @throws IOException if encoding fails
     */
    private byte[] createEnvelope(long pSessionId, long pSequence, byte[] pNonce, byte[] pPayload) throws IOException
    {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        
        try (DataOutputStream out = new DataOutputStream(bos))
        {
	        out.writeInt(RemoteConstants.SECURITY_MAGIC);
	        out.writeByte(RemoteConstants.SECURITY_REQUEST);
	        out.writeLong(pSessionId);
	        out.writeLong(pSequence);
	        out.writeInt(pNonce.length);
	        out.write(pNonce);
	        out.writeInt(pPayload.length);
	        out.write(pPayload);
        }

        return bos.toByteArray();
    }

    /**
     * Reads an encrypted response envelope.
     *
     * @param pData the envelope
     * @return the parsed envelope
     * @throws IOException if the envelope is invalid
     */
    private EncryptedEnvelope readEnvelope(byte[] pData) throws IOException
    {
        DataInputStream in = new DataInputStream(new ByteArrayInputStream(pData));
        
        long id = in.readLong();
        long sequence = in.readLong();
        
        int nonceLength = in.readInt();

        if (nonceLength != RemoteSecurity.NONCE_SIZE)
        {
            throw new IOException("Invalid remote JDBC response nonce");
        }

        byte[] nonce = new byte[nonceLength];
        
        in.readFully(nonce);

        int payloadLength = in.readInt();

        if (payloadLength <= 16 || payloadLength > 16 * 1024 * 1024)
        {
            throw new IOException("Invalid remote JDBC response payload");
        }

        byte[] payload = new byte[payloadLength];
        
        in.readFully(payload);

        return new EncryptedEnvelope(id, sequence, nonce, payload);
    }

    /**
     * Clears sensitive session state.
     */
    private void clearSecurityState()
    {
    	sessionId = 0;
    	
        if (connectionKey != null)
        {
            Arrays.fill(connectionKey, (byte)0);
            connectionKey = null;
        }

        if (requestKey != null)
        {
            Arrays.fill(requestKey, (byte)0);
            requestKey = null;
        }

        if (responseKey != null)
        {
            Arrays.fill(responseKey, (byte)0);
            responseKey = null;
        }
    }	
	
    private static final class EncryptedEnvelope
    {
        private final long sessionId;
        private final long sequence;
        
        private final byte[] nonce;
        private final byte[] payload;

        private EncryptedEnvelope(long pSessionId, long pSequence, byte[] pNonce, byte[] pPayload)
        {
            sessionId = pSessionId;
            sequence = pSequence;
            nonce = pNonce;
            payload = pPayload;
        }
    }	
}
