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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

/**
 * Verifies the JDBC behavior for security and concurrency.
 * 
 * @author René Jahn
 */
public class SecurityAndConcurrencyTest
{
	/**
	 * Verifies the JDBC behavior for connection state isolation.
	 * 
	 * @throws Exception if the operation fails
	 */
    @Test
    public void testConnectionStateIsolation() throws Exception
    {
        try (Connection connectionA = TestConnection.create();
             Connection connectionB = TestConnection.create())
        {
            boolean autoCommitA = connectionA.getAutoCommit();
            boolean autoCommitB = connectionB.getAutoCommit();
            boolean readOnlyA = connectionA.isReadOnly();
            boolean readOnlyB = connectionB.isReadOnly();
            
            int isolationA = connectionA.getTransactionIsolation();
            int isolationB = connectionB.getTransactionIsolation();

            try
            {
                connectionA.setAutoCommit(!autoCommitA);

                if (!readOnlyA)
                {
                    connectionA.setReadOnly(true);
                }
                
                assertEquals(!autoCommitA, connectionA.getAutoCommit());
                assertEquals(autoCommitB, connectionB.getAutoCommit());

                if (!readOnlyA)
                {
                    assertTrue(connectionA.isReadOnly());
                    assertEquals(readOnlyB, connectionB.isReadOnly());
                }
                
                assertEquals(isolationA, connectionA.getTransactionIsolation());
                assertEquals(isolationB, connectionB.getTransactionIsolation());
            }
            finally
            {
                try
                {
                    connectionA.rollback();
                }
                catch (SQLException ignored)
                {
                }
                
                connectionA.setReadOnly(readOnlyA);
                connectionA.setAutoCommit(autoCommitA);
            }
        }
    }

	/**
	 * Verifies the JDBC behavior for transaction state isolation.
	 * 
	 * @throws Exception if the operation fails
	 */
    @Test
    public void testTransactionStateIsolation() throws Exception
    {
        try (Connection connectionA = TestConnection.create();
             Connection connectionB = TestConnection.create())
        {
            boolean originalA = connectionA.getAutoCommit();
            boolean originalB = connectionB.getAutoCommit();

            try
            {
                connectionA.setAutoCommit(false);

                assertFalse(connectionA.getAutoCommit());
                assertEquals(originalB, connectionB.getAutoCommit());

                connectionA.rollback();

                assertFalse(connectionA.getAutoCommit());
                assertEquals(originalB, connectionB.getAutoCommit());
            }
            finally
            {
                try
                {
                    connectionA.rollback();
                }
                catch (SQLException ignored)
                {
                }
                connectionA.setAutoCommit(originalA);
                connectionB.setAutoCommit(originalB);
            }
        }
    }

	/**
	 * Verifies the JDBC behavior for result set isolation between sessions.
	 * 
	 * @throws Exception if the operation fails
	 */
    @Test
    public void testResultSetIsolationBetweenSessions() throws Exception
    {
        try (Connection connectionA = TestConnection.create();
             Connection connectionB = TestConnection.create())
        {
            DatabaseMetaData metadataA = connectionA.getMetaData();
            DatabaseMetaData metadataB = connectionB.getMetaData();

            try (ResultSet resultA = metadataA.getTables(null, null, "%", null);
            ResultSet resultB = metadataB.getTables(null, null, "%", null))
            {
                assertNotNull(resultA);
                assertNotNull(resultB);

                boolean hasA = resultA.next();
                boolean hasB = resultB.next();

                assertEquals(hasA, hasB);

                if (hasA)
                {
                    String tableA = resultA.getString("TABLE_NAME");
                    String tableB = resultB.getString("TABLE_NAME");

                    assertEquals(tableA, tableB);

                    // Advance only A. B must keep its own cursor position.
                    boolean nextA = resultA.next();

                    assertEquals(tableB, resultB.getString("TABLE_NAME"));

                    if (nextA)
                    {
                        assertFalse(resultA.isBeforeFirst());
                    }
                }
            }
        }
    }

	/**
	 * Verifies the JDBC behavior for metadata objects are session local.
	 * 
	 * @throws Exception if the operation fails
	 */
    @Test
    public void testMetadataObjectsAreSessionLocal() throws Exception
    {
        try (Connection connectionA = TestConnection.create();
             Connection connectionB = TestConnection.create())
        {
            DatabaseMetaData metadataA = connectionA.getMetaData();
            DatabaseMetaData metadataB = connectionB.getMetaData();

            assertNotNull(metadataA);
            assertNotNull(metadataB);

            assertNotNull(metadataA.getConnection());
            assertNotNull(metadataB.getConnection());
            assertFalse(metadataA.getConnection() == metadataB.getConnection());
        }
    }

	/**
	 * Verifies the JDBC behavior for closed session cannot be used.
	 * 
	 * @throws Exception if the operation fails
	 */
    @Test
    public void testClosedSessionCannotBeUsed() throws Exception
    {
        Connection connection = TestConnection.create();

        try
        {
            Statement statement = connection.createStatement();
            connection.close();

            assertTrue(connection.isClosed());

            try
            {
                statement.execute("/* JDBC lifecycle test */");
                
                fail("A statement belonging to a closed connection must not execute.");
            }
            catch (SQLException expected)
            {
                // Expected JDBC lifecycle boundary.
            }
            finally
            {
                try
                {
                    statement.close();
                }
                catch (SQLException ignored)
                {
                }
            }
        }
        finally
        {
            if (!connection.isClosed())
            {
                connection.close();
            }
        }
    }

	/**
	 * Verifies the JDBC behavior for concurrent independent connections.
	 * 
	 * @throws Exception if the operation fails
	 */
    @Test
    public void testConcurrentIndependentConnections() throws Exception
    {
        final int threadCount = 12;
        final ExecutorService executor = Executors.newFixedThreadPool(threadCount);

        try
        {
            List<Future<String>> futures = new ArrayList<Future<String>>();

            for (int i = 0; i < threadCount; i++)
            {
                final int worker = i;

                // Supports the test scenario by handling call.
                futures.add(executor.submit(new Callable<String>()
                {
                    @Override
                    public String call() throws Exception
                    {
                        try (Connection connection = TestConnection.create())
                        {
                            DatabaseMetaData metadata = connection.getMetaData();

                            assertNotNull(metadata);
                            assertNotNull(metadata.getConnection());

                            return metadata.getDriverName() + "#" + worker;
                        }
                    }
                }));
            }
            
            for (int i = 0; i < futures.size(); i++)
            {
                String value = futures.get(i).get(30, TimeUnit.SECONDS);

                assertNotNull(value);
                assertTrue(value.endsWith("#" + i));
            }
        }
        finally
        {
            executor.shutdownNow();
            
            assertTrue(executor.awaitTermination(30, TimeUnit.SECONDS));
        }
    }

	/**
	 * Verifies the JDBC behavior for concurrent metadata operations.
	 * 
	 * @throws Exception if the operation fails
	 */
    @Test
    public void testConcurrentMetadataOperations() throws Exception
    {
        final int threadCount = 8;
        
        final ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        final CountDownLatch start = new CountDownLatch(1);

        try
        {
            List<Future<Boolean>> futures = new ArrayList<Future<Boolean>>();

            for (int i = 0; i < threadCount; i++)
            {
            	//Supports the test scenario by handling call.
                futures.add(executor.submit(new Callable<Boolean>()
                {
                    @Override
                    public Boolean call() throws Exception
                    {
                        start.await(30, TimeUnit.SECONDS);

                        try (Connection connection = TestConnection.create())
                        {
                            DatabaseMetaData metadata = connection.getMetaData();

                            assertNotNull(metadata);
                            assertNotNull(metadata.getDatabaseProductName());
                            assertNotNull(metadata.getDriverName());

                            // One metadata ResultSet per independent session is
                            // sufficient to verify concurrent remote operation.
                            try (ResultSet resultSet = metadata.getTables(null, null, "%", null))
                            {
                                assertNotNull(resultSet);

                                if (resultSet.next())
                                {
                                    assertNotNull(resultSet.getString("TABLE_NAME"));
                                }
                            }
                        }
                        
                        return Boolean.TRUE;
                    }
                }));
            }
            
            start.countDown();

            for (Future<Boolean> future : futures)
            {
                assertTrue(future.get(60, TimeUnit.SECONDS));
            }
        }
        finally
        {
            executor.shutdownNow();
            
            assertTrue(executor.awaitTermination(30, TimeUnit.SECONDS));
        }
    }

	/**
	 * Verifies the JDBC behavior for concurrent connection state changes.
	 * 
	 * @throws Exception if the operation fails
	 */
    @Test
    public void testConcurrentConnectionStateChanges() throws Exception
    {
        final int threadCount = 10;
        
        final ExecutorService executor = Executors.newFixedThreadPool(threadCount);

        try
        {
            List<Future<Boolean>> futures = new ArrayList<Future<Boolean>>();

            for (int i = 0; i < threadCount; i++)
            {
                final int worker = i;

                //Supports the test scenario by handling call.
                futures.add(executor.submit(new Callable<Boolean>()
                {
                    @Override
                    public Boolean call() throws Exception
                    {
                        try (Connection connection = TestConnection.create())
                        {
                            boolean originalAutoCommit = connection.getAutoCommit();
                            boolean originalReadOnly = connection.isReadOnly();

                            try
                            {
                                connection.setAutoCommit(!originalAutoCommit);

                                if (!originalReadOnly)
                                {
                                    connection.setReadOnly(true);
                                }
                                
                                assertEquals(!originalAutoCommit,
                                
                                		connection.getAutoCommit());

                                if (!originalReadOnly)
                                {
                                    assertTrue(connection.isReadOnly());
                                }

                                // The worker value is only used to make every
                                // invocation independently observable in the
                                // debugger; it is never shared with another session.
                                assertTrue(worker >= 0);
                                
                                return true;
                            }
                            finally
                            {
                                try
                                {
                                    connection.rollback();
                                }
                                catch (SQLException ignored)
                                {
                                }
                                
                                connection.setReadOnly(originalReadOnly);
                                connection.setAutoCommit(originalAutoCommit);
                            }
                        }
                    }
                }));
            }
            
            for (Future<Boolean> future : futures)
            {
                assertTrue(future.get(30, TimeUnit.SECONDS));
            }
        }
        finally
        {
            executor.shutdownNow();
            assertTrue(executor.awaitTermination(30, TimeUnit.SECONDS));
        }
    }
}
