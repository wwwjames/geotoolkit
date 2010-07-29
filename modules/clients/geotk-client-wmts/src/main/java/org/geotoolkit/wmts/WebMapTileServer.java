/*
 *    Geotoolkit - An Open Source Java GIS Toolkit
 *    http://www.geotoolkit.org
 *
 *    (C) 2009-2010, Geomatys
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package org.geotoolkit.wmts;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.geotoolkit.client.Server;
import org.geotoolkit.util.logging.Logging;
import org.geotoolkit.wmts.v100.GetCapabilities100;
import org.geotoolkit.wmts.v100.GetTile100;
import org.geotoolkit.wmts.xml.WMTSBindingUtilities;
import org.geotoolkit.wmts.xml.WMTSVersion;
import org.geotoolkit.wmts.xml.v100.Capabilities;


/**
 * Generates WMTS requests objects on a WMTS server.
 *
 * @author Guilhem Legal (Geomatys)
 * @module pending
 */
public class WebMapTileServer implements Server{

    private static final Logger LOGGER = Logging.getLogger(WebMapTileServer.class);

    private final WMTSVersion version;
    private final URL serverURL;
    private Capabilities capabilities;

    /**
     * Builds a web map server with the given server url and version.
     *
     * @param serverURL The server base url.
     * @param version A string representation of the service version.
     * @throws IllegalArgumentException if the version specified is not applyable.
     */
    public WebMapTileServer(final URL serverURL, final String version) {
        this(serverURL, WMTSVersion.getVersion(version));
    }

    /**
     * Builds a web map server with the given server url and version.
     *
     * @param serverURL The server base url.
     * @param version The service version.
     */
    public WebMapTileServer(final URL serverURL, final WMTSVersion version) {
        this(serverURL, version, null);
    }

    /**
     * Builds a web map server with the given server url, version and getCapabilities response.
     *
     * @param serverURL The server base url.
     * @param version A string representation of the service version.
     * @param capabilities A getCapabilities response.
     * @throws IllegalArgumentException if the version specified is not applyable.
     */
    public WebMapTileServer(final URL serverURL, final String version, final Capabilities capabilities) {
        this(serverURL, WMTSVersion.getVersion(version), capabilities);
    }

    /**
     * Builds a web map server with the given server url, version and getCapabilities response.
     *
     * @param serverURL The server base url.
     * @param version A string representation of the service version.
     * @param capabilities A getCapabilities response.
     */
    public WebMapTileServer(final URL serverURL, final WMTSVersion version, final Capabilities capabilities) {
        this.version = version;
        this.serverURL = serverURL;
        this.capabilities = capabilities;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public URI getURI(){
        try {
            return serverURL.toURI();
        } catch (URISyntaxException ex) {
            LOGGER.log(Level.WARNING, ex.getLocalizedMessage(), ex);
        }
        return null;
    }

    /**
     * Returns the {@linkplain AbstractWMSCapabilities capabilities} response for this
     * request.
     */
    public Capabilities getCapabilities() {

        if (capabilities != null) {
            return capabilities;
        }
        //Thread to prevent infinite request on a server
        final Thread thread = new Thread() {
            @Override
            public void run() {
                try {
                    capabilities = WMTSBindingUtilities.unmarshall(createGetCapabilities().getURL(), version);
                } catch (Exception ex) {
                    capabilities = null;
                    try {
                        LOGGER.log(Level.WARNING, "Wrong URL, the server doesn't answer : " +
                                createGetCapabilities().getURL().toString(), ex);
                    } catch (MalformedURLException ex1) {
                        LOGGER.log(Level.WARNING, "Malformed URL, the server doesn't answer. ", ex1);
                    }
                }
            }
        };
        thread.start();
        final long start = System.currentTimeMillis();
        try {
            thread.join(10000);
        } catch (InterruptedException ex) {
            LOGGER.log(Level.WARNING, "The thread to obtain GetCapabilities doesn't answer.", ex);
        }
        if ((System.currentTimeMillis() - start) > 10000) {
            LOGGER.log(Level.WARNING, "TimeOut error, the server takes too much time to answer. ");
        }

        return capabilities;
    }

    /**
     * Returns the request version.
     */
    public WMTSVersion getVersion() {
        return version;
    }

    /**
     * Returns the request object, in the version chosen.
     *
     * @throws IllegalArgumentException if the version requested is not supported.
     */
    public GetTileRequest createGetTile() {
        switch (version) {
            case v100:
                return new GetTile100(serverURL.toString());
            default:
                throw new IllegalArgumentException("Version was not defined");
        }
    }

    /**
     * Returns the request object, in the version chosen.
     *
     * @throws IllegalArgumentException if the version requested is not supported.
     */
    public GetCapabilitiesRequest createGetCapabilities() {
        switch (version) {
            case v100:
                return new GetCapabilities100(serverURL.toString());
            default:
                throw new IllegalArgumentException("Version was not defined");
        }
    }

}
