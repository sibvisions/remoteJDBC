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

/**
 * Provides the token management functionality.
 * 
 * @author René Jahn
 */
public interface ITokenManager 
{
	/**
	 * Gets the authentication token for given pre-configured token and environment.
	 * 
	 * @param pToken the pre-configured token
	 * @param pEnvironment the environment
	 * @return the authentication token
	 */
	public byte[] getToken(byte[] pToken, String pEnvironment);	
}
