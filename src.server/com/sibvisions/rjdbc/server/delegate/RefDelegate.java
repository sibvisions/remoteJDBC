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
import java.util.Map;

import com.sibvisions.rjdbc.server.JdbcContext;

/**
 * Server-side delegate for {@code Ref} JDBC operations.
 * 
 * @author René Jahn
 */
public final class RefDelegate extends JdbcDelegate
{
    /**
     * Creates a new {@code RefDelegate} instance.
     *
     * @param pContext the context
     */
    public RefDelegate(JdbcContext pContext)
    {
        super(pContext);
    }

    /** {@inheritDoc} */
    @SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
    public Object call(long pId, String pSignature, Object[] pArgs) throws SQLException
    {
        switch (pSignature)
        {
            case "getBaseTypeName()": return context.ref(pId).getBaseTypeName();
            case "getObject()": return remoteValue(context.ref(pId).getObject());
            case "getObject(Map)": return remoteValue(context.ref(pId).getObject((Map)pArgs[0]));
            case "setObject(Object)": 
            	context.ref(pId).setObject(pArgs[0]);
            	
            	return null;
            
            default: throw new SQLFeatureNotSupportedException("Unsupported JDBC method: " + pSignature);
        }
    }
}
