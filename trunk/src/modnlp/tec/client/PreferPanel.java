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
package modnlp.tec.client;

import modnlp.tec.client.gui.event.*;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.lang.reflect.Array;
import java.util.Enumeration;
import java.util.Vector;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

//import java.awt.*;
/**
 *  This class receives and manages user preferences 
 * 
 * @author  S. Luz &#60;luzs@acm.org&#62;
 * @version <font size=-1>$Id: PreferPanel.java,v 1.3 2001/07/31 16:18:53 luzs Exp $</font>
 * @see  ContextClient
 * @see  Browser
 */
public class PreferPanel extends JFrame 
  implements  ActionListener, ItemListener, DefaultManager 
{

  private static final String COTXBT = "Concordance context ";
  private static final String EXTXBT = "File extract context ";
  private static String [] fseltab = {"10", "12","14","16","18"};
  private static  int FSELMAX = Array.getLength(fseltab);
  private JPanel set1 = new JPanel();
  private JPanel set2 = new JPanel();
  private JComboBox  fontsel;
  private Vector defaultListeners = new Vector ();
  private String HIDESGML = "Show markup along with text";
  private JTextField context;
  private JTextField extrctx;
  private JTextField httpProxyField = new JTextField();
  private JTextField headerBaseField = new JTextField();
  private String httpProxy = null;
  private JComboBox sortctx = new JComboBox();
  public static final int SCTXMAX = 6;
  public int fontSize = 12;
  public int sortContextHorizon = 1;
  public JCheckBox ckbSGML = new JCheckBox(HIDESGML);
  public String stSGML = "no";
  public int maxContext = 130;
  public int maxExtrCtx = 600;
  JButton applyButton = new JButton("Apply");
  JButton cancelButton = new JButton("Cancel");
  JButton doneButton = new JButton("Done");

  /** Set up layout and display
   */
  public PreferPanel (){
    super();

    //setLayout(new GridLayout(9,2));
    JPanel prefer = new JPanel();
    JPanel ctrl = new JPanel();
    JPanel set3 = new JPanel();
    JPanel set4 = new JPanel();
    JPanel set5 = new JPanel();
    JPanel set6 = new JPanel();
    JPanel set6a = new JPanel();
    JPanel set7 = new JPanel();
    JPanel set8 = new JPanel();
    prefer.setLayout(new BoxLayout(prefer, BoxLayout.Y_AXIS));

    resetContext();
    resetExtrCtx();

    set3.add(ckbSGML);

    fontsel = new JComboBox();
    for (int i = 0 ; i < FSELMAX ; i++)
      fontsel.addItem(fseltab[i]);
    set4.add(new JLabel("Font size "));    
    fontsel.setSelectedItem(fontSize+""); 
    set4.add(fontsel);

    for (int i = 1 ; i <= SCTXMAX ; i++)
      sortctx.addItem(""+i);

    set5.add(new JLabel("Sort context horizon "));
    set5.add(sortctx);


    set6.setLayout(new GridLayout(2,2));
    //set6.setLayout(new BoxLayout(set6, BoxLayout.X_AXIS));
    //httpProxyField.setMinimumSize(new Dimension(30, 5)); 
    httpProxyField.setMaximumSize(new Dimension(300, 28)); 
    set6.add(new JLabel("HTTP Proxy:  "));
    if (System.getProperty("tec.client.runmode") != null &&
        System.getProperty("tec.client.runmode").equals("jnlp"))
      {
        httpProxyField.setEnabled(false);
        httpProxyField.setBackground(set6.getBackground());
        httpProxyField.setText("Using browser settings!");
      }
    else
      if (System.getProperty("http.proxyHost") != null){
        httpProxyField.setText(System.getProperty("http.proxyHost"));
        if (System.getProperty("http.proxyPort") != null)
          httpProxyField.setText(httpProxyField.getText()
                                 +":"
                                 +System.getProperty("http.proxyPort"));
      }
    set6.add(httpProxyField);
    //set6a.setLayout(new BoxLayout(set6a, BoxLayout.X_AXIS));
    set6.add(new JLabel("Headers URL: "));
    set6.add(headerBaseField);

    ctrl.setLayout(new GridLayout(1,3));
    ctrl.add(applyButton);
    ctrl.add( cancelButton );
    ctrl.add( doneButton );
    ((FlowLayout)set1.getLayout()).setAlignment(FlowLayout.LEFT);
    ((FlowLayout)set2.getLayout()).setAlignment(FlowLayout.LEFT);
    ((FlowLayout)set3.getLayout()).setAlignment(FlowLayout.LEFT);
    ((FlowLayout)set4.getLayout()).setAlignment(FlowLayout.LEFT);
    ((FlowLayout)set5.getLayout()).setAlignment(FlowLayout.LEFT);
    ((FlowLayout)set7.getLayout()).setAlignment(FlowLayout.LEFT);

    prefer.add(set1);
    prefer.add(set2);
    prefer.add(set3);
    prefer.add(set4);
    prefer.add(set5);
    prefer.add(set6);
    prefer.add(set6a);
    prefer.add(set7);
    prefer.add(ctrl);

    //ctrl.setFont( new Font("Helvetica", Font.BOLD, 12));
    //prefer.setFont( new Font("Helvetica", Font.PLAIN, 12));
    this.getContentPane().add("Center",prefer);

    ckbSGML.addItemListener(this);
    applyButton.addActionListener(this);
    cancelButton.addActionListener(this);
    doneButton.addActionListener(this);
  }

  /** Reset value of context; used for initialization and for
   *  redisplay when user enters an invalid value */
  public void resetContext() {
    set1.removeAll();
    context = new JTextField(""+(maxContext/2)+"",3);
    set1.add( new JLabel(COTXBT));
    set1.add(context);    
    set1.add(new JLabel(" characters (maximum "+maxContext+")"));
    set1.validate();
  }

  /** Reset value of  extract context; used for initialization and for
   *  redisplay when user enters an invalid value */
  public void resetExtrCtx() {
    set2.removeAll();
    set2.add( new JLabel(EXTXBT));
    extrctx = new JTextField(""+(maxExtrCtx/2)+"",3);
    set2.add(extrctx);    
    set2.add(new JLabel(" characters (maximum "+maxExtrCtx+")"));
    set2.validate();
  }


  /*  public void paint(Graphics g){
      validate();
      }
  */

  public void actionPerformed(ActionEvent evt)
  {
    //labelMessage("Building concordance list. Please wait...");
    String arg = evt.getActionCommand();
    System.out.println("ARG:"+arg);
    Object source = evt.getSource();
    if(source == applyButton || source == doneButton )
      {
        try {// check if context and extract context are OK
          if ( (new Integer(context.getText())).intValue() 
               > maxContext )
            resetContext();
          if ( (new Integer(extrctx.getText())).intValue() 
               > maxExtrCtx )
            resetExtrCtx();
        }
        catch (NumberFormatException e){
          resetExtrCtx();
          resetContext();
        }
        int nfs = (new Integer((String)fontsel.getSelectedItem())).intValue();
        if ( nfs != fontSize )
          {
            fontSize = nfs;
            raiseDefaultChangeEvent(new FontSizeChangeEvent(this, nfs));
          }
        int nsctx = (new Integer((String)sortctx.getSelectedItem())).intValue();
        if ( nsctx != sortContextHorizon )
          {
            sortContextHorizon = nsctx;
            raiseDefaultChangeEvent(new SortHorizonChangeEvent(this, nsctx));
          }
        if ( updatedHTTPProxySelection() ) {
          System.setProperty("http.proxyHost",getHTTPProxyHost());
          System.setProperty("http.proxyPort",getHTTPProxyPort());
        }
      }
    if ( source == doneButton  || source == cancelButton ) 
      dispose();
  }

  public String getHTTPProxy () 
  {
    return httpProxy;
  }

  public String getHTTPProxyHost () 
  {
    try {
      return httpProxy.substring(0,httpProxy.lastIndexOf(":"));
    } 
    catch (Exception e) {
      return "";
    }
  }
  public String getHTTPProxyPort () 
  {
    try {
      return httpProxy.substring(httpProxy.indexOf(":")+1);
    } 
    catch (Exception e) {
      return "";
    }
  }

  public String getHTTPProxySelection () 
  {
    String ht = httpProxyField.getText();
    //ht = ht.substring(ht.indexOF(" ")+1);
    // ht.substring(0,ht.lastIndexOf(" "));
    return ht.trim();
  }
  private boolean updatedHTTPProxySelection() 
  {
    if (getHTTPProxySelection().equals(getHTTPProxy()))
      return false;
    httpProxy = getHTTPProxySelection();
    return true;
  }

  public void itemStateChanged(ItemEvent e) {
    Object source = e.getItemSelectable();

    if (source == ckbSGML){
      if ( e.getStateChange() == ItemEvent.DESELECTED )// set sgml flag
        stSGML = "no";
      else 
        stSGML = "yes";
    }
  }

  public int getContextSize ()
  {
    return (new Integer (context.getText())).intValue();
  }

  public int getExtractContextSize ()
  {
    return (new Integer (extrctx.getText())).intValue();
  }

  public int getSortHorizon ()
  {
    return sortContextHorizon;
  }

  public int getFontSize ()
  {
    return fontSize;
  }

  public String getSGMLFlag ()
  {
    return stSGML;
  }

  public String getHeaderBaseURL ()
  {
    return headerBaseField.getText();
  }

  public void setHeaderBaseURL (String u)
  {
    headerBaseField.setText(u);
  }


  // The DefaultManager interface methds

  public void addDefaultChangeListener(DefaultChangeListener obj)
  {
    defaultListeners.addElement(obj);
  }

  public void removeDefaultChangeListener(DefaultChangeListener obj)
  {
    defaultListeners.removeElement(obj);
  }

  private void raiseDefaultChangeEvent (FontSizeChangeEvent e){

    for (Enumeration f = defaultListeners.elements(); 
         f.hasMoreElements() ;)
      {
        TecDefaultChangeListener li = (TecDefaultChangeListener)f.nextElement();
        li.defaultChanged(e);
      }
  }
  private void raiseDefaultChangeEvent (SortHorizonChangeEvent e){

    for (Enumeration f = defaultListeners.elements(); 
         f.hasMoreElements() ;)
      {
        TecDefaultChangeListener li = (TecDefaultChangeListener)f.nextElement();
        li.defaultChanged(e);
      }
  }
  private void raiseDefaultChangeEvent (DefaultChangeEvent e){

    for (Enumeration f = defaultListeners.elements(); 
         f.hasMoreElements() ;)
      {
        DefaultChangeListener li = (DefaultChangeListener)f.nextElement();
        li.defaultChanged(e);
      }
		
  }
}
