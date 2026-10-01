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

import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Provides the {@code jdbc servlet} functionality.
 * 
 * @author René Jahn
 */
public class JdbcServlet extends HttpServlet
{
    private JdbcHttpHandler handler;
    
    private JdbcSessionManager manager;
    

    /** {@inheritDoc} */
    @Override
    public void init(ServletConfig pConfig)
    {
        manager = new JdbcSessionManager(pConfig.getInitParameter("allowedJdbcUrls"), 
        								 pConfig.getInitParameter("jdbcUrl"), 
        								 pConfig.getInitParameter("idleTimeoutMinutes"), 
        								 pConfig.getInitParameter("clobPrefetchSize"));
        
        handler = new JdbcHttpHandler(manager);
    }

    /** {@inheritDoc} */
    @Override
    public void destroy()
    {
        if (manager != null)
        {
            manager.dispose();
        }
        
        super.destroy();
    }

    /** {@inheritDoc} */
    @Override
    protected void doPost(HttpServletRequest pRequest, HttpServletResponse pResponse) throws IOException
    {
        pResponse.setContentType("application/octet-stream");
        pResponse.setStatus(HttpServletResponse.SC_OK);
        
        try
        {
            handler.handle(pRequest.getInputStream(), pResponse.getOutputStream());
        }
        catch (JdbcHttpException e)
        {
            pResponse.reset();
            
            pResponse.setContentType("text/plain;charset=UTF-8");
            pResponse.sendError(e.getStatus(), e.getMessage());
        }
    }
}
