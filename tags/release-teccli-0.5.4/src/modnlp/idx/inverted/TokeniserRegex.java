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
package modnlp.idx.inverted;

import modnlp.util.Tokeniser;
import modnlp.util.PrintUtil;

import java.net.URL;
import java.io.*;

import java.util.regex.*;

/**
 *  Tokenise a chunk of text and record the position of each token
 *
 * @author  S Luz &#60;luzs@cs.tcd.ie&#62;
 * @version <font size=-1>$Id: TokeniserRegex.java,v 1.2 2006/05/22 17:26:02 amaral Exp $</font>
 * @see  
*/
public class TokeniserRegex extends Tokeniser {

  private String bigWordRegexp = "\\p{L}[\\p{L}-.]*'?s?"; // include dots for abbrev. (e.g. U.S.A.)
  private String wordRegexp = "[\\p{L}.]+|'s?";
  private String ignoredElements = "(omit|ignore)";
  
  public TokeniserRegex(String t){
    super(t);
  }

  public TokeniserRegex(File t, String e) throws IOException {
    super(t,e);
  }

  public TokeniserRegex(URL t, String e) throws IOException {
    super(t,e);
  }

  /**
   * Get the <code>IgnoredElements</code> value.
   *
   * @return a <code>String</code> value
   */
  public final String getIgnoredElements() {
    return ignoredElements;
  }

  /**
   * Get the <code>bigWordRegexp</code> value.
   *
   * @return a <code>String</code> value
   */
  public final String getBigWordRegexp() {
    return bigWordRegexp;
  }

  /**
   * Set the <code>IgnoredElements</code> value.
   *
   * @param newIgnoredElements The new IgnoredElements value.
   */
  public final void setIgnoredElements(final String newIgnoredElements) {
    this.ignoredElements = newIgnoredElements;
  }

  public void tokenise ()  {
    String ignregexp = "--+|\\.\\.+|\\.+\\p{Space}";  // delete full stops and dashes (typically not used).
    if (ignoredElements != null && ignoredElements.length() > 0)
      ignregexp = ignregexp+"|< *"+ignoredElements+".*?>.*?</"+ignoredElements+" *>";
    if (!tagIndexing)
      ignregexp = ignregexp+"|<.*?>";
    //ignregexp = ignregexp+"|\\W\\W+";

    Pattern p = Pattern.compile(ignregexp);
    Matcher igns = p.matcher(originalText);

    StringBuffer tx = new StringBuffer(originalText);
    int ct = 1;
    while (igns.find()) {
      int s = igns.start();
      int e = igns.end();
      if (verbose)
        PrintUtil.printNoMove("Processing exclusions ...",ct++);
      //System.err.println("replacing"+originalText.substring(s,e));
      char sp[] = new char[e-s];
      for (int j = 0;  j < sp.length; j++) {
        sp[j] = ' ';
      }
      tx.replace(s,e,new String(sp));
    }
    if (verbose)
      PrintUtil.donePrinting(); ct = 1;
    //verbose = false;
    String text = new String(tx);
    //System.out.println("-->"+text+"<--");
    Matcher bwre = (Pattern.compile(bigWordRegexp)).matcher(text);
    Pattern wrep = Pattern.compile(wordRegexp);
    while (bwre.find()) {
      int pos = bwre.start();
      String word = bwre.group();
      if (verbose)
        PrintUtil.printNoMove("Tokenising ...",ct++);
      int iofd = word.indexOf('.');  // index of first dot
      int iolc = word.length()-1;    // index of last char
      boolean hogc = word.indexOf('-')+word.indexOf('\'') == -2 ? false : true;
      if ( iofd >= 0) {      // word is an acronym, possibly with a missing dot or ...
        //System.out.println("-->"+word+"<-- iofd="+iofd+" iolc="+iolc+" hogc="+hogc);
        if (iofd == iolc)    // ... a normal word with a trailing dot
          tokenMap.putPos(word.substring(0,iolc), pos);
        else if (iofd == 0)    // ... a normal word with a leading dot
          tokenMap.putPos(word.substring(1,iolc+1), pos); 
        else if (word.charAt(iolc) == '.' || hogc) // right, this is a complete acronym. hyphenated or genitive
          tokenMap.putPos(word, pos);      // ... so store as is.
        else if (!hogc)                     // incomplete acronym...
          tokenMap.putPos(word+".", pos);  // ... add missing dot and store acronym
      }
      else {
        tokenMap.putPos(word, pos);      // word, hyphenated or genitive containing no dots 
      }
      if ( !hogc ) // no hyphenated subwords or genitives to index
        continue;
      Matcher wm = wrep.matcher(word);
      // hyphenated subword indexing overgenerates. E.g. "U.S.A-backed
      // terrorism" would cause indexing of "U", "S" and "A" as well
      // as "backed". We will put up with this slight impairment to
      // precision for the sake of recall.
      while (wm.find()) {
        if (verbose)
          PrintUtil.printNoMove("Tokenising ...",ct++);
        String sword = wm.group();
        // System.out.println("->"+sword+"<-");
        int pos2 = wm.start();
        if (sword.equals("'s")) // genitives are all indexed as "'"
          tokenMap.putPos("'",pos+pos2);
        else    // some of these will really be single quotes; should we ignore them??
          tokenMap.putPos(sword,pos+pos2);
      }
    }
    if (verbose)
      PrintUtil.donePrinting(); ct = 1;
  }

}
