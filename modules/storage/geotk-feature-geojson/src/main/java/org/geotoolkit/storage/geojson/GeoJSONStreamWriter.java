/*
 *    Geotoolkit - An Open Source Java GIS Toolkit
 *    http://www.geotoolkit.org
 *
 *    (C) 2020, Geomatys
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
package org.geotoolkit.storage.geojson;

import com.fasterxml.jackson.core.JsonEncoding;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.apache.sis.feature.builder.FeatureTypeBuilder;
import org.apache.sis.feature.builder.PropertyTypeBuilder;
import org.apache.sis.internal.feature.AttributeConvention;
import org.apache.sis.storage.DataStoreException;
import org.apache.sis.storage.FeatureNaming;
import org.apache.sis.storage.IllegalNameException;
import org.apache.sis.util.collection.BackingStoreException;
import org.geotoolkit.internal.geojson.GeoJSONParser;
import org.geotoolkit.internal.geojson.GeoJSONUtils;
import org.locationtech.jts.geom.Geometry;
import org.opengis.feature.Feature;
import org.opengis.feature.FeatureType;
import org.opengis.feature.Operation;

/**
 * @author Quentin Boileau (Geomatys)
 * @author Johann Sorel (Geomatys)
 * @version 2.0
 * @since   2.0
 * @module
 */
public final class GeoJSONStreamWriter implements Iterator<Feature>, AutoCloseable {

    private final GeoJSONWriter writer;
    private final FeatureType featureType;

    private Feature edited;
    private Feature lastWritten;
    private int currentFeatureIdx;

    private final boolean hasIdentifier;

    final Function idConverter;

    /**
     *
     * @param outputStream stream were GeoJSON will be written
     * @param featureType {@link FeatureType} of features to write.
     * @param doubleAccuracy number of coordinates fraction digits
     * @throws DataStoreException
     */
    public GeoJSONStreamWriter(OutputStream outputStream, FeatureType featureType, final int doubleAccuracy)
            throws DataStoreException {
        this(outputStream, featureType, JsonEncoding.UTF8, doubleAccuracy);
    }

    /**
     *
     * @param outputStream stream were GeoJSON will be written
     * @param featureType {@link FeatureType} of features to write.
     * @param encoding character encoding
     * @param doubleAccuracy number of coordinates fraction digits
     * @throws DataStoreException
     */
    public GeoJSONStreamWriter(OutputStream outputStream, FeatureType featureType, final JsonEncoding encoding, final int doubleAccuracy)
            throws DataStoreException {
        this(outputStream, featureType, encoding, doubleAccuracy, false);
    }

    public GeoJSONStreamWriter(OutputStream outputStream, FeatureType featureType, final JsonEncoding encoding, final int doubleAccuracy, boolean prettyPrint)
            throws DataStoreException {

        //remove any operation attribute
        List<Operation> geometries = featureType.getProperties(true).stream()
                .filter(Operation.class::isInstance)
                .map(Operation.class::cast)
                .filter(AttributeConvention::isGeometryAttribute)
                .filter(GeoJSONUtils.IS_NOT_CONVENTION)
                .collect(Collectors.toList());

        final FeatureTypeBuilder ftb = new FeatureTypeBuilder(featureType);
        final Iterator<PropertyTypeBuilder> it = ftb.properties().iterator();
        final FeatureNaming naming = new FeatureNaming();
        geometries.stream()
                .map(Operation::getName)
                .forEach(name -> {
                    try {
                        naming.add(null, name, name);
                    } catch (IllegalNameException e) {
                        //hack
                    }
                });
        while (it.hasNext()) {
            try {
                naming.get(null, it.next().getName().toString());
                it.remove();
            } catch (IllegalNameException e) {
                // normal behavior
            }
        }

        for (final Operation op : geometries) {
            GeoJSONUtils.castOrUnwrap(op).ifPresent(ftb::addAttribute);
        }

        this.featureType = ftb.build();
        hasIdentifier = GeoJSONUtils.hasIdentifier(featureType);
        if (hasIdentifier) {
            idConverter = GeoJSONUtils.getIdentifierConverter(featureType);
        } else {
            // It should not be used, but we don't set it to null in case someons use it by mistake.
            idConverter = input -> input;
        }

        try {
            writer = new GeoJSONWriter(outputStream, GeoJSONParser.JSON_FACTORY, encoding, doubleAccuracy, prettyPrint);
            //start write feature collection.
            writer.writeStartFeatureCollection(GeoJSONUtils.getCRS(featureType), null);
            writer.flush();
        } catch (IOException ex) {
            throw new DataStoreException(ex.getMessage(), ex);
        }
    }

    /**
     * Utility method to write a single Feature into an OutputStream
     *
     * @param outputStream
     * @param feature to write
     * @param encoding
     * @param doubleAccuracy
     * @param prettyPrint
     */
    public static void writeSingleFeature(OutputStream outputStream, Feature feature, final JsonEncoding encoding,
            final int doubleAccuracy, boolean prettyPrint) throws IOException {

        try (GeoJSONWriter writer = new GeoJSONWriter(outputStream, GeoJSONParser.JSON_FACTORY, encoding, doubleAccuracy, prettyPrint)) {
            writer.writeSingleFeature(feature);
        }
    }

    /**
     * Utility method to write a single Geometry into an OutputStream
     *
     * @param outputStream
     * @param geometry to write
     * @param encoding
     * @param doubleAccuracy
     * @param prettyPrint
     */
    public static void writeSingleGeometry(OutputStream outputStream, Geometry geometry, final JsonEncoding encoding,
            final int doubleAccuracy, boolean prettyPrint) throws IOException {

        try (GeoJSONWriter writer = new GeoJSONWriter(outputStream, GeoJSONParser.JSON_FACTORY, encoding, doubleAccuracy, prettyPrint)) {
            writer.writeSingleGeometry(geometry);
        }
    }

    public FeatureType getFeatureType() {
        return featureType;
    }

    @Override
    public Feature next() throws BackingStoreException {
        edited = featureType.newInstance();
        if (hasIdentifier) {
            edited.setPropertyValue(AttributeConvention.IDENTIFIER_PROPERTY.toString(), idConverter.apply(currentFeatureIdx++));
        }
        return edited;
    }

    @Override
    public void remove() throws BackingStoreException {
        throw new BackingStoreException("Not supported on reader.");
    }

    public void write() throws BackingStoreException {
        if (edited == null || edited.equals(lastWritten)) {
            return;
        }

        lastWritten = edited;
        try {
            writer.writeFeature(edited);
            writer.flush();
        } catch (IOException | IllegalArgumentException e) {
            throw new BackingStoreException(e.getMessage(), e);
        }
    }

    @Override
    public boolean hasNext() throws BackingStoreException {
        return true;
    }

    @Override
    public void close() {
        try {
            writer.writeEndFeatureCollection();
            writer.flush();
            writer.close();
        } catch (IOException ex) {
            throw new BackingStoreException(ex);
        }
    }
}
