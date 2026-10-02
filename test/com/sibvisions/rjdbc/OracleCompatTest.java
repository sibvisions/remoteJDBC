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

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Verifies compatibility with oracle database.
 * 
 * @author René Jahn
 */
public class OracleCompatTest 
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
        connection = TestConnection.createOracle();
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
     * Test resultset of alter statement call.
     * 
     * @throws SQLException if the operation fails
     */
	@Test
	public void testMetaDataForAlterStatement() throws SQLException
	{
		try (PreparedStatement ps = connection.prepareStatement(
			    "ALTER SESSION SET " +
			    " \"_optimizer_push_pred_cost_based\"=FALSE " +
			    " \"_optimizer_squ_bottomup\"=FALSE " +
			    " \"_optimizer_cost_based_transformation\"='OFF' " +
			    " OPTIMIZER_FEATURES_ENABLE='10.2.0.5'"
			))
		{
			try (ResultSet rs = ps.executeQuery())
			{
				ResultSetMetaData md = rs.getMetaData();

				Assert.assertEquals(-1, md.getColumnCount());
				Assert.assertFalse(rs.isBeforeFirst());
				Assert.assertFalse(rs.next());
			}
		}
	}
}
