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
import java.sql.SQLException;
import java.sql.SQLXML;
import java.sql.Statement;
import java.sql.Types;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Verifies the JDBC behavior for SQLXML for Oracle database.
 * 
 * @author René Jahn
 */
public class OracleSqlXmlTest
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
        connection = TestConnection.create();

        prepareTestData();
    }    
    
	/**
	 * Prepares test data required by the tests.
	 * 
	 * @throws Exception if the operation fails
	 */
    private void prepareTestData() throws Exception
    {
        SQLXML xmlObj = connection.createSQLXML();
        
        xmlObj.setString("<katalog><buch id=\"1\"><titel>Clean Code</titel></buch></katalog>");

        try (PreparedStatement pstmt = connection.prepareStatement("INSERT INTO test_xml_data (id, description, xml_content) VALUES (?, ?, ?)"))
        {
            pstmt.setInt(1, 42);
            pstmt.setString(2, "Special");
            pstmt.setSQLXML(3, xmlObj);
            pstmt.executeUpdate();
        }
        
        xmlObj.free();
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
            try (Statement stmt = connection.createStatement())
            {
                stmt.execute("TRUNCATE TABLE test_xml_data");
            }
            finally
            {
            	connection.close();
            }
        }
    }

	/**
	 * Verifies the JDBC behavior for read.
	 * 
	 * @throws Exception if the operation fails
	 */
    @Test
    public void testRead() throws Exception
    {
        try (PreparedStatement pstmt = connection.prepareStatement("SELECT xml_content FROM test_xml_data WHERE id = ?"))
        {
            pstmt.setInt(1, 42);
            
            try (ResultSet rs = pstmt.executeQuery())
            {
                Assert.assertTrue("Record should be available", rs.next());

                SQLXML resultXml = rs.getSQLXML("xml_content");
                String resultString = resultXml.getString();

                Assert.assertNotNull("SQLXML string must not be null", resultString);
                Assert.assertTrue("The XML should contain the book title", resultString.contains("<titel>Clean Code</titel>"));

                resultXml.free();
            }
        }
    }

	/**
	 * Verifies the JDBC behavior for xml query with x path.
	 * 
	 * @throws Exception if the operation fails
	 */
    @Test
    public void testXmlQueryWithXPath() throws Exception
    {
        try (PreparedStatement pstmt = connection.prepareStatement("SELECT XMLCAST(XMLQUERY('/katalog/buch/titel/text()' " +
        														   "PASSING xml_content RETURNING CONTENT) AS VARCHAR2(100)) AS titel " +
        														   "FROM test_xml_data WHERE id = ?"))
        {
            pstmt.setInt(1, 42);

            try (ResultSet rs = pstmt.executeQuery())
            {
                Assert.assertTrue("Row with ID 42 should exist", rs.next());

                String titel = rs.getString("titel");

                Assert.assertNotNull("Title must not be null", titel);
                Assert.assertEquals("Clean Code", titel);
            }
        }
    }

	/**
	 * Verifies the JDBC behavior for statement execute and generated keys.
	 * 
	 * @throws Exception if test fails
	 */
    @Test
    public void testStatementExecuteAndGeneratedKeys() throws Exception
    {
        try (Statement stmt = connection.createStatement())
        {
	        boolean hasResult = stmt.execute("INSERT INTO TEST_XML_DATA (ID, DESCRIPTION, XML_CONTENT) " +
	        								 "VALUES (999, 'generated', XMLTYPE('<root/>'))", 
	        								 Statement.RETURN_GENERATED_KEYS);
	
	        Assert.assertFalse(hasResult);
	
	        int updateCount = stmt.getUpdateCount();
	        
	        Assert.assertEquals(1, updateCount);
	
	        try (ResultSet keys = stmt.getGeneratedKeys())
	        {
	            Assert.assertNotNull(keys);
	
	            while (keys.next())
	            {
	                System.out.println("generated key = " + keys.getObject(1));
	            }
	        }
        }
    }

	/**
	 * Verifies the JDBC behavior for execute query result set.
	 * 
	 * @throws Exception if test fails
	 */
    @Test
    public void testExecuteQueryResultSet() throws Exception
    {
        try (Statement stmt = connection.createStatement())
        {
            boolean hasResult = stmt.execute("SELECT ID, DESCRIPTION, XML_CONTENT FROM TEST_XML_DATA");

            Assert.assertTrue(hasResult);

            try (ResultSet rs = stmt.getResultSet())
            {
                Assert.assertNotNull(rs);

                while (rs.next())
                {
                    rs.getLong("ID");
                    rs.getString("DESCRIPTION");
                    rs.getSQLXML("XML_CONTENT");
                }
            }
        }
    }

	/**
	 * Verifies the JDBC behavior for more results.
	 * 
	 * @throws Exception if test fails
	 */
    @Test
    public void testMoreResults() throws Exception
    {
        try (Statement stmt = connection.createStatement())
        {
            boolean first = stmt.execute("SELECT 1 FROM DUAL");

            Assert.assertTrue(first);

            try (ResultSet rs = stmt.getResultSet())
            {
                Assert.assertTrue(rs.next());
            }

            boolean more = stmt.getMoreResults();

            Assert.assertFalse(more);
        }
    }

	/**
	 * Verifies the JDBC behavior for prepared statement parameters.
	 * 
	 * @throws Exception if the operation fails
	 */
    @Test
    public void testPreparedStatementParameters() throws Exception
    {
        //we need the record
        testStatementExecuteAndGeneratedKeys();

        String sql = "SELECT ID, DESCRIPTION " +
        			 "FROM TEST_XML_DATA " +
        			 "WHERE ID = ? AND DESCRIPTION = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql))
        {
            ps.setInt(1, 999);
            ps.setString(2, "generated");

            try (ResultSet rs = ps.executeQuery())
            {
                Assert.assertTrue(rs.next());
                Assert.assertEquals(999, rs.getInt("ID"));
                Assert.assertEquals("generated", rs.getString("DESCRIPTION"));
                Assert.assertFalse(rs.next());
            }
        }
    }

	/**
	 * Verifies the JDBC behavior for prepared statement null.
	 * 
	 * @throws Exception if the operation fails
	 */
    @Test
    public void testPreparedStatementNull() throws Exception
    {
        try
        {
            String sql = "SELECT COUNT(*) FROM TEST_XML_DATA WHERE DESCRIPTION IS NULL";

            try (PreparedStatement ps = connection.prepareStatement(sql))
            {
                ps.setNull(1, Types.VARCHAR);

                try (ResultSet rs = ps.executeQuery())
                {
                    Assert.assertTrue(rs.next());
                    Assert.assertEquals(0, rs.getInt(1));
                }
            }
        }
        catch (SQLException sqle)
        {
            Assert.assertTrue(sqle.getMessage().toLowerCase().contains("invalid column")
            				  || sqle.getMessage().toLowerCase().contains("ungültiger spaltenindex"));
        }

        String sql = "SELECT COUNT(*) FROM TEST_XML_DATA WHERE DESCRIPTION = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql))
        {
            ps.setNull(1, Types.VARCHAR);

            try (ResultSet rs = ps.executeQuery())
            {
                Assert.assertTrue(rs.next());
                Assert.assertEquals(0, rs.getInt(1));
            }
        }
    }
}
