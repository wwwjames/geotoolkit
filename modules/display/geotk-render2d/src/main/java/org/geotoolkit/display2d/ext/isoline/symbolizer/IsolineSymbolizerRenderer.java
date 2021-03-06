/*
 *    Geotoolkit - An Open Source Java GIS Toolkit
 *    http://www.geotoolkit.org
 *
 *    (C) 2014, Geomatys
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

package org.geotoolkit.display2d.ext.isoline.symbolizer;

import java.awt.Rectangle;
import java.util.Map;
import org.apache.sis.coverage.grid.GridCoverage;
import org.apache.sis.coverage.grid.GridExtent;
import org.apache.sis.coverage.grid.GridGeometry;
import org.apache.sis.geometry.GeneralEnvelope;
import org.apache.sis.internal.storage.query.CoverageQuery;
import org.apache.sis.parameter.Parameters;
import org.apache.sis.storage.DataStoreException;
import org.apache.sis.storage.FeatureSet;
import org.apache.sis.storage.GridCoverageResource;
import org.apache.sis.storage.Resource;
import org.geotoolkit.storage.memory.InMemoryGridCoverageResource;
import org.geotoolkit.display.PortrayalException;
import org.geotoolkit.display2d.GO2Utilities;
import org.geotoolkit.display2d.canvas.RenderingContext2D;
import org.geotoolkit.display2d.container.FeatureLayerJ2D;
import org.geotoolkit.display2d.primitive.ProjectedCoverage;
import org.geotoolkit.display2d.style.CachedRasterSymbolizer;
import org.geotoolkit.display2d.style.renderer.*;
import org.geotoolkit.image.interpolation.InterpolationCase;
import org.geotoolkit.image.interpolation.ResampleBorderComportement;
import org.geotoolkit.map.FeatureMapLayer;
import org.geotoolkit.map.MapBuilder;
import org.geotoolkit.map.MapLayer;
import org.geotoolkit.process.*;
import org.geotoolkit.processing.coverage.isoline.IsolineDescriptor;
import org.geotoolkit.processing.coverage.resample.ResampleDescriptor;
import static org.geotoolkit.processing.coverage.resample.ResampleDescriptor.*;
import org.geotoolkit.processing.coverage.resample.ResampleProcess;
import org.geotoolkit.style.MutableStyle;
import org.geotoolkit.style.function.Jenks;
import org.opengis.geometry.Envelope;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.datum.PixelInCell;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.style.ColorMap;
import org.opengis.style.LineSymbolizer;
import org.opengis.style.RasterSymbolizer;
import org.opengis.style.TextSymbolizer;

/**
 * @author Quentin Boileau (Geomatys)
 */
public class IsolineSymbolizerRenderer  extends AbstractCoverageSymbolizerRenderer<CachedIsolineSymbolizer> {

    public IsolineSymbolizerRenderer(final SymbolizerRendererService service, CachedIsolineSymbolizer cache, RenderingContext2D context) {
        super(service, cache, context);
    }

    @Override
    public boolean portray(ProjectedCoverage graphic) throws PortrayalException {

        IsolineSymbolizer isolineSymbolizer = symbol.getSource();

        try {
            boolean isRendered = false;
            ////////////////////
            // 1 - Render raster
            ////////////////////
            final CachedRasterSymbolizer cachedRasterSymbolizer = symbol.getCachedRasterSymbolizer();
            if (!isolineSymbolizer.getIsolineOnly() || isJenksFunction(cachedRasterSymbolizer)) {
                isRendered = GO2Utilities.portray(graphic, cachedRasterSymbolizer, renderingContext);
            }

//            final MutableStyle rasterStyle = GO2Utilities.STYLE_FACTORY.style(cachedRasterSymbolizer.getSource());
//            final CoverageMapLayer covMapLayer = MapBuilder.createCoverageLayer(resampledCoverage, rasterStyle, name.getLocalPart());
//            final StatelessCoverageLayerJ2D statelessCoverageLayer = new StatelessCoverageLayerJ2D(renderingContext.getCanvas(), covMapLayer);
//            statelessCoverageLayer.paintLayer(renderingContext);

            final LineSymbolizer lineSymbolizer = isolineSymbolizer.getLineSymbolizer();
            final TextSymbolizer textSymbolizer = isolineSymbolizer.getTextSymbolizer();

            /////////////////////
            // 2 - Isolines
            ////////////////////
            if (lineSymbolizer != null) {

                double[] intervales = symbol.getSteps();
                ////////////////////
                // 2.1 - Resample input coverage
                ////////////////////
                final MapLayer layer = graphic.getLayer();
                final CoordinateReferenceSystem coverageMapLayerCRS = layer.getBounds().getCoordinateReferenceSystem();

                final Resource resource = layer.getResource();
                if (resource instanceof GridCoverageResource) {
                    final GridCoverageResource coverageReference = (GridCoverageResource) resource;

                    double[] resolution = renderingContext.getResolution();
                    Envelope bounds = new GeneralEnvelope(renderingContext.getCanvasObjectiveBounds());
                    resolution = checkResolution(resolution, bounds);
                    if(resolution.length!=bounds.getDimension()){
                        double[] res = new double[bounds.getDimension()];
                        res[0] = resolution[0];
                        res[1] = resolution[1];
                        for(int i=2;i<res.length;i++){
                            res[i] = bounds.getSpan(i);
                        }
                        resolution = res;
                    }

                    final Map<String, Double> queryValues = RasterSymbolizerRenderer.extractQuery(layer);
                    if (queryValues != null && !queryValues.isEmpty()) {
                        bounds = RasterSymbolizerRenderer.fixEnvelopeWithQuery(queryValues, bounds, coverageMapLayerCRS);
                        resolution = RasterSymbolizerRenderer.fixResolutionWithCRS(resolution, coverageMapLayerCRS);
                    }

                    GridCoverage inCoverage = coverageReference.read(coverageReference.getGridGeometry().derive().subgrid(bounds, resolution).build());
                    inCoverage = inCoverage.forConvertedValues(true);

                    final Rectangle rec = renderingContext.getPaintingDisplayBounds();
                    final GridExtent gridEnv = new GridExtent(null, new long[]{rec.x,rec.y}, new long[]{rec.width,rec.height}, false);
                    final CoordinateReferenceSystem crs = renderingContext.getObjectiveCRS2D();
                    final MathTransform gridToCRS = renderingContext.getDisplayToObjective();
                    final GridGeometry inGridGeom = new GridGeometry(gridEnv, PixelInCell.CELL_CENTER, gridToCRS, crs);

                    final Parameters resampleParams = Parameters.castOrWrap(ResampleDescriptor.INPUT_DESC.createValue());
                    resampleParams.getOrCreate(IN_COVERAGE).setValue(inCoverage);
                    resampleParams.getOrCreate(IN_COORDINATE_REFERENCE_SYSTEM).setValue(crs);
                    resampleParams.getOrCreate(IN_GRID_GEOMETRY).setValue(inGridGeom);
                    resampleParams.getOrCreate(IN_INTERPOLATION_TYPE).setValue(InterpolationCase.BILINEAR);
                    resampleParams.getOrCreate(IN_BORDER_COMPORTEMENT_TYPE).setValue(ResampleBorderComportement.FILL_VALUE);
                    final ResampleProcess resampleProcess = new ResampleProcess(resampleParams);
                    final Parameters output = Parameters.castOrWrap(resampleProcess.call());

                    final GridCoverage resampledCoverage = (GridCoverage) output.parameter(ResampleDescriptor.OUT_COVERAGE.getName().getCode()).getValue();
                    final GridCoverageResource resampledCovRef = new InMemoryGridCoverageResource(coverageReference.getIdentifier().orElse(null), resampledCoverage);

                    final CoverageQuery query = new CoverageQuery();
                    query.setDomain(resampledCovRef.getGridGeometry().derive().subgrid(bounds, resolution).build());
                    final GridCoverageResource subref = resampledCovRef.subset(query);

                    /////////////////////
                    // 2.2 - Compute isolines
                    ////////////////////
                    FeatureSet isolines = null;
                    ProcessDescriptor isolineDesc = symbol.getIsolineDesc();
                    if (isolineDesc != null) {
                        final Parameters inputs = Parameters.castOrWrap(isolineDesc.getInputDescriptor().createValue());
                        inputs.getOrCreate(IsolineDescriptor.COVERAGE_REF).setValue(subref);
                        inputs.getOrCreate(IsolineDescriptor.INTERVALS).setValue(intervales);
                        final org.geotoolkit.process.Process process = isolineDesc.createProcess(inputs);
                        final ParameterValueGroup result = process.call();
                        isolines = (FeatureSet) result.parameter(IsolineDescriptor.FCOLL.getName().getCode()).getValue();
                    }

                    /////////////////////
                    // 2.3 - Render isolines
                    ////////////////////
                    if (isolines != null) {
                        MutableStyle featureStyle = null;
                        if (textSymbolizer != null) {
                            featureStyle = GO2Utilities.STYLE_FACTORY.style(lineSymbolizer, textSymbolizer);
                        } else {
                            featureStyle = GO2Utilities.STYLE_FACTORY.style(lineSymbolizer);
                        }

                        FeatureMapLayer fml = MapBuilder.createFeatureLayer(isolines, featureStyle);

                        FeatureLayerJ2D statelessFeatureLayerJ2D = new FeatureLayerJ2D(renderingContext.getCanvas(), fml);
                        isRendered |= statelessFeatureLayerJ2D.paintLayer(renderingContext);
                    }
                }
            }

            return isRendered;

        } catch (DataStoreException ex) {
            throw new PortrayalException(ex.getMessage(), ex);
        } catch (ProcessException e) {
            throw new PortrayalException(e.getMessage(), e);
        }
    }

    private boolean isJenksFunction(CachedRasterSymbolizer cachedRasterSymbolizer) {
        if (cachedRasterSymbolizer != null) {
            RasterSymbolizer source = cachedRasterSymbolizer.getSource();
            if (source != null && source.getColorMap() != null) {
                ColorMap colorMap = source.getColorMap();
                return (source.getColorMap().getFunction() instanceof Jenks);
            }
        }
        return false;
    }
}
