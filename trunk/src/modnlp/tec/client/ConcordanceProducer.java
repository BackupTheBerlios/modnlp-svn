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

import modnlp.idx.database.Dictionary;
import modnlp.idx.query.WordQuery;
import modnlp.idx.query.WordQueryException;
import java.io.*;
  
/**
 *  Access and produce condordances directly from index
 *
 * 
 * @author  S Luz &#60;luzs@cs.tcd.ie&#62;
 * @version <font size=-1>$Id: $</font>
 * @see  
*/

public class ConcordanceProducer extends Thread {

  BufferedReader in = null;
  PrintWriter out = null;
  TecClientRequest request;
  Dictionary dictionary;
  
  public ConcordanceProducer (Dictionary d, TecClientRequest r){
    super();
    this.dictionary = d;
    this.request = r;
    try {
      PipedWriter pipeOut = new PipedWriter();
      in = new BufferedReader(new PipedReader(pipeOut));
      out = new PrintWriter(pipeOut);
    } catch (IOException e) {
      System.err.println("Concordancer error creating pipe: " + e);
    }
  }
  
  public BufferedReader getBufferedReader (){
    return in;
  }
  
  public void run () {
    try {
      boolean cse = ((String)request.get("case")).equalsIgnoreCase("sensitive");
      WordQuery wquery = 
        new WordQuery((String)request.get("keyword"), dictionary, cse);
      int ctx = 
        (new Integer((String)request.get("context"))).intValue();
      boolean ignx = 
        ((String)request.get("sgml")).equalsIgnoreCase("no")? true : false;
      dictionary.printConcordances(wquery, ctx, ignx, out);
    }
    catch (WordQueryException e){
      out.println(1);
      out.println("ERROR |0|Invalid query: "+request.get("keyword")+e);
    }
  }
}
