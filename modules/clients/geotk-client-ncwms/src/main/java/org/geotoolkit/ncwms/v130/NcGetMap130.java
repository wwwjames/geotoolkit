/*
 *    Geotoolkit - An Open Source Java GIS Toolkit
 *    http://www.geotoolkit.org
 *
 *    (C) 2011, Geomatys
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
package org.geotoolkit.ncwms.v130;

import org.geotoolkit.ncwms.AbstractNcGetMap;
import org.geotoolkit.security.ClientSecurity;



/**
 * Implementation for the GetMap request version 1.3.0.
 *
 * @author Olivier Terral (Geomatys)
 * @author Cédric Briançon (Geomatys)
 * @author Johann Sorel (Geomatys)
 * @module
 */
public class NcGetMap130 extends AbstractNcGetMap  {
    /**
     * Defines the server url and its version.
     *
     * @param serverURL The url of the webservice.
     */
    public NcGetMap130(final String serverURL, final ClientSecurity security){
        super(serverURL,"1.3.0",security);
    }

    @Override
    protected String getCRSParameterName() {
        return "CRS";
    }
}
