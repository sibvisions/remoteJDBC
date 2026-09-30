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
package com.sibvisions.rjdbc.server;

import java.io.IOException;

/**
 * Exception raised by the remote JDBC implementation.
 * 
 * @author René Jahn
 */
final class JdbcHttpException extends IOException
{
    private final int status;

    /**
     * Creates a new {@code JdbcHttpException} instance.
     *
     * @param pStatus the status
     * @param pMessage the message
     * @param pCause the cause
     */
    JdbcHttpException(int pStatus, String pMessage, Throwable pCause)
    {
        super(pMessage, pCause);
        status = pStatus;
    }

    /**
     * Returns status.
     * 
     * @return the resulting JDBC value
     */
    int getStatus()
    {
        return status;
    }
}
