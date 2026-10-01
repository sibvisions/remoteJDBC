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

import com.sibvisions.rjdbc.server.JdbcContext;

/**
 * Server-side delegate for {@code SQLXML} JDBC operations.
 * 
 * @author René Jahn
 */
public final class SQLXMLDelegate extends JdbcDelegate
{
    /**
     * Creates a new {@code SQLXMLDelegate} instance.
     *
     * @param pContext the context
     */
    public SQLXMLDelegate(JdbcContext pContext)
    {
        super(pContext);
    }

    /** {@inheritDoc} */
    @Override
    public Object call(long pId, String pSignature, Object[] pArgs) throws SQLException
    {
        switch (pSignature)
        {
            case "free()": 
            	context.sqlxml(pId).free(); 
            	
            	return null;
            case "getBinaryStream()": return readBytes(context.sqlxml(pId).getBinaryStream());
            case "setBinaryStream()": return stream("sqlxmlBinary", pId, null);
            case "getCharacterStream()": return readChars(context.sqlxml(pId).getCharacterStream());
            case "setCharacterStream()": return stream("sqlxmlCharacter", pId, null);
            case "getString()": return context.sqlxml(pId).getString();
            case "setString(String)": 
            	context.sqlxml(pId).setString((String)pArgs[0]); 
            	
            	return null;

            default: throw new SQLFeatureNotSupportedException("Unsupported JDBC method: " + pSignature);
        }
    }
}
