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
 * Server-side delegate for {@code ParameterMetaData} JDBC operations.
 * 
 * @author René Jahn
 */
public final class ParameterMetaDataDelegate extends JdbcDelegate
{
    /**
     * Creates a new {@code ParameterMetaDataDelegate} instance.
     *
     * @param pContext the context
     */
    public ParameterMetaDataDelegate(JdbcContext pContext)
    {
        super(pContext);
    }

    /** {@inheritDoc} */
    @Override
    public Object call(long pId, String pSignature, Object[] pArgs) throws SQLException
    {
        switch (pSignature)
        {
            case "getParameterCount()": return context.parameterMetaData(pId).getParameterCount();
            case "isNullable(int)": return context.parameterMetaData(pId).isNullable((int)pArgs[0]);
            case "isSigned(int)": return context.parameterMetaData(pId).isSigned((int)pArgs[0]);
            case "getPrecision(int)": return context.parameterMetaData(pId).getPrecision((int)pArgs[0]);
            case "getScale(int)": return context.parameterMetaData(pId).getScale((int)pArgs[0]);
            case "getParameterType(int)": return context.parameterMetaData(pId).getParameterType((int)pArgs[0]);
            case "getParameterTypeName(int)": return context.parameterMetaData(pId).getParameterTypeName((int)pArgs[0]);
            case "getParameterClassName(int)": return context.parameterMetaData(pId).getParameterClassName((int)pArgs[0]);
            case "getParameterMode(int)": return context.parameterMetaData(pId).getParameterMode((int)pArgs[0]);
            case "unwrap(Class)": throw new SQLFeatureNotSupportedException("unwrap");
            case "isWrapperFor(Class)": return false;
            
            default: throw new SQLFeatureNotSupportedException("Unsupported JDBC method: " + pSignature);
        }
    }
}
