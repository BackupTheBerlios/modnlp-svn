/**
 *   Copyright (c) 2011 G Lynch. All Rights Reserved.
 *                 2012 G Lynch, S Luz
 *   This program  is free software; you can redistribute it and/or
 *   modify it under the terms of the GNU General Public License
 *   as published by the Free Software Foundation; either version 2
 *   of the License, or (your option) any later version.
 *   
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
     
 *   You should have received a copy of the GNU General Public License
 *   along with this program; if not, write to the Free Software
 *   Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 **/ 


package modnlp.capte;

import javax.swing.JOptionPane;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JFrame;
import javax.swing.JTextArea;
import javax.swing.JFileChooser;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.text.Document;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;
import javax.swing.table.TableModel;
//import javax.jnlp.*;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CodingErrorAction;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

//import net.sourceforge.jnlp.util.FileUtils;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
//import javax.swing.table.RowNumberTable;
import org.apache.commons.httpclient.methods.multipart.StringPart;
import javax.swing.event.ChangeEvent;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import javax.swing.ListSelectionModel;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import javax.swing.UIManager;
import java.awt.event.KeyEvent;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.Dimension;
import java.awt.BorderLayout;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JTable;
import java.util.Vector;
import javax.swing.JCheckBox;
import javax.swing.JButton;
import javax.swing.event.CellEditorListener;
import java.awt.event.ItemListener;
import java.awt.event.ActionListener;
import java.awt.Component;

public class AlignmentInterfaceWS extends JPanel implements ActionListener, ItemListener, CellEditorListener {
  /**
   * Basic prototype of components for word alignment
   * stuff in Java
   *
   * Date: March/April 2011
   * Author: Gerard Lynch
   */
  private static final long serialVersionUID = 1L;
  public static String html;
  public static String alignpath,dictpath;
  static private final String newline = "\n";
  JButton openButton, saveButton, alignButton;
  public static JCheckBox splitButton, convLine ;
  public static JButton mergeSource, mergeTarget,export,newSegment,deleteSegment,moveUp,moveDown;
  public static JButton lockSelected, unlockSelected,reAlign;
  public static ExampleTableModel ex;
  public static String sourceFile, targetFile, outputFile;
  public static String sourcel, targetl;
  public static String [] configs;
  public static String outputtable;
  public static String htmldisplay;
  public static boolean doSplit;
  public static boolean lineConvert;
  public static Vector<Object[]> data;
  public static Vector<String> cols; 
  public static boolean AlreadyRun; // Legacy variable used when overwriting stuff
  public static JTable table;  //The main table which stores the aligned output files
  public static int reNumber = 0; //How many times the realigner has been run
  //public static AlignmentService aserver; //This does the main alignment for the system
  //public JFrame frame;
  public JFrame edit;
  JTextField sl; 
  JTextField tl;
  JLabel sol;
  JLabel tol;
  JTextArea log;
  JFileChooser fc;

  public AlignmentInterfaceWS(){
    //Setup file chooser 
    super(new BorderLayout());
    Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
    this.setSize(dim.width, dim.height);

    //Create the log first, because the action listeners
    //need to refer to it.
    log = new JTextArea(5,20);
    log.setMargin(new Insets(5,5,5,5));
    log.setEditable(false);
    JScrollPane logScrollPane = new JScrollPane(log);

    //Create a file chooser
    fc = new JFileChooser();

    //Uncomment one of the following lines to try a different
    //file selection mode.  The first allows just directories
    //to be selected (and, at least in the Java look and feel,
    //shown).  The second allows both files and directories
    //to be selected.  If you leave these lines commented out,
    //then the default mode (FILES_ONLY) will be used.
    //
    //fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
    //fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);

    //Create the open button.  We use the image from the JLF
    //Graphics Repository (but we extracted it from the jar).
    alignButton = new JButton("Align Texts");
    alignButton.addActionListener(this);
    openButton = new JButton("Open Source File...");
    openButton.addActionListener(this);

    //Create the save button.  We use the image from the JLF
    //Graphics Repository (but we extracted it from the jar).
    saveButton = new JButton("Open Target File...");
    saveButton.addActionListener(this);

    //Create two JText fields for input source and target language codes

    sl = new JTextField("en");
    sourcel = sl.getText();
    sol = new JLabel("Source");
    tl = new JTextField("es");
    targetl = tl.getText();
    tol = new JLabel("Target");
    splitButton = new JCheckBox("Splitter");
    splitButton.setMnemonic(KeyEvent.VK_C);
    splitButton.setSelected(true);
    splitButton.addItemListener(this);
    convLine = new JCheckBox("Convert EOL");
    convLine.setMnemonic(KeyEvent.VK_S);
    convLine.setSelected(true);
    convLine.addItemListener(this);
    sl.addActionListener(this);
    tl.addActionListener(this);

    //For layout purposes, put the buttons in a separate panel
    JPanel buttonPanel = new JPanel(); //use FlowLayout
    buttonPanel.add(openButton);
    buttonPanel.add(saveButton);

    //Make another panel for the aligner button
    JPanel alignPanel = new JPanel();
    alignPanel.add(convLine);
    alignPanel.add(splitButton);
    alignPanel.add(sol);
    alignPanel.add(sl);
    alignPanel.add(tol);
    alignPanel.add(tl);
    alignPanel.add(alignButton);

    //Add the buttons and the log to this panel.
    add(buttonPanel, BorderLayout.PAGE_START);
    add(logScrollPane, BorderLayout.CENTER);
    add(alignPanel,BorderLayout.PAGE_END);
  }

  public static void main(String args []){
    try{
      //set splitting action equal to true
      //UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
      doSplit = true;
      //default AlreadyRun to false
      AlreadyRun = false;
      //lineConvert = true;
      lineConvert = true;
      // Read config file
      boolean isMac = false;
      boolean isWin = false;
      boolean isUnix = false;
      String tmp = "";
      String winsep = "\\";
      String unixsep = "/";
      String ostype= System.getProperty("os.name").toLowerCase();
      System.out.println("Operating system type =>"+ ostype);
      if(ostype.indexOf("win") >= 0){
        isWin = true;
        isUnix = false;
        isMac = false;
      }
      else if((ostype.indexOf("nix") >= 0) || (ostype.indexOf("nux") >=0)){
        isUnix = true;
        isWin = false;
        isMac = false;
      }
      else if(ostype.indexOf("mac") >= 0){
        isWin = false;
        isUnix = false;
        isMac = true;
      }
      else{
        throw new UnsupportedOperationException("your OS is not supported!");
      }
      //creating and showing this application's GUI.
      SwingUtilities.invokeLater(new Runnable() {
          public void run() {
            //Turn off metal's use of bold fonts
            UIManager.put("swing.boldMetal", Boolean.FALSE); 
            createAndShowGUI();
          }
        });
    }
    catch(Exception e){
      e.printStackTrace();
    }
  }//end of main method


  /*
   * Methods for implementing file processing options
   *
   *
   */
  public void itemStateChanged(ItemEvent e){

    if(e.getSource() == splitButton){
      if (e.getStateChange() == ItemEvent.DESELECTED){

        doSplit = false;
        System.out.println("Sentence splitting disabled");
      }
      else{

        doSplit = true;
        System.out.println("Sentence splitting enabled");
      }
    }
    else if(e.getSource() == convLine){
      if (e.getStateChange() == ItemEvent.DESELECTED){

        lineConvert = false;
        System.out.println("No line ending conversion");
      }
      else{

        lineConvert = true;
        System.out.println("line ending conversion enabled");
      }
    }
  }

  public void actionPerformed(ActionEvent e) {

    //Handle open button action.
    if (e.getSource() == openButton) {
      int returnVal = fc.showOpenDialog(AlignmentInterfaceWS.this);

      if (returnVal == JFileChooser.APPROVE_OPTION) {
        File file = fc.getSelectedFile();
        //This is where a real application would open the file.
        sourceFile = file.getAbsolutePath();
        log.append("Source File: " + sourceFile + "." + newline);
      } else {
        log.append("Open command cancelled by user." + newline);
      }
      log.setCaretPosition(log.getDocument().getLength());

    }
    //deleteSegment
    else if(e.getSource() == deleteSegment){
      Object [] o ;
      String ivalue ;
      int [] selected = table.getSelectedRows();
      ExampleTableModel em = (ExampleTableModel)table.getModel();

      if(selected.length < 1){

        System.out.println("Please select at least one row");
        JOptionPane.showMessageDialog(edit,"Please select at least one row");
      }
      else{
        System.out.println("Deleting rows...");
        for(int i = selected.length -1;i > -1;i--){
          em.removeRow(selected[i]);
          //update numbers
             
          System.out.println("Removed row " + selected[i]);
        }
      }
      //Table should update itself automatically
    }
    else if(e.getSource() == moveUp){
      int [] selected = table.getSelectedRows();
      ExampleTableModel em = (ExampleTableModel)table.getModel();
      if(selected.length > 1){
        System.out.println("You can only move one segment at a time!");
        JOptionPane.showMessageDialog(edit,"You can only move one segment at a time!");
      }
      else if(selected[0] == 0){
        System.out.println("Can't move up anymore!");
        JOptionPane.showMessageDialog(edit,"Can't move up anymore!");
      }
      else{
        System.out.println("Moving " + selected[0]);
        em.moveSegmentUp(selected[0]);
        //Table should repaint
      }
    }
    else if(e.getSource() == moveDown){

      int [] selected = table.getSelectedRows();
      ExampleTableModel em = (ExampleTableModel)table.getModel();
      if(selected.length > 1){

        System.out.println("You can only move one segment at a time!");
        JOptionPane.showMessageDialog(edit,"You can only move one segment at a time!");
      }
      else if(selected[0] == em.getRowCount() - 1){

        System.out.println("Can't move down anymore!");
        JOptionPane.showMessageDialog(edit,"Can't move down anymore");
      }
      else{
        System.out.println("Moving " + selected[0]);
        em.moveSegmentDown(selected[0]);
        //Table should repaint
      }
    }
    else if(e.getSource() == mergeSource){
      int [] selected = table.getSelectedRows();
      ExampleTableModel em = (ExampleTableModel)table.getModel();

      if(selected.length < 2 || selected.length > 2){

        System.out.println("Please select two rows to merge ");
        JOptionPane.showMessageDialog(edit,"Please select two rows to merge");
      }
      else if(selected[0] - selected[1] > 1 || selected[0] - selected[1] < -1){

        System.out.println("Can only merge adjacent rows");
        JOptionPane.showMessageDialog(edit,"Can only merge adjacent rows");
      }
      else{
        System.out.println("Merging source in rows " + selected[0] + " " + selected[1]);
        em.mergeSource(selected[0],selected[1]); 
      }
    }
    else if(e.getSource() == mergeTarget){
      int [] selected = table.getSelectedRows();
      ExampleTableModel em = (ExampleTableModel)table.getModel();

      if(selected.length < 2 || selected.length > 2){

        System.out.println("Please select two rows to merge ");
        JOptionPane.showMessageDialog(edit,"Please select two rows to merge");
      }
      else if(selected[0] - selected[1] > 1 || selected[0] - selected[1] < -1){

        System.out.println("Can only merge adjacent rows");
        JOptionPane.showMessageDialog(edit,"Can only merge adjacent rows");
      }
      else{
        System.out.println("Merging target in " + selected[0] + " " + selected[1]);
        em.mergeTarget(selected[0],selected[1]);
      }

    }
    else if(e.getSource() == newSegment){
      int [] selected = table.getSelectedRows();
      ExampleTableModel em = (ExampleTableModel)table.getModel();

      if(selected.length < 1){

        System.out.println("Please select a position to insert at:");
        JOptionPane.showMessageDialog(edit,"Please select a position to insert at");
      }
      else{
        System.out.println("Inserting new segment at " + (selected[0] + 1));
        //insert empty string array
        Object [] sa = new Object[6];
        sa[0] = "";
        sa[1] = "";
        sa[2] = "0.0";
        sa[3] = new Boolean(false);
        sa[4] = "0";
        sa[5] = em.getValueAt((selected[0]),5) + "(+)";
                 
        em.insertRow(sa,(selected[0] + 1));
 
      }
    }
    else if(e.getSource() == lockSelected){
      int [] selected = table.getSelectedRows();
      ExampleTableModel em = (ExampleTableModel)table.getModel();

      if(selected.length < 1){

        System.out.println("Please select some rows to lock:");
        JOptionPane.showMessageDialog(edit,"Please select some rows to lock:");
      }
      else{
                 
        //lock selected rows

        for(int i = 0;i < selected.length;i++){
          em.lockRow(selected[i]);
          System.out.println("Locking row " + selected[i]);

        } 

      }
    }
    else if(e.getSource() == unlockSelected){
      int [] selected = table.getSelectedRows();
      ExampleTableModel em = (ExampleTableModel)table.getModel();

      if(selected.length < 1){

        System.out.println("Please select some rows to unlock:");
        JOptionPane.showMessageDialog(edit,"Please select some rows to unlock:");
      }
      else{
                 
        //lock selected rows
        for(int i = 0;i < selected.length;i++){
          em.unlockRow(selected[i]);
          System.out.println("Unlocking row " + selected[i]);
        } 

      }
    }
    else if(e.getSource() == reAlign){
      //  if(true){
      // JOptionPane.showMessageDialog(edit,"This feature is currently disabled");
      // }else{
      reNumber++;
      ExampleTableModel em = (ExampleTableModel)table.getModel();

      // Get list of locked segments
      // Find lowest locked segment
      // Realign from lowest locked segment
      // Join the realigned bit back up with the locked bit
      // refresh the table
      int lowestlock = 0;
      Vector<Object []> slice = new Vector<Object[]>();
      Vector<Object []> result = new Vector<Object[]>();
      Vector<Object []> locked = new Vector<Object []>() ;
      Boolean b = new Boolean(true);
      for(int i = 0;i< em.getRowCount();i++){
        b = (Boolean) em.getValueAt(i,3);
        if(b.booleanValue() == true){
          lowestlock = i;
        }
      }
 
      //get slice of table for realignment
      System.out.println("The lowest lock point is " + (lowestlock));
      System.out.println("Realigning from row:" + (lowestlock + 1) +" to : " + em.getRowCount() );
      System.out.println("Total size of realign array =:" + (em.getRowCount() - (lowestlock)));
      //Get locked bits
      for(int h = 0; h < lowestlock + 1;h++){
            	  
        locked.add(em.getRow(h));
      }
      //Get bits to realign
      for(int j = lowestlock + 1;j<em.getRowCount();j++){

        slice.add(em.getRow(j));

      }
      //flush 
      em.flush();
      for(int z = 0;z < locked.size();z++){          	  
        em.insertRow(locked.get(z),z);
      }
      // System.out.println("Total size of array after bits removed  = " + (em.getRowCount()));
      //get the directory where the source files came from
      File parent = new File(sourceFile).getParentFile();
      String dir = parent.getAbsolutePath();
      //create files
      File sf = null;
      File tf = null;
      try{
        sf = File.createTempFile("source","tmp");
        tf = File.createTempFile("target","tmp");
      }catch(IOException ef){
        ef.printStackTrace();
      }
      System.out.println("Writing temp file:" + sf.getName());
      System.out.println("Writing temp file:" + tf.getName());
      // File mf = new File("merged.tmp");
      //get absolute paths
      String sourceF = sf.getAbsolutePath();
      String targetF = tf.getAbsolutePath();
      //String alignF = mf.getAbsolutePath();
      //write out source and target to files
      //NEW write files to server and return string
      String alignment = "";
      AlignerUtils.reWriteAlignment(targetF,sourceF,slice);
      try{
            	 
        alignment = AlignerUtils.MultiPartFileUpload(targetF,sourceF);
             
      }
      catch(IOException es){
        es.printStackTrace();
      }
      //convert the String to a Vector form
      result = AlignerUtils.StringToData(alignment,true,reNumber);
      // append the resultant file to the table
      //System.out.println("Total size of array before bits inserted  = " + (em.getRowCount()));
      for(int y = 0, z = em.getRowCount();y < result.size();y++,z++){

        em.insertRow(result.get(y),z);
        System.out.println("Inserting at position: " + z );
        System.out.println("Inserting from position: " + y);
                
      }
      //  System.out.println("Total size of array after bits inserted  = " + (em.getRowCount()));
      // }
    }
    else if (e.getSource() == saveButton) {
      int returnVal = fc.showSaveDialog(AlignmentInterfaceWS.this);
      if (returnVal == JFileChooser.APPROVE_OPTION) {
        File file = fc.getSelectedFile();
        //This is where a real application would save the file.
        targetFile = file.getAbsolutePath();
        log.append("Target File " + targetFile + "." + newline);
      } else {
        log.append("Save command cancelled by user." + newline);
      }
      log.setCaretPosition(log.getDocument().getLength());
    }
    else if (e.getSource() == export) {
      int returnVal = fc.showSaveDialog(AlignmentInterfaceWS.this);
      if (returnVal == JFileChooser.APPROVE_OPTION) {
        File file = fc.getSelectedFile();               
        outputFile = file.getAbsolutePath();
        //aserver.writeAlignment(targetFile,data);
        AlignerUtils.writeAlignment(outputFile, data);
        log.append(newline + "Saving " + outputFile + "." + newline);
      } else {
        log.append("Save command cancelled by user." + newline);
      }
      log.setCaretPosition(log.getDocument().getLength());
    }
    else if(e.getSource() == alignButton){

      if(sl.getText().length() >=2 && tl.getText().length() >=2){
                
        log.append("Attempting to align texts");
        sourcel = sl.getText();
        targetl = tl.getText();
        String aligned = "";
        try{
          aligned = AlignerUtils.MultiPartFileUpload(sourceFile,targetFile);
        }
        catch(IOException ed){
            	  
          ed.printStackTrace();
        }
        //Convert string to alignment format
        data = AlignerUtils.StringToData(aligned,false,0);

        int i = 0;

        //
        // AlreadyRun = true;
        if(i == 0){
          //log.setCaretPosition(log.getDocument().getLength());
          log.append("\nAutomatic alignment successful!");
          log.append("\nOpening display window......");
          //Set up the editor window
          JFrame edit = new JFrame("Alignment Editor");
          cols = new Vector<String>();
          cols.add("Source");
          cols.add("Target");
          cols.add("Score");
          cols.add("Lock");
          cols.add("Index");
          cols.add("Orig");
                   
          System.out.println("Size of data array " + data.size());
          System.out.println(data.get(0)[0]);
          ex = new ExampleTableModel(cols,data);
          //ex.addTableModelListener(this);
          table = new JTable(ex);
          table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

          TableColumnModel cmodel = table.getColumnModel();
                  
          cmodel.getColumn(0).setCellRenderer(new TextAreaRenderer());
          cmodel.getColumn(1).setCellRenderer(new TextAreaRenderer());
          cmodel.getColumn(2).setCellRenderer(new TextAreaRenderer());
          cmodel.getColumn(4).setCellRenderer(new TextAreaRenderer());
          cmodel.getColumn(5).setCellRenderer(new TextAreaRenderer());
          TextAreaEditor textEditor = new TextAreaEditor();
          textEditor.addCellEditorListener(this);
          cmodel.getColumn(0).setCellEditor(textEditor);
          cmodel.getColumn(1).setCellEditor(textEditor);
          cmodel.getColumn(2).setCellEditor(textEditor);      
          cmodel.getColumn(4).setCellEditor(textEditor); 
          cmodel.getColumn(5).setCellEditor(textEditor); 
          mergeTarget = new JButton("Merge target");
          mergeSource = new JButton("Merge source");
          export = new JButton("Export to File");
          newSegment = new JButton("Create New Segment");
          deleteSegment = new JButton("Delete Selected");
          moveUp = new JButton("Move Segment Up");
          moveDown = new JButton("Move Segment Down");
          lockSelected = new JButton("Lock Selected");
          unlockSelected = new JButton("Unlock Selected");
          reAlign = new JButton("Realign");
          reAlign.addActionListener(this);
          lockSelected.addActionListener(this);
          unlockSelected.addActionListener(this);
          mergeSource.addActionListener(this);
          mergeTarget.addActionListener(this);
          export.addActionListener(this);
          newSegment.addActionListener(this); 
          deleteSegment.addActionListener(this);
          moveUp.addActionListener(this);
          moveDown.addActionListener(this);
          JPanel control = new JPanel();
          JPanel manipulate = new JPanel();
          control.add(moveUp); 
          control.add(moveDown);
          control.add(mergeTarget);
          control.add(mergeSource);
          //control.add(export);
          control.add(newSegment);
          control.add(deleteSegment);
          manipulate.add(reAlign);
          manipulate.add(lockSelected);
          manipulate.add(unlockSelected);
          manipulate.add(export);
          edit.add(control,BorderLayout.PAGE_START);
          edit.add(manipulate,BorderLayout.PAGE_END);
          JScrollPane scr = new JScrollPane(table);
          //   JTable rowTable = new FirstRowNumberTable(table);
                  
          //scr.add(table);
          //scr.setRowHeaderView(rowTable);
          //scr.setCorner(JScrollPane.UPPER_LEFT_CORNER, rowTable.getTableHeader()); 
          scr.repaint();
          edit.add(scr,BorderLayout.CENTER);
          Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
          edit.setSize(screenSize.width - 4, screenSize.height - 50);
          int totwidth = screenSize.width - 50 ;
          table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
          cmodel.getColumn(0).setPreferredWidth((totwidth / 36) * 15);
          cmodel.getColumn(1).setPreferredWidth((totwidth / 36) *15);
          cmodel.getColumn(2).setPreferredWidth(totwidth / 36 * 2);
          cmodel.getColumn(3).setPreferredWidth(totwidth / 36 * 2);
          cmodel.getColumn(4).setPreferredWidth(totwidth / 36 * 2);
          cmodel.getColumn(4).setPreferredWidth(totwidth / 36 * 2);
          edit.validate();                // Make sure layout is ok

          //edit.setSize(1024,700);
          edit.setVisible(true);
		
        }
        else{
          //log.setCaretPosition(log.getDocument().getLength());
          log.append("\nAutomatic alignment unsuccessful..check error logs");
        }
      }  
      else{
        log.append("Please enter valid two letter language codes");
      }
      log.setCaretPosition(log.getDocument().getLength());

    }  
  }

  /** Returns an ImageIcon, or null if the path was invalid. */
  // protected static ImageIcon createImageIcon(String path) {
  //    java.net.URL imgURL = AlignmentPipeline.class.getResource(path);
  //    if (imgURL != null) {
  //        return new ImageIcon(imgURL);
  //   } else {
  //       System.err.println("Couldn't find file: " + path);
  //       return null;
  //   }
  // }

  /**
   * Create the GUI and show it.  For thread safety,
   * this method should be invoked from the
   * event dispatch thread.
   */
  private static void createAndShowGUI() {
    //Create and set up the window.
    JFrame frame = new JFrame("Aligner Version 1.1 (Beta)");
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    //Add content to the window.
    frame.add(new AlignmentInterfaceWS());

    //Display the window.
    frame.pack();
    frame.setVisible(true);
  }

  public JTable autoResizeColWidth(JTable table, DefaultTableModel model) {
    table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
    table.setModel(model);

    int margin = 5;

    for (int i = 0; i < table.getColumnCount(); i++) {
      int                     vColIndex = i;
      DefaultTableColumnModel colModel  = (DefaultTableColumnModel) table.getColumnModel();
      TableColumn             col       = colModel.getColumn(vColIndex);
      int                     width     = 0;

      // Get width of column header
      TableCellRenderer renderer = col.getHeaderRenderer();

      if (renderer == null) {
        renderer = table.getTableHeader().getDefaultRenderer();
      }

      Component comp = renderer.getTableCellRendererComponent(table, col.getHeaderValue(), false, false, 0, 0);

      width = comp.getPreferredSize().width;

      // Get maximum width of column data
      for (int r = 0; r < table.getRowCount(); r++) {
        renderer = table.getCellRenderer(r, vColIndex);
        comp     = renderer.getTableCellRendererComponent(table, table.getValueAt(r, vColIndex), false, false,
                                                          r, vColIndex);
        width = Math.max(width, comp.getPreferredSize().width);
      }

      // Add margin
      width += 2 * margin;

      // Set the width
      col.setPreferredWidth(width);
    }

    ((DefaultTableCellRenderer) table.getTableHeader().getDefaultRenderer()).setHorizontalAlignment(
                                                                                                    SwingConstants.LEFT);

    // table.setAutoCreateRowSorter(true);
    table.getTableHeader().setReorderingAllowed(false);

    return table;
  }

  //JTable methods, may move to separate class
  public static int getPreferredRowHeight(JTable table, int rowIndex, int margin) {
    // Get the current default height for all rows
    int height = table.getRowHeight();

    // Determine highest cell in the row
    for (int c=0; c<table.getColumnCount(); c++) {
      TableCellRenderer renderer = table.getCellRenderer(rowIndex, c);
      Component comp = table.prepareRenderer(renderer, rowIndex, c);
      int h = comp.getPreferredSize().height + 2*margin;
      height = Math.max(height, h);
    }
    return height;
  }

  public static void packRows(JTable table, int margin) {
    packRows(table, 0, table.getRowCount(), margin);
  }

  // For each row >= start and < end, the height of a
  // row is set to the preferred height of the tallest cell
  // in that row.
  public static void packRows(JTable table, int start, int end, int margin) {
    for (int r=0; r<table.getRowCount(); r++) {
      // Get the preferred height
      int h = getPreferredRowHeight(table, r, margin);

      // Now set the row height using the preferred height
      if (table.getRowHeight(r) != h) {
        table.setRowHeight(r, h);
      }
    }
  }


  public void editingStopped(ChangeEvent e){


  }
  public void editingCanceled(ChangeEvent e){

  }
  /*This method should create a new filename given a filename, extension and postfix string
   *	Used for creating tmp files etc
   * 
   */






}
