/*
 *    Geotoolkit - An Open Source Java GIS Toolkit
 *    http://www.geotoolkit.org
 *
 *    (C) 2010-2015, Geomatys
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

package org.geotoolkit.feature.op;

import java.util.Set;
import org.apache.sis.feature.builder.FeatureTypeBuilder;
import org.apache.sis.internal.system.DefaultFactories;
import org.geotoolkit.util.NamesExt;
import org.junit.AfterClass;
import org.junit.Assert;
import static org.junit.Assert.assertEquals;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opengis.feature.Feature;
import org.opengis.feature.FeatureType;
import org.opengis.filter.FilterFactory;
import org.opengis.filter.expression.Expression;

/**
 * Test calculated expression attribut.
 *
 * @author Johann Sorel (Geomatys)
 * @module
 */
public class ExpressionOperationTest {

    private final FilterFactory FF = DefaultFactories.forBuildin(FilterFactory.class);

    public ExpressionOperationTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Test
    public void testGetValue() {
        final Expression exp = FF.add(FF.property("att1"), FF.property("att2"));

        final FeatureTypeBuilder ftb = new FeatureTypeBuilder();
        ftb.setName("test");
        ftb.addAttribute(Integer.class).setName("att1");
        ftb.addAttribute(Integer.class).setName("att2");
        final ExpressionOperation operation = new ExpressionOperation(NamesExt.create(null, "calc"), exp);
        final Set<String> dependencies = operation.getDependencies();
        assertEquals(2, dependencies.size());
        Assert.assertTrue(dependencies.contains("att1"));
        Assert.assertTrue(dependencies.contains("att2"));
        ftb.addProperty(operation);
        final FeatureType sft = ftb.build();

        final Feature sf = sft.newInstance();
        sf.setPropertyValue("att1", 45);
        sf.setPropertyValue("att2", 12);

        assertEquals(57.0, (Double)sf.getPropertyValue("calc"), 0.0);
    }


}
