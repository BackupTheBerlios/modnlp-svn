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
package modnlp.idx.database;

import com.sleepycat.bind.tuple.IntegerBinding;
import com.sleepycat.je.DatabaseEntry;
import java.util.Comparator;
/**
 *  Compare two integers in descending order
 *
 * @author  S Luz &#60;luzs@cs.tcd.ie&#62;
 * @version <font size=-1>$Id: DescIntComparator.java,v 1.1 2006/05/22 17:26:02 amaral Exp $</font>
 * @see  
*/

public class DescIntComparator implements Comparator{

  /**
   * Compare two objects for sorting in descending order.  Should
   * return negative if d2 < d1, positive if d2 > d1, and 0
   * otherwise.
   *
   * @param d1 an <code>Object</code> value
   * @param d2 an <code>Object</code> value
   * @return an <code>int</code> value
   */
  public int compare(Object d1, Object d2) {
    /*
    String s1 = null; String s2 = null;
      byte[] b1 = (byte[])d1;
        byte[] b2 = (byte[])d2;
        
        s1 = new DatabaseEntry(b1);
        s2 = new DatabaseEntry(b2);
    */
        
    int a = IntegerBinding.entryToInt( new DatabaseEntry((byte[])d1));
    int b = IntegerBinding.entryToInt( new DatabaseEntry((byte[])d2));

    return (b - a);
    
  }


}
