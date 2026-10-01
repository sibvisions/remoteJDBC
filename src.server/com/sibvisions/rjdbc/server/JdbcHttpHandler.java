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

import com.sibvisions.rad.remote.UniversalSerializer;

import java.io.*;
import java.net.HttpURLConnection;
import java.util.Map;

/**
 * Provides the {@code jdbc http handler} functionality.
 * 
 * @author René Jahn
 */
final class JdbcHttpHandler
{
    //16MB
    private static final long MAX_REQUEST_SIZE = 16L * 1024L * 1024L;

    
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
     * Handles the suppress warnings operation for the remote JDBC resource.
     *
     * @param pInput the input
     * @param pOutput the output
     * @throws IOException if the operation fails
     	*/
    @SuppressWarnings("unchecked")
    void handle(InputStream pInput, OutputStream pOutput) throws IOException
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
}
