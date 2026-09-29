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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.io.StringReader;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.NClob;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.SQLXML;

import javax.xml.parsers.DocumentBuilderFactory;

import org.junit.Test;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

/**
 * Verifies the JDBC behavior for blob.
 */
public class LobAndSpecialTypesTest
{
	/**
	 * Verifies the JDBC behavior for blob.
	 * 
	 * @throws Exception if the operation fails
	 */
    @Test
    public void testBlob() throws Exception
    {
        try (Connection connection = TestConnection.create())
        {
            try
            {
                Blob blob = connection.createBlob();

                assertNotNull(blob);
                assertEquals(0, blob.length());

                byte[] data = new byte[] {1, 2, 3, 4, 5};

                blob.setBytes(1L, data);

                assertEquals(5L, blob.length());
                assertArrayEquals(data, blob.getBytes(1L, 5));

                assertArrayEquals(
                new byte[] { 2, 3 },
                blob.getBytes(2L, 2));

                blob.truncate(3L);

                assertEquals(3L, blob.length());
                assertArrayEquals(
                new byte[] { 1, 2, 3 },
                blob.getBytes(1L, 3));

                blob.free();

                try
                {
                    blob.length();
                    
                    fail("Blob should be unusable after free()");
                }
                catch (SQLException expected)
                {
                    // expected
                }
            }
            catch (SQLFeatureNotSupportedException e)
            {
                // Optional JDBC feature.
            }
        }
    }

	/**
	 * Verifies the JDBC behavior for clob.
	 * 
	 * @throws Exception if the operation fails
	 */
    @Test
    public void testClob() throws Exception
    {
        try (Connection connection = TestConnection.create())
        {
            try
            {
                Clob clob = connection.createClob();

                assertNotNull(clob);
                assertEquals(0L, clob.length());

                String text = "Hello RJdbc";

                clob.setString(1L, text);

                assertEquals(text.length(), clob.length());
                assertEquals(text, clob.getSubString(1L, text.length()));

                clob.setString(7L, "JDBC");

                assertEquals("Hello JDBCc", clob.getSubString(1L, (int) clob.length()));

                clob.truncate(5L);

                assertEquals(5L, clob.length());
                assertEquals("Hello", clob.getSubString(1L, 5));

                clob.free();

                try
                {
                    clob.length();
                    
                    fail("Clob should be unusable after free()");
                }
                catch (SQLException expected)
                {
                    // expected
                }
            }
            catch (SQLFeatureNotSupportedException e)
            {
                // Optional JDBC feature.
            }
        }
    }

	/**
	 * Verifies the JDBC behavior for n clob.
	 * 
	 * @throws Exception if the operation fails
	 */
    @Test
    public void testNClob() throws Exception
    {
        try (Connection connection = TestConnection.create())
        {
            try
            {
                NClob nclob = connection.createNClob();

                assertNotNull(nclob);

                String text = "ÄÖÜ 日本語";

                nclob.setString(1L, text);

                assertEquals(text.length(), nclob.length());
                assertEquals(text, nclob.getSubString(1L, text.length()));

                nclob.free();

                try
                {
                    nclob.length();
                    
                    fail("NClob should be unusable after free()");
                }
                catch (SQLException expected)
                {
                    // expected
                }
            }
            catch (SQLFeatureNotSupportedException e)
            {
                // Optional JDBC feature.
            }
        }
    }

	/**
	 * Verifies the JDBC behavior for sqlxml.
	 * 
	 * @throws Exception if the operation fails
	 */
    @Test
    public void testSQLXML() throws Exception
    {
        try (Connection connection = TestConnection.create())
        {
            try
            {
                SQLXML sqlxml = connection.createSQLXML();

                assertNotNull(sqlxml);

                String xml = "<root><value>rjdbc</value></root>";

                sqlxml.setString(xml);

                String result = sqlxml.getString();

                assertNotNull(result);

                Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder()
                						.parse(new InputSource(new StringReader(result)));

                assertEquals("root", document.getDocumentElement().getNodeName());
                assertEquals("rjdbc", document.getElementsByTagName("value").item(0).getTextContent());

                sqlxml.free();

                try
                {
                    sqlxml.getString();
                    
                    fail("SQLXML getString() must fail after free()");
                }
                catch (SQLException expected)
                {
                    // expected
                }
            }
            catch (SQLFeatureNotSupportedException e)
            {
                // Optional JDBC feature.
            }
        }
    }

	/**
	 * Verifies the JDBC behavior for array.
	 * 
	 * @throws Exception if the operation fails
	 */
    @Test
    public void testArray() throws Exception
    {
        try (Connection connection = TestConnection.create())
        {
            try
            {
                Array array = connection.createArrayOf("VARCHAR", new Object[] {"A", "B", "C"});

                assertNotNull(array);

                assertArrayEquals(new Object[] { "A", "B", "C" }, (Object[]) array.getArray());

                array.free();
            }
            catch (SQLFeatureNotSupportedException e)
            {
                // Optional JDBC feature.
            }
            catch (SQLException e)
            {
                // ARRAY support is DB-dependent.
                assertNotNull(e);
            }
        }
    }
}
