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
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.Map;

import com.sibvisions.rad.remote.UniversalSerializer;
import com.sibvisions.util.log.LoggerFactory;

/**
 * Handles authenticated and encrypted remote JDBC HTTP requests.
 * 
 * @author René Jahn
 */
final class JdbcHttpHandler
{
    private static final String REQUEST_DIRECTION = "request";
    private static final String RESPONSE_DIRECTION = "response";    

    //16MB
    private static final long MAX_REQUEST_SIZE = 16L * 1024L * 1024L;

    private static final int MAX_ENVELOPE_SIZE = 16 * 1024 * 1024;
    
    private final JdbcSessionManager sessions;
    
    private final UniversalSerializer serializer = new UniversalSerializer();
    

    /**
     * Creates a new {@code JdbcHttpHandler} instance.
     *
     * @param pSessions the sessions
     */
    JdbcHttpHandler(JdbcSessionManager pSessions)
    {
        sessions = pSessions;
    }

    /**
     * Handles a remote JDBC HTTP request.
     *
     * @param pInput the input
     * @param pOutput the output
     * @throws IOException if the operation fails
     */
    void handle(InputStream pInput, OutputStream pOutput) throws IOException
    {
        JdbcSession session = null;

        Map<String,Object> request = null;
        
        LimitedInputStream limitedInput = new LimitedInputStream(pInput, MAX_REQUEST_SIZE);

        byte[] body = readAll(limitedInput);
        
        boolean created = false;
        boolean requestStarted = false;
        boolean closeSession = false;
        boolean encrypted = false;

        try
        {
            DataInputStream in = new DataInputStream(new ByteArrayInputStream(body));
            
            int marker = in.readInt();

            if (marker == RemoteSecurityProtocol.MAGIC)
            {
            	LoggerFactory.getInstance(getClass()).debug("Encrypted communication");
            	
                byte type = in.readByte();

                if (type == RemoteSecurityProtocol.CONNECT_ENCRYPTED)
                {
                    encrypted = true;

                    int markerLength = in.readInt();

                    if (markerLength <= 0 || markerLength > MAX_ENVELOPE_SIZE)
                    {
                        throw new SecurityException("Invalid remote JDBC connection request");
                    }

                    byte[] connectMarker = new byte[markerLength];
                    in.readFully(connectMarker);

                    Map<String,Object> candidate = readMap(connectMarker);
                    Arrays.fill(connectMarker, (byte)0);

                    if (!"connect".equals(candidate.get("action")))
                    {
                        throw new SecurityException("Invalid remote JDBC connection request");
                    }

                    int encryptedLength = in.readInt();

                    if (encryptedLength <= 0 || encryptedLength > MAX_ENVELOPE_SIZE)
                    {
                        throw new SecurityException("Invalid remote JDBC authentication data");
                    }

                    byte[] handshake = new byte[encryptedLength];
                    in.readFully(handshake);

                    JdbcSecurity.Authentication authentication = sessions.getSecurity().authenticate(handshake, sessions.getEnvironment());
                    Arrays.fill(handshake, (byte)0);
                    
                    byte[] connectionKey = authentication.getConnectionKey();

                    try
                    {
                        request = readMap(authentication.getPayload());

                        if (!"connect".equals(request.get("action")))
                        {
                            throw new SecurityException("Invalid remote JDBC connection request");
                        }

                        session = sessions.create();
                        session.setConnectionKey(connectionKey);
                        
                        connectionKey = null;
                        created = true;
                        
                        request.put("sessionId", session.getId());
                        
                        session.getContext().beginRequest();
                        
                        requestStarted = true;
                    }
                    finally
                    {
                        Arrays.fill(authentication.getPayload(), (byte)0);

                        if (connectionKey != null)
                        {
                            Arrays.fill(connectionKey, (byte)0);
                        }
                    }
                }
                else if (type == RemoteSecurityProtocol.REQUEST_ENCRYPTED)
                {
                    encrypted = true;
                    
                    byte[] encryptedRequest = new byte[in.available()];
                    in.readFully(encryptedRequest);
                    
                    EncryptedRequest decoded = decryptRequest(encryptedRequest);
                    
                    session = decoded.session;
                    request = decoded.request;
                    
                    requestStarted = true;
                }
                else
                {
                    throw new SecurityException("Invalid remote JDBC request type");
                }
            }
            else
            {
            	LoggerFactory.getInstance(getClass()).debug("Standard communication");
            	
                request = readMap(body);
                
                Object action = request.get("action");

                if ("connect".equals(action))
                {
                    sessions.getSecurity().authenticateToken((String)request.remove("token"), sessions.getEnvironment());
                    
                    session = sessions.create();
                    
                    created = true;
                    
                    request.put("sessionId", session.getId());
                    
                    session.getContext().beginRequest();
                    
                    requestStarted = true;
                }
                else
                {
                    long sessionId = ((Number)request.get("sessionId")).longValue();
                    
                    session = sessions.beginRequest(sessionId);
                    
                    requestStarted = true;
                }
            }

            Object requestAction = request.get("action");

            if ("closeSession".equals(requestAction))
            {
                Map<String,Object> response = successResponse();
                response.put("sessionId", session.getId());

                writeResponse(session, response, pOutput, encrypted);
                
                closeSession = true;
            }
            else
            {
                Map<String,Object> response = session.getDispatcher().execute(request);
                
                if (created)
                {
                	response.put("sessionId", session.getId());
                }

                writeResponse(session, response, pOutput, encrypted);

                if (Boolean.FALSE.equals(response.get("success")))
                {
                    closeSession = created;
                }
            }
        }
        catch (JdbcSessionExpiredException e)
        {
        	closeSession(session, created);
        	
        	LoggerFactory.getInstance(getClass()).error(e);
        	
            throw new JdbcHttpException(HttpURLConnection.HTTP_GONE, e.getMessage(), e);
        }
        catch (SecurityException | GeneralSecurityException e)
        {
        	closeSession(session, created);
        	
        	LoggerFactory.getInstance(getClass()).error(e);

            throw new JdbcHttpException(HttpURLConnection.HTTP_UNAUTHORIZED, "Remote JDBC authentication failed", e);
        }
        catch (Exception e)
        {
        	closeSession(session, created);
        	
        	LoggerFactory.getInstance(getClass()).error(e);

            throw new IOException("Remote JDBC request failed", e);
        }
        finally
        {
            if (requestStarted && session != null)
            {
                session.getContext().endRequest();
            }

            closeSession(session, closeSession);

            Arrays.fill(body, (byte)0);
        }
    }
    
    /**
     * Closes given session if necessary.
     * 
     * @param pSession the session
     * @param pClose {@code true} to close session
     */
    private final void closeSession(JdbcSession pSession, boolean pClose)
    {
        if (pClose && pSession != null)
        {
            sessions.close(pSession.getId());
        }
    }
    
    /**
     * Handles a remote JDBC HTTP request.
     *
     * @param pInput the input
     * @param pOutput the output
     * @throws IOException if the operation fails
     	*/
    @SuppressWarnings("unchecked")
    void handleOld(InputStream pInput, OutputStream pOutput) throws IOException
    {
        LimitedInputStream limitedInput = new LimitedInputStream(pInput, MAX_REQUEST_SIZE);
        
        DataInputStream in = new DataInputStream(limitedInput);
        
        Map<String,Object> request;

        try
        {
            Object value = serializer.read(in);

            if (!(value instanceof Map))
            {
                throw new IOException("Remote JDBC request is not a Map");
            }

            request = (Map<String,Object>)value;

            if (limitedInput.isExceeded())
            {
                throw new IOException("Remote JDBC request exceeds maximum size");
            }
        }
        catch (Exception e)
        {
            if (limitedInput.isExceeded())
            {
                throw new IOException("Remote JDBC request exceeds maximum size", e);
            }

            throw new IOException("Remote JDBC request could not be read", e);
        }
        finally
        {
            if (!limitedInput.isDrained())
            {
                try
                {
                    limitedInput.drain();
                }
                catch (IOException ignored)
                {
                }
            }
        }

        String action = (String)request.get("action");
        
        JdbcSession session = null;
        
        boolean created = false;
        
        try
        {
            if ("connect".equals(action))
            {
                session = sessions.create();
                
                created = true;
                
                request.put("sessionId", session.getId());
            }
            else if ("closeSession".equals(action))
            {
                Object sessionValue = request.get("sessionId");

                if (!(sessionValue instanceof Number))
                {
                    throw new IOException("Missing JDBC session id");
                }
                
                sessions.close(((Number)sessionValue).longValue());
                
                Map<String,Object> response = new java.util.HashMap<>();
                response.put("success", Boolean.TRUE);
                
                DataOutputStream out = new DataOutputStream(pOutput);
                serializer.write(out, response);
                out.flush();

                return;
            }
            else
            {
                Object sessionValue = request.get("sessionId");

                if (!(sessionValue instanceof Number))
                {
                    throw new IOException("Missing JDBC session id");
                }
                
                session = sessions.beginRequest(((Number)sessionValue).longValue());
            }

            if (created)
            {
                session.getContext().beginRequest();
            }
            
            try
            {
                Map<String,Object> response = session.getDispatcher().execute(request);

                if (created)
                {
                    response.put("sessionId", session.getId());
                }

                DataOutputStream out = new DataOutputStream(pOutput);
                serializer.write(out, response);
                out.flush();
            }
            finally
            {
                session.getContext().endRequest();
            }
        }
        catch (JdbcSessionExpiredException e)
        {
            if (created && session != null)
            {
                sessions.close(session.getId());
            }
            
            throw new JdbcHttpException(HttpURLConnection.HTTP_GONE, e.getMessage(), e);
        }
        catch (Exception e)
        {
            if (created && session != null)
            {
                sessions.close(session.getId());
            }
            
            throw new IOException("Remote JDBC request failed", e);
        }
    }
    
    /**
     * Decrypts an encrypted request envelope.
     *
     * @param pHeader the serialized encrypted request header
     * @return the decrypted request
     * @throws Exception if the request is invalid
     */
    @SuppressWarnings("unchecked")
    private EncryptedRequest decryptRequest(byte[] pHeader) throws Exception
    {
        DataInputStream header = new DataInputStream(new ByteArrayInputStream(pHeader));
        
        long sessionId = header.readLong();
        long sequence = header.readLong();
        
        int nonceLength = header.readInt();

        if (nonceLength != 12)
        {
            throw new SecurityException("Invalid remote JDBC request nonce");
        }

        byte[] nonce = new byte[nonceLength];
        header.readFully(nonce);

        int payloadLength = header.readInt();

        if (payloadLength <= 16 || payloadLength > MAX_ENVELOPE_SIZE)
        {
            throw new SecurityException("Invalid remote JDBC request payload");
        }

        byte[] encrypted = new byte[payloadLength];
        header.readFully(encrypted);

        JdbcSession session = sessions.get(sessionId);
        byte[] connectionKey = session.getConnectionKey();

        if (connectionKey == null)
        {
            throw new SecurityException("Remote JDBC session is not authenticated");
        }

        JdbcSecurity security = sessions.getSecurity();
        
        byte[] requestKey = security.deriveKey(connectionKey, REQUEST_DIRECTION);
        byte[] aad = security.createAad(sessionId, sequence, REQUEST_DIRECTION);
        byte[] plain = security.decrypt(requestKey, nonce, encrypted, aad);

        try
        {
        	//will throw an exception, so before beginRequest
        	Object value = readMap(plain);

            sessions.beginRequest(sessionId, sequence);

            return new EncryptedRequest(session, (Map<String,Object>)value);
        }
        finally
        {
            Arrays.fill(requestKey, (byte)0);
            Arrays.fill(plain, (byte)0);
        }
    }    
    
    /**
     * Creates a successful empty response.
     *
     * @return the response
     */
    private Map<String,Object> successResponse()
    {
        Map<String,Object> response = new java.util.HashMap<>();
        response.put("success", Boolean.TRUE);

        return response;
    }   
    
    /**
     * Writes either an encrypted or an unencrypted response.
     *
     * @param pSession the session
     * @param pResponse the response
     * @param pOutput the output
     * @param pEncrypted whether the response must be encrypted
     * @throws Exception if writing fails
     */
    private void writeResponse(JdbcSession pSession, Map<String,Object> pResponse, OutputStream pOutput, boolean pEncrypted) throws Exception
    {
        if (pEncrypted)
        {
            writeEncryptedResponse(pSession, pResponse, pOutput);
        }
        else
        {
            byte[] plain = serialize(pResponse);

            try
            {
                pOutput.write(plain);
                pOutput.flush();
            }
            finally
            {
                Arrays.fill(plain, (byte)0);
            }
        }
    }    
    
    /**
     * Writes an encrypted response.
     *
     * @param pSession the session
     * @param pResponse the response
     * @param pOutput the output
     * @throws Exception if encryption or writing fails
     */
    private void writeEncryptedResponse(JdbcSession pSession, Map<String,Object> pResponse, OutputStream pOutput) throws Exception
    {
        long sequence = pSession.nextResponseSequence();
        
        byte[] plain = serialize(pResponse);
        byte[] nonce = sessions.getSecurity().createNonce();
        byte[] key = sessions.getSecurity().deriveKey(pSession.getConnectionKey(), RESPONSE_DIRECTION);

        try
        {
            byte[] aad = sessions.getSecurity().createAad(pSession.getId(), sequence, RESPONSE_DIRECTION);
            byte[] encrypted = sessions.getSecurity().encrypt(key, nonce, plain, aad);

            DataOutputStream out = new DataOutputStream(pOutput);
            out.writeLong(pSession.getId());
            out.writeLong(sequence);
            out.writeInt(nonce.length);
            out.write(nonce);
            out.writeInt(encrypted.length);
            out.write(encrypted);
            out.flush();
        }
        finally
        {
            Arrays.fill(key, (byte)0);
            Arrays.fill(plain, (byte)0);
        }
    }    
    
    /**
     * Reads all request bytes.
     *
     * @param pInput the limited input
     * @return the bytes
     * @throws IOException if reading fails
     */
    private byte[] readAll(LimitedInputStream pInput) throws IOException
    {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        
        byte[] buffer = new byte[8192];
        int count;

        try
        {
            while ((count = pInput.read(buffer)) >= 0)
            {
                if (count > 0)
                {
                    output.write(buffer, 0, count);
                }
            }

            if (pInput.isExceeded())
            {
                throw new IOException("Remote JDBC request exceeds maximum size");
            }

            return output.toByteArray();
        }
        finally
        {
            if (!pInput.isDrained())
            {
                try
                {
                    pInput.drain();
                }
                catch (IOException ignored)
                {
                }
            }
        }
    }    
    
    /**
     * Reads a serialized request map.
     *
     * @param pData the serialized map
     * @return the map
     * @throws Exception if the map cannot be read
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> readMap(byte[] pData) throws Exception
    {
        Object value = serializer.read(new DataInputStream(new ByteArrayInputStream(pData)));

        if (!(value instanceof Map))
        {
            throw new IOException("Invalid remote JDBC request payload (Map)");
        }

        return (Map<String, Object>)value;
    }

    /**
     * Serializes a response.
     *
     * @param pValue the response
     * @return the serialized response
     * @throws Exception if serialization fails
     */
    private byte[] serialize(Object pValue) throws Exception
    {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        
        DataOutputStream dataout = new DataOutputStream(output);
        serializer.write(dataout, pValue);
        dataout.flush();

        return output.toByteArray();
    }    
    
    /**
     * Limits the number of bytes consumed from a remote request.
     */
    private static final class LimitedInputStream extends FilterInputStream
    {
        private long remaining;

        private boolean exceeded;

        private boolean drained;

        /**
         * Creates a limited input stream.
         *
         * @param pInput the input stream
         * @param pLimit the maximum number of bytes
         */
        LimitedInputStream(InputStream pInput, long pLimit)
        {
            super(pInput);
            
            remaining = pLimit;
        }

        @Override
        public int read() throws IOException
        {
            if (remaining == 0)
            {
                int value = super.read();

                if (value >= 0)
                {
                    exceeded = true;
                }

                return -1;
            }

            int value = super.read();

            if (value >= 0)
            {
                remaining--;
            }

            return value;
        }

        @Override
        public int read(byte[] pBuffer, int pOffset, int pLength) throws IOException
        {
            if (pLength == 0)
            {
                return 0;
            }

            if (remaining == 0)
            {
                int value = super.read();

                if (value >= 0)
                {
                    exceeded = true;
                }

                return -1;
            }

            int length = (int)Math.min(pLength, remaining);
            int count = super.read(pBuffer, pOffset, length);

            if (count > 0)
            {
                remaining -= count;
            }

            return count;
        }

        /**
         * Returns whether the input exceeded the configured limit.
         *
         * @return {@code true} if more data than allowed was received
         */
        boolean isExceeded()
        {
            return exceeded;
        }

        /**
         * Returns whether the complete input has already been consumed.
         *
         * @return {@code true} if the input has been drained
         */
        boolean isDrained()
        {
            return drained;
        }

        /**
         * Consumes the remaining input until EOF.
         *
         * @throws IOException if the input cannot be consumed
         */
        void drain() throws IOException
        {
            byte[] buffer = new byte[8192];

            while (remaining > 0)
            {
                int length = (int)Math.min(buffer.length, remaining);
                int count = super.read(buffer, 0, length);

                if (count < 0)
                {
                    drained = true;

                    return;
                }

                if (count > 0)
                {
                    remaining -= count;
                }
            }

            int value = super.read();

            if (value >= 0)
            {
                exceeded = true;

                //"clear stream"
                while (super.read(buffer) >= 0)
                {
                }
            }

            drained = true;
        }
    }
    
    private static final class RemoteSecurityProtocol
    {
        private static final int MAGIC = 0x524A4443;
        private static final byte CONNECT_ENCRYPTED = 1;
        private static final byte REQUEST_ENCRYPTED = 2;
    }

    private static final class EncryptedRequest
    {
        private final JdbcSession session;
        private final Map<String,Object> request;

        private EncryptedRequest(JdbcSession pSession, Map<String,Object> pRequest)
        {
            session = pSession;
            request = pRequest;
        }
    }    
}
