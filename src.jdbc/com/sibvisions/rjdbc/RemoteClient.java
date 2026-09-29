/*
 * Copyright (C)2026 SIB Visions GmbH
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

import com.sibvisions.rad.remote.UniversalSerializer;

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
import java.sql.SQLException;
import java.util.Map;

/**
     * Handles the serialize operation for the remote JDBC resource.
 */
final class RemoteClient
{
    private final URI endpoint;
    
    private final HttpClient http;
    
    private final UniversalSerializer serializer = new UniversalSerializer();
    
    private volatile long sessionId;
    
    private volatile boolean sessionBroken;
    
    private volatile boolean connectedOnce;

    /**
     * Creates a new {@code RemoteClient} instance.
     *
     * @param pUrl the url
     * @throws SQLException if the operation fails
     */
    RemoteClient(String pUrl) throws SQLException
    {
        try
        {
            endpoint = URI.create(pUrl);
            
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
     * Handles the suppress warnings operation for the remote JDBC resource.
     *
     * @param pRequest the request
     * @return the resulting JDBC value
     * @throws SQLException if the JDBC operation cannot be completed
     */
    @SuppressWarnings("unchecked")
    Map<String, Object> call(Map<String, Object> pRequest) throws SQLException
    {
        try
        {
            if (sessionBroken && !RemoteConstants.CONNECT.equals(pRequest.get(RemoteConstants.ACTION)))
            {
            	if (!connectedOnce)
            	{
            		throw new SQLException("Remote JDBC connection can't be established");
            	}
            	
        		connectedOnce = false;
            	
                throw new SQLException("Remote JDBC connection is no longer available");
            }

            if (sessionId != 0 && !RemoteConstants.CONNECT.equals(pRequest.get(RemoteConstants.ACTION)))
            {
                pRequest.put(RemoteConstants.SESSION_ID, sessionId);
            }

            byte[] payload = serialize(pRequest);

            HttpRequest httpRequest = HttpRequest.newBuilder(endpoint)
							            .header("Content-Type", "application/octet-stream")
							            .header("Accept", "application/octet-stream")
							            .POST(HttpRequest.BodyPublishers.ofByteArray(payload))
							            .build();

            HttpResponse<byte[]> response = http.send(httpRequest, HttpResponse.BodyHandlers.ofByteArray());

            if (response.statusCode() == HttpURLConnection.HTTP_GONE)
            {
                sessionId = 0;
                sessionBroken = true;
                
                throw new SQLException("Remote JDBC session expired or is no longer available");
            }

            if (response.statusCode() != HttpURLConnection.HTTP_OK)
            {
                throw new SQLException("Remote JDBC HTTP status: " + response.statusCode());
            }

            Object value = deserialize(response.body());

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
            
            connectedOnce = true;

            return result;
        }
        catch (SQLException e)
        {
            throw e;
        }
        catch (InterruptedException e)
        {
            Thread.currentThread().interrupt();
            
            throw new SQLException("Remote JDBC request interrupted", e);
        }
        catch (IOException e)
        {
            if (sessionId != 0)
            {
                sessionId = 0;
                sessionBroken = true;

            }

        	if (!connectedOnce)
        	{
        		throw new SQLException("Remote JDBC connection can't be established");
        	}
        	
    		connectedOnce = false;
            
            throw new SQLException("Remote JDBC connection is no longer available", e);
        }
        catch (Exception e)
        {
            throw new SQLException("Remote JDBC communication failed", e);
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
            sessionBroken = false;

            return;
        }

        Map<String,Object> request = new java.util.HashMap<String,Object>();
        request.put(RemoteConstants.ACTION, RemoteConstants.CLOSE_SESSION);
        
        try
        {
            call(request);
        }
        finally
        {
            sessionId = 0;
            sessionBroken = false;
        }

    }

	/**
	 * Returns whether session broken.
	 * 
	 * @return whether the condition is true
	 */
    boolean isSessionBroken()
    {
        return sessionBroken;
    }

	/**
	 * Returns whether session closed.
	 * 
	 * @return whether the condition is true
	 */
    boolean isSessionClosed()
    {
        return sessionId == 0;
    }

	/**
	 * Handles the serialize operation for the remote JDBC resource.
	 *
	 * @param pObject the object
	 * @return the resulting JDBC value
	 * @throws Exception if the operation fails
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
     * Handles the deserialize operation for the remote JDBC resource.
     *
     * @param pBytes the bytes
     * @return the resulting JDBC value
     * @throws Exception if the operation fails
     */
	private Object deserialize(byte[] pBytes) throws Exception
    {
        DataInputStream dis = new DataInputStream(new ByteArrayInputStream(pBytes));

        return serializer.read(dis);
    }

	/**
	 * Handles the remote exception operation for the remote JDBC resource.
	 *
	 * @param pError the error
	 * @return the resulting JDBC value
	 */
	private SQLException remoteException(Object pError)
    {
        if (pError instanceof SQLException)
        {
            return (SQLException)pError;
        }

        return new SQLException(String.valueOf(pError));
    }
}
