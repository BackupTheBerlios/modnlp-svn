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
package modnlp.util;

import modnlp.dstruct.TokenMap;

import java.net.URL;
import java.util.StringTokenizer;
import java.io.*;

/**
 *  Tokenise a chunk of text and record the position of each token
 *
 * @author  S Luz &#60;luzs@cs.tcd.ie&#62;
 * @version <font size=-1>$Id: Tokeniser.java,v 1.2 2006/05/22 17:26:02 amaral Exp $</font>
 * @see  
*/
public class Tokeniser {

  protected boolean tagIndexing = false; 
  protected boolean verbose = false; 
  protected String originalText;
  protected TokenMap tokenMap;
  
  protected String encoding = "UTF8";

  public static final char[] SEPTKARR = {' ',
                                         '|',
                                         '\'',
                                         '`',
                                         '"',
                                         '-',
                                         '_',
                                         (new String(",")).charAt(0),
                                         (new String("\n")).charAt(0),
                                         (new String("\t")).charAt(0),
                                         //(new String(".")).charAt(0),
                                         // we want to be able to get abbreviations (e.g. "U.S.A." => "USA")
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
  public static final String SEPTOKEN = new String(SEPTKARR);


  public Tokeniser (String text) {
    originalText = text;
    tokenMap = new TokenMap();
  }

  public Tokeniser (URL url, String e) throws IOException {
    encoding = e;
    BufferedReader in = 
      new BufferedReader(new InputStreamReader(url.openStream(), encoding));
    StringBuffer sb = new StringBuffer(in.readLine()+" ");
    String line = null;
    while ((line = in.readLine()) != null) {
      sb.append(line);
      sb.append(" ");
    }
    originalText = sb.toString();
    tokenMap = new TokenMap();
  }

  public Tokeniser (File file,  String e) throws IOException {
    encoding = e;
    BufferedReader in = 
      new BufferedReader(new InputStreamReader(new FileInputStream(file), encoding));
    StringBuffer sb = new StringBuffer(in.readLine()+" ");
    String line = null;
    while ((line = in.readLine()) != null) {
      sb.append(line);
      sb.append(" ");
    }
    originalText = sb.toString();
    tokenMap = new TokenMap();
  }

  public void setTokenMap(TokenMap t){
    tokenMap = t;
  }

  public boolean getTagIndexing() {
    return tagIndexing;
  }

  public void setTagIndexing(boolean v) {
    tagIndexing = v;
  }

  public boolean getVerbose() {
    return verbose;
  }

  public void setVerbose(boolean v) {
    verbose = v;
  }

  public String getEncoding() {
    return encoding;
  }

  public void setEncoding(String v) {
    encoding = v;
  }

  public TokenMap getTokenMap() {
    return tokenMap;
  }

  public String getOriginalText() {
    return originalText;
  }


  /**
   * <code>tokenise</code>: Very basic tokenisation; Serious tokenisers
   *  must override this method. Note that positions in the tokenMap
   *  here correspond to the ORDER in which the token appears in
   *  originalText not its actual OFFSET.
   *
   *  @see modnlp.idx.inverted.TokeniserRegex for a proper
   *  implementation.
   */
  public void tokenise ()
  {
    int ct = 0;
    StringTokenizer st = new StringTokenizer(originalText, SEPTOKEN, false);
    while (st.hasMoreTokens()){
      tokenMap.putPos(st.nextToken(), ct++);
      if (verbose)
        PrintUtil.printNoMove("Tokenising ...",ct);
    }
    if (verbose)
      PrintUtil.donePrinting();
  }

  /**
   * Delete dots (e.g. "U.S.A" => "USA"), remove spaces, clean up any
   * remaining garbage left by Tokenizer
   * @return type: a 'clean' type
   */
  public static String fixType(String type)
  {
    char[] nt = new char[type.length()];
    int j = 0;
    for (int i = 0; i < type.length(); i++)
      {
        if ( type.charAt(i) != '.' )
          nt[j++] = type.charAt(i);
      }
      
    return new String(nt, 0, j);
  }

  /**
   * Check is token is a negated token (e.g '¬c' in p(t|¬c))
   */
  public static boolean isBar(String token){
    return token.charAt(0) == '-';
  }

  /**
   *  Disbar token
   */
  public static String disbar(String token){
    return isBar(token) ? token.substring(1) : token;
  }


}
