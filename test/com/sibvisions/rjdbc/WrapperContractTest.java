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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Wrapper;

import org.junit.Test;

/**
 * Verifies the JDBC behavior for connection wrapper contract.
 */
public class WrapperContractTest
{
	/**
	 * Verifies the JDBC behavior for connection wrapper contract.
	 * 
	 * @throws Exception if the operation fails
	 */
    @Test
    public void testConnectionWrapperContract() throws Exception
    {
        try (Connection connection = TestConnection.create())
        {
            verifyWrapperContract(connection, Connection.class);
        }
    }

	/**
	 * Verifies the JDBC behavior for statement wrapper contract.
	 * 
	 * @throws Exception if the operation fails
	 */
    @Test
    public void testStatementWrapperContract() throws Exception
    {
        try (Connection connection = TestConnection.create();
             Statement statement = connection.createStatement())
        {
            verifyWrapperContract(statement, Statement.class);
        }
    }

	/**
	 * Verifies the JDBC behavior for prepared statement wrapper contract.
	 * 
	 * @throws Exception if the operation fails
	 */
    @Test
    public void testPreparedStatementWrapperContract() throws Exception
    {
        try (Connection connection = TestConnection.create();
             PreparedStatement statement = connection.prepareStatement("SELECT 1 FROM DUAL"))
        {
            verifyWrapperContract(statement, PreparedStatement.class);
        }
    }

	/**
	 * Verifies the JDBC behavior for result set wrapper contract.
	 * 
	 * @throws Exception if the operation fails
	 */
    @Test
    public void testResultSetWrapperContract() throws Exception
    {
        try (Connection connection = TestConnection.create();
        	 Statement statement = connection.createStatement();
        	 ResultSet resultSet = statement.executeQuery("SELECT 1 FROM DUAL"))
        {
            verifyWrapperContract(resultSet, ResultSet.class);
        }
    }

	/**
	 * Verifies the JDBC behavior for database meta data wrapper contract.
	 * 
	 * @throws Exception if the operation fails
	 */
    @Test
    public void testDatabaseMetaDataWrapperContract() throws Exception
    {
        try (Connection connection = TestConnection.create())
        {
            DatabaseMetaData metaData = connection.getMetaData();

            verifyWrapperContract(metaData, DatabaseMetaData.class);
        }
    }

	/**
	 * Verifies the JDBC behavior for unsupported wrapper.
	 * 
	 * @throws Exception if the operation fails
	 */
    @Test
    public void testUnsupportedWrapper() throws Exception
    {
        try (Connection connection = TestConnection.create())
        {
            verifyUnsupportedWrapper(connection);
        }
        
        try (Connection connection = TestConnection.create();
             Statement statement = connection.createStatement())
        {
            verifyUnsupportedWrapper(statement);
        }
        
        try (Connection connection = TestConnection.create();
        	 PreparedStatement statement = connection.prepareStatement("SELECT 1 FROM DUAL"))
        {
            verifyUnsupportedWrapper(statement);
        }
        
        try (Connection connection = TestConnection.create();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT 1 FROM DUAL"))
        {
            verifyUnsupportedWrapper(resultSet);
        }
        
        try (Connection connection = TestConnection.create())
        {
            verifyUnsupportedWrapper(connection.getMetaData());
        }
    }

	/**
	 * Supports the test scenario by handling verify wrapper contract.
	 * 
	 * @param object       the object
	 * @param wrapperClass the wrapper class
	 * @throws Exception if the operation fails
	 */
    private void verifyWrapperContract(Object object, Class<?> wrapperClass) throws Exception
    {
        assertNotNull(object);

        boolean isWrapper = false;

        try
        {
            if (object instanceof Wrapper)
            {
                isWrapper = ((Wrapper)object).isWrapperFor(wrapperClass);
            }
        }
        catch (SQLException e)
        {
            fail("isWrapperFor() must not fail for the implemented JDBC interface: " + e.getMessage());
        }
        
        if (object instanceof Wrapper)
        {
            try
            {
                Object unwrapped = ((Wrapper)object).unwrap(wrapperClass);

                assertNotNull(unwrapped);
                assertTrue(wrapperClass.isInstance(unwrapped));

                if (!isWrapper)
                {
                    fail("unwrap() succeeded although isWrapperFor() returned false");
                }
            }
            catch (SQLException e)
            {
                if (isWrapper)
                {
                    fail("unwrap() failed although isWrapperFor() returned true: " + e.getMessage());
                }
            }
        }
    }

	/**
	 * Supports the test scenario by handling verify unsupported wrapper.
	 * 
	 * @param object the object
	 * @throws Exception if the operation fails
	 */
    private void verifyUnsupportedWrapper(Object object) throws Exception
    {
        assertTrue(object instanceof Wrapper);

        Class<?> unsupportedClass = UnsupportedWrapper.class;

        assertFalse(((Wrapper)object).isWrapperFor(unsupportedClass));

        try
        {
            ((Wrapper)object).unwrap(unsupportedClass);

            fail("unwrap() should fail for an unsupported wrapper type");
        }
        catch (SQLException expected)
        {
            // expected
        }
    }

    /**
     * Provides the {@code unsupported wrapper} functionality.
     */
    private static final class UnsupportedWrapper
    {
    }
}
