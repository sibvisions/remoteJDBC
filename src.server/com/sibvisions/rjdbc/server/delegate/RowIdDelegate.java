/*
 * Copyright (C)2026 SIB Visions GmbH
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

import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;

import com.sibvisions.rjdbc.server.JdbcContext;

/**
 * Server-side delegate for {@code RowId} JDBC operations.
 * 
 * @author René Jahn
 */
public final class RowIdDelegate extends JdbcDelegate
{
    /**
     * Creates a new {@code RowIdDelegate} instance.
     *
     * @param pContext the context
     */
    public RowIdDelegate(JdbcContext pContext)
    {
        super(pContext);
    }

    /** {@inheritDoc} */
    @Override
    public Object call(long pId, String pSignature, Object[] pArgs) throws SQLException
    {
        switch (pSignature)
        {
            case "getBytes()": return context.rowId(pId).getBytes();
            case "toString()": return context.rowId(pId).toString();
            case "hashCode()": return context.rowId(pId).hashCode();
            case "equals(Object)": return context.rowId(pId).equals(pArgs[0]);
            
            default: throw new SQLFeatureNotSupportedException("Unsupported JDBC method: " + pSignature);
        }
    }
}
