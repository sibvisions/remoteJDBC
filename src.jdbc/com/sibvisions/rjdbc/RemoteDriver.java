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

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.PublicKey;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.DriverPropertyInfo;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;

import com.sibvisions.util.type.StringUtil;

/**
 * Remote JDBC implementation of {@code Driver} functionality.
 * 
 * @author René Jahn
 */
public final class RemoteDriver implements Driver
{
    public static final String PREFIX = "jdbc:rjdbc:";

    public static final int MAJOR = 1;
    public static final int MINOR = 12;

    public static final String VERSION = "" + MAJOR + "." + MINOR;

    private static final long DEFAULT_HTTP_REQUEST_TIMEOUT = 300000L;

    
    static
    {
        try
        {
            DriverManager.registerDriver(new RemoteDriver());
        }
        catch (SQLException e)
        {
            throw new ExceptionInInitializerError(e);
        }

    }

    /** {@inheritDoc} */
    @Override
    public Connection connect(String pUrl, Properties pInfo) throws SQLException
    {
        if (!acceptsURL(pUrl))
        {
            throw new SQLException("Invalid remote JDBC url (" + pUrl + ")");
        }

        String endpoint = pUrl.substring(PREFIX.length());

        if (endpoint.isEmpty())
        {
            throw new SQLException("Missing remote JDBC endpoint");
        }

        Properties properties = new Properties();

        if (pInfo != null)
        {
            properties.putAll(pInfo);

        }

        long requestTimeout = DEFAULT_HTTP_REQUEST_TIMEOUT;
        String timeoutValue = properties.getProperty(RemoteConstants.HTTP_REQUEST_TIMEOUT);

        if (timeoutValue != null && !timeoutValue.trim().isEmpty())
        {
            try
            {
                requestTimeout = Long.parseLong(timeoutValue);
            }
            catch (NumberFormatException e)
            {
                throw new SQLException("Invalid HTTP request timeout: " + timeoutValue, e);
            }

            if (requestTimeout < 0)
            {
                throw new SQLException("HTTP request timeout must be >= 0");
            }
        }
        
        String certificate = properties.getProperty(RemoteConstants.SERVER_CERTIFICATE);
        String token = properties.getProperty(RemoteConstants.TOKEN);

        try
        {
        	PublicKey serverPublicKey = null;
        	
        	if (!StringUtil.isEmpty(certificate))
        	{
        		serverPublicKey = RemoteSecurity.loadPublicKey(certificate);
        	}

            return RemoteConnection.connect(new RemoteClient(endpoint, requestTimeout, serverPublicKey, token), endpoint, properties);
        }
        catch (GeneralSecurityException | IOException e)
        {
            throw new SQLException("Could not load remote JDBC server certificate", e);
        }
    }

    /** {@inheritDoc} */
    @Override
    public boolean acceptsURL(String pUrl)
    {
        return pUrl != null && pUrl.startsWith(PREFIX);
    }

    /** {@inheritDoc} */
    @Override
    public DriverPropertyInfo[] getPropertyInfo(String pUrl, Properties pInfo)
    {
        List<DriverPropertyInfo> result = new ArrayList<>();
        
        boolean hasJdbcUrl = false;
        boolean hasHttpRequestTimeout = false;
        boolean hasServerCertificate = false;
        boolean hasToken = false;        

        if (pInfo != null)
        {
            for (String name : pInfo.stringPropertyNames())
            {
                if (RemoteConstants.JDBC_URL.equals(name))
                {
                    hasJdbcUrl = true;
                }
                else if (RemoteConstants.HTTP_REQUEST_TIMEOUT.equals(name))
                {
                    hasHttpRequestTimeout = true;
                }
                else if (RemoteConstants.SERVER_CERTIFICATE.equals(name))
                {
                    hasServerCertificate = true;
                }
                else if (RemoteConstants.TOKEN.equals(name))
                {
                    hasToken = true;
                }                
                
                result.add(new DriverPropertyInfo(name, pInfo.getProperty(name)));
            }

        }

        if (!hasJdbcUrl)
        {
        	// Optional because the server may provide the credentials
        	
            result.add(new DriverPropertyInfo(RemoteConstants.JDBC_URL, null));
        }

        if (!hasHttpRequestTimeout)
        {
        	DriverPropertyInfo info = new DriverPropertyInfo(RemoteConstants.HTTP_REQUEST_TIMEOUT, String.valueOf(DEFAULT_HTTP_REQUEST_TIMEOUT));
        	info.description = "Http request timeout in milliseconds";
        	
            result.add(info);
        }
        
        if (!hasServerCertificate)
        {
        	DriverPropertyInfo info = new DriverPropertyInfo(RemoteConstants.SERVER_CERTIFICATE, null);
        	info.description = "Public server key for encrypted communication";
        			
            result.add(info);
        }

        if (!hasToken)
        {
            DriverPropertyInfo info = new DriverPropertyInfo(RemoteConstants.TOKEN, null);
            info.description = "Authentication token instead of username/password";
            
            result.add(info);
        }        

        return result.toArray(new DriverPropertyInfo[0]);
    }

    /** {@inheritDoc} */
    @Override
    public int getMajorVersion()
    {
        return MAJOR;
    }

    /** {@inheritDoc} */
    @Override
    public int getMinorVersion()
    {
        return MINOR;
    }

    /** {@inheritDoc} */
    @Override
    public boolean jdbcCompliant()
    {
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public Logger getParentLogger()
    {
        return Logger.getLogger("com.sibvisions.rjdbc");
    }
}
