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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.SQLXML;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import com.sibvisions.util.xml.XmlNode;

/**
 * Remote JDBC implementation of {@code SQLXML} functionality.
 */
public class RemoteSQLXML implements SQLXML
{
    protected final RemoteClient client;
    
    protected final long id;

    private Result pendingResult;
    
    private XmlNode cachedXmlNode;
    
    private boolean xmlLoaded;
    private boolean freed;

    /**
     * Creates a new {@code RemoteSQLXML} instance.
     *
     * @param pClient the client
     * @param pId the remote resource identifier
     */
    public RemoteSQLXML(RemoteClient pClient, long pId)
    {
        this(pClient, pId, null, false);
    }

    /**
     * Creates a new {@code RemoteSQLXML} instance.
     *
     * @param pClient the client
     * @param pId the remote resource identifier
     * @param pXmlNode the xml node
     * @param pLoaded the loaded
     */
    public RemoteSQLXML(RemoteClient pClient, long pId, XmlNode pXmlNode, boolean pLoaded)
    {
        client = pClient;
        id = pId;
        
        cachedXmlNode = pXmlNode;
        xmlLoaded = pLoaded;
    }

    /** {@inheritDoc} */
    @Override
    public String toString()
    {
        return "RemoteSQLXML[" + id + "]";
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode()
    {
        return Long.hashCode(id);
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(Object pObject)
    {
        return this == pObject;
    }

    /** {@inheritDoc} */
    @Override
    public void free() throws SQLException
    {
        if (freed)
        {
            return;
        }
        
        RemoteUtil.invoke(client, id, SQLXML.class, "free", new Class<?>[]{}, new Object[]{}, void.class);
        
        freed = true;
        pendingResult = null;
        cachedXmlNode = null;
        xmlLoaded = false;
    }

    /** {@inheritDoc} */
    @Override
    public InputStream getBinaryStream() throws SQLException
    {
        return new ByteArrayInputStream(loadString().getBytes(java.nio.charset.StandardCharsets.UTF_8));
    }

    /** {@inheritDoc} */
    @Override
    public OutputStream setBinaryStream() throws SQLException
    {
        checkFreed();

        return (OutputStream)RemoteUtil.invoke(client, id, SQLXML.class, "setBinaryStream", new Class<?>[]{}, new Object[]{}, OutputStream.class);
    }

    /** {@inheritDoc} */
    @Override
    public Reader getCharacterStream() throws SQLException
    {
        checkFreed();

        return new StringReader(loadString());
    }

    /** {@inheritDoc} */
    @Override
    public Writer setCharacterStream() throws SQLException
    {
        checkFreed();

        return (Writer)RemoteUtil.invoke(client, id, SQLXML.class, "setCharacterStream", new Class<?>[]{}, new Object[]{}, Writer.class);
    }

    /** {@inheritDoc} */
    @Override
    public String getString() throws SQLException
    {
        checkFreed();

        return loadString();
    }

    /** {@inheritDoc} */
    @Override
    public void setString(String pValue) throws SQLException
    {
        checkFreed();
        
        RemoteUtil.invoke(client, id, SQLXML.class, "setString", new Class<?>[]{String.class}, new Object[]{pValue}, void.class);
        
        try
        {
            cachedXmlNode = com.sibvisions.util.xml.XmlWorker.readNode(new ByteArrayInputStream(pValue.getBytes("UTF-8")));
            
            xmlLoaded = true;
        }
        catch (Exception e)
        {
            cachedXmlNode = null;
            xmlLoaded = false;
        }
    }

    /** {@inheritDoc} */
    @Override
    public <T extends Source> T getSource(Class<T> pSourceClass) throws SQLException
    {
        checkFreed();

        if (pSourceClass == null)
        {
            throw new SQLException("Source class must not be null");
        }
        
        String xml = getString();
        
        try
        {
        
            if (DOMSource.class.isAssignableFrom(pSourceClass))
            {
                return pSourceClass.cast(new DOMSource(DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new InputSource(new StringReader(xml)))));
            }

            if (SAXSource.class.isAssignableFrom(pSourceClass))
            {
            	SAXParserFactory spf = SAXParserFactory.newInstance();
            	spf.setNamespaceAware(false);
    
            	XMLReader reader = spf.newSAXParser().getXMLReader();

                return pSourceClass.cast(new SAXSource(reader, new InputSource(new StringReader(xml))));
            }

            if (StreamSource.class.isAssignableFrom(pSourceClass))
            {
                return pSourceClass.cast(new StreamSource(new StringReader(xml)));
            }
            
            throw new SQLFeatureNotSupportedException("Unsupported SQLXML Source class: " + pSourceClass.getName());
        }
        catch (SQLException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            throw new SQLException("Could not create SQLXML Source: " + pSourceClass.getName(), e);
        }
    }

    /** {@inheritDoc} */
    @Override
    public <T extends Result> T setResult(Class<T> pResultClass) throws SQLException
    {
        checkFreed();

        if (pResultClass == null)
        {
            throw new SQLException("Result class must not be null");
        }
        
        try
        {
            Result result;
        
            if (DOMResult.class.isAssignableFrom(pResultClass))
            {
                result = new DOMResult(DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument());
            }
            else if (StreamResult.class.isAssignableFrom(pResultClass))
            {
                result = new StreamResult(new StringWriter());
            }
            else if (SAXResult.class.isAssignableFrom(pResultClass))
            {
                throw new SQLFeatureNotSupportedException("SAXResult is not supported for SQLXML setResult");
            }
            else
            {
                throw new SQLFeatureNotSupportedException("Unsupported SQLXML Result class: " + pResultClass.getName());
            }
            
            pendingResult = result;
            

            return pResultClass.cast(result);
        }
        catch (SQLException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            throw new SQLException("Could not create SQLXML Result: " + pResultClass.getName(), e);
        }
    }
    
    /**
     * Checks freed for the current JDBC operation.
     * 
     * @throws SQLException if the operation fails
     */
    private void checkFreed() throws SQLException
    {
        if (freed)
        {
            throw new SQLException("SQLXML has been freed");
        }
    }

    /**
     * Handles the cached string operation for the remote JDBC resource.
     * 
     * @return the resulting JDBC value
     * @throws SQLException if the JDBC operation cannot be completed
     */
    private String cachedString() throws SQLException
    {
        checkFreed();

        if (!xmlLoaded)
        {
            cachedXmlNode = null;

            return null;
        }

        return cachedXmlNode == null ? null : cachedXmlNode.toString();
    }

	/**
	 * Handles the load string operation for the remote JDBC resource.
	 * 
	 * @return the resulting JDBC value
	 * @throws SQLException if the JDBC operation cannot be completed
	 */
    private String loadString() throws SQLException
    {
        checkFreed();
        syncPendingResult();

        if (xmlLoaded)
        {
            return cachedString();
        }
        
        String value = (String)RemoteUtil.invoke(client, id, SQLXML.class, "getString", new Class<?>[]{}, new Object[]{}, String.class);
        
        try
        {
            cachedXmlNode = com.sibvisions.util.xml.XmlWorker.readNode(new ByteArrayInputStream(value.getBytes("UTF-8")));
            
            xmlLoaded = true;
        }
        catch (Exception e)
        {
            throw new SQLException("Could not parse SQLXML", e);
        }

        return value;
    }

	/**
	 * Handles the sync pending result operation for the remote JDBC resource.
	 * 
	 * @throws SQLException if the operation fails
	 */
	private void syncPendingResult() throws SQLException
    {
        if (pendingResult == null)
        {
            return;
        }
        
        try
        {
            StringWriter writer = new StringWriter();

            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.transform(resultAsSource(pendingResult), new StreamResult(writer));
            
            RemoteUtil.invoke(client, id, SQLXML.class, "setString", new Class<?>[]{String.class}, new Object[]{writer.toString()}, void.class);
            
            pendingResult = null;
        }
        catch (SQLException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            throw new SQLException("Could not synchronize SQLXML Result", e);
        }
    }

	/**
	 * Handles the result as source operation for the remote JDBC resource.
	 *
	 * @param pResult the result
	 * @return the resulting JDBC value
	 * @throws Exception if the operation fails
	 */
	private static Source resultAsSource(Result pResult) throws Exception
    {
        if (pResult instanceof DOMResult)
        {
            return new DOMSource(((DOMResult)pResult).getNode());
        }

        if (pResult instanceof StreamResult)
        {
            StreamResult stream = (StreamResult)pResult;

            if (stream.getWriter() != null)
            {
                return new StreamSource(new StringReader(stream.getWriter().toString()));
            }

            if (stream.getOutputStream() != null)
            {
                return new StreamSource(new ByteArrayInputStream(((ByteArrayOutputStream)stream.getOutputStream()).toByteArray()));
            }

            if (stream.getSystemId() != null)
            {
                return new StreamSource(stream.getSystemId());
            }
        }
        
        throw new SQLFeatureNotSupportedException("Unsupported SQLXML Result implementation: " + pResult.getClass().getName());
    }
}
