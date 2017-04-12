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
 * HoloJ_ main class for running program
 * 
 *
 * @author Brian MItchell
 * @student number 12261769
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
    public static void main(String args[])
    {//Used for accsessor to create jar file
        HoloJFrame hjf = new HoloJFrame();	
                hjf.setVisible(true);
    }
           
    public void run(java.lang.String arg) {
                HoloJFrame hjf = new HoloJFrame();	
                hjf.setVisible(true);	
	}
/*
    @Override
    public void run(String string) {
         HoloJFrame hjf = new HoloJFrame();	
                hjf.setVisible(true);//To change body of generated methods, choose Tools | Templates.
    }*/

    
	
        
        
}