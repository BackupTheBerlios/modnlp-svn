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
import modnlp.dstruct.CorpusList;
import modnlp.idx.inverted.TokeniserRegex;
import modnlp.dstruct.TokenMap;
import modnlp.idx.database.Dictionary;
import modnlp.idx.database.AlreadyIndexedException;

import java.io.File;
import java.util.Enumeration;

/**
 *  Read input file(s) and create case, occurrence, position and
 *  frequency tables for use with TEC
 *
 * @author  S Luz &#60;luzs@cs.tcd.ie&#62;
 * @version <font size=-1>$Id: MakeTECIndex.java,v 1.2 2006/05/22 17:26:02 amaral Exp $</font>
 * @see  
*/
public class MakeTECIndex {
  private static boolean verbose = true;


  public static void main(String[] args) {
    System.out.println(System.setProperty("file.encoding", "ISO8859_1"));
    Dictionary d = null;
    try {
      d = new Dictionary(true); 
      d.setVerbose(verbose);
      // MakeTECIndex mti = new MakeTECIndex();
      CorpusList clist =  new CorpusList(args[0]);
      for (Enumeration e = clist.elements(); e.hasMoreElements() ;) {
        try {
          String fname = (String)e.nextElement();
          TokeniserRegex tkr = new TokeniserRegex(new File(fname));
          if (verbose) {
            System.err.print("\n----- Processing: "+fname+" ------\n");
            tkr.setVerbose(verbose);
          }
          if (d.indexed(fname)){
            throw new AlreadyIndexedException(fname);
          }
          tkr.tokenise();
          TokenMap tm = tkr.getTokenMap();
          //System.err.print(tm.toString());
          d.addToDictionary(tm, fname);
          // test
        }
        catch (AlreadyIndexedException ex){
          System.err.println("Warning: "+ex);
          System.err.println("Ignoring this entry.");
        }
      } // end for
      //d.dump();
      //d.printSortedFreqList(new java.io.PrintWriter(System.out));
      d.close();
    } // end try
    catch (Exception ex){
      System.err.println(ex);
      ex.printStackTrace();
      if (d != null)
        d.close();
      usage();
    }
  }



  public static void usage() {
    System.err.println("\nUSAGE: MakeTECIndex CORPUS_LIST [-e]");
    System.err.println("\ttokenise and index each file in CORPUS_LIST");
    System.err.println("\tOptions:");
    System.err.println("\t\t-e\tStop indexing if index contains");
    System.err.println("\t\t  \ta file in CORPUS_LIST");
  }
}
