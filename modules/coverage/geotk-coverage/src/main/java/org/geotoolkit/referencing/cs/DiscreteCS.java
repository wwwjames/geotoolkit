/*
 *    Geotoolkit.org - An Open Source Java GIS Toolkit
 *    http://www.geotoolkit.org
 *
 *    (C) 2010, Open Source Geospatial Foundation (OSGeo)
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
package org.geotoolkit.referencing.cs;

import java.util.Set;
import java.util.Arrays;
import java.util.Collection;
import java.io.Serializable;

import org.opengis.util.GenericName;
import org.opengis.util.InternationalString;
import org.opengis.coverage.grid.GridEnvelope;
import org.opengis.coverage.grid.GridGeometry;
import org.opengis.referencing.ReferenceIdentifier;
import org.opengis.referencing.cs.TimeCS;
import org.opengis.referencing.cs.VerticalCS;
import org.opengis.referencing.cs.CartesianCS;
import org.opengis.referencing.cs.EllipsoidalCS;
import org.opengis.referencing.cs.CoordinateSystem;
import org.opengis.referencing.cs.CoordinateSystemAxis;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.Matrix;

import org.geotoolkit.lang.Decorator;
import org.geotoolkit.resources.Errors;
import org.geotoolkit.coverage.grid.GeneralGridEnvelope;
import org.geotoolkit.referencing.operation.transform.ProjectiveTransform;


/**
 * An implementation of {@link CoordinateSystem} delegating every method calls to the wrapped CS,
 * except the axes. The axes will be instances of {@link DiscreteCoordinateSystemAxis} built from
 * the ordinate values given at construction time.
 * <p>
 * This class implements {@link GridGeometry}. But the <cite>grid to CRS</cite> transform
 * returned by the later is correct only if every axes are regular.  This is not verified
 * because the threshold for determining if an axis is regular or not is at caller choice.
 *
 * @author Martin Desruisseaux (Geomatys)
 * @version 3.15
 *
 * @since 3.15
 * @module
 */
@Decorator(CoordinateSystem.class)
class DiscreteCS implements CoordinateSystem, GridGeometry, Serializable {
    /**
     * For cross-version compatibility.
     */
    private static final long serialVersionUID = -4078252991316521062L;

    /**
     * The wrapped coordinate system.
     */
    protected final CoordinateSystem cs;

    /**
     * The axes. Each elements in this array shall also implement the
     * {@link CoordinateSystemAxis} interface.
     */
    protected final DiscreteCoordinateSystemAxis[] axes;

    /**
     * The grid envelope, created only when first needed.
     */
    private transient GridEnvelope gridRange;

    /**
     * The grid to CRS transform, created only when first needed.
     */
    private transient MathTransform gridToCRS;

    /** A discrete cartesian CS. */
    static final class Cartesian extends DiscreteCS implements CartesianCS {
        private static final long serialVersionUID = -6589829160460750116L;
        Cartesian(final CartesianCS cs, final double[]... ordinates) throws IllegalArgumentException {
            super(cs, ordinates);
        }
    }

    /** A discrete ellipsoidal CS. */
    static final class Ellipsoidal extends DiscreteCS implements EllipsoidalCS {
        private static final long serialVersionUID = -847574453071413273L;
        Ellipsoidal(final EllipsoidalCS cs, final double[]... ordinates) throws IllegalArgumentException {
            super(cs, ordinates);
        }
    }

    /** A discrete vertical CS. */
    static final class Vertical extends DiscreteCS implements VerticalCS {
        private static final long serialVersionUID = 2486019427140923781L;
        Vertical(final VerticalCS cs, final double[]... ordinates) throws IllegalArgumentException {
            super(cs, ordinates);
        }
    }

    /** A discrete temporal CS. */
    static final class Time extends DiscreteCS implements TimeCS {
        private static final long serialVersionUID = -879676008647755725L;
        Time(final TimeCS cs, final double[]... ordinates) throws IllegalArgumentException {
            super(cs, ordinates);
        }
    }

    /**
     * Creates a new instance wrapping the given CS with the given ordinate values for each axis.
     *
     * @param  cs The coordinate system to wrap.
     * @param  ordinates The ordinate values for each axis. The arrays are <strong>not</strong> cloned.
     * @throws IllegalArgumentException If the length of the {@code ordinates} array is not equals
     *         to the {@linkplain CoordinateSystem#getDimension() dimension} of the given coordinate
     *         system.
     */
    DiscreteCS(final CoordinateSystem cs, final double[]... ordinates) throws IllegalArgumentException {
        this.cs = cs;
        final int dimension = cs.getDimension();
        if (dimension != ordinates.length) {
            throw new IllegalArgumentException(Errors.format(
                    Errors.Keys.MISMATCHED_DIMENSION_$3, "ordinates", ordinates.length, dimension));
        }
        axes = new DiscreteCoordinateSystemAxis[dimension];
        for (int i=0; i<dimension; i++) {
            axes[i] = DiscreteReferencingFactory.createDiscreteAxis(cs.getAxis(i), ordinates[i]);
        }
    }

    /**
     * Creates a new instance wrapping the given CS with the given axes.
     *
     * @param cs The coordinate system to wrap.
     * @param ordinates The axes. This array is <strong>not</strong> cloned.
     */
    DiscreteCS(final CoordinateSystem cs, final DiscreteCoordinateSystemAxis... axes) {
        this.cs = cs;
        this.axes = axes;
    }

    /**
     * Returns the name of the wrapped CS.
     */
    @Override
    public final ReferenceIdentifier getName() {
        return cs.getName();
    }

    /**
     * Returns the alias of the wrapped CS.
     */
    @Override
    public final Collection<GenericName> getAlias() {
        return cs.getAlias();
    }

    /**
     * Returns the identifiers of the wrapped CS.
     */
    @Override
    public final Set<ReferenceIdentifier> getIdentifiers() {
        return cs.getIdentifiers();
    }

    /**
     * Returns the number of axes. This is the length of the array given at
     * construction time.
     */
    @Override
    public final int getDimension() {
        return axes.length;
    }

    /**
     * Returns the axes at the given dimension. This is one of the axis given
     * at construction time.
     */
    @Override
    public final CoordinateSystemAxis getAxis(final int dimension) throws IndexOutOfBoundsException {
        return (CoordinateSystemAxis) axes[dimension];
    }

    /**
     * Returns the grid range. The {@linkplain GridEnvelope#getLow() lower} values are
     * always 0, and the {@linkplain GridEnvelope#getHigh() upper} values are determined
     * by the number of discrete ordinates for each axes.
     */
    @Override
    public final synchronized GridEnvelope getGridRange() {
        if (gridRange == null) {
            final int[] upper = new int[axes.length];
            for (int i=0; i<upper.length; i++) {
                upper[i] = axes[i].length();
            }
            gridRange = new GeneralGridEnvelope(new int[upper.length], upper, false);
        }
        return gridRange;
    }

    /**
     * Returns the transform from grid coordinates to CRS coordinates mapping pixel center.
     * This method assumes that all axes are regular (this is not verified).
     */
    @Override
    public final synchronized MathTransform getGridToCRS() {
        if (gridToCRS == null) {
            final Matrix matrix = DiscreteReferencingFactory.getAffineTransform(axes);
            if (matrix == null) {
                throw new UnsupportedOperationException(Errors.format(Errors.Keys.NOT_AN_AFFINE_TRANSFORM));
            }
            gridToCRS = ProjectiveTransform.create(matrix);
        }
        return gridToCRS;
    }

    /**
     * Returns the remarks of the wrapped CS.
     */
    @Override
    public final InternationalString getRemarks() {
        return cs.getRemarks();
    }

    /**
     * Returns the WKT formatted by the wrapped CS. This is okay to delegate to the
     * wrapped CS despite having different axes, because the WKT representation of
     * those axes is not changed.
     */
    @Override
    public final String toWKT() throws UnsupportedOperationException {
        return cs.toWKT();
    }

    /**
     * Returns the string representation of the wrapped CS.
     * This is usually the same than the WKT representation.
     */
    @Override
    public final String toString() {
        return cs.toString();
    }

    /**
     * Returns a hash code value for this CS.
     */
    @Override
    public final int hashCode() {
        return cs.hashCode() + 31 * Arrays.hashCode(axes);
    }

    /**
     * Compares this CS with the given object for equality.
     *
     * @param other The object to compare with this CS for equality.
     */
    @Override
    public final boolean equals(final Object other) {
        if (other != null && other.getClass().equals(getClass())) {
            final DiscreteCS that = (DiscreteCS) other;
            return cs.equals(that.cs) && Arrays.equals(axes, that.axes);
        }
        return false;
    }
}
