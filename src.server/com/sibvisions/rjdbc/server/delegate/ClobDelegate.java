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

import java.sql.Clob;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;

import com.sibvisions.rjdbc.server.JdbcContext;

/**
 * Handles the clob operation for the remote JDBC resource.
 * 
 * @author René Jahn
 */
public class ClobDelegate extends JdbcDelegate
{
    /**
     * Creates a new {@code ClobDelegate} instance.
     *
     * @param pContext the context
     */
    public ClobDelegate(JdbcContext pContext)
    {
        super(pContext);
    }

    /** {@inheritDoc} */
    @Override
    public Object call(long pId, String pSignature, Object[] pArgs) throws SQLException
    {
        switch (pSignature)
        {
            case "length()": return remoteValue(clob(pId).length());
            case "getSubString(long,int)": return remoteValue(clob(pId).getSubString((long)pArgs[0], (int)pArgs[1]));
            case "getCharacterStream()": return readChars(clob(pId).getCharacterStream());
            case "getAsciiStream()": return readBytes(clob(pId).getAsciiStream());
            case "position(String,long)": return remoteValue(clob(pId).position((String)pArgs[0], (long)pArgs[1]));
            case "position(Clob,long)": return remoteValue(clob(pId).position(clob(number(pArgs[0])), (long)pArgs[1]));
            case "setString(long,String)": return remoteValue(clob(pId).setString((long)pArgs[0], (String)pArgs[1]));
            case "setString(long,String,int,int)": return remoteValue(clob(pId).setString((long)pArgs[0], (String)pArgs[1], (int)pArgs[2], (int)pArgs[3]));
            case "setAsciiStream(long)": return stream(streamPrefix() + "Ascii", pId, pArgs[0]);
            case "setCharacterStream(long)": return stream(streamPrefix() + "Character", pId, pArgs[0]);
            case "truncate(long)": clob(pId).truncate((long)pArgs[0]); return null;
            case "free()": clob(pId).free(); return null;
            case "getCharacterStream(long,long)": return readChars(clob(pId).getCharacterStream((long)pArgs[0], (long)pArgs[1]));
            
            default: throw new SQLFeatureNotSupportedException("Unsupported JDBC method: " + pSignature);
        }

    }

    /**
     * Handles the clob operation for the remote JDBC resource.
     *
     * @param pId the remote resource identifier
     * @return the resulting JDBC value
     * @throws SQLException if the JDBC operation cannot be completed
     */
    protected Clob clob(long pId) throws SQLException
    {
        return context.clob(pId);
    }

	/**
	 * Handles the stream prefix operation for the remote JDBC resource.
	 * 
	 * @return the resulting JDBC value
	 */
    protected String streamPrefix()
    {
        return "clob";
    }
}
