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

import modnlp.tec.client.gui.GraphicalSubcorpusSelector;
import modnlp.tec.client.gui.RemoteCorpusChooser;
import modnlp.tec.client.gui.PreferPanel;
import modnlp.tec.client.gui.SplashScreen;
import modnlp.idx.database.Dictionary;
import modnlp.idx.database.DictProperties;
import modnlp.idx.gui.CorpusChooser;
import modnlp.idx.query.WordQuery;
import modnlp.util.IOUtil;

import java.awt.Font;
import java.awt.Insets;
import java.awt.Event;
import java.awt.Color;
import java.awt.Container;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.*;
import java.awt.event.MouseListener;
import java.awt.event.MouseEvent;
import javax.swing.*;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import java.sql.*;
import javax.swing.event.*;
//import javax.swing.filechooser.ExtensionFileFilter;
import java.io.*;
import java.util.Arrays;
import java.util.Vector;
import java.util.Enumeration;
import java.util.Date;
import java.util.StringTokenizer;
import java.net.*;
import org.xml.sax.Parser;
import org.xml.sax.InputSource;
import org.xml.sax.DocumentHandler;
import org.xml.sax.ErrorHandler;
import org.xml.sax.helpers.ParserFactory;

/**
 *  This frame implements a 'concordance browser' that interacts with
 *  the <a href="../server/index.html">TEC Server</a> or works
 *  stand-alone accessing the corpus index through <a
 *  href="../../idx/database/Dictionary.html">Dictionary.java</a>
 *
 * @author  S. Luz &#60;luzs@acm.org&#62;
 * @version <font size=-1>$Id: Browser.java,v 1.9 2003/08/06 16:58:56 luzs Exp $</font>
 * @see  Dictionary
 * @see  Server
*/

public class Browser extends JFrame
  implements  ActionListener, ItemListener,
                ListSelectionListener, TecDefaultChangeListener
{

  
  public static final String RELEASE = "0.5.1";
  public static final String REVISION = "$Revision: 1.9 $";
  String BRANDNAME = "MODNLP/TEC";
  /* The next 2 thread objects will eventually become plug-in's */
  public ConcordanceThread concThread = null;
  ConcordanceProducer concordanceProducer = null;

  private SortThread sortThread = null;

  private Dictionary dictionary = null;
  /** Deafult location of TecServer (a valid DNS where the server lives) */
  public String SERVER;
	/** Default port */
  public int PORTNUM = 1240;
  /** Deafult URL where to find header files */
  public String HEDBAS;
  // plugin list
  private static final String PLGLIST = "teclipluginlist.txt";
  /** Object to record user preferences */
  private PreferPanel preferenceFrame = new PreferPanel(this);
  private ClientProperties clProperties;

  // Strings for the GUI
  private static final String HEDBUT = "Metadata";
  private static final String EXTBUT = "Extract";
  private static final String DLDBUT = "Save";
  private static final String STLBUT = "Sort Left";
  private static final String STRBUT = "Sort Right";
  private static final String PREBUT = "Preferences";
  private static final String DOBUTT = "Search";
  private static final String ASCBUTT = "Subcorpus";
  private static final String QUITBUT = "QUIT";

  private final static int FRAME_WIDTH = 1000;
  private final static int FRAME_HEIGHT = 700;
  private final static int ONE_SECOND = 1000;
  private final static int SRTBARMAX = 6;

  private static SplashScreen splashScreen; 

  private JProgressBar progressBar;
  private Timer timer;
  private Timer srt_timer;
  private Timer ucrt_timer;
  private JComboBox leftSortCtx = new JComboBox();
  private JComboBox rightSortCtx = new JComboBox();
  private ListDisplay concList = new ListDisplay(this);
  private String concordances = null;
  private JTextField keyword;
  //private JComboBox case_select;
  private int currentIndex = 0;
  private int height;
  private JButton concButton;

  private JMenu  fileMenu = new JMenu("File");
  private JMenuItem nlcButton = new JMenuItem("New local corpus...");
  private JMenuItem nrcButton = new JMenuItem("New Internet corpus...");    
  private JMenuItem dldButton = new JMenuItem("Save concordances...");  
	private JMenuItem quitButton = new JMenuItem(QUITBUT);

  private JMenu  prefMenu = new JMenu("Options");
  private JCheckBoxMenuItem caseCheckBox = 
    new JCheckBoxMenuItem("Case sensitive");
  private JMenuItem advConcButton = 
    new JMenuItem("Select Sub-corpus...");
  private boolean advConcFlag = false;
  private JCheckBoxMenuItem advConcFlagItem = 
    new JCheckBoxMenuItem("Activate sub-corpus selection");
  private JMenuItem prefButton = new JMenuItem("Preferences...");

  private JMenu  pluginMenu = new JMenu("Plugins");

  private JMenu  helpMenu = new JMenu("Help");
  private JMenuItem helpButton = new JMenuItem("Contents");
  private JMenuItem aboutButton = new JMenuItem("About MODNLP...");    

  private JButton stlButton = new JButton(STLBUT);
  private JButton strButton = new JButton(STRBUT);
  private JButton	extractButton = new JButton(EXTBUT);
  private	JButton	headerButton = new JButton(HEDBUT);

  private String keywordString = "";
  private String sqlString = "";
  private JPanel optArea = new JPanel();
  private JPanel outArea = new JPanel();
  private JPanel statusArea = new JPanel();
  private JLabel statusLabel = new JLabel();
  private JLabel statusLabelScroll = new JLabel();
  private JPanel li1 = new JPanel();
  private JPanel opt = new JPanel();
  private JPanel concLabel = new JPanel();
  private boolean nextOn = false;
  private boolean prevOn = false;
  private boolean noApplet = false;
  private boolean standAlone = false;
  private boolean debug = true;
  private boolean firstRemoteFlag = true;
  private Connection conn;
  private int srt_i = 0;
  //private AdvConcSearch advSearchFrame;
  private String xquerywhere;
  private String encoding = null;
  GraphicalSubcorpusSelector guiSelector = null;

  /** Create a TEC Window Object
   * @param width   window width
   * @param height   window height
   */
  public Browser(int width, int height){

    super();
    setTitle(getBrowserName());
    clProperties = new ClientProperties();

    Container contentPane = getContentPane();
    setSize(width,height);
    setFont(new Font("Helvetica",Font.PLAIN, 12));
    
    
    splashScreen.incProgress();
    // this will eventually be moved into a plugin
    //advSearchFrame = new AdvConcSearch(this);
    //advSearchFrame.setSize(700,500);
    
    splashScreen.incProgress();

    // Lay out menu bar. 
    JMenuBar menuBar;
    
    menuBar = new JMenuBar();
    setJMenuBar(menuBar);
    
    fileMenu.add(nlcButton);
    fileMenu.add(nrcButton);
    fileMenu.add(dldButton);
    fileMenu.addSeparator();
    fileMenu.add(quitButton);
    
    prefMenu.add(caseCheckBox);
    prefMenu.add(advConcButton);
    prefMenu.add(advConcFlagItem);
    prefMenu.addSeparator();
    prefMenu.add(prefButton);

    helpMenu.add(helpButton);
    helpMenu.add(aboutButton);

    menuBar.add(fileMenu);
    menuBar.add(prefMenu);
    menuBar.add(pluginMenu);
    menuBar.add(Box.createHorizontalGlue());
    menuBar.add(helpMenu);

    splashScreen.incProgress();

    /// create  sub-components

    // create keyword box
    JPanel kwd = new JPanel();
    kwd.add( new JLabel("Keyword"));
    keyword = new JTextField(15);
    kwd.add( keyword);
    keyword.setToolTipText("Syntax: word_1[+[[context]]word2...]. E.g. 'seen+before' will find '...never seen before...' etc; 'seen+[2]before' finds the '...seen her before...'");
    kwd.setBorder(BorderFactory.createEtchedBorder());
    concButton = new JButton(DOBUTT);
    kwd.add( concButton );

    // sort panels
    JPanel lsp = new JPanel();
    JPanel rsp = new JPanel();
    lsp.setBorder(BorderFactory.createEtchedBorder());
    rsp.setBorder(BorderFactory.createEtchedBorder());
    for (int i = 1 ; i <= PreferPanel.SCTXMAX ; i++){
      leftSortCtx.addItem(""+i);
      rightSortCtx.addItem(""+i);
    }
    lsp.add(leftSortCtx);
    lsp.add(stlButton);
    
    rsp.add(rightSortCtx);
    rsp.add(strButton);

    leftSortCtx.setEnabled(false);
    rightSortCtx.setEnabled(false);
    stlButton.setEnabled(false);
    strButton.setEnabled(false);
    extractButton.setEnabled(false);
    headerButton.setEnabled(false);
    dldButton.setEnabled(false);
    
    nlcButton.setToolTipText("Select a new corpus index");
    nrcButton.setToolTipText("Select a new corpus index server");
    dldButton.setToolTipText("Save the displayed concordances to disk");
    stlButton.setToolTipText("Sort with left context horizon indicated on the box");
    strButton.setToolTipText("Sort with right context horizon indicated on the box");
    extractButton.setToolTipText("Display text extract of the selected line");
    headerButton.setToolTipText("Display header file of the selected line");
    
    progressBar = new JProgressBar(0, ConcArray.arraymax);
    progressBar.setValue(0);
    progressBar.setStringPainted(true);
    progressBar.setString("");
    
    //Create a timer for monitoring dwld progress.
    timer = new Timer(ONE_SECOND, new ActionListener() {
        public void actionPerformed(ActionEvent evt) {
          progressBar.setValue(concThread.ctRead);
          //progressBar.setString(perct+"% completed");
          if ( concThread.ctRead >= concThread.noFound ||
               concThread.ctRead >= ConcArray.arraymax ||
               ! concThread.atWork() )
            {
              timer.stop();
              progressBar.setValue(progressBar.getMaximum());
            }
        }
      });
    ucrt_timer = new Timer(300, new ActionListener() {
        public void actionPerformed(ActionEvent evt) {
          progressBar.setValue(srt_i++ % SRTBARMAX);
          //progressBar.setString(perct+"% completed");
          if (concThread.serverResponded ||!concThread.atWork())
            {
              ucrt_timer.stop();
              progressBar.setString("Done");
              progressBar.setValue(progressBar.getMaximum());
            }
        }
      });
    srt_timer = new Timer(300, new ActionListener() {
        public void actionPerformed(ActionEvent evt) {
          //Int perct = (Int) (progressBar.getPercentComplete()*100);
          progressBar.setValue(srt_i++ % SRTBARMAX);
          if ( sortThread.thread == null )
            {
              srt_timer.stop();
              progressBar.setString("Done");
              progressBar.setValue(progressBar.getMaximum());
            }
        }
      });
    
    // create status line (bottom of the screen)
    statusArea.setLayout( new FlowLayout(FlowLayout.LEFT));
    statusArea.add(progressBar);
    
    statusArea.add(statusLabel);
    statusArea.add(statusLabelScroll);

    // -------- plugins partly disabled for the time being
    splashScreen.setMessage("Loading plugins...");
    splashScreen.incProgress();
    pluginMenu.setEnabled(true);
    loadPluginMenu();

    
    // set up even listening
    concButton.addActionListener( this );
    quitButton.addActionListener( this );
    prefButton.addActionListener( this );
    advConcButton.addActionListener( this );
    advConcFlagItem.addActionListener( this );
    keyword.addActionListener( this );
    
    caseCheckBox.addActionListener( this );
    caseCheckBox.setState(false);
    advConcFlagItem.setState(false);
    advConcFlagItem.addActionListener(new ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
          setAdvConcFlag(advConcFlagItem.isSelected());
        }});
    stlButton.addActionListener(this);
    strButton.addActionListener(this);
    extractButton.addActionListener(this);
    headerButton.addActionListener(this);
    nlcButton.addActionListener(this);
    nrcButton.addActionListener(this);
    dldButton.addActionListener(this);

    helpButton.addActionListener(this);
    aboutButton.addActionListener(this);
    
    // lay out top toolbox
    contentPane.setLayout(new BorderLayout());
    outArea.setLayout(new BorderLayout());
    concLabel.setLayout(new BoxLayout(concLabel, BoxLayout.X_AXIS));

    concLabel.setFont(new Font("Helvetica", Font.PLAIN, 12));
    concLabel.setForeground(Color.green.darker().darker());
    
    concLabel.add(Box.createGlue());
    concLabel.add( kwd );
    concLabel.add(Box.createGlue());
    concLabel.add(lsp);
    concLabel.add(rsp);
    concLabel.add(Box.createGlue());
    concLabel.add(extractButton);
    concLabel.add(Box.createGlue());
    concLabel.add(headerButton);
    concLabel.add(Box.createGlue());
    
    
    // lay out main interactive area (toolbox + list)
    outArea.add("North",concLabel);
    outArea.add("Center",concList);
    
    // add status area (status indicator + messages)
    contentPane.add("North",optArea);
    contentPane.add("Center",outArea);
    contentPane.add("South",statusArea);
    
    // et voila'...
    addComponentListener(concList);
    preferenceFrame.addDefaultChangeListener(concList);
    preferenceFrame.addDefaultChangeListener(this);
    return;
  }
  
  private void loadPluginMenu () {
      ClassLoader cl = this.getClass().getClassLoader();
      BufferedReader in
        = new BufferedReader(new 
                             InputStreamReader(cl.getResourceAsStream(PLGLIST)));
      String plg = null;
      try {
        while ( (plg = in.readLine() ) != null ){
          try {
            StringTokenizer st = new StringTokenizer(plg, ":");
            // first token: class name (ignore for now)
            final Plugin tp = (Plugin)IOUtil.loadPlugin(st.nextToken());
            tp.setParent(this);
            JMenuItem pmi = new JMenuItem(st.nextToken());
            pluginMenu.add(pmi);
            pmi.addActionListener(
                                  new ActionListener(){
                                    public void actionPerformed(ActionEvent event)
                                    {
                                      tp.activate();
                                    }
                                  });
          }
          catch (ClassNotFoundException e) {
            System.err.println("Warning (Browser): error loading plugin: "+e);
          }
        }
      }
      catch (Exception e) {
        e.printStackTrace(System.err);
      }
  }

  public static String getRelease (){
    return RELEASE;
  }
  
  public static String getVersion (){
    return REVISION.substring(11,REVISION.lastIndexOf("$"));
  }
  
  public void setBrand(String b){
    BRANDNAME = b;
  }

  public String getBrowserName (){
    return BRANDNAME+" Concordance Browser (v. "+getRelease()+")";
  }

  /*public void getADSMenu(AdvConcSearch acs)
    {
    acs.setMenus();
    }*/
  

  ///////////////////////// ACTION LISTENER ///////////////////////////////////////
  public void actionPerformed(ActionEvent evt)
  {
    String arg = evt.getActionCommand();
		//System.out.println("ARG:"+arg);
    if(evt.getSource() instanceof JTextField){
      if (!WordQuery.isValidQuery(keyword.getText())) {
        alertWindow("Invalid query syntax");
        return;
      }
      if (sortThread != null)
        sortThread.stop();
      labelMessage("Building concordance list. Please wait...");
      updateStatusLabelScroll("");
      concordancer();
      //goSearch = "false";
      // displayConcord();
    }
    if(evt.getSource() instanceof JButton)
      {
        if(arg.equals(DOBUTT))
	  {
      if (sortThread != null)
	      sortThread.stop();
	    labelMessage("Building concordance list. Please wait...");
      updateStatusLabelScroll("");
      //goSearch = "false";
      concordancer();
	    // displayConcord();
	  }
	else if(arg.equals(STLBUT))
	  {
	    if ( concThread.atWork()  ) {
	      if ( ! interruptDownloading() )
          return;
	      concThread.stop();
	    }
	    if (sortThread != null)
	      sortThread.stop();
	    int sortContextHorizon = getSortLeftCtxHorizon();
	    concThread.conc.setSortContextHorizonFlag(0-sortContextHorizon);
	    sortThread = new
              SortThread(concThread.conc.concArray,
                         0,
                         concThread.ctRead,
                         new LeftComparer(sortContextHorizon,
                                          preferenceFrame.maxContext/2));
	    progressBar.setMaximum(SRTBARMAX-1);
	    progressBar.setString("Sorting");
	    sortThread.addConcordanceDisplayListener(concList);
	    sortThread.start();
	    srt_timer.start();
	    labelMessage("Sorting with context horizon "
                   +sortContextHorizon+" (left)");
	  }
	else if(arg.equals(STRBUT))
	  {
	    if ( concThread.atWork()  ) {
	      if (! interruptDownloading() )
          return;
	      concThread.stop();
	    }
	    if (sortThread != null)
	      sortThread.stop();
	    int sortContextHorizon = getSortRightCtxHorizon();
	    concThread.conc.setSortContextHorizonFlag(sortContextHorizon);
	    sortThread = new
        SortThread(concThread.conc.concArray,
                   0,
                   concThread.ctRead,
                   new RightComparer(sortContextHorizon,
                                     preferenceFrame.maxContext/2));
	    sortThread.addConcordanceDisplayListener(concList);
	    progressBar.setMaximum(SRTBARMAX-1);
	    progressBar.setString("Sorting");
	    sortThread.start();
	    srt_timer.start();
	    labelMessage("Sorting with context horizon "
                         +sortContextHorizon+" (right)");
	  }
	else if(arg.equals(EXTBUT))
	  {
	    ConcordanceObject sel = concList.getSelectedValue();
	    if (sel == null) {
	      alertWindow("Please select a concordance!");
	    }
	    else
	      showExtract(sel);
	  }
	else if(arg.equals(HEDBUT))
	  {
	    ConcordanceObject sel = concList.getSelectedValue();
	    if (sel == null) {
	      alertWindow("Please select a concordance!");
	    }
	    else
	      showHeader(sel);
	  }
      }
    else 
      if( evt.getSource() == prefButton )
        {
          preferenceFrame.setSize(400,300);
          preferenceFrame.show();
        }        
      else if(  evt.getSource() == advConcButton )
        {
          guiSelector.activate();
        }
      else if(  evt.getSource() == nrcButton )
        chooseNewRemoteCorpus();
      else if(  evt.getSource() == nlcButton )
        chooseNewLocalCorpus();
      else if(  evt.getSource() == dldButton )
        {
          try
            {
              JFileChooser filedial = new JFileChooser(keyword.getText()
                                                       +".tec");
              //ExtensionFileFilter filter = new ExtensionFileFilter();
              //filter.addExtension("tec");
              //filter.setDescription("Tec concordance files");
              //filedial.setFileFilter(filter);
              //File dire = filedial.getCurrentDirectory();
              int returnVal = filedial.showDialog(this, "Save to disk");
              if (returnVal == JFileChooser.APPROVE_OPTION)
                {
                  File file = filedial.getSelectedFile();
                  //System.out.println(file.getName());
                  Download dlf =
                    new Download(file);
                  dlf.dumpConcordance(concThread.conc);
                }
            }
          catch (java.io.IOException e)
            {
              alertWindow("Error downloading concordances\n!"+e);
            }
        }
      else
        if (  evt.getSource() == helpButton ){
          HelpBrowser hb = new HelpBrowser();
          if ( hb.ok() )
            hb.show();
        }
        if (  evt.getSource() == aboutButton ){
          HelpBrowser hb = new HelpBrowser("modnlp/tec/client/help/about.html", "About...");
          if ( hb.ok() )
            hb.show();
        }
      else
        if (  evt.getSource() == quitButton ){
          if ( noApplet )
            quit();
				}
  }
  ///////////////////////// END OF ACTION LISTENER /////////////////////////////////////


  private void quit(){
    System.err.println("BYE...");
    if (dictionary != null)
      dictionary.close();
    if (guiSelector != null)
      guiSelector.dispose();
    System.exit(0);
  }


  public void itemStateChanged(ItemEvent e) {
    JMenuItem source = (JMenuItem)(e.getSource());
    String s = "Item event detected.";
  }

	//Listener method for list selection changes.
	public void valueChanged(ListSelectionEvent e) {
	  if (e.getValueIsAdjusting() == false) {

			if (concList.getSelectedIndex() == -1) {
				//No selection: disable header and extract
				extractButton.setEnabled(false);
				headerButton.setEnabled(false);
			} else {
				extractButton.setEnabled(true);
				headerButton.setEnabled(true);
			}
		}
	}

	private boolean interruptDownloading()
	{
		int stop =
			JOptionPane.
      showConfirmDialog(this,
                        "Concordance list is not completed. "+
                        "\nInterrupt transfer and continue sorting?",
                        "Interrupt transfer and continue sorting?",
                        JOptionPane.YES_NO_OPTION);
		return stop == 0 ? true : false;
	}

	private void alertWindow (String msg)
	{
		JOptionPane.showMessageDialog(null,
                                  msg,
                                  "alert",
                                  JOptionPane.ERROR_MESSAGE);
	}

  /** Manage the main concordance interaction with the server
   *  and builds a list of concordances into <code>array</code>
   *  @see #array
   *  @see ConcArray
   */
  private void concordancer()
  {
    keywordString = keyword.getText();
    //sqlString = advSearchFrame.getSQLQuery();
    TecClientRequest request = new TecClientRequest();
    request.put("keyword",keywordString);
    request.put("context",preferenceFrame.getContextSize());
    request.put("sgml",preferenceFrame.getSGMLFlag());
    String casestate = caseCheckBox.getState()? "sensitive" : "insensitive";
    request.put("case",casestate);
    request.put("request","concord");
    if (advConcFlag && xquerywhere != null )
      request.put("xquerywhere",xquerywhere);
    if ( (concThread != null) ) {
      concThread.stop();
    }
    concList.removeAll();
    //concList.reset();
    if (standAlone) {
      concThread = new ConcordanceThread(this, 
                                         concordanceProducer.getBufferedReader(), 
                                         request);
      concThread.start();
      concordanceProducer.setRequest(request);
      concordanceProducer.start();
    }
    else {
      request.setServerURL("http://"+SERVER);
      request.setServerPORT(PORTNUM);
      request.setServerProgramPath("/concordancer");
      concThread = new ConcordanceThread(this, request);
      concThread.setEncoding(encoding);
      concThread.start();
    }
    concThread.addConcordanceDisplayListener(concList);
    //SwingUtilities.invokeLater(concThread);
    //concList = new ListDisplay(this, concThread.conc);
    currentIndex = 0;
    progressBar.setMaximum(SRTBARMAX-1);
    ucrt_timer.start();
    progressBar.setString("Searching...");
    //		}
  }
  
  public void setXQueryWhere(String w){
    xquerywhere = w;
  }

  public void displaySortedList (String msg)
  {
		//concList.resetFontIfChanged(preferenceFrame.fontSize);
		concList.displayArraySegment(concThread.conc, 0);
		updateStatusLabel("Sorting done.");
		extractButton.setEnabled(false);
		headerButton.setEnabled(false);
		dldButton.setEnabled(true);
		extractButton.revalidate();
  }

  /** Display concordance list stored in <code>array</code>
   *  @see #array
   *  @see ConcArray
   */
  public void displayConcord ()
  {
    try {
      //updateStatusLabel(null);
      if ( concList != null && concList.list != null)
        concList.list.clearSelection();
      if (concThread.noFound > 0){
        if (concThread.noFound > concThread.ctRead)
          updateStatusLabel("  Searching through "
                            +concThread.noFound+" concordances ");
        timer.start();
        progressBar.setString(null);
        progressBar.setMaximum(concThread.noFound);
        strButton.setEnabled(true);
        stlButton.setEnabled(true);
        leftSortCtx.setEnabled(true);
        rightSortCtx.setEnabled(true);
        extractButton.setEnabled(false);
        headerButton.setEnabled(false);
        if ( noApplet )
          dldButton.setEnabled(true);
      }
      else if (concThread.noFound == 0)
        labelMessage(" No concordances found");
    }
    catch (NumberFormatException e){
      labelMessage("Error caught: Server may be down. ");
      concThread.stop();
    }
    catch (NullPointerException e){
      labelMessage("Error caught: Server may be down. ");
      concThread.stop();
    }
  }

	public void updateStatusLabel (String msg){
    if (debug)
      System.err.println(msg);
		statusLabel.setText(msg);
	}
	public void updateStatusLabelScroll (String msg){
		statusLabelScroll.setText(msg);
	}
  /** Display a message in this window's user message area.*/
  public void labelMessage (String msg)
  {
    updateStatusLabel(msg);
    concLabel.validate();
    concLabel.repaint();
  }

  public void clearMessageArea() {
    updateStatusLabel("");
    updateStatusLabelScroll("");
  }

  /** Show extract of text identified by position <code>sel</code>
   * in the current <code>ConcArray</code>
   *  @see #array
   *  @see ConcArray
   */
  public void showExtract(ConcordanceObject sel)
  {
    String filename = sel.filename;
    long filepos = sel.filepos;
    if ( filepos < 0 ) {
      System.err.println("SERVER ERROR: invalid position "+filepos);
    }
    else {
      TecClientRequest request = new TecClientRequest();
      request.put("filename",filename);
      request.put("context",preferenceFrame.getExtractContextSize());
      request.put("sgml",preferenceFrame.getSGMLFlag());
      request.put("position",filepos);
      request.put("request","extract");
      request.put("keyword",keywordString);
      request.setServerURL("http://"+SERVER);
      request.setServerPORT(PORTNUM);
      request.setServerProgramPath("/extractor");
      //if (concThread != null && concThread.isAlive() ){
      //concThread.stop();
      //}
      ContextClient tecClient = null;
      if (standAlone)
        tecClient = new ContextClient(request, dictionary);
      else
        tecClient = new ContextClient(request);
      preferenceFrame.addDefaultChangeListener(tecClient);
    }
  }


  public Object [] getSQLMetadata(String field)
  {
    TecClientRequest request = new TecClientRequest();
    request.put("dbfield",field);
    request.setServerURL("http://"+SERVER);
    request.setServerPORT(PORTNUM);
    request.put("request","sqlmetaquery");
    request.setServerProgramPath("/sqlmetaquery");
    SQLMetaQuery sq = new SQLMetaQuery(SERVER, PORTNUM, request);
    return sq.response.toArray();
  }

  /** Show header file of text identified by position <code>sel</code>
   *  in the current <code>ConcArray</code>
   *  @see #array
   *  @see ConcArray
   */
  public void showHeader(ConcordanceObject sel)
  {
    String filename = sel.filename;
    String headerName = 
      filename.substring(0,filename.lastIndexOf('.'))+".hed";
    int p = headerName.lastIndexOf(java.io.File.separator);
    headerName = p < 0? headerName : headerName.substring(p);
    showHeader(headerName);
  }

  public void showHeader(String headerName)
  {
    int windowHeight = 400;
    int windowWidth = 350;
    String tmp;
      StringBuffer content = new StringBuffer();
    //System.out.println("URL--:"+HEDBAS+headerName);
    //HeaderReader header = new HeaderReader(HEDBAS+headerName);
    //HeaderXMLHandler parser =  new HeaderXMLHandler();
    try {
			BufferedReader in = null;
      URL headerURL = null;
      if (standAlone) {
        in = 
          new BufferedReader(new InputStreamReader(new FileInputStream(HEDBAS+
                                                                       java.io.File.separator+
                                                                       headerName)));
      }
      else {
        headerURL = new URL(HEDBAS+java.io.File.separator+headerName);
        in = new BufferedReader(new InputStreamReader(headerURL.
                                                      openConnection().
                                                      getInputStream(),
                                                      "UTF-8"));
      }
      InputSource source = new InputSource(in);
      source.setEncoding(encoding);
      /*
      if ( preferenceFrame.stSGML.equals("no") ) {
        parser.parse(source);
      }
      else
      */
        while ( (tmp = in.readLine()) != null )
          content.append(tmp+"\n");
    }
    catch (Exception e) {
      System.err.println("Error retrieving metadata: "+e);
    }
    // HeaderClass header = new HeaderClass(filename);
    FullTextWindow window =  new FullTextWindow(headerName,
                                                content);
		preferenceFrame.addDefaultChangeListener(window);
    window.setSize(windowWidth, windowHeight);
    //System.err.println(content);
    window.show();
  }

	// The TEC default change interface
  public void defaultChanged(DefaultChangeEvent e)
	{
	}

  public void defaultChanged(SortHorizonChangeEvent e)
	{
		leftSortCtx.setSelectedItem(""+e.getNewHorizon());
		rightSortCtx.setSelectedItem(""+e.getNewHorizon());
	}

  public void defaultChanged(FontSizeChangeEvent e)
	{
	}

	public int getFontSize()
	{
		return preferenceFrame.getFontSize();
	}

	public int getSortRightCtxHorizon()
	{
		return (new Integer((String)rightSortCtx.getSelectedItem())).intValue();
	}
	public int getSortLeftCtxHorizon()
	{
		return (new Integer((String)leftSortCtx.getSelectedItem())).intValue();
	}


	public boolean concArrayExists ()
	{
		if ( concThread != null &&
				 concThread.conc != null )
			return true;
		else
			return false;
	}

	public ConcArray getConcArray ()
	{
		return concThread.getConcArray();
	}

	public int getNoFound () {
		if ( concThread != null  )
			return concThread.getNoFound();
		else
			return 0;
	}  

  public void setAdvConcFlag (boolean f){
    if (advConcFlagItem.isSelected() != f)
      advConcFlagItem.doClick();
    advConcFlag = f;
  }
  
  public boolean isStandAlone() {
    return standAlone;
  }

  public void chooseNewLocalCorpus(){
    CorpusChooser ncc = new CorpusChooser(clProperties.getProperty("last.index.dir"));
    int r;
    while (!((r = ncc.showChooseCorpus()) == CorpusChooser.APPROVE_OPTION ||
             (r != CorpusChooser.CANCEL_OPTION )) ) 
      {
        JOptionPane.showMessageDialog(null, "Please choose a corpus directory (folder)");      
      }
    if (r == CorpusChooser.CANCEL_OPTION)
      return;
    String cdir = ncc.getSelectedFile().toString();
    setLocalCorpus(cdir);
  }

  public void setLocalCorpus (String cdir) {
    DictProperties dictProps = new DictProperties(cdir);
    if (dictionary != null)
      dictionary.close();
    dictionary = new Dictionary(false,dictProps);

    clProperties.setProperty("last.index.dir", cdir);
    standAlone = true;
    clProperties.setProperty("stand.alone","yes");

    setTitle(getBrowserName()+": index at "+cdir);
    dictionary.setVerbose(debug);
    setHeadersURL(dictProps);
    encoding = dictProps.getProperty("file.encoding");
    if (guiSelector != null)
      guiSelector.dispose();
    guiSelector = null;
    System.gc();
    guiSelector = new GraphicalSubcorpusSelector(dictionary, this);
    concordanceProducer = new ConcordanceProducer(dictionary);
  }

  private void setHeadersURL(DictProperties dictProps){
    int r;
    String hh = null;
    if ((hh = dictProps.getProperty("headers.home")) == null)  // an unsafe default
      {
        CorpusChooser ncc = new CorpusChooser(null);
        while (!((r = ncc.showChooseDir("Choose a headers directory")) == CorpusChooser.APPROVE_OPTION ||
                 r != CorpusChooser.CANCEL_OPTION)  ) 
          {
            JOptionPane.showMessageDialog(null, "Please choose a headers directory (folder)");      
          }
        if (r == CorpusChooser.CANCEL_OPTION){
          String cdir = clProperties.getProperty("last.index.dir");
          hh = cdir.substring(0,cdir.lastIndexOf('/', cdir.length()-1))+"/headers/";
        }
        else
          hh = ncc.getSelectedFile().toString();
        dictProps.setProperty("headers.home", hh);
        dictProps.save();
      }
    HEDBAS = hh;
    clProperties.setProperty("tec.client.headers", HEDBAS);
    preferenceFrame.setHeaderBaseURL(HEDBAS);
  }

  public void chooseNewRemoteCorpus () {
    RemoteCorpusChooser rcc = 
      new RemoteCorpusChooser(this, clProperties.getProperty("tec.client.server")+":"+
                              clProperties.getProperty("tec.client.port"));
    int r;
    if ((r = rcc.showChooseCorpus()) == RemoteCorpusChooser.CANCEL_OPTION)
      return;
    if ( !firstRemoteFlag && r == RemoteCorpusChooser.SAME_IP)
      return;
    else
      firstRemoteFlag = false;

    String s = rcc.getServer();
    int p =  rcc.getPort();
    setRemoteCorpus(s,p);
    clProperties.setProperty("stand.alone","no");
  }

  public void setRemoteCorpus(String s, int p){
    SERVER = s;
    PORTNUM = p;
    setTitle(getBrowserName()+": index at "+SERVER+":"+PORTNUM);
    standAlone = false;
    TecClientRequest request = new TecClientRequest();
    request.setServerURL("http://"+SERVER);
    request.setServerPORT(PORTNUM);
    request.put("request","headerbaseurl");
    request.setServerProgramPath("/headerbaseurl");
    try {
      if (dictionary != null)
        dictionary.close();
      if (guiSelector != null)
        guiSelector.dispose();
      guiSelector = new GraphicalSubcorpusSelector(null, this);
      URL exturl = new URL(request.toString());
      HttpURLConnection exturlConnection = (HttpURLConnection) exturl.openConnection();
      //exturlConnection.setUseCaches(false);
      exturlConnection.setRequestMethod("GET");
      BufferedReader input = new
        BufferedReader(new
                       InputStreamReader(exturlConnection.getInputStream() ));
      HEDBAS = input.readLine();
      System.err.println("HEDBAS=>>>>"+HEDBAS);
      if (HEDBAS == null || HEDBAS.equals(""))
        HEDBAS = "http://"+SERVER+"/tec/headers";
      preferenceFrame.setHeaderBaseURL(HEDBAS);
      encoding = input.readLine();
      System.err.println("encoding=>>>>"+encoding);
      encoding = encoding == null? "---UTF8---" : encoding;
      clProperties.setProperty("tec.client.server",SERVER);
      clProperties.setProperty("tec.client.port",PORTNUM+"");
      clProperties.setProperty("stand.alone","no");
      clProperties.save();
      concordanceProducer = null;
    }
    catch(IOException e)
      {
        if (guiSelector != null)
          guiSelector.dispose();
        System.err.println("Exception: couldn't create URL input stream: "+e);
        HEDBAS = "http://"+SERVER+"/tec/headers";
        System.err.println("Setting URL to "+HEDBAS);
        preferenceFrame.setHeaderBaseURL(HEDBAS);
      }
  }
  
  public boolean workOffline() {
    int option;
    if ((clProperties.getProperty("stand.alone")).equals("yes"))
      return true;
    if (JOptionPane.showConfirmDialog(this,
                                      "Work offline (stand-alone corpus)?",
                                      "Work offline (stand-alone corpus)?",
                                      JOptionPane.YES_NO_OPTION) 
        == JOptionPane.YES_OPTION)
      {
        clProperties.setProperty("stand.alone","yes");
        return true;
      }
    else 
      {
        clProperties.setProperty("stand.alone","no");
        return false;
      }
  }

  public void initialCorpusSelection() {
    int option = -1;
    String sal = clProperties.getProperty("stand.alone");
    
    String lc = (sal != null && sal.equals("yes")) ?
      clProperties.getProperty("last.index.dir") :
      clProperties.getProperty("tec.client.server")+":"+clProperties.getProperty("tec.client.port");

    String [] opts = {"Use last corpus", "Choose new remote corpus", "Choose new local corpus"};
    JPanel pl = new JPanel();
    pl.setLayout(new BorderLayout());
    pl.add(new JLabel("The corpus you used last time was "+lc), BorderLayout.NORTH);
    pl.add(new JLabel("What would you like to do?"), BorderLayout.SOUTH);
    String op = 
      (String )JOptionPane.showInputDialog(this,
                                           pl,
                                           "Corpus selection",
                                           JOptionPane.QUESTION_MESSAGE,
                                           null,
                                           opts,
                                           "Use last corpus");
    for (int i = 0; i < opts.length; i++) {
      if (opts[i].equals(op)) {
        option = i;
        break;
      }
    }

    switch (option)
      {
      case 0:
         if (sal != null && sal.equals("yes"))
           setLocalCorpus(clProperties.getProperty("last.index.dir"));
         else
           setRemoteCorpus(clProperties.getProperty("tec.client.server"),
                           (new Integer(clProperties.getProperty("tec.client.port"))).intValue());
         break;
      case 1:
        chooseNewRemoteCorpus();
        break;
      case 2:
        chooseNewLocalCorpus();
        break;
      default:
        setTitle(getBrowserName()+": No index selected");
        break;
      }
    clProperties.save();
  }

  public Dictionary getDictionary(){
    return dictionary;
  }

  public static void main(String[] args) {
    try {
      splashScreen = new SplashScreen("Initialising. Please wait...", 20,
                                      "modnlp/tec/client/icons/modnlp-small.jpg");
      splashScreen.incProgress();
      final Browser f = new Browser(FRAME_WIDTH, FRAME_HEIGHT);
      if (f.clProperties.getProperty("browser.brand") != null) {
        f.setBrand(f.clProperties.getProperty("browser.brand"));
      }


      f.addWindowListener(new WindowAdapter() {
          public void windowClosing(WindowEvent e) {
            f.quit();
          }
        });
      
      if (args.length > 0 && args[0] != null)
        if (args[0].equals("-standalone") ) {
          f.standAlone = true;
          f.chooseNewLocalCorpus();
          //f.dictionary = new Dictionary();
        }
        else
          f.SERVER = args[0];
      else
        f.initialCorpusSelection();
      
      f.HEDBAS = "http://"+f.SERVER+"/tec/headers";
      if ( f.clProperties.getProperty("tec.client.headers") != null )
        f.HEDBAS = f.clProperties.getProperty("tec.client.headers");
      //f.show();
      if ( f.clProperties.getProperty("tec.client.port") != null )
        f.PORTNUM = new Integer(f.clProperties.getProperty("tec.client.port")).intValue();
      //f.setAdvSearchOptions();
      f.pack();
      splashScreen.dismiss();
      f.setVisible(true);
      f.noApplet = true;
      //System.err.println("SERVER="+f.SERVER+" PORT="+f.PORTNUM+"\nHEADERS="+f.HEDBAS);
    }
    catch (Exception e){
      System.err.println(e+" Usage: Browser HOSTNAME\n See also client.properties");
      e.printStackTrace();
      System.exit(1);
    }
  }
}
