/*
 *    Geotoolkit.org - An Open Source Java GIS Toolkit
 *    http://www.geotoolkit.org
 *
 *    (C) 2015, Geomatys
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

package org.geotoolkit.coverage.io;

import java.util.concurrent.CancellationException;
import org.apache.sis.storage.DataStoreException;
import org.geotoolkit.coverage.grid.GridCoverage;

/**
 * Generalized version of the GridCoverageWriter for possible none grid and
 * multi-dimensional coverages.
 *
 * @author Johann Sorel (Geomatys)
 */
public interface CoverageWriter<T extends GridCoverage> {

    /**
     * Writes a single coverage.
     *
     * @param  coverage The coverage to write.
     * @param  param Optional parameters used to control the writing process, or {@code null}.
     * @throws IllegalStateException If the output destination has not been set.
     * @throws DataStoreException If an error occurs while writing the information to the output destination.
     * @throws CancellationException If {@link #abort()} has been invoked in an other thread during
     *         the execution of this method.
     */
    public void write(T coverage, GridCoverageWriteParam param)
            throws DataStoreException, CancellationException;

    /**
     * Writes one or many coverages.
     *
     * @param  coverages The coverages to write.
     * @param  param Optional parameters used to control the writing process, or {@code null}.
     * @throws IllegalStateException If the output destination has not been set.
     * @throws DataStoreException If the iterable contains an unsupported number of coverages,
     *         or if an error occurs while writing the information to the output destination.
     * @throws CancellationException If {@link #abort()} has been invoked in an other thread during
     *         the execution of this method.
     *
     * @since 3.20
     */
    public void write(final Iterable<? extends T> coverages, final GridCoverageWriteParam param)
            throws DataStoreException, CancellationException;

    /**
     * Restores the {@code CoverageWriter} to its initial state.
     *
     * @throws DataStoreException If an error occurs while restoring to the initial state.
     */
    public void reset() throws DataStoreException;

    /**
     * Allows any resources held by this writer to be released. The result of calling
     * any other method subsequent to a call to this method is undefined.
     *
     * @throws DataStoreException If an error occurs while disposing resources.
     */
    public void dispose() throws DataStoreException;

}
