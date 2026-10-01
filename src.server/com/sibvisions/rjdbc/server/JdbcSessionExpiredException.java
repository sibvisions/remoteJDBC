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
package com.sibvisions.rjdbc.server;

import java.sql.SQLException;

/**
 * Exception raised by the remote JDBC implementation.
 * 
 * @author René Jahn
 */
final class JdbcSessionExpiredException extends SQLException
{
    /**
     * Creates a new {@code JdbcSessionExpiredException} instance.
     *
     * @param pId the remote resource identifier
     */
    JdbcSessionExpiredException(long pId)
    {
        super("Remote JDBC session expired or is no longer available: " + pId);
    }
}
