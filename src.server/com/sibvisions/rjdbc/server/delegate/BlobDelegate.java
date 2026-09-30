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
 * Server-side delegate for {@code Blob} JDBC operations.
 * 
 * @author René Jahn
 */
public final class BlobDelegate extends JdbcDelegate
{
    /**
     * Creates a new {@code BlobDelegate} instance.
     *
     * @param pContext the context
     */
    public BlobDelegate(JdbcContext pContext)
    {
        super(pContext);
    }

    /** {@inheritDoc} */
    @Override
    public Object call(long pId, String pSignature, Object[] pArgs) throws SQLException
    {
        switch (pSignature)
        {
            case "length()": return remoteValue(context.blob(pId).length());
            case "getBytes(long,int)": return remoteValue(context.blob(pId).getBytes((long)pArgs[0], (int)pArgs[1]));
            case "getBinaryStream()": return readBytes(context.blob(pId).getBinaryStream());
            case "position(byte[],long)": return remoteValue(context.blob(pId).position((byte[]) pArgs[0], (long)pArgs[1]));
            case "position(Blob,long)": return remoteValue(context.blob(pId).position(context.blob(number(pArgs[0])), (long)pArgs[1]));
            case "setBytes(long,byte[])": return remoteValue(context.blob(pId).setBytes((long)pArgs[0], (byte[]) pArgs[1]));
            case "setBytes(long,byte[],int,int)": return remoteValue(context.blob(pId).setBytes((long)pArgs[0], (byte[]) pArgs[1], (int)pArgs[2], (int)pArgs[3]));
            case "setBinaryStream(long)": return stream("blob", pId, pArgs[0]);
            case "truncate(long)": context.blob(pId).truncate((long)pArgs[0]); return null;
            case "free()": context.blob(pId).free(); return null;
            case "getBinaryStream(long,long)": return readBytes(context.blob(pId).getBinaryStream((long)pArgs[0], (long)pArgs[1]));
            
            default: throw new SQLFeatureNotSupportedException("Unsupported JDBC method: " + pSignature);
        }

    }
}
