/**
 *   Copyright (c) 2008 S Luz <luzs@acm.org>. All Rights Reserved.
 *
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
package modnlp.tec.client;

import modnlp.Constants;
import modnlp.tec.client.gui.GraphicalSubcorpusSelector;
import modnlp.tec.client.gui.RemoteCorpusChooser;
import modnlp.tec.client.gui.PreferPanel;
import modnlp.tec.client.gui.SplashScreen;
import modnlp.tec.client.gui.BrowserFrame;
import modnlp.tec.client.gui.BrowserGUI;
import modnlp.tec.client.gui.PreferPanel;
import modnlp.tec.client.gui.*;
import modnlp.idx.database.Dictionary;
import modnlp.idx.database.DictProperties;
import modnlp.idx.headers.HeaderDBManager;
import modnlp.idx.gui.CorpusChooser;
import modnlp.util.IOUtil;
import java.util.Comparator;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.StringTokenizer;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import javax.swing.JOptionPane;
import java.net.HttpURLConnection;
import java.net.URL;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import javax.swing.JLabel;
import java.util.Vector;

/**
 *  Display concordances etc: interact with
 *  <a href="../server/index.html">TEC Servers</a> or work
 *  stand-alone accessing the corpus index through <a
 *  href="../../idx/database/Dictionary.html">Dictionary.java</a>
 *
 * 
 * @author  S Luz &#60;luzs@cs.tcd.ie&#62;
 * @version <font size=-1>$Id: $</font>
 * @see  
*/
public class Browser
  implements ConcordanceBrowser //, ConcordanceDisplayListener 
{

  // constants
  public static final String RELEASE = "0.7.1";
  public static final String REVISION = "$Revision: 1.9 $";
  String BRANDNAME = "MODNLP/TEC";
  private static final String PLGLIST = "teclipluginlist.txt";
  private static final boolean debug = true;

  // properties, state
  private boolean standAlone = true;
  private boolean commandLineServer  = false;
  private boolean advConcFlag = false;
  private boolean firstRemoteFlag = true;
  /** Deafult location of TecServer  */
  private String remoteServer;
  /** Default port */
  private int remotePort = 1240;
  /** Deafult URL where to find header files */
  private String headerBaseURL;
  private String keywordString;
  private String encoding = null;
  private String xquerywhere = null;
  private int language = Constants.LANG_EN;

  // GUI
  private SplashScreen splashScreen;
  private BrowserFrame browserFrame;
  private PreferPanel preferenceFrame;
  private GraphicalSubcorpusSelector guiSubcorpusSelector = null;
  private ClientProperties clProperties;

  // DBs
  private Dictionary dictionary = null;
  private HeaderDBManager hdbmanager = null;

  // threads
  private SortThread sortThread = null;
  private ConcordanceThread concThread = null;
  private ConcordanceProducer concordanceProducer = null;

  // text data 
  private ConcordanceVector concVector = new ConcordanceVector();

  public Browser (String sp) {
    standAlone = false;
    commandLineServer = true;
    clProperties = new ClientProperties();
    preferenceFrame = new PreferPanel(this);
    browserFrame = new BrowserFrame(0, 0, this);
    preferenceFrame.addDefaultChangeListener(browserFrame);
    String serv = sp.substring(0,sp.indexOf(':'));
    int port = (new Integer(sp.substring(sp.indexOf(':')+1))).intValue();
    setRemoteCorpus(serv,port);
    init();
  }

  public Browser (boolean sa) {
    standAlone = sa;
    commandLineServer = false;
    clProperties = new ClientProperties();
    preferenceFrame = new PreferPanel(this);
    browserFrame = new BrowserFrame(0, 0, this);
    preferenceFrame.addDefaultChangeListener(browserFrame);
    init();
  }

  private final void init(){
    if (clProperties.getProperty("browser.brand") != null) {
      setBrand(clProperties.getProperty("browser.brand"));
    }

    browserFrame.addWindowListener(new WindowAdapter() {
        public void windowClosing(WindowEvent e) {
          quit();
        }
      });
    if ( !commandLineServer )
      if ( isStandAlone() )
        chooseNewLocalCorpus();
      else
        initialCorpusSelection();
    
    splashScreen = new SplashScreen("Initialising. Please wait...", 20,
                                    "modnlp/tec/client/icons/modnlp-small.jpg");
    incProgress();    

    browserFrame.initGUI();
    incProgress();

    loadPlugins();
    incProgress();
      
    //headerBaseURL = "http://"+remoteServer+"/tec/headers";
    //if ( clProperties.getProperty("tec.client.headers") != null )
    //  headerBaseURL = clProperties.getProperty("tec.client.headers");
    //if ( clProperties.getProperty("tec.client.port") != null )
    //  remotePort = new Integer(clProperties.getProperty("tec.client.port")).intValue();
    //f.setAdvSearchOptions();
    browserFrame.pack();
    incProgress();
    splashScreen.dismiss();
    browserFrame.setVisible(true);
  }

  public void quit(){
    System.err.println("BYE...");

    // stop all threada
    if (concThread != null)
      concThread.stop();
    if (sortThread != null)
      sortThread.stop();

    if (guiSubcorpusSelector != null)
      stopSubCorpusSelectorGUI();

    // close all DBs
    if (dictionary != null)
      dictionary.close();
    if (hdbmanager != null)
      hdbmanager.close();
    System.exit(0);
  }


  public ConcordanceThread getConcordanceThread(){
    return concThread;
  }

  public ConcordanceVector getConcordanceVector(){
    return concVector;
  }

  public String getKeywordString(){
    return keywordString;
  }

  public void setStandAlone(boolean b){
    standAlone = b;
  }

  public boolean isStandAlone() {
    return standAlone;
  }

  public void incProgress(){
    splashScreen.incProgress();
  }

  public void dismissProgress(){
    splashScreen.incProgress();
  }

  private void loadPlugins () {
    splashScreen.setMessage("Loading plugins...");
    splashScreen.incProgress();
    ClassLoader cl = this.getClass().getClassLoader();
    BufferedReader in
      = new BufferedReader(new 
                           InputStreamReader(cl.getResourceAsStream(PLGLIST)));
    String plg = null;
    try {
      while ( (plg = in.readLine() ) != null ){
        try {
          System.err.println("Loading: "+plg);
          StringTokenizer st = new StringTokenizer(plg, ":");
          // first token: class name (ignore for now)
          final Plugin tp = (Plugin)IOUtil.loadPlugin(st.nextToken(),cl);
          tp.setParent(this);
          browserFrame.addPluginMenuItem(tp, st.nextToken());
          splashScreen.incProgress();
        }
        catch (ClassNotFoundException e) {
          System.err.println("Warning (Browser): error loading plugin: "+e);
          e.printStackTrace(System.err);
        }
      }
    }
    catch (Exception e) {
      e.printStackTrace(System.err);
    }
  }

  public void requestConcordance(String query){
    keywordString = query;
    if (sortThread != null)
      sortThread.stop();
    browserFrame.labelMessage("Building concordance list. Please wait...");
    browserFrame.updateStatusLabelScroll("");
    TecClientRequest request = new TecClientRequest();
    request.put("keyword",keywordString);
    request.put("context",preferenceFrame.getContextSize());
    request.put("sgml",preferenceFrame.getSGMLFlag());
    String casestate = browserFrame.getCase()? "sensitive" : "insensitive";
    request.put("case",casestate);
    request.put("request","concord");
    if ( subCorpusSelected() )
      request.put("xquerywhere",xquerywhere);
    if ( (concThread != null) ) {
      concThread.stop();
    }
    concVector.clear();
    concVector.setHalfConcordance(request.getContextSize());
      concVector.setSortContextHorizon(0);
    //concList.removeAll();
    //concList.reset();
    if (standAlone) {
      concThread = 
        new ConcordanceThread(concVector, 
                              concordanceProducer.getBufferedReader(), 
                              request);
      concThread.start();
      concordanceProducer.setRequest(request);
      concordanceProducer.start();
    }
    else {
      request.setServerURL("http://"+remoteServer);
      request.setServerPORT(remotePort);
      request.setServerProgramPath("/concordancer");
      concThread = new ConcordanceThread(concVector, request);
      concThread.setEncoding(encoding);
      concThread.start();
    }
    //--??--concThread.addConcordanceDisplayListener(concVector);
    //SwingUtilities.invokeLater(concThread);
    //concList = new ListDisplay(this, concThread.conc);
    //currentIndex = 0;
    concThread.addConcordanceDisplayListener(browserFrame);
    browserFrame.progressBarUnknownStart("Searching... ");
  }


  public void showSubcorpusSelector(){
    guiSubcorpusSelector.activate();
  }

  public void startSorting(int horizon, boolean sortleft){
    if ( concThread.atWork()  ) {
      if ( ! browserFrame.interruptDownloading() )
        return;
      concThread.stop();
    }
    if (sortThread != null)
      sortThread.stop();
    int sortContextHorizon = browserFrame.getSortLeftCtxHorizon();
    if (sortleft) 
      concVector.setSortContextHorizon(0-sortContextHorizon);
    else {
      sortContextHorizon = browserFrame.getSortRightCtxHorizon();
      concVector.setSortContextHorizon(sortContextHorizon);
    }

    Comparator cprer = sortleft ?
      new LeftComparer(sortContextHorizon, 
                       preferenceFrame.maxContext/2, 
                       browserFrame.getPunctuation()) :
      new RightComparer(sortContextHorizon, 
                        preferenceFrame.maxContext/2, 
                        browserFrame.getPunctuation());
    sortThread = new SortThread(concVector, cprer);
    
    sortThread.addConcordanceDisplayListener(browserFrame);
    sortThread.start();

    browserFrame.progressBarUnknownStart("Sorting... ");
    browserFrame.labelMessage("Sorting with context horizon "
                              +sortContextHorizon+(sortleft?" (left)":" (right)")); 
  }

  public void showPreferencesEditor(){
    preferenceFrame.setSize(400,300);
    preferenceFrame.show(); 
  }

  public void showHelp()
  {
    HelpBrowser hb = new HelpBrowser();
    if ( hb.ok() )
      hb.show();
  }
  public void showAbout()
  {
    HelpBrowser hb = new HelpBrowser("modnlp/tec/client/help/about.html", "About...");
  if ( hb.ok() )
    hb.show();
  }

  public void showErrorMessage(String e){
    JOptionPane.showMessageDialog(null, e, "ERROR", JOptionPane.ERROR_MESSAGE);
  }

  /** Show extract of text identified by position <code>sel</code>
   * in the current <code>ConcordanceVector</code>
   *  @see ConcordanceVector
   */
  public void showExtract(ConcordanceObject sel)
  {
    String filename = sel.filename;
    long filepos = sel.filepos;
    if ( filepos < 0 ) {
      System.err.println("remoteServer ERROR: invalid position "+filepos);
    }
    else {
      TecClientRequest request = new TecClientRequest();
      request.put("filename",filename);
      request.put("context",preferenceFrame.getExtractContextSize());
      request.put("sgml",preferenceFrame.getSGMLFlag());
      request.put("position",filepos);
      request.put("request","extract");
      request.put("keyword",keywordString);
      request.setServerURL("http://"+remoteServer);
      request.setServerPORT(remotePort);
      request.setServerProgramPath("/extractor");
      //if (concThread != null && concThread.isAlive() ){
      //concThread.stop();
      //}
      ContextClient tecClient = null;
      if (standAlone)
        tecClient = new ContextClient(request, dictionary);
      else
        tecClient = new ContextClient(request);
      tecClient.setVisible(true); // a bug in jdk 1.5 won't let it
                                  // show the frame unless it is
                                  // resized *after* it is made
                                  // visible, hence the call to start() below.
      tecClient.start();
      preferenceFrame.addDefaultChangeListener(tecClient);
    }
  }

  /** Show header file of text identified by position <code>sel</code>
   *  in the current <code>ConcordanceVector</code>
   *  @see ConcordanceVector
   */
  public void showHeader(ConcordanceObject sel)
  {
    String filename = sel.sfilename;
    String headerName = 
      filename.substring(0,filename.lastIndexOf('.'))+".hed";
    //int p = headerName.lastIndexOf(java.io.File.separator);
    //headerName = p < 0? headerName : headerName.substring(p);
    showHeader(headerName);
  }

 public void showHeader(String headerName)
  {
    int windowHeight = 600;
    int windowWidth = 500;
    String sep = java.io.File.separator;
    if (headerBaseURL.startsWith("http://") || 
        headerBaseURL.startsWith("https://") || 
        headerBaseURL.startsWith("file://"))
      sep = "/";
    String tmp;
    String img = headerBaseURL+sep+headerName.substring(0,headerName.indexOf('.'))+".jpg";
    
    StringBuffer content = new StringBuffer();
    System.err.println("URL--:"+headerBaseURL);
    //HeaderReader header = new HeaderReader(headerBaseURL+headerName);
    //HeaderXMLHandler parser =  new HeaderXMLHandler();
    try {
      InputStream is = null;
      URL headerURL = null;

      if (standAlone) {
        is = new FileInputStream(headerBaseURL+
                                                 sep+
                                                 headerName);
        img = "file://"+img;
      }
      else {
        headerURL = new URL(headerBaseURL+sep+headerName);
        is = headerURL.openConnection().getInputStream();
      }
      //InputSource source = new InputSource(in);
      //source.setEncoding(encoding);
      if (preferenceFrame.isShowingSGMLFlag()){
        BufferedReader in = 
          new BufferedReader(new InputStreamReader(is,"UTF-8"));
        while ( (tmp = in.readLine()) != null )
          content.append(tmp+"\n");
      }
      else {
        HeaderXMLHandler parser =  new HeaderXMLHandler();
        parser.parse(is);

        content = new StringBuffer("<html><img src='"+img+"' height=183 width=128 alt='Book Cover'><pre>"+parser.getContent()+"</pre></html>");
        System.err.println(content+"");
      }
    }
    catch (Exception e) {
      System.err.println("Error retrieving metadata: "+e);
      content.append("\nError retrieving metadata: "+e);
      e.printStackTrace(System.err);
    }
    // HeaderClass header = new HeaderClass(filename);
    FullTextWindow window =  new FullTextWindow(headerName,
                                                content);
    preferenceFrame.addDefaultChangeListener(window);
    //System.err.println(content);
    window.setVisible(true); // a bug in jdk 1.5 won't let it show the
                             // frame unless it is resized *after* it
                             // is made visible
    window.setSize(windowWidth, windowHeight);
  }
  
  public void downloadConcordance(Download dlf) throws java.io.IOException {
    dlf.dumpConcordance(getConcordanceVector());
  }

  public int getPreferredFontSize (){
    return preferenceFrame.getFontSize();
  }

  public void setAdvConcFlag (boolean f){
    browserFrame.clickAdvConcFlag(f);
    advConcFlag = f;
  }

  public boolean isSubCorpusSelectionON (){
    return advConcFlag;
  }
  
  public boolean subCorpusSelected() {
    if (advConcFlag && xquerywhere != null )
      return true;
    else
      return false;
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

  public int getLanguage(){
    return language;
  }

  public void setLocalCorpus (String cdir) {
    DictProperties dictProps = new DictProperties(cdir);
    if (dictionary != null)
      dictionary.close();
    dictionary = new Dictionary(false,dictProps);
    clProperties.setProperty("last.index.dir", cdir);
    standAlone = true;
    clProperties.setProperty("stand.alone","yes");
    browserFrame.setTitle(getBrowserName()+": index at "+cdir);
    dictionary.setVerbose(debug);
    setLocalHeadersDirectory(dictProps);
    encoding = dictProps.getProperty("file.encoding");
    language = dictProps.getLanguage();
    if (guiSubcorpusSelector != null)
      stopSubCorpusSelectorGUI();
    guiSubcorpusSelector = null;
    System.gc();
    try {
      hdbmanager = new HeaderDBManager(dictProps);
    }
    catch (Exception e) {
      JOptionPane.showMessageDialog(browserFrame, "Header DB error: Subcorpus selection disabled",
                                    "ERROR", JOptionPane.ERROR_MESSAGE);
      System.err.println("Browser: error opening header DB: " + e);
      e.printStackTrace(System.err);
    }
    concVector.setLanguage(language);
    guiSubcorpusSelector = new GraphicalSubcorpusSelector(this);
    concordanceProducer = new ConcordanceProducer(dictionary);
  }

  private void setLocalHeadersDirectory(DictProperties dictProps){
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
    headerBaseURL = hh;
    clProperties.setProperty("tec.client.headers", headerBaseURL);
    preferenceFrame.setHeaderBaseURL(headerBaseURL);
  }

  public void chooseNewRemoteCorpus () {
    RemoteCorpusChooser rcc = 
      new RemoteCorpusChooser(browserFrame, clProperties.getProperty("tec.client.server")+":"+
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
  }

  public void setRemoteCorpus(String s, int p){
    remoteServer = s;
    remotePort = p;
    standAlone = false;
    TecClientRequest request = new TecClientRequest();
    request.setServerURL("http://"+remoteServer);
    request.setServerPORT(remotePort);
    request.put("request","headerbaseurl");
    request.setServerProgramPath("/headerbaseurl");
    try {
      if (dictionary != null)
        dictionary.close();
      if (guiSubcorpusSelector != null)
        stopSubCorpusSelectorGUI();
      guiSubcorpusSelector = new GraphicalSubcorpusSelector(this);
      URL exturl = new URL(request.toString());
      HttpURLConnection exturlConnection = (HttpURLConnection) exturl.openConnection();
      //exturlConnection.setUseCaches(false);
      exturlConnection.setRequestMethod("GET");
      BufferedReader input = new
        BufferedReader(new
                       InputStreamReader(exturlConnection.getInputStream() ));
      headerBaseURL = input.readLine();
      System.err.println("headerBaseURL=>>>>"+headerBaseURL);
      if (headerBaseURL == null || headerBaseURL.equals(""))
        headerBaseURL = "http://"+remoteServer+"/tec/headers";
      preferenceFrame.setHeaderBaseURL(headerBaseURL);
      encoding = input.readLine();
      String lg = input.readLine();
      if (lg == null || lg.length() == 0) 
        language = Constants.LANG_EN;
      else
        language = (new Integer(lg)).intValue();
      concVector.setLanguage(language);
      System.err.println("language=>>>>"+language);
      System.err.println("encoding=>>>>"+encoding);
      encoding = encoding == null? "---UTF8---" : encoding;
      clProperties.setProperty("tec.client.server",remoteServer);
      clProperties.setProperty("tec.client.port",remotePort+"");
      clProperties.setProperty("stand.alone","no");
      clProperties.save();
      concordanceProducer = null;
    }
    catch(IOException e)
      {
        if (guiSubcorpusSelector != null)
          stopSubCorpusSelectorGUI();
        showErrorMessage("Error: couldn't create URL input stream: "+e);
        System.err.println("Exception: couldn't create URL input stream: "+e);
        headerBaseURL = "http://"+remoteServer+"/tec/headers";
        System.err.println("Setting URL to "+headerBaseURL);
        language = Constants.LANG_EN;
        preferenceFrame.setHeaderBaseURL(headerBaseURL);
      }
    if (guiSubcorpusSelector.hasNetworkError())
      showErrorMessage("Error: couldn't select new Internet corpus.");
    else
      browserFrame.setTitle(getBrowserName()+": index at "+remoteServer+":"+remotePort);

  }
  
  public boolean workOffline() {
    int option;
    if ((clProperties.getProperty("stand.alone")).equals("yes"))
      return true;
    if (JOptionPane.showConfirmDialog(browserFrame,
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
      (String )JOptionPane.showInputDialog(browserFrame,
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
        browserFrame.setTitle(getBrowserName()+": No index selected");
        break;
      }
    clProperties.save();
  }

  private void stopSubCorpusSelectorGUI(){
    if (hdbmanager != null){
      hdbmanager.finalize();
      hdbmanager = null;
    }
    guiSubcorpusSelector.dispose();
  }

  public final void setXQueryWhere(String w){
    xquerywhere = w;
  }
  
  public final String getXQueryWhere(){
    return xquerywhere;
  }

  public final int getRemotePort(){
    return remotePort;
  }

  public final String getRemoteServer(){
    return remoteServer;
  }

  public final Dictionary getDictionary(){
    return dictionary;
  }

  public final ClientProperties getClientProperties(){
    return clProperties;
  }

  public final HeaderDBManager getHeaderDBManager(){
    return hdbmanager;
  }

  public final int getExpectedNoOfConcordances(){
    return concThread.getNoFound();
  }

  public final int getNoOfConcordancesReadSoFar(){
    return concThread.getNoRead();
  }

  public final boolean gotResponseFromServer(){
    return concThread.getServerResponded();
  }

  public final boolean isReceivingFromServer(){
    return concThread.atWork();
  }

  public final boolean isCaseSensitive(){
    return browserFrame.getCase();
  }

  public final boolean isSorting(){
    return (sortThread.atWork());
  }

  public final void setBrand(String b){
    BRANDNAME = b;
  }

  public final String getBrand(){
    return BRANDNAME;
  }

  // ok
  public final String getRelease (){
    return RELEASE;
  }
  
  // ok
  public final String getVersion (){
    return REVISION.substring(11,REVISION.lastIndexOf("$"));
  }
  
  // ok
  public String getBrowserName (){
    return getBrand()+" Concordance Browser (v. "+getRelease()+")";
  }


  public BrowserGUI getBrowserGUI(){
    return browserFrame;
  }


  public static void main(String[] args) {
    try {
      final Browser b;
      if (args.length > 0)
        if ( args[0].equals("-standalone") )
          b = new Browser(true);
        else
          b = new Browser(args[0]);
      else
        b = new Browser(false);
      b.browserFrame.setVisible(true);
    }
    catch (Exception e){
      System.err.println(e+" Usage: Browser HOSTNAME:PORTNUM\n See also client.properties");
      e.printStackTrace();
      System.exit(1);
    }
  }

}

