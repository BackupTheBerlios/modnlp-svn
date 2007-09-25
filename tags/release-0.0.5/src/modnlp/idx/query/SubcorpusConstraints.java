/**
 *  © 2007 S Luz <luzs@cs.tcd.ie>
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
package modnlp.idx.query;

import modnlp.idx.database.SubcorpusTable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

/**
 *  Store constraints for subcorpus selection in the form
 *
 *<pre>
 *   KEY         |  SUBCORPUS_POS_PAIR
 *   -----------------------------------
 *   FILE_ID     |  [SECTION_ID, ...]
 *</pre>
 *
 *  Section IDs can be any string (identified as an ID type in the DTD
 *  for the file from which sections are extracted by, for instance,
 *  {@link modnlp.idx.MakeSectionIndex}). 
 * 
 * @author  S Luz &#60;luzs@cs.tcd.ie&#62;
 * @version <font size=-1>$Id: $</font>
 * @see  
*/

public class SubcorpusConstraints extends HashMap {
  static final String ALLSIDS = "_ALLSIDS_";
  static final String[] ALLSIDSA = {ALLSIDS};
  static final HashSet ALLSIDSHS = new HashSet(Arrays.asList(ALLSIDSA));
  boolean negativeConstraint = false;

  public SubcorpusConstraints(){
    //super();
    negativeConstraint = false;
  }

  public SubcorpusConstraints(boolean negative){
    //super();
    negativeConstraint = negative;
  }

  public Object put (String fid, HashSet hs){
    return super.put(fid, hs);
  }

  public Object putEntireFile (String fid){
    return put(fid, ALLSIDSHS);
  }

  public boolean accept(String fid, int pos, SubcorpusTable sbct){
    //System.err.println("FID="+fid);
    if (sbct == null) // no sections identified
      if (negativeConstraint) 
        return containsKey(fid) ? false : true;
      else
        return containsKey(fid) ? true : false;
    return accept(fid, sbct.getSectionID(pos));
  }

  public boolean accept(String fid, String sid){
    HashSet hs = (HashSet)get(fid);
    if (negativeConstraint)
      // section isn't excluded unless we explicitly say so.
      return hs==null? true : !hs.contains(sid);
    else
      // file isn't included unless we say so
      return hs!=null && hs.contains(sid);
  }


  public static void main (String[] argv){
    try {
      com.sleepycat.je.EnvironmentConfig ec = new com.sleepycat.je.EnvironmentConfig();
      ec.setReadOnly(true);
      com.sleepycat.je.Environment e = new com.sleepycat.je.Environment(new java.io.File(argv[0]), ec);
      String f = argv[1];  // file id
      SubcorpusTable s = new SubcorpusTable(e, f, false);
      SubcorpusConstraints sc = new SubcorpusConstraints();
      String [] as = {"s1", "s5", "s10", "w1"};
      sc.put("2", new HashSet(java.util.Arrays.asList(as)));
      if (argv.length > 2) {
        int p = (new Integer(argv[2])).intValue(); // position
        System.err.println("Accept position "+p+"? "+sc.accept(f,p,s));
        System.exit(0);
      }
      System.out.print("Enter position or press <ENTER> to quit.\n> ");
      java.io.BufferedReader cline 
        = new java.io.BufferedReader(new java.io.InputStreamReader(System.in));
      String req = cline.readLine();
      while ( ! req.equalsIgnoreCase("") ) {
        int p = (new Integer(req)).intValue(); // position
        System.err.println("Accept position "+p+"? "+sc.accept(f,p,s));
        req = cline.readLine();
      }      
    } catch (Exception e) {
      System.err.println("Error:");
      e.printStackTrace(System.err);
    }
  }

}
