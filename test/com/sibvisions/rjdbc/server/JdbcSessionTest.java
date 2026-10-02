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
package com.sibvisions.rjdbc.server;

import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;

/**
 * Tests authenticated request sequence handling of a JDBC session.
 *
 * @author René Jahn
 */
public class JdbcSessionTest
{
    /**
     * Verifies that requests must use consecutive sequence numbers.
     */
    @Test
    public void testRequestSequence() 
    {
        JdbcContext context = new JdbcContext("jdbc:test:*", "jdbc:test:server", null, null, 0, 0, JdbcSecurity.ENVIRONMENT_TEST);
        JdbcSession session = new JdbcSession(1, context);
        
        byte[] key = new byte[32];
        Arrays.fill(key, (byte)1);
        session.setConnectionKey(key);

        Assert.assertTrue(session.acceptRequestSequence(1));
        Assert.assertFalse(session.acceptRequestSequence(1));
        Assert.assertFalse(session.acceptRequestSequence(3));
        Assert.assertTrue(session.acceptRequestSequence(2));
        Assert.assertFalse(session.acceptRequestSequence(4));

        JdbcSessionWrapper.clearConnectionKey(session);
    }

    /**
     * Verifies that clearing a session removes its connection key.
     */
    @Test
    public void testConnectionKeyCleanup()
    {
        JdbcContext context = new JdbcContext("jdbc:test:*", "jdbc:test:server", null, null, 0, 0, JdbcSecurity.ENVIRONMENT_TEST);
        JdbcSession session = new JdbcSession(1, context);
        session.setConnectionKey(new byte[32]);

        Assert.assertNotNull(session.getConnectionKey());

        JdbcSessionWrapper.clearConnectionKey(session);

        Assert.assertNull(session.getConnectionKey());
    }
}
