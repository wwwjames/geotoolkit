/*
 *    Geotoolkit.org - An Open Source Java GIS Toolkit
 *    http://www.geotoolkit.org
 *
 *    (C) 2001-2011, Open Source Geospatial Foundation (OSGeo)
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
 *    This package contains documentation from OpenGIS specifications.
 *    OpenGIS consortium's work is fully acknowledged here.
 */
package org.geotoolkit.referencing.crs;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import javax.measure.unit.Unit;
import javax.measure.unit.NonSI;
import javax.measure.quantity.Angle;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import net.jcip.annotations.Immutable;

import org.opengis.referencing.cs.AxisDirection;
import org.opengis.referencing.cs.CoordinateSystem;
import org.opengis.referencing.cs.CoordinateSystemAxis;
import org.opengis.referencing.cs.EllipsoidalCS;
import org.opengis.referencing.datum.Ellipsoid;
import org.opengis.referencing.datum.GeodeticDatum;
import org.opengis.referencing.crs.GeographicCRS;
import org.opengis.geometry.MismatchedDimensionException;

import org.geotoolkit.measure.Measure;
import org.geotoolkit.metadata.iso.extent.DefaultExtent;
import org.geotoolkit.referencing.IdentifiedObjects;
import org.geotoolkit.referencing.AbstractReferenceSystem;
import org.geotoolkit.referencing.cs.DefaultEllipsoidalCS;
import org.geotoolkit.referencing.datum.DefaultEllipsoid;
import org.geotoolkit.referencing.datum.DefaultGeodeticDatum;
import org.geotoolkit.internal.referencing.AxisDirections;
import org.geotoolkit.util.UnsupportedImplementationException;
import org.geotoolkit.io.wkt.Formatter;
import org.geotoolkit.measure.Units;


/**
 * A coordinate reference system based on an ellipsoidal approximation of the geoid; this provides
 * an accurate representation of the geometry of geographic features for a large portion of the
 * earth's surface.
 *
 * <TABLE CELLPADDING='6' BORDER='1'>
 * <TR BGCOLOR="#EEEEFF"><TH NOWRAP>Used with CS type(s)</TH></TR>
 * <TR><TD>
 *   {@link EllipsoidalCS Ellipsoidal}
 * </TD></TR></TABLE>
 *
 * @author Martin Desruisseaux (IRD, Geomatys)
 * @version 3.15
 *
 * @since 1.2
 * @module
 */
@Immutable
@XmlRootElement(name = "GeographicCRS")
public class DefaultGeographicCRS extends AbstractSingleCRS implements GeographicCRS {
    /**
     * Serial number for inter-operability with different versions.
     */
    private static final long serialVersionUID = 861224913438092335L;

    /**
     * A two-dimensional geographic coordinate reference system using the WGS84 datum.
     * This CRS uses (<var>longitude</var>, <var>latitude</var>) ordinates with longitude values
     * increasing towards the East and latitude values increasing towards the North. The angular
     * units are decimal degrees and the prime meridian is Greenwich.
     * <p>
     * This CRS is equivalent to {@code EPSG:4326} except for axis order,
     * since EPSG puts latitude before longitude.
     *
     * @see DefaultGeodeticDatum#WGS84
     */
    public static final DefaultGeographicCRS WGS84;

    /**
     * A three-dimensional geographic coordinate reference system using the WGS84 datum.
     * This CRS uses (<var>longitude</var>, <var>latitude</var>, <var>height</var>)
     * ordinates with longitude values increasing towards the East, latitude values
     * increasing towards the North and height positive above the ellipsoid. The angular
     * units are decimal degrees, the height unit is the metre, and the prime meridian
     * is Greenwich.
     * <p>
     * This CRS is equivalent to {@code EPSG:4979} (the successor to
     * {@code EPSG:4329}, itself the successor to {@code EPSG:4327}) except for
     * axis order, since EPSG puts latitude before longitude.
     *
     * @see DefaultGeodeticDatum#WGS84
     */
    public static final DefaultGeographicCRS WGS84_3D;
    static {
        final Map<String,Object> properties = new HashMap<String,Object>(4);
        properties.put(NAME_KEY, "WGS84(DD)"); // Name used in WCS 1.0.
        final String[] alias = {
            "WGS84",
            "WGS 84",                // EPSG:4979 name.
            "WGS 84 (3D)",           // EPSG:4329 name.
            "WGS 84 (geographic 3D)" // EPSG:4327 name.
        };
        properties.put(ALIAS_KEY, alias);
        properties.put(DOMAIN_OF_VALIDITY_KEY, DefaultExtent.WORLD);
        // Do not declare EPSG identifiers, because axis order are not the same.
        WGS84    = new DefaultGeographicCRS(properties, DefaultGeodeticDatum.WGS84,
                                            DefaultEllipsoidalCS.GEODETIC_2D);
        alias[1] = "WGS 84 (geographic 3D)"; // Replaces the EPSG name.
        WGS84_3D = new DefaultGeographicCRS(properties, DefaultGeodeticDatum.WGS84,
                                            DefaultEllipsoidalCS.GEODETIC_3D);
    }

    /**
     * A two-dimensional geographic coordinate reference system using a spherical datum.
     * This CRS uses (<var>longitude</var>, <var>latitude</var>) ordinates with longitude values
     * increasing towards the East and latitude values increasing towards the North. The angular
     * units are decimal degrees and the prime meridian is Greenwich.
     *
     * {@note This CRS is close, but not identical, to the geographic CRS based on the <cite>GRS
     *        1980 Authalic Sphere</cite> (EPSG:4047). This CRS uses a sphere radius of 6371000
     *        metres, while the GRS 1980 Authalic Sphere uses a sphere radius of 6371007 metres.}
     *
     * @see DefaultGeodeticDatum#SPHERE
     *
     * @since 3.15
     */
    public static final DefaultGeographicCRS SPHERE = new DefaultGeographicCRS(
            IdentifiedObjects.getProperties(DefaultGeodeticDatum.SPHERE),
            DefaultGeodeticDatum.SPHERE, DefaultEllipsoidalCS.GEODETIC_2D);

    /**
     * Constructs a new object in which every attributes are set to a default value.
     * <strong>This is not a valid object.</strong> This constructor is strictly
     * reserved to JAXB, which will assign values to the fields using reflexion.
     */
    private DefaultGeographicCRS() {
        this(org.geotoolkit.internal.referencing.NullReferencingObject.INSTANCE);
    }

    /**
     * Constructs a new geographic CRS with the same values than the specified one.
     * This copy constructor provides a way to wrap an arbitrary implementation into a
     * Geotk one or a user-defined one (as a subclass), usually in order to leverage
     * some implementation-specific API. This constructor performs a shallow copy,
     * i.e. the properties are not cloned.
     *
     * @param crs The coordinate reference system to copy.
     *
     * @since 2.2
     */
    public DefaultGeographicCRS(final GeographicCRS crs) {
        super(crs);
    }

    /**
     * Constructs a geographic CRS with the same properties than the given datum.
     * The inherited properties include the {@linkplain #getName name} and aliases.
     *
     * @param datum The datum.
     * @param cs The coordinate system.
     *
     * @since 2.5
     */
    public DefaultGeographicCRS(final GeodeticDatum datum, final EllipsoidalCS cs) {
        this(IdentifiedObjects.getProperties(datum), datum, cs);
    }

    /**
     * Constructs a geographic CRS from a name.
     *
     * @param name The name.
     * @param datum The datum.
     * @param cs The coordinate system.
     */
    public DefaultGeographicCRS(final String         name,
                                final GeodeticDatum datum,
                                final EllipsoidalCS    cs)
    {
        this(Collections.singletonMap(NAME_KEY, name), datum, cs);
    }

    /**
     * Constructs a geographic CRS from a set of properties. The properties are given unchanged to
     * the {@linkplain AbstractReferenceSystem#AbstractReferenceSystem(Map) super-class constructor}.
     *
     * @param properties Set of properties. Should contains at least {@code "name"}.
     * @param datum The datum.
     * @param cs The coordinate system.
     */
    public DefaultGeographicCRS(final Map<String,?> properties,
                                final GeodeticDatum datum,
                                final EllipsoidalCS cs)
    {
        super(properties, datum, cs);
    }

    /**
     * Returns the coordinate system.
     */
    @Override
    @XmlElement(name="ellipsoidalCS")
    public EllipsoidalCS getCoordinateSystem() {
        return (EllipsoidalCS) super.getCoordinateSystem();
    }

    /**
     * Used by JAXB only (invoked by reflection).
     */
    final void setCoordinateSystem(final EllipsoidalCS cs) {
        super.setCoordinateSystem(cs);
    }

    /**
     * Returns the datum.
     */
    @Override
    @XmlElement(name="geodeticDatum")
    public GeodeticDatum getDatum() {
        return (GeodeticDatum) super.getDatum();
    }

    /**
     * Used by JAXB only (invoked by reflection).
     */
    final void setDatum(final GeodeticDatum datum) {
        super.setDatum(datum);
    }

    /**
     * Computes the orthodromic distance between two points. This convenience method delegates
     * the work to the underlying {@linkplain DefaultEllipsoid ellipsoid}, if possible.
     *
     * @param  coord1 Coordinates of the first point.
     * @param  coord2 Coordinates of the second point.
     * @return The distance between {@code coord1} and {@code coord2}.
     * @throws UnsupportedOperationException if this coordinate reference system can't compute
     *         distances.
     * @throws MismatchedDimensionException if a coordinate doesn't have the expected dimension.
     */
    @Override
    public Measure distance(final double[] coord1, final double[] coord2)
            throws UnsupportedOperationException, MismatchedDimensionException
    {
        final DefaultEllipsoidalCS cs;
        final DefaultEllipsoid e;
        final CoordinateSystem coordinateSystem = getCoordinateSystem();
        if (!(coordinateSystem instanceof DefaultEllipsoidalCS)) {
            throw new UnsupportedImplementationException(coordinateSystem.getClass());
        }
        final Ellipsoid ellipsoid = getDatum().getEllipsoid();
        if (!(ellipsoid instanceof DefaultEllipsoid)) {
            throw new UnsupportedImplementationException(ellipsoid.getClass());
        }
        cs = (DefaultEllipsoidalCS) coordinateSystem;
        e  = (DefaultEllipsoid)     ellipsoid;
        if (coord1.length!=2 || coord2.length!=2 || cs.getDimension()!=2) {
            /*
             * Not yet implemented (an exception will be thrown later).
             * We should probably revisit the way we compute distances.
             */
            return super.distance(coord1, coord2);
        }
        return new Measure(e.orthodromicDistance(cs.getLongitude(coord1),
                                                 cs.getLatitude (coord1),
                                                 cs.getLongitude(coord2),
                                                 cs.getLatitude (coord2)), e.getAxisUnit());
    }

    /**
     * Returns a hash value for this geographic CRS.
     *
     * @return The hash code value. This value doesn't need to be the same
     *         in past or future versions of this class.
     */
    @Override
    public int hashCode() {
        return (int)serialVersionUID ^ super.hashCode();
    }

    /**
     * Returns the angular unit of the specified coordinate system.
     * The preference will be given to the longitude axis, if found.
     */
    static Unit<Angle> getAngularUnit(final CoordinateSystem coordinateSystem) {
        Unit<Angle> unit = NonSI.DEGREE_ANGLE;
        for (int i=coordinateSystem.getDimension(); --i>=0;) {
            final CoordinateSystemAxis axis = coordinateSystem.getAxis(i);
            final Unit<?> candidate = axis.getUnit();
            if (Units.isAngular(candidate)) {
                unit = candidate.asType(Angle.class);
                if (AxisDirection.EAST.equals(AxisDirections.absolute(axis.getDirection()))) {
                    break; // Found the longitude axis.
                }
            }
        }
        return unit;
    }

    /**
     * Formats the inner part of a
     * <A HREF="http://www.geoapi.org/snapshot/javadoc/org/opengis/referencing/doc-files/WKT.html#GEOGCS"><cite>Well
     * Known Text</cite> (WKT)</A> element.
     *
     * @param  formatter The formatter to use.
     * @return The name of the WKT element type, which is {@code "GEOGCS"}.
     */
    @Override
    public String formatWKT(final Formatter formatter) {
        final Unit<Angle> oldUnit = formatter.getAngularUnit();
        final Unit<Angle> unit = getAngularUnit(getCoordinateSystem());
        final GeodeticDatum datum = getDatum();
        formatter.setAngularUnit(unit);
        formatter.append(datum);
        formatter.append(datum.getPrimeMeridian());
        formatter.append(unit);
        final int dimension = getDimension();
        for (int i=0; i<dimension; i++) {
            formatter.append(getAxis(i));
        }
        if (!unit.equals(getUnit())) {
            formatter.setInvalidWKT(GeographicCRS.class);
        }
        formatter.setAngularUnit(oldUnit);
        return "GEOGCS";
    }
}
