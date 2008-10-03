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

import java.io.File;
import java.util.Arrays;
import java.util.StringTokenizer;
import javax.swing.JLabel;
/**
 *  Manage the array of concordances returned by the server.
 *  Consisting mostly of legacy code, this class is in 
 *  need of a radical overhaul 
 * 
 * @author  S Luz &#60;luzs@acm.org&#62;
 * @version <font size=-1>$Id: ConcordanceObject.java,v 1.1.1.1 2000/07/07 16:54:36 luz Exp $</font>
 * @see  ContextClient
 */
public class ConcordanceObject 
  implements Comparable{

  public static char[] SEPTKARR = {' ',
                                   '|',
                                   '\'',
                                   '`',
                                   '"',
                                   '-',
                                   '_',
                                   (new String(",")).charAt(0),
                                   (new String(".")).charAt(0),
                                   '?',
                                   '!',
                                   (new String(";")).charAt(0),
                                   ':',
                                   '<',
                                   '>',
                                   '{',
                                   '}',
                                   '[',
                                   ']',
                                   '=',
                                   '+',
                                   '/',
                                   '\\',
                                   '%',
                                   '$',
                                   '*',
                                   '&',
                                   '(',
                                   ')' };
  public static String SEPTOKEN = new String(SEPTKARR);
  public String concordance;
  public String filename;
  public String sfilename;
  public int filepos;
  public int index;
  public long bytepos;

  // Sort variables
  public static int LEFT = -1;
  public static int RIGHT = 1;
  public int sortDirection = LEFT;
  public int sortContextHorizon = 1;
  public int halfConcordance = 65;

  public ConcordanceObject(String concLine){

    if (concLine == null) 
      throw new NullPointerException();
    char[] data = concLine.toCharArray();
    int start = 0;
    // index will be mapped by ConArray
    //int index = -1;
		
    if(concLine.equals("null"))
      {
        concordance = null;
        filename = null;
        filepos = 0;
      }
		
    for(int i = start ; i < data.length ; i++)
      {
        if(data[i] == '|')
          {
            filename = new String(data, start, i);
            sfilename =   (new File(filename)).getName();
            //  filename.substring(filename.lastIndexOf('/')+1);
            start = i + 1;
            break;
          }
      }
		
    for(int i = start; i < data.length ; i++)
      {
        if(data[i] == '|')
          {
            Integer iW = new Integer(new String(data, start, (i-start)));
            filepos = iW.intValue();
            start = i + 1;
            break;
          }
      }
		
    concordance = new String(data, start, (data.length - start));
    bytepos = filepos;
  }

  public JLabel labelConcLine (int lfn_size){
    String offset = adjustOffSet(lfn_size,filename.length());
    return new JLabel(filename+offset+concordance);
  }

  public String textConcLine (int lfn_size){
    String fn = sfilename.length() > lfn_size? 
      sfilename.substring(0,lfn_size-1) : sfilename;
    String offset = adjustOffSet(lfn_size,fn.length());
    sfilename = fn == null ?  " " : fn+offset;
    return sfilename+""+concordance+" ["+(index+1)+"]";
  }

  public String textFilename (int lfn_size){
    String fn = sfilename.length() > lfn_size? 
      sfilename.substring(0,lfn_size-1) : sfilename;
    String offset = adjustOffSet(lfn_size,fn.length());
    sfilename = fn;
    return fn == null ? " " : fn+offset;
  }

  public String textConcordance () {
    return concordance+" ["+(index+1)+"]";
  }

  public String getLeftContext(){
    return concordance.substring(0,halfConcordance);
  }

  public StringTokenizer getLeftContextTokens(){
    return new StringTokenizer(getLeftContext(),
                                SEPTOKEN,
                                false);
  }

  public StringTokenizer getRightContextTokens(){
    StringTokenizer s = new StringTokenizer(getKeywordAndRightContext(),
                                            SEPTOKEN,
                                            false);
    s.nextToken();
    return s;
  }

  public String getKeywordAndRightContext(){
    return concordance.substring(halfConcordance);
  }

  public int compareTo(Object o) {
    ConcordanceObject b = (ConcordanceObject)o;
		
    if ( this == null ) {
      return -1;
    }
    if ( o == null ) {
      return 1;
    }
    return (sortDirection == LEFT)? 
      compareLeft(concordance, b.concordance):
      compareRight(concordance, b.concordance);
  }

  public int compareLeft (String a, String b){
		
    StringBuffer ab = new StringBuffer(a.substring(0,halfConcordance));
    StringBuffer bb = new StringBuffer(b.substring(0,halfConcordance));
    StringTokenizer at = new StringTokenizer(ab.reverse().toString(),
                                             SEPTOKEN,
                                             false);
    StringTokenizer bt = new StringTokenizer(bb.reverse().toString(),
                                             SEPTOKEN,
                                             false);
    StringBuffer ac = new StringBuffer("");
    StringBuffer bc = new StringBuffer("");
    // Read up to horizon
    for (int i = 1; i < sortContextHorizon; i++) {
      StringBuffer t = new StringBuffer(at.nextToken());
      ac.append(t.reverse().toString()+" ");
      t = new StringBuffer(bt.nextToken());
      bc.append(t.reverse().toString()+" ");
    }
    // Insert horizon word
    StringBuffer t = new StringBuffer(at.nextToken());
    ac.insert(0, t.reverse().toString()+" ");
    t = new StringBuffer(bt.nextToken());
    bc.insert(0, t.reverse().toString()+" ");

    return ac.toString().toLowerCase().compareTo(bc.toString().toLowerCase());
  }
	
  public int compareRight (String a, String b){

    String as = a.substring(halfConcordance);
    String bs = b.substring(halfConcordance);
    StringTokenizer at = new StringTokenizer(as,
                                             SEPTOKEN,
                                             false);
    StringTokenizer bt = new StringTokenizer(bs,
                                             SEPTOKEN,
                                             false);
    // Discard half-keywords
    at.nextToken();            
    bt.nextToken();
    StringBuffer ac = new StringBuffer("");
    StringBuffer bc = new StringBuffer("");
    // Read up to horizon
    for (int i = 1; i < sortContextHorizon ; i++) {
      if ( at.hasMoreTokens() ) {
        ac.append(at.nextToken()+" ");
      }
      if ( bt.hasMoreTokens() ) {
        bc.append(bt.nextToken()+" ");
      }
    }
    // Insert horizon word if any
    if ( at.hasMoreTokens() ) 
      ac.insert(0, at.nextToken()+" ");
    if ( bt.hasMoreTokens() )  
      bc.insert(0, bt.nextToken()+" ");
		
    return ac.toString().toLowerCase().compareTo(bc.toString().toLowerCase());
  }


  public HighlightString indexOfSortContext(){
    if (sortContextHorizon < 0)
      return indexOfSortContextLeft(0-sortContextHorizon);
    if (sortContextHorizon > 0)
      return indexOfSortContextRight(sortContextHorizon);
    return new HighlightString(0,"");
  }

  /** Return the index of context horizon ctx
   *	to the left of the keyword on the concordance line.  
   **/
  public HighlightString indexOfSortContextLeft(int srtctx){
    StringBuffer a = new StringBuffer(concordance.substring(0,halfConcordance));
    TecTokenizer at = new TecTokenizer(a.reverse().toString(),
                                       SEPTOKEN,
                                       true);
    try {
      int ind = 0;
      for (int i = 1; i < srtctx; i++) {
        String w = at.safeNextToken();
        ind = ind + w.length(); 
        if (isSeparatorChar(w.charAt(0))){// ignore separator
          --i;
        }
      }	
      StringBuffer wb;
      do {
        wb = new StringBuffer(at.safeNextToken());
        ind = ind + wb.length(); 
      } while (isSeparatorChar(wb.charAt(0)));
			
      String word = wb.reverse().toString();
      return new HighlightString(halfConcordance - ind, 
                                 word);
    }
    catch (StringIndexOutOfBoundsException e){
      return new HighlightString(0,"");
    }
  }


  /** Return the index of context horizon ctx
   *	to the right of the keyword on the concordance line.  
   **/
  public HighlightString indexOfSortContextRight(int srtctx){
    StringBuffer a = new StringBuffer(concordance.substring(halfConcordance));
    TecTokenizer at = new TecTokenizer(a.toString(),
                                       SEPTOKEN,
                                       true);
    try {
      int ind = 0;
      // discard keyword
      ind = ind + at.safeNextToken().length();            
      for (int i = 1; i < srtctx; i++) {
        String w = at.safeNextToken();
        ind = ind + w.length(); 
        if (isSeparatorChar(w.charAt(0))){// ignore separator
          --i;
        }
      }	
      String word;
      do {
        word = at.safeNextToken();
        ind = ind + word.length(); 
      } while (isSeparatorChar(word.charAt(0)));
      ind = ind - word.length(); // move backwards
      return new HighlightString(halfConcordance + ind, 
                                 word);
    }
    catch (StringIndexOutOfBoundsException e){
      return new HighlightString(0,"");
    }

  }


  public HighlightString indexOfKeyword(){

    StringBuffer a = new StringBuffer(concordance.substring(halfConcordance));
    StringTokenizer at = new StringTokenizer(a.toString(),
                                             SEPTOKEN,
                                             false);
    return  new HighlightString(halfConcordance , at.nextToken());            

  }


  public boolean isSeparatorChar(char c){
    // why doesn't binarySearch work for '.' and ','?????
    return ( c == '.' || c == ',' || Arrays.binarySearch(SEPTKARR, c) >= 0);
  }


  public static int getLengthLongestFname(ConcordanceObject[] c)
  {
    int lfn = 0;

    for(int count = 0; count < c.length && c[count] != null; count++)
      {
        if ( c[count].filename.length() > lfn )
          lfn = c[count].filename.length();
      }
    return lfn;
  }

  public static String adjustOffSet(int maxs, int size){
		
    char[] auxA = new char[maxs-size];
    for(int i = 0; i < (maxs-size) ; i++)
      auxA[i] = ' ';
    
    return new String(auxA);
  }


}
