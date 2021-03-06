/*
 *    Geotoolkit - An Open Source Java GIS Toolkit
 *    http://www.geotoolkit.org
 *
 *    (C) 2013-2019, Geomatys
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
package org.geotoolkit.storage.coverage;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.*;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.sis.coverage.SampleDimension;
import org.apache.sis.image.ComputedImage;
import org.apache.sis.storage.DataStoreException;
import org.apache.sis.util.collection.BackingStoreException;
import org.apache.sis.util.logging.Logging;
import org.geotoolkit.image.BufferedImages;
import org.geotoolkit.math.XMath;
import org.geotoolkit.storage.multires.Mosaic;
import org.geotoolkit.storage.multires.Tile;

/**
 * Implementation of RenderedImage using GridMosaic.
 * With this a GridMosaic can be see as a RenderedImage.
 *
 * @author Thomas Rouby (Geomatys)
 * @author Quentin Boileau (Geomatys)
 * @author Johann Sorel (Geomatys)
 * @module
 */
public class MosaicImage extends ComputedImage implements RenderedImage {

    private static final Logger LOGGER = Logging.getLogger("org.geotoolkit.storage.coverage");

    /**
     * The original mosaic to read
     */
    private final Mosaic mosaic;
    /**
     * Tile range to map as a rendered image in the mosaic
     */
    private final Rectangle gridRange;

    /**
     * The color model of the mosaic rendered image
     */
    private final ColorModel colorModel;
    /**
     * The sample model of the mosaic rendered image
     */
    private final SampleModel sampleModel;
    /**
     * The raster model of the mosaic rendered image
     */
    private final Raster rasterModel;

    public static MosaicImage create(Mosaic mosaic, Rectangle gridRange, final List<SampleDimension> sampleDimensions) throws DataStoreException {
        if (gridRange == null) {
            gridRange = new Rectangle(mosaic.getGridSize());
        }

        if (gridRange.width == 0 || gridRange.height == 0) {
            throw new DataStoreException("Mosaic grid range is empty : " + gridRange);
        }

        RenderedImage sample;
        try {
            Tile tile = mosaic.anyTile();
            if (tile instanceof ImageTile) {
                sample = ((ImageTile) tile).getImage();
            } else {
                throw new DataStoreException("Mosaic does not contain images");
            }
        } catch (DataStoreException | IOException ex) {
            if (sampleDimensions != null) {
                //use a fake tile created from sample dimensions
                final Dimension tileSize = mosaic.getTileSize();
                sample = BufferedImages.createImage(tileSize.width, tileSize.height, sampleDimensions.size(), DataBuffer.TYPE_DOUBLE);
            } else if (ex instanceof DataStoreException) {
                throw (DataStoreException) ex;
            } else {
                throw new DataStoreException(ex.getMessage(), ex);
            }
        }

        final SampleModel sm = sample.getSampleModel();
        final ColorModel cm = sample.getColorModel();
        final Raster rm = sample.getTile(sample.getMinTileX(), sample.getMinTileY());
        return new MosaicImage(mosaic, gridRange, sm, cm, rm);
    }

    /**
     * Constructor
     * @param mosaic the mosaic to read as a rendered image
     * @param gridRange the tile to include in the rendered image.
     *        rectangle max max values are exclusive.
     */
    private MosaicImage(final Mosaic mosaic, Rectangle gridRange, SampleModel sampleModel, ColorModel colorModel, Raster rasterModel){
        super(sampleModel);
        this.mosaic = mosaic;
        this.gridRange = gridRange;
        this.sampleModel = sampleModel;
        this.colorModel = colorModel;
        this.rasterModel = rasterModel;
    }

    /**
     * Return intern GridMosaic
     * @return GridMosaic
     */
    public Mosaic getGridMosaic(){
        return this.mosaic;
    }

    /**
     *
     * @return
     */
    public Rectangle getGridRange() {
        return (Rectangle) gridRange.clone();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ColorModel getColorModel() {
        return this.colorModel;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SampleModel getSampleModel() {
        return this.sampleModel;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getWidth() {
        return gridRange.width * this.mosaic.getTileSize().width;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getHeight() {
        return gridRange.height * this.mosaic.getTileSize().height;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getNumXTiles() {
        return  gridRange.width;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getNumYTiles() {
        return  gridRange.height;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getTileWidth() {
        return this.mosaic.getTileSize().width;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getTileHeight() {
        return this.mosaic.getTileSize().height;
    }

    @Override
    protected Raster computeTile(int tileX, int tileY, WritableRaster previous) throws Exception {
        return getTile(tileX, tileY, false);
    }

    private Raster getTile(final int tileX, final int tileY, boolean nullable) {
        final int mosaictileX = gridRange.x + tileX;
        final int mosaictileY = gridRange.y + tileY;

        Raster raster = null;
        try {
            DataBuffer buffer = null;

            if (!mosaic.isMissing(mosaictileX,mosaictileY)) {
                final ImageTile tile = (ImageTile) mosaic.getTile(mosaictileX,mosaictileY);
                //can happen if tile is really missing, the isMissing method is a best effort call
                if (tile != null) {
                    final RenderedImage image = tile.getImage();
                    Raster tileRaster;
                    if (image instanceof BufferedImage) {
                        tileRaster = ((BufferedImage) image).getRaster();
                    } else {
                        tileRaster = image.getData();
                    }
                    tileRaster = makeConform(tileRaster);
                    buffer = tileRaster.getDataBuffer();
                }
            }

            if (!nullable && buffer == null) {
                //create an empty buffer
                buffer = getSampleModel().createDataBuffer();
                //TODO should be filled with no data pixel value
            }

            if (buffer != null) {
                //create a raster from tile image with tile position offset.
                LOGGER.log(Level.FINE, "Request tile {0}:{1} ", new Object[]{tileX,tileY});
                final int rX = tileX * this.getTileWidth();
                final int rY = tileY * this.getTileHeight();
                raster = Raster.createWritableRaster(getSampleModel(), buffer, new Point(rX, rY));
            }

        } catch (DataStoreException | IOException e) {
            LOGGER.log(Level.WARNING, e.getMessage(), e);
        }

        if (raster == null && !nullable) {
            //create an empty buffer
            final DataBuffer buffer = getSampleModel().createDataBuffer();
            final int rX = tileX * this.getTileWidth();
            final int rY = tileY * this.getTileHeight();
            raster = Raster.createWritableRaster(getSampleModel(), buffer, new Point(rX, rY));
            //TODO should be filled with no data pixel value
        }

        return raster;
    }

    private boolean isTileMissing(int x, int y) throws DataStoreException{
        return mosaic.isMissing(x+gridRange.x, y+gridRange.y);
    }

    /**
     * Ensure the given raster is compatible with declared sample model.
     * If not the data will be copied to a new raster.
     */
    private Raster makeConform(Raster in) {
        final int inNumBands = in.getNumBands();
        final int outNumBands = rasterModel.getNumBands();
        if (inNumBands != outNumBands) {
            //this is a severe issue, the mosaic to no respect it's own sample dimension definition
            throw new BackingStoreException("Mosaic tile image declares " + inNumBands
                    + " bands, but sample dimensions have " + outNumBands
                    + ", please fix mosaic implementation " + mosaic.getClass().getName());
        }

        final int inDataType = in.getDataBuffer().getDataType();
        final int outDataType = rasterModel.getDataBuffer().getDataType();
        if (inDataType != outDataType) {
            //difference in input and output types, this may be caused by an aggregated resource
            final int x = 0;
            final int y = 0;
            final int width = in.getWidth();
            final int height = in.getHeight();
            final WritableRaster raster = rasterModel.createCompatibleWritableRaster(width, height);
            final int nbSamples = width * height * inNumBands;
            switch (outDataType) {
                case DataBuffer.TYPE_BYTE :
                case DataBuffer.TYPE_SHORT :
                case DataBuffer.TYPE_USHORT :
                case DataBuffer.TYPE_INT :
                    int[] arrayi = new int[nbSamples];
                    in.getPixels(x, y, width, height, arrayi);
                    raster.setPixels(x, y, width, height, arrayi);
                    break;
                case DataBuffer.TYPE_FLOAT :
                    float[] arrayf = new float[nbSamples];
                    in.getPixels(x, y, width, height, arrayf);
                    raster.setPixels(x, y, width, height, arrayf);
                    break;
                case DataBuffer.TYPE_DOUBLE :
                default :
                    double[] arrayd = new double[nbSamples];
                    in.getPixels(x, y, width, height, arrayd);
                    raster.setPixels(x, y, width, height, arrayd);
                    break;
            }
            in = raster;
        }
        return in;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Raster getData() {
        return getData(new Rectangle(getWidth(), getHeight()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Raster getData(Rectangle rect) {
        Raster rasterOut = null;
        try {
            final Point upperLeftPosition = this.getPositionOf(rect.x, rect.y);
            final Point lowerRightPosition = this.getPositionOf(rect.x + rect.width - 1, rect.y + rect.height - 1);

            for (int y = Math.max(upperLeftPosition.y, 0); y < Math.min(lowerRightPosition.y + 1, this.getNumYTiles()); y++) {
                for (int x = Math.max(upperLeftPosition.x, 0); x < Math.min(lowerRightPosition.x + 1, this.getNumXTiles()); x++) {
                    if (!isTileMissing(x, y)) {
                        Raster tile = getTile(x, y, true);
                        if (tile != null) {
                            final Rectangle tileRect = new Rectangle(x * this.getTileWidth(), y * this.getTileHeight(), this.getTileWidth(), this.getTileHeight());

                            final int minX, maxX, minY, maxY;
                            minX = XMath.clamp(rect.x, tileRect.x, tileRect.x + tileRect.width);
                            maxX = XMath.clamp(rect.x + rect.width, tileRect.x, tileRect.x + tileRect.width);
                            minY = XMath.clamp(rect.y, tileRect.y, tileRect.y + tileRect.height);
                            maxY = XMath.clamp(rect.y + rect.height, tileRect.y, tileRect.y + tileRect.height);

                            final Rectangle rectIn = new Rectangle(minX, minY, maxX - minX, maxY - minY);
                            rectIn.translate(-tileRect.x, -tileRect.y);
                            final Rectangle rectOut = new Rectangle(minX, minY, maxX - minX, maxY - minY);
                            rectOut.translate(-rect.x, -rect.y);

                            if (rectIn.width <= 0 || rectIn.height <= 0 || rectOut.width <= 0 || rectOut.height <= 0) {
                                continue;
                            }

                            Object dataElements = tile.getSampleModel().getDataElements(rectIn.x, rectIn.y, rectIn.width, rectIn.height, null, tile.getDataBuffer());

                            if (rasterOut == null) {
                                rasterOut = createRaster(tile, rect.width, rect.height);
                            }

                            rasterOut.getSampleModel().setDataElements(rectOut.x, rectOut.y, rectOut.width, rectOut.height,
                                    dataElements,
                                    rasterOut.getDataBuffer());
                        }
                    }
                }
            }

            if (rasterOut == null) {
                //create an empty raster
                SampleModel sampleModel = getSampleModel();
                DataBuffer databuffer = getSampleModel().createDataBuffer();
                Raster raster = Raster.createRaster(sampleModel, databuffer, new Point(0, 0));
                rasterOut = createRaster(raster, rect.width, rect.height);
            }

            if (rect.x != 0 || rect.y != 0) {
                rasterOut = rasterOut.createTranslatedChild(rect.x, rect.y);
            }

        } catch (Exception ex) {
            LOGGER.log(Level.WARNING, "", ex);
        }
        return rasterOut;
    }

    private WritableRaster createRaster(Raster base, int width, int height) {
        WritableRaster rasterOut = base.createCompatibleWritableRaster(0, 0, width, height);
        // Clear dataBuffer to 0 value for all bank
        for (int s = 0; s < rasterOut.getDataBuffer().getSize(); s++) {
            for (int b = 0; b < rasterOut.getDataBuffer().getNumBanks(); b++) {
                rasterOut.getDataBuffer().setElem(b, s, 0);
            }
        }
        return rasterOut;
    }

    /**
     * Get the tile column and row position for a pixel.
     * Return value can be out of the gridSize
     * @param x
     * @param y
     * @return
     */
    private Point getPositionOf(int x, int y){
        final int posX = (int)(Math.floor(x/this.getTileWidth()));
        final int posY = (int)(Math.floor(y/this.getTileHeight()));
        return new Point(posX, posY);
    }

}
