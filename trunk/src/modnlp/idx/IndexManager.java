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
import modnlp.idx.gui.*;
import modnlp.idx.inverted.TokeniserRegex;
import modnlp.idx.database.Dictionary;
import modnlp.idx.database.DictProperties;
import modnlp.idx.database.AlreadyIndexedException;
import modnlp.dstruct.CorpusList;
import modnlp.dstruct.TokenMap;

import java.io.File;
import java.util.Enumeration;

import javax.swing.JOptionPane;

/**
 *  GUI for corpus maintainance.
 *
 * @author  S Luz &#60;luzs@cs.tcd.ie&#62;
 * @version <font size=-1>$Id: IndexManager.java,v 1.2 2006/05/22 17:26:02 amaral Exp $</font>
 * @see  
*/
public class IndexManager {
  private static boolean verbose = true;
  private IndexManagerProperties props = new IndexManagerProperties();
  Dictionary dict;
  CorpusList clist;
  IndexManagerUI imui;
  IndexingThread indexingThread;

  public IndexManager () {
    imui = new IndexManagerUI(this);
  }

  public void chooseNewCorpus(){
    CorpusChooser ncc = new CorpusChooser(props.getProperty("last.index.dir"));
    int r;
    while (!((r = ncc.showChooseCorpus()) == CorpusChooser.APPROVE_OPTION ||
             (r != CorpusChooser.CANCEL_OPTION && dict != null)) ) 
      {
        JOptionPane.showMessageDialog(null, "Please choose a corpus directory (folder)");      
      }
    if (r == CorpusChooser.CANCEL_OPTION)
      return;
    String cdir = ncc.getSelectedFile().toString();
    props.setProperty("last.index.dir", cdir);
    DictProperties dp = new DictProperties(cdir);
    if (dict != null)
      dict.close();
    dict = new Dictionary(true,dp);
    imui.setCurrentDir(dp.getProperty("last.datafile.dir"));
    imui.setTitle("IndexManager: operating on index at "+cdir);
    imui.print("\n----- Selected corpus: "+cdir+" ------\n");
    imui.setCorpusListData(dict.getIndexedFileNames());
    dict.setVerbose(verbose);
  }

  public void indexSelectedFiles (File[] files) {
    dict.getDictProps().setProperty("last.datafile.dir", files[0].getParent());
    indexingThread = new IndexingThread(new CorpusList(files));
    indexingThread.start();
  }

  public void exit(int c)
  {
    if (dict != null)
      dict.close();
    if (props != null)
      props.save();
    System.exit(c);
  }

  public static void main(String[] args) {
    IndexManager im = new IndexManager();
    try {
      im.chooseNewCorpus();
      im.imui.pack();
      im.imui.setVisible(true);
      //System.out.println(System.setProperty("file.encoding", "ISO8859_1"));
      /*CorpusChooser ncc = new CorpusChooser();
        while (ncc.showChooseCorpus() != CorpusChooser.APPROVE_OPTION) {
        JOptionPane.showMessageDialog(null, "Please choose a corpus directory (folder)");      
        }
        DictProperties dp = new DictProperties(ncc.getSelectedFile().toString());
        Dictionary d = null;
        try {
        dict = new Dictionary(true,dp);
        dict.setVerbose(verbose);
        // IndexManager mti = new IndexManager();
        */
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
    System.err.println("\ttGUI for index maintainance");
  }
  
  class IndexingThread extends Thread {
    CorpusList clist;
    public IndexingThread(CorpusList cl) {
      super("Indexing thread");
      clist = cl;
    }
        
    public void run() {
      for (Enumeration e = clist.elements(); e.hasMoreElements() ;) {
        String fname = (String)e.nextElement();
        try {
          TokeniserRegex tkr = new TokeniserRegex(new File(fname));
          // if (verbose) {
          imui.print("\n----- Processing: "+fname+" ------\n");
          tkr.setVerbose(false);
          //}
          if (dict.indexed(fname)){
            throw new AlreadyIndexedException(fname);
          }
          imui.print("-- Tokenising ...\n");
          tkr.tokenise();
          TokenMap tm = tkr.getTokenMap();
          //System.err.print(tm.toString());
          imui.print("-- Indexing ...\n");
          dict.setVerbose(false);
          dict.addToDictionary(tm, fname);
          imui.print("-- Done.\n");
        }
        catch (AlreadyIndexedException ex){
          imui.print("Warning: "+ex+"\n");
          imui.print("Ignoring this entry.\n");
        }
        catch (java.io.IOException ex){
          imui.print("IO Error processing file "+fname+": "+ex+"\n");
          imui.print("Indexing stopped.\n");
          return;
        }
      }

    }
  }


}
