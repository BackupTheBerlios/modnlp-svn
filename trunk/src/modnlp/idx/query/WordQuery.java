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
package modnlp.idx.query;

import modnlp.dstruct.WordForms;
import modnlp.idx.database.Dictionary;
import modnlp.idx.database.WordPositionTable;

import java.util.StringTokenizer;
import java.util.*;
/**
 *  Tokenize and parse the keyword query
 *
 * 
 * @author Saturnino Luz &#60;luzs@acm.org&#62;
 * @version <font size=-1>$Id: WordQuery.java,v 1.4 2003/06/22 17:55:15 luzs Exp $</font>
 * @see  
*/
public class WordQuery {
  
  public static final String QSEPTOKEN = "+[]";
  public static char[] SEPTKARR 
    = {' ', '|', '\'','`','"','-','_',(new String(",")).charAt(0),
       (new String(".")).charAt(0),'?','!',(new String(";")).charAt(0),':',
       '<','>','{','}','[',']','=','+','/','\\','%','$','*','&','(',')' };
  public static String SEPTOKEN = new String(SEPTKARR);

  private String originalQuery;

  // keyword is the least frequent word if query contains more than
  // one word
  private String keyword;

  // the first word in originalQuery (left to right)
  private String firstWord;
  private int firstWordPos = 0;

  private boolean justKeyword = false;
  private boolean caseSensitive = false;
  
  // Array of keywords: [k1, k2, k3, ..., kn]
  private String [] queryArray;
  // Array of max allowed intervening words between k1 and ki
  // [-1, i1, i2, i3, ..., in], where:
  //   i1 = max allowed intervening words between k1 and k1
  //   i2 = max allowed intervening words between k1 and k2
  // ...
  private int [] intervArray;
  private WordForms[] wformsArray;
  private WordForms keywordforms;
  private Dictionary dictionary;

  /**
   * Creates a new <code>WordQuery</code> instance based on a query
   * string, to be processed against dict. The query expression can be expressed in the following syntax: 
   *
   * <center><code>k_1[+[i_1]]k_2+[i_2]+k_3+...+[i_{n-1}]k_n].</code></center>
   *
   * where <code>word_i</code> can be a single keyword or a
   * (Unix-style) wildcard (e.g.  <code>test*</code> will retrive all
   * words wich start with <code>test</code> (e.g. <code>test</code>,
   * <code>tests</code>, <code>testament</code>,
   * etc). <code>i_n</code> denotes the maximum number of
   * intervening words between <code>k_{n+1}</code> and
   * <code>k_1</code>.
   *
   * <p> The syntax also allows you to specify sequences of key words,
   * and/or wildcards, and the maximum number of intervening words you
   * wish to allow between each element in the sequence.
   *
   * <p>Examples: 
   *
   * <ul> 
   *
   * <li> Entering <code>seen+before</code> will find * <code>...never seen before...</code> etc;
   *
   * <li> entering  <code>seen+[1]before</code> finds, in addition, 
   * <code>...seen her before...</code>, <code>...seen ie before...</code>, and all
   * sequences in which there is at most one word between
   * <code>seen</code> and <code>before</code>.  
   *
   * <li> Combinations of words and wildcards are also allowed, so
   * entering <code>know+before*</code> will find <code>...know before...</code>, 
   * <code>...know beforehand</code>, etc.
   *
   *
   * N.B.: much (perhaps most) of the functionality in this class
   * should really be moved into
   * modnlp.idx.database.Dictionary. Ideally, Dictionary should handle
   * the functionality currently implemented in
   * modnlp.idx.query.WordQuery.matchConcordance() and auxiliary
   * methods, leaving only query parsing and related methods for
   * WordQuery. This hasn't been done yet to preserve backward
   * compatibility with tec-server
   * 
   * @param query a <code>String</code> The query string
   * @param dict a <code>Dictionary</code> the top-level index accessor class
   * @param cs a <code>boolean</code> if true, the query is case sensitive.
   * @exception WordQueryException if an error occurs
   */
  public WordQuery (String query, Dictionary dict, boolean cs)
      throws WordQueryException
  {
    caseSensitive = cs; 
    originalQuery = query;
    int qsize = (new StringTokenizer(query, "+")).countTokens();
    queryArray = new String[qsize];
    intervArray = new int[qsize];

    // after parseQuery() queryArray will contain the keywords [kw1,
    // kw2, ..., kwn] and interv array the admissible gaps between kw1
    // and all other keywords (def.: gap between kw1 and kw1 = -1),
    // [-1, gap(kw1,kw2), ..., gap(kw1,kwn)]
    parseQuery(query);

    keywordforms = dict.getLeastFrequentWord(queryArray, cs);
    if ( keywordforms == null)
      keyword = "";
    else {
      keyword = keywordforms.getKeyword();
      wformsArray = new WordForms[queryArray.length];
      for (int i = 0; i < queryArray.length ; i++)
        wformsArray[i] = dict.getWordForms(queryArray[i],cs);
    }

      
    Horizon a = getLeftHorizon();
    Horizon b = getRightHorizon();

    // at this point all words/wildcard will have been expanded and their
    // expanded forms stored in wformsArray


    //dictionary = dict;


    /*
    System.err.println(modnlp.util.PrintUtil.toString(intervArray)+" kw="+keyword);
    Horizon a = getLeftHorizon();
    Horizon b = getRightHorizon();

    if (a != null){
      System.err.println("LIA= ["+modnlp.util.PrintUtil.toString(a.getHorizonArray())+"]");
      System.err.println("LWA= ["+modnlp.util.PrintUtil.toString(a.getWordArray())+"]");
      System.err.println("MLH= "+a.getMaxSearchHorizon());
    }
    else 
      System.err.println("null");

    if (b != null){
      System.err.println("RIA= ["+modnlp.util.PrintUtil.toString(b.getHorizonArray())+"]");
      System.err.println("RWA= ["+modnlp.util.PrintUtil.toString(b.getWordArray())+"]");
      System.err.println("MRH= "+b.getMaxSearchHorizon());
    }
    else 
      System.err.println("null");
    */

  }

  public WordForms getKeyWordForms(){
    return keywordforms;
  }

  public WordForms[] getWFormsArray(){
    return wformsArray;
  }

 /**
   * Get a <code>Horizon</code> object conaining the maximum distances
   * allowed in this query between the main keyword and the keywords
   * to the right of it in the query expression.
   *
   * @return a <code>Horizon</code> object or <code>null</code> if 1
   * or less keywords.
   */
  public Horizon getRightHorizon(){
    // if single keyword or zero-frequency keyword, return null
    if (intervArray.length == 1 || keywordforms == null ) 
      return null;

    int j = 0;
    int l2 = intervArray.length - 1;
    while (  j < intervArray.length && !queryArray[j].equals(keyword) )
      {j++;} // j = index kw
    int lria =  intervArray.length - (j+1);
    if (lria == 0)  // kw is rightmost word
      return null;
    int [] ria = new int[lria];
    WordForms[] wfa = new WordForms[lria];
    int distok1 = intervArray[j];
    for (int i = 1; i <= ria.length; i++) {
      ria[i-1] = intervArray[i+j]-distok1;
      wfa[i-1] = wformsArray[i+j];
    }
    return new Horizon(ria, wfa, ria[ria.length-1]);
  }

  /**
   * Get a <code>Horizon</code> object conaining the maximum distances
   * allowed in this query between the main keyword and the keywords
   * to the left of it in the query expression.
   *
   * @return a <code>Horizon</code> object or <code>null</code> if 1
   * or less keywords in query.
   */
  public Horizon getLeftHorizon(){
    // if single keyword or zero-frequency keyword, return null
    if (intervArray.length == 1 || keywordforms == null ) 
      return null;
    int[] aux = new int[intervArray.length-1];

    int j = 0;
    for (int i = 0; i < intervArray.length; i++) {
      if (intervArray[i] < 0)
        continue;
      aux[i-1] = intervArray[i];
      if ( queryArray[i].equals(keyword) ){
        j = i;
        break;
      }
    }
    if (j == 0)
      return null;
    int [] lia = new int[j];
    WordForms[] wfa = new WordForms[j];
    int k = j-1;
    int distok1 = aux[k];
    lia[k] = distok1+1;  // we need to look distok1 + 1 words back (the 1 includes k1) 
    for (int i = 0; i < k; i++){
      lia[k-(i+1)] = distok1 - aux[i];
      wfa[i] = wformsArray[k-i];
    }
    wfa[k] = wformsArray[0];
    return new Horizon(lia, wfa, lia[k]);
    /*    lia[0] = distok1+1;  // we need to look distok1 + 1 words back (the 1 includes k1) 
    for (int i = 0; i < k; i++){
      lia[i+1] = distok1 - aux[i];
      wfa[i] = wformsArray[i];
    }
    wfa[k] = wformsArray[k];
    return new Horizon(lia, wfa, lia[0]);
    */
  }


  public static boolean isWildcard(String key) {
    return ( key.lastIndexOf('*') > -1 );
  }

  public static String getWildcardsLHS (String key) {
    return key.substring(0,  key.lastIndexOf('*'));
  }

  public String getKeyword () {
    return keyword;
  }

  public String getFirstWord () {
    return firstWord;
  }

  public boolean isJustKeyword () {
    return justKeyword;
  }

  /**
   * <code>matchConcordance</code> match <code>cline</code> against
   * this query (represented after <code>parseQuery()</code> by
   * <code>queryArray</code> and <code>intervArray</code>)
   *
   * NB: this method really belongs in <code>Dictionary</code>. TO DO:
   * Check potential backward compat problems in tec-server and
   * deprecate WordQuery.matchConcordance() in favour of
   * Dictionary.matchConcordance().
   *
   * @param cline a <code>String</code> value
   * @param ctx an <code>int</code> value
   * @return <code>true</code> if cline matches, false otherwise.
   */
  public boolean matchConcordance (String cline, int ctx)
  {
    if ( justKeyword ) {
      firstWordPos = ctx;
      return true;
    }
    else{
      String [] la = getTokenArray(cline.substring(0, ctx+keyword.length()), true);
      //
      //return 
      firstWordPos = matchKeyWordFirstWord(cline, ctx, la);
      if (firstWordPos < 0)
        return false;
      String [] ra = getTokenArray(cline.substring(firstWordPos), false);
      //System.out.println("FWP"+firstWordPos);
      return partialMatchConcordance(ra);
    }
  }

  private String [] getTokenArray (String line, boolean inverted) {
    StringTokenizer sct = new StringTokenizer(line,SEPTOKEN,false);
    int asize = sct.countTokens();
    String[] sca = new String[asize];
    int i = inverted? asize-1 : 0;
    // store all tokens in an array
    while (sct.hasMoreElements()) {
      if (inverted)
        sca[i--] = (String)sct.nextElement();
      else
        sca[i++] = (String)sct.nextElement();
    }
    return sca;
  }
  
  // return the position of the first word on cline or -1 if no match
  private int matchKeyWordFirstWord (String cline, int ctx, String[] la) {
    if ( firstWord.equals(keyword) )
      return ctx;
    // find out how far keyword is from firstWord
    int k =  getIntervToWord(keyword)+1; 

    StringBuffer sub = new StringBuffer(" ");
    for (int j = 0; j <= k && j < la.length && la[j] != null ; j++)
      {
        sub.append(la[j]+" ");
      }
    String tmtch = isWildcard(firstWord) ?
      " "+getWildcardsLHS(firstWord) : " "+firstWord+" ";
    //System.out.println("FW"+tmtch+"SUB"+sub+"|\n"+cline);
    if ( caseSensitive ){
      if ( sub.toString().indexOf(tmtch) < 0 )
        return -1;
    }
    else {
      if ( sub.toString().toLowerCase().indexOf(tmtch.toLowerCase()) < 0 )
        return -1;
    }
    return cline.lastIndexOf(tmtch, ctx)+1;
  }

  public int getIntervToWord(String w){
    for (int i = 0; i < queryArray.length ; i++) 
      if ( w.equals(queryArray[i]) )
        return intervArray[i];
    return -1;
  }

  private String[] getLHSMatch (String [] qa, String sw) {
    String [] lhsMatch = new String[1];
    String [] aux = new String[qa.length-1];
    for (int i = 0 ; i < qa.length ; i++){
      if ( qa[i].equals(sw) ) {
        lhsMatch = new String[i];
        for (int j = 0; j < i; j++)
          lhsMatch[j] = aux[j];
        return lhsMatch;
      }
      aux[i] = qa[i];
    }
    return lhsMatch;
  }

  private String[] getRHSMatch (String [] qa, String sw) {
    String [] rhsMatch =  new String[1];
    String [] aux = new String[qa.length-1];
    boolean passk = false;
    int j = 0;
    for (int i = 0 ; i < qa.length ; i++){
      if (  qa[i].equals(sw) && !passk ) {
        passk = true;
        rhsMatch = new String[qa.length-(i+1)];
        continue;
      }
      if ( passk )
        rhsMatch[j++] = qa[i];
    }
    return rhsMatch;
  }

  private boolean partialMatchConcordance(String [] sca) {
    
    //for (Enumeration e = keys() ; e.hasMoreElements() ;) 
    
    for (int i = 1; i < queryArray.length ; i++)
      {
        String key =  queryArray[i]; //(String) e.nextElement();
        //Integer val = (Integer) get(key);
        //int k = val.intValue();
        int k =  intervArray[i]+1;
        StringBuffer sub = new StringBuffer(" ");
        for (int j = 1; j <= k && j < sca.length && sca[j] != null ; j++)
          {
            sub.append(sca[j]+" ");
          }
        String tmtch = isWildcard(key) ?
          " "+getWildcardsLHS(key) : " "+key+" ";
        //System.out.println("KEY |"+key+"| TMTCH"+ tmtch +"SUB |"+sub+"| K = "+k);
        if ( caseSensitive ){
          if ( sub.toString().indexOf(tmtch) < 0 )
            return false;
        }
        else {
          if ( sub.toString().toLowerCase().indexOf(tmtch.toLowerCase()) < 0 )
            return false;
        }
      }
    return true;
  }
  
  private void parseQuery (String query)
    throws WordQueryException
  {
    StringTokenizer st = new StringTokenizer(query, QSEPTOKEN, true);
    int ind = 0;
    firstWord = (String)st.nextElement(); 
    queryArray[ind] = firstWord;
    intervArray[ind++] = -1;
    try {
      if ( !st.hasMoreElements() ){
        justKeyword = true;
        return;
      }  
      int intervening = 0;
      while ( st.hasMoreElements() )
        {
          String el = (String) st.nextElement();
          String filter      = "";
          if (el.equals("+") )
            {
              String s = (String) st.nextElement();
              if (s.equals("[") )
                {
                  intervening += 
                    (new Integer((String)st.nextElement())).intValue();
                  String c = (String) st.nextElement();
                  filter = (String) st.nextElement();
                }
              else 
                {
                  filter = s;
                }
            }
          else 
            {
              throw new WordQueryException("Tec: Error Parsing Query", originalQuery);        
            }
          //System.out.println("-Putting "+filter+" at"+intervening);
          queryArray[ind] = filter;
          intervArray[ind++] = intervening++;          
        }
    }
    catch (NoSuchElementException e){
     e.printStackTrace(); 
     throw new WordQueryException(e.getMessage(), originalQuery);
    }
    catch (Exception e){
     e.printStackTrace();
    }
  }


}


