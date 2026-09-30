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
import java.sql.SQLException;

import org.junit.Assert;
import org.junit.Test;

/**
 * Simmple driver verification.
 * 
 * @author René Jahn
 */
public final class DriverTest
{
    /**
	 * Tests JDBC Url.
	 * 
	 * @throws SQLException if the operation fails
	 */
	@Test
    public void testJDBCUrl() throws SQLException
    {
        Driver driver = new RemoteDriver();

        Assert.assertTrue("URL not accepted", driver.acceptsURL("jdbc:rjdbc:https://localhost/remote-jdbc"));
        Assert.assertFalse("PostgreSQL URL incorrectly accepted", driver.acceptsURL("jdbc:postgresql://localhost/test"));
        Assert.assertTrue("Driver not registered", DriverManager.getDriver("jdbc:rjdbc:https://localhost/remote-jdbc").getClass() == RemoteDriver.class);
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
