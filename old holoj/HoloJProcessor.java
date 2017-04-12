package holoj;

import ij.*;
import ij.gui.*;
import ij.measure.Calibration;
import ij.process.*;
import java.awt.*;

/**
 * Class HoloJProcessor contains data and methods to provide high level access and manipulation to complex valued pixel data.
 *
 * @author Luca Ortolani and Pier Francesco Fazzini
 * @version 1.0
 */

    /* DISCLAIMER
    Permission to use the software and accompanying documentation provided on these pages for educational, 
    research, and not-for-profit purposes, without fee and without a signed licensing agreement, is hereby
    granted, provided that the above copyright notice, this paragraph and the following two paragraphs 
    appear in all copies. The copyright holder is free to make upgraded or improved versions of the 
    software available for a fee or commercially only.

    IN NO EVENT SHALL THE COPYRIGHT HOLDER BE LIABLE TO ANY PARTY FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, 
    OR CONSEQUENTIAL DAMAGES, OF ANY KIND WHATSOEVER, ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS 
    DOCUMENTATION, EVEN IF HE HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

    THE COPYRIGHT HOLDER SPECIFICALLY DISCLAIMS ANY WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
    WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE AND ACCOMPANYING 
    DOCUMENTATION IS PROVIDED "AS IS". THE COPYRIGHT HOLDER HAS NO OBLIGATION TO PROVIDE MAINTENANCE, 
    SUPPORT, UPDATES, ENHANCEMENTS, OR MODIFICATIONS.
    */

public class HoloJProcessor {
	
    // radius of the excluded region in sideband center search.
    private static final int EXCLUDED_RADIUS = 20;

    // fields for storing image data.	
    private double realPixels[] = null;
    private double complexPixels[] = null;

    // fields for image size.
    private int width = 0;
    private int height = 0;
    private int size = 0;
    
    // image calibration.
    private Calibration cal;
    
    // HoloJ processor title.
    private String title = null;

    // flags to store info on FFT operation.
    private boolean isRealOrigin = true;
    private boolean isSpectrumDomain = false;
    
   /* ************************************************************ *
    *
    *	CONSTRUCTORS
    *
    * ************************************************************ */
   	
   /**
    * Creates a new object of the class HoloJProcessor.
    *
    * The size of the image are given as parameter.
    * The data pixels are empty and are not initialized.
    *
    * @param width      the size of the image along the X-axis
    * @param height     the size of the image along the Y-axis
    */
    public HoloJProcessor(int width, int height) {
    	if (width < 1)
            throw new 
                ArrayStoreException("Constructor: width < 1.");
        if (height < 1)
            throw new 
                ArrayStoreException("Constructor: height < 1.");
        this.width = width;
        this.height = height;
        size = width*height;
        realPixels = new double[size];
        complexPixels = new double[size];
        setComplexOrigin();
    } // Constructor (int width, int height)
    
   /** 
    * Creates a new object of the class HoloJProcessor.
    *
    * Data is retrieved from ImageProcessor given as argument.
    * ImageProcessor data fill the real pixels data, while complex
    * data is empty and not initialized.
    *
    * @param ip	ImageProcessor providing real data value.
    */
    public HoloJProcessor(ImageProcessor ip) {
        if (ip == null) 
            throw new 
                ArrayStoreException("Constructor: ImageProcessor == null.");
        width = ip.getWidth();
        height = ip.getHeight();
        size = width*height;
        realPixels = new double[size];
        complexPixels = new double[size];
        if (ip.getPixels() instanceof byte[]) {
            byte[] bsrc = (byte[])ip.getPixels();
            for (int k=0; k<size; k++)
                realPixels[k] = (double)(bsrc[k] & 0xFF);
            setRealOrigin();
        } else if (ip.getPixels() instanceof short[]) {
            short[] ssrc = (short[])ip.getPixels();
            for (int k=0; k<size; k++)
                realPixels[k] = (double)(ssrc[k] & 0xFFFF);
            setRealOrigin();
        } else if (ip.getPixels() instanceof float[]) {
                 float[] fsrc = (float[])ip.getPixels();
                 for (int k=0; k<size; k++)
                        realPixels[k] = (double)fsrc[k];
                 setRealOrigin();
        } else  {
                throw new 
                        ArrayStoreException("Constructor: Unexpected image type.");
        }
    } // Constructor (ImageProcessor ip)

   /**
    * Creates a new object of the class HoloJProcessor, starting from data contained in the two ImageProcessors provided.
    *
    * @param realIp     ImageProcessor providing real data value.
    * @param complexIp	ImageProcessor providing complex data value.
    */
    public HoloJProcessor(ImageProcessor realIp, ImageProcessor complexIp) {
	if ((realIp == null) || (complexIp == null)) 
            throw new 
		ArrayStoreException("Constructor: ImageProcessor == null.");
	// check for processor size.
	width = realIp.getWidth();
	height = realIp.getHeight();
	size = width*height;
	int cwidth = complexIp.getWidth();
	int cheight = complexIp.getHeight();
	int csize = width*height;
	if ((cwidth != width) || (cheight != height) || (csize != size))
            throw new
		ArrayStoreException("Constructor: Real and Complex part differ in size.");
	
	realPixels = new double[size];
	complexPixels = new double[size];
        setComplexOrigin();
	// filling real data pixels
	if (realIp.getPixels() instanceof byte[]) {
            byte[] bsrc = (byte[])realIp.getPixels();
            for (int k=0; k<size; k++)
                realPixels[k] = (double)(bsrc[k] & 0xFF);
	} else if (realIp.getPixels() instanceof short[]) {
            short[] ssrc = (short[])realIp.getPixels();
            for (int k=0; k<size; k++)
                realPixels[k] = (double)(ssrc[k] & 0xFFFF);
	} else if (realIp.getPixels() instanceof float[]) {
            float[] fsrc = (float[])realIp.getPixels();
            for (int k=0; k<size; k++)
                realPixels[k] = (double)fsrc[k];
	} else {
            throw new 
		ArrayStoreException("Constructor: Unexpected image type in real part.");
	}		
        // filling complex pixels data
	if (complexIp.getPixels() instanceof byte[]) {
            byte[] bsrc = (byte[])complexIp.getPixels();
            	for (int k=0; k<size; k++)
                    complexPixels[k] = (double)(bsrc[k] & 0xFF);
	} else if (complexIp.getPixels() instanceof short[]) {
            short[] ssrc = (short[])complexIp.getPixels();
            for (int k=0; k<size; k++)
                complexPixels[k] = (double)(ssrc[k] & 0xFFFF);
	} else if (complexIp.getPixels() instanceof float[]) {
            float[] fsrc = (float[])complexIp.getPixels();
            for (int k=0; k<size; k++)
                complexPixels[k] = (double)fsrc[k];
	} else {
            throw new 
                ArrayStoreException("Constructor: Unexpected image type in complex part.");
	}	
	} // Constructor (ImageProcessor realIp, ImageProcessor complexIp)
	
   /**
    * Creates a new object of the HoloJProcessor class.
    * Data is provided by two double array containing real and complex pixels.
    * 
    * @param realPixels		real pixels array.
    * @param complexPixels 	complex pixels array.
    * @param width		image width.
    * @param height		image height.
    */
    public HoloJProcessor(double[] realPixels, double[] complexPixels, int width, int height) {
        if (realPixels.length != complexPixels.length) {
            throw new
                ArrayStoreException("Constructor: real and imaginary part differ in size.");
	} else {
            this.realPixels = realPixels;
            this.complexPixels = complexPixels;
            this.width = width;
            this.height = height;
            this.size = width*height;
            setComplexOrigin();
        }
    }// Constructor (double[] realPixels, double[] complexPixels, int width, int height)
	
   /**
    * Creates a new object of the HoloJProcessor class.
    * Data is provided by double array containing real pixels.
    * 
    * @param realPixels		real pixels array.
    * @param width              image width.
    * @param height             image height.
    */
    public HoloJProcessor(double[] realPixels, int width, int height) {
        this.realPixels = realPixels;
        this.width = width;
        this.height = height;
        this.size = width*height;
        setRealOrigin();
    } // HoloJProcessor(double[] realPixels, int width, int height)
	
   /* ************************************************************ *
    *
    *	METHODS :: General : Data accessors and modifiers
    *
    * ************************************************************ */
   
   /**
    *	Returns a double array containing the real pixels in row major mode.
    *
    *	@return the pixels array.
    */
    public double[] getRealPixelsArray() {
        return realPixels;
    }// getRealPixelsArray()

   /**
    *	Returns a double array containing the complex pixels in row major mode.
    *
    *	@return the pixels array.
    */   	
    public double[] getComplexPixelsArray() {
        return complexPixels;
    }// getComplexPixelsArray()
        
   /**
    *	Sets real pixels from a double array of data in row major mode.
    *
    *	@param pixels   the pixels array.
    */
    public void setRealPixelsArray(double[] pixels) {
        realPixels = pixels;
    }// setRealPixelsArray()

   /**
    *	Sets complex pixels from a double array of data in row major mode.
    *
    *	@param pixels   the pixels array.
    */ 	
    public void setComplexPixelsArray(double[] pixels) {
        complexPixels = pixels;
    }// setComplexPixelsArray()

   /**
    *	Returns the size of the image.
    *
    *	@return the image size.
    */
    public int getSize(){
        return size;	
    } // getSize()
	
   /**
    *	Returns the width of the image.
    *
    *	@return the image width.
    */
    public int getWidth(){
        return width;	
    }// getWidth
	
   /**
    *	Returns the height of the image.
    *
    *	@return the image height.
    */
    public int getHeight(){
        return height;	
    }// getHeight
   	
   /**
    *	Returns true if the image has being Fourier transformed.
    *
    *	@return	true if image is in spectrum domain.
    */
    public boolean isSpectrumDomain() {
        return isSpectrumDomain;	
    }

   /**
    * Adds the complex pixels of operand HoloJProcessor to real and complex pixels.
    * 
    * @param operand		theHoloJProcessorr operand.
    */  
    public void add(HoloJProcessor operand){
        if (size != operand.getSize()) 
            throw new IndexOutOfBoundsException("add: sizes must be equal.");
        double[] opRealPixels = operand.getRealPixelsArray();
        double[] opComplexPixels = operand.getComplexPixelsArray();
        for (int i=0; i<size; i++) {
            realPixels[i] = realPixels[i]+opRealPixels[i];
            complexPixels[i] = complexPixels[i]+opComplexPixels[i];	
        }	
        setComplexOrigin();
    }
   	
   /**
    * Subtracts the complex pixels of operand HoloJProcessor to real and complex pixels.
    * 
    * @param operand		theHoloJProcessorr operand.
    */  
    public void subtract(HoloJProcessor operand){
        if (size != operand.getSize()) 
            throw new IndexOutOfBoundsException("subtract: sizes must be equal.");
        double[] opRealPixels = operand.getRealPixelsArray();
        double[] opComplexPixels = operand.getComplexPixelsArray();
        for (int i=0; i<size; i++) {
            realPixels[i] = realPixels[i]-opRealPixels[i];
            complexPixels[i] = complexPixels[i]-opComplexPixels[i];	
        }
        setComplexOrigin();
    }

   /**
    * Multiplies real and complex pixels by the complex pixels of operand HoloJProcessor.
    * 
    * @param operand		theHoloJProcessorr operand.
    */  
    public void multiply(HoloJProcessor operand){
        if (size != operand.getSize()) 
            throw new IndexOutOfBoundsException("multiply: sizes must be equal.");
        double[] opRealPixels = operand.getRealPixelsArray();
        double[] opComplexPixels = operand.getComplexPixelsArray();
        for (int i=0; i<size; i++) {
            double real = realPixels[i]*opRealPixels[i]-complexPixels[i]*opComplexPixels[i];
            double complex = realPixels[i]*opComplexPixels[i]+opRealPixels[i]*complexPixels[i];
            realPixels[i] = real;
            complexPixels[i] = complex;	
        }	
        setComplexOrigin();
    }
    
   /**
    * Multiplies real and complex pixels by the scalar pixels of operand ImageProcessor.
    * 
    * @param operand		the ImageProcessor operand.
    */      
    public void multiply(ImageProcessor operand){
        int opWidth = operand.getWidth();
        int opHeight = operand.getHeight();
        int opSize = opWidth*opHeight;
        if (size != opSize) 
            throw new IndexOutOfBoundsException("multiply: sizes must be equal.");
        float[] opPixels = (float[]) operand.convertToFloat().getPixels();
        for (int i = 0; i < size; i++){
            realPixels[i] *= opPixels[i];
            complexPixels[i] *= opPixels[i];
        }
        setComplexOrigin();
    }
    
   /**
    * Add to real pixels the real pixels of operand ImageProcessor.
    * 
    * @param operand		the ImageProcessor operand.
    */      
    public void add(ImageProcessor operand){
        int opWidth = operand.getWidth();
        int opHeight = operand.getHeight();
        int opSize = opWidth*opHeight;
        if (size != opSize) 
            throw new IndexOutOfBoundsException("add: sizes must be equal.");
        float[] opPixels = (float[]) operand.convertToFloat().getPixels();
        for (int i = 0; i < size; i++){
            realPixels[i] += opPixels[i];
        }
        setComplexOrigin();
    }
   /**
    * Subtract to real pixels, the real pixels of operand ImageProcessor.
    * 
    * @param operand		the ImageProcessor operand.
    */       
    public void subtract(ImageProcessor operand){
        int opWidth = operand.getWidth();
        int opHeight = operand.getHeight();
        int opSize = opWidth*opHeight;
        if (size != opSize) 
            throw new IndexOutOfBoundsException("subtract: sizes must be equal.");
        float[] opPixels = (float[]) operand.convertToFloat().getPixels();
        for (int i = 0; i < size; i++){
            realPixels[i] -= opPixels[i];
        }
        setComplexOrigin();
    }
    /**
    * Divide the real and complex pixels by the real pixels of operand ImageProcessor.
    * 
    * @param operand		the ImageProcessor operand.
    */      
    public void divide(ImageProcessor operand){
        int opWidth = operand.getWidth();
        int opHeight = operand.getHeight();
        int opSize = opWidth*opHeight;
        if (size != opSize) 
            throw new IndexOutOfBoundsException("subtract: sizes must be equal.");
        float[] opPixels = (float[]) operand.convertToFloat().getPixels();
        for (int i = 0; i < size; i++){
            realPixels[i] /= opPixels[i];
            complexPixels[i] /= opPixels[i];
        }
        setComplexOrigin();
    }
 
   /**
    * Multiply complex pixels by scalar data in operand array.
    * 
    * @param operandArray   the array operand.
    */   
    public void multiply(double[] operandArray){
        int opSize = operandArray.length;
        if (size != opSize) 
            throw new IndexOutOfBoundsException("multiply: sizes must be equal.");
        for (int i = 0; i < size; i++){
            realPixels[i] *= operandArray[i];
            complexPixels[i] *= operandArray[i];
        }
        setComplexOrigin();
    }
   /**
    * Divide complex pixels by scalar data in operand array.
    * 
    * @param operandArray   the array operand.
    */ 
    public void divide(double[] operandArray){
        int opSize = operandArray.length;
        if (size != opSize) 
            throw new IndexOutOfBoundsException("divide: sizes must be equal.");
        for (int i = 0; i < size; i++){
            realPixels[i] /= operandArray[i];
            complexPixels[i] /= operandArray[i];
        }
        setComplexOrigin();
    }
    /**
    * Add to complex pixels the scalar data in operand array.
    * 
    * @param operandArray   the array operand.
    */ 
    public void add(double[] operandArray){
        int opSize = operandArray.length;
        if (size != opSize) 
            throw new IndexOutOfBoundsException("add: sizes must be equal.");
        for (int i = 0; i < size; i++){
            realPixels[i] += operandArray[i];
        }
        setComplexOrigin();
    }
        /**
    * Subtract to complex pixels the scalar data in operand array.
    * 
    * @param operandArray   the array operand.
    */ 
    public void subtract(double[] operandArray){
        int opSize = operandArray.length;
        if (size != opSize) 
            throw new IndexOutOfBoundsException("subtract: sizes must be equal.");
        for (int i = 0; i < size; i++){
            realPixels[i] -= operandArray[i];
        }
        setComplexOrigin();
    }
   	
   /**
    * Divides real and complex pixels by the complex pixels of operand HoloJProcessor.
    * 
    * @param operand		the HoloJProcessor operand.
    */  
    public void divide(HoloJProcessor operand){
        if (size != operand.getSize()) 
            throw new IndexOutOfBoundsException("divide: sizes must be equal.");
        double[] opRealPixels = operand.getRealPixelsArray();
        double[] opComplexPixels = operand.getComplexPixelsArray();
        for (int i=0; i<size; i++) {
            double real = (realPixels[i]*opRealPixels[i]-complexPixels[i]*opComplexPixels[i])/
                        (opRealPixels[i]*opRealPixels[i]+
                        opComplexPixels[i]*opComplexPixels[i]);
            double complex = (realPixels[i]*opComplexPixels[i]+opRealPixels[i]*complexPixels[i])/
                        (opRealPixels[i]*opRealPixels[i]+
                        opComplexPixels[i]*opComplexPixels[i]);
            realPixels[i] = real;
            complexPixels[i] = complex;	
        }
        setComplexOrigin();
    }
    
    /**
     *  Add to complex pixels phase the specified radians.
     *
     * @param angle     radians to add.
     **/
    public void addPhase(double angle){
        double realAdd = Math.cos(angle);
        double complexAdd = Math.sin(angle);
        for (int i=0; i<size; i++){
            realPixels[i] += realAdd;
            complexPixels[i] += complexAdd;
        }
    }
    /**
     * Add to complex pixels the phase plate specified by the double array.
     *
     * @param plateArray    the array of radians to add.
     */
    public void addPhasePlate(double[] plateArray){
        if (size != plateArray.length) 
            throw new IndexOutOfBoundsException("addPhasePlate: size of the phase-plate must be the same of HoloJProcessor.");
        for (int i=0; i<size; i++) {
            realPixels[i]+=Math.cos(plateArray[i]);
            complexPixels[i]+=Math.sin(plateArray[i]);
        }
    }
    /**
     * Set origin of this HoloJProcessor to Real.
     *
     */    
    public void setRealOrigin(){
        isRealOrigin = true;
    }
    /**
     * Set origin of this HoloJProcessor to Complex.
     */
    public void setComplexOrigin(){
        isRealOrigin = false;
    }
    /**
     * Set the calibration of this HoloJProcessor to new calibration.
     *
     * @param newCal    new calibration to apply.
     */
    public void setCalibration(Calibration newCal){
        cal = newCal.copy();
    }
    
    /**
     * Return the calibration of this HoloJPpocessor.
     *
     * @return  the calibration.
     */
    public Calibration getCalibration(){
        return cal;
    }
    
    /**
     * Set the title of this HoloJProcesor to new title.
     *
     * @param newTitle  the new title to use.
     */
    public void setTitle(String newTitle){
        title = newTitle;
    }
    
    /**
     * Return the title of this HoloJProcessor.
     *
     * @return the title.
     */
    public String getTitle(){
        return title;
    }
   	   	
   /* ************************************************************ *
    *
    *	METHODS :: Fast Fourier Transforming
    *
    * ************************************************************ */

   /**
    * Perform Fast Fourier Transform, in the forward direction of data stored in real and complex pixels.
    * If source image is real, performes a RealToComplex transform using IJ FHT class.
    */
    public void doFFT() {
            if (isRealOrigin) doRealToComplexFFT();
            else doComplexToComplexFFT(1);
            isSpectrumDomain = true;
    }// doFFT()
   
   /**
    * Perform Fast Fourier Transform, in the backward direction of data stored in real and complex pixels.
    */	
    public void doInverseFFT() {
            if (isRealOrigin) doComplexToRealFFT();
            else doComplexToComplexFFT(-1);
            isSpectrumDomain = false;
    }// doInverseFFT
	
   /**
    * Calculates the Fast Fourier Transform of real data stored in the 
    * double array of real pixels. Starting from real values, it assumes 
    * forward direction.
    * It uses the Fast Hartley Transform provided by ImageJ class FHT.
    * The complex result of the method is stored in realPixels and 
    * complexPixels.
    */	
    private void doRealToComplexFFT() {
        //double[] pixels = (double[]) realPixels.clone();
        FloatProcessor fp = new FloatProcessor(width, height, realPixels);
        FHT transformer = new FHT(fp);
        transformer.transform();
        // scale factor to normalize FHT
        double factor = 1.0/width;
        transformer.swapQuadrants();
        // from real FHT to complex FFT
        float[] transformedPixels = (float[])transformer.getPixels();
        for (int row=0; row<height; row++) {
            int base = row * width;
            for(int col=0; col<width; col++) {
                int pos = base+col;
                int antipos = ((height-row)%height)*width+(width-col)%width;
                realPixels[row*width+col]=factor*0.5*(transformedPixels[pos]
                                                     +transformedPixels[antipos]);
                complexPixels[row*width+col]=factor*0.5*(transformedPixels[pos]
                                                   -transformedPixels[antipos]);
            }
        }
    }// doRealToComplexFFT(double[] pixels)
        
    /**
    * Calculates inverse Fast Fourier Transform of complex pixels 
    * assuming they are the result of a RealToComplexFFT.
    */	
    private void doComplexToRealFFT(){
        double[] pixels = new double[size];
        for (int i = 0; i < size; i++) 
            pixels[i]=realPixels[i]+complexPixels[i];
        FloatProcessor fp = new FloatProcessor(width, height, pixels);
        FHT transformer = new FHT(fp);
        transformer.swapQuadrants();
        transformer.transform();
        double factor = 1.0/width;
        float[] tPixels = (float[]) transformer.getPixels();
        for (int i = 0; i < size; i++) {
            realPixels[i]=factor*tPixels[i];
            complexPixels[i]=0.0;
        }
    }

   /**
    * Calculates the Fast Fourier Transform of complex data stored in the 
    * double arrays of real and complex pixels.
    * The complex result of the method is stored in realPixels and 
    * complexPixels.
    *
    * @param direction		forward or reverse direction.
    */		
    private void doComplexToComplexFFT(int direction){
        double[] pixelsData = HoloJUtils.arrangeToComplexArray(realPixels, complexPixels);
        int[] dimensionArray = new int[2];
        dimensionArray[0] = width;
        dimensionArray[1] = height;
        HoloJUtils.c2cfft(pixelsData, dimensionArray, direction);
        realPixels = HoloJUtils.extractRealPixels(pixelsData);
        complexPixels = HoloJUtils.extractComplexPixels(pixelsData);
    } // doComplextoComplexFFT(double[] realPixels, double[] complexPixels, int direction)
	
   /**
    * Creates and shows an ImagePlus containing the power spectrum.
    * The method uses showPowerSpectrum.
    *
    * @param title      title for the image displayied.
    */	
    public void show(String title) {
            showPowerSpectrum(title);
    } // show()
	
   /**
    * Creates and show an ImagePlus displaying the power spectrum of data.
    *
    * @param title		the title of the displayied image.
    */
    public void showPowerSpectrum(String title) {
        FloatProcessor fp = createPowerSpectrumProcessor();
        fp.resetMinAndMax();
        ImagePlus impResult = new ImagePlus(title, fp);
        impResult.show();
    } // showPowerSpectrum()
	
    /**
    * Creates and show an ImagePlus displaying the spectrum of data.
    *
    * @param title		the title of the displayied image.
    */
    public void showSpectrum(String title) {
        FloatProcessor fp = createSpectrumProcessor();
        fp.resetMinAndMax();
        ImagePlus impResult = new ImagePlus(title, fp);
        impResult.show();
    } // showSpectrum()

   /**
    * Creates and show an ImagePlus displaying the amplitude of data.
    *
    * @param title		the title of the displayied image.
    */	
    public void showAmplitude(String title) {
        FloatProcessor fp = createAmplitudeProcessor();
        fp.resetMinAndMax();
        ImagePlus impResult = new ImagePlus(title, fp);
        impResult.setCalibration(cal);
        impResult.show();
    } // showAmplitude()
	
   /**
    * Creates and show an ImagePlus displaying the phase of data.
    *
    * @param title		the title of the displayied image.
    */	
    public void showPhase(String title) {
        FloatProcessor fp = createPhaseProcessor();
        fp.resetMinAndMax();
        ImagePlus impResult = new ImagePlus(title, fp);
        impResult.setCalibration(cal);
        impResult.show();
    } // showPhase()
	
   /**
    * Creates an ImagePlus with the power spectrum of data.
    *
    * @param title		the title of the image.
    */
    public ImagePlus makePowerSpectrumImage(String title) {
        FloatProcessor fp = createPowerSpectrumProcessor();
        fp.resetMinAndMax();
        ImagePlus imp = new ImagePlus(title, fp);
        return imp;
    }
	
    /**
    * Creates an ImagePlus with the spectrum of data.
    *
    * @param title		the title of the image.
    */
    public ImagePlus makeSpectrumImage(String title) {
        FloatProcessor fp = createSpectrumProcessor();
        fp.resetMinAndMax();
        ImagePlus imp = new ImagePlus(title, fp);
        return imp;
    }
	
   /**
    * Creates an ImagePlus with the amplitude of data.
    *
    * @param title		the title of the image.
    */
    public ImagePlus makeAmplitudeImage(String title) {
        FloatProcessor fp = createAmplitudeProcessor();
        fp.resetMinAndMax();
        ImagePlus imp = new ImagePlus(title, fp);
        imp.setCalibration(cal);
        return imp;
    }
	
   /**
    * Creates an ImagePlus with the phase of data.
    *
    * @param title		the title of the image.
    */
    public ImagePlus makePhaseImage(String title) {
        FloatProcessor fp = createPhaseProcessor();
        fp.resetMinAndMax();
        ImagePlus imp = new ImagePlus(title, fp);
        imp.setCalibration(cal);
        return imp;
    }// makePhaseImage()

   /**
    *	Creates a power spectrum ImageProcessor from realPixels and complexPixels.
    *
    *	@return		the FloatProcessor
    */
    public FloatProcessor createPowerSpectrumProcessor() {
        FloatProcessor fp = new  FloatProcessor(width, height) ;
        float[] fsrc = new float[size];
        for (int k=0; k<size; k++)
            fsrc[k] = (float)(0.5*Math.log(Math.sqrt(realPixels[k]*realPixels[k]+
                            complexPixels[k]*complexPixels[k])));
        fp.setPixels(fsrc);
        return fp;
    } // createPowerSpectrumProcessor()

    /**
    *	Creates a spectrum ImageProcessor from realPixels and complexPixels.
    *
    *	@return		the FloatProcessor
    */
    public FloatProcessor createSpectrumProcessor() {
        FloatProcessor fp = new  FloatProcessor(width, height) ;
        float[] fsrc = new float[size];
        for (int k=0; k<size; k++)
                fsrc[k] = (float)(Math.log(Math.sqrt(realPixels[k]*realPixels[k]+
                                complexPixels[k]*complexPixels[k])));
        fp.setPixels(fsrc);
        return fp;
    } // createSpectrumProcessor()
	
   /**
    *	Creates a amplitude ImageProcessor from realPixels and complexPixels.
    *
    *	@return		the FloatProcessor
    */
    public FloatProcessor createAmplitudeProcessor() {
        FloatProcessor fp = new  FloatProcessor(width, height) ;
        float[] fsrc = new float[size];
        for (int k=0; k<size; k++)
            fsrc[k] = (float)(HoloJUtils.modulus(realPixels[k],complexPixels[k]));
        fp.setPixels(fsrc);
        return fp;
    } // crateAmplitudeProcessor()
		
   /**
    *	Creates a phase ImageProcessor from realPixels and complexPixels.
    *
    *	@return		the FloatProcessor
    */
    public FloatProcessor createPhaseProcessor() {
        FloatProcessor fp = new  FloatProcessor(width, height) ;
        float[] fsrc = new float[size];
        for (int k=0; k<size; k++)
            fsrc[k] = (float)(Math.atan2(realPixels[k],complexPixels[k])+Math.PI);
        fp.setPixels(fsrc);
        HoloJUtils.resetMin(fp);
        return fp;
    } // cratePhaseProcessor()	
	
   /**
    * Finds the maximum of the modulus of realPixels and complexPixels. 
    *
    * @return		the maximum position.
    */		
    public Point getMaximumPosition(){
        double maxVal = Double.NEGATIVE_INFINITY;
        Point maxPos = new Point(0,0);
        double curVal = 0.0;
        for (int row = 0; row < height; row++)
            for (int col = 0; col < width; col++) {
                curVal = HoloJUtils.modulus(realPixels[row*width+col], complexPixels[row*width+col]);
                if (maxVal < curVal) {
                    maxVal = curVal;
                    maxPos.setLocation(col,row);									
                }	
            }
        return maxPos;
    }// getMaximumPosition()
	
   /**
    * Finds the maximum of the modulus of realPixels and 
    * complexPixels in the region specified by the selection. 
    *
    * @param selection		selected area of the pixels.
    * @return				the maximum position.
    */		
    public Point getMaximumPosition(Roi selection){
        double maxVal = Double.NEGATIVE_INFINITY;
        Point maxPos = new Point(0,0);
        double curVal = 0.0;
        for (int row = 0; row < height; row++)
            for (int col = 0; col < width; col++) {
                if (((row >= selection.getBounds().y)&&
                    (row <= selection.getBounds().y+selection.getBounds().height)) &&
                    ((col >= selection.getBounds().x) &&
                    (col <= selection.getBounds().x+selection.getBounds().width))) {
                        curVal = HoloJUtils.modulus(realPixels[row*width+col], 
                                                    complexPixels[row*width+col]);
                        if (maxVal < curVal) {
                            maxVal = curVal;
                            maxPos.setLocation(col,row);									
                        }	
                   }
            }
        return maxPos;
    }// getMaximumPosition(Roi selection)

   /**
    * Finds the position of the maximum lateral peak of the spectrum.
    * The method search for the maximum of the spectrum excluding a 
    * circular region of radius 20 pixels centered around the origin of the spectrum. 
    *
    * @param side   side of the spectrum where to look for the maximum.
    * @return       the sideband position.
    */		
    public Point getSidebandCenter(int side) {
            if (isSpectrumDomain == false) 
                    throw new ArrayStoreException("getSidebandCenter: image is not in Fourier domain.");
            double maxValue = Double.NEGATIVE_INFINITY;
            Point maxPos = new Point(0,0);
            int xC = width >> 1;
            int yC = height >> 1; 
            double curVal = 0;
            if ( side >= 0 ) {
                    for (int row = 0; row < (height >> 1); row++)
                            for (int col = 0; col < width; col++) {
                                    curVal = HoloJUtils.modulus(realPixels[row*width+col], 
                                                                                            complexPixels[row*width+col]); 
                                    if (
                                            ( ( (row-xC)*(row-xC) +
                                            (col-yC)*(col-yC) ) > EXCLUDED_RADIUS*EXCLUDED_RADIUS ) &&
                                            ( maxValue < curVal )
                                    ) {
                                            maxValue = curVal;
                                            maxPos.setLocation(col,row);	
                                    }

                            }
            } else {
                    for (int row = height >> 1; row < height; row++)
                            for (int col = 0; col < width; col++) {
                                    curVal = HoloJUtils.modulus(realPixels[row*width+col], 
                                                                                            complexPixels[row*width+col]); 
                                    if (
                                            ( ( (row-xC)*(row-xC) +
                                            (col-yC)*(col-yC) ) > EXCLUDED_RADIUS*EXCLUDED_RADIUS ) &&
                                            ( maxValue < curVal )
                                    ) {
                                            maxValue = curVal;
                                            maxPos.setLocation(col,row);	
                                    }

                            }
            }
            return maxPos;	
    }// getSidebandCenter()
	
   /**
     *   Returns an HoloJProcessor containing the selected sideband data.
     * 
     * 
     * 
     * @param sideCenter	Point of sideband center.
     * @param radius		radius of the selected region.
     * @param scaleFactor	scale factor of final reconstructed image.
     * @param useButterworth	flag for using soft aperture.
     * @return the selected sideband.
     */
	public HoloJProcessor getSideband(Point sideCenter, int radius, int scaleFactor, boolean useButterworth) {
		if (isSpectrumDomain == false) 
			throw new ArrayStoreException("getSideband: image is not in Fourier domain.");
		// determinates maximum area to be copied.
		int desiredWidth = width / scaleFactor;
		int desiredHeight = height / scaleFactor;
		int maxWidth = Math.min(width-sideCenter.x,sideCenter.x);
		int maxHeight = Math.min(height-sideCenter.y,sideCenter.y);
		int maxHalfDim = Math.min(maxWidth,maxHeight);
		int maxDim = maxHalfDim << 1;

		if (maxDim > desiredHeight) maxDim = desiredHeight;
		else if (maxDim > desiredWidth) maxDim = desiredWidth;
		
		maxHalfDim = maxDim >> 1;
		
		int starter = sideCenter.x - maxHalfDim + (sideCenter.y - maxHalfDim)*width;
		int sideSize = maxDim*maxDim;
		double[] mask = new double[maxDim*maxDim];
		if (useButterworth == true) {
			mask = HoloJUtils.butterworthMask(maxDim, maxDim, radius, 0.414);
		} else {
			mask = HoloJUtils.circularMask(maxDim, maxDim, radius);
		}
		
		double[] sideRealPix = new double[sideSize];
		double[] sideComplexPix = new double[sideSize];
		
		for ( int j=0; j < maxDim; j++ ) {
			for (int i = 0; i < maxDim; i++) {
				int pos = i+j*maxDim;
				int pos2 = starter+j*width+i;
				sideRealPix[pos] = mask[pos]*realPixels[pos2];
				sideComplexPix[pos] = mask[pos]*complexPixels[pos2];
			}	
		}
		HoloJProcessor sideband = new HoloJProcessor(sideRealPix, sideComplexPix, maxDim, maxDim);		
		return sideband;
				
	}// getSideband()	
}// end of class HoloJProcessor