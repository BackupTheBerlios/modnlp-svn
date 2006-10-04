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
import java.lang.*;
import java.util.Comparator;
import java.util.StringTokenizer;
/**
 *  Compare left hand side of concordance strings (for sorting) 
 *
 * 
 * @author  Saturnino Luz &#60;luzs@acm.org&#62;
 * @version <font size=-1>$Id: LeftComparer.java,v 1.1.1.1 2000/07/07 16:54:36 luz Exp $</font>
 * @see  
*/
public class LeftComparer 
  implements Comparator{

	public int sortContextHorizon = 1;
	public int halfConcordance = 65;

  public LeftComparer(int ctx, int half){
		sortContextHorizon = ctx;
		halfConcordance = half;
	}

  public int compare(Object o1, Object o2) {

    ConcordanceObject coa = (ConcordanceObject) o1;
    ConcordanceObject cob = (ConcordanceObject) o2;
    StringBuffer ab = 
			new StringBuffer(coa.concordance.substring(0,halfConcordance));
		StringBuffer bb = 
			new StringBuffer(cob.concordance.substring(0,halfConcordance));
		TecTokenizer at = new TecTokenizer(ab.reverse().toString(),
																						 ConcordanceObject.SEPTOKEN,
																						 false);
		TecTokenizer bt = new TecTokenizer(bb.reverse().toString(),
																						 ConcordanceObject.SEPTOKEN,
																						 false);
		StringBuffer ac = new StringBuffer("");
		StringBuffer bc = new StringBuffer("");
		// Read up to horizon
		for (int i = 1; i < sortContextHorizon; i++) {
			StringBuffer t = new StringBuffer(at.safeNextToken());
			ac.append(t.reverse().toString()+" ");
			t = new StringBuffer(bt.safeNextToken());
			bc.append(t.reverse().toString()+" ");
		}
		// Insert horizon word
		StringBuffer t = new StringBuffer(at.safeNextToken());
		ac.insert(0, t.reverse().toString()+" ");
		t = new StringBuffer(bt.safeNextToken());
		bc.insert(0, t.reverse().toString()+" ");

		return ac.toString().toLowerCase().compareTo(bc.toString().toLowerCase());

  }


}
