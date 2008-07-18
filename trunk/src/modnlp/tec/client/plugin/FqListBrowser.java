/**
 *  © 2006 S Luz <luzs@cs.tcd.ie>
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2,
 * or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
*/
package modnlp.tec.client.plugin;

import modnlp.idx.headers.HeaderDBManager;
import modnlp.tec.client.Plugin;
import modnlp.tec.client.Browser;
import modnlp.tec.client.TecClientRequest;
import modnlp.idx.database.Dictionary;

import java.awt.Font;
import java.awt.Dimension;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.FocusListener;
import java.awt.event.FocusEvent;

import java.net.URL;
import java.net.HttpURLConnection;
import javax.swing.JFrame;
import java.io.BufferedReader;
import javax.swing.JTextField;
import javax.swing.JButton;
import javax.swing.Timer;
import java.io.PrintWriter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.JTable;
import javax.swing.table.JTableHeader;
import javax.swing.JScrollPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JLabel;
import java.util.StringTokenizer;
import java.io.PipedWriter;
import java.io.PipedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import javax.swing.JOptionPane;
import java.util.Vector;
import java.util.Collections;
import java.awt.event.MouseAdapter;
import javax.swing.table.TableColumnModel;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.JFileChooser;
import java.io.File;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.Comparator;
import java.awt.event.MouseEvent;
import java.text.NumberFormat;
/**
 *  Basic Frequency list browser
 *
 * @author  S Luz &#60;luzs@cs.tcd.ie&#62;
 * @version <font size=-1>$Id: $</font>
 * @see  
*/
public class FqListBrowser extends JFrame
  implements  Runnable, Plugin, FocusListener
{
  
  private Thread ftwThread;

  private BufferedReader input;
  private JFrame thisFrame = null;

  private int MAXCHUNKSIZE = 900;
  private int NOCOLUMNS = 40;
  private HttpURLConnection exturlConnection;
  int maxListSize = 500;
  int dldCount = 0;
  double ttratio = 0;
  int notokens = 0;
  int dldi = 0;
  Timer dld_timer;
  JTextField maxListField = new JTextField(""+maxListSize, 4);
  JButton saveButton = new JButton("Save");

  private PrintWriter fqlout = null;

  DefaultTableModel model = new DefaultTableModel();
  DefaultTableModel noCaseModel = null; 
  JTable table = new JTable(model);
  JLabel statsLabel = new JLabel("...");
  JLabel scorpusLabel = new JLabel(" full corpus ");
  private JProgressBar progressBar;

  private static String title = new String("MODNLP Plugin: FqListBrowser 0.1"); 
  private Browser parent = null;
  private boolean guiLayoutDone = false;

  public FqListBrowser() {
    thisFrame = this;
  }

  public void setParent(Object p){
    parent = (Browser)p;
  }

  public void activate() {
    if (guiLayoutDone){
      setVisible(true);
      return;
    }
    model.addColumn("Rank");
    model.addColumn("Type");
    model.addColumn("Frequency");
    model.addColumn("% total");
    TableColumnModel tcm = table.getColumnModel();
    tcm.setColumnMargin(10);
    TableColumn tcr = tcm.getColumn(0);
    tcr.setPreferredWidth(40);
    tcm.getColumn(1).setPreferredWidth(150);
    TableColumn tcf = tcm.getColumn(2);
    tcf.setPreferredWidth(100);
    TableColumn tcp = tcm.getColumn(3);
    tcp.setPreferredWidth(60);
    FNumberRenderer fnr = new FNumberRenderer();
    PctRenderer pcr = new PctRenderer();
    tcr.setCellRenderer(fnr);
    tcf.setCellRenderer(fnr);
    tcp.setCellRenderer(pcr);
    table.setAutoCreateColumnsFromModel(false);
    
    table.setPreferredScrollableViewportSize(new Dimension(500, 500));
    JTableHeader header = table.getTableHeader();
    
    header.addMouseListener(new ColumnHeaderListener());    
    
    JScrollPane scrollPane = new JScrollPane(table);
    JButton dismissButton = new JButton("Quit");
    scrollPane.setPreferredSize(new Dimension(600, 600));
    dismissButton.addActionListener(new QuitListener());
    saveButton.addActionListener(new SaveListener());
    JButton go = new JButton("Get list");
    go.addActionListener(new GoListener());
    maxListField.setToolTipText("Limit list size to this many types. Set to 0 for whole list.");
        
    JPanel pa = new JPanel();
    pa.add(go);
    pa.add(new JLabel(" of "));
    pa.add(maxListField);
    pa.add(new JLabel("most frequent words in"));
    pa.add(scorpusLabel);
    pa.add(saveButton);
    pa.add(new JLabel("     "));
    pa.add(dismissButton);
    progressBar = new JProgressBar(0,800);
    progressBar.setStringPainted(true);
    dld_timer = new Timer(300, new ActionListener() {
        public void actionPerformed(ActionEvent evt) {
          //Int perct = (Int) (progressBar.getPercentComplete()*100);
          progressBar.setValue(dldi++ % 6);
          if ( dldCount > 0 )
            {
              dld_timer.stop();
              progressBar.setString("Done");
              progressBar.setValue(progressBar.getMaximum());
             
            }
        }
      });

    JPanel pa2 = new JPanel(new FlowLayout(FlowLayout.LEFT));
    pa2.add(progressBar);
    pa2.add(statsLabel);
    
    getContentPane().add(pa, BorderLayout.NORTH);
    getContentPane().add(scrollPane, BorderLayout.CENTER);
    getContentPane().add(pa2, BorderLayout.SOUTH);

    addFocusListener(this);
    //textArea.setFont(new Font("Courier", Font.PLAIN, parent.getFontSize()));
    saveButton.setEnabled(false);
    pack();
    setVisible(true);
    guiLayoutDone = true;
  }

  public void focusGained(FocusEvent e){
    checkSubCorpusSelectionStatus();
  }
  public void focusLost(FocusEvent e){
    checkSubCorpusSelectionStatus();
  }

  private void checkSubCorpusSelectionStatus (){
    System.err.println("----------------");
    if (parent.isSubCorpusSelectionON())
      scorpusLabel.setText(" subcorpus ");
    else
      scorpusLabel.setText(" full corpus ");
  }

  public void run() {
    String textLine = "";
    StringBuffer cstats = new StringBuffer();
    try {
      int i= 0;
      dldCount = 0;
      int rank = 1;
      int ttok = 0;
      if (parent.isStandAlone()) {
        (new FqlPrinter()).start();
      }
      NumberFormat nf =  NumberFormat.getInstance(); 
      //new java.text.DecimalFormat("###,###,###,###.#####");
      //NumberFormat pf =  NumberFormat.getIntegerInstance(); 
      // new java.text.DecimalFormat("###.###");

      while ((maxListSize == 0 || dldCount < maxListSize) && (textLine = input.readLine()) != null) {
        if ( i < MAXCHUNKSIZE ) {
          StringTokenizer st = new StringTokenizer(textLine, "\t");
          Object [] row = {new Integer(st.nextToken()), st.nextToken(), st.nextToken(), null};
          if (row[0].toString().equals("0")) {
            ttratio = (new Double(row[2].toString())).doubleValue();
            cstats.append(row[1]+": "+nf.format(ttratio)+";  ");
            if (row[1].equals(modnlp.idx.database.Dictionary.TTOKENS_LABEL)){
              notokens = (new Integer(row[2].toString())).intValue();
              progressBar.setString("Displaying list...");
              progressBar.setMaximum((int)(notokens*ttratio));
              progressBar.setValue(dldCount++);
            }
          }
          else {
            row[2] = new Integer(row[2].toString());
            row[3] = new Float((float)((Integer)row[2]).intValue()/notokens);
            model.addRow(row);
            progressBar.setValue(dldCount++);
          }
        }
        else{
          model.fireTableStructureChanged();
          i = 0;
        }
      }
      statsLabel.setText(cstats.toString());
      saveButton.setEnabled(true);
    }
    catch (Exception e)
      {
        statsLabel.setText(cstats.toString());
        saveButton.setEnabled(true);
        System.err.println("Exception: " + e);
        System.err.println("Line: " + textLine);
        e.printStackTrace();
      }
  }

  public void start() {
    model.setRowCount(0);

    try {
      if (parent.isStandAlone()) {
        PipedWriter pipeOut = new PipedWriter();
        input = new BufferedReader(new PipedReader(pipeOut));
        fqlout = new PrintWriter(pipeOut); 
      }
      else {
        TecClientRequest rq = new TecClientRequest();
        rq.setServerURL("http://"+parent.SERVER);
        rq.setServerPORT(parent.PORTNUM);
        rq.put("request","freqlist");
        rq.setServerProgramPath("/freqlist");
        URL exturl = new URL(rq.toString());
        exturlConnection = (HttpURLConnection) exturl.openConnection();
        //exturlConnection.setUseCaches(false);
        exturlConnection.setRequestMethod("GET");
        input = new
          BufferedReader(new
                         InputStreamReader(exturlConnection.getInputStream() ));
      }
    }
    catch(IOException e)
      {
        System.err.println("Exception: couldn't create stream socket"+e);
        JOptionPane.showMessageDialog(null, "Couldn't get frequency list: "+e);      
      }
    
    if ( ftwThread == null ){
      ftwThread = new Thread(this);
      ftwThread.setPriority (Thread.MIN_PRIORITY);
      ftwThread.start();
    }
  }

  public void stop() {
    if ( ftwThread != null ){
      //ftwThread.stop();
      if (!parent.isStandAlone() && exturlConnection != null)
        exturlConnection.disconnect();
      ftwThread = null;
    }
  }


  public void sortRowsBy(int colIndex, boolean ascending) {
    Vector data = model.getDataVector();
    Collections.sort(data, new ColumnSorter(colIndex, ascending));
    model.fireTableStructureChanged();
  }
    

  class ColumnSorter implements Comparator {
        int colIndex;
        boolean ascending;
        ColumnSorter(int colIndex, boolean ascending) {
            this.colIndex = colIndex;
            this.ascending = ascending;
        }
        public int compare(Object a, Object b) {
            Vector v1 = (Vector)a;
            Vector v2 = (Vector)b;
            Object o1 = v1.get(colIndex);
            Object o2 = v2.get(colIndex);
    
            // Treat empty strains like nulls
            if (o1 instanceof String && ((String)o1).length() == 0) {
                o1 = null;
            }
            if (o2 instanceof String && ((String)o2).length() == 0) {
                o2 = null;
            }
    
            // Sort nulls so they appear last, regardless
            // of sort order
            if (o1 == null && o2 == null) {
                return 0;
            } else if (o1 == null) {
                return 1;
            } else if (o2 == null) {
                return -1;
            } else if (colIndex != 1) {
              Integer io1;
              Integer io2;
              try {
                io1 = new Integer(o1.toString());
              }
              catch (NumberFormatException ne) {
                return 1;
              }
              try {
                io2 = new Integer(o2.toString());
              }
              catch (NumberFormatException ne) {
                return -1;
              }
              if (ascending) 
                return io1.compareTo(io2);
              else
                return io2.compareTo(io1);
            } else {
              String s1 =  o1.toString().toLowerCase();
              String s2 =  o2.toString().toLowerCase();
              if (ascending) {
                return s1.compareTo(s2);
              } else {
                return s2.compareTo(s1);
              }
            }
        }
    }


  public class ColumnHeaderListener extends MouseAdapter {
    boolean [] colAscend = {true, false, false, false}; // rank is in ascending, frequency and density in descending, and type in no particular order
    public void mouseClicked(MouseEvent evt) {
      JTable table = ((JTableHeader)evt.getSource()).getTable();
      TableColumnModel colModel = table.getColumnModel();
      
      int vColIndex = colModel.getColumnIndexAtX(evt.getX());
      int mColIndex = table.convertColumnIndexToModel(vColIndex);

      // Return if not clicked on any column header
      if (vColIndex == -1) {
        return;
      }
      colAscend[mColIndex] = !colAscend[mColIndex];
      sortRowsBy(mColIndex, colAscend[mColIndex]);
    }
  }

  class QuitListener implements ActionListener
  {
    public void actionPerformed(ActionEvent e)
    {
      stop();
      thisFrame.setVisible(false);
      //dispose();
    }
  }

  class GoListener implements ActionListener
  {
    public void actionPerformed(ActionEvent e)
    {
      stop();
      Integer i;
      dld_timer.start();
      progressBar.setString("Building index (be patient)");
      progressBar.setMaximum(5);
      progressBar.setValue(0);
      //statsLabel.setText("Retrieving frequency list...");
      try {
        i = new Integer(maxListField.getText());
        maxListSize = i.intValue();
      }
      catch (NumberFormatException ex){
        maxListField.setText(""+maxListSize);
      }
      start();
    }
  }

  class SaveListener implements ActionListener {
    public void actionPerformed(ActionEvent e)
    {
      try
        {
          JFileChooser filedial = new JFileChooser();
          int returnVal = filedial.showDialog(thisFrame, "Save CSV file to disk");
          if (returnVal == JFileChooser.APPROVE_OPTION)
            {
              File file = filedial.getSelectedFile();
              //System.out.println(file.getName());
              PrintWriter dlf =
                new PrintWriter(new BufferedWriter(new FileWriter(file)));
              Object[] va =  (model.getDataVector()).toArray();
              dlf.println("Rank order\tType\tFrequency");
              for (int i = 0; i < va.length ; i++) {
                dlf.println(((Vector)va[i]).get(0)+"\t"
                           +((Vector)va[i]).get(1)+"\t"
                           +((Vector)va[i]).get(2));
              }
              dlf.close();
            }
        }
      catch (Exception ex) {
        JOptionPane.showMessageDialog(parent, "Error writing freqency list" + ex,
                                      "Error!", JOptionPane.ERROR_MESSAGE);
      }
    }
  }

  class FqlPrinter extends Thread {
    public FqlPrinter () {
      super("Frequency list producer");
    }
    public void run (){
      try {
        Dictionary d = parent.getDictionary();
        if (parent.subCorpusSelected()){
          HeaderDBManager hdbm = parent.getHeaderDBManager();
          d.printSortedFreqList(fqlout, maxListSize,
                                hdbm.getSubcorpusConstraints(parent.getXQueryWhere())); 
        }
        else
          d.printSortedFreqList(fqlout, maxListSize);
      } catch (Exception e) {
        System.err.println("FqListBrowser opening header DB: " + e);
        e.printStackTrace();
      }
    }
  }

  public class FNumberRenderer extends DefaultTableCellRenderer {
    NumberFormat formatter = null;

    public FNumberRenderer() {
      super();
      setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
      formatter = NumberFormat.getInstance();
    }

    public void setValue(Object value) {
      if ((value != null) && (value instanceof Number)) {
        Number numberValue = (Number) value;
        value = formatter.format(numberValue.doubleValue());
      }
      super.setValue(value);
    }
  }

  public class PctRenderer extends DefaultTableCellRenderer {
    NumberFormat formatter = null;

    public PctRenderer() {
      super();
      setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
      formatter = NumberFormat.getPercentInstance();
      formatter.setMinimumFractionDigits(3);
    }

    public void setValue(Object value) {
      if ((value != null) && (value instanceof Number)) {
        Number numberValue = (Number) value;
        value = formatter.format(numberValue.doubleValue());
      } 
      super.setValue(value);
    }
  }


}

