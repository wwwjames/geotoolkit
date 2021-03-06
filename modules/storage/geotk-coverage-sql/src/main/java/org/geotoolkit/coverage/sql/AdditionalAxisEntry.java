/*
 *    Geotoolkit.org - An Open Source Java GIS Toolkit
 *    http://www.geotoolkit.org
 *
 *    (C) 2018, Geomatys
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
package org.geotoolkit.coverage.sql;

import org.opengis.referencing.crs.SingleCRS;
import org.opengis.referencing.crs.VerticalCRS;
import org.opengis.referencing.crs.TemporalCRS;
import org.opengis.referencing.operation.MathTransform1D;
import org.opengis.metadata.spatial.DimensionNameType;
import org.apache.sis.referencing.operation.transform.MathTransforms;


/**
 * Information about an additional axis (vertical or other).
 *
 * @author Martin Desruisseaux (Geomatys)
 * @author Johann Sorel (Geomatys)
 */
final class AdditionalAxisEntry extends Entry {
    /**
     * The coordinate reference system for this entry.
     */
    final SingleCRS crs;

    /**
     * The transform from grid coordinates to the {@linkplain #crs}.
     */
    final MathTransform1D gridToCRS;

    /**
     * Number of values along this axis.
     */
    final int count;

    /**
     * Creates a new entry for an additional axis.
     *
     * @param values  limits of all layers. The array length is the number of layers + 1.
     *                The first and last values are the raster bounds along the axis.
     *                Other values are interstice between layers.
     */
    AdditionalAxisEntry(final SingleCRS crs, final double[] values) {
        this.crs  = crs;
        gridToCRS = MathTransforms.interpolate(null, values);       // Integer indices map lower bounds.
        count     = values.length - 1;
    }

    /**
     * Returns a standardized identifier for this axis, or {@code null} if none.
     */
    final DimensionNameType type() {
        if (crs instanceof VerticalCRS) {
            return DimensionNameType.VERTICAL;
        } else if (crs instanceof TemporalCRS) {
            return DimensionNameType.TIME;
        } else {
            return null;
        }
    }
}
