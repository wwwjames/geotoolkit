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
package org.geotoolkit.coverage.processing;

import java.awt.image.RenderedImage;
import java.util.Collection;
import javax.media.jai.PlanarImage;
import static org.apache.sis.util.ArgumentChecks.ensureNonNull;
import org.apache.sis.util.collection.Cache;
import org.geotoolkit.coverage.grid.GridCoverage;
import org.geotoolkit.coverage.grid.GridCoverage2D;
import org.geotoolkit.factory.Hints;
import org.geotoolkit.lang.Decorator;
import org.opengis.parameter.ParameterValueGroup;


/**
 * A coverage processor that cache the result of operations. Given that {@linkplain GridCoverage
 * grid coverages} may be expensive to compute and consume a lot of memory, we can save a lot of
 * resources by returning cached instances every time the same operation
 * with the same {@linkplain ParameterValueGroup parameters} is applied on the same coverage.
 * Coverages are cached using {@linkplain java.lang.ref.WeakReference weak references}.
 *
 * @author Martin Desruisseaux (IRD, Geomatys)
 */
@Decorator(AbstractCoverageProcessor.class)
public class CachingCoverageProcessor extends AbstractCoverageProcessor {

    public static final CachingCoverageProcessor INSTANCE = new CachingCoverageProcessor();

    /**
     * The underlying processor.
     */
    protected final AbstractCoverageProcessor processor;

    /**
     * A set of {@link GridCoverage}s resulting from previous invocations to {@link #doOperation}.
     * Current implementation retains the coverage by weak references only.
     *
     * @todo Use the capability of {@link Cache} to evict entries based on cost calculation.
     */
    private final Cache<CachedOperationParameters, GridCoverage> cache = new Cache<>(12, 0, false);

    /**
     * Creates a default caching processor.
     * <p>
     * This constructor should not be invoked directly - consider using
     * {@link org.geotoolkit.coverage.CoverageFactoryFinder#getCoverageProcessor(Hints)} instead.
     */
    public CachingCoverageProcessor() {
        this(EMPTY_HINTS);
    }

    /**
     * Creates a caching processor using the specified hints.
     * <p>
     * This constructor should not be invoked directly - consider using
     * {@link org.geotoolkit.coverage.CoverageFactoryFinder#getCoverageProcessor(Hints)} instead.
     *
     * @param userHints An optional set of hints, or {@code null} if none.
     */
    public CachingCoverageProcessor(final Hints userHints) {
        processor = new DefaultCoverageProcessor(userHints, this);
    }

    /**
     * Creates a new buffered processor backed by the specified processor.
     *
     * @param processor The coverage processor for which to cache the results.
     */
    public CachingCoverageProcessor(final AbstractCoverageProcessor processor) {
        ensureNonNull("processor", processor);
        this.processor = processor;
    }

    /**
     * Retrieves grid processing operations information. The default implementation forward
     * the call directly to the {@linkplain #processor underlying processor}.
     */
    @Override
    public Collection<AbstractOperation> getOperations() {
        return processor.getOperations();
    }

    /**
     * Returns the operation for the specified name. The default implementation forward
     * the call directly to the {@linkplain #processor underlying processor}.
     *
     * @param  name Name of the operation.
     * @return The operation for the given name.
     * @throws OperationNotFoundException if there is no operation for the specified name.
     */
    @Override
    public AbstractOperation getOperation(final String name) throws OperationNotFoundException {
        return processor.getOperation(name);
    }

    /**
     * Applies an operation. The default implementation first checks if a coverage has already
     * been created from the same parameters. If such a coverage is found, it is returned.
     * Otherwise, this method forward the call to the {@linkplain #processor underlying processor}
     * and caches the result.
     *
     * @param  parameters Parameters required for the operation.
     * @return The result as a coverage.
     * @throws OperationNotFoundException if there is no operation for the parameter group name.
     * @throws CoverageProcessingException if the operation can not be executed.
     */
    @Override
    public GridCoverage doOperation(final ParameterValueGroup parameters)
            throws OperationNotFoundException, CoverageProcessingException
    {
        final String operationName = getOperationName(parameters);
        final AbstractOperation operation = processor.getOperation(operationName);
        final CachedOperationParameters key = new CachedOperationParameters(operation, parameters);
        GridCoverage coverage = cache.peek(key);
        if (coverage != null) {
            log(getPrimarySource(parameters), coverage, operationName, true);
            return coverage;
        }
        final Cache.Handler<GridCoverage> handler = cache.lock(key);
        try {
            coverage = handler.peek();
            if (coverage != null) {
                log(getPrimarySource(parameters), coverage, operationName, true);
                return coverage;
            }
            coverage = processor.doOperation(parameters);
            if (coverage instanceof GridCoverage2D) {
                final RenderedImage image = ((GridCoverage2D) coverage).getRenderedImage();
                if (image instanceof PlanarImage) {
                    /*
                     * Adds a sink to the planar image in order to prevent GridCoverage2D.dispose
                     * to dispose this image as long as it still in the cache. Note that the sink
                     * is stored as a weak reference (as well as values in the cache map), so it
                     * will not prevent the garbage collector to collect the coverage. However,
                     * the current approach make GridCoverage2D.dispose(false) useless for cached
                     * coverages. We may need to find a better mechanism later (GEOT-1041).
                     */
                    ((PlanarImage) image).addSink(key);
                }
            }
            return coverage;
        } finally {
            handler.putAndUnlock(coverage);
        }
    }
}
