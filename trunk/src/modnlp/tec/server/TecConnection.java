/** 
 * Project: MODNLP/TEC/SERVER, Based on software developed for the
 * Translated English Corpora (TEC) project, from the Dept of Language
 * Engineering - UMIST (DLE-UMIST)
 *

 * Copyright (c) 2006 S.Luz (TCD)
 *           (with contributions by Noel Skehan)
 *           (c) 1998 S.Luz (DLE-UMIST) 
 *           All Rights Reserved.
 *
 *   This program is free software; you can redistribute it and/or
 *   modify it under the terms of the GNU General Public License
 *   as published by the Free Software Foundation; either version 2
 *   of the License, or (at your option) any later version.
 *   
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
     
 *   You should have received a copy of the GNU General Public License
 *   along with this program; if not, write to the Free Software
 *   Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 *
 *     
 */ 
package modnlp.tec.server;

import modnlp.idx.database.Dictionary;
import modnlp.idx.query.WordQuery;
import modnlp.idx.query.WordQueryException;

import java.io.*;
import java.net.*;
import java.lang.*;
import java.util.StringTokenizer;
import java.util.Hashtable; 
import java.util.Vector; 
import java.util.Enumeration;
import java.util.StringTokenizer;

/** Deal with client's requests for concordance and extracts through
 * the methods described below. 
 * 
 * @author Nino Luz &#60;luzs@acm.org&#62;
 * @version $Id: TecConnection.java,v 1.6 2003/08/08 18:40:02 luzs Exp $
 * @see TecServer
 * @see TecCorpusFile 
 * @see TecLogFile 
 * @see Dictionary
 * @see FilePosStr
 */
public class TecConnection extends Thread {

  private final int MAXCTX = 130;
  private final int MAXEXT = 600;
  Socket  cSokt = null;
  Dictionary dtab;
  TecLogFile logf;
  String sqlquery = "";

  /** Initialize a new connection thread
   */
  public TecConnection(Socket s, Dictionary d, TecLogFile f) {
    cSokt = s;
    dtab = d;
    logf = f;
    setPriority(NORM_PRIORITY - 1);
    start();
  }

  /** Wait for connections (forever) parse clients requests
   *  and trigger the appropriate actions
   */
  public void run() {

    BufferedReader is = null; 
    PrintWriter os = null;
    //SQLConnection sc;
		InetAddress inaddrr;

		try {
			os = new PrintWriter
				(new BufferedOutputStream(cSokt.getOutputStream()));
			is = new BufferedReader
				(new InputStreamReader(cSokt.getInputStream()));
			//sc = new SQLConnection();
      inaddrr = cSokt.getInetAddress();
      String inLine, outLine;
      if ((inLine = is.readLine()) != null) 
        {		    
          logf.logMsg("->"+inLine+"<-");
          processInput(inLine, os);	    
          logf.logMsg("["+inaddrr.getHostName()+"] "+inLine);
          os.println("");
          //os.println("____FINISHED___");
          os.flush(); 
        }
      // Cleanup
      is.close();
      os.close();
      cleanUp(cSokt);
    }
		catch (WordQueryException e) {
      logf.logMsg("doConcordance: Malformed query: |"+e.getOriginalQuery());
			os.println(-1);
			os.println("Error: Malformed query:"+e.getOriginalQuery());
			os.println("");
			os.flush();
			cleanUp(cSokt);
		}
		catch (IOException e) {
      logf.logMsg("TecConnection: Connection lost to client socket "+e);
      cleanUp(cSokt);
			return;
		}	
		catch (Exception e) {
      logf.logMsg("TecConnection: serious exception caught: " + e);
			e.printStackTrace();
			cleanUp(cSokt);
		}
  }
	
  /** Close client socket 
   * @param cls   the socket to be closed 
   */
  private void cleanUp (Socket cls)  {
    try {
      //System.out.println("cleaning up ...");
      cls.close();
    }
    catch (IOException e) {
      logf.logMsg("TecConnection: error closing Socket "+e);
    }
  }
	
  /** Select a request and perform appropriate action
   * @param inStr  A string containing the clients request 
   *               in a CGI-like format to be converted into
   *               a <code>Request</code>
   * @param os     the output stream (to be received by client)
   * @see Request
   */
  private void processInput(String inStr, PrintWriter os) 
    throws IOException
  {
    String outStr = null;
    Request req = new Request(inStr);
    switch (req.typeOfRequest())
      {
      case Request.CONCORD:
        doConcordance(req, os);
        break;
      case Request.EXTRACT:
        getExtract(req, os);
        break;
      case Request.FREQLIST:
        getFreqList(req, os);
        break;
      case Request.HEADERBASEURL:
        getHeaderBaseURL(os);
        break;
      default:
        logf.logMsg("TecServ: couldn't understand req "+
                    req.get("request")+req.typeOfRequest());
        break;
      }
  }
	
	
  /** Retrieve each line containing a concordance for
   *  a given keyword
   *
   * @param req    A pre-parsed client request (key-value pairs)
   * @param os     the output stream (to be received by client)
   * @see Request
   * @see TecCorpusFile
   */
  private void doConcordance(Request req, PrintWriter os) 
		throws IOException
  {
    WordQuery wquery = null;
    try {
      boolean cse = ((String)req.get("case")).equalsIgnoreCase("sensitive");
      wquery = new WordQuery ((String) req.get("keyword"), dtab, cse);    
      int ctx = getSafeInteger((String)req.get("context"),MAXCTX).intValue();
      boolean ignx = 
        ((String)req.get("sgml")).equalsIgnoreCase("no")? true : false;
      dtab.printCorcordances(wquery, ctx, ignx, os);
    }
    catch (WordQueryException e) {
      logf.logMsg("doConcordance: Malformed query: |"+wquery+"|"+e);
			os.println(-1);
      os.println("Malformed query: "+wquery);
      os.flush();
    }
    catch (NullPointerException e) {
      e.printStackTrace();
      logf.logMsg("doConcordance: word not found: |"+wquery+"|"+e);
			os.println(-1);
			os.println("Server error retrieving concordance of "+wquery);
      os.flush();
    }
  }
	
  /** Retrieve a bit of text surrounding a given keyword
   *  
   * @param req    A pre-parsed client request (key-value pairs)
   * @param os     the output stream (to be received by client)
   * @see Request
   * @see TecCorpusFile
   */
  public void  getExtract(Request req,  PrintWriter os)
  {
    String fn = null;
    try {
    int ctx = getSafeInteger((String)req.get("context"),MAXEXT).intValue();
    fn  = (String)req.get("filename");
    long  bp  = new Integer((String) req.get("position")).longValue();
    boolean ignx = ((String)req.get("sgml")).equalsIgnoreCase("no")? true : false;
    //logf.logMsg("Reading "+fn);
    String textLine = 
      dtab.getExtract((String)req.get("filename"),
                            ctx, bp, ignx);
    os.println(textLine);
    }
    catch (Exception e) {
      logf.logMsg("doConcordance: error reading corpus "+e);
    }
  }

  public void getHeaderBaseURL(PrintWriter os){
    System.err.println(dtab.getDictProps().getProperty("headers.url"));
    os.println(dtab.getDictProps().getProperty("headers.url"));
    os.flush();
  }

  /** Retrieve a (case-insensitive) frequency list
   *  
   * @param req    A pre-parsed client request (key-value pairs)
   * @param os     the output stream (to be received by client)
   * @see Request
   * @see TecCorpusFile
   */
  public void  getFreqList(Request req,  PrintWriter os)
  {
    // we'll use req in the future to get freq by sub-corpora etc
    dtab.printSortedFreqList(os);
  }

  // utilities for internal use 
  private Integer getSafeInteger(String inp, int max){
    try {
      Integer ctx = new Integer(inp);
      if (ctx.intValue() > max) // check context limiti
				ctx = new Integer(max);
      return ctx;
    }
    catch (NumberFormatException e){
      Integer ctx = new Integer(max);
      return ctx;
    }
  }
	
}


