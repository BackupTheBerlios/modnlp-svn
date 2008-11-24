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
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.JTabbedPane;
import java.awt.LayoutManager;
import java.awt.GridLayout;
import java.awt.FlowLayout;
import java.awt.BorderLayout;
import java.awt.Font;
import java.sql.*;
import java.io.*;

/**
 *  GUI for advanced search customisation
 *
 * @author  Noel Skehan
 * @version <font size=-1>$Id: AdvConcSearch.java,v 1.2 2003/08/06 16:58:56 luzs Exp $</font>
 * @see  Browser
 * @see  ContextClient
 * @see  TecServer
 * @deprecated will be removed soon.
*/
public class AdvConcSearch extends JFrame
{
  private Browser parentFrame;
	private TextArea output;
  private JPanel top = new JPanel();
	private JPanel bottom = new JPanel();
	private Connection conn;
	private JPanel t_fieldsPanel = new JPanel();
  private JPanel a_fieldsPanel = new JPanel();
	private JPanel fileLang = new JPanel();
	private JButton pressButton, qBut, clear;
	private JTextField conc;
	private JCheckBox file, translator, author, lang;
	private static final String FILEBUT = "File";
	private static final String TRANSBUT = "Translator";
	private static final String AUTHBUT = "Author";
	private static final String LANGBUT = "Language";
	//public JPanel t_labelPanel, a_labelPanel, fileLang;
	private JLabel authLabel, transLabel;
	private String sqlQuery;
  public String labels[] = { "Name", "Gender", "Nationality", "Sexual Orientation" };
	private Object tnames [];
  private Object tgends [];
  private Object tnats [];
  private Object tsexOr [];
	private Object anames [];
  private Object agends [];
  private Object anats [];
  private Object asexOr [];
  private Object tlans [];
  private Object documents [];

  private final Vector tmpTran = new Vector();
  private final Vector tmpTgn = new Vector();
  private final Vector tmpTnat = new Vector();
  private final Vector tmpTsex = new Vector();
  private final Vector tmpAuth = new Vector();
  private final Vector tmpAgn = new Vector();
  private final Vector tmpAnat = new Vector();
  private final Vector tmpAsex = new Vector();
  private final Vector tmpFile = new Vector();
  private final Vector tmpLang = new Vector();
	String ln, fn;
	private boolean fileSelect, langSelect, tnameSelect, tgendSelect, tnatSelect, tsexSelect, authSelect, agendSelect, anatSelect, asexSelect, bool;
  public String toSearch;
  Vector doc;

  JTabbedPane tabbedPane;

  
  JTextArea qbox = new JTextArea(4, 50);

  
  JList tmpAName, tmpAGend, tmpANat, tmpASex, aname, agend, anat, asexorient;
  JList langs, files, tmpFName, tmpLn;
  JList tname, tmpTName, tgend, tmpTGend, tnat, tmpTNat, tsexorient, tmpTSex; 

  DefaultListModel tmpTNameLM, tmpFNameLM, tmpLnLM, tmpTGendLM, tmpTNatLM, 
    tmpTSexLM, tmpANameLM, tmpAGendLM, tmpANatLM, tmpASexLM;

  Component fPanel, auPanel, trPanel, advPanel;
  private FileWriter fw_log;
  private BufferedWriter bw_log;
  
  public AdvConcSearch(Browser parent )
  {
    super("Advanced Concordance Search");    
    doc = new Vector();
    this.parentFrame = parent; // toSearch = "false";
    // getMenu();
  }
  
  public void setTNames (Object [] s) {
    tnames = s;
  }
  public void setTGends (Object [] s) {
    tgends = s;
  }
  public void setTNats (Object [] s) {
    tnats = s;
  }
  public void setTSexOr (Object [] s) {
    tsexOr = s;
  }

  public void setANames (Object [] s) {
    anames = s;
  }
  public void setAGends (Object [] s) {
    agends = s;
  }
  public void setANats (Object [] s) {
    anats = s;
  }
  public void setASexOr (Object [] s) {
    asexOr = s;
  }
  public void setTLans (Object [] s) {
    tlans = s;
  }
  public void setDocuments (Object [] s) {
    documents = s;
  }


  public void getMenu()
  {
		// get context pane and set its layout
    Container c = getContentPane();
    
    // Start screen layout
    //BorderLayout bl = new BorderLayout(12, 12);
    c.setLayout( new BorderLayout(10, 10) );
		c.add(top, BorderLayout.NORTH);
		//output = new TextArea(6, 30);
    c.add( fileLang, BorderLayout.WEST);
    c.add( t_fieldsPanel, BorderLayout.CENTER );
		c.add( a_fieldsPanel, BorderLayout.EAST);
    
    tabbedPane = new JTabbedPane();
    //JTabbedPane tabbedPane = new JTabbedPane();
    

    trPanel = makeTrPanel();
    tabbedPane.addTab("Translator", trPanel);
    
    auPanel = makeAuPanel();
    tabbedPane.addTab("Author", auPanel);
    
    fPanel = makeFPanel();
    tabbedPane.addTab("Source Language & File", fPanel);

    advPanel = makeAdvPanel();
    tabbedPane.addTab("Advanced search", advPanel);


    tabbedPane.setSelectedIndex(0);
    c.add(tabbedPane);
    
		/** Top Panel **/
		top.setLayout(new FlowLayout(100, 150, 10));
		bottom.setLayout(new BorderLayout());
    
		c.add(bottom, BorderLayout.SOUTH);
		pressButton = new JButton("Confirm Selection");
    setQuery();
		pressButton.
      addActionListener(
                        new ActionListener(){
                          public void actionPerformed(ActionEvent event)
                          {
                            setQuery();
                            if ( tmpFNameLM.getSize() > 0
                                 || 
                                 tmpLnLM.getSize() > 0
                                 || 
                                 tmpTNameLM.getSize() > 0 
                                 || 
                                 tmpTGendLM.getSize() > 0
                                 ||
                                 tmpTNatLM.getSize() > 0
                                 || 
                                 tmpTSexLM.getSize() > 0
                                 || 
                                 tmpANameLM.getSize() > 0
                                 || 
                                 tmpAGendLM.getSize() > 0 
                                 || 
                                 tmpANatLM.getSize() > 0 
                                 || 
                                 tmpASexLM.getSize() > 0 )
                              sqlQuery = sqlQuery + " AND (";
                            
                            selectByFile();
                            selectByLang();
                            selectByTrans();
                            selectByAuth();
                            selectAdv();
                            
                            sqlQuery = sqlQuery + ")";
                            
                            doc.removeAllElements();
                            parentFrame.setAdvConcFlag(true);
                            hide();
                            dispose();
                            //System.out.println(sqlQuery);
                          }
                        }
                        );
		bottom.add(pressButton, BorderLayout.CENTER);
    
		qBut = new JButton("Cancel");
		qBut.
      addActionListener(
                        new ActionListener(){
                          public void actionPerformed(ActionEvent event)
                          {
                            hide();
                            dispose();
                            removeVecElements();
                          }
                        }
                        );
		bottom.add(qBut, BorderLayout.WEST);
		clear = new JButton("Clear");
		clear.
      addActionListener(
                        new ActionListener(){
                          public void actionPerformed(ActionEvent event)
                          {
                            if (tabbedPane.getSelectedComponent() == trPanel)
                              {
                                tmpTNameLM.removeAllElements();
                                tmpTGendLM.removeAllElements();
                                tmpTNatLM.removeAllElements();
                                tmpTSexLM.removeAllElements();
                                tgend.setVisible(true);
                                tnat.setVisible(true);
                                tsexorient.setVisible(true);
                                tmpTGend.setVisible(true);
                                tmpTNat.setVisible(true);
                                tmpTSex.setVisible(true);
                              }
                            else if (tabbedPane.getSelectedComponent() == auPanel)
                              {
                                tmpANameLM.removeAllElements();
                                tmpAGendLM.removeAllElements();
                                tmpANatLM.removeAllElements();
                                tmpASexLM.removeAllElements();
                                agend.setVisible(true);
                                anat.setVisible(true);
                                asexorient.setVisible(true);
                                tmpAGend.setVisible(true);
                                tmpANat.setVisible(true);
                                tmpASex.setVisible(true);
                              }
                            else if (tabbedPane.getSelectedComponent() == fPanel)
                              {
                                tmpFNameLM.removeAllElements();
                                tmpLnLM.removeAllElements();
                              }
                            else 
                              {
                                qbox.setText("");

                              }
                            tname.clearSelection();
                            tgend.clearSelection();
                            tnat.clearSelection();
                            tsexorient.clearSelection();
                            aname.clearSelection();
                            agend.clearSelection();
                            anat.clearSelection();
                            asexorient.clearSelection();
                            files.clearSelection();
                            langs.clearSelection();
                          }
                        }
                        );
		bottom.add(clear, BorderLayout.EAST);
    
  }
  
  // this must be called last!! (i.e. after makeTrPanel, makeFPanel, etc)
  protected Component makeAdvPanel()
  {
    JPanel ap = new JPanel();

    JPanel tp = new JPanel();
    JPanel bp = new JPanel();

    tp.setBorder(BorderFactory.
                 createTitledBorder(BorderFactory.createLineBorder(Color.black),
                                    "SQL Expression"));
    tp.add(qbox);
    tp.setBackground(Color.white);
    bp.setBackground(Color.white);

    ap.setLayout(new BoxLayout(ap, BoxLayout.Y_AXIS));
    bp.setBorder(BorderFactory.
                 createTitledBorder(BorderFactory.createLineBorder(Color.black),
                                    "Fields you can use:"));
    String help = "<html><body> \n<table border=0><tr><td><b>Author:</b></td><td><b>Translator:</b></td></tr>\n<tr>\n<td>\n <em>authName</em> = \"Author's Name\",<br>\n <em>aGender</em> = \"Author's Gender\",<br>\n <em>aNation</em> = \"Author's Nationality\",<br> \n <em>aSexO</em> = \"Author's Sexual Orientation\"\n</td> \n<td>\n <em>transName</em> = \"Translator's Name\",<br>\n <em>tGender</em> = \"Translator's Gender\",<br>\n <em>tNation</em> = \"Translator's Nationality\",<br>\n <em>tSexO</em> = \"Translator's Sexual Orientation\"\n</td>\n</tr>\n<tr>\n<td>\n <b>Source Language:</b></td> <td><b>File:</b></td></tr>\n<tr>\n<td>\n <em>language</em> = \"Language\"\n</td>\n<td> \n <em>filename</em> = \"File\"\n</td>\n</tr>\n</table> \n<h3><font color='#666698'> Sample query:</font></h3> <b><em> aGender =\"female\" AND tGender =\"female\" AND \n  (tNation=\"British\" OR tNation=\"Australian\") </em></b><br><br>Entering the selection above will select a subcorpus containing texts written by women and <br>translated by either British or Australian women.</body></html>" ;


    JEditorPane hbox = new  JEditorPane("text/html", help);
    //hbox.setPreferredSize(new Dimension(661, 238));
    // JScrollPane hsp = new JScrollPane(hbox);
    //

    bp.add(hbox);

    ap.setBorder(BorderFactory.createEtchedBorder(Color.gray, Color.darkGray));
    ap.add(tp);
    ap.add(bp);
    
    qbox.grabFocus();
    return new JScrollPane(ap);
  }
  protected Component makeTrPanel()
  {
    t_fieldsPanel.setLayout(new GridLayout(labels.length+2, 3, 10, 1));
    t_fieldsPanel.setBackground(Color.white);
    t_fieldsPanel.setBorder(BorderFactory.createEtchedBorder(Color.gray, Color.darkGray));
   
    tmpTNameLM = new DefaultListModel();
    tmpTName = new JList(tmpTNameLM);
    tmpTName.setBackground(Color.lightGray);

    tmpTGendLM = new DefaultListModel();
    tmpTGend = new JList(tmpTGendLM);
    tmpTGend.setBackground(Color.lightGray);

    tmpTNatLM = new DefaultListModel();
    tmpTNat = new JList(tmpTNatLM);
    tmpTNat.setBackground(Color.lightGray);

    tmpTSexLM = new DefaultListModel();
    tmpTSex = new JList(tmpTSexLM);
    tmpTSex.setBackground(Color.lightGray);
    
    JLabel trDet = new JLabel("Translator Details");
    trDet.setFont(new Font("Serif", Font.BOLD, 16));
    trDet.setForeground(Color.darkGray);
		t_fieldsPanel.add( trDet );
    JLabel trDot = new JLabel(".............");
    trDot.setFont(new Font("Serif", Font.BOLD, 16));
    trDot.setForeground(Color.darkGray);
    t_fieldsPanel.add( trDot );
    //t_fieldsPanel.add(new JLabel(""));
    t_fieldsPanel.add(new JLabel(""));
    
		t_fieldsPanel.add( new JLabel( labels[ 0 ]));
    tname = new JList(tnames);
    tname.setVisibleRowCount(3);
    tname.setBackground(Color.white);

    t_fieldsPanel.add(  new JScrollPane(tname) );
		tname.setVisible(true);
    tname.clearSelection();

    t_fieldsPanel.add(new JScrollPane(tmpTName));
		t_fieldsPanel.add( new JLabel( labels[ 1 ]));
    tgend = new JList(tgends);
    tgend.setBackground(Color.white);

    t_fieldsPanel.add(new JScrollPane(tgend) );
    tgend.setVisible(true);
    //t_fieldsPanel.add(new JLabel(""));
    t_fieldsPanel.add(new JScrollPane(tmpTGend));
    
		t_fieldsPanel.add( new JLabel( labels[ 2 ]));
		tnat = new JList(tnats);
    tnat.setBackground(Color.white);
    t_fieldsPanel.add(new JScrollPane( tnat) );
		tnat.setVisible(true);

    t_fieldsPanel.add(new JScrollPane(tmpTNat));    
		t_fieldsPanel.add( new JLabel( labels[ 3 ]));
		tsexorient = new JList(tsexOr);
    tsexorient.setBackground(Color.white);
    t_fieldsPanel.add(new JScrollPane( tsexorient ));
		tsexorient.setVisible(true);
    //t_fieldsPanel.add(new JLabel(""));
    
    t_fieldsPanel.add(new JScrollPane(tmpTSex));
    
    t_fieldsPanel.add(new JLabel(""));
    
    JButton addT = new JButton("  Add  ");
    JPanel addP = new JPanel(new FlowLayout());
    addP.setBackground(Color.white);
    addP.add(addT);
    t_fieldsPanel.add(addP);
    JButton remove = new JButton("Remove");
    JPanel removeP = new JPanel(new FlowLayout());
    removeP.setBackground(Color.white);
    removeP.add(remove);
    t_fieldsPanel.add(removeP);


    addT.
      addActionListener(
                        new ActionListener(){
                          
                          public void actionPerformed(ActionEvent event)
                          {
                            if ( tname.getSelectedIndex() > -1 ) {
                              addElements(tmpTNameLM, tname.getSelectedValues());
                              tmpTGendLM.removeAllElements();
                              tmpTNatLM.removeAllElements();
                              tmpTSexLM.removeAllElements();
                              tgend.setVisible(false);
                              tnat.setVisible(false);
                              tsexorient.setVisible(false);
                              tmpTGend.setVisible(false);
                              tmpTNat.setVisible(false);
                              tmpTSex.setVisible(false);
                              tname.clearSelection();
                              return;
                            }
                            if ( tgend.getSelectedIndex() > -1 )
                              addElements(tmpTGendLM, tgend.getSelectedValues());
                            if ( tnat.getSelectedIndex() > -1 )
                              addElements(tmpTNatLM, tnat.getSelectedValues());
                            if ( tsexorient.getSelectedIndex() > -1 )
                              addElements(tmpTSexLM, tsexorient.getSelectedValues());
                            tgend.clearSelection();
                            tnat.clearSelection();
                            tsexorient.clearSelection();
                          }
                        }
                        );

    remove.
      addActionListener(
                        new ActionListener(){
                          public void actionPerformed(ActionEvent event)
                          {
                            if ( tmpTName.getSelectedIndex() > -1 ) {
                              removeElements(tmpTNameLM, tmpTName.getSelectedValues());
                              if ( tmpTNameLM.getSize() == 0 ) {
                                tgend.setVisible(true);
                                tnat.setVisible(true);
                                tsexorient.setVisible(true);
                                tmpTGend.setVisible(true);
                                tmpTNat.setVisible(true);
                                tmpTSex.setVisible(true);
                              }
                            }
                            if ( tmpTGend.getSelectedIndex() > -1 )
                              removeElements(tmpTGendLM, tmpTGend.getSelectedValues());
                            if ( tmpTNat.getSelectedIndex() > -1 )
                              removeElements(tmpTNatLM, tmpTNat.getSelectedValues());
                            if ( tmpTSex.getSelectedIndex() > -1 )
                              removeElements(tmpTSexLM, tmpTSex.getSelectedValues());
                            tmpTName.clearSelection();
                            tmpTGend.clearSelection();
                            tmpTName.clearSelection();
                            tmpTSex.clearSelection();
                          }
                        }
                        );

    return t_fieldsPanel;
  }
  
  protected Component makeAuPanel()
  {
    a_fieldsPanel.setLayout(new GridLayout(labels.length+2, 3, 10, 1));
    a_fieldsPanel.setBackground(Color.white);
    a_fieldsPanel.setBorder(BorderFactory.createEtchedBorder(Color.gray, Color.darkGray));
    
    tmpANameLM = new DefaultListModel();
    tmpAName = new JList(tmpANameLM);
    tmpAName.setBackground(Color.lightGray);

    tmpAGendLM = new DefaultListModel();
    tmpAGend = new JList(tmpAGendLM);
    tmpAGend.setBackground(Color.lightGray);

    tmpANatLM = new DefaultListModel();
    tmpANat = new JList(tmpANatLM);
    tmpANat.setBackground(Color.lightGray);

    tmpASexLM = new DefaultListModel();
    tmpASex = new JList(tmpASexLM);
    tmpASex.setBackground(Color.lightGray);
    
    JLabel trDet = new JLabel("Author Details");
    trDet.setFont(new Font("Serif", Font.BOLD, 16));
    trDet.setForeground(Color.darkGray);
		a_fieldsPanel.add( trDet );
    JLabel trDot = new JLabel(".............");
    trDot.setFont(new Font("Serif", Font.BOLD, 16));
    trDot.setForeground(Color.darkGray);
    a_fieldsPanel.add( trDot );
    //a_fieldsPanel.add(new JLabel(""));
    a_fieldsPanel.add(new JLabel(""));
    
		a_fieldsPanel.add( new JLabel( labels[ 0 ]));
    aname = new JList(anames);
    aname.setVisibleRowCount(3);
    aname.setBackground(Color.white);

    a_fieldsPanel.add(  new JScrollPane(aname) );
		aname.setVisible(true);
    aname.clearSelection();

    a_fieldsPanel.add(new JScrollPane(tmpAName));
		a_fieldsPanel.add( new JLabel( labels[ 1 ]));
    agend = new JList(agends);
    agend.setBackground(Color.white);

    a_fieldsPanel.add(new JScrollPane(agend) );
    agend.setVisible(true);
    //a_fieldsPanel.add(new JLabel(""));
    a_fieldsPanel.add(new JScrollPane(tmpAGend));
    
		a_fieldsPanel.add( new JLabel( labels[ 2 ]));
		anat = new JList(anats);
    anat.setBackground(Color.white);
    a_fieldsPanel.add(new JScrollPane( anat) );
		anat.setVisible(true);

    a_fieldsPanel.add(new JScrollPane(tmpANat));    
		a_fieldsPanel.add( new JLabel( labels[ 3 ]));
		asexorient = new JList(tsexOr);
    asexorient.setBackground(Color.white);
    a_fieldsPanel.add(new JScrollPane( asexorient ));
		asexorient.setVisible(true);
    //a_fieldsPanel.add(new JLabel(""));
    
    a_fieldsPanel.add(new JScrollPane(tmpASex));
    
    a_fieldsPanel.add(new JLabel(""));
    
    JButton addT = new JButton("  Add  ");
    JPanel addP = new JPanel(new FlowLayout());
    addP.setBackground(Color.white);
    addP.add(addT);
    a_fieldsPanel.add(addP);
    JButton remove = new JButton("Remove");
    JPanel removeP = new JPanel(new FlowLayout());
    removeP.setBackground(Color.white);
    removeP.add(remove);
    a_fieldsPanel.add(removeP);


    addT.
      addActionListener(
                        new ActionListener(){
                          
                          public void actionPerformed(ActionEvent event)
                          {
                            if ( aname.getSelectedIndex() > -1 ) {
                              addElements(tmpANameLM, aname.getSelectedValues());
                              tmpAGendLM.removeAllElements();
                              tmpANatLM.removeAllElements();
                              tmpASexLM.removeAllElements();
                              agend.setVisible(false);
                              anat.setVisible(false);
                              asexorient.setVisible(false);
                              tmpAGend.setVisible(false);
                              tmpANat.setVisible(false);
                              tmpASex.setVisible(false);
                              aname.clearSelection();
                              return;
                            }
                            if ( agend.getSelectedIndex() > -1 )
                              addElements(tmpAGendLM, agend.getSelectedValues());
                            if ( anat.getSelectedIndex() > -1 )
                              addElements(tmpANatLM, anat.getSelectedValues());
                            if ( asexorient.getSelectedIndex() > -1 )
                              addElements(tmpASexLM, asexorient.getSelectedValues());
                            agend.clearSelection();
                            anat.clearSelection();
                            asexorient.clearSelection();
                          }
                        }
                        );

    remove.
      addActionListener(
                        new ActionListener(){
                          public void actionPerformed(ActionEvent event)
                          {
                            if ( tmpAName.getSelectedIndex() > -1 ) {
                              removeElements(tmpANameLM, tmpAName.getSelectedValues());
                              if ( tmpANameLM.getSize() == 0 ) {
                                agend.setVisible(true);
                                anat.setVisible(true);
                                asexorient.setVisible(true);
                                tmpAGend.setVisible(true);
                                tmpANat.setVisible(true);
                                tmpASex.setVisible(true);
                              }
                            }
                            if ( tmpAGend.getSelectedIndex() > -1 )
                              removeElements(tmpAGendLM, tmpAGend.getSelectedValues());
                            if ( tmpANat.getSelectedIndex() > -1 )
                              removeElements(tmpANatLM, tmpANat.getSelectedValues());
                            if ( tmpASex.getSelectedIndex() > -1 )
                              removeElements(tmpASexLM, tmpASex.getSelectedValues());
                            tmpAName.clearSelection();
                            tmpAGend.clearSelection();
                            tmpAName.clearSelection();
                            tmpASex.clearSelection();
                          }
                        }
                        );

    return a_fieldsPanel;
  }
    

  protected Component makeFPanel()
  {
    fileLang.setLayout( new GridLayout(5, 3, 10, 1) );
    fileLang.setBackground(Color.white);
    fileLang.setBorder(BorderFactory.createEtchedBorder(Color.gray, Color.darkGray));
    
    tmpFNameLM = new DefaultListModel();
    tmpFName = new JList(tmpFNameLM);
    tmpFName.setBackground(Color.lightGray);

    tmpLnLM = new DefaultListModel();
    tmpLn = new JList(tmpLnLM);
    tmpLn.setBackground(Color.lightGray);
    
    JLabel filnam = new JLabel("File Name");
    JLabel srctxt = new JLabel("Source Language");
		filnam.setFont(new Font("Serif", Font.BOLD, 16));
		srctxt.setFont(new Font("Serif", Font.BOLD, 16));
    filnam.setForeground(Color.darkGray);
    srctxt.setForeground(Color.darkGray);
		fileLang.add(filnam);
    fileLang.add(new JLabel("........"));
    fileLang.add(new JLabel(""));
		fileLang.add(new JLabel("File name"));
		//fileLang.add(srcDot);
		files = new JList(documents);
    files.setBackground(Color.white);
		fileLang.add(new JScrollPane( files) );
    fileLang.add(new JScrollPane( tmpFName ));
    files.setVisible(true);
    tmpFName.setVisible(true);

    
		fileLang.add(srctxt);
		fileLang.add(new JLabel("........"));
    fileLang.add( new JLabel(""));
		fileLang.add( new JLabel("Language"));
		langs = new JList(tlans);
    langs.setBackground(Color.white);

		fileLang.add(new JScrollPane(langs));
		langs.setVisible(true);
    fileLang.add(new JScrollPane(tmpLn));
    
    fileLang.add( new JLabel(""));

    JButton addFL = new JButton("Add");
    JPanel addP = new JPanel(new FlowLayout());
    addP.setBackground(Color.white);
    addP.add(addFL);


    JButton removeFL = new JButton("Remove");
    JPanel removeP = new JPanel(new FlowLayout());
    removeP.setBackground(Color.white);
    removeP.add(removeFL);

    fileLang.add(addP);
    fileLang.add(removeP);


    addFL.
      addActionListener(
                        new ActionListener(){
                          public void actionPerformed(ActionEvent event)
                          {
                            if ( files.getSelectedIndex() > -1 ) 
                              addElements(tmpFNameLM, files.getSelectedValues());
                            if ( langs.getSelectedIndex() > -1 )
                              addElements(tmpLnLM, langs.getSelectedValues());
                            files.clearSelection();
                            langs.clearSelection();
                          }
                        }
                        );
    removeFL.
      addActionListener(
                        new ActionListener(){
                          public void actionPerformed(ActionEvent event)
                          {
                            if ( tmpFName.getSelectedIndex() > -1 ) 
                              removeElements(tmpFNameLM, tmpFName.getSelectedValues());
                            if ( tmpLn.getSelectedIndex() > -1 )
                              removeElements(tmpLnLM, tmpLn.getSelectedValues());
                            tmpFName.clearSelection();
                            tmpLn.clearSelection();
                          }
                        }
                        );



    return fileLang;
  }
  
  protected void addElements (DefaultListModel lm, Object[] oa) 
  {
    for (int i = 0 ; i < oa.length ; i++ ) 
      if (! lm.contains(oa[i]))
        lm.addElement(oa[i]);
  }
  protected void removeElements (DefaultListModel lm, Object[] oa) 
  {
    for (int i = 0 ; i < oa.length ; i++ ) 
      lm.removeElement(oa[i]);
  }


  public Vector sortVector(Vector a)
  {
    String temp ;
    String [] elements = new String[a.size()];
    Enumeration er = a.elements();
    int jh=0;
    while(er.hasMoreElements())
      {
        elements[jh] = (String)er.nextElement();
        jh++;
      }
    
    for (int i = 0; i<a.size(); i++) {
      for (int j = i + 1; j < a.size(); j++) {
        if (elements[i].toLowerCase().compareTo(elements[j].toLowerCase())>0) {
          temp = elements[i];
          elements[i] = elements[j];
          elements[j] = temp;
        }
      }
    }
    
    for(int s=0;s<elements.length;s++)
      a.setElementAt(elements[s], s);
    
    return a;
    
  }
  
  public boolean getM()
  {
    return bool;
  }
  
	public void setQuery()
	{
		sqlQuery = "SELECT d.filename FROM document as d, title as t, section as s, translator as tr, author as a" + " " +
      "WHERE d.filename = t.filename" + " " +
      "AND d.dsect = s.sect" + " " +
      "AND s.sect_trans = tr.id" + " " +
      "AND s.sect_author = a.id" + " ";
	}
  
  public String getSrc()
  {
    return toSearch;
  }
  
  public void setSQL(String s)
  {
    toSearch = s;
  }
  
	public void executeTheRequest()
	{
		try
      {
        String QLquery = getSQLQuery();
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(QLquery);
        
        while (rs.next()) {
          String s = rs.getString("filename");
          if(!doc.contains(s))
            {
              doc.add(s);
            }
			}
        
      } catch (SQLException E)
        {
          System.err.println("SQLException: " + E.getMessage());
          System.err.println("SQLState: " + E.getSQLState());
          System.err.println("VendorError: " + E.getErrorCode());
        }
	}
  
	public void selectByFile()
	{
    for(int ert=0;ert<tmpFNameLM.size();ert++)
      {
        if(ert==0)
          sqlQuery = sqlQuery + "t.filename='" + tmpFNameLM.elementAt(0) + "'";
        else
          sqlQuery = sqlQuery + " OR t.filename='" + tmpFNameLM.elementAt(ert) + "'";
      }
	}
  
	public void selectByLang()
	{
    for(int ert=0;ert<tmpLnLM.size();ert++)
      {
        if(tmpFNameLM.getSize() > 0)
          sqlQuery = sqlQuery + " OR s.language='" + tmpLnLM.elementAt(ert) + "'";
        else if(ert!=0)
          sqlQuery = sqlQuery + " OR s.language='" + tmpLnLM.elementAt(ert) + "'";
        else
          sqlQuery = sqlQuery + "s.language='" + tmpLnLM.elementAt(0) + "'";
      }
    
	}
  
  public void selectByTrans()
  {
    for(int ert=0;ert<tmpTNameLM.size();ert++)
      {
        if(tmpFNameLM.getSize() > 0 || tmpLnLM.getSize() > 0 )
          sqlQuery = sqlQuery + " OR tr.transName='" + tmpTNameLM.elementAt(ert) + "'";
        else if(ert!=0)
          sqlQuery = sqlQuery + "OR tr.transName='" + tmpTNameLM.elementAt(ert) + "'";
        else
          sqlQuery = sqlQuery + "tr.transName='" + tmpTNameLM.elementAt(0) + "'";
      }
    
    for(int egn=0;egn<tmpTGendLM.size();egn++)
      {
        try {
        if(tmpFNameLM.getSize() > 0 || tmpLnLM.getSize() > 0 || tmpTNameLM.getSize() > 0)
          sqlQuery = sqlQuery + " OR tr.tGender='" + tmpTGendLM.elementAt(egn) + "'";
        else if(egn!=0)
          sqlQuery = sqlQuery + " OR tr.tGender='" + tmpTGendLM.elementAt(egn) + "'";
        else
          sqlQuery = sqlQuery + "tr.tGender='" + tmpTGendLM.elementAt(0) + "'";
        }
        catch (Exception e)
          {
              System.out.println(e);
            
          }
      }
    
    for(int enat=0;enat<tmpTNatLM.size();enat++)
      {
        if(tmpFNameLM.getSize() > 0 || tmpLnLM.getSize() > 0 || 
           tmpTNameLM.getSize() > 0 || tmpTGendLM.getSize() > 0)
          sqlQuery = sqlQuery + " OR tr.tNation='" + tmpTNatLM.elementAt(enat) + "'";
        else if(enat!=0)
          sqlQuery = sqlQuery + " OR tr.tNation='" + tmpTNatLM.elementAt(enat) + "'";
        else
          sqlQuery = sqlQuery + "tr.tNation='" + tmpTNatLM.elementAt(0) + "'";
      }
    
    for(int esex=0;esex<tmpTSexLM.size();esex++)
      {
        if(tmpFNameLM.getSize() > 0 || tmpLnLM.getSize() > 0 
           || tmpTNameLM.getSize() > 0 || tmpTGendLM.getSize() > 0 
           || tmpTNatLM.getSize() > 0)
          sqlQuery = sqlQuery + " OR tr.tSexO='" + tmpTSexLM.elementAt(esex) + "'";
        else if(esex!=0)
          sqlQuery = sqlQuery + " OR tr.tSexO='" + tmpTSexLM.elementAt(esex) + "'";
        else
          sqlQuery = sqlQuery + "tr.tSexO='" + tmpTSexLM.elementAt(0) + "'";
      }
  }
  
	public void selectByAuth()
	{
    for(int ert=0;ert<tmpANameLM.size();ert++)
      {
        if(tmpTNameLM.getSize() > 0 || tmpTGendLM.getSize() > 0 
           || tmpTNatLM.getSize() > 0 || tmpTSexLM.getSize() > 0)
          sqlQuery = sqlQuery + " OR a.authName='" + tmpANameLM.elementAt(0) + "'";
        else if(ert==0)
          sqlQuery = sqlQuery + "a.authName='" + tmpANameLM.elementAt(0) + "'";
        else
          sqlQuery = sqlQuery + "OR a.authName='" + tmpANameLM.elementAt(ert) + "'";
      }
    
    for(int egn=0;egn<tmpAGendLM.size();egn++)
      {
        if(tmpTNameLM.getSize() > 0 || tmpTGendLM.getSize() > 0 
           || tmpTNatLM.getSize() > 0 || tmpTSexLM.getSize() > 0 
           || tmpANameLM.getSize() > 0)
          sqlQuery = sqlQuery + " OR a.aGender='" + tmpAGendLM.elementAt(egn) + "'";
        else if(egn==0)
          sqlQuery = sqlQuery + "a.aGender='" + tmpAGendLM.elementAt(0) + "'";
        else
          sqlQuery = sqlQuery + "OR a.aGender='" + tmpAGendLM.elementAt(egn) + "'";
      }
    
    for(int enat=0;enat<tmpANatLM.size();enat++)
      {
        if(tmpTNameLM.getSize() > 0 || tmpTGendLM.getSize() > 0 || tmpTNatLM.getSize() > 0 
           || tmpTSexLM.getSize() > 0 || tmpANameLM.getSize() > 0 || tmpAGendLM.getSize() > 0)
          sqlQuery = sqlQuery + " OR a.aNation='" + tmpANatLM.elementAt(enat) + "'";
        else if(enat==0)
          sqlQuery = sqlQuery + "a.aNation='" + tmpANatLM.elementAt(0) + "'";
        else
          sqlQuery = sqlQuery + " OR a.aNation='" + tmpANatLM.elementAt(enat) + "'";
      }
    
    for(int esex=0;esex<tmpASexLM.size();esex++)
      {
        if(tmpTNameLM.getSize() > 0 || tmpTGendLM.getSize() > 0 || tmpTNatLM.getSize() > 0 
           || tmpTSexLM.getSize() > 0 || tmpANameLM.getSize() > 0 || tmpAGendLM.getSize() > 0 
           || tmpANatLM.getSize() > 0)
          sqlQuery = sqlQuery + "OR a.aSexO='" + tmpASexLM.elementAt(esex) + "'";
        else if(esex!=0)
          sqlQuery = sqlQuery + " OR a.aSexO='" + tmpASexLM.elementAt(esex) + "'";
        else
          sqlQuery = sqlQuery + "a.aSexO='" + tmpASexLM.elementAt(0) + "'";
      }
	}
  
  public void selectAdv () {
    String aq = qbox.getText().trim();
    if ( ! aq.equals("") )
      sqlQuery +=  "AND ("+aq;
  }


  public void removeVecElements()
  {
    tmpTNameLM.removeAllElements();
    tmpTGendLM.removeAllElements();
    tmpTNatLM.removeAllElements();
    tmpTSexLM.removeAllElements();
    
    tmpANameLM.removeAllElements();
    tmpAGendLM.removeAllElements();
    tmpANatLM.removeAllElements();
    tmpASexLM.removeAllElements();
    
    tmpFNameLM.removeAllElements();
    tmpLnLM.removeAllElements();
  }
  
  public String getSQLQuery()
  {
    return sqlQuery;
  }
}
