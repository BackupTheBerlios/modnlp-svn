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
import java.io.*;
import java.net.*;
import java.util.Properties;
/**
 *  Encapsulate client-related defaults 
 *
 * @author  Saturnino Luz &#60;luzs@acm.org&#62;
 * @version <font size=-1>$Id: ClientProperties.java,v 1.2 2001/07/31 16:18:53 luzs Exp $</font>
 * @see  
*/
public class ClientProperties extends Properties{

	public ClientProperties () 
	{
    super();
    try {
      ClassLoader cl = this.getClass().getClassLoader();
      InputStream fis = ((cl.getResource("tecli.properties")).openConnection()).getInputStream();
       //FileInputStream fis = new FileInputStream(new File("tecli.properties"));
      this.load(fis);
    }
    catch (Exception e) {
	    System.err.println("Property ERROR: " + e);
	    e.printStackTrace(System.out);
			System.exit(1);
		}
	}

}
