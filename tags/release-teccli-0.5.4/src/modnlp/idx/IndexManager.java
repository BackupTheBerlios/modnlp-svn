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
package modnlp.idx;

import modnlp.idx.inverted.SubcorpusIndexer;
import modnlp.idx.inverted.TokeniserRegex;
import modnlp.idx.database.Dictionary;
import modnlp.idx.database.SubcorpusDirectory;
import modnlp.idx.database.DictProperties;
import modnlp.idx.database.AlreadyIndexedException;
import modnlp.idx.database.EmptyFileException;
import modnlp.idx.database.NotIndexedException;
import modnlp.idx.headers.HeaderDBManager;
import modnlp.dstruct.CorpusList;
import modnlp.dstruct.SubcorpusMap;
import modnlp.dstruct.TokenMap;
import modnlp.idx.gui.IndexManagerUI;
import modnlp.idx.gui.CorpusChooser;
import modnlp.idx.gui.HeaderURLChooser;

import com.sleepycat.je.DatabaseNotFoundException;

import java.io.File;
import java.util.Enumeration;

import javax.swing.JOptionPane;
import java.io.IOException;


/**
 *  GUI for corpus maintainance.
 *
 * @author  S Luz &#60;luzs@cs.tcd.ie&#62;
 * @version <font size=-1>$Id: IndexManager.java,v 1.2 2006/05/22 17:26:02 amaral Exp $</font>
 * @see  
*/
public class IndexManager {
  private IndexManagerProperties props;
  String ignElement;
  String subcElement;
  String subcAttribute;
  boolean indexHeaders;
  Dictionary dict = null;
  SubcorpusDirectory sbcd = null;
  HeaderDBManager hdbm = null;
  DictProperties dictProps;
  CorpusList clist;
  IndexManagerUI imui;
  IndexingThread indexingThread;
  DeindexingThread deindexingThread;
  boolean guiEnabled = true;
  boolean stop = false;
  boolean activeIndexing = false;
  boolean debug = false; // print debug info on stderr?

  // GUI-enabled version
  public IndexManager () {
    try {
      props =  new IndexManagerProperties(IndexManagerProperties.PROP_FNAME);
    }
    catch (IOException e){
      String msg =  "Error: Index properties not set.\n"+
        "Please set the values in idxmgr.properties.PLEASEEDIT\n"+
        "according to your corpus and rename it to idxmgr.properties.";
      JOptionPane.showMessageDialog(null, msg, "ERROR!", JOptionPane.ERROR_MESSAGE);
      System.exit(1);
    }
    setProperties();
    imui = new IndexManagerUI(this);
  }

  // command line version
  public IndexManager (String cdir) {
    dictProps =  new DictProperties(cdir);
    dict = new Dictionary(true,dictProps);
    sbcd = new SubcorpusDirectory(dict);
    indexHeaders =  dictProps.getProperty("index.headers").equalsIgnoreCase("true");
    if (indexHeaders){
      System.err.println("\n----- Opening Headers DB:  ------\n");
      try { hdbm = new HeaderDBManager(dict.getDictProps()); }
      catch(Exception e) 
        {System.err.println("\n----- Error opening Headers DB: "+e+" ------\n");}
    }
    ignElement = dictProps.getProperty("tokeniser.ignore.elements");
    subcElement = dictProps.getProperty("subcorpusindexer.element");
    subcAttribute = dictProps.getProperty("subcorpusindexer.attribute");
    // we can use imui to print our progress messages
    // but never actually make it visible
    imui = new IndexManagerUI(this);
    guiEnabled = false;
  }

  public void setProperties(){
    ignElement = props.getProperty("tokeniser.ignore.elements");
    subcElement = props.getProperty("subcorpusindexer.element");
    subcAttribute = props.getProperty("subcorpusindexer.attribute");
    indexHeaders =  props.getProperty("index.headers").equalsIgnoreCase("true");
  }

  public void setStop(boolean b){
    if (b){
      imui.print("-- Stop requested. ");
      if (activeIndexing)
        imui.print("I will finish indexing current file before stopping...\n");
      else
        imui.print("\n");
    }
    stop = b;
  }

  public void chooseNewCorpus(){
    CorpusChooser ncc = new CorpusChooser(props.getProperty("last.index.dir"));
    int r;
    while ((r = ncc.showChooseCorpus()) != CorpusChooser.APPROVE_OPTION &&
           r != CorpusChooser.CANCEL_OPTION)
      {
        JOptionPane.showMessageDialog(null, "Please choose a corpus index directory (folder)");      
      }
    if (r == CorpusChooser.CANCEL_OPTION)
      return;
    String cdir = ncc.getSelectedFile().toString();
    props.setProperty("last.index.dir", cdir);
    dictProps = new DictProperties(cdir);
    if (dict != null)
      dict.close();
    dict = new Dictionary(true,dictProps);
    sbcd = new SubcorpusDirectory(dict);
    if (indexHeaders){
      try { hdbm = new HeaderDBManager(dict.getDictProps()); }
      catch(Exception e) 
        {imui.print("\n----- Error opening Headers DB: "+e+" ------\n");}
    }
    imui.setCurrentDir(dictProps.getProperty("last.datafile.dir"));
    imui.setTitle("IndexManager: operating on index at "+cdir);
    imui.print("\n----- Selected corpus: "+cdir+" ------\n");
    imui.setCorpusListData(dict.getIndexedFileNames());
    // choose headers directory
    String hh = null;
    if ((hh = dictProps.getProperty("headers.home")) == null)  // see if dictProps already exists
      {
        while ( (r = ncc.showChooseDir("Choose the directory (folder) where the headers are stored"))
                != CorpusChooser.APPROVE_OPTION  ) 
          {
            JOptionPane.showMessageDialog(null, "Please choose a headers directory (folder)");      
          }
        hh = ncc.getSelectedFile().toString();
        dictProps.setProperty("headers.home", hh);
        dictProps.save();
      }
    if ((hh = dictProps.getProperty("headers.url")) == null)  // see if dictProps already exists
      {
        HeaderURLChooser huc = new HeaderURLChooser(imui, null);
        while ( (r = huc.showChooseURL()) == HeaderURLChooser.CANCEL_OPTION ) 
          JOptionPane.showMessageDialog(null, "Please choose a headers URL");
        dictProps.setProperty("headers.url", huc.getURL());
        dictProps.save();
      }
    dict.setVerbose(debug);
  }

  public void setDebug(boolean v){
    debug = v;
  }

  public void indexSelectedFiles (File[] files) {
    dictProps.setProperty("last.datafile.dir", files[0].getParent());
    indexingThread = new IndexingThread(new CorpusList(files));
    indexingThread.start();
  }

  public synchronized void indexSelectedFiles (String flist) {//throws InterruptedException{
    System.err.println("=="+flist);
    indexingThread = new IndexingThread(new CorpusList(flist));
    indexingThread.run();
    //indexingThread.join();
  }

  public void deindexSelectedFiles (Object[] files) {
    deindexingThread = new DeindexingThread(new CorpusList(files));
    deindexingThread.start();
  }

  public void deindexSelectedFiles (String flist) {
    deindexingThread = new DeindexingThread(new CorpusList(flist));
    deindexingThread.run();
  }


  public void exit(int c)
  {
    setStop(true);
    try {
      while ( activeIndexing )
        Thread.sleep(500);
    }
    catch (Exception e) {}
    if (dict != null)
      dict.close();
    if (props != null)
      props.save();
    if (dictProps != null)
      dictProps.save();
    System.exit(c);
  }

  public static void main(String[] args) {
    IndexManager im = null;

    try {
      if (args.length > 0 && args[0].equals("-h")){
        usage();
        System.exit(0);
      }
      if (args.length > 1)
        // assuming command-line execution: args[0] is directory index 
        // args[1] is corpuslist
        {
          im = new IndexManager(args[0]);
          if (args.length > 2 && args[2].equals("-v") ) 
            im.setDebug(true);
          if (args.length > 3 && args[3].equals("-d") ) 
            im.deindexSelectedFiles(args[1]);
          else
            im.indexSelectedFiles(args[1]);
          im.exit(0);
        }
      else {
        im = new IndexManager();
        im.chooseNewCorpus();
        if (im.dict == null)
          System.exit(0);
        im.imui.pack();
        im.imui.setVisible(true);
      }
    } // end try
    catch (Exception ex){
      System.err.println(ex+"\n-->"+args[0]+"--"+args[1]);
      ex.printStackTrace();
      if (im.dict != null)
        im.dict.close();
      usage();
      System.exit(1);
    }
  }

  public static void usage() {
    System.err.println("\nUSAGE:\n   modnlp.idx.IndexManager [indexdir filelist] [-v] [-d]\n");
    System.err.println("   With no parameters, starts GUI for index maintainance,");
    System.err.println("   otherwise run (de)indexer from the command line.\n");
    System.err.println("OPTIONS: ");
    System.err.println("\t indexdir: the directory where dictionary.properties lives");
    System.err.println("\t\t and the indices will be stored.");
    System.err.println("\t filelist: list of files to be indexed/deindexed.");
    System.err.println("\t -v: verbose output.");
    System.err.println("\t -d: deindex files in filelist.");
  }
  
  class IndexingThread extends Thread {
    CorpusList clist;
    public IndexingThread(CorpusList cl) {
      super("Indexing thread");
      clist = cl;
    }
    
    public void run() {
      activeIndexing = true;
      String fenc = dictProps.getProperty("file.encoding");
      //String hedhome = dictProps.getProperty("headers.home");
      long stt = (new java.util.Date()).getTime();
      for (Enumeration e = clist.elements(); e.hasMoreElements() ;) {
        if (stop) {
          stop = false;
          imui.print("----- Indexing aborted by user.");
          imui.enableChoice(true);
          activeIndexing = false;
          dict.sync();
          return;
        }
        String fname = (String)e.nextElement();
        try {
          TokeniserRegex tkr = new TokeniserRegex(new File(fname), 
                                                  fenc);
          tkr.setVerbose(debug);
          tkr.setIgnoredElements(ignElement);
          // if (debug) {
          imui.print("\n----- Processing: "+fname+" ------\n");
          //}
          if (dict.isIndexed(fname)){
            throw new AlreadyIndexedException(fname);
          }
          imui.print("-- Tokenising ...\n");
          tkr.tokenise();
          TokenMap tm = tkr.getTokenMap();
          //System.err.print(tm.toString());
          imui.print("-- Indexing ...\n");
          dict.setVerbose(debug);
          int fid = dict.addToDictionary(tm, fname);
          dict.sync();
          if (subcElement != null){
            imui.print("-- Indexing sub-corpus sections.\n");
            SubcorpusIndexer sir = new SubcorpusIndexer(tkr.getOriginalText(),subcElement, subcAttribute);
            sir.section();
            SubcorpusMap sm = sir.getSectionIndex();
            sbcd.add(sm, fname);
          }
          if (indexHeaders) {
            imui.print("-- Indexing Header file.\n");
            hdbm.add(dictProps.getHeaderAbsoluteFilename(fname), fid);
          } 
          imui.print("-- Done.\n");
          if (guiEnabled)
            imui.addIndexedFile(fname);
        }
        catch (EmptyFileException ex){
          imui.print("Warning: "+ex+"\n");
          imui.print("Ignoring this entry.\n");
        }
        catch (AlreadyIndexedException ex){
          imui.print("Warning: "+ex+"\n");
          imui.print("Ignoring this entry.\n");
        }
        catch (NotIndexedException ex){
          imui.print("Warning: "+ex+"\n");
          imui.print("Ignoring this entry.\n");
        }
        catch (org.xmldb.api.base.XMLDBException ex){
          imui.print("Warning (headers DB): "+ex+"\n");
          imui.print("Failed indexing header.\n");
        }
        catch (java.io.IOException ex){
          imui.print("IO Error processing file "+fname+": "+ex+"\n");
          imui.print("Indexing stopped.\n");
          activeIndexing = false;
          imui.enableChoice(true);
          return;
        }
        catch (DatabaseNotFoundException ex){
          imui.print("Warning: "+ex+"\n");
          imui.print("Ignoring this entry.\n");
        }
      } // end for 
      long tsec = ((new java.util.Date()).getTime()-stt)/1000;
      if (tsec > 60)
        imui.print("----- Indexing completed in "+(tsec/60)+ " minutes.");
      else
        imui.print("----- Indexing completed in "+(tsec)+ " seconds.");
      activeIndexing = false;
      imui.enableChoice(true);
      //notifyFinishedIndexing();
    } // end run()
    
  } // end IndexingThread


  class DeindexingThread extends Thread {
    CorpusList clist;
    public DeindexingThread(CorpusList cl) {
      super("Indexing thread");
      clist = cl;
    }
        
    public void run() {
      activeIndexing = true;
      for (Enumeration e = clist.elements(); e.hasMoreElements() ;) {
        if (stop) {
          stop = false;
          imui.print("----- De-indexing aborted by user.");
          imui.enableChoice(true);
          activeIndexing = false;
          dict.sync();
          return;
        }
        String fname = (String)e.nextElement();
        try {
          // if (debug) {
          imui.print("\n----- De-indexing: "+fname+" ------\n");
          if (subcElement != null){
            imui.print("----- Removing subcorpus sections ------\n");
            sbcd.remove(fname);
          }
          imui.print("----- Removing inverted index ------\n");
          dict.setVerbose(debug);
          dict.removeFromDictionary(fname);
          dict.sync();
          imui.print("-- Done.\n");
          imui.removeIndexedFile(fname);
        }
        catch (NotIndexedException ex){
          imui.print("Warning: "+ex+"\n");
          imui.print("Ignoring this entry.\n");
        }
      }
      activeIndexing = false;
      imui.enableChoice(true);
    } // end run()

  } // end DeindexingThread


}
