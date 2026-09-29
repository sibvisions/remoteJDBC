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

import java.sql.Driver;
import java.sql.DriverManager;

import org.junit.Test;

/**
 * Supports the test scenario by handling main.
 */
public final class DriverTest
{
    /**
	 * Supports the test scenario by handling main.
	 * 
	 * @param pArgs the arguments
	 * @throws Exception if the operation fails
	 */
    public static void main(String[] pArgs) throws Exception
    {
        Driver driver = new RemoteDriver();

        if (!driver.acceptsURL("jdbc:rjdbc:https://localhost/remote-jdbc"))
        {
            throw new AssertionError("URL not accepted");
        }
        
        if (driver.acceptsURL("jdbc:postgresql://localhost/test"))
        {
            throw new AssertionError("PostgreSQL URL incorrectly accepted");
        }
        
        if (DriverManager.getDriver("jdbc:rjdbc:https://localhost/remote-jdbc").getClass() != RemoteDriver.class)
        {
            throw new AssertionError("Driver not registered");
        }
        
        System.out.println("OK");
    }

    /**
	 * Verifies the JDBC behavior for database meta data.
	 * 
	 * @throws Exception if the operation fails
	 */
    @Test
    public void testDatabaseMetaData() throws Exception
    {
    	/*
         Class<?> driverClass = Class.forName("com.sibvisions.rjdbc.RemoteDriver");

         Driver customDriver = (Driver) driverClass.getDeclaredConstructor().newInstance();

         DriverManager.registerDriver(customDriver);

         Properties prop = new Properties();
         prop.put("user", TestConnection.propDb.getProperty("pgsql.db.user"));
         prop.put("password", TestConnection.propDb.getProperty("pgsql.db.pwd"));

         Connection connection = DriverManager.getConnection(TestConnection.propDb.getProperty("pgsql.db.url"), prop);
         
         //same with rjdbc
         //prop.put("jdbcUrl", TestConnection.propDb.getProperty("pgsql.db.url"));
         //Connection connection = DriverManager.getConnection(TestConnection.propDb.getProperty("rjdbc.url"), prop);

         System.out.println(connection.getMetaData().getURL());
         System.out.println(connection.getMetaData().getUserName());
         System.out.println(connection.getCatalog());
         System.out.println(connection.getSchema());
         System.out.println(connection.getMetaData().getDatabaseProductName());
         */
        
    }
}
