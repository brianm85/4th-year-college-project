
package holoj;
import ij.IJ;
import ij.ImageListener;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.OvalRoi;
import ij.gui.Roi;
import ij.io.OpenDialog;
import ij.io.Opener;
import ij.measure.Calibration;
import ij.util.Java2;
import java.awt.Point;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JTextField;
import holoj.HoloJUtils;
import holoj.HoloJProcessor;
import java.util.Scanner;
import ij.process.ImageProcessor;

/**
 *
 * @author  Brian Mitchell
 * /---- Added extra functionality and methods to origional GUI -----/
 * @version 1.0
 */
public class HoloJFrame extends javax.swing.JFrame {
    
    private int x=0;
    private int y=0;
    private Point sideCenter = new Point();
    private int radius=50;
    private int ratio=2;
    private boolean butterworth=false;
    private boolean amplitude=false;
    private boolean phase=false;
    private String standardItem =new String("No Image selected");
    private HoloJProcessor hp;
    private Calibration imageCal;
    private String title = null;
	private double distance, wavelength, dx, dy;//varibles for allowing edit within the class
	//private double dx = 0.00000345;
	//private double dy = 0.00000345;
	//private double distance = 0.00899;
	//private double wavelength = 0.000000633;
	HoloJProcessor holo,ref,rec;//new holoj processors
    
    private void operate(){//DONT EDIT. Removed redundant code 
		holo=getHologramProcessor();
		ref=getReferenceProcessor();
         
        if (holo == null) 
            throw new ArrayStoreException("reconstruct: No hologram selected.");
        else 
		{

			rec = HoloJUtils.reconstruct(radius,ratio,sideCenter,holo,butterworth);
            if (ref != null) 
			{
				rec = HoloJUtils.reconstruct(radius,ratio,sideCenter,ref,holo,butterworth);
            }

            rec.setTitle(""+title);
			rec.showHolo("Hologram : "+rec.getTitle()+" :");
		}
    } 
	/*Added new function method for operation of custom filter in GUI*/
	private void operate2(){
			

			wavelength=getDouble(wavelengthTF);
			dx=getDouble(dxTF);
			dy=getDouble(dyTF);
			distance=getDouble(distanceTF);
            if (ref == null) 
			{
                rec = HoloJUtils.propogatefunc(rec, rec.getWidth(),rec.getHeight(), dx, dy, distance, wavelength);
            }
			else if((ref==null) && (holo == null))
			{
				throw new ArrayStoreException("reconstruct: No hologram or reference selected.");
			}
			else 
			{
				rec = HoloJUtils.propogatefunc(rec, rec.getWidth(),rec.getHeight(), dx, dy, distance, wavelength);

            }
            rec.setTitle(""+title);
			if (phase) rec.showPhase("Hologram : "+rec.getTitle()+" : Phase");
            if (amplitude) rec.showAmplitude("Hologram : "+rec.getTitle()+" : Amplitude");
        }
   
    private ImagePlus createSidebandImage(HoloJProcessor hologram){
         
        ImagePlus imp;    
        int size=hologram.getHeight();
        int l=hologram.getWidth();
        int side=getInteger(ratioTF);
        int r=getInteger(radiusTF);
             
        hologram.doFFT();
        sideCenter=hologram.getSidebandCenter(ratio);
        imp=hologram.makeSpectrumImage("Select Sideband");
        OvalRoi or=new OvalRoi(sideCenter.x-r,sideCenter.y-r,2*r,2*r);
        imp.setRoi(or);
        
        return imp;
    }
    
    /** Creates new form HoloJFrame */
    public HoloJFrame() {
        Java2.setSystemLookAndFeel();
        initComponents();
        initFileList(holoCB);
        initFileList(refCB);
		
    }
   
    private void initFileList(JComboBox cb){
        int[] ids=WindowManager.getIDList();
        int n= WindowManager.getImageCount();
        
        cb.removeAllItems();
        if (n>0){
            cb.addItem(standardItem);
            for (int i=0;i<WindowManager.getImageCount();i++){
             cb.addItem(WindowManager.getImage(ids[i]).getTitle());
            }
        }
        else cb.addItem(standardItem);
        
        //pack();
    }
    private boolean hasIt(JComboBox cb, String s){
        for (int i=0;i<WindowManager.getImageCount();i++){
            if(cb.getItemAt(i).equals(s)) return true;
        }
        return false;
    }
    
    private void addFileToList(JComboBox cb){
        OpenDialog od=new OpenDialog("Choose image","");
        String tmp=od.getFileName();
        if (!hasIt(cb, tmp)) cb.addItem(tmp);
        cb.setSelectedItem(tmp);
        cb.removeItem(standardItem);
        pathTF.setText(od.getDirectory());
    }
    
    private void setDir(JTextField tf){
        OpenDialog od=new OpenDialog("Choose directory ","");
        tf.setText(od.getDirectory());
    }
    
    private ImagePlus getOpenedImage(String name){
        int[] ids=WindowManager.getIDList();
        int n= WindowManager.getImageCount();
        ImagePlus imp;
      
        if (n>0){
            for (int i=0;i<n;i++){
                imp=WindowManager.getImage(ids[i]);
                if(imp.getTitle()==name) return imp;
            }   
        }
        return null;
    }
    
    private ImagePlus getImage(JComboBox cb){
        String dir=pathTF.getText();
        String name=cb.getSelectedItem().toString();
        ImagePlus imp;
        
        imp=getOpenedImage(name);
        if(imp==null){
            Opener op = new Opener();
            imp=op.openImage(dir,name);
        }
        return imp;
    }
    
    private HoloJProcessor getHologramProcessor(){
        HoloJProcessor proc=null;
        ImagePlus imp=getImage(holoCB);
        if(imp!=null) {
            imageCal = imp.getCalibration().copy();
            title = imp.getTitle();
            proc=new HoloJProcessor(imp.getProcessor());
        }
        return proc;
    }
    
    private HoloJProcessor getReferenceProcessor(){
        HoloJProcessor proc=null;
        ImagePlus imp=getImage(refCB);
        if(imp!=null) proc=new HoloJProcessor(imp.getProcessor());
        return proc;
    }
    
    private void sidebandFromFFT(){
        hp=getHologramProcessor();
        if(hp!=null){
            ImagePlus imp=createSidebandImage(hp);
            imp.addImageListener(
                new ImageListener(){
                    public void imageClosed(ImagePlus ip){
                        Roi sel = ip.getRoi(); 
                        if(ip.getRoi()!=null) {
                            sideCenter = HoloJUtils.getMaximumPosition(hp,sel);
                            //radiusTF.setText((Math.max(h,w)>>1)+"");
                            xTF.setText(((int)sideCenter.x)+"");
                            yTF.setText(((int)sideCenter.y)+"");
                        }
                        ImagePlus.removeImageListener(this);
                    }
                    public void imageUpdated(ImagePlus ip){}
                    public void imageOpened(ImagePlus ip){}
                }
            );
            
            imp.show();
        }
    }
	
	private void button6function(){
		IJ.log(" button custom reconstruct pressed");
		//ip.show();
	}
    
    private int getInteger(JTextField tf){
        int ret=(new Integer(tf.getText())).intValue();
        //IJ.write(""+ret);
        return ret;      
    }
	private double getDouble(JTextField tf){
        double ret=(new Double(tf.getText())).doubleValue();
        return ret;
    }
    private boolean getBoolean(JCheckBox cb){
        boolean ret=cb.isSelected();
        return ret;
    }
	private void button3function(){
		String[] args={};
        Interactive_3D_Surface_Plot.main(args);
	}
////////////////////////////////////////////////////////////////////////////////////////////////////
	private void button2function(){
		//IJ.log(" button unwrap1 pressed");
		UnwrapJ_ um = new UnwrapJ_();
		ImagePlus imp=WindowManager.getCurrentImage();
		if(imp == null){IJ.log("error in imp,  ..... its null");}
		ImageProcessor ip = imp.getProcessor();
		if(ip == null){IJ.log("error in ip its null");}
		um.run(ip);
		
	}
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">                          
    private void initComponents() {
		

        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        pathTF = new javax.swing.JTextField();
        pathB = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        holoB = new javax.swing.JButton();
        holoCB = new javax.swing.JComboBox();
        jLabel3 = new javax.swing.JLabel();
        refCB = new javax.swing.JComboBox();
        rholoB = new javax.swing.JButton();
        jButton1 = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        jLabel4 = new javax.swing.JLabel();
        xTF = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        yTF = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        radiusTF = new javax.swing.JTextField();
        jButton4 = new javax.swing.JButton();
        jLabel8 = new javax.swing.JLabel();
        ratioTF = new javax.swing.JTextField();
        jPanel3 = new javax.swing.JPanel();
        jButton5 = new javax.swing.JButton();
        amplitudeCB = new javax.swing.JCheckBox();
        phaseCB = new javax.swing.JCheckBox();
        jLabel7 = new javax.swing.JLabel();
        butterCB = new javax.swing.JCheckBox();
        jButton3 = new javax.swing.JButton();
        jButton6 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
		dxTF = new javax.swing.JTextField();
        dyTF = new javax.swing.JTextField();
        wavelengthTF = new javax.swing.JTextField();
        distanceTF = new javax.swing.JTextField();
        jLabel9 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("HoloJ");

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("Files"));

        jLabel1.setText("Path");

        pathTF.setText("No Directory Selected");
        pathTF.setMaximumSize(new java.awt.Dimension(500, 20));
        pathTF.setPreferredSize(new java.awt.Dimension(50, 20));

        pathB.setText("...");
        pathB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                pathBActionPerformed(evt);
            }
        });

        jLabel2.setText("Hologram");

        holoB.setText("...");
        holoB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                holoBActionPerformed(evt);
            }
        });

        holoCB.setEditable(true);
        holoCB.setMaximumRowCount(5);
        holoCB.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "No Image Selected" }));
        holoCB.setMaximumSize(new java.awt.Dimension(138, 22));

        jLabel3.setText("Reference");

        refCB.setEditable(true);
        refCB.setMaximumRowCount(5);
        refCB.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "No Image Selected" }));
        refCB.setMaximumSize(new java.awt.Dimension(138, 22));

        rholoB.setText("...");
        rholoB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rholoBActionPerformed(evt);
            }
        });

        jButton1.setText("Reset File List");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jLabel1)
                .addGap(45, 45, 45)
                .addComponent(pathTF, javax.swing.GroupLayout.PREFERRED_SIZE, 179, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(16, 16, 16)
                .addComponent(pathB, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jLabel2)
                .addGap(6, 6, 6)
                .addComponent(holoCB, javax.swing.GroupLayout.PREFERRED_SIZE, 194, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(1, 1, 1)
                .addComponent(holoB, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jLabel3)
                .addGap(7, 7, 7)
                .addComponent(refCB, javax.swing.GroupLayout.PREFERRED_SIZE, 194, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(1, 1, 1)
                .addComponent(rholoB, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(71, 71, 71)
                .addComponent(jButton1))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel1)
                    .addComponent(pathTF, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(pathB, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(3, 3, 3)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel2)
                    .addComponent(holoCB, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(holoB, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(3, 3, 3)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel3)
                    .addComponent(refCB, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(rholoB, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(5, 5, 5)
                .addComponent(jButton1))
        );

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder("Sideband"));

        jLabel4.setText("x");

        xTF.setHorizontalAlignment(javax.swing.JTextField.TRAILING);
        xTF.setText("0");
        xTF.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                xTFActionPerformed(evt);
            }
        });
        xTF.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                xTFFocusLost(evt);
            }
        });

        jLabel5.setText("y");

        yTF.setHorizontalAlignment(javax.swing.JTextField.TRAILING);
        yTF.setText("0");
        yTF.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                yTFActionPerformed(evt);
            }
        });
        yTF.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                yTFFocusLost(evt);
            }
        });

        jLabel6.setText("Radius");

        radiusTF.setHorizontalAlignment(javax.swing.JTextField.TRAILING);
        radiusTF.setText("50");
        radiusTF.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                radiusTFActionPerformed(evt);
            }
        });
        radiusTF.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                radiusTFFocusLost(evt);
            }
        });

        jButton4.setText("Select from FFT");
        jButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton4ActionPerformed(evt);
            }
        });

        jLabel8.setText("Ratio");

        ratioTF.setHorizontalAlignment(javax.swing.JTextField.TRAILING);
        ratioTF.setText("4");
        ratioTF.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ratioTFActionPerformed(evt);
            }
        });
        ratioTF.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                ratioTFFocusLost(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(55, 55, 55)
                .addComponent(jLabel4)
                .addGap(4, 4, 4)
                .addComponent(xTF, javax.swing.GroupLayout.PREFERRED_SIZE, 64, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(20, 20, 20)
                .addComponent(jLabel8)
                .addGap(18, 18, 18)
                .addComponent(ratioTF, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(55, 55, 55)
                .addComponent(jLabel5)
                .addGap(4, 4, 4)
                .addComponent(yTF, javax.swing.GroupLayout.PREFERRED_SIZE, 64, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(20, 20, 20)
                .addComponent(jLabel6)
                .addGap(7, 7, 7)
                .addComponent(radiusTF, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(87, 87, 87)
                .addComponent(jButton4, javax.swing.GroupLayout.PREFERRED_SIZE, 154, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel4)
                    .addComponent(xTF, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel8)
                    .addComponent(ratioTF, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel5)
                    .addComponent(yTF, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel6)
                    .addComponent(radiusTF, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addComponent(jButton4))
        );

        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder("Reconstruct"));

        jButton5.setText("Reconstruct");
        jButton5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton5ActionPerformed(evt);
            }
        });

        amplitudeCB.setText("Amplitude");
        amplitudeCB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                amplitudeCBActionPerformed(evt);
            }
        });

        phaseCB.setText("Phase");
        phaseCB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                phaseCBActionPerformed(evt);
            }
        });

        jLabel7.setText("Extract:");

        butterCB.setText("Butterworth Filter");
        butterCB.setMaximumSize(new java.awt.Dimension(135, 25));
        butterCB.setMinimumSize(new java.awt.Dimension(135, 25));
        butterCB.setPreferredSize(new java.awt.Dimension(135, 25));
        butterCB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                butterCBActionPerformed(evt);
            }
        });

        jButton3.setText("3D graph");
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });

        jButton6.setText("Custom Reconstruct");
        jButton6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton6ActionPerformed(evt);
            }
        });

        jButton2.setText("UnWrap");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });
/////////////////////////////////////////////////////////////////////////////////////////
        dxTF.setText("0.00000345");
        dxTF.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                dxTFFocusLost(evt);
            }
        });
        dxTF.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                dxTFActionPerformed(evt);
            }
        });

        dyTF.setText("0.00000345");
        dyTF.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                dyTFFocusLost(evt);
            }
        });
        dyTF.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                dyTFActionPerformed(evt);
            }
        });

        wavelengthTF.setText("0.000000633");
        wavelengthTF.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                wavelengthTFFocusLost(evt);
            }
        });
        wavelengthTF.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                wavelengthTFActionPerformed(evt);
            }
        });

        distanceTF.setText("0.00899");
        distanceTF.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                distanceTFFocusLost(evt);
            }
        });
        distanceTF.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                distanceTFActionPerformed(evt);
            }
        });

        jLabel9.setText("dx");

        jLabel10.setText("dy");

        jLabel11.setText("wavelength");

        jLabel12.setText("distance");

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(amplitudeCB, javax.swing.GroupLayout.PREFERRED_SIZE, 138, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(jPanel3Layout.createSequentialGroup()
                                .addGap(8, 8, 8)
                                .addComponent(jLabel7))
                            .addComponent(phaseCB, javax.swing.GroupLayout.PREFERRED_SIZE, 186, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 203, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(butterCB, javax.swing.GroupLayout.PREFERRED_SIZE, 191, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel10)
                            .addComponent(jLabel9))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(dxTF)
                            .addComponent(dyTF))))
                .addGap(28, 28, 28)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jButton5, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGroup(jPanel3Layout.createSequentialGroup()
                            .addComponent(jLabel11)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(wavelengthTF, javax.swing.GroupLayout.PREFERRED_SIZE, 137, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(jPanel3Layout.createSequentialGroup()
                            .addComponent(jLabel12)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(distanceTF, javax.swing.GroupLayout.PREFERRED_SIZE, 160, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addComponent(jButton3, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 203, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jButton6, javax.swing.GroupLayout.PREFERRED_SIZE, 203, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(25, 25, 25))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel3Layout.createSequentialGroup()
                                .addGap(8, 8, 8)
                                .addComponent(jLabel7)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(phaseCB, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(amplitudeCB)
                                .addGap(1, 1, 1)
                                .addComponent(butterCB, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel3Layout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(jButton5)
                                .addGap(29, 29, 29)
                                .addComponent(jButton6)))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(dxTF, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(wavelengthTF, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel9)))
                    .addComponent(jLabel11, javax.swing.GroupLayout.Alignment.TRAILING))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(dyTF, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(distanceTF, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLabel12))
                    .addComponent(jLabel10))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 12, Short.MAX_VALUE)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton2)
                    .addComponent(jButton3))
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(19, 19, 19)
                        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(40, 40, 40)
                        .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>                              
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    private void radiusTFFocusLost(java.awt.event.FocusEvent evt) {                                   
        radius = getInteger(radiusTF);
    }                                  

    private void ratioTFFocusLost(java.awt.event.FocusEvent evt) {                                  
        ratio = getInteger(ratioTF);
    }                                 

    private void yTFFocusLost(java.awt.event.FocusEvent evt) {                              
        sideCenter.y = getInteger(yTF);
    }                             

    private void xTFFocusLost(java.awt.event.FocusEvent evt) {                              
        sideCenter.x = getInteger(xTF);
    }                             

    private void jButton5ActionPerformed(java.awt.event.ActionEvent evt) {                                         
        operate();
    }                                        

    private void amplitudeCBActionPerformed(java.awt.event.ActionEvent evt) {                                            
        amplitude=getBoolean(amplitudeCB);
    }                                           

    private void phaseCBActionPerformed(java.awt.event.ActionEvent evt) {                                        
        phase=getBoolean(phaseCB);
    }                                       

    private void butterCBActionPerformed(java.awt.event.ActionEvent evt) {                                         
        butterworth=getBoolean(butterCB);
    }                                        

    private void yTFActionPerformed(java.awt.event.ActionEvent evt) {                                    
        sideCenter.y=getInteger(yTF);
    }                                   

    private void xTFActionPerformed(java.awt.event.ActionEvent evt) {                                    
        sideCenter.x=getInteger(xTF);
    }                                   

    private void ratioTFActionPerformed(java.awt.event.ActionEvent evt) {                                        
        ratio=getInteger(ratioTF);
    }                                       

    private void radiusTFActionPerformed(java.awt.event.ActionEvent evt) {                                         
        radius=getInteger(radiusTF);
    }                                        

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {                                         
        initFileList(holoCB);
        initFileList(refCB);
    }                                        

    private void rholoBActionPerformed(java.awt.event.ActionEvent evt) {                                       
        addFileToList(refCB);
    }                                      

    private void holoBActionPerformed(java.awt.event.ActionEvent evt) {                                      
        addFileToList(holoCB);
    }                                     

    private void pathBActionPerformed(java.awt.event.ActionEvent evt) {                                      
       setDir(pathTF);
    }                                     

    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {                                         
        sidebandFromFFT();
    }                                        
	
	private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {                                         
        //sidebandFromFFT();
		button3function(); 
    }                                        
	
	private void jButton6ActionPerformed(java.awt.event.ActionEvent evt) {                                         
        
		operate2();
		
    }                                        

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) { 
		button2function();
      
    }
	private void dxTFActionPerformed(java.awt.event.ActionEvent evt) {                                     
       dx=getDouble(dxTF);
    }                                    

    private void dyTFActionPerformed(java.awt.event.ActionEvent evt) {                                     
        dy=getDouble(dyTF);
    }                                    

    private void distanceTFActionPerformed(java.awt.event.ActionEvent evt) {                                           
        distance=getDouble(distanceTF);
    }                                          

    private void wavelengthTFActionPerformed(java.awt.event.ActionEvent evt) {                                             
        wavelength=getDouble(wavelengthTF);
    }                                            

    private void wavelengthTFFocusLost(java.awt.event.FocusEvent evt) {                                       
        wavelength = getDouble(wavelengthTF);
    }                                      

    private void distanceTFFocusLost(java.awt.event.FocusEvent evt) {                                     
        distance = getDouble(distanceTF);
    }                                    

    private void dyTFFocusLost(java.awt.event.FocusEvent evt) {                               
        dy = getDouble(dyTF);
    }                              

    private void dxTFFocusLost(java.awt.event.FocusEvent evt) {                               
        dx = getDouble(dxTF);
    }

    // Variables declaration - do not modify                     
    private javax.swing.JCheckBox amplitudeCB;
    private javax.swing.JCheckBox butterCB;
    private javax.swing.JTextField distanceTF;
    private javax.swing.JTextField dxTF;
    private javax.swing.JTextField dyTF;
    private javax.swing.JButton holoB;
    private javax.swing.JComboBox holoCB;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton5;
    private javax.swing.JButton jButton6;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JButton pathB;
    private javax.swing.JTextField pathTF;
    private javax.swing.JCheckBox phaseCB;
    private javax.swing.JTextField radiusTF;
    private javax.swing.JTextField ratioTF;
    private javax.swing.JComboBox refCB;
    private javax.swing.JButton rholoB;
    private javax.swing.JTextField wavelengthTF;
    private javax.swing.JTextField xTF;
    private javax.swing.JTextField yTF;
    // End of variables declaration                  
    
}
