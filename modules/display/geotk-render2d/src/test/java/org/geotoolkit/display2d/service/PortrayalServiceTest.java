/*
 *    Geotoolkit - An Open Source Java GIS Toolkit
 *    http://www.geotoolkit.org
 *
 *    (C) 2010-2014, Geomatys
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
package org.geotoolkit.display2d.service;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import java.awt.image.WritableRaster;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import javax.measure.Unit;
import org.apache.sis.coverage.SampleDimension;
import org.apache.sis.coverage.grid.GridCoverage;
import org.apache.sis.coverage.grid.GridCoverage2D;
import org.apache.sis.coverage.grid.GridCoverageBuilder;
import org.apache.sis.coverage.grid.GridExtent;
import org.apache.sis.coverage.grid.GridGeometry;
import org.apache.sis.feature.builder.AttributeRole;
import org.apache.sis.feature.builder.FeatureTypeBuilder;
import org.apache.sis.geometry.GeneralEnvelope;
import org.apache.sis.internal.referencing.j2d.AffineTransform2D;
import org.apache.sis.internal.system.DefaultFactories;
import org.apache.sis.measure.Units;
import org.apache.sis.referencing.CRS;
import org.apache.sis.referencing.CommonCRS;
import org.apache.sis.referencing.crs.AbstractCRS;
import org.apache.sis.referencing.cs.AxesConvention;
import org.apache.sis.storage.DataStoreException;
import org.apache.sis.storage.FeatureSet;
import org.apache.sis.storage.GridCoverageResource;
import org.apache.sis.storage.WritableFeatureSet;
import org.geotoolkit.display.PortrayalException;
import org.geotoolkit.display.SearchArea;
import org.geotoolkit.display.canvas.RenderingContext;
import org.geotoolkit.display.canvas.control.StopOnErrorMonitor;
import org.geotoolkit.display2d.GO2Hints;
import org.geotoolkit.display2d.GraphicVisitor;
import org.geotoolkit.display2d.canvas.J2DCanvas;
import org.geotoolkit.display2d.canvas.J2DCanvasBuffered;
import org.geotoolkit.factory.Hints;
import org.geotoolkit.geometry.jts.JTS;
import org.geotoolkit.map.MapBuilder;
import org.geotoolkit.map.MapContext;
import org.geotoolkit.map.MapLayer;
import org.geotoolkit.storage.memory.InMemoryFeatureSet;
import org.geotoolkit.storage.memory.InMemoryGridCoverageResource;
import org.geotoolkit.style.DefaultStyleFactory;
import org.geotoolkit.style.MutableStyle;
import org.geotoolkit.style.MutableStyleFactory;
import org.geotoolkit.style.StyleConstants;
import org.junit.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.opengis.feature.Feature;
import org.opengis.feature.FeatureType;
import org.opengis.filter.FilterFactory;
import org.opengis.filter.expression.Expression;
import org.opengis.geometry.Envelope;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.datum.PixelInCell;
import org.opengis.style.*;
import org.opengis.util.FactoryException;

import static java.lang.Double.NEGATIVE_INFINITY;
import static java.lang.Double.POSITIVE_INFINITY;
import static org.geotoolkit.style.StyleConstants.DEFAULT_ANCHOR_POINT;
import static org.geotoolkit.style.StyleConstants.DEFAULT_DESCRIPTION;
import static org.geotoolkit.style.StyleConstants.DEFAULT_DISPLACEMENT;
import static org.geotoolkit.style.StyleConstants.LITERAL_ONE_FLOAT;
import static org.geotoolkit.style.StyleConstants.MARK_CIRCLE;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 * Testing portrayal service.
 *
 * @author Johann Sorel (Geomatys)
 */
public class PortrayalServiceTest extends org.geotoolkit.test.TestBase {
    private static final FilterFactory FF = DefaultFactories.forBuildin(FilterFactory.class);
    private static final GeometryFactory GF = new GeometryFactory();
    private static final MutableStyleFactory SF = new DefaultStyleFactory();

    private final List<FeatureSet> featureColls = new ArrayList<>();
    private final List<GridCoverage> coverages = new ArrayList<>();
    private final List<Envelope> envelopes = new ArrayList<>();
    private final List<double[]> timestamps = new ArrayList<>();
    private final List<double[]> elevations = new ArrayList<>();

    public PortrayalServiceTest() throws Exception {

        // create the feature collection for tests -----------------------------
        final FeatureTypeBuilder sftb = new FeatureTypeBuilder();
        sftb.setName("test");
        sftb.addAttribute(Point.class).setName("geom").setCRS(CommonCRS.WGS84.normalizedGeographic()).addRole(AttributeRole.DEFAULT_GEOMETRY);
        sftb.addAttribute(String.class).setName("att1");
        sftb.addAttribute(Double.class).setName("att2");
        final FeatureType sft = sftb.build();

        WritableFeatureSet col = new InMemoryFeatureSet("id", sft);

        Feature sf1 = sft.newInstance();
        sf1.setPropertyValue("geom", GF.createPoint(new Coordinate(0, 0)));
        sf1.setPropertyValue("att1", "value1");
        Feature sf2 = sft.newInstance();
        sf2.setPropertyValue("geom", GF.createPoint(new Coordinate(-180, -90)));
        sf2.setPropertyValue("att1", "value1");
        Feature sf3 = sft.newInstance();
        sf3.setPropertyValue("geom", GF.createPoint(new Coordinate(-180, 90)));
        sf3.setPropertyValue("att1", "value1");
        Feature sf4 = sft.newInstance();
        sf4.setPropertyValue("geom", GF.createPoint(new Coordinate(180, -90)));
        sf4.setPropertyValue("att1", "value1");
        Feature sf5 = sft.newInstance();
        sf5.setPropertyValue("geom", GF.createPoint(new Coordinate(180, -90)));
        sf5.setPropertyValue("att1", "value1");

        col.add(Arrays.asList(sf1, sf2, sf3, sf4, sf5).iterator());

        featureColls.add(col);


        //create a serie of envelopes for tests --------------------------------
        GeneralEnvelope env = new GeneralEnvelope(CommonCRS.WGS84.geographic());
        env.setRange(0, -90, 90);
        env.setRange(1, -180, 180);
        envelopes.add(env);
        env = new GeneralEnvelope(CommonCRS.WGS84.geographic());
        env.setRange(0, -12, 31);
        env.setRange(1, -5, 46);
        envelopes.add(env);
        env = new GeneralEnvelope(CommonCRS.WGS84.normalizedGeographic());
        env.setRange(0, -180, 180);
        env.setRange(1, -90, 90);
        envelopes.add(env);
        env = new GeneralEnvelope(CommonCRS.WGS84.normalizedGeographic());
        env.setRange(0, -5, 46);
        env.setRange(1, -12, 31);
        envelopes.add(env);
        env = new GeneralEnvelope(CRS.forCode("EPSG:3395"));
        env.setRange(0, -1200000, 3100000);
        env.setRange(1, -500000, 4600000);
        envelopes.add(env);

        //create a serie of date ranges ----------------------------------------
        timestamps.add(new double[]{             1000, 15000});
        timestamps.add(new double[]{NEGATIVE_INFINITY, 15000});
        timestamps.add(new double[]{             1000, POSITIVE_INFINITY});
        timestamps.add(new double[]{NEGATIVE_INFINITY, POSITIVE_INFINITY});

        //create a serie of elevation ranges -----------------------------------
        elevations.add(new double[]{             -15d, 50d});
        elevations.add(new double[]{NEGATIVE_INFINITY, 50d});
        elevations.add(new double[]{             -15d, POSITIVE_INFINITY});
        elevations.add(new double[]{NEGATIVE_INFINITY, POSITIVE_INFINITY});


        //create some coverages ------------------------------------------------

        env = new GeneralEnvelope(CRS.forCode("EPSG:32738"));
        env.setRange(0,  695035,  795035);
        env.setRange(1, 7545535, 7645535);
        BufferedImage img = new BufferedImage(100, 100, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setColor(Color.RED);
        g.fill(new Rectangle(0, 0, 100, 100));
        GridCoverageBuilder GCF = new GridCoverageBuilder();
        GCF.setDomain(env);
        GCF.setValues(img);
        GridCoverage coverage = GCF.build();
        coverages.add(coverage);

        env = new GeneralEnvelope(CommonCRS.WGS84.geographic());
        env.setRange(0,  -10,  25);
        env.setRange(1, -56, -21);
        img = new BufferedImage(100, 100, BufferedImage.TYPE_INT_ARGB);
        g = img.createGraphics();
        g.setColor(Color.RED);
        g.fill(new Rectangle(0, 0, 100, 100));
        GCF = new GridCoverageBuilder();
        GCF.setDomain(env);
        GCF.setValues(img);
        coverage = GCF.build();
        coverages.add(coverage);

    }

    /**
     * Test the grid geometry we provided is used.
     */
    public void testGridGeometryPreserved() throws PortrayalException, FactoryException {

        final SceneDef sceneDef = new SceneDef();
        final GridGeometry gridGeometry = new GridGeometry(new GridExtent(360, 180), CRS.getDomainOfValidity(CommonCRS.WGS84.normalizedGeographic()));
        final CanvasDef canvasDef = new CanvasDef(gridGeometry);
        final J2DCanvas canvas = new J2DCanvasBuffered(CommonCRS.WGS84.geographic(), new Dimension(10, 10));
        DefaultPortrayalService.prepareCanvas(canvas, canvasDef, sceneDef);

        assertEquals(gridGeometry, canvas.getGridGeometry());
        assertEquals(gridGeometry, canvas.getGridGeometry2D());

    }

    @Test
    public void testEnvelopeNotNull() throws FactoryException, PortrayalException {
        MapContext context = MapBuilder.createContext(CommonCRS.WGS84.geographic());
        GeneralEnvelope env = new GeneralEnvelope(CommonCRS.WGS84.geographic());
        env.setRange(0, -180, 180);
        env.setRange(1, -90, 90);

        DefaultPortrayalService.portray(
                new CanvasDef(new Dimension(800, 600), env),
                new SceneDef(context));



        //CRS can not obtain envelope for this projection. we check that we don't reaise any error.
        context = MapBuilder.createContext(CommonCRS.defaultGeographic());
        env = new GeneralEnvelope(CommonCRS.defaultGeographic());
        env.setRange(0, -180, 180);
        env.setRange(1, -90, 90);

        DefaultPortrayalService.portray(
                new CanvasDef(new Dimension(800, 600), env),
                new SceneDef(context));
    }

    @Test
    public void testFeatureRendering() throws Exception {
        for(FeatureSet col : featureColls){
            final MapLayer layer = MapBuilder.createFeatureLayer(col, SF.style(SF.pointSymbolizer()));
            testRendering(layer);
        }
    }

    /**
     * Test rendering of a coverage inside a feature property.
     */
    @Test
    public void testCoveragePropertyRendering() throws Exception {
        final FeatureTypeBuilder ftb = new FeatureTypeBuilder();
        ftb.setName("test");
        ftb.addAttribute(GridCoverage.class).setName("coverage");
        final FeatureType ft = ftb.build();

        final BufferedImage img = new BufferedImage(90, 90, BufferedImage.TYPE_INT_ARGB);
        final Graphics2D g = img.createGraphics();
        g.setColor(Color.GREEN);
        g.fillRect(0, 0, 90, 90);
        g.dispose();

        final GridCoverageBuilder gcb = new GridCoverageBuilder();
        gcb.setDomain(new GridGeometry(null, PixelInCell.CELL_CENTER, new AffineTransform2D(1, 0, 0, 1, 0.5, 0.5), CommonCRS.WGS84.normalizedGeographic()));
        gcb.setValues(img);

        final Feature f = ft.newInstance();
        f.setPropertyValue("coverage",gcb.build());
        final FeatureSet collection = new InMemoryFeatureSet(ft, Arrays.asList(f));


        final String name = "mySymbol";
        final Description desc = DEFAULT_DESCRIPTION;
        final String geometry = "coverage";
        final Unit unit = Units.POINT;
        final Expression opacity = LITERAL_ONE_FLOAT;
        final ChannelSelection channels = null;
        final OverlapBehavior overlap = null;
        final ColorMap colormap = null;
        final ContrastEnhancement enhance = null;
        final ShadedRelief relief = null;
        final Symbolizer outline = null;

        final RasterSymbolizer symbol = SF.rasterSymbolizer(
                name,geometry,desc,unit,opacity,
                channels,overlap,colormap,enhance,relief,outline);
        final MutableStyle style = SF.style(symbol);

        final MapLayer layer = MapBuilder.createFeatureLayer(collection, style);
        final MapContext context = MapBuilder.createContext();
        context.layers().add(layer);

        final SceneDef sdef = new SceneDef(context);
        final GeneralEnvelope env = new GeneralEnvelope(CommonCRS.WGS84.normalizedGeographic());
        env.setRange(0, -180, +180);
        env.setRange(1, -90, +90);
        final CanvasDef cdef = new CanvasDef(new Dimension(360, 180), env);

        final BufferedImage result = DefaultPortrayalService.portray(cdef, sdef);
        final Raster raster = result.getData();
        final int[] pixel = new int[4];
        final int[] trans = new int[]{0,0,0,0};
        final int[] green = new int[]{0,255,0,255};
        assertNotNull(result);
        raster.getPixel(0, 0, pixel);
        assertArrayEquals(trans, pixel);
        raster.getPixel(179, 45, pixel);
        assertArrayEquals(trans, pixel);
        raster.getPixel(181, 45, pixel);
        assertArrayEquals(green, pixel);
    }

    @Test
    public void testCoverageRendering() throws Exception{
        for(GridCoverage col : coverages){
            final MapLayer layer = MapBuilder.createCoverageLayer(col, SF.style(SF.rasterSymbolizer()), "cov");
            testRendering(layer);
        }
    }

//    @Test
//    public void testCoverageNDRendering() throws Exception{
//        //todo
//    }

    /**
     * Test rendering a coverage which envelope is larger then the objective CRS
     * validity area.
     */
    @Test
    public void testCoverageOutofValidityArea() throws FactoryException, PortrayalException {

        final double scale = 360.0 / 4320.0;
        final AffineTransform2D gridToCRS = new AffineTransform2D(scale, 0, 0, -scale, -180, 90);
        final BufferedImage img = new BufferedImage(4320, 2160, BufferedImage.TYPE_INT_ARGB);
        final Graphics2D g = img.createGraphics();
        g.setColor(Color.BLUE);
        g.fillRect(0, 0, 4320, 2160);
        g.dispose();

        final GridCoverageBuilder gcb = new GridCoverageBuilder();
        gcb.setDomain(new GridGeometry(null, PixelInCell.CELL_CORNER, gridToCRS, CommonCRS.WGS84.normalizedGeographic()));
        gcb.setValues(img);
        final GridCoverage coverage = gcb.build();

        final MapContext context = MapBuilder.createContext();
        final MapLayer layer = MapBuilder.createCoverageLayer(coverage);
        context.layers().add(layer);

        final GeneralEnvelope env = new GeneralEnvelope(CRS.forCode("EPSG:3857"));
        env.setRange(0, -20037508.342789244, 20037508.342789244);
        env.setRange(1, -20037508.342789244, 20037508.342789244);

        final SceneDef scene = new SceneDef(context);
        final CanvasDef canvas = new CanvasDef(new Dimension(256, 256), env);

        final BufferedImage result = DefaultPortrayalService.portray(canvas, scene);
        final int color = Color.BLUE.getRGB();
        for (int y=0;y<256;y++) {
            for (int x=0;x<256;x++) {
                assertEquals(color, result.getRGB(x, y));
            }
        }
    }

    @Test
    public void testLongitudeFirst() throws Exception {

        final int[] pixel = new int[4];
        final int[] red = new int[]{255,0,0,255};
        final int[] white = new int[]{255,255,255,255};

        final Hints hints = new Hints();
        hints.put(GO2Hints.KEY_COLOR_MODEL, ColorModel.getRGBdefault());

        //create a map context with a layer that will cover the entire area we will ask for
        final GridCoverage coverage;
        {
            final GeneralEnvelope covenv = new GeneralEnvelope(CommonCRS.WGS84.normalizedGeographic());
            covenv.setRange(0, -180, 180);
            covenv.setRange(1, -90, 90);
            final BufferedImage img = new BufferedImage(360, 180, BufferedImage.TYPE_INT_ARGB);
            final Graphics2D g = img.createGraphics();
            g.setColor(Color.RED);
            g.fill(new Rectangle(0, 0, 360, 180));
            final GridGeometry grid = new GridGeometry(new GridExtent(360, 180), covenv);
            final List<SampleDimension> bands = new ArrayList<>();
            bands.add(new SampleDimension.Builder().setName("r").build());
            bands.add(new SampleDimension.Builder().setName("g").build());
            bands.add(new SampleDimension.Builder().setName("b").build());
            bands.add(new SampleDimension.Builder().setName("a").build());
            coverage = new GridCoverage2D(grid, bands, img);
        }

        final MapLayer layer = MapBuilder.createCoverageLayer(coverage, SF.style(SF.rasterSymbolizer()), "unnamed");
        final MapContext context = MapBuilder.createContext();
        context.layers().add(layer);


        //sanity test, image should be a red vertical band in the middle
        final CoordinateReferenceSystem epsg4326 = CommonCRS.WGS84.geographic();
        GeneralEnvelope env = new GeneralEnvelope(epsg4326);
        env.setRange(0, -180, 180);
        env.setRange(1, -180, 180);

        CanvasDef cdef = new CanvasDef(new Dimension(360, 360), env);
        cdef.setBackground(Color.WHITE);
        BufferedImage buffer = DefaultPortrayalService.portray(cdef, new SceneDef(context, hints));
        assertEquals(360,buffer.getWidth());
        assertEquals(360,buffer.getHeight());

        WritableRaster raster = buffer.getRaster();
        raster.getPixel(0, 0, pixel);       assertArrayEquals(white, pixel);
        raster.getPixel(359, 0, pixel);     assertArrayEquals(white, pixel);
        raster.getPixel(359, 359, pixel);   assertArrayEquals(white, pixel);
        raster.getPixel(0, 359, pixel);     assertArrayEquals(white, pixel);
        raster.getPixel(180, 0, pixel);     assertArrayEquals(red, pixel);
        raster.getPixel(180, 359, pixel);   assertArrayEquals(red, pixel);
        raster.getPixel(0, 180, pixel);     assertArrayEquals(white, pixel);
        raster.getPixel(359, 180, pixel);   assertArrayEquals(white, pixel);



        //east=horizontal test, image should be a red horizontal band in the middle
        cdef = new CanvasDef(new Dimension(360, 360), env);
        cdef.setBackground(Color.WHITE);
        cdef.setLongitudeFirst();
        buffer = DefaultPortrayalService.portray(cdef, new SceneDef(context, hints));
        //ImageIO.write(buffer, "png", new File("flip.png"));
        assertEquals(360,buffer.getWidth());
        assertEquals(360,buffer.getHeight());

        raster = buffer.getRaster();
        raster.getPixel(0, 0, pixel);       assertArrayEquals(white, pixel);
        raster.getPixel(359, 0, pixel);     assertArrayEquals(white, pixel);
        raster.getPixel(359, 359, pixel);   assertArrayEquals(white, pixel);
        raster.getPixel(0, 359, pixel);     assertArrayEquals(white, pixel);
        raster.getPixel(180, 0, pixel);     assertArrayEquals(white, pixel);
        raster.getPixel(180, 359, pixel);   assertArrayEquals(white, pixel);
        raster.getPixel(0, 180, pixel);     assertArrayEquals(red, pixel);
        raster.getPixel(359, 180, pixel);   assertArrayEquals(red, pixel);
    }

    /**
     * Test the CoverageReader view of a scene.
     */
    @Test
    public void testPortrayalCoverageResource() throws DataStoreException {

        //create a test coverage
        final BufferedImage img = new BufferedImage(360, 180, BufferedImage.TYPE_INT_ARGB);
        final Graphics2D g2d = img.createGraphics();
        g2d.setColor(Color.GREEN);
        g2d.fillRect(0, 0, 360, 180);
        final GeneralEnvelope env = new GeneralEnvelope(CommonCRS.WGS84.normalizedGeographic());
        env.setRange(0, -180, 180);
        env.setRange(1, -90, 90);
        final GridCoverageBuilder gcb = new GridCoverageBuilder();
        gcb.setDomain(env);
        gcb.setValues(img);
        final GridCoverage coverage = gcb.build();

        //display it
        final MapContext context = MapBuilder.createContext();
        final MapLayer cl = MapBuilder.createCoverageLayer(
                coverage, SF.style(StyleConstants.DEFAULT_RASTER_SYMBOLIZER), "coverage");
        context.layers().add(cl);

        final SceneDef sceneDef = new SceneDef(context);
        final GridCoverageResource resource = DefaultPortrayalService.asResource(sceneDef);

        final GridGeometry gridGeom = resource.getGridGeometry();
        assertNotNull(gridGeom);

        final GridCoverage result = resource.read(null);
        final RenderedImage image = result.render(null);
        assertEquals(1000, image.getWidth());
    }

    /**
     * Test that a large graphic outside the map area is still rendered.
     */
    @Test
    public void testMarginRendering() throws Exception {
        final List<GraphicalSymbol> symbols = new ArrayList<>();
        final Stroke stroke = SF.stroke(Color.BLACK, 0);
        final Fill fill = SF.fill(Color.BLACK);
        final Mark mark = SF.mark(MARK_CIRCLE, fill, stroke);
        symbols.add(mark);
        final Graphic graphic = SF.graphic(symbols, LITERAL_ONE_FLOAT, FF.literal(8), LITERAL_ONE_FLOAT, DEFAULT_ANCHOR_POINT, DEFAULT_DISPLACEMENT);
        final PointSymbolizer symbolizer = SF.pointSymbolizer("mySymbol",(String)null,DEFAULT_DESCRIPTION, Units.POINT, graphic);

        final CoordinateReferenceSystem crs = CommonCRS.WGS84.normalizedGeographic();


        final FeatureTypeBuilder ftb = new FeatureTypeBuilder();
        ftb.setName("test");
        ftb.addAttribute(Point.class).setName("geom").setCRS(crs).addRole(AttributeRole.DEFAULT_GEOMETRY);
        final FeatureType ft = ftb.build();
        final Feature feature = ft.newInstance();
        final Point pt = GF.createPoint(new Coordinate(12, 5));
        JTS.setCRS(pt, crs);
        feature.setPropertyValue("geom", pt);

        final FeatureSet col = new InMemoryFeatureSet(ft, Arrays.asList(feature));
        final MapLayer layer = MapBuilder.createFeatureLayer(col,SF.style(symbolizer));
        final MapContext context = MapBuilder.createContext();
        context.layers().add(layer);

        final GeneralEnvelope env = new GeneralEnvelope(crs);
        env.setRange(0, 0, 10);
        env.setRange(1, 0, 10);

        final CanvasDef cdef = new CanvasDef(new Dimension(10, 10), env);
        cdef.setBackground(Color.WHITE);
        final SceneDef sdef = new SceneDef(context);

        final BufferedImage img = DefaultPortrayalService.portray(cdef, sdef);

        assertEquals(Color.BLACK.getRGB(), img.getRGB(9, 5));
        assertEquals(Color.BLACK.getRGB(), img.getRGB(8, 5));
        assertEquals(Color.WHITE.getRGB(), img.getRGB(7, 5));
    }

    /**
     * Test picking on a coverage in range 0-360.
     * @throws PortrayalException
     */
    @Test
    public void testCoverageVisit0_360() throws PortrayalException {

        // Create 0-360 coverage
        final BufferedImage img = new BufferedImage(350, 180, BufferedImage.TYPE_INT_ARGB);
        CoordinateReferenceSystem crs = CommonCRS.WGS84.normalizedGeographic();
        crs = AbstractCRS.castOrCopy(crs).forConvention(AxesConvention.POSITIVE_RANGE);
        final AffineTransform2D gridToCrs = new AffineTransform2D(1, 0, 0, -1, 0, 90);
        final GridExtent extent = new GridExtent(350, 180);
        final GridGeometry gg = new GridGeometry(extent, PixelInCell.CELL_CORNER, gridToCrs, crs);
        final GridCoverageBuilder gcb = new GridCoverageBuilder();
        gcb.setValues(img);
        gcb.setDomain(gg);
        final GridCoverage coverage = gcb.build();

        final GridCoverageResource gcr = new InMemoryGridCoverageResource(coverage);
        final MapLayer layer = MapBuilder.createCoverageLayer(gcr);
        layer.setSelectable(true);

        final MapContext context = MapBuilder.createContext();
        context.layers().add(layer);


        final GeneralEnvelope viewEnv = new GeneralEnvelope(CommonCRS.WGS84.normalizedGeographic());
        viewEnv.setRange(0, -180, +180);
        viewEnv.setRange(1, -90, +90);

        final AtomicInteger count = new AtomicInteger();

        GraphicVisitor gv = new GraphicVisitor() {
            @Override
            public void startVisit() {
            }

            @Override
            public void endVisit() {
            }

            @Override
            public void visit(org.opengis.display.primitive.Graphic graphic, RenderingContext context, SearchArea area) {
                count.incrementAndGet();
            }

            @Override
            public boolean isStopRequested() {
                return false;
            }
        };


        final SceneDef scene = new SceneDef(context);
        final CanvasDef canvas = new CanvasDef(new Dimension(360, 180), viewEnv);
        final VisitDef visit = new VisitDef(new Rectangle(10, 80, 2, 2), gv);

        DefaultPortrayalService.visit(canvas, scene, visit);

        assertEquals(1, count.get());
    }

    private void testRendering(final MapLayer layer) throws Exception {
        final StopOnErrorMonitor monitor = new StopOnErrorMonitor();

        final MapContext context = MapBuilder.createContext(CommonCRS.WGS84.normalizedGeographic());
        context.layers().add(layer);
        assertEquals(1, context.layers().size());

        for(final Envelope env : envelopes){
            for(double[] drange : timestamps){
                for(double[] erange : elevations){
                    final GeneralEnvelope cenv = new GeneralEnvelope(CRS.compound(
                            env.getCoordinateReferenceSystem(), CommonCRS.Vertical.ELLIPSOIDAL.crs(), CommonCRS.Temporal.JAVA.crs()
                    ));
                    cenv.subEnvelope(0, 2).setEnvelope(env);
                    cenv.setRange(2, erange[0], erange[1]);
                    cenv.setRange(3, drange[0], drange[1]);
                    final CanvasDef cdef = new CanvasDef(new Dimension(800,600), cenv);
                    cdef.setAzimuth(0);
                    cdef.setMonitor(monitor);
                    final BufferedImage img = DefaultPortrayalService.portray(
                        cdef, new SceneDef(context));
                    assertNull(monitor.getLastException());
                    assertNotNull(img);
                }
            }
        }
    }
}
