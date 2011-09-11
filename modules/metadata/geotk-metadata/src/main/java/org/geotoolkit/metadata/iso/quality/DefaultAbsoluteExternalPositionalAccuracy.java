/*
 *    Geotoolkit.org - An Open Source Java GIS Toolkit
 *    http://www.geotoolkit.org
 *
 *    (C) 2004-2011, Open Source Geospatial Foundation (OSGeo)
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
package org.geotoolkit.metadata.iso.quality;

import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlRootElement;
import net.jcip.annotations.ThreadSafe;

import org.opengis.metadata.quality.Result;
import org.opengis.metadata.quality.AbsoluteExternalPositionalAccuracy;


/**
 * Closeness of reported coordinate values to values accepted as or being true.
 *
 * @author Martin Desruisseaux (IRD, Geomatys)
 * @author Touraïvane (IRD)
 * @version 3.19
 *
 * @since 2.1
 * @module
 */
@ThreadSafe
@XmlType(name = "DQ_AbsoluteExternalPositionalAccuracy_Type")
@XmlRootElement(name = "DQ_AbsoluteExternalPositionalAccuracy")
public class DefaultAbsoluteExternalPositionalAccuracy extends AbstractPositionalAccuracy
       implements AbsoluteExternalPositionalAccuracy
{
    /**
     * Serial number for inter-operability with different versions.
     */
    private static final long serialVersionUID = 4116627805950579738L;

    /**
     * Constructs an initially empty absolute external positional accuracy.
     */
    public DefaultAbsoluteExternalPositionalAccuracy() {
    }

    /**
     * Constructs a metadata entity initialized with the values from the specified metadata.
     *
     * @param source The metadata to copy, or {@code null} if none.
     *
     * @since 2.4
     */
    public DefaultAbsoluteExternalPositionalAccuracy(final AbsoluteExternalPositionalAccuracy source) {
        super(source);
    }

    /**
     * Creates an positional accuracy initialized to the given result.
     *
     * @param result The value obtained from applying a data quality measure against a specified
     *               acceptable conformance quality level.
     */
    public DefaultAbsoluteExternalPositionalAccuracy(final Result result) {
        super(result);
    }

    /**
     * Returns a Geotk metadata implementation with the same values than the given arbitrary
     * implementation. If the given object is {@code null}, then this method returns {@code null}.
     * Otherwise if the given object is already a Geotk implementation, then the given object is
     * returned unchanged. Otherwise a new Geotk implementation is created and initialized to the
     * attribute values of the given object, using a <cite>shallow</cite> copy operation
     * (i.e. attributes are not cloned).
     *
     * @param  object The object to get as a Geotk implementation, or {@code null} if none.
     * @return A Geotk implementation containing the values of the given object (may be the
     *         given object itself), or {@code null} if the argument was null.
     *
     * @since 3.18
     */
    public static DefaultAbsoluteExternalPositionalAccuracy castOrCopy(final AbsoluteExternalPositionalAccuracy object) {
        return (object == null) || (object instanceof DefaultAbsoluteExternalPositionalAccuracy)
                ? (DefaultAbsoluteExternalPositionalAccuracy) object : new DefaultAbsoluteExternalPositionalAccuracy(object);
    }
}
