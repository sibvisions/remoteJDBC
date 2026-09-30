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
package com.sibvisions.rjdbc;

import java.sql.NClob;

/**
 * Remote JDBC implementation of {@code NClob} functionality.
 * 
 * @author René Jahn
 */
public class RemoteNClob extends RemoteClob 
                         implements NClob
{
    /**
     * Creates a new {@code RemoteNClob} instance.
     *
     * @param pClient the client
     * @param pId the remote resource identifier
     */
    public RemoteNClob(RemoteClient pClient, long pId)
    {
        super(pClient, pId);
    }

    /**
     * Creates a new {@code RemoteNClob} instance.
     *
     * @param pClient the client
     * @param pId the remote resource identifier
     * @param pValue the value to convert
     * @param pValueLoaded the value loaded
     */
    public RemoteNClob(RemoteClient pClient, long pId, String pValue, boolean pValueLoaded)
    {
        super(pClient, pId, pValue, pValueLoaded);
    }

    /** {@inheritDoc} */
    @Override
    protected Class<?> remoteInterface()
    {
        return NClob.class;
    }

    /** {@inheritDoc} */
    @Override
    protected String streamPrefix()
    {
        return "nclob";
    }
}
