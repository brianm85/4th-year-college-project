/*
 * HoloJFrame.java
 *
 * Created on 6 juin 2007, 19:35
 */

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

/**
 *
 * @author  Luca Ortolani and Pier Francesco Fazzini
 * @version 1.0
 */
public class HoloJFrame extends javax.swing.JFrame {
    
    private int x=0;
    private int y=0;
    private Point sideCenter = new Point();
    private int radius=50;
    private int ratio=4;
    private boolean butterworth=false;
    private boolean amplitude=false;
    private boolean phase=false;
    private String standardItem =new String("No Image selected");
    private HoloJProcessor hp;
    private Calibration imageCal;
    private String title = null;
    
    private void operate(){
        HoloJProcessor holo=getHologramProcessor();
        HoloJProcessor ref=getReferenceProcessor();
        
     
        if (holo == null) 
            throw new ArrayStoreException("reconstruct: No hologram selected.");
        else {
            HoloJProcessor rec = new HoloJProcessor(holo.getWidth(),holo.getHeight());
            imageCal.pixelWidth *= ratio;
            imageCal.pixelHeight *= ratio;
            if (ref == null) {
                //IJ.write("reconstruct without reference");
                rec = HoloJUtils.reconstruct(radius,ratio,sideCenter,holo,butterworth);
            } else {
                //IJ.write("reconstruct with reference");
                rec = HoloJUtils.reconstruct(radius,ratio,sideCenter,holo,ref,butterworth);
            }
            rec.setCalibration(imageCal);
            rec.setTitle(""+title);
            if (phase) rec.showPhase("Hologram : "+rec.getTitle()+" : Phase");
            if (amplitude) rec.showAmplitude("Hologram : "+rec.getTitle()+" : Amplitude");
        }

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
    
    private int getInteger(JTextField tf){
        int ret=(new Integer(tf.getText())).intValue();
        //IJ.write(""+ret);
        return ret;      
    }
    
    private boolean getBoolean(JCheckBox cb){
        boolean ret=cb.isSelected();
        return ret;
    }
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

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

        getContentPane().setLayout(new java.awt.GridBagLayout());

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("HoloJ");
        jPanel1.setLayout(new java.awt.GridBagLayout());

        jPanel1.setBorder(new javax.swing.border.TitledBorder("Files"));
        jLabel1.setText("Path");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        jPanel1.add(jLabel1, gridBagConstraints);

        pathTF.setText("No Directory Selected");
        pathTF.setMaximumSize(new java.awt.Dimension(500, 20));
        pathTF.setPreferredSize(new java.awt.Dimension(50, 20));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.ipadx = 129;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 0, 0);
        jPanel1.add(pathTF, gridBagConstraints);

        pathB.setText("...");
        pathB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                pathBActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.ipadx = -21;
        gridBagConstraints.ipady = -3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHEAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 1, 0, 0);
        jPanel1.add(pathB, gridBagConstraints);

        jLabel2.setText("Hologram");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 0, 0);
        jPanel1.add(jLabel2, gridBagConstraints);

        holoB.setText("...");
        holoB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                holoBActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.ipadx = -21;
        gridBagConstraints.ipady = -3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 1, 0, 0);
        jPanel1.add(holoB, gridBagConstraints);

        holoCB.setEditable(true);
        holoCB.setMaximumRowCount(5);
        holoCB.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "No Image Selected" }));
        holoCB.setMaximumSize(new java.awt.Dimension(138, 22));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipadx = 30;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 6, 0, 0);
        jPanel1.add(holoCB, gridBagConstraints);

        jLabel3.setText("Reference");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 0, 0);
        jPanel1.add(jLabel3, gridBagConstraints);

        refCB.setEditable(true);
        refCB.setMaximumRowCount(5);
        refCB.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "No Image Selected" }));
        refCB.setMaximumSize(new java.awt.Dimension(138, 22));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipadx = 30;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 6, 0, 0);
        jPanel1.add(refCB, gridBagConstraints);

        rholoB.setText("...");
        rholoB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rholoBActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.ipadx = -21;
        gridBagConstraints.ipady = -3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 1, 0, 0);
        jPanel1.add(rholoB, gridBagConstraints);

        jButton1.setText("Reset File List");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.insets = new java.awt.Insets(5, 16, 0, 0);
        jPanel1.add(jButton1, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        getContentPane().add(jPanel1, gridBagConstraints);

        jPanel2.setLayout(new java.awt.GridBagLayout());

        jPanel2.setBorder(new javax.swing.border.TitledBorder("Sideband"));
        jLabel4.setText("x");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        jPanel2.add(jLabel4, gridBagConstraints);

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

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.ipadx = 49;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 0, 0);
        jPanel2.add(xTF, gridBagConstraints);

        jLabel5.setText("y");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        jPanel2.add(jLabel5, gridBagConstraints);

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

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.ipadx = 49;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 0, 0);
        jPanel2.add(yTF, gridBagConstraints);

        jLabel6.setText("Radius");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 20, 0, 0);
        jPanel2.add(jLabel6, gridBagConstraints);

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

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 7, 0, 0);
        jPanel2.add(radiusTF, gridBagConstraints);

        jButton4.setText("Select from FFT");
        jButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton4ActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.ipadx = 11;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 24, 0, 0);
        jPanel2.add(jButton4, gridBagConstraints);

        jLabel8.setText("Ratio");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 20, 0, 0);
        jPanel2.add(jLabel8, gridBagConstraints);

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

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 7, 0, 0);
        jPanel2.add(ratioTF, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        getContentPane().add(jPanel2, gridBagConstraints);

        jPanel3.setLayout(new java.awt.GridBagLayout());

        jPanel3.setBorder(new javax.swing.border.TitledBorder("Reconstruct"));
        jButton5.setText("Reconstruct");
        jButton5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton5ActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.ipadx = 9;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 3, 4, 0);
        jPanel3.add(jButton5, gridBagConstraints);

        amplitudeCB.setText("Amplitude");
        amplitudeCB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                amplitudeCBActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(7, 5, 0, 46);
        jPanel3.add(amplitudeCB, gridBagConstraints);

        phaseCB.setText("Phase");
        phaseCB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                phaseCBActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(7, 3, 0, 0);
        jPanel3.add(phaseCB, gridBagConstraints);

        jLabel7.setText("Extract:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = new java.awt.Insets(7, 50, 0, 0);
        jPanel3.add(jLabel7, gridBagConstraints);

        butterCB.setText("Butterworth Filter");
        butterCB.setMaximumSize(new java.awt.Dimension(135, 25));
        butterCB.setMinimumSize(new java.awt.Dimension(135, 25));
        butterCB.setPreferredSize(new java.awt.Dimension(135, 25));
        butterCB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                butterCBActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.ipady = -10;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 80, 0, 0);
        jPanel3.add(butterCB, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipady = -10;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        getContentPane().add(jPanel3, gridBagConstraints);

        pack();
    }
    // </editor-fold>//GEN-END:initComponents

    private void radiusTFFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_radiusTFFocusLost
        radius = getInteger(radiusTF);
    }//GEN-LAST:event_radiusTFFocusLost

    private void ratioTFFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_ratioTFFocusLost
        ratio = getInteger(ratioTF);
    }//GEN-LAST:event_ratioTFFocusLost

    private void yTFFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_yTFFocusLost
        sideCenter.y = getInteger(yTF);
    }//GEN-LAST:event_yTFFocusLost

    private void xTFFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_xTFFocusLost
        sideCenter.x = getInteger(xTF);
    }//GEN-LAST:event_xTFFocusLost

    private void jButton5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton5ActionPerformed
        operate();
    }//GEN-LAST:event_jButton5ActionPerformed

    private void amplitudeCBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_amplitudeCBActionPerformed
        amplitude=getBoolean(amplitudeCB);
    }//GEN-LAST:event_amplitudeCBActionPerformed

    private void phaseCBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_phaseCBActionPerformed
        phase=getBoolean(phaseCB);
    }//GEN-LAST:event_phaseCBActionPerformed

    private void butterCBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_butterCBActionPerformed
        butterworth=getBoolean(butterCB);
    }//GEN-LAST:event_butterCBActionPerformed

    private void yTFActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_yTFActionPerformed
        sideCenter.y=getInteger(yTF);
    }//GEN-LAST:event_yTFActionPerformed

    private void xTFActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_xTFActionPerformed
        sideCenter.x=getInteger(xTF);
    }//GEN-LAST:event_xTFActionPerformed

    private void ratioTFActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ratioTFActionPerformed
        ratio=getInteger(ratioTF);
    }//GEN-LAST:event_ratioTFActionPerformed

    private void radiusTFActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_radiusTFActionPerformed
        radius=getInteger(radiusTF);
    }//GEN-LAST:event_radiusTFActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        initFileList(holoCB);
        initFileList(refCB);
    }//GEN-LAST:event_jButton1ActionPerformed

    private void rholoBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rholoBActionPerformed
        addFileToList(refCB);
    }//GEN-LAST:event_rholoBActionPerformed

    private void holoBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_holoBActionPerformed
        addFileToList(holoCB);
    }//GEN-LAST:event_holoBActionPerformed

    private void pathBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_pathBActionPerformed
       setDir(pathTF);
    }//GEN-LAST:event_pathBActionPerformed

    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed
        sidebandFromFFT();
    }//GEN-LAST:event_jButton4ActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox amplitudeCB;
    private javax.swing.JCheckBox butterCB;
    private javax.swing.JButton holoB;
    private javax.swing.JComboBox holoCB;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton5;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
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
    private javax.swing.JTextField xTF;
    private javax.swing.JTextField yTF;
    // End of variables declaration//GEN-END:variables
    
}
