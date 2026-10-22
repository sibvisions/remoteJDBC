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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Properties;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Verifies compatibility with oracle database.
 * 
 * @author René Jahn
 */
public class DB2CompatTest 
{
	private Connection connection;
	
	/**
	 * Initializes the database objects and state required by the test class.
	 * 
	 * @throws Exception if the operation fails
	 */
    @Before
    public void setUp() throws Exception
    {
        // Establish a connection
        connection = TestConnection.create(createDB2Properties());
    }

	/**
	 * Releases the database objects and resets the state created by the test class.
	 * 
	 * @throws Exception if the operation fails
	 */
    @After
    public void tearDown() throws Exception
    {
        if (connection != null && !connection.isClosed())
        {
            connection.close();
        }
    }	
    
    /**
     * Creates remote JDBC properties for a connection that uses the client database configuration.
     *
     * @return the remote JDBC properties
     */
    private static Properties createDB2Properties()
    {
        Properties properties = new Properties();
        properties.put(RemoteConstants.USER, TestConnection.propDb.getProperty("db2.db.user"));
        properties.put(RemoteConstants.PASSWORD, TestConnection.propDb.getProperty("db2.db.pwd"));
        properties.put(RemoteConstants.JDBC_URL, TestConnection.propDb.getProperty("db2.db.url"));

        return properties;
    }    
	
    /**
     * Test resultset of alter statement call.
     * 
     * @throws SQLException if the operation fails
     */
	@Test
	public void testMetaDataForAlterStatement() throws Exception
	{
		try (Connection conDb2 = TestConnection.createDB2();
			 PreparedStatement ps = conDb2.prepareStatement("SELECT * FROM SYSCAT.DATATYPES WHERE METATYPE = 'S' ORDER BY TYPESCHEMA,TYPENAME WITH UR"))
		{
			try (ResultSet rs = ps.executeQuery())
			{
				ResultSetMetaData md = rs.getMetaData();

				Assert.assertNotNull(md);
				
				//fetch all data
				while (rs.next());

				//With DB2, it's not possible to get the metadata after resultset if fully fetched
				
				try
				{
					Assert.assertNotNull(rs.getMetaData());
					
					Assert.fail("ResultSet should be closed but isn't!");
				}
				catch (SQLException sqle)
				{
					Assert.assertTrue(sqle.getMessage().contains("result set is closed"));
				}
			}
		}		
		
		try (PreparedStatement ps = connection.prepareStatement("SELECT * FROM SYSCAT.DATATYPES WHERE METATYPE = 'S' ORDER BY TYPESCHEMA,TYPENAME WITH UR"))
		{
			try (ResultSet rs = ps.executeQuery())
			{
				ResultSetMetaData md = rs.getMetaData();

				Assert.assertNotNull(md);
				
				//fetch all data
				while (rs.next());

				//With remoteJDBC it's possible to get the metadata also after full fetch
				
				Assert.assertNotNull(rs.getMetaData());
			}
		}
	}
}
