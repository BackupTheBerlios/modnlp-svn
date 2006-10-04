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
//import java.applet.*;
import java.awt.event.*;
import java.awt.*;
import java.lang.*;

/**
 *  This applet calls a Client to interact with the 
 *  <a href="../../Server/index.html">TEC  Server</a>
 * 
 * @author  S. Luz &#60;luzs@acm.org&#62;
 * @version <font size=-1>$Id: TecApplet.java,v 1.1.1.1 2000/07/07 16:54:36 luz Exp $</font>
 * @see  ContextClient
 * @see  Browser
*/
public class TecApplet extends JApplet 
	implements ActionListener{


  //Browser f = null;
	JFrame f = null;
  String headerURL = null;
  String serverLocation = null;

  public void init(){
    setBackground(Color.lightGray);
    JPanel p = new JPanel();
    JButton b = new JButton("TEC Browser");
    p.add(b);
    p.setFont(new Font("Helvetica",Font.PLAIN, 12));
    getContentPane().add(p);
    p.setBackground(Color.lightGray);
    headerURL = getParameter("headerURL");
    serverLocation = getParameter("serverLocation");
    //System.err.println(headerURL+serverLocation);
		b.addActionListener(this);
  }
 

    public void actionPerformed(ActionEvent evt)
  {

    if(evt.getSource() instanceof JButton)
			{  
				if (f == null){
					System.err.println(headerURL+serverLocation);
					f = new Browser(800,600);
					/* FullTextWindow("ssss");
					if (headerURL != null)
						f.HEDBAS = headerURL;
					if (serverLocation != null)
						f.SERVER = serverLocation;
					*/
					//Thread t = new Thread(f);
					//t.start();
					//f.pack();
					f.setVisible(true);
					/*
					f.leftSortCtx.setEditable(true);
					f.rightSortCtx.setEditable(true);
					*/
				}
				else 
					f.setVisible(true);
			}
  }
	
  //public void finalize(){
  //  
  //}
}

