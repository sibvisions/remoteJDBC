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

import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Map;

import com.sibvisions.rjdbc.server.JdbcContext;

/**
 * Server-side delegate for {@code Array} JDBC operations.
 * 
 * @author René Jahn
 */
public final class ArrayDelegate extends JdbcDelegate
{
    /**
     * Creates a new {@code ArrayDelegate} instance.
     *
     * @param pContext the context
     */
    public ArrayDelegate(JdbcContext pContext)
    {
        super(pContext);
    }

    /** {@inheritDoc} */
    @SuppressWarnings({"rawtypes", "unchecked"})
	@Override
    public Object call(long pId, String pSignature, Object[] pArgs) throws SQLException
    {
        switch (pSignature)
        {
            case "getBaseTypeName()": return context.array(pId).getBaseTypeName();
            case "getBaseType()": return context.array(pId).getBaseType();
            case "getArray()": return remoteValue(context.array(pId).getArray());
            case "getArray(Map)": return remoteValue(context.array(pId).getArray((Map)pArgs[0]));
            case "getArray(long,int)": return remoteValue(context.array(pId).getArray((long)pArgs[0], (int)pArgs[1]));
            case "getArray(long,int,Map)": return remoteValue(context.array(pId).getArray((long)pArgs[0], (int)pArgs[1], (Map)pArgs[2]));
            case "getResultSet()": return remoteValue(context.array(pId).getResultSet());
            case "getResultSet(Map)": return remoteValue(context.array(pId).getResultSet((Map)pArgs[0]));
            case "getResultSet(long,int)": return remoteValue(context.array(pId).getResultSet((long)pArgs[0], (int)pArgs[1]));
            case "getResultSet(long,int,Map)": return remoteValue(context.array(pId).getResultSet((long)pArgs[0], (int)pArgs[1], (Map)pArgs[2]));
            case "free()": context.array(pId).free(); return null;
            
            default: throw new SQLFeatureNotSupportedException("Unsupported JDBC method: " + pSignature);
        }
    }
}
