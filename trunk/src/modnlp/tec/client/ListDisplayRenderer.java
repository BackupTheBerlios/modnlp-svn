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
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.border.*;

import java.awt.Component;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.FontMetrics;


/**
 *  Cell renderer for TEC concordance list
 *
 * 
 * @author  Saturnino Luz &#60;luzs@acm.org&#62;
 * @version <font size=-1>$Id: ListDisplayRenderer.java,v 1.1.1.1 2000/07/07 16:54:36 luz Exp $</font>
 * @see  
*/
public class ListDisplayRenderer extends JLabel 
  implements ListCellRenderer {//, ListSelectionListener  {

	private ConcordanceObject cobjct;
  private static final int MAXFNSIZE = 12;

  public ListDisplayRenderer() {
		super();
		setOpaque(true);
		setHorizontalAlignment(LEFT);
  }

  public Component getListCellRendererComponent(JList list,
                                                Object value,
                                                int index,
                                                boolean isSelected,
                                                boolean cellHasFocus) {


    if (isSelected) {
      setBackground(list.getSelectionBackground());
      setForeground(list.getSelectionForeground());
    } else {
      setBackground(list.getBackground());
      setForeground(list.getForeground());
     }
    cobjct = (ConcordanceObject)value;
		int linesize;
		if (cobjct == null){
			setText(" ");
		}
		else {
			setText(cobjct.textConcLine(MAXFNSIZE));
		}
		return this;
  }
  
	public void paintComponent(Graphics g){
		if (cobjct == null){
			return;
		}
		String concordance = cobjct.concordance;
		String filename = cobjct.sfilename;
		super.paintComponent(g);
		FontMetrics fm = getFontMetrics(getFont());
		// highlight filename
		g.setColor(Color.red.darker());
		g.drawString(filename, 0, fm.getAscent());
		//System.out.println("horizon->"+cobjct.sortContextHorizon+"<-");
		// highlight keyword
		g.setColor(Color.green.darker());
		HighlightString hls = cobjct.indexOfKeyword();
		g.drawString(hls.string, 
								 fm.stringWidth(filename+concordance.substring(0, hls.position)),
								 fm.getAscent());
		// highlight sort keyword (if needed)
		if ( cobjct.sortContextHorizon != 0) {
			g.setColor(Color.blue.darker());
	 		hls = cobjct.indexOfSortContext();
			g.drawString(hls.string, 
									 fm.stringWidth(filename+concordance.substring(0, hls.position)),
									 fm.getAscent());
		}
	}
	
}












