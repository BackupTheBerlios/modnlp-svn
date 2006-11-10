/** 
 * Project: MODNLP/TEC/SERVER, Based on software developed for the
 * Translated English Corpora (TEC) project, from the Dept of Language
 * Engineering - UMIST (DLE-UMIST)
 *

 * Copyright (c) 2006 S.Luz (TCD)
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

import java.io.*;
import java.net.*;
import java.util.StringTokenizer;
import java.util.Hashtable; 
import java.util.Vector; 
import java.util.Enumeration;

/** Streamserver for TEC clients. Initially, load a precompiled
 * serialized hashtable for stroring all the words in the corpus
 * and await client connections. Then deal with requests through
 * the methods described below. 
 * 
 * @author Nino Luz &#60;luzs@acm.org&#62;
 * @version $Id: TecServer.java,v 1.3 2003/06/22 17:55:15 luzs Exp $
 * @see TecCorpusFile 
 * @see TecLogFile 
 * @see TecDicFile
 * @see TecDictionary
 * @see FilePosStr
 */
public class Server extends Thread {

  private static final boolean DEBUG = false;
  private final int MAXCTX = 130;
  private final int MAXEXT = 600;
  private static final int  PORTNUM = 1240;
  // These are feaaults to be chaged in main() 
	private static ServerProperties sprop = new ServerProperties();
  private static String LOGF = sprop.getProperty("log.file");
  
  private static ServerSocket serverSocket;
  private static Dictionary dtab;
  private static TecLogFile logf;
  //private Connection conn;
  //private CategTextHandler handler;
	
	
  /** Initialize the server
   */
  public Server() {
    super("Server");
    try {
      System.out.println("socket open");
      serverSocket = new ServerSocket(PORTNUM);
      System.out.println("socket open");
      dtab = new Dictionary();
      // separate method for initLogFile  to catch different IOException
      initLogFile();
      logf.logMsg("TEC Server accepting connections on port # "+PORTNUM);
      setLogDebug();
      // startSQLConnect();
    }
    catch (FileNotFoundException e) {
      logf.logMsg("TecServer: couldn't find file "+e);
      System.exit(1);
    }
    catch (IOException e) {
      System.err.println("TecServer: couldn't create socket"+e);
      System.exit(1);
    }
  }

  public void run() {
    try {
    while (true)
	{
	    new TecConnection( serverSocket.accept(), dtab, logf);
	}
    }
    catch (IOException e) {
      logf.logMsg("TecServer: couldn't create socket"+e);
    }
    catch (Exception e){
	System.err.println("Usage: DocumentStats CORPUS_LIST");
	e.printStackTrace();
    }
  }

  
  /** Initialize the logfile for this server
   */
  public void initLogFile() {
    try{
      logf    = new TecLogFile(LOGF);
    }
    catch (IOException e){
      System.err.println("initLogFile: " + e);
      System.exit(-1);
    }
  }

  private void setLogDebug() {
    if (DEBUG)
      logf.debugOn = true;
    else 
      logf.debugOn = false;
  }
	
  /** Start a server to listen for connection on
   *  <code>PORTNUM</code>
   * @param arg[0]  The (serialized object) dictionary
   *                file to be used by the server
   */
  public static void main(String[] arg) throws IOException {
    System.out.println("hey");
    Server server = new Server();
    //server.setDaemon(true);
    server.start();
  }
	
}


