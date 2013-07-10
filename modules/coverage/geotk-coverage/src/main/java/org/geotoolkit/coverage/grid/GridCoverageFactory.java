/*
 *    Geotoolkit.org - An Open Source Java GIS Toolkit
 *    http://www.geotoolkit.org
 *
 *    (C) 2005-2012, Open Source Geospatial Foundation (OSGeo)
 *    (C) 2009-2012, Geomatys
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
package org.geotoolkit.coverage.grid;

import java.util.Map;
import java.util.Arrays;
import java.util.Objects;
import java.awt.Color;
import java.awt.RenderingHints;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.awt.image.renderable.ParameterBlock;
import java.awt.geom.AffineTransform;
import javax.media.jai.JAI;
import javax.media.jai.PlanarImage;
import javax.media.jai.ImageFunction;
import javax.media.jai.RasterFactory;
import javax.media.jai.util.CaselessStringKey;
import javax.measure.unit.Unit;
import net.jcip.annotations.ThreadSafe;

import org.opengis.coverage.SampleDimensionType;
import org.opengis.coverage.grid.GridCoverage;
import org.opengis.coverage.grid.GridEnvelope;
import org.opengis.referencing.cs.AxisDirection;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.geometry.Envelope;

import org.geotoolkit.factory.Hints;
import org.geotoolkit.factory.Factory;
import org.geotoolkit.internal.FactoryUtilities;
import org.apache.sis.geometry.GeneralEnvelope;
import org.geotoolkit.coverage.GridSampleDimension;
import org.geotoolkit.referencing.crs.DefaultGeographicCRS;
import org.geotoolkit.resources.Errors;


/**
 * A factory for {@linkplain GridCoverage2D grid coverage} objects. This factory expects various
 * combinations of the following informations:
 * <p>
 * <ul>
 *   <li>A name as a {@linkplain CharSequence character sequence}.</li>
 *
 *   <li>A {@linkplain WritableRaster raster}, <strong>or</strong> an {@linkplain RenderedImage
 *       image}, <strong>or</strong> an {@linkplain ImageFunction image function}, <strong>or</strong>
 *       a matrix of kind {@code float[][]}.</li>
 *
 *   <li>A ({@linkplain CoordinateReferenceSystem coordinate reference system} -
 *       {@linkplain MathTransform transform}) pair, <strong>or</strong> an {@linkplain Envelope
 *       envelope}, <strong>or</strong> a {@linkplain GridGeometry2D grid geometry}. The envelope
 *       is easier to use, while the transform provides more control.</li>
 *
 *   <li>Information about each {@linkplain GridSampleDimension sample dimensions} (often
 *       called <cite>bands</cite> in the particular case of images), <strong>or</strong> minimal
 *       and maximal expected values for each bands.</li>
 *
 *   <li>Optional properties as a {@linkplain Map map} of <cite>key</cite>-<cite>value</cite> pairs.
 *       "Properties" in <cite>Java Advanced Imaging</cite> are called "Metadata" by OpenGIS.
 *       Keys are {@link String} objects ({@link CaselessStringKey} are accepted as well), while
 *       values may be any {@link Object}.</li>
 * </ul>
 * <p>
 * The {@linkplain CoordinateReferenceSystem coordinate reference system} is inferred from the
 * supplied {@linkplain Envelope envelope} or {@linkplain GridGeometry2D grid geometry} parameters.
 * If those parameters do not have CRS information, then this factory fallback on a {@linkplain
 * #getDefaultCRS default CRS}.
 * <p>
 * Every {@code create} methods will ultimately delegate their work to a master
 * {@link #create(CharSequence, RenderedImage, GridGeometry2D, GridSampleDimension[],
 * GridCoverage[], Map) create} variant. Developers can override this method if they
 * want to intercept the creation of all {@link GridCoverage2D} objects in this factory.
 *
 * @author Martin Desruisseaux (IRD, Geomatys)
 * @version 3.20
 *
 * @since 2.1
 * @module
 *
 * @deprecated Replaced by {@link GridCoverageBuilder}.
 */
@ThreadSafe
@Deprecated
public class GridCoverageFactory extends Factory {
    /**
     * Whatever we should use {@link GridCoverageBuilder}. As of Geotk 3.20, we use the builder
     * for testing purpose on the JDK7 branch but conservatively keep the old algorithms on the
     * default branch.
     *
     * @since 3.20
     */
    private static final boolean USE_BUILDER = true;

    /**
     * The hints to be given to {@link GridCoverageBuilder}, or {@code null} if none.
     */
    private final Hints builderHints;

    /**
     * The hints to be given to coverage constructor.
     *
     * @todo Put there only the hints we need.
     */
    private final Hints userHints = null;

    /**
     * Creates a default factory. Users should not need to creates instance of this class directly.
     * Invoke {@link org.geotoolkit.coverage.CoverageFactoryFinder#getGridCoverageFactory} instead.
     */
    public GridCoverageFactory() {
        this(EMPTY_HINTS);
    }

    /**
     * Creates a factory using the specified set of hints.
     * The factory recognizes the following hints:
     * <p>
     * <ul>
     *   <li>{@link Hints#DEFAULT_COORDINATE_REFERENCE_SYSTEM}</li>
     *   <li>{@link Hints#TILE_ENCODING}</li>
     * </ul>
     *
     * @param userHints An optional set of hints to use for coverage constructions.
     */
    public GridCoverageFactory(final Hints userHints) {
        CoordinateReferenceSystem defaultCRS = null;
        String tileEncoding = null;
        if (userHints != null) {
            defaultCRS = (CoordinateReferenceSystem) userHints.get(Hints.DEFAULT_COORDINATE_REFERENCE_SYSTEM);
            if (Objects.equals(defaultCRS, DefaultGeographicCRS.WGS84) ||
                Objects.equals(defaultCRS, DefaultGeographicCRS.WGS84_3D))
            {
                // Will be handled in a special way by getDefaultCRS(int)
                defaultCRS = null;
            }
            tileEncoding = (String) userHints.get(Hints.TILE_ENCODING);
            if (tileEncoding != null) {
                tileEncoding = tileEncoding.trim();
                if (tileEncoding.isEmpty()) {
                    tileEncoding = null;
                }
            }
        }
        hints.put(Hints.DEFAULT_COORDINATE_REFERENCE_SYSTEM, defaultCRS);
        hints.put(Hints.TILE_ENCODING, tileEncoding);
        final Hints copy = new Hints(EMPTY_HINTS);
        FactoryUtilities.addValidEntries(hints, copy, true);
        builderHints = copy.isEmpty() ? null : copy;
    }

    /**
     * Returns the default coordinate reference system to use when no CRS were explicitly
     * specified by the user. If a {@link Hints#DEFAULT_COORDINATE_REFERENCE_SYSTEM
     * DEFAULT_COORDINATE_REFERENCE_SYSTEM} hint were provided at factory construction
     * time, then the specified CRS is returned. Otherwise, the default implementation
     * returns {@link DefaultGeographicCRS#WGS84} or its 3D variant. Subclasses should
     * override this method if they want to use different defaults.
     *
     * @param dimension The number of dimension expected in the CRS to be returned.
     * @return The new grid coverage.
     *
     * @since 2.2
     */
    protected CoordinateReferenceSystem getDefaultCRS(final int dimension) {
        final CoordinateReferenceSystem candidate = (CoordinateReferenceSystem)
                hints.get(Hints.DEFAULT_COORDINATE_REFERENCE_SYSTEM);
        if (candidate != null) {
            return candidate;
        }
        switch (dimension) {
            case  2: return DefaultGeographicCRS.WGS84;
            case  3: return DefaultGeographicCRS.WGS84_3D;
            default: throw new IllegalArgumentException(Errors.format(
                    Errors.Keys.ILLEGAL_ARGUMENT_2, "dimension", dimension));
        }
    }

    /**
     * Constructs a grid coverage from an {@linkplain ImageFunction image function}.
     *
     * @param name
     *          The grid coverage name, or {@code null} if none.
     * @param function
     *          The image function.
     * @param gridGeometry
     *          The grid geometry. The {@linkplain GridGeometry2D#getExtent() grid extent}
     *          must contains the expected image size (width and height).
     * @param bands
     *          Sample dimensions for each image band, or {@code null} for default sample dimensions.
     * @param properties
     *          The set of properties for this coverage, or {@code null} if there is none.
     * @return The new grid coverage.
     *
     * @since 2.2
     */
    public GridCoverage2D create(final CharSequence          name,
                                 final ImageFunction         function,
                                 final GridGeometry2D        gridGeometry,
                                 final GridSampleDimension[] bands,
                                 final Map<?,?>              properties)
    {
        final MathTransform transform = gridGeometry.getGridToCRS2D();
        if (!(transform instanceof AffineTransform)) {
            throw new IllegalArgumentException(Errors.format(Errors.Keys.NOT_AN_AFFINE_TRANSFORM));
        }
        final AffineTransform at = (AffineTransform) transform;
        if (at.getShearX()!=0 || at.getShearY()!=0) {
            // TODO: We may support that in a future version.
            //       1) Create a copy with shear[X/Y] set to 0. Use the copy.
            //       2) Compute the residu with createInverse() and concatenate().
            //       3) Apply the residu with JAI.create("Affine").
            throw new IllegalArgumentException("Shear and rotation not supported");
        }
        final double xScale =  at.getScaleX();
        final double yScale =  at.getScaleY();
        final double xTrans = -at.getTranslateX() / xScale;
        final double yTrans = -at.getTranslateY() / yScale;
        final GridEnvelope extent = gridGeometry.getExtent();
        final ParameterBlock param = new ParameterBlock()
                .add(function)
                .add(extent.getSpan(0)) // width
                .add(extent.getSpan(1)) // height
                .add((float) xScale)
                .add((float) yScale)
                .add((float) xTrans)
                .add((float) yTrans);
        final PlanarImage image = JAI.create("ImageFunction", param);
        return create(name, image, gridGeometry, bands, null, properties);
    }

    /**
     * Constructs a grid coverage from the specified matrix and {@linkplain Envelope envelope}.
     * A default color palette is built from the minimal and maximal values found in the matrix.
     *
     * @param name
     *          The grid coverage name, or {@code null} if none.
     * @param matrix
     *          The matrix data in a {@code [row][column]} layout.
     *          {@linkplain Float#NaN NaN} values are mapped to a transparent color.
     * @param envelope
     *          The envelope.
     * @return The new grid coverage.
     *
     * @since 2.2
     */
    public GridCoverage2D create(final CharSequence name,
                                 final float[][]    matrix,
                                 final Envelope     envelope)
    {
        if (USE_BUILDER) {
            final GridCoverageBuilder builder = new GridCoverageBuilder(builderHints);
            builder.setName(name);
            builder.setEnvelope(envelope);
            builder.setRenderedImage(matrix);
            return builder.getGridCoverage2D();
        }
        int width  = 0;
        int height = matrix.length;
        for (int j=0; j<height; j++) {
            final float[] row = matrix[j];
            if (row != null) {
                if (row.length > width) {
                    width = row.length;
                }
            }
        }
        final WritableRaster raster;
        // Need to use JAI raster factory, since WritableRaster
        // does not supports TYPE_FLOAT as of J2SE 1.5.0_06.
        raster = RasterFactory.createBandedRaster(DataBuffer.TYPE_FLOAT, width, height, 1, null);
        for (int j=0; j<height; j++) {
            int i = 0;
            final float[] row = matrix[j];
            if (row != null) {
                for (; i<row.length; i++) {
                    raster.setSample(i, j, 0, row[i]);
                }
            }
            for (; i<width; i++) {
                raster.setSample(i, j, 0, Float.NaN);
            }
        }
        return create(name, raster, envelope);
    }

    /**
     * Constructs a grid coverage from the specified {@linkplain WritableRaster raster} and
     * {@linkplain Envelope envelope}. A default color palette is built from the minimal and
     * maximal values found in the raster.
     *
     * @param name
     *          The grid coverage name, or {@code null} if none.
     * @param raster
     *          The data (may be floating point numbers). {@linkplain Float#NaN NaN}
     *          values are mapped to a transparent color.
     * @param envelope
     *          The envelope.
     * @return The new grid coverage.
     */
    public GridCoverage2D create(final CharSequence   name,
                                 final WritableRaster raster,
                                 final Envelope       envelope)
    {
        return create(name, raster, envelope, null, null, null, null, null);
    }

    /**
     * Constructs a grid coverage from the specified {@linkplain WritableRaster raster} and
     * {@linkplain Envelope envelope}.
     *
     * See the {@linkplain #create(CharSequence, RenderedImage, Envelope, GridSampleDimension[],
     * GridCoverage[], Map) rendered image variant} for a note on heuristic rules applied by this
     * method.
     *
     * @param name
     *          The grid coverage name, or {@code null} if none.
     * @param raster
     *          The data (may be floating point numbers).
     *          {@linkplain Float#NaN NaN} values are mapped to a transparent color.
     * @param envelope
     *          The grid coverage coordinates and its CRS. This envelope must have at least two
     *          dimensions. The two first dimensions describe the image location along <var>x</var>
     *          and <var>y</var> axis. The other dimensions are optional and may be used to locate
     *          the image on a vertical axis or on the time axis.
     * @param minValues
     *          The minimal value for each band in the raster,
     *          or {@code null} for computing it automatically.
     * @param maxValues
     *          The maximal value for each band in the raster,
     *          or {@code null} for computing it automatically.
     * @param units
     *          The units of sample values, or {@code null} if unknown.
     * @param colors
     *          The colors to use for values from {@code minValues} to {@code maxValues} for each
     *          bands, or {@code null} for a default color palette. If non-null, each arrays
     *          {@code colors[b]} may have any length; colors will be interpolated as needed.
     * @param hints
     *          An optional set of rendering hints, or {@code null} if none. Those hints will not
     *          affect the grid coverage to be created. However, they may affect the grid coverage
     *          to be returned by <code>{@link GridCoverage2D#view view}(View.RENDERED)</code>.
     *          The optional {@link Hints#SAMPLE_DIMENSION_TYPE SAMPLE_DIMENSION_TYPE} hint
     *          specifies the {@link SampleDimensionType} to be used at rendering time, which can
     *          be one of {@link SampleDimensionType#UNSIGNED_8BITS UNSIGNED_8BITS} or
     *          {@link SampleDimensionType#UNSIGNED_16BITS UNSIGNED_16BITS}.
     * @return The new grid coverage.
     *
     * @since 2.2
     */
    public GridCoverage2D create(final CharSequence   name,
                                 final WritableRaster raster,
                                 final Envelope       envelope,
                                 final double[]       minValues,
                                 final double[]       maxValues,
                                 final Unit<?>        units,
                                 final Color[][]      colors,
                                 final RenderingHints hints)
    {
        if (USE_BUILDER) {
            final Hints merge = new Hints(builderHints);
            if (hints != null) merge.putAll(hints);
            final GridCoverageBuilder builder = new GridCoverageBuilder(merge);
            builder.setName(name);
            builder.setEnvelope(envelope);
            builder.setSampleDimensions(minValues, maxValues, units, colors);
            builder.setRenderedImage(raster);
            return builder.getGridCoverage2D();
        }
        final int numBands = raster.getNumBands();
        final Unit<?>[] unitsArray = new Unit<?>[numBands]; Arrays.fill(unitsArray, units);
        final CharSequence[] names = new CharSequence[numBands]; Arrays.fill(names, name);
        final GridSampleDimension[] bands =
                RenderedSampleDimension.create(names, raster, minValues, maxValues, unitsArray, colors, hints);
        final ColorModel model = bands[0].getColorModel(0, bands.length, raster.getDataBuffer().getDataType());
        final RenderedImage image = new BufferedImage(model, raster, false, null);
        return create(name, image, envelope, bands, null, null);
    }

    /**
     * Constructs a grid coverage from the specified {@linkplain WritableRaster raster} and
     * "{@linkplain GridGeometry2D#getGridToCRS grid to CRS}" transform.
     *
     * @param name
     *          The grid coverage name, or {@code null} if none.
     * @param raster
     *          The data (may be floating point numbers).
     *          {@linkplain Float#NaN NaN} values are mapped to a transparent color.
     * @param crs
     *          The coordinate reference system. This specifies the CRS used when
     *          accessing a grid coverage with the {@code evaluate} methods.
     * @param gridToCRS
     *          The math transform from grid to coordinate reference system.
     * @param minValues
     *          The minimal value for each band in the raster,
     *          or {@code null} for computing it automatically.
     * @param maxValues
     *          The maximal value for each band in the raster,
     *          or {@code null} for computing it automatically.
     * @param units
     *          The units of sample values, or {@code null} if unknown.
     * @param colors
     *          The colors to use for values from {@code minValues} to {@code maxValues} for each
     *          bands, or {@code null} for a default color palette. If non-null, each arrays
     *          {@code colors[b]} may have any length; colors will be interpolated as needed.
     * @param hints
     *          An optional set of rendering hints, or {@code null} if none. Those hints will not
     *          affect the grid coverage to be created. However, they may affect the grid coverage
     *          to be returned by <code>{@link GridCoverage2D#view view}(View.RENDERED)</code>.
     *          The optional {@link Hints#SAMPLE_DIMENSION_TYPE SAMPLE_DIMENSION_TYPE} hint
     *          specifies the {@link SampleDimensionType} to be used at rendering time, which can
     *          be one of {@link SampleDimensionType#UNSIGNED_8BITS UNSIGNED_8BITS} or
     *          {@link SampleDimensionType#UNSIGNED_16BITS UNSIGNED_16BITS}.
     * @return The new grid coverage.
     */
    public GridCoverage2D create(final CharSequence              name,
                                 final WritableRaster            raster,
                                 final CoordinateReferenceSystem crs,
                                 final MathTransform             gridToCRS,
                                 final double[]                  minValues,
                                 final double[]                  maxValues,
                                 final Unit<?>                   units,
                                 final Color[][]                 colors,
                                 final RenderingHints            hints)
    {
        if (USE_BUILDER) {
            final Hints merge = new Hints(builderHints);
            if (hints != null) merge.putAll(hints);
            final GridCoverageBuilder builder = new GridCoverageBuilder(merge);
            builder.setName(name);
            builder.setCoordinateReferenceSystem(crs);
            builder.setGridToCRS(gridToCRS);
            builder.setSampleDimensions(minValues, maxValues, units, colors);
            builder.setRenderedImage(raster);
            return builder.getGridCoverage2D();
        }
        final int numBands = raster.getNumBands();
        final Unit<?>[] unitsArray = new Unit<?>[numBands]; Arrays.fill(unitsArray, units);
        final CharSequence[] names = new CharSequence[numBands]; Arrays.fill(names, name);
        final GridSampleDimension[] bands =
            RenderedSampleDimension.create(names, raster, minValues, maxValues, unitsArray, colors, hints);
        final ColorModel    model = bands[0].getColorModel(0, bands.length, raster.getDataBuffer().getDataType());
        final RenderedImage image = new BufferedImage(model, raster, false, null);
        return create(name, image, crs, gridToCRS, bands, null, null);
    }

    /**
     * Creates default bands for the given raster.
     *
     * @param  name   The sample dimension name, or {@code null} if none.
     * @param  raster The raster for which sample dimensions are being built.
     * @return The sample dimensions.
     */
    private static GridSampleDimension[] createDefaultBands(final CharSequence name, final Raster raster) {
        final GridSampleDimension[] bands = new GridSampleDimension[raster.getNumBands()];
        RenderedSampleDimension.create(name, null, raster, raster.getSampleModel(), null, bands);
        return bands;
    }

    /**
     * Constructs a grid coverage from the specified {@linkplain WritableRaster raster}
     * and {@linkplain Envelope envelope}. This convenience constructor performs the same
     * assumptions on axis order than the {@linkplain #create(CharSequence, RenderedImage,
     * Envelope, GridSampleDimension[], GridCoverage[], Map) rendered image variant}.
     * <p>
     * The {@linkplain CoordinateReferenceSystem coordinate reference system} is inferred from the
     * supplied envelope. The envelope must have at least two dimensions. The two first dimensions
     * describe the image location along <var>x</var> and <var>y</var> axis. The other dimensions
     * are optional and may be used to locate the image on a vertical axis or on the time axis.
     *
     * @param name
     *          The grid coverage name, or {@code null} if none.
     * @param raster
     *          The raster.
     * @param envelope
     *          The grid coverage coordinates.
     * @param bands
     *          Sample dimensions for each image band, or {@code null} for default sample dimensions.
     *          If non-null, then this array length must matches the number of bands in {@code image}.
     * @return The new grid coverage.
     *
     * @since 2.2
     */
    public GridCoverage2D create(final CharSequence           name,
                                 final WritableRaster         raster,
                                 final Envelope               envelope,
                                       GridSampleDimension... bands)
    {
        if (USE_BUILDER) {
            final GridCoverageBuilder builder = new GridCoverageBuilder(builderHints);
            builder.setName(name);
            builder.setEnvelope(envelope);
            builder.setSampleDimensions(bands);
            builder.setRenderedImage(raster);
            return builder.getGridCoverage2D();
        }
        if (bands == null || bands.length == 0) {
            bands = createDefaultBands(name, raster);
        }
        final ColorModel    model = bands[0].getColorModel(0, bands.length, raster.getDataBuffer().getDataType());
        final RenderedImage image = new BufferedImage(model, raster, false, null);
        return create(name, image, envelope, bands, null, null);
    }

    /**
     * Constructs a grid coverage from the specified {@linkplain WritableRaster raster} and
     * "{@linkplain GridGeometry2D#getGridToCRS grid to CRS}" transform.
     *
     * @param name
     *          The grid coverage name, or {@code null} if none.
     * @param raster
     *          The raster.
     * @param crs
     *          The coordinate reference system. This specifies the CRS used when accessing a grid
     *          coverage with the {@code evaluate} methods. The number of dimensions must matches
     *          the number of target dimensions of {@code gridToCRS}.
     * @param gridToCRS
     *          The math transform from grid to coordinate reference system.
     * @param bands
     *          Sample dimensions for each image band, or {@code null} for default sample dimensions.
     *          If non-null, then this array length must matches the number of bands in {@code image}.
     * @return The new grid coverage.
     *
     * @since 2.2
     */
    public GridCoverage2D create(final CharSequence              name,
                                 final WritableRaster            raster,
                                 final CoordinateReferenceSystem crs,
                                 final MathTransform             gridToCRS,
                                       GridSampleDimension...    bands)
    {
        if (USE_BUILDER) {
            final GridCoverageBuilder builder = new GridCoverageBuilder(builderHints);
            builder.setName(name);
            builder.setCoordinateReferenceSystem(crs);
            builder.setGridToCRS(gridToCRS);
            builder.setSampleDimensions(bands);
            builder.setRenderedImage(raster);
            return builder.getGridCoverage2D();
        }
        if (bands == null || bands.length == 0) {
            bands = createDefaultBands(name, raster);
        }
        final ColorModel    model = bands[0].getColorModel(0, bands.length, raster.getDataBuffer().getDataType());
        final RenderedImage image = new BufferedImage(model, raster, false, null);
        return create(name, image, crs, gridToCRS, bands, null, null);
    }

    /**
     * Constructs a grid coverage from the specified {@linkplain RenderedImage image} and
     * {@linkplain Envelope envelope}. A default set of {@linkplain GridSampleDimension sample
     * dimensions} is used. The {@linkplain CoordinateReferenceSystem coordinate reference system}
     * is inferred from the supplied envelope.
     * <p>
     * The envelope must have at least two dimensions. The two first dimensions describe the image
     * location along <var>x</var> and <var>y</var> axis. The other dimensions are optional and may
     * be used to locate the image on a vertical axis or on the time axis.
     *
     * @param name     The grid coverage name, or {@code null} if none.
     * @param image    The image.
     * @param envelope The grid coverage coordinates.
     * @return The new grid coverage.
     *
     * @since 2.2
     */
    public GridCoverage2D create(final CharSequence  name,
                                 final RenderedImage image,
                                 final Envelope      envelope)
    {
        if (USE_BUILDER) {
            final GridCoverageBuilder builder = new GridCoverageBuilder(builderHints);
            builder.setName(name);
            builder.setEnvelope(envelope);
            builder.setRenderedImage(image);
            return builder.getGridCoverage2D();
        }
        return create(name, image, envelope, null, null, null);
    }

    /**
     * Constructs a grid coverage from the specified {@linkplain RenderedImage image} and
     * {@linkplain Envelope envelope}. An {@linkplain AffineTransform affine transform} will
     * be computed automatically from the specified envelope using heuristic rules described below.
     * <p>
     * This convenience constructor assumes that axis order in the supplied image matches exactly
     * axis order in the supplied envelope. In other words, in the usual case where axis order in
     * the image is (<var>column</var>, <var>row</var>), then the envelope should probably have a
     * (<var>longitude</var>, <var>latitude</var>) or (<var>easting</var>, <var>northing</var>)
     * axis order.
     * <p>
     * An exception to the above rule applies for CRS using exactly the following axis order:
     * ({@link AxisDirection#NORTH NORTH}|{@link AxisDirection#SOUTH SOUTH},
     * {@link AxisDirection#EAST EAST}|{@link AxisDirection#WEST WEST}).
     * An example of such CRS is {@code EPSG:4326}. This convenience constructor will
     * interchange automatically the (<var>y</var>,<var>x</var>) axis for such CRS.
     * <p>
     * If more control on axis order and direction reversal is wanted, use the {@linkplain
     * #create(CharSequence, RenderedImage, CoordinateReferenceSystem, MathTransform,
     * GridSampleDimension[], GridCoverage[], Map) constructor variant expecting an explicit
     * transform}.
     *
     * @param name
     *          The grid coverage name, or {@code null} if none.
     * @param image
     *          The image.
     * @param envelope
     *          The grid coverage coordinates. This envelope must have at least two dimensions.
     *          The two first dimensions describe the image location along <var>x</var> and
     *          <var>y</var> axis. The other dimensions are optional and may be used to locate
     *          the image on a vertical axis or on the time axis.
     * @param bands
     *          Sample dimensions for each image band, or {@code null} for default sample dimensions.
     *          If non-null, then this array length must matches the number of bands in {@code image}.
     * @param sources
     *          The sources for this grid coverage, or {@code null} if none.
     * @param properties
     *          The set of properties for this coverage, or {@code null} if there is none.
     * @return The new grid coverage.
     *
     * @since 2.2
     */
    public GridCoverage2D create(final CharSequence          name,
                                 final RenderedImage         image,
                                       Envelope              envelope,
                                 final GridSampleDimension[] bands,
                                 final GridCoverage[]        sources,
                                 final Map<?,?>              properties)
    {
        if (USE_BUILDER) {
            final GridCoverageBuilder builder = new GridCoverageBuilder(builderHints);
            builder.setName(name);
            builder.setEnvelope(envelope);
            builder.setRenderedImage(image); // It is safer to define after the grid geometry.
            builder.setSampleDimensions(bands);
            builder.setSources(sources);
            builder.setProperties(properties);
            return builder.getGridCoverage2D();
        }
        /*
         * Makes sure that the specified envelope has a CRS.
         * If no CRS were specified, a default one is used.
         */
        if (envelope.getCoordinateReferenceSystem() == null) {
            final GeneralEnvelope e = new GeneralEnvelope(envelope);
            e.setCoordinateReferenceSystem(getDefaultCRS(e.getDimension()));
            envelope = e;
        }
        final GridGeometry2D gm = new GridGeometry2D(
                new GeneralGridEnvelope(image, envelope.getDimension()), envelope);
        return create(name, image, gm, bands, sources, properties);
    }

    /**
     * Constructs a grid coverage from the specified {@linkplain RenderedImage image} and
     * "{@linkplain GridGeometry2D#getGridToCRS grid to CRS}" transform.
     *
     * @param name
     *          The grid coverage name, or {@code null} if none.
     * @param image
     *          The image.
     * @param crs
     *          The coordinate reference system. This specifies the CRS used when accessing a grid
     *          coverage with the {@code evaluate} methods. The number of dimensions must matches
     *          the number of target dimensions of {@code gridToCRS}.
     * @param gridToCRS
     *          The math transform from grid to coordinate reference system.
     * @param bands
     *          Sample dimension for each image band, or {@code null} for default sample dimensions.
     *          If non-null, then this array length must matches the number of bands in the {@code image}.
     * @param sources
     *          The sources for this grid coverage, or {@code null} if none.
     * @param properties
     *          The set of properties for this coverage, or {@code null} if there is none.
     * @return The new grid coverage.
     */
    public GridCoverage2D create(final CharSequence              name,
                                 final RenderedImage             image,
                                 final CoordinateReferenceSystem crs,
                                 final MathTransform             gridToCRS,
                                 final GridSampleDimension[]     bands,
                                 final GridCoverage[]            sources,
                                 final Map<?,?>                  properties)
    {
        if (USE_BUILDER) {
            final GridCoverageBuilder builder = new GridCoverageBuilder(builderHints);
            builder.setName(name);
            builder.setCoordinateReferenceSystem(crs);
            builder.setGridToCRS(gridToCRS);
            builder.setRenderedImage(image); // It is safer to define after the grid geometry.
            builder.setSampleDimensions(bands);
            builder.setSources(sources);
            builder.setProperties(properties);
            return builder.getGridCoverage2D();
        }
        final GridGeometry2D gm = new GridGeometry2D(new GeneralGridEnvelope(image,
                crs.getCoordinateSystem().getDimension()), gridToCRS, crs);
        return create(name, image, gm, bands, sources, properties);
    }

    /**
     * Constructs a grid coverage from the specified {@linkplain RenderedImage image} and
     * {@linkplain GridGeometry2D grid geometry}. The {@linkplain Envelope envelope}
     * (including the {@linkplain CoordinateReferenceSystem coordinate reference system})
     * is inferred from the grid geometry.
     * <p>
     * This is the most general constructor, the one that gives the maximum control
     * on the grid coverage to be created. Every {@code create} methods will ultimately
     * delegate their work this master method. Developers can override this method if they
     * want to intercept the creation of all {@link GridCoverage2D} objects in this factory.
     *
     * @param name
     *          The grid coverage name, or {@code null} if none.
     * @param image
     *          The image.
     * @param gridGeometry
     *          The grid geometry (must contains an {@linkplain GridGeometry2D#getEnvelope envelope}
     *          with its {@linkplain GridGeometry2D#getCoordinateReferenceSystem coordinate reference
     *          system} and a "{@linkplain GridGeometry2D#getGridToCRS grid to CRS}" transform).
     * @param bands
     *          Sample dimensions for each image band, or {@code null} for default sample dimensions.
     *          If non-null, then this array length must matches the number of bands in {@code image}.
     * @param sources
     *          The sources for this grid coverage, or {@code null} if none.
     * @param properties
     *          The set of properties for this coverage, or {@code null} none.
     * @return The new grid coverage.
     *
     * @since 2.2
     */
    public GridCoverage2D create(final CharSequence          name,
                                 final RenderedImage         image,
                                       GridGeometry2D        gridGeometry,
                                 final GridSampleDimension[] bands,
                                 final GridCoverage[]        sources,
                                 final Map<?,?>              properties)
    {
        if (USE_BUILDER) {
            final GridCoverageBuilder builder = new GridCoverageBuilder(builderHints);
            builder.setName(name);
            builder.setGridGeometry(gridGeometry);
            builder.setRenderedImage(image); // It is safer to define after the grid geometry.
            builder.setSampleDimensions(bands);
            builder.setSources(sources);
            builder.setProperties(properties);
            return builder.getGridCoverage2D();
        }
        /*
         * Makes sure that the specified grid geometry has a CRS.
         * If no CRS were specified, a default one is used.
         */
        if (!gridGeometry.isDefined(GridGeometry2D.CRS)) {
            final int dimension = gridGeometry.getGridToCRS().getTargetDimensions();
            gridGeometry = new GridGeometry2D(gridGeometry, getDefaultCRS(dimension));
        }
        final GridCoverage2D coverage;
        coverage = new GridCoverage2D(name, PlanarImage.wrapRenderedImage(image),
                gridGeometry, bands, sources, properties, userHints);
        coverage.tileEncoding = (String) hints.get(Hints.TILE_ENCODING);
        return coverage;
    }
}
