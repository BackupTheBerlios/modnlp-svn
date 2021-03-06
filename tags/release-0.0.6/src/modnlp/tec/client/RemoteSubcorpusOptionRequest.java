/**
 *  � 2007 S Luz <luzs@cs.tcd.ie>
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

import modnlp.Constants;
import modnlp.idx.database.DictProperties;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
/**
 *  
 *
 * @author  S Luz &#60;luzs@cs.tcd.ie&#62;
 * @version <font size=-1>$Id: $</font>
 * @see  
*/

public class RemoteSubcorpusOptionRequest {

  private String server;
  private int portnum;

  public RemoteSubcorpusOptionRequest(String s, int p){
    server = s;
    portnum = p;
  }

  public String[] getAttributeChooserSpecs() throws Exception{
    TecClientRequest request = new TecClientRequest();
    request.put("request","attchooserspecs");
    request.setServerURL("http://"+server);
    request.setServerPORT(portnum);
    request.setServerProgramPath("/attchooser");
    URL exturl = new URL(request.toString());
    
    HttpURLConnection exturlConnection = (HttpURLConnection) exturl.openConnection();
    //exturlConnection.setUseCaches(false);
    exturlConnection.setRequestMethod("GET");
    BufferedReader input = new
      BufferedReader(new
                     InputStreamReader(exturlConnection.getInputStream() ));
    String al = input.readLine();
    return DictProperties.parseAttributeChooserSpecs(al);
  }

  public String[] getOptionSet (String xqatts) throws Exception{
      TecClientRequest request = new TecClientRequest();
      request.put("request","attoptions");
      request.put("xqueryattribs",xqatts);
      request.setServerURL("http://"+server);
      request.setServerPORT(portnum);
      request.setServerProgramPath("/attoptions");
      URL exturl = new URL(request.toString());
      
      HttpURLConnection exturlConnection = (HttpURLConnection) exturl.openConnection();
      //exturlConnection.setUseCaches(false);
      exturlConnection.setRequestMethod("GET");
      BufferedReader input = 
        new BufferedReader(new
                           InputStreamReader(exturlConnection.getInputStream() ));
      String s = input.readLine();
      return s.split(Constants.ATTRIBUTE_OPTION_SEP);
    }

}
