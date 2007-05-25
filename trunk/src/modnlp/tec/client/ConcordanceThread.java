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

import java.net.*;
import java.io.*;
import javax.swing.SwingUtilities;


/**
 *  Receive reply from server (list of concondances)
 *  and display them from time to time. This class should
 *  in the future be an extension of ListDisplay and so its
 *  updates by itself (thus its design as a class that implements
 *  <code>Runnable</code> rather than extends
 *  <code>java.lang.Thread</code> .
 *
 * @author  S. Luz &#60;luzs@acm.org&#62;
 * @version <font size=-1>$Id: ConcordanceThread.java,v 1.5 2003/08/06 16:58:56 luzs Exp $</font>
 * @see
*/
public class ConcordanceThread
	implements Runnable, ConcordanceMonitor{

  private Thread concThread;
  // SL: all these public vars are a real mess! This class must be
  // fixed urgently (though we have much more interesting stuff to do
  // than fixing legacy code)
  /**  This variable sets where we should start showing the list */
  public int updateThreshold = 40;
  public Browser parent;
  private String commandToServer = null;
	/** default port; normally reset by Browser */
  public int noFound = 0;
  public int noActuallyFound = 0;
  public ConcArray conc = new ConcArray();
  public int ctRead;
	boolean stop = false;
	public boolean serverResponded = false;
	private ConcordanceDisplayListener concList = null;
	private TecClientRequest request = null;

  Socket  socket = null;
  PrintStream output;
  BufferedReader input = null;
  //DataInputStream input;



  /** Starts a new connection and performs a request to
   *  be displayed in the <code>ListDisplay</code> provided.
   *
   * @param server  The default server to which we'll connect
   * @param upd     The ListDisplay to update
   * @param ch      The request to be passed on to <code> server</code>
   */
  public ConcordanceThread(Browser pa, TecClientRequest cr)
	{
		request = cr;
    parent = pa;
    serverResponded = false;
    // initConcordanceThread();
	}

  public ConcordanceThread(Browser pa, BufferedReader in, TecClientRequest cr)
	{
		input = in;
    parent = pa;
		request = cr;
    serverResponded = false;
    // initConcordanceThread();
	}

  /** Starts a new connection and performs a request to
   *  be displayed in the <code>ListDisplay</code> provided.
   *
   * @param server  The default server to which we'll connect
   * @param upd     The ListDisplay to update
   * @param ch      The request to be passed on to <code> server</code>
   */
  private void initConcordanceThread( )
	{
		try {
			noFound = -1;
      if (input == null) {
        //socket = new Socket(InetAddress.getByName(SERVER), PORTNUM);
        URL concurl = new URL( request.toString());
        HttpURLConnection concurlConnection =
          (HttpURLConnection) concurl.openConnection();
        concurlConnection.setUseCaches(false);
        concurlConnection.setRequestMethod("GET");
        concurlConnection.connect();  
        input = new
          BufferedReader(new
                         InputStreamReader(concurlConnection.getInputStream() ));
      }
      String temp = input.readLine();
      serverResponded = true;
      noFound = (new Integer(temp)).intValue();
      System.out.println("____noFound_____"+noFound);
      if (noFound < 0)
				{
					String mes = input.readLine();
					System.out.println("________"+mes);
					ctRead = noFound;
					parent.updateStatusLabel(mes);
					stop();
				}
		}
		catch (java.io.IOException e){
			parent.updateStatusLabel("  **** TRANSFER INTERRUPTED **** "+e);
			stop();
		}
		catch (Exception e){
      e.printStackTrace();
			stop();
		}
  }

  public void run() {
    boolean fshow = false;
    boolean notru = true;
		stop = false;
    ctRead = 0;
    try {
      initConcordanceThread();
      String concordance = null;
      int ctsz = request.getContextSize();
      //System.out.println("_________context size: "+ctsz);
      while (!stop && 
						 ctRead < noFound && 
						 (concordance = input.readLine()) != null &&
						 conc.assertElement(concordance, 0,  ctsz ) )
				{
					//System.err.println("Found---: "+noFound+" Read---:"+ctRead);
					//System.err.println("conc="+concordance);
					ctRead++;
					if ( conc.index > updateThreshold || ctRead >= noFound)
						if ( !fshow )
							{
								fireDisplayEvent(0);
                parent.displayConcord();
								//parent.concList.displayArraySegment(conc, 0);
								fshow = true;
							}
				}
      noActuallyFound =  (ctRead == ConcArray.arraymax) ? noFound : ctRead;
      noFound = ctRead;
			if ( !fshow )
				{
					fireDisplayEvent(0);
					//parent.concList.displayArraySegment(conc, 0);
          parent.displayConcord();
					fshow = true;
				}
			if (noFound > 0) {
				parent.updateStatusLabel(" Returned "+
                                 noActuallyFound+
                                 " lines matching your query");
				fireListSizeEvent(noFound);
			}
      stop();
    }
    catch (java.io.IOException e){
      parent.updateStatusLabel("  **** TRANSFER INTERRUPTED **** "+e);
			if (noFound > 0) {
        noActuallyFound =  (ctRead == ConcArray.arraymax) ? noFound : ctRead;
        noFound = ctRead;
				parent.updateStatusLabel(" Returned "+
                                 noActuallyFound+
                                 " lines matching your query");
				fireListSizeEvent(noFound);
        fireDisplayEvent(0);
        parent.displayConcord();
      }
      stop();
    }
    catch (NullPointerException e){
      parent.updateStatusLabel("  ConcordanceThread error: "+e);
      fireDisplayEvent(0);
      parent.displayConcord();
      stop();
    }
  }

  public void start(){
    if ( concThread == null ){
      concThread = new Thread(this);
      concThread.start();
    }
  }

  public void stop() {
		if (concThread == null)
			return;
		concThread = null;
    try{
      if (output != null){
        output.flush();
        output.close();
      }
      if (input != null) 
        input.close();
      if (socket != null) 
        socket.close();
			input = null;
			output = null;
			socket = null;
    }
    catch (java.lang.Exception e){
      System.err.println("Error Stopping thread "+e);
    }
  }

	public boolean atWork() {
    if ( concThread != null )
			return true;
		else
			return false;
	}

	public ConcArray getConcArray () {
		return conc;
	}
	public int getNoFound () {
		return noFound;
	}

	public void fireDisplayEvent (int from) {
		if (concList != null)
			concList.concordanceChanged(new ConcordanceDisplayEvent(this, from));
	}
	public void fireListSizeEvent (int size)
  {
		if (concList != null)
			concList.concordanceChanged(new ConcordanceListSizeEvent(this, size));
	}

	/* Implement ConcordanceMonitor */

	public void addConcordanceDisplayListener(ConcordanceDisplayListener conc)
  {
		concList = conc;
  }

  public void removeConcordanceDisplayListener(ConcordanceDisplayListener conc)
  {
		concList = null;
  }
}
