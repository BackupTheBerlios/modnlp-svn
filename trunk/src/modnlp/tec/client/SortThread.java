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
import java.util.*;
/**
 *  Receive reply from server (list of concondances) 
 *  and display them from time to time. This class should
 *  in the future be an extension of ListDisplay and so its
 *  updates by itself (thus its design as a class that implements
 *  <code>Runnable</code> rather than extends 
 *  <code>java.lang.Thread</code> .
 * 
 * @author  S. Luz &#60;luzs@acm.org&#62;
 * @version <font size=-1>$Id: SortThread.java,v 1.1.1.1 2000/07/07 16:54:36 luz Exp $</font>
 * @see  
*/
public class SortThread  
	implements Runnable, ConcordanceMonitor{


		public Thread thread = null;
		public Object[] array;
		public int fromIndex;
		public int toIndex;
		public Comparator comparator;
		private ConcordanceDisplayListener concList = null; 
		
		public SortThread(Object[] a, int from,
											int to, Comparator c) 
			{
				array = a;
				fromIndex = from;
				toIndex = to;
				comparator = c;
				
			}  
		
		public void run() 
			{
				
				Arrays.sort(array,
										fromIndex,
										toIndex, 
										comparator);		
				long e = (new Date()).getTime();
				//System.out.println("End Time---->:"+e);
				fireDisplayEvent(0);
				stop();
			}
		
		public void start(){
			if ( thread == null ){
				thread = new Thread(this);
				thread.start();
			}
		}
		
		public void stop() {
      //System.err.println("\nStopping thread here\n");    
			thread = null;
		}
		
		public boolean atWork() {
			if ( thread != null )
				return true;
			else 
				return false;
		}


		public void fireDisplayEvent (int from) {
			if (concList != null)
				concList.concordanceChanged(new ConcordanceDisplayEvent(this, from));
		}
		
		/* Implement ConcordanceMonitor */
		
		public void addConcordanceDisplayListener(ConcordanceDisplayListener conc){
			concList = conc;
		}
		
		public void removeConcordanceDisplayListener(ConcordanceDisplayListener conc){
			concList = null;
  }
		

}
