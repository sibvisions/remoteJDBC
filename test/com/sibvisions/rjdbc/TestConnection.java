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
	 * Creates the requested test object for use by the tests.
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

        return DriverManager.getConnection(propDb.getProperty("rjdbc.url"),  prop);
    }

	/**
	 * Creates oracle for use by the tests.
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
