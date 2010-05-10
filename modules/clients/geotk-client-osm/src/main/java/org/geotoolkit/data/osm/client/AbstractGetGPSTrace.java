/*
 *    Geotoolkit - An Open Source Java GIS Toolkit
 *    http://www.geotoolkit.org
 *
 *    (C) 2010, Geomatys
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
package org.geotoolkit.data.osm.client;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import org.geotoolkit.client.AbstractRequest;
import org.geotoolkit.referencing.CRS;
import org.geotoolkit.referencing.crs.DefaultGeographicCRS;

import org.opengis.geometry.Envelope;
import org.opengis.referencing.operation.TransformException;


/**
 * Abstract implementation of {@link GetGPSTraceRequest}, which defines the
 * parameters for a get GPS trace request.
 *
 * @author Johann Sorel (Geomatys)
 * @module pending
 */
public abstract class AbstractGetGPSTrace extends AbstractRequest implements GetGPSTraceRequest{

    private Envelope envelope = null;
    private int page = 0;

    /**
     * Defines the server url and the service version for this kind of request.
     *
     * @param serverURL The server url.
     */
    protected AbstractGetGPSTrace(final String serverURL, String subpath){
        super(serverURL, subpath);
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void setEnvelope(Envelope env) {
        this.envelope = env;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public Envelope getEnvelope() {
        return envelope;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void setPage(int page) {
        this.page = page;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public int getPage() {
        return page;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public URL getURL() throws MalformedURLException {
        if(envelope == null){
            throw new IllegalArgumentException("Envelope is not defined");
        }

        final Envelope geoEnv;
        try {
            geoEnv = CRS.transform(envelope, DefaultGeographicCRS.WGS84);
        } catch (TransformException ex) {
            throw new IllegalArgumentException("Could not reproject given envelope to OSM projection", ex);
        }

        // bbox=left,bottom,right,top
        final StringBuilder sb = new StringBuilder();
        sb.append(geoEnv.getMinimum(0)).append(',').append(geoEnv.getMinimum(1)).append(',');
        sb.append(geoEnv.getMaximum(0)).append(',').append(geoEnv.getMaximum(1));
        requestParameters.put("bbox",sb.toString());
        requestParameters.put("page",Integer.toString(page));
        return super.getURL();
    }

    @Override
    public InputStream getSOAPResponse() throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
