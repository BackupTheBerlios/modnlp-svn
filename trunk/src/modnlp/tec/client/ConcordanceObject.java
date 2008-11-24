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
public class ConcordanceObject {

  public static char[] SEPTKARR =  modnlp.util.Tokeniser.SEPTKARR;

  public static final String SEPTOKEN = new String(SEPTKARR);
  public static final ConcordanceObject RENDERER_PROTOTYPE = getRendererPrototype();

  /* store a pointer to the ConcordanceVector so that we can access
   * properties of shared by all ConcordanceObject's, such as
   * longestFileName, sortContextHorizon etc */
  private ConcordanceVector coVector;
  
  public String concordance;
  public String filename;
  public String sfilename;
  public int filepos;
  public int index;
  public long bytepos;
  
  public ConcordanceObject(String concLine, ConcordanceVector cv){

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
    
    coVector = cv;
    concordance = new String(data, start, (data.length - start));
    bytepos = filepos;
  }

  public static ConcordanceObject getRendererPrototype(){
    ConcordanceObject c = new ConcordanceObject("/tmp/idxtest/data/ep/EN20050127.xml|15914|ed responsibility is blurred again by the wording that has now been proposed. I do not wish to drag outxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx",
                                            null);
    c.setIndex(9999998);
    return c;
  }

  public JLabel labelConcLine (int lfn_size){
    String offset = adjustOffSet(lfn_size,filename.length());
    return new JLabel(filename+offset+concordance);
  }

  public void setIndex(int i){
    index = i;
  }

  public String textConcLine (int lfn_size){
    String fn = sfilename.length() > lfn_size? 
      sfilename.substring(0,lfn_size-1) : sfilename;
    String offset = adjustOffSet(lfn_size,fn.length());
    sfilename = fn == null ?  " " : fn+offset;
    return sfilename+""+concordance+" ["+(index+1)+"]";
  }

  public int getFilenameLength() {
    return filename.length(); 
  }

  public String textFilename (int lfn_size) {
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
    return concordance.substring(0,coVector.getHalfConcordance());
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

  public final String getKeywordAndRightContext(){
    return concordance.substring(coVector.getHalfConcordance());
  }

  public final int getSortContextHorizon(){
    return coVector.getSortContextHorizon();
  }

  public final HighlightString indexOfSortContext(){
    int sch = coVector.getSortContextHorizon(); 
    if (sch < 0)
      return indexOfSortContextLeft(0-sch);
    if (sch > 0)
      return indexOfSortContextRight(sch);
    return new HighlightString(0,"");
  }

  /** Return the index of context horizon ctx
   *	to the left of the keyword on the concordance line.  
   **/
  public HighlightString indexOfSortContextLeft(int srtctx){
    int hc = coVector.getHalfConcordance();
    StringBuffer a = new StringBuffer(concordance.substring(0,hc));
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
      return new HighlightString(hc - ind, 
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
    int hc = coVector.getHalfConcordance();
    StringBuffer a = new StringBuffer(concordance.substring(hc));
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
      return new HighlightString(hc + ind, 
                                 word);
    }
    catch (StringIndexOutOfBoundsException e){
      return new HighlightString(0,"");
    }

  }

  // used by ListDisplayRenderer
  public HighlightString indexOfKeyword(){
    int hc = coVector.getHalfConcordance();
    StringBuffer sb = new StringBuffer();
    String s = concordance.substring(hc);
    int l = s.length();
    for (int i = 0; i < l; i++) {
      char c = s.charAt(i);
      if (Character.isLetterOrDigit(c))
        sb.append(c);
      else 
        break;
    }
    return new HighlightString(hc , sb.toString());
  }


  public boolean isSeparatorChar(char c){
    // why doesn't binarySearch work for '.' and ','?????
    return ( c == '.' || c == ',' || Arrays.binarySearch(SEPTKARR, c) >= 0);
  }



  public static String adjustOffSet(int maxs, int size){
		
    char[] auxA = new char[maxs-size];
    for(int i = 0; i < (maxs-size) ; i++)
      auxA[i] = ' ';
    
    return new String(auxA);
  }


}
