import holoj.HoloJFrame;
import ij.*;
import ij.io.*;
import ij.plugin.*;
import ij.process.*;
import ij.gui.*;
import java.awt.event.*;
import holoj.HoloJProcessor;
import holoj.HoloJUtils;

/**
 * HoloJ_ plugin for holographic reconstruction.<br/>
 * http://www.bo.imm.cnr.it/~ortolani/holoj.html
 *
 * @author Luca Ortolani and Pier Francesco Fazzini
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

public class HoloJ_ implements  PlugIn {
	
	public void run(java.lang.String arg) {
                HoloJFrame hjf = new HoloJFrame();	
                hjf.setVisible(true);	
	}
        
}