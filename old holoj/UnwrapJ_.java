import holoj.HoloJProcessor;
import holoj.HoloJUtils;
import ij.IJ;
import ij.ImagePlus;
import ij.measure.Calibration;
import ij.plugin.filter.PlugInFilter;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;


/**
 * UnwrapJ_ plugin for automatic phase unwrapping.<br/>
 * http://www.bo.imm.cnr.it/~ortolani/holoj.html<br/>
 * Based on algorithm proposed by Marvin A. Schofield and Yimei Zhu (see Optics Letter, 28 (2003), 1194-1196.)
 *
 * @author Luca Ortolani & Pier Francesco Fazzini
 * @version 1.0
 */

    /*
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

public class UnwrapJ_ implements PlugInFilter {
    
    public int setup(String arg, ImagePlus imp){
        return DOES_ALL;
    }
    
    public void run(ImageProcessor ip) {
        
        Calibration cal = IJ.getImage().getCalibration().copy();
        String title = IJ.getImage().getTitle();
        
        ImageProcessor source = HoloJUtils.getPhaseRenormalizedProcessor(ip);
        ImageProcessor simmetric = HoloJUtils.makeSimmetricProcessor(source);
        
        int width = simmetric.getWidth();
        int height = simmetric.getHeight();
        
        HoloJProcessor cos = new HoloJProcessor(HoloJUtils.makeCosProcessor(simmetric));
        HoloJProcessor sin = new HoloJProcessor(HoloJUtils.makeSinProcessor(simmetric));
        ImageProcessor parMask = HoloJUtils.makeParabolicMask(simmetric.getWidth(),simmetric.getHeight());
        ImageProcessor invParMask = HoloJUtils.makeInverseParabolicMask(simmetric.getWidth(),simmetric.getHeight());
        cos.doFFT();
        sin.doFFT();
        cos.multiply(parMask);
        sin.multiply(parMask);
        cos.setRealOrigin();
        sin.setRealOrigin();
        cos.doInverseFFT();
        sin.doInverseFFT();
        cos.multiply(HoloJUtils.makeSinProcessor(simmetric));
        sin.multiply(HoloJUtils.makeCosProcessor(simmetric));
        cos.setRealOrigin();
        sin.setRealOrigin();
        cos.doFFT();
        sin.doFFT();
        cos.multiply(invParMask);
        sin.multiply(invParMask);
        sin.subtract(cos);
        sin.setRealOrigin();
        sin.doInverseFFT();
        double[] pix = sin.getRealPixelsArray();
        double[] newPix = new double[pix.length>>2];
        int newWidth = width>>1;
        int newHeight = height>>1;
        for (int i=0; i<newHeight; i++)
            for (int j=0; j<newWidth; j++)
                newPix[i*newWidth+j] = pix[i*width+j];
        FloatProcessor cont = new FloatProcessor(newWidth,newHeight,newPix);
        FloatProcessor diff = HoloJUtils.multiply(1/(2*Math.PI),HoloJUtils.subtract(cont, source));
        HoloJUtils.round(diff);
        diff = HoloJUtils.multiply(2*Math.PI, diff);
        FloatProcessor unwrap = HoloJUtils.add(source, diff);
        int maxRec = (int) IJ.getNumber("Recurrencies: ",0);
        for (int i=0; i<maxRec; i++){
            diff = HoloJUtils.multiply(1/(2*Math.PI),HoloJUtils.subtract(cont, unwrap));
            HoloJUtils.round(diff);
            diff = HoloJUtils.multiply(2*Math.PI, diff);
            unwrap = HoloJUtils.add(unwrap, diff);
        }
        HoloJUtils.resetMin(unwrap);
        unwrap.resetMinAndMax();
        ImagePlus img = new ImagePlus(title+" : Unwrapped",unwrap);
        img.setCalibration(cal);
        img.show();
    }
}
