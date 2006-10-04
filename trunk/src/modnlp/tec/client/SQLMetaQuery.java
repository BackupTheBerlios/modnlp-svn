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
import java.util.TreeSet;
/**
 *  This is a Client to interact with the
 *  <a href="../../Server/index.html">TEC  Server</a>
 *  It handles responses from the SQL database and formats them as <code>String Vectors</code>

 * @author  S Luz &#60;luzs@acm.org&#62;
 * @version <font size=-1>$Id: SQLMetaQuery.java,v 1.1 2003/06/22 13:48:05 luzs Exp $</font>
 * @see  TecServer
 * @see  Browser
 * @see  AdvConcSearch
*/
public class SQLMetaQuery 
{

  // guarantees that the response is sorted
  public TreeSet response = new TreeSet(); 

  private Socket  socket;
  private PrintStream output;
  private BufferedReader input;
  //DataInputStream input;

  /** Create an "extract thread" with its own socket
   *  through which <code>request</code> will be sent to <code>TecServer</code>
   */
  public SQLMetaQuery(String server, int portnum, TecClientRequest rq){
    try {
      URL exturl = new URL(rq.toString());
      HttpURLConnection exturlConnection = (HttpURLConnection) exturl.openConnection();
      //exturlConnection.setUseCaches(false);
      exturlConnection.setRequestMethod("GET");
			input = new
				BufferedReader(new
                       InputStreamReader(exturlConnection.getInputStream() ));
      //input = new DataInputStream(socket.getInputStream());
      String textLine;
      while (( textLine = input.readLine()) != null)
        response.add(textLine);
      
    }
    catch(IOException e)
      {
				System.err.println("Exception: couldn't create stream socket"+e);
				System.exit(1);
      }
  }

}
