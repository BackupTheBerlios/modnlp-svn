/**
 *  © 2008 S Luz <luzs@cs.tcd.ie>
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
import modnlp.tec.client.ConcordanceBrowser;
import modnlp.tec.client.ConcordanceObject;
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
import java.util.Vector;
import java.util.HashMap;
import java.util.Enumeration;
import java.util.Iterator;
import java.awt.event.MouseEvent;
import java.text.NumberFormat;
/**
 *  Basic concordance tree generator
 *
 * @author  S Luz &#60;luzs@acm.org&#62;
 * @version <font size=-1>$Id: $</font>
 * @see  
*/

public class ConcordanceTree extends JFrame
  implements Plugin
{
  
  private Thread ftwThread;
  private JFrame thisFrame = null;

  JButton saveButton = new JButton("Save");

  private PrintWriter fqlout = null;

  private static String title = new String("MODNLP Plugin: ConcordanceTree 0.1"); 
  private ConcordanceBrowser parent = null;
  private boolean guiLayoutDone = false;

  public ConcordanceTree() {
    thisFrame = this;
  }

  // plugin interface method
  public void setParent(Object p){
    parent = (ConcordanceBrowser)p;
  }

  // plugin interface method
  public void activate() {
    if (guiLayoutDone){
      setVisible(true);
      return;
    }

    JButton dismissButton = new JButton("Quit");
    dismissButton.addActionListener(new QuitListener());
    saveButton.addActionListener(new SaveListener());

    JPanel pas = new JPanel();
    pas.add(saveButton);
    pas.add(dismissButton);

    //getContentPane().add(pan, BorderLayout.NORTH);
    //getContentPane().add(scrollPane, BorderLayout.CENTER);
    getContentPane().add(pas, BorderLayout.SOUTH);

    //addFocusListener(this);
    //textArea.setFont(new Font("Courier", Font.PLAIN, parent.getFontSize()));
    saveButton.setEnabled(true);
    pack();
    setVisible(true);
    guiLayoutDone = true;
  }



  class QuitListener implements ActionListener
  {
    public void actionPerformed(ActionEvent e)
    {
      thisFrame.setVisible(false);
      //dispose();
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

              //ConcordanceV va =  parent.getConcordanceVector().concArray;
              Vector columns = new Vector();
              for (Iterator<ConcordanceObject> p = parent.getConcordanceVector().iterator(); p.hasNext(); ){
                //for (int i = 0; i < va.length ; i++) {
                ConcordanceObject co = p.next();
                if (co == null)
                  break;
                StringTokenizer st = co.getRightContextTokens();
                int j = 0;
                while(st.hasMoreTokens()){
                  HashMap wt;
                  try {
                    wt = (HashMap)columns.get(j++);
                  }
                  catch (IndexOutOfBoundsException ex){
                    wt = new HashMap();
                    columns.add(wt);
                  }
                  String t = st.nextToken();
                  Integer c = (Integer)wt.get(t);
                  int ct = c == null? 1 : c.intValue()+1; 
                  wt.put(t,new Integer(ct));
                }
              }
              
              for (Enumeration en = columns.elements(); en.hasMoreElements();)
                System.out.println(en.nextElement());
              dlf.close();
            }
        }
      catch (Exception ex) {
        ex.printStackTrace(System.err);
        JOptionPane.showMessageDialog(null, //parent.getBrowserGUI(), 
                                      "Error writing concordance table" + ex,
                                      "Error!", JOptionPane.ERROR_MESSAGE);
      }
    }
  }


}

