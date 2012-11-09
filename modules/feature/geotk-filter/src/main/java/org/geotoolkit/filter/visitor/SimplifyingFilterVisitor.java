/*
 *    Geotoolkit - An Open Source Java GIS Toolkit
 *    http://www.geotoolkit.org
 * 
 *    (C) 2004-2008, Open Source Geospatial Foundation (OSGeo)
 *    (C) 2009, Geomatys
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
package org.geotoolkit.filter.visitor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import org.opengis.filter.And;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterVisitor;
import org.opengis.filter.Id;
import org.opengis.filter.Or;
import org.opengis.filter.PropertyIsEqualTo;
import org.opengis.filter.expression.Literal;
import org.opengis.filter.identity.FeatureId;
import org.opengis.filter.identity.GmlObjectId;
import org.opengis.filter.identity.Identifier;

/**
 * Takes a filter and returns a simplified, equivalent one. At the moment the filter simplifies out
 * {@link Filter#INCLUDE} and {@link Filter#EXCLUDE} and deal with FID filter validation.
 * <p>
 * FID filter validation is meant to wipe out non valid feature ids from {@link Id} filters. This is
 * so in order to avoid sending feature ids down to DataStores that are not valid as per the
 * specific FeatureType fid structure. Since this is structure is usually DataStore specific, some
 * times being a strategy based on how the feature type primary key is generated, fid validation is
 * abstracted out to the {@link FIDValidator} interface so when a DataStore is about to send a query
 * down to the backend it van provide this visitor with a validator specific for the feature type
 * fid structure being queried.
 * </p>
 * <p>
 * By default all feature ids are valid. DataStores that want non valid fids to be wiped out should
 * set a {@link FIDValidator} through the {@link #setFIDValidator(FIDValidator)} method.
 * </p>
 * 
 * @author Andrea Aime - OpenGeo
 * @author Gabriel Roldan (OpenGeo)
 * @module pending
 * @since 2.5.x
 * @version $Id$
 */
public class SimplifyingFilterVisitor extends DuplicatingFilterVisitor {

    /**
     * Defines a simple means of assessing whether a feature id in an {@link Id} filter is
     * structurally valid and hence can be send down to the backend with confidence it will not
     * cause trouble, the most common one being filtering by pk number even if the type name prefix
     * does not match.
     */
    public static interface FIDValidator {
        public boolean isValid(String fid);
    }

    /**
     * A 'null-object' fid validator that assumes any feature id in an {@link Id} filter is valid
     */
    public static final FIDValidator ANY_FID_VALID = new FIDValidator() {
        @Override
        public boolean isValid(String fid) {
            return true;
        }
    };

    /**
     * A FID validator that matches the fids with a given regular expression to determine the fid's
     * validity.
     * 
     * @author Gabriel Roldan (OpenGeo)
     */
    public static class RegExFIDValidator implements FIDValidator {

        private Pattern pattern;

        /**
         * @param regularExpression
         *            a regular expression as used by the {@code java.util.regex} package
         */
        public RegExFIDValidator(final String regularExpression) {
            pattern = Pattern.compile(regularExpression);
        }

        @Override
        public boolean isValid(final String fid) {
            return pattern.matcher(fid).matches();
        }
    }

    /**
     * A convenient fid validator for the common case of a feature id being a composition of a
     * {@code <typename>.<number>}
     */
    public static class TypeNameDotNumberFidValidator extends RegExFIDValidator {
        /**
         * @param typeName
         *            the typename that will be used for a regular expression match in the form of
         *            {@code <typename>.<number>}
         */
        public TypeNameDotNumberFidValidator(final String typeName) {
            super(typeName + "\\.\\d+");
        }
    }

    private FIDValidator fidValidator = ANY_FID_VALID;

    public void setFIDValidator(final FIDValidator validator) {
        this.fidValidator = validator == null ? ANY_FID_VALID : validator;
    }

    @Override
    public Object visit(final And filter, final Object extraData) {
        // scan, clone and simplify the children
        final List<Filter> children = filter.getChildren();
        final List<Filter> newChildren = new ArrayList<Filter>(children.size());

        for (Filter child : children) {
            final Filter cloned = (Filter) child.accept(this, extraData);

            // if any of the child filters is exclude, 
            // the whole chain of AND is equivalent to EXCLUDE
            if(cloned == Filter.EXCLUDE)
                return Filter.EXCLUDE;
            
            // these can be skipped
            if(cloned == Filter.INCLUDE)
                continue;

            newChildren.add(cloned);
        }
        
        // we might end up with an empty list
        if(newChildren.size() == 0)
            return Filter.INCLUDE;
        
        // remove the logic we have only one filter
        if(newChildren.size() == 1)
            return newChildren.get(0);
        
        // else return the cloned and simplified up list
        return getFactory(extraData).and(newChildren);
    }
    
    @Override
    public Object visit(final Or filter, final Object extraData) {
     // scan, clone and simplify the children
        final List<Filter> children = filter.getChildren();
        final List<Filter> newChildren = new ArrayList<Filter>(children.size());

        Set<Identifier> mergedIds = null;
        Id regroupedIds = null;
        
        for (Filter child : children) {
            final Filter cloned = (Filter) child.accept(this, extraData);

            // if any of the child filters is include, 
            // the whole chain of OR is equivalent to INCLUDE
            if(cloned == Filter.INCLUDE)
                return Filter.INCLUDE;
            
            // these can be skipped
            if(cloned == Filter.EXCLUDE)
                continue;
            
            if(cloned instanceof Id){
                //merge id filters
                if(regroupedIds == null && mergedIds == null){
                    regroupedIds = (Id) cloned;
                }else{
                    if(mergedIds == null){
                        mergedIds = new HashSet<Identifier>(regroupedIds.getIdentifiers());
                    }
                    regroupedIds = null;
                    mergedIds.addAll( ((Id)cloned).getIdentifiers() );
                }
            }else{
                newChildren.add(cloned);
            }
        }
        
        if(regroupedIds != null){
            newChildren.add(regroupedIds);
        }else if(mergedIds != null){
            newChildren.add(ff.id(mergedIds));
        }
        
        
        // we might end up with an empty list
        if(newChildren.size() == 0)
            return Filter.EXCLUDE;
        
        // remove the logic we have only one filter
        if(newChildren.size() == 1)
            return newChildren.get(0);
        
        // else return the cloned and simplified up list
        return getFactory(extraData).or(newChildren);
    }
    
    /**
     * Uses the current {@link FIDValidator} to wipe out illegal feature ids from the returned
     * filters.
     * 
     * @return a filter containing only valid fids as per the current {@link FIDValidator}, may be
     *         {@link Filter#EXCLUDE} if none matches or the filter is already empty 
     */
    @Override
    public Object visit(final Id filter, final Object extraData) {
        // if the set of ID is empty, it's actually equivalent to Filter.EXCLUDE
        if (filter.getIDs().size() == 0) {
            return Filter.EXCLUDE;
        }

        final Set<Identifier> validFids = new HashSet<Identifier>();

        for (Identifier id : filter.getIdentifiers()) {
            if(id instanceof FeatureId || id instanceof GmlObjectId){
                // both FeatureId an GmlObjectId.getID() return String, but Identifier.getID()
                // returns Object. Yet, FeatureId and GmlObjectId are the only known subclasses of
                // Identifier that apply to Feature land
                if (fidValidator.isValid((String)id.getID())) {
                    validFids.add(id);
                }
            }
        }

        if (validFids.size() == 0) {
            return Filter.EXCLUDE;
        } else {
            return getFactory(extraData).id(validFids);
        }
    }

    @Override
    public Object visit(PropertyIsEqualTo filter, Object extraData) {
        if(   filter.getExpression1() instanceof Literal 
           && filter.getExpression2() instanceof Literal){
            //we can preevaluate this one
            return (filter.evaluate(null)) ? Filter.INCLUDE : Filter.EXCLUDE;
        }else{
            return super.visit(filter, extraData);
        }
    }
    
}
