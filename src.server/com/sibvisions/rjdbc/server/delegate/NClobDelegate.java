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
package com.sibvisions.rjdbc.server.delegate;

import java.sql.Clob;
import java.sql.SQLException;

import com.sibvisions.rjdbc.server.JdbcContext;

/**
 * Server-side delegate for {@code NClob} JDBC operations.
 * 
 * @author René Jahn
 */
public final class NClobDelegate extends ClobDelegate
{
    /**
     * Creates a new {@code NClobDelegate} instance.
     *
     * @param pContext the context
     */
    public NClobDelegate(JdbcContext pContext)
    {
        super(pContext);
    }

    /** {@inheritDoc} */
    @Override
    protected Clob clob(long pId) throws SQLException
    {
        return context.nclob(pId);
    }

    /** {@inheritDoc} */
    @Override
    protected String streamPrefix()
    {
        return "nclob";
    }
}
