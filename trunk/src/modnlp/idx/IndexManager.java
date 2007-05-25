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
import modnlp.idx.inverted.TokeniserRegex;
import modnlp.idx.database.Dictionary;
import modnlp.idx.database.DictProperties;
import modnlp.idx.database.AlreadyIndexedException;
import modnlp.idx.database.EmptyFileException;
import modnlp.idx.database.NotIndexedException;
import modnlp.dstruct.CorpusList;
import modnlp.dstruct.TokenMap;
import modnlp.idx.gui.IndexManagerUI;
import modnlp.idx.gui.CorpusChooser;
import modnlp.idx.gui.HeaderURLChooser;

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
  private IndexManagerProperties props = new IndexManagerProperties();
  Dictionary dict = null;
  DictProperties dictProps;
  CorpusList clist;
  IndexManagerUI imui;
  IndexingThread indexingThread;
  DeindexingThread deindexingThread;
  boolean stop = false;
  boolean activeIndexing = false;
  boolean debug = false; // print debug info on stderr?
  public IndexManager () {
    imui = new IndexManagerUI(this);
  }

  public void setStop(boolean b){
    imui.print("-- Stop requested. I will finish indexing current file before stopping...\n");
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

  public void indexSelectedFiles (File[] files) {
    dictProps.setProperty("last.datafile.dir", files[0].getParent());
    indexingThread = new IndexingThread(new CorpusList(files));
    indexingThread.start();
  }

  public void deindexSelectedFiles (Object[] files) {
    deindexingThread = new DeindexingThread(new CorpusList(files));
    deindexingThread.start();
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
    IndexManager im = new IndexManager();
    try {
      im.chooseNewCorpus();
      if (im.dict == null)
        System.exit(0);
      im.imui.pack();
      im.imui.setVisible(true);
    } // end try
    catch (Exception ex){
      System.err.println(ex);
      ex.printStackTrace();
      if (im.dict != null)
        im.dict.close();
      usage();
    }
  }

  public static void usage() {
    System.err.println("\nUSAGE: IndexManager ");
    System.err.println("\tGUI for index maintainance");
  }
  
  class IndexingThread extends Thread {
    CorpusList clist;
    public IndexingThread(CorpusList cl) {
      super("Indexing thread");
      clist = cl;
    }
    
    public void run() {
      activeIndexing = true;
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
                                                  dictProps.getProperty("file.encoding"));
          tkr.setVerbose(debug);
          tkr.setIgnoredElements(props.getProperty("tokeniser.ignore.elements"));
          // if (debug) {
          imui.print("\n----- Processing: "+fname+" ------\n");
          //}
          if (dict.indexed(fname)){
            throw new AlreadyIndexedException(fname);
          }
          imui.print("-- Tokenising ...\n");
          tkr.tokenise();

          TokenMap tm = tkr.getTokenMap();
          //System.err.print(tm.toString());
          imui.print("-- Indexing ...\n");
          dict.setVerbose(debug);
          dict.addToDictionary(tm, fname);
          dict.sync();
          imui.print("-- Done.\n");
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
        catch (java.io.IOException ex){
          imui.print("IO Error processing file "+fname+": "+ex+"\n");
          imui.print("Indexing stopped.\n");
          activeIndexing = false;
          imui.enableChoice(true);
          return;
        }
      } // end for 
      imui.print("----- Indexing completed.");
      activeIndexing = false;
      imui.enableChoice(true);
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
