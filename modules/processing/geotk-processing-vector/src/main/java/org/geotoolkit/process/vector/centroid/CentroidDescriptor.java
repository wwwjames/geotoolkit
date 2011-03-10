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

package org.geotoolkit.process.vector.centroid;

import org.geotoolkit.process.ProcessDescriptor;
import org.geotoolkit.process.Process;
import org.geotoolkit.process.vector.VectorDescriptor;

/**
 * Description of centroid process.
 * @author Quentin Boleau
 * @module pending
 */
public class CentroidDescriptor extends VectorDescriptor {

    public static final String NAME = "centroid";

    public static final ProcessDescriptor INSTANCE = new CentroidDescriptor();

    private CentroidDescriptor(){
        super(NAME, "Return the centroid of a feature");
    }

    @Override
    public Process createProcess() {
        return new Centroid();
    }

}
