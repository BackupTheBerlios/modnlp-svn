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
//import java.applet.*;

import modnlp.idx.database.Dictionary;
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

  public static final String RELEASE = "0.4";
  public static final String REVISION = "$Revision: 1.9 $";
  /* The next 2 thread objects will eventually become plug-in's */
  public ConcordanceThread concThread = null;
  private SortThread sortThread = null;

  private Dictionary dictionary = null;
  /** Deafult location of TecServer (a valid DNS where the server lives) */
  public String SERVER;
	/** Default port */
  public int PORTNUM = 1240;
  /** Deafult URL where to find header files */
  public String HEDBAS;
  // plugin list
  private static final String PLGLIST = "modnlp/tec/client/pluginlist.txt";
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

  private final static int WIDTH = 1000;
  private final static int HEIGHT = 700;
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
  private JMenuItem dldButton = new JMenuItem("Save concordances...");  
	private JMenuItem quitButton = new JMenuItem(QUITBUT);

  private JMenu  prefMenu = new JMenu("Options");
  private JCheckBoxMenuItem caseCheckBox = 
    new JCheckBoxMenuItem("Case sensitive");
  private JMenuItem advConcButton = 
    new JMenuItem("Select Sub-corpus...");
  private JCheckBoxMenuItem advConcFlag = 
    new JCheckBoxMenuItem("Activate sub-corpus selection");
  private JMenuItem prefButton = new JMenuItem("Preferences...");

  private JMenu  pluginMenu = new JMenu("Plugins");

  private JMenu  helpMenu = new JMenu("Help");
  private JMenuItem helpButton = new JMenuItem("Contents");
  private JMenuItem aboutButton = new JMenuItem("About TEC...");    

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
  private Connection conn;
  private int srt_i = 0;
  private AdvConcSearch advSearchFrame;
  private String goSearch;

  /** Create a TEC Window Object
   * @param width   window width
   * @param height   window height
   */
  public Browser(int width, int height){

    super("TEC Concordance Browser (v. "+getRelease()+")");
    Container contentPane = getContentPane();
    setSize(width,height);
    setFont(new Font("Helvetica",Font.PLAIN, 12));
    
    
    splashScreen.incProgress();
    // this will eventually be moved into a plugin
    advSearchFrame = new AdvConcSearch(this);
    advSearchFrame.setSize(700,500);
    
    splashScreen.incProgress();

    // Lay out menu bar. 
    JMenuBar menuBar;
    
    menuBar = new JMenuBar();
    setJMenuBar(menuBar);
    
    fileMenu.add(dldButton);
    fileMenu.addSeparator();
    fileMenu.add(quitButton);
    
    prefMenu.add(caseCheckBox);
    prefMenu.add(advConcButton);
    prefMenu.add(advConcFlag);
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
          if ( //concThread.ctRead >= concThread.noFound ||
               //concThread.ctRead >= ConcArray.arraymax ||
              concThread.serverResponded ||
               ! concThread.atWork() 
               )
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

    // -------- plugins disabled for the time being
    //splashScreen.setMessage("Loading plugins...");
    //splashScreen.incProgress();
     pluginMenu.setEnabled(false);
     //loadPluginMenu();

    
    // set up even listening
    concButton.addActionListener( this );
    quitButton.addActionListener( this );
    prefButton.addActionListener( this );
    advConcButton.addActionListener( this );
    advConcFlag.addActionListener( this );
    keyword.addActionListener( this );
    
    caseCheckBox.addActionListener( this );
    caseCheckBox.setState(false);
    advConcFlag.setState(false);
    
    stlButton.addActionListener(this);
    strButton.addActionListener(this);
    extractButton.addActionListener(this);
    headerButton.addActionListener(this);
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

  /*
  private void loadPluginMenu () {
    try {
      ClassLoader cl = this.getClass().getClassLoader();
      BufferedReader in
        = new BufferedReader(new 
                             InputStreamReader(cl.getResourceAsStream(PLGLIST)));
      String plg = null;
      while ( (plg = in.readLine() ) != null ){
        StringTokenizer st = new StringTokenizer(plg, ":");
        // first token: class name (ignore for now)
        st.nextToken();
        JMenuItem pmi = new JMenuItem(st.nextToken());
        pluginMenu.add(pmi);
        // need to create an object via ObjectFactory 
        // take a look at ImageJ to see how this is done
        // .addActionListener(st.nextToken();
        final Browser dad = this;
        pmi.addActionListener(
                              new ActionListener(){
                                public void actionPerformed(ActionEvent event)
                                {
                                  new ie.tcd.cs.plugin.FqListBrowser(dad);
                                }
                              });
      }
    }
    catch (Exception e) {
      e.printStackTrace(System.err);
    }
  }
  */

  public void setAdvSearchOptions (){

    splashScreen.setMessage("Setting options...");
    splashScreen.incProgress();
    advSearchFrame.setANames(getSQLMetadata("AuthorNames"));
    splashScreen.incProgress();
    advSearchFrame.setAGends(getSQLMetadata("AuthorGenders"));
    splashScreen.incProgress();
    advSearchFrame.setANats(getSQLMetadata("AuthorNationalities"));
    splashScreen.incProgress();
    advSearchFrame.setASexOr(getSQLMetadata("AuthorSexualOrientation"));

    advSearchFrame.setTNames(getSQLMetadata("TranslatorNames"));
    splashScreen.incProgress();
    advSearchFrame.setTGends(getSQLMetadata("TranslatorGenders"));
    splashScreen.incProgress();
    advSearchFrame.setTNats(getSQLMetadata("TranslatorNationalities"));
    splashScreen.incProgress();
    advSearchFrame.setTSexOr(getSQLMetadata("TranslatorSexualOrientation"));
    splashScreen.incProgress();

    advSearchFrame.setTLans(getSQLMetadata("SourceLanguage"));
    splashScreen.incProgress();
    advSearchFrame.setDocuments(getSQLMetadata("Filenames"));
    splashScreen.incProgress();

    advSearchFrame.getMenu();
    splashScreen.incProgress();

  }

  public void showHelp() {
    
  }
  public static String getRelease (){
    return RELEASE;
  }
  
  public static String getVersion (){
    return REVISION.substring(11,REVISION.lastIndexOf("$"));
  }
  
  /*public void getADSMenu(AdvConcSearch acs)
    {
    acs.setMenus();
    }*/
  
  public void actionPerformed(ActionEvent evt)
  {
    String arg = evt.getActionCommand();
		//System.out.println("ARG:"+arg);
    if(evt.getSource() instanceof JTextField){
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
          //advSearchFrame.setSize( 1000, 250 );
          advSearchFrame.show();
        }
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
          HelpBrowser hb = new HelpBrowser("modnlp/tec/client/help/about.html", "About TEC");
          if ( hb.ok() )
            hb.show();
        }
      else
        if (  evt.getSource() == quitButton ){
          if ( noApplet )
						System.exit(0);
				}
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
    sqlString = advSearchFrame.getSQLQuery();
    TecClientRequest request = new TecClientRequest();
    request.put("keyword",keywordString);
    // --------------------------- new WordQuery(q,dictionary,true);
    // --- SL: this (resetting the constraints each time) is ridiculous! (and
    // even if it weren't, boolean values should be assigned to
    // Boolean data types, not Strings! 
    // TO DO: check if getting rid of goSearch doesn't break anything; get rid of it.)
    // SL: Done.
    if( advConcFlag.isSelected() )
      {
        //goSearch = "true";
        request.put("sqlquery", sqlString);
        request.put("isSQL", "true");
        //System.out.println(sqlString);
        //advSearchFrame.setSQL("false");
      }
    else    // if(ty.equals("false"))
      {
        //goSearch = "false";
        request.put("isSQL", "false");
      }
    request.put("context",preferenceFrame.getContextSize());
    request.put("sgml",preferenceFrame.getSGMLFlag());
    String casestate = caseCheckBox.getState()? "sensitive" : "insensitive";
    request.put("case",casestate);
    request.put("request","concord");
    if ( (concThread != null) ) {
      concThread.stop();
    }
    concList.removeAll();
    //concList.reset();
    if (standAlone) {
      ConcordanceProducer cp = new ConcordanceProducer(dictionary, request);
      concThread = new ConcordanceThread(this, cp.getBufferedReader(), request);
      concThread.start();
      cp.start();
    }
    else {
      request.setServerURL("http://"+SERVER);
      request.setServerPORT(PORTNUM);
      request.setServerProgramPath("/concordancer");
      concThread = new ConcordanceThread(this, request);
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
    catch (sun.applet.AppletSecurityException e){
      labelMessage("Error caught: server not responding.");
      concThread.stop();
    }
  }

	public void updateStatusLabel (String msg){
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


  /** Show header file of text identified by
   *  position <code>sel</code>
   * in the current <code>ConcArray</code>
   *  @see #array
   *  @see ConcArray
   */
  public void showHeader(ConcordanceObject sel)
  {
    String filename = sel.filename;
    String headerName =
      filename.substring(0,filename.lastIndexOf("."))+".hed";
    showHeader(headerName);
  }

  public void showHeader(String headerName)
  {
    int windowHeight = 400;
    int windowWidth = 350;
    String tmp;
    //System.out.println("URL--:"+HEDBAS+headerName);
    //HeaderReader header = new HeaderReader(HEDBAS+headerName);
    HeaderXMLHandler parser =  new HeaderXMLHandler();
    try {
      URL headerURL = null;
      if (standAlone)
        headerURL = new URL("file://"+dictionary.getCorpusDir()+"/"+headerName);
      else
        headerURL = new URL(HEDBAS+"/"+headerName);
			BufferedReader in
				= new BufferedReader(new InputStreamReader(headerURL.
                                                   openConnection().
                                                   getInputStream()));
      InputSource source = new InputSource(in);
      source.setEncoding("ISO-8859-1");
      if ( preferenceFrame.stSGML.equals("no") ) {
        parser.parse(source);
      }
      else
        while ( (tmp = in.readLine()) != null )
          parser.content.append(tmp+"\n");
    }
    catch (Exception e) {
      System.err.println("Error retrieving metadata: "+e);
    }
    // HeaderClass header = new HeaderClass(filename);
    FullTextWindow window =  new FullTextWindow(headerName,
                                                "<pre>"+parser.content+"</pre>");
		preferenceFrame.addDefaultChangeListener(window);
    window.setSize(windowWidth, windowHeight);
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
    if ( advConcFlag.isSelected() != f ) {
      advConcFlag.doClick();
      advConcButton.setSelected(f);
    }
  }
  
  public static void main(String[] args) {
    try {
      //System.err.println("entered main");
			ClientProperties p = new ClientProperties();
      splashScreen = new SplashScreen("Initialising TEC. Please wait...", 20);
      splashScreen.incProgress();
      Browser f = new Browser(WIDTH, HEIGHT);
			f.addWindowListener(new WindowAdapter() {
					public void windowClosing(WindowEvent e) {
						System.exit(0);
					}
        });
      if (args.length > 0 && args[0] != null)
        if (args[0].equals("-standalone")) {
          f.standAlone = true;
          f.dictionary = new Dictionary();
          String u = f.dictionary.getCorpusDir();
          u = u.substring(0,u.lastIndexOf('/', u.length()-1))+"/headers/";
          f.preferenceFrame.setHeaderBaseURL(u);
        }
        else
          f.SERVER = args[0];
      else if ( p.getProperty("tec.client.server") != null )
				f.SERVER = p.getProperty("tec.client.server");
      f.HEDBAS = "http://"+f.SERVER+"/tec/headers";
      if ( p.getProperty("tec.client.headers") != null )
				f.HEDBAS = p.getProperty("tec.client.headers");
      //f.show();
			if ( p.getProperty("tec.client.port") != null )
				f.PORTNUM = new Integer(p.getProperty("tec.client.port")).intValue();
      f.clProperties = p;
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
