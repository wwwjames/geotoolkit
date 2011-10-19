/*
 *    Geotoolkit.org - An Open Source Java GIS Toolkit
 *    http://www.geotoolkit.org
 *
 *    (C) 1999-2011, Open Source Geospatial Foundation (OSGeo)
 *    (C) 2009-2011, Geomatys
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
 *
 *    This package contains formulas from the PROJ package of USGS.
 *    USGS's work is fully acknowledged here. This derived work has
 *    been relicensed under LGPL with Frank Warmerdam's permission.
 */
package org.geotoolkit.referencing.operation.projection;

import java.awt.geom.Point2D;
import net.jcip.annotations.Immutable;

import org.opengis.referencing.operation.Matrix;
import org.geotoolkit.referencing.operation.matrix.Matrix2;

import static java.lang.Math.*;


/**
 * The USGS equatorial case of the {@linkplain Stereographic stereographic} projection.
 * This is a special case of oblique stereographic projection for a latitude of origin
 * set to 0&deg;.
 *
 * @author André Gosselin (MPO)
 * @author Martin Desruisseaux (MPO, IRD, Geomatys)
 * @author Rueben Schulz (UBC)
 * @author Rémi Maréchal (Geomatys)
 * @version 3.18
 *
 * @see PolarStereographic
 * @see ObliqueStereographic
 *
 * @since 2.0
 * @module
 */
@Immutable
public class EquatorialStereographic extends Stereographic {
    /**
     * For cross-version compatibility.
     */
    private static final long serialVersionUID = -5098015759558831875L;

    /**
     * Constructs an equatorial stereographic projection (EPSG equations).
     *
     * @param parameters The parameters of the projection to be created.
     */
    protected EquatorialStereographic(final Parameters parameters) {
        super(parameters);
        assert φ0 == 0 : φ0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Matrix transform(final double[] srcPts, final int srcOff,
                            final double[] dstPts, final int dstOff, boolean derivate)
            throws ProjectionException
    {
        double x = rollLongitude(srcPts[srcOff]);
        double y = srcPts[srcOff + 1];
        final double χ = 2*atan(ssfn(y, sin(y))) - PI/2;
        final double cosχ = cos(χ);
        final double A = 1 + cosχ * cos(x);    // typo in (12-29)
        x = (cosχ * sin(x)) / A;
        y = sin(χ) / A;
        /*
         * The multiplication by k0 is performed by the "denormalize" affine transform.
         */
        assert checkTransform(srcPts, srcOff, dstPts, dstOff, x, y);
        dstPts[dstOff]   = x;
        dstPts[dstOff+1] = y;
        return null;
    }

    /**
     * Computes using oblique formulas and compare with the
     * result from equatorial formulas. Used in assertions only.
     */
    private boolean checkTransform(final double[] srcPts, final int srcOff,
                                   final double[] dstPts, final int dstOff,
                                   final double x, final double y)
            throws ProjectionException
    {
        super.transform(srcPts, srcOff, dstPts, dstOff, false);
        return Assertions.checkTransform(dstPts, dstOff, x, y);
    }

    /**
     * Provides the transform equations for the spherical case of the
     * Equatorial Stereographic projection.
     *
     * @author Gerald Evenden (USGS)
     * @author André Gosselin (MPO)
     * @author Martin Desruisseaux (MPO, IRD)
     * @author Rueben Schulz (UBC)
     * @version 3.00
     *
     * @since 2.4
     * @module
     */
    @Immutable
    static final class Spherical extends EquatorialStereographic {
        /**
         * For cross-version compatibility.
         */
        private static final long serialVersionUID = -4790138052004333003L;

        /**
         * Constructs a new map projection from the supplied parameters.
         *
         * @param parameters The parameters of the projection to be created.
         */
        protected Spherical(final Parameters parameters) {
            super(parameters);
            parameters.ensureSpherical();
        }

        /**
         * Returns {@code true} since this class uses spherical formulas.
         */
        @Override
        final boolean isSpherical() {
            return true;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Matrix transform(final double[] srcPts, final int srcOff,
                                final double[] dstPts, final int dstOff, boolean derivate)
                throws ProjectionException
        {
            double x = rollLongitude(srcPts[srcOff]);
            double y = srcPts[srcOff + 1];
            final double cosφ = cos(y);
            final double f = 1 + cosφ * cos(x); // Inverse of (21-14)
            x = cosφ * sin(x) / f;   // (21-2)
            y = sin(y)        / f;   // (21-13)

            assert checkTransform(srcPts, srcOff, dstPts, dstOff, x, y);
            dstPts[dstOff]   = x;
            dstPts[dstOff+1] = y;
            return null;
        }

        /**
         * Computes using ellipsoidal formulas and compare with the
         * result from spherical formulas. Used in assertions only.
         */
        private boolean checkTransform(final double[] srcPts, final int srcOff,
                                       final double[] dstPts, final int dstOff,
                                       final double x, final double y)
                throws ProjectionException
        {
            super.transform(srcPts, srcOff, dstPts, dstOff, false);
            return Assertions.checkTransform(dstPts, dstOff, x, y);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected void inverseTransform(final double[] srcPts, final int srcOff,
                                        final double[] dstPts, final int dstOff)
                throws ProjectionException
        {
            double x = srcPts[srcOff  ];
            double y = srcPts[srcOff+1];
            final double ρ = hypot(x, y);
            if (ρ < EPSILON) {
                y = 0; // φ0
                x = 0;
            } else {
                final double c    = 2*atan(ρ);
                final double sinc = sin(c);
                final double ct   = ρ*cos(c);
                final double t    = x*sinc;
                y = asin(y * sinc/ρ);  // (20-14)  with φ1=0
                x = atan2(t, ct);
            }
            x = unrollLongitude(x);
            assert checkInverseTransform(srcPts, srcOff, dstPts, dstOff, x, y);
            dstPts[dstOff  ] = x;
            dstPts[dstOff+1] = y;
        }

        /**
         * Computes using ellipsoidal formulas and compare with the
         * result from spherical formulas. Used in assertions only.
         */
        private boolean checkInverseTransform(final double[] srcPts, final int srcOff,
                                              final double[] dstPts, final int dstOff,
                                              final double λ, final double φ)
                throws ProjectionException
        {
            super.inverseTransform(srcPts, srcOff, dstPts, dstOff);
            return Assertions.checkInverseTransform(dstPts, dstOff, λ, φ);
        }

        /**
         * Gets the derivative of this transform at a point.
         *
         * @param  point The coordinate point where to evaluate the derivative.
         * @return The derivative at the specified point as a 2&times;2 matrix.
         * @throws ProjectionException if the derivative can't be evaluated at the specified point.
         *
         * @since 3.18
         */
        @Override
        public Matrix derivative(final Point2D point) throws ProjectionException {
            final double λ = rollLongitude(point.getX());
            final double φ = point.getY();
            final double sinφ = sin(φ);
            final double sinλ = sin(λ);
            final double cosφ = cos(φ);
            final double cosλ = cos(λ);
            final double cosφcosλ = cosφ*cosλ;
            final double cosφsinλ = cosφ*sinλ;
            final double sinφcosλ = sinφ*cosλ;
            final double F = 1 + cosφcosλ;
            final Matrix derivative = new Matrix2(
                    (cosφsinλ*cosφsinλ/F + cosφcosλ)  / F,
                    (sinφcosλ*cosφsinλ/F - sinφ*sinλ) / F,
                     cosφsinλ*sinφ / (F*F),
                    (sinφcosλ*sinφ/F + cosφ) / F);
            assert Assertions.checkDerivative(derivative, super.derivative(point));
            return derivative;
        }
    }

    /**
     * Gets the derivative of this transform at a point.
     *
     * @param  point The coordinate point where to evaluate the derivative.
     * @return The derivative at the specified point as a 2&times;2 matrix.
     * @throws ProjectionException if the derivative can't be evaluated at the specified point.
     *
     * @since 3.18
     */
    @Override
    public Matrix derivative(final Point2D point) throws ProjectionException {
        final double λ = rollLongitude(point.getX());
        final double φ = point.getY();
        final double sinφ     = sin(φ);
        final double sinλ     = sin(λ);
        final double cosλ     = cos(λ);
        final double ssfn     = ssfn(φ, sinφ);
        final double A        = (1 + ssfn*ssfn);
        final double dχ_dφ    = 2*dssfn_dφ(φ, sinφ, cos(φ))*ssfn / A;
        final double sinχ     = (ssfn*ssfn - 1) / A;
        final double cosχ     = 2*ssfn / A;
        final double cosχcosλ = cosχ*cosλ;
        final double cosχsinλ = cosχ*sinλ;
        final double sinχcosλ = sinχ*cosλ;
        final double F = 1 + cosχcosλ;
        return new Matrix2(
                (cosχsinλ*cosχsinλ/F + cosχcosλ)        / F,
                (sinχcosλ*cosχsinλ/F - sinχ*sinλ)*dχ_dφ / F,
                 cosχsinλ*sinχ / (F*F),
                (sinχcosλ*sinχ/F + cosχ)*dχ_dφ / F);
    }
}
