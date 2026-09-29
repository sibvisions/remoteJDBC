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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Initializes the database objects and state required by the test class.
 */
public class BatchTest
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
        connection = TestConnection.create();
        connection.setAutoCommit(false);

        // Prepare the table according to ANSI SQL / Oracle standards (drop the old table if it exists)
        dropTableIfExists();

        // ANSI SQL standard CREATE TABLE (Oracle prefers VARCHAR2, but VARCHAR also works)
        try (Statement stmt = connection.createStatement())
        {
            stmt.executeUpdate("CREATE TABLE test_data (id NUMBER PRIMARY KEY, name VARCHAR2(100))");
            
            connection.commit();
        }
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
            try
            {
                // Clean up the table
                dropTableIfExists();
            }
            finally
            {
                // Close the connection
                connection.close();
            }
        }
    }

	/**
	 * Verifies the JDBC behavior for batch insert1000 rows.
	 * 
	 * @throws SQLException if the operation fails
	 */
    @Test
    public void testBatchInsert1000Rows() throws SQLException
    {
        // Standard ANSI-SQL INSERT
        String insertSql = "INSERT INTO test_data (id, name) VALUES (?, ?)";

        try (PreparedStatement pstmt = connection.prepareStatement(insertSql))
        {
            // Add 1,000 rows to the batch
            for (int i = 1; i <= 1000; i++)
            {
                pstmt.setInt(1, i);
                pstmt.setString(2, "Name_" + i);
                pstmt.addBatch();
            }

            // Execute the batch
            int[] updateCounts = pstmt.executeBatch();
            connection.commit();

            // Verify that 1,000 entries were processed
            assertNotNull("Update-Counts shoul not be NULL", updateCounts);
            assertEquals("Exactly 1000 records should be processed.", 1000, updateCounts.length);
        }
        catch (SQLException e)
        {
            connection.rollback();
            
            throw e;
        }
    }

	/**
	 * Removes the test table when it exists so the next test can start from a clean
	 * state.
	 */
    private void dropTableIfExists()
    {
        try (Statement stmt = connection.createStatement())
        {
            stmt.executeUpdate("DROP TABLE test_data");

            connection.commit();
        }
        catch (SQLException ignored)
        {
            try
            {
                connection.rollback();
            }
            catch (SQLException e)
            {
                //ignore
            }
        }
    }
}
