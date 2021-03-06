 /*
 *    Geotoolkit.org - An Open Source Java GIS Toolkit
 *    http://www.geotoolkit.org
 *
 *    (C) 2012, Geomatys
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
package org.geotoolkit.image.interpolation;

import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.image.*;
import javax.imageio.ImageTypeSpecifier;
import org.apache.sis.geometry.Envelope2D;
import org.apache.sis.geometry.Envelopes;
import org.apache.sis.image.PixelIterator;
import org.apache.sis.image.WritablePixelIterator;
import org.apache.sis.internal.referencing.j2d.AffineTransform2D;
import org.apache.sis.referencing.CRS;
import org.apache.sis.referencing.operation.transform.MathTransforms;
import static org.junit.Assert.*;
import org.junit.Test;
import org.opengis.geometry.Envelope;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.crs.ProjectedCRS;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;
import org.opengis.util.FactoryException;

/**
 * Test resampling class.
 *
 * @author Rémi Marechal (Geomatys).
 */
public class ResampleTest extends org.geotoolkit.test.TestBase {
    /**
     * Transformation applicate to source or destination image to considered a pixel orientation center.
     */
    private static final MathTransform pixelInCellCenter = new AffineTransform2D(1, 0, 0, 1, 0.5, 0.5);

    /**
     * Destination image.
     * Resampling result.
     */
    private WritableRenderedImage targetImage;

    /**
     * Source image.
     * Image within interpolation computing is applicate.
     */
    private WritableRenderedImage sourceImg;

    public ResampleTest() {

        final ColorSpace cs = ColorSpace.getInstance(ColorSpace.CS_GRAY);
        final ColorModel cm = new ComponentColorModel(cs, new int[]{Double.SIZE}, false, false, Transparency.OPAQUE, DataBuffer.TYPE_DOUBLE);
        final ImageTypeSpecifier imgTypeSpec = new ImageTypeSpecifier(cm, cm.createCompatibleSampleModel(1, 1));
        sourceImg = imgTypeSpec.createBufferedImage(3, 3);
        final WritableRaster raster = sourceImg.getWritableTile(0, 0);

        raster.setSample(0, 0, 0, 1);
        raster.setSample(1, 0, 0, 1);
        raster.setSample(2, 0, 0, 1);

        raster.setSample(0, 1, 0, 1);
        raster.setSample(1, 1, 0, 2);
        raster.setSample(2, 1, 0, 1);

        raster.setSample(0, 2, 0, 1);
        raster.setSample(1, 2, 0, 1);
        raster.setSample(2, 2, 0, 1);
    }

    /**
     * Effectuate Resample test which use internaly a grid.
     * This test, test bilinear resample from computed coordinate into grid and moreover image sample values.
     */
    @Test
    public void gridCoordinateTest() throws NoSuchAuthorityCodeException, FactoryException, TransformException {

        final ColorSpace cs = ColorSpace.getInstance(ColorSpace.CS_GRAY);
        final ColorModel cm = new ComponentColorModel(cs, new int[]{Double.SIZE}, false, false, Transparency.OPAQUE, DataBuffer.TYPE_DOUBLE);
        final ImageTypeSpecifier imgTypeSpec = new ImageTypeSpecifier(cm, cm.createCompatibleSampleModel(1, 1));
        sourceImg = imgTypeSpec.createBufferedImage(4, 4);
        final WritableRaster raster = sourceImg.getWritableTile(0, 0);
        //band0
        raster.setSample(0, 0, 0, 1);
        raster.setSample(1, 0, 0, 1);
        raster.setSample(2, 0, 0, 1);
        raster.setSample(3, 0, 0, 1);

        raster.setSample(0, 1, 0, 1);
        raster.setSample(1, 1, 0, 2);
        raster.setSample(2, 1, 0, 2);
        raster.setSample(3, 1, 0, 1);

        raster.setSample(0, 2, 0, 1);
        raster.setSample(1, 2, 0, 2);
        raster.setSample(2, 2, 0, 2);
        raster.setSample(3, 2, 0, 1);

        raster.setSample(0, 3, 0, 1);
        raster.setSample(1, 3, 0, 1);
        raster.setSample(2, 3, 0, 1);
        raster.setSample(3, 3, 0, 1);

        setTargetImage(12, 12, DataBuffer.TYPE_DOUBLE, -1000);

        //-- creation du crs
        final CoordinateReferenceSystem crs = CRS.forCode("EPSG:2154");//-- world mercator 3395

        final ProjectedCRS projCRS = (ProjectedCRS) crs;

        final CoordinateReferenceSystem baseCrs = projCRS.getBaseCRS();

        final MathTransform mt = projCRS.getConversionFromBase().getMathTransform(); //-- source to dest

//        final Envelope srcEnv = new Envelope2D(baseCrs, 20, 44.5, 45, 45); //-- geographic

        final Envelope srcEnv = new Envelope2D(baseCrs,  45,-8, 5, 16); //-- geographic

        final Envelope destEnv = Envelopes.transform(mt, srcEnv);//-- dest envelop

        final AffineTransform2D srcGridToCrs = new AffineTransform2D(srcEnv.getSpan(0) / 4, 0, 0, -srcEnv.getSpan(1) / 4, srcEnv.getMinimum(0), srcEnv.getMaximum(1));

        final AffineTransform2D destGridToCrs = new AffineTransform2D(destEnv.getSpan(0) / 12, 0, 0, -destEnv.getSpan(1) / 12, destEnv.getMinimum(0), destEnv.getMaximum(1));

        final MathTransform pixSrcGridToCrs = MathTransforms.concatenate(pixelInCellCenter, srcGridToCrs);

        final MathTransform pixDestGridToProjCrs = MathTransforms.concatenate(pixelInCellCenter, destGridToCrs);

        final MathTransform theMTSrcToDest = MathTransforms.concatenate(pixSrcGridToCrs, mt, pixDestGridToProjCrs.inverse());

        /*
         * Resampling
         */
        final Resample resample = new Resample(theMTSrcToDest.inverse(), targetImage, sourceImg,
                InterpolationCase.BICUBIC, ResampleBorderComportement.EXTRAPOLATION, new double[]{0});
        resample.fillImage();

        //-- get the grid
        final ResampleGrid resGrid = null;  // resample.getGrid();
        if (true) return;                   // TODO grid support has been removed for now because not used.

        assertNotNull("grid should not be null", resGrid);

        final double[] expectedResultByFeedBack = compareGrid(resGrid, 12, 12, theMTSrcToDest);

        final Raster coverageRaster = targetImage.getTile(0, 0);

        final double tol = StrictMath.sqrt(2) * 0.125;
        java.awt.image.DataBufferDouble datadouble = (java.awt.image.DataBufferDouble) coverageRaster.getDataBuffer();
        final double[] testedArray = datadouble.getData(0);
        assertArrayEquals(expectedResultByFeedBack, testedArray, tol);
    }

    /**
     * Study grid built during resample an verify pertinency of its values from
     * destination coordinates transformed by {@link MathTransform}.
     *
     * @param testedGrid the grid which will be test.
     * @param destImgWidth destination image width.
     * @param destImgHeight destination image height.
     * @param srcToDest {@link MathTransform} use to resample image.
     * @throws TransformException
     */
    private double[] compareGrid(final ResampleGrid testedGrid, final int destImgWidth, final int destImgHeight, final MathTransform srcToDest) throws TransformException {

        final int gridW = testedGrid.getGridWidth();
        final int gridH = testedGrid.getGridHeight();
        final int stepx = testedGrid.getStepX();
        final int stepy = testedGrid.getStepY();

        final MathTransform destToSrc = srcToDest.inverse();

        //-- interpolation value
        final double[] feedBack = new double[destImgHeight * destImgWidth];
        int fbid = 0;

        final Interpolation interpol = Interpolation.create(PixelIterator.create(sourceImg),
                InterpolationCase.BICUBIC, 0, ResampleBorderComportement.EXTRAPOLATION, new double[]{0});

        double[] grid = testedGrid.getGrid();

        int desty = 0;
        int destx = 0;
        int gx = 0;
        int gy = 0;
        int nextgy = stepy;

        double[] destCoords = new double[2];

        while (desty < destImgHeight) {
            if (desty == nextgy) {
                nextgy += stepy;
                gy++;
                gy = StrictMath.min(gy, gridH - 2);
            }
            int nextgx = stepx;
            destx = 0;
            gx = 0;
            while (destx < destImgWidth) {
                if (destx == nextgx) {
                    nextgx += stepx;
                    gx++;
                    gx = StrictMath.min(gx, gridW - 2);
                }
                destCoords[0] = destx;
                destCoords[1] = desty;
                destToSrc.transform(destCoords, 0, destCoords, 0, 1);

                feedBack[fbid++] = interpol.interpolate(destCoords[0], destCoords[1], 0);

                // compare result entre result transformation et interpol grid[gx][gy]
                final int id00 = ((gy * gridW + gx) << 1);
                final int id01 = id00 + (gridW << 1);

                final double testedx = interpolate2D(gx, gy, grid[id00], grid[id00 + 2], grid[id01], grid[id01 + 2], destx / ((double)stepx), desty / ((double)stepy));
                final double testedy = interpolate2D(gx, gy, grid[id00 | 1], grid[(id00 + 2) | 1], grid[id01 | 1], grid[(id01 + 2) | 1], destx / ((double)stepx), desty / ((double)stepy));

                assertEquals("at ("+destx+", "+desty+") X : ", destCoords[0], testedx, 0.125);
                assertEquals("at ("+destx+", "+desty+") Y : ", destCoords[1], testedy, 0.125);
                destx ++;
            }
            desty++;
        }
        return feedBack;
    }

    /**
     * Bilinear interpolation.
     *
     * @param t0x bilinear interpolation origine in X direction.
     * @param t0y bilinear interpolation origine in Y direction.
     * @param x position which will be interpolate in X direction.
     * @param y position which will be interpolate in Y direction.
     */
    private static double interpolate2D(double t0x, double t0y, double f00, double f10, double f01, double f11, double x, double y) {
        final double x0 = interpolate1D(t0x, x, f00, f10);
        final double x1 = interpolate1D(t0x, x, f01, f11);
        return interpolate1D(t0y, y, x0, x1);
    }

    /**
     * Compute linear interpolation between 2 values.
     * {@inheritDoc }
     */
    private static double interpolate1D(double t0, double t, double f0, double f1) {
        return (t - t0) * (f1 - f0) + f0;
    }

    /**
     * Affect appropriate image for tests.
     *
     * @param width image width.
     * @param height image height.
     * @param dataType image data type.
     * @param value fill image with this value.
     */
    private void setTargetImage(int width, int height, int dataType, double value) {
        final ColorSpace cs = ColorSpace.getInstance(ColorSpace.CS_GRAY);
        final ColorModel cm = new ComponentColorModel(cs, false, false, Transparency.OPAQUE, dataType);
        final ImageTypeSpecifier imgTypeSpec = new ImageTypeSpecifier(cm, cm.createCompatibleSampleModel(1, 1));
        targetImage = imgTypeSpec.createBufferedImage(width, height);

        final WritablePixelIterator pix = WritablePixelIterator.create(targetImage);

        while (pix.next()) {
            pix.setSample(0, value);
        }
    }
}
