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

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;

/**
 * Helper class for (test) connection creation.
 */
public class TestConnection
{
	public static final String TEST_TOKEN = "rjdbc-test-token";
	
	public static final String TEST_SERVER_PRIVATE_KEY          = "/com/sibvisions/rjdbc/server/rjdbc-server-test.p12";
	public static final String TEST_SERVER_PRIVATE_KEY_PASSWORD = "rjdbc-test-password";
	public static final String TEST_SERVER_PRIVATE_KEY_ALIAS    = "rjdbc-server";
	
	public static final String TEST_CLIENT_PUBLIC_KEY  = "/rjdbc-server-test.crt";
	
	static Properties propDb;
	
	static
	{
		propDb = new Properties();

		try
		{
			try (InputStream is = new FileInputStream(new File(new File("").getAbsolutePath(), "/test/db.properties")))
			{
				propDb.load(is);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	
	/**
	 * Creates a remote JDBC connection.
	 * 
	 * @return the requested value
	 * @throws Exception if the operation fails
	 */
    public static Connection create() throws Exception
    {
        Properties prop = new Properties();
        prop.put("user", propDb.getProperty("oracle.db.user"));
        prop.put("password", propDb.getProperty("oracle.db.pwd"));
        prop.put("jdbcUrl", propDb.getProperty("oracle.db.url"));
        prop.put(RemoteConstants.SERVER_CERTIFICATE, TestConnection.TEST_CLIENT_PUBLIC_KEY);

        return create(prop);
    }
    
    /** 
     * Creates a remote JDBC connection with the supplied properties.
     *
     * @param pProperties the remote JDBC properties
     * @return the resulting connection
     * @throws Exception if the operation fails
     */
    public static Connection create(Properties pProperties) throws Exception
    {
        return DriverManager.getConnection(propDb.getProperty("rjdbc.url"), pProperties);
    }    

	/**
	 * Creates oracle JDBC connection.
	 * 
	 * @return the requested value
	 * @throws Exception if the operation fails
	 */
    public static Connection createOracle() throws Exception
    {
        Properties prop = new Properties();
        prop.put("user", propDb.getProperty("oracle.db.user"));
        prop.put("password", propDb.getProperty("oracle.db.pwd"));
        
        return DriverManager.getConnection(propDb.getProperty("oracle.db.url"),  prop);
    }
}
