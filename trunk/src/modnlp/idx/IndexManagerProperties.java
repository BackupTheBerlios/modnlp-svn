/**
 *  � 2006 S Luz <luzs@cs.tcd.ie>
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
package modnlp.idx;
import java.io.FileInputStream;
import java.io.FileOutputStream;
/**
 *  Properties for IndexManager
 *
 * @author  S Luz &#60;luzs@cs.tcd.ie&#62;
 * @version <font size=-1>$Id: $</font>
 * @see  
*/

public class IndexManagerProperties extends java.util.Properties{

  public static String PROP_FNAME = System.getProperty("user.dir")+
    java.io.File.separator+"idxmgr.properties";

	public IndexManagerProperties () 
	{
    super();
    try {
      ClassLoader cl = this.getClass().getClassLoader();
      //FileInputStream fis = 
        //((cl.getResource(PROP_FNAME))
        //               .openConnection()).getInputStream();
      this.load(new FileInputStream(PROP_FNAME));
    }
    catch (Exception e) {
	    System.err.println("Error reading property file "+PROP_FNAME+": "+e);
      
	    System.err.println("Creating new property file");
      setProperty("last.directory",System.getProperty("user.dir"));
		}
	}


  /**
   * Return the number of corpora currently maintained by the
   * IndexManager
   *
   * @return an <code>int</code> value
   */
  public int getNumberOfCorpora() {
    return new Integer(getProperty("number.of.corpora")).intValue();
  }

  public void save () {
    try {
      store(new FileOutputStream(PROP_FNAME), 
            "modnlp.idx.IndexManager's properties");
    }
    catch (Exception e){
      System.err.println("Error writing property file "+PROP_FNAME+": "+e);
    }
  }
  protected void finalize () throws java.lang.Throwable {
    save();
    super.finalize();
  }

}