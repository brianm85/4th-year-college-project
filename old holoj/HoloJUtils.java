package holoj;

import ij.*;
import ij.process.*;
import java.awt.*;
import ij.gui.Roi;

/**
 * Class HoloJUtils contains static methods to perform useful task such as FFT and other mathematics.
 *
 * @author Luca Ortolani and Pier Francesco Fazzini
 * @version 1.0
 */ 
public final class HoloJUtils {
	
    // radius of the excluded region in sideband center search.
    private static final int EXCLUDED_RADIUS = 20;

   /**
    * Calculates the modulus of complex number with real and imaginary parts passed as arguments.
    *
    * @param real       real part.
    * @param imaginary  complex part.
    * @return the modulus.
    */
    public static double modulus(double real, double imaginary){
        double mod = Math.sqrt(real*real+imaginary*imaginary);
        return mod;
    }

   /**
    * Arranges the two double array in a single double array with the sequence: re1,im1,re2,im2,...,reN,imN.
    * 
    * @param real       real double array.
    * @param complex    complex double array.
    * @return the rearranged double array.
    */
    public static double[] arrangeToComplexArray(double[] real, double[] complex) {
        double[] output = new double[real.length << 1];
        for (int i=0;i<real.length;i++) {
            output[2*i] = real[i];
            output[2*i+1] = complex[i];	
        }
        return output;
    }
    
   /**
    * Extract the real part of the complex array arranged with arrangeToComplexArray.
    * 
    * @param input       the input complex array.
    * @return the extracted double real parts array.
    */	
    public static double[] extractRealPixels(double[] input) {
        int nmax = input.length >> 1;
        double[] output = new double[nmax];
        for (int k=0;k<nmax;k++)
            output[k]=input[2*k];
        return output;
    }
    
   /**
    * Extract the complex part of the complex array arranged with arrangeToComplexArray.
    * 
    * @param input       the input complex array.
    * @return the extracted double complex parts array.
    */	
    public static double[] extractComplexPixels(double[] input) {
        int nmax = input.length >> 1;
        double[] output = new double[nmax];
        for (int k=0;k<nmax;k++)
            output[k]=input[2*k+1];
        return output;
    }
   /**
    *
    */
    public static double[][][] arrangeToComplexMatrix(double[] real, double[] complex, int rowWidth) {
        int colHeight = (int) real.length/rowWidth;
        int pos = 0;
        double[][][] output = new double[rowWidth][colHeight][2];
        for (int row=0; row<colHeight; row++) 
            for (int col=0; col<rowWidth; col++) {
                pos = row*rowWidth+col;
                output[row][col][0] = real[pos];
                output[row][col][1] = complex[pos];
            }
        return output;
    }
	
   /**
    * Computes the Fast Fourier Transform of data passed as argument.
    *
    * @param data       data to be transformed in row-major mode and RE1,CO1,RE2,CO2... sequence.
    * @param nn         array containing dimensions os data array.
    * @param isign	forward or reverse direction.
    */
    public static void c2cfft (double[] data, int[] nn, int isign) {
	int idim,i1,i2,i3,i2rev,i3rev,ip1,ip2,ip3,ifp1,ifp2;
	int ibit,k1,k2,n,nprev,nrem,ntot;
	double tempi,tempr,theta,wi,wpi,wpr,wr,wtemp;
	double tmp;
	int ndim=nn.length;
	ntot = data.length >> 1;
	nprev=1;
	if (isign == -1) swap(data, nn[0]);
	for (idim=ndim-1;idim>=0;idim--) {
	    n=nn[idim];
	    nrem=ntot/(n*nprev);
	    ip1=nprev << 1;
	    ip2=ip1*n;
	    ip3=ip2*nrem;
	    i2rev=0;
	    for (i2=0;i2<ip2;i2+=ip1) {
                if (i2 < i2rev) {
                    for (i1=i2;i1<i2+ip1-1;i1+=2) {
                        for (i3=i1;i3<ip3;i3+=ip2) {
                            i3rev=i2rev+i3-i2;
                            tmp=data[i3];
                            data[i3]=data[i3rev];
                            data[i3rev]=tmp;
                            tmp=data[i3+1];
                            data[i3+1]=data[i3rev+1];
                            data[i3rev+1]=tmp;
                        }
                    }
                }
                ibit=ip2 >> 1;
                while (ibit >= ip1 && i2rev+1 > ibit) {
                    i2rev -= ibit;
                    ibit >>= 1;
                }
                i2rev += ibit;
	    }
	    ifp1=ip1;
	    while (ifp1 < ip2) {
                ifp2=ifp1 << 1;
                theta=isign*6.28318530717959/ifp2*ip1;
                wtemp=Math.sin(0.5*theta);
                wpr=-2*wtemp*wtemp;
                wpi=Math.sin(theta);
                wr=1.0;
                wi=0.0;
                for (i3=0;i3<ifp1;i3+=ip1) {
                    for (i1=i3;i1<i3+ip1-1;i1+=2) {
                        for (i2=i1;i2<ip3;i2+=ifp2) {
                            k1=i2;
                            k2=k1+ifp1;
                            tempr=wr*data[k2]-wi*data[k2+1];
                            tempi=wr*data[k2+1]+wi*data[k2];
                            data[k2]=data[k1]-tempr;
                            data[k2+1]=data[k1+1]-tempi;
                            data[k1] += tempr;
                            data[k1+1] += tempi;
                        }
                    }
                    wr=(wtemp=wr)*wpr-wi*wpi+wr;
                    wi=wi*wpr+wtemp*wpi+wi;
                }
                ifp1=ifp2;
	    }
	    nprev *= n;
	}
	// Rescale data back down.
	double factor = 1.0/nn[0];
	for (int off=0; off<ntot<<1; off++) {
            data[off] *= factor;
	}
	if (isign == 1) swap(data, nn[0]);
	return;
    } 
    
    /**
     * Swap the quadrants of data.
     *
     * @param data      array of data to be swapped.
     * @param nx        length of a row.
     */
    public static void swap (double[] data, int nx) {
        int i1,i2,j1,j2;
        double tempr;
        for (int ii1=0; ii1<nx*nx;ii1+=2*nx) {
            for (int ii2=0; ii2<nx; ii2+=2) {
                i1=ii1+ii2;
                i2=i1+nx;
                j1=i1+nx*nx+nx;
                j2=j1-nx;

                tempr = data[i1];
                data[i1]=data[j1];
                data[j1]=tempr;

                tempr = data[i2];
                data[i2]=data[j2];
                data[j2]=tempr;

                tempr = data[i1+1];
                data[i1+1]=data[j1+1];
                data[j1+1]=tempr;

                tempr = data[i2+1];
                data[i2+1]=data[j2+1];
                data[j2+1]=tempr;
            }
        }
        return;
    }
    
    /**
     *  Creates a Butterworth filter mask and store it as double array.
     *
     * @param width     width of the mask.
     * @param height    height of the mask.
     * @param radius    radius of the aperture.
     * @param c         value of aperture half-width.
     * @return  a double array with the mask.
     */
    public static double[] butterworthMask(int width, int height, int radius, double c) {
        double[] mask = new double[width*height];
        int hWidth = width >> 1;
        int hHeight = height >> 1;
        for (int j=0; j<height; j++)
            for (int i=0; i<width; i++) {
                mask[i+j*width] = (double) 1.0/(1+c*Math.pow(((double)((i-hWidth)*(i-hWidth)+(j-hHeight)*(j-hHeight))/(radius * radius)),8));	
            }
        return mask;
    }

    /**
     *  Creates an ImagePlus containing a Butterworth filter mask.
     *
     * @param width     width of the mask.
     * @param height    height of the mask.
     * @param radius    radius of the aperture.
     * @param c         value of aperture half-width.
     * @return an ImagePlus containing the mask.
     */
    public static ImagePlus butterworthMaskImage(int width, int height, int radius, double c, String title) {
        double[] mask = new double[width*height];
        int hWidth = width >> 1;
        int hHeight = height >> 1;
        for (int j=0; j<height; j++)
            for (int i=0; i<width; i++) {
                mask[i+j*width] = (double) 1.0 / ( 1.0 + c*Math.pow(( (double) ( (i-hWidth)*(i-hWidth)+(j-hHeight)*(j-hHeight) ) /( radius * radius )) ,8) );	
            }
        FloatProcessor fp = new FloatProcessor(width, height, mask);
        fp.resetMinAndMax();
        ImagePlus imp = new ImagePlus(title, fp);
        return imp;
    }
    
    /**
     *  Creates a circular mask and store it as double array.
     *
     * @param width     width of the mask.
     * @param height    height of the mask.
     * @param radius    radius of the aperture.
     * @return  a double array with the mask.
     */
    public static double[] circularMask(int width, int height, int radius){
        double[] mask = new double[width*height];	
        int hWidth = width >> 1;
        int hHeight = height >> 1;
        for (int j=0; j<height; j++)
            for (int i=0; i<width; i++) {
                int rad2 = (i-hWidth)*(i-hWidth)+(j-hHeight)*(j-hHeight);
                if (rad2 < radius*radius) mask[i+j*width] = 1.0;
                else mask[i+j*width] = 0.0;
            }
        return mask;
    }
	
   /**
     * Finds the maximum of the modulus of pixels of HoloJProcessor. 

     * @param processor	the HoloJProcessor.
     * @return the maximum position as a Point.
     */		
    public static Point getMaximumPosition(HoloJProcessor processor){
        double maxVal = Double.NEGATIVE_INFINITY;
        Point maxPos = new Point(0,0);
        double curVal = 0.0;
        double[] realPixels = processor.getRealPixelsArray();
        double[] complexPixels = processor.getComplexPixelsArray();
        int height = processor.getHeight();
        int width = processor.getWidth();
        for (int row = 0; row < height; row++)
            for (int col = 0; col < width; col++) {
                curVal = modulus(realPixels[row*width+col], complexPixels[row*width+col]);
                if (maxVal < curVal) {
                    maxVal = curVal;
                    maxPos.setLocation(col,row);									
                }	
            }
        return maxPos;
    }// getMaximumPosition()
	
   /**
     * Finds the maximum of the modulus of HoloJProcessor pixels in the region specified by the selection. 
     * 
     * @param processor         HoloJProcessor to use. 
     * @param selection		selected area of the pixels.
     * @return the maximum position.
     */		
    public static Point getMaximumPosition(HoloJProcessor processor, Roi selection){
        double maxVal = Double.NEGATIVE_INFINITY;
        Point maxPos = new Point(0,0);
        double curVal = 0.0;
        double[] realPixels = processor.getRealPixelsArray();
        double[] complexPixels = processor.getComplexPixelsArray();
        int height = processor.getHeight();
        int width = processor.getWidth();
        for (int row = 0; row < height; row++)
            for (int col = 0; col < width; col++) {
                if (((row >= selection.getBounds().y)&&
                    (row <= selection.getBounds().y+selection.getBounds().height)) &&
                    ((col >= selection.getBounds().x) &&
                    (col <= selection.getBounds().x+selection.getBounds().width))) {
                        curVal = modulus(realPixels[row*width+col], 
                                    complexPixels[row*width+col]);
                        if (maxVal < curVal) {
                            maxVal = curVal;
                            maxPos.setLocation(col,row);									
                        }	
                   }
            }
        return maxPos;
    }// getMaximumPosition()
	
   /**
    * Finds the position of the maximum lateral peak of the spectrum of HoloJProcessor.
    * The method search for the maximum of the spectrum excluding a circular region of radius 20 pixels centered around the origin of the spectrum and assuming the processor is in spectrum domain. 
    * 
    * @param processor	the HoloJProcessor.
    * @param side		the selected side of spectrum.
    * @return the sideband center position.
    */		
    public static Point getSidebandCenter(HoloJProcessor processor, int side) {
        if (processor.isSpectrumDomain() == false) 
            throw new ArrayStoreException("getSidebandCenter: image is not in Fourier domain.");
        double maxValue = Double.NEGATIVE_INFINITY;
        Point maxPos = new Point(0,0);
        double[] realPixels = processor.getRealPixelsArray();
        double[] complexPixels = processor.getComplexPixelsArray();
        int height = processor.getHeight();
        int width = processor.getWidth();
        int xC = width >> 1;
        int yC = height >> 1; 
        double curVal = 0;
        if ( side >= 0 ) {
            for (int row = 0; row < (height >> 1); row++)
                for (int col = 0; col < width; col++) {
                    curVal = modulus(realPixels[row*width+col], complexPixels[row*width+col]); 
                    if ((((row-xC)*(row-xC)+(col-yC)*(col-yC) ) > EXCLUDED_RADIUS*EXCLUDED_RADIUS ) && ( maxValue < curVal )) {
                        maxValue = curVal;
                        maxPos.setLocation(col,row);	
                    }
                }
        } else {
            for (int row = height >> 1; row < height; row++)
                for (int col = 0; col < width; col++) {
                    curVal = modulus(realPixels[row*width+col], 
                            complexPixels[row*width+col]); 
                    if ((((row-xC)*(row-xC) +(col-yC)*(col-yC) ) > EXCLUDED_RADIUS*EXCLUDED_RADIUS ) && ( maxValue < curVal )) {
                        maxValue = curVal;
                        maxPos.setLocation(col,row);	
                    }
                }
        }
        return maxPos;	
    }// getSidebandCenter()
        
   /**
    * Adds the complex number represented by (realValue, complexValue) to real and complex pixels of HoloJProcessor operand.
    *
    * @param operand            HoloJProcessor.
    * @param realValue          real part of operand.
    * @param complexValue       complex part of operand.
    * @return the resulting HoloJProcessor.
    */
    public static HoloJProcessor sum(HoloJProcessor operand, double realValue, double complexValue){
            double[] realPixels = operand.getRealPixelsArray();
            double[] complexPixels = operand.getComplexPixelsArray();
            int size = operand.getSize();
            for (int i=0; i<size; i++) {
                    realPixels[i] += realValue;
                    complexPixels[i] += complexValue;	
            }
            HoloJProcessor result = new HoloJProcessor(realPixels, complexPixels, operand.getWidth(), operand.getHeight());
            return result;
    }// sum()
        
   /**
    * Subtract the complex number represented by (realValue, complexValue) from real and complex pixels of HoloJProcessor operand.
    *
    * @param operand            HoloJProcessor.
    * @param realValue          real part of operand.
    * @param complexValue       complex part of operand.
    * @return the resulting HoloJProcessor.
    */
    public static HoloJProcessor subtract(HoloJProcessor operand, double realValue, double complexValue){
        double[] realPixels = operand.getRealPixelsArray();
        double[] complexPixels = operand.getComplexPixelsArray();
        int size = operand.getSize();
        for (int i=0; i<size; i++) {
                realPixels[i] -= realValue;
                complexPixels[i] -= complexValue;	
        }
        HoloJProcessor result = new HoloJProcessor(realPixels, complexPixels, operand.getWidth(), operand.getHeight());
        return result;
    }// subtract()
        
   /**
    *	Multiplies thbye scalar value the real and complex pixels of HoloJProcessor operand ad return the result in a new HoloJProcessor.
    *
    *	@param operand		HoloJProcessor.
    *       @param value            scalar value multiplier.
    *       @return the resulting HoloJProcessor.
    */
    public static HoloJProcessor multiply(HoloJProcessor operand, double value){
        double[] realPixels = operand.getRealPixelsArray();
        double[] complexPixels = operand.getComplexPixelsArray();
        int size = operand.getSize();
        for (int i=0; i<size; i++) {
                realPixels[i] *= value;
                complexPixels[i] *= value;	
        }	
        HoloJProcessor result = new HoloJProcessor(realPixels, complexPixels, operand.getWidth(), operand.getHeight());
        return result;
    }
   	
   /**
    *	Divide by scalar value the real and complex pixels of HoloJProcessor operand and returns the result in a new HoloJProcessor.
    *
    *	@param operand		HoloJProcessor.
    *       @param value            scalar value multiplier.
    *       @return the resulting HoloJProcessor.
    */
    public static HoloJProcessor divide(HoloJProcessor operand, double value){
        double[] realPixels = operand.getRealPixelsArray();
        double[] complexPixels = operand.getComplexPixelsArray();
        int size = operand.getSize();
        for (int i=0; i<size; i++) {
                realPixels[i] /= value;
                complexPixels[i] /= value;	
        }	
        HoloJProcessor result = new HoloJProcessor(realPixels, complexPixels, operand.getWidth(), operand.getHeight());
        return result;
    }
        
   /**
    * Adds the complex pixels of operand1 HoloJProcessor to real and complex pixels of operand2 and returns the resulting HoloJProcessor.
    * 
    * 
    * 
    * @param operand1	the first HoloJProcessor operand.
    * @param operand2       the second HoloJProcessor operand.
    * @return the resulting HoloJProcessor.
    */  
    public static HoloJProcessor sum(HoloJProcessor operand1, HoloJProcessor operand2){
        if (operand1.getSize() != operand2.getSize()) 
            throw new IndexOutOfBoundsException("sum: sizes must be equal.");
        double[] realPixels1 = operand1.getRealPixelsArray();
        double[] realPixels2 = operand2.getRealPixelsArray();
        double[] complexPixels1 = operand1.getComplexPixelsArray();
        double[] complexPixels2 = operand2.getComplexPixelsArray();
        double[] resultReal = new double[operand1.getSize()];
        double[] resultComplex = new double[operand1.getSize()];
        for (int i=0; i<operand1.getSize(); i++) {
            resultReal[i]=realPixels1[i]+realPixels2[i];
            resultComplex[i]=complexPixels1[i]+complexPixels2[i];
        }
        HoloJProcessor result = new HoloJProcessor(resultReal, resultComplex, operand1.getWidth(), operand1.getHeight());
        return result;
    }
   	
   /**
    * Subtracts the complex pixels of operand HoloJProcessor to real and complex pixels.
    * 
    * @param operand1	the HoloJProcessor first operand.
    * @param operand2       the HoloJProcessor second operand.
    * @return the resulting HoloJProcessor.
    */  
    public static HoloJProcessor subtract(HoloJProcessor operand1, HoloJProcessor operand2){
        if (operand1.getSize() != operand2.getSize()) 
            throw new IndexOutOfBoundsException("subtract: sizes must be equal.");
        double[] realPixels1 = operand1.getRealPixelsArray();
        double[] realPixels2 = operand2.getRealPixelsArray();
        double[] complexPixels1 = operand1.getComplexPixelsArray();
        double[] complexPixels2 = operand2.getComplexPixelsArray();
        double[] resultReal = new double[operand1.getSize()];
        double[] resultComplex = new double[operand1.getSize()];
        for (int i=0; i<operand1.getSize(); i++) {
            resultReal[i]=realPixels1[i]-realPixels2[i];
            resultComplex[i]=complexPixels1[i]-complexPixels2[i];
        }
        HoloJProcessor result = new HoloJProcessor(resultReal, resultComplex, operand1.getWidth(), operand1.getHeight());
        return result;	
    }

   /**
    * Returns an HoloJProcessor result from the complex multiplication of operand1 pixels for operand2 pixels.
    * 
    * @param operand1       the HoloJProcessor first operand.
    * @param operand2       the HoloJProcessor second operand.
    * @return the resulting HoloJProcessor.
    */  
    public static HoloJProcessor multiply(HoloJProcessor operand1, HoloJProcessor operand2){
            if (operand1.getSize() != operand2.getSize()) 
                    throw new IndexOutOfBoundsException("multiply: sizes must be equal.");
            double[] realPixels1 = operand1.getRealPixelsArray();
            double[] realPixels2 = operand2.getRealPixelsArray();
            double[] complexPixels1 = operand1.getComplexPixelsArray();
            double[] complexPixels2 = operand2.getComplexPixelsArray();
            double[] resultReal = new double[operand1.getSize()];
            double[] resultComplex = new double[operand1.getSize()];
            for (int i=0; i<operand1.getSize(); i++) {
                    resultReal[i] = realPixels1[i]*realPixels2[i]-complexPixels1[i]*complexPixels2[i];
                    resultComplex[i] = realPixels1[i]*complexPixels2[i]+realPixels2[i]*complexPixels1[i];	
            }
            HoloJProcessor result = new HoloJProcessor(resultReal, resultComplex, operand1.getWidth(), operand1.getHeight());
            return result;
    }
   	
   /**
    * Returns the HoloJProcessor result of the complex division of operand1 by operand2.
    * 
    * @param operand1       the HoloJProcessor first operand.
    * @param operand2       the HoloJProcessor second operand.
    * @return the resulting HoloJProcessor.
    */  
    public static HoloJProcessor divide(HoloJProcessor operand1, HoloJProcessor operand2){
        if (operand1.getSize() != operand2.getSize()) 
            throw new IndexOutOfBoundsException("divide: sizes must be equal.");
        double[] realPixels1 = operand1.getRealPixelsArray();
        double[] realPixels2 = operand2.getRealPixelsArray();
        double[] complexPixels1 = operand1.getComplexPixelsArray();
        double[] complexPixels2 = operand2.getComplexPixelsArray();
        double[] resultReal = new double[operand1.getSize()];
        double[] resultComplex = new double[operand1.getSize()];
        for (int i=0; i<operand1.getSize(); i++) {
            resultReal[i] = (realPixels1[i]*realPixels2[i]+complexPixels1[i]*complexPixels2[i])/
                          (realPixels2[i]*realPixels2[i]+complexPixels2[i]*complexPixels2[i]);
            resultComplex[i] = (realPixels2[i]*complexPixels1[i]-realPixels1[i]*complexPixels2[i])/
                          (realPixels2[i]*realPixels2[i]+complexPixels2[i]*complexPixels2[i]);	
        }
        HoloJProcessor result = new HoloJProcessor(resultReal, resultComplex, operand1.getWidth(), operand1.getHeight());
        return result;
    }
    /**
     * Returns an HoloJProcessor containing reconstructed data.
     * 
     * 
     * 
     * @param radius			radius of reconstructed frequency region.
     * @param scaleFactor		scale factor of final reconstructed image.
     * @param side			upper or lower region.
     * @param hologram                  hologram to be reconstructed.
     * @param useButterworth            flag for using soft aperture.
     * @return the reconstructed image.
     */
    public static HoloJProcessor reconstruct(int radius, int scaleFactor, Point sideCenter, HoloJProcessor hologram, boolean useButterworth) {
            hologram.doFFT();
            HoloJProcessor holoRec = hologram.getSideband(sideCenter,radius,scaleFactor,useButterworth);
            holoRec.doInverseFFT();
            return holoRec;
    }
    /**
     * Returns an HoloJProcessor containing reconstructed data from hologram and reference.
     * 
     * 
     * 
     * @param radius			radius of reconstructed frequency region.
     * @param scaleFactor		scale factor of final reconstructed image.
     * @param side			upper or lower region.
     * @param hologram                  hologram to be reconstructed.
     * @param reference                 hologram used as reference in void.
     * @param useButterworth            flag for using soft aperture.
     * @return the reconstructed image.
     */
    public static HoloJProcessor reconstruct(int radius, int scaleFactor,Point sideCenter, HoloJProcessor hologram, HoloJProcessor reference, boolean useButterworth) {
        hologram.doFFT();
        reference.doFFT();

        HoloJProcessor holoRec = hologram.getSideband(sideCenter,radius,scaleFactor,useButterworth);
        HoloJProcessor refRec = reference.getSideband(sideCenter,radius,scaleFactor,useButterworth);

        holoRec.doInverseFFT();
        refRec.doInverseFFT();

        HoloJProcessor result = HoloJUtils.divide(holoRec,refRec);

        return result;
    }
    
    /**
     *  Calculates the cosine of pixels in source.
     *
     *  @param source   the source ImaeProcessor.
     *  @return a FloatProcessor containing the cosine.
     */
    public static FloatProcessor makeCosProcessor(ImageProcessor source){
        float[] srcPixels = (float[]) source.convertToFloat().getPixels();
        int size = source.getWidth()*source.getHeight();
        double[] resPixels = new double[size];
        for(int i=0; i<size; i++) resPixels[i]=Math.cos(srcPixels[i]);
        return new FloatProcessor(source.getWidth(),source.getHeight(),resPixels);
    }
    
    /**
     *  Calculates the sine of pixels in source.
     *
     *  @param source   the source ImaeProcessor.
     *  @return a FloatProcessor containing the sine.
     */    
    public static FloatProcessor makeSinProcessor(ImageProcessor source){
        float[] srcPixels = (float[]) source.convertToFloat().getPixels();
        int size = source.getWidth()*source.getHeight();
        double[] resPixels = new double[size];
        for(int i=0; i<size; i++) resPixels[i]=Math.sin(srcPixels[i]);
        return new FloatProcessor(source.getWidth(),source.getHeight(),resPixels);
    }
        
    /**
     *  Makes a new ImageProcessor by mirroring the source processor.
     *
     *  @param source   source ImageProcessor.
     *  @return the symmetric ImageProcessor.
     */
    public static ImageProcessor makeSimmetricProcessor(ImageProcessor source){
        int newWidth = source.getWidth()<<1;
        int newSize = source.getWidth()*source.getHeight()<<2;
        double[] resPixels = new double[newSize];
        float[] srcPixels = (float[]) source.convertToFloat().getPixels();
        int width = source.getWidth();

        for (int row = 0; row < source.getHeight(); row++)
            for (int col = 0; col < source.getWidth(); col++) {
                int pos1 = (newWidth-col-1) + (row)*newWidth;
                int pos2 = col+(newWidth-row-1)*newWidth;
                int pos3 = (newWidth-col-1) + (newWidth-row-1)*newWidth;
                int pos4 = col + row*newWidth;
                int pos0 = col + row*width;
                resPixels[pos4] = srcPixels[pos0];
                resPixels[pos3] = srcPixels[pos0];
                resPixels[pos2] = srcPixels[pos0];
                resPixels[pos1] = srcPixels[pos0];
            }
        return new FloatProcessor(newWidth,newWidth,resPixels);
    }
    
    /**
     *  Renormalize the dynamic range of the source ImageProcessor to the range 2Pi.
     *  
     *  @param source   source ImageProcessor.
     *  @return a renormalized ImageProcessor.
     */
    public static ImageProcessor getPhaseRenormalizedProcessor(ImageProcessor source){
        int size = source.getWidth()*source.getHeight();
        double[] resPixels = new double[size];
        double min = (double) source.getMin();
        double max = (double) source.getMax();
        float[] srcPixels = (float[]) source.convertToFloat().getPixels();
        for (int i = 0; i < size; i++)
            resPixels[i] = (srcPixels[i]-min)/(max-min)*Math.PI*2;
        return new FloatProcessor(source.getWidth(),source.getHeight(),resPixels);
    }
        
    /**
     *  Creates a parabolic mask (intensity=x^2+x^y).
     *
     *  @param width        width of the mask.
     *  @param height       height of the mask.
     *  @return the ImageProcessor mask.
     */
    public static ImageProcessor makeParabolicMask(int width, int height) {
        double[] resPixels = new double[width*height];
        int xc = width>>1;
        int yc = height>>1;
        for (int row = 0; row < height; row++)
            for (int col = 0; col < width; col++)
                resPixels[col+row*width] = (col-xc)*(col-xc) + (row-yc)*(row-yc);
        return new FloatProcessor(width,height,resPixels);
    }
    
    /**
     *  Creates an inverse parabolic mask (intensity=1/(x^2+x^y)).
     *
     *  @param width        width of the mask.
     *  @param height       height of the mask.
     *  @return the ImageProcessor mask.
     */    
    public static ImageProcessor makeInverseParabolicMask(int width, int height) {
        int size = width*height;
        double[] resPixels = new double[size];
        int xc = width>>1;
        int yc = height>>1;
        for (int row = 0; row < height; row++)
            for (int col = 0; col < width; col++) {
                 double fac = (col-xc)*(col-xc) + (row-yc)*(row-yc);
                 if (fac == 0.0) resPixels[col+row*width] = 0.0;
                 else resPixels[col+row*width] = 1 / fac;
            }
        return new FloatProcessor(width,height,resPixels);
    }
        
    /**
     *  Multiply each pixel ImageProcessor for pixels of another ImageProcessor one-by-one.
     *
     *  @param op1      ImageProcessor 1.
     *  @param op2      ImageProcessor 2.
     *  @return the resulting ImageProcessor. 
     */
    public static FloatProcessor multiply (ImageProcessor op1, ImageProcessor op2){
        if ( (op1.getWidth()!=op2.getWidth()) || (op1.getHeight()!=op2.getHeight() ) ) 
            throw new IndexOutOfBoundsException("multiply: sizes must be equal.");
        int size = op1.getWidth()*op2.getHeight();
        float[] op1Pixels = (float[]) op1.convertToFloat().getPixels();
        float[] op2Pixels = (float[]) op2.convertToFloat().getPixels();
        double[] resPixels = new double[size];
        for (int i = 0; i < size; i++)
            resPixels[i] = op1Pixels[i]*op2Pixels[i];
        return new FloatProcessor(op1.getWidth(),op1.getHeight(),resPixels);
    }
    
    /**
     *  Multiply each pixel of ImageProcessor for the specified scalar factor.
     *
     *  @param factor   scalar factor.
     *  @param op1      ImageProcessor.
     *  @return the resulting FloatProcessor. 
     */    
    public static FloatProcessor multiply (double factor, ImageProcessor op1){
        int size = op1.getWidth()*op1.getHeight();
        float[] op1Pixels = (float[]) op1.convertToFloat().getPixels();
        double[] resPixels = new double[size];
        for (int i = 0; i < size; i++)
            resPixels[i] = op1Pixels[i]*factor;
        return new FloatProcessor(op1.getWidth(),op1.getHeight(),resPixels);
    }
    /**
     *  Divide pixels of ImageProcessor by pixels of another ImageProcessor one-by-one.
     *
     *  @param op1      ImageProcessor 1.
     *  @param op2      ImageProcessor 2.
     *  @return the resulting ImageProcessor. 
     */
    public static FloatProcessor divide (ImageProcessor op1, ImageProcessor op2){
        if ( (op1.getWidth()!=op2.getWidth()) || (op1.getHeight()!=op2.getHeight() ) ) 
            throw new IndexOutOfBoundsException("divide: sizes must be equal.");
        int size = op1.getWidth()*op2.getHeight();
        float[] op1Pixels = (float[]) op1.convertToFloat().getPixels();
        float[] op2Pixels = (float[]) op2.convertToFloat().getPixels();
        double[] resPixels = new double[size];
        for (int i = 0; i < size; i++)
            resPixels[i] = op1Pixels[i]/op2Pixels[i];
        return new FloatProcessor(op1.getWidth(),op1.getHeight(),resPixels);
    }
    
    /**
     *  Add each pixel ImageProcessor to pixels of another ImageProcessor one-by-one.
     *
     *  @param op1      ImageProcessor 1.
     *  @param op2      ImageProcessor 2.
     *  @return the resulting ImageProcessor. 
     */    
    public static FloatProcessor add (ImageProcessor op1, ImageProcessor op2){
        if ( (op1.getWidth()!=op2.getWidth()) || (op1.getHeight()!=op2.getHeight() ) ) 
            throw new IndexOutOfBoundsException("add: sizes must be equal.");
        int size = op1.getWidth()*op2.getHeight();
        float[] op1Pixels = (float[]) op1.convertToFloat().getPixels();
        float[] op2Pixels = (float[]) op2.convertToFloat().getPixels();
        double[] resPixels = new double[size];
        for (int i = 0; i < size; i++)
            resPixels[i] = op1Pixels[i]+op2Pixels[i];
        return new FloatProcessor(op1.getWidth(),op1.getHeight(),resPixels);
    }
      
    /**
     *  Subtract each pixel ImageProcessor to pixels of another ImageProcessor one-by-one.
     *
     *  @param op1      ImageProcessor 1.
     *  @param op2      ImageProcessor 2.
     *  @return the resulting ImageProcessor. 
     */
    public static FloatProcessor subtract (ImageProcessor op1, ImageProcessor op2){
        if ( (op1.getWidth()!=op2.getWidth()) || (op1.getHeight()!=op2.getHeight() ) ) 
            throw new IndexOutOfBoundsException("subtract: sizes must be equal.");
        int size = op1.getWidth()*op2.getHeight();
        float[] op1Pixels = (float[]) op1.convertToFloat().getPixels();
        float[] op2Pixels = (float[]) op2.convertToFloat().getPixels();
        double[] resPixels = new double[size];
        for (int i = 0; i < size; i++)
            resPixels[i] = op1Pixels[i]-op2Pixels[i];
        return new FloatProcessor(op1.getWidth(),op1.getHeight(),resPixels);
    }
        
    /**
     *  Reset the minimum of FloatProcessor to zero.
     *
     *  @param ip   source FloatProcessor.
     */
    public static void resetMin(FloatProcessor ip){
        float min = Float.POSITIVE_INFINITY;
        float[] pix = (float[]) ip.getPixels();
        int size = ip.getHeight()*ip.getWidth();
        for (int i = 0; i < size; i++) {
            if (pix[i]<min) min = pix[i];
        }
        for (int i = 0; i < size; i++) {
            pix[i]=pix[i]-min;
        }
    }
       
    /**
     *  Round each pixel to nearby integer value.
     *
     *  @param ip   source FloatProcessor. 
     */
    public static void round (FloatProcessor ip){
        float[] pix = (float[]) ip.getPixels();
        for (int i=0;i<pix.length;i++) pix[i]=Math.round(pix[i]);
    }
    
    /**
     *  Add to the source HoloJProcessor the specified phase in radians.
     *
     *  @param processor        an HoloJProcessor.
     *  @param angle            radians to add.
     *  @return the resulting HoloJProcessor.
     */
    public static HoloJProcessor addPhase(HoloJProcessor processor, double angle){
        double realAdd = Math.cos(angle);
        double complexAdd = Math.sin(angle);
        double[] realPixels = processor.getRealPixelsArray();
        double[] complexPixels = processor.getComplexPixelsArray();
        for (int i=0; i<processor.getSize(); i++){
            realPixels[i] += realAdd;
            complexPixels[i] += complexAdd;
        }
        return new HoloJProcessor(realPixels, complexPixels, processor.getWidth(), processor.getHeight());
    }
    
    /**
     *  Add to the specified HoloJProcessor a phase plate stored in a double array.
     *
     *  @param  processor       the HoloJProcessor.
     *  @param  plateArray      a phase plate.
     *  @return the resulting HoloJProcessor.
     */
    public HoloJProcessor addPhasePlate(HoloJProcessor processor, double[] plateArray){
        if (processor.getSize() != plateArray.length) 
            throw new IndexOutOfBoundsException("addPhasePlate: size of the phase-plate must be the same of HoloJProcessor.");
        double[] realPixels = processor.getRealPixelsArray();
        double[] complexPixels = processor.getComplexPixelsArray();
        for (int i=0; i<processor.getSize(); i++) {
            realPixels[i]+=Math.cos(plateArray[i]);
            complexPixels[i]+=Math.sin(plateArray[i]);
        }
        return new HoloJProcessor(realPixels, complexPixels, processor.getWidth(), processor.getHeight());
    }
} 