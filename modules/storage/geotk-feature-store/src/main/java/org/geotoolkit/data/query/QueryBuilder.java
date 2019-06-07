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

package org.geotoolkit.data.query;

import java.util.Date;
import org.apache.sis.internal.feature.AttributeConvention;
import org.geotoolkit.factory.Hints;
import org.opengis.filter.Filter;
import org.opengis.filter.sort.SortBy;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.util.GenericName;


/**
 * Query builder, convenient utility class to build queries.
 *
 * @author Johann Sorel (Geomatys)
 * @module
 * @deprecated use SIS SimpleQuery
 */
@Deprecated
public final class QueryBuilder {

    private static final String[] ONLY_ID_PROPERTIES = new String[]{
        AttributeConvention.IDENTIFIER_PROPERTY.toString()
    };

    private String typeName = null;

    private Filter filter = Filter.INCLUDE;
    private String[] properties = null;
    private SortBy[] sortBy = null;
    private CoordinateReferenceSystem crs = null;
    private long startIndex = 0;
    private long maxFeatures = -1;
    private Hints hints = null;
    private double[] resolution = null;
    private String language = Query.GEOTK_QOM;
    private Object version = null;

    public QueryBuilder(){
    }

    public QueryBuilder(final Query query){
        copy(query);
    }

    public QueryBuilder(final String name){
        setTypeName(name);
    }

    public void reset(){
        typeName = null;
        filter = Filter.INCLUDE;
        properties = null;
        sortBy = null;
        crs = null;
        startIndex = 0;
        maxFeatures = -1;
        resolution = null;
        hints = null;
        version = null;
    }

    public void copy(final Query query){
        this.crs = query.getCoordinateSystemReproject();
        this.resolution = (query.getResolution()==null)?null:query.getResolution().clone();
        this.filter = query.getFilter();
        this.hints = query.getHints();
        this.maxFeatures = query.getLimit();
        this.properties = query.getPropertyNames();
        this.sortBy = query.getSortBy();
        this.startIndex = query.getOffset();
        this.typeName = query.getTypeName();
        this.version = query.getVersionDate();
        if(this.version==null) this.version = query.getVersionLabel();
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(final GenericName typeName) {
        this.typeName = typeName.toString();
    }

    public void setTypeName(final String typeName) {
        this.typeName = typeName;
    }

    public Filter getFilter() {
        return filter;
    }

    public void setFilter(final Filter filter) {
        this.filter = filter;
    }

    public String[] getProperties() {
        return properties;
    }

    public void setProperties(final String[] properties) {
        this.properties = properties;
    }

    public SortBy[] getSortBy() {
        return sortBy;
    }

    public void setSortBy(final SortBy[] sortBy) {
        this.sortBy = sortBy;
    }

    public long getStartIndex() {
        return startIndex;
    }

    public void setStartIndex(final long startIndex) {
        this.startIndex = startIndex;
    }

    public long getMaxFeatures() {
        return maxFeatures;
    }

    public void setMaxFeatures(final long maxFeatures) {
        this.maxFeatures = maxFeatures;
    }

    public CoordinateReferenceSystem getCRS() {
        return crs;
    }

    public void setCRS(final CoordinateReferenceSystem crs) {
        this.crs = crs;
    }

    public void setResolution(final double[] resolution) {
        this.resolution = resolution;
    }

    public double[] getResolution() {
        return resolution;
    }

    public void setVersionLabel(String label) {
        this.version = label;
    }

    public String getVersionLabel() {
        if(version instanceof String){
            return (String)version;
        }
        return null;
    }

    public void setVersionDate(Date version) {
        this.version = version;
    }

    public Date getVersionDate() {
        if(version instanceof Date){
            return (Date)version;
        }
        return null;
    }

    public Hints getHints() {
        return hints;
    }

    public void setHints(final Hints hints) {
        this.hints = hints;
    }

    public Query buildQuery(){
        return new Query(typeName, filter, properties, sortBy, crs, startIndex, maxFeatures, resolution, version, hints);
    }

    /**
     * Create a simple query with only a filter parameter.
     *
     * @param name
     * @param filter
     * @return Immutable query
     */
    public static Query filtered(final String name, final Filter filter){
        final QueryBuilder builder = new QueryBuilder();
        builder.setTypeName(name);
        builder.setFilter(filter);
        return builder.buildQuery();
    }

    /**
     * Create a simple query with only a sorted parameter.
     *
     * @param name
     * @param filter
     * @return Immutable query
     */
    public static Query sorted(final String name, final SortBy ... sorts){
        final QueryBuilder builder = new QueryBuilder();
        builder.setTypeName(name);
        builder.setSortBy(sorts);
        return builder.buildQuery();
    }

    /**
     * Implements a query that will fetch all features from a source. This
     * query should retrieve all properties, with no maxFeatures, no
     * filtering, and the default featureType.
     */
    public static Query all(final GenericName name){
        return new Query(name.toString());
    }

    /**
     * Implements a query that will fetch all features from a source. This
     * query should retrieve all properties, with no maxFeatures, no
     * filtering, and the default featureType.
     */
    public static Query all(final String name){
        return new Query(name);
    }

    /**
     * Implements a query that will fetch all the FeatureIDs from a source.
     * This query should retrive no properties, with no maxFeatures, no
     * filtering, and the a featureType with no attributes.
     */
    public static Query fids(final String name){
        return new Query(name, ONLY_ID_PROPERTIES);
    }

    /**
     * Create a simple query with only a reproject crs.
     *
     * @param name
     * @param filter
     * @return Immutable query
     */
    public static Query reprojected(final String name, final CoordinateReferenceSystem crs){
        final QueryBuilder builder = new QueryBuilder();
        builder.setTypeName(name);
        builder.setCRS(crs);
        return builder.buildQuery();
    }

    /**
     *
     * @param sortBy array or null
     * @return true is the given array of sort by operand is equal to natural sort by
     */
    public static boolean isNaturalSortBy(SortBy[] sortBy){
        if(sortBy == null || sortBy.length == 0){
            return true;
        }

        for(SortBy sb : sortBy){
            if(sb != SortBy.NATURAL_ORDER){
                return false;
            }
        }

        return true;
    }

}
