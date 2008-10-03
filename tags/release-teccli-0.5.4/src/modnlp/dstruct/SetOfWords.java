package modnlp.dstruct;

import modnlp.util.Tokeniser;

import java.util.Map;
import java.util.Iterator;
import java.util.HashSet;
/**
 *  Term set for text
 * @author  Saturnino Luz &#60;luzs@acm.org&#62;
 * @version <font size=-1>$Id: SetOfWords.java,v 1.1.1.1 2005/05/26 13:59:30 amaral Exp $</font>
 * @see  
*/
public class SetOfWords extends HashSet{


  private boolean ignoreCase = true;

  public SetOfWords (String text)
  {
    super();
    addTokens(text);
  }

  public SetOfWords (String text, StopWordList swlist)
  {
    super();
    addTokens(text, swlist);
  }

  public void addTokens(String text){
    Tokeniser tkzr = new Tokeniser(text);
    tkzr.tokenise();
    TokenMap tm = tkzr.getTokenMap();
    for (Iterator e = tm.entrySet().iterator(); e.hasNext() ;)
			{
        Map.Entry kv = (Map.Entry) e.next();
        String t = (String) kv.getKey();
        addToken(t);
      } 
    //putAll(tkzr.getTokenMap());
  }

  public void addTokens(String text, StopWordList swlist){
    Tokeniser tkzr = new Tokeniser(text);
    tkzr.tokenise();
    TokenMap tm = tkzr.getTokenMap();
    for (Iterator e = tm.entrySet().iterator(); e.hasNext() ;)
			{
        Map.Entry kv = (Map.Entry) e.next();
        String t = (String) kv.getKey();
        if (swlist.contains(t))
          continue;
        addToken(t);
      } 
  }


  private boolean addToken (String type)
  {
    String key = isIgnoreCase() ? 
      Tokeniser.fixType(type.toLowerCase()) : Tokeniser.fixType(type);
    if (  key.equals("") )
      return false;
    return add(key);
  }

  public void removeStopWords (StopWordList swl)
  {
    removeAll(swl);
  }

  /**
   * Get the value of ignoreCase.
   * @return value of ignoreCase.
   */
  public boolean isIgnoreCase() {
    return ignoreCase;
  }
  

  /**
   * Set the value of ignoreCase.
   * @param v  Value to assign to ignoreCase.
   */
  public void setIgnoreCase(boolean  v) {
    this.ignoreCase = v;
  }
  

}
