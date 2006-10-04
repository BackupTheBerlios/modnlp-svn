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

import javax.swing.JFrame;
import javax.swing.*;
import java.awt.*;
/**
 *  Splash screen (shown at startup time)
 *
 * @author  Saturnino Luz &#60;luzs@acm.org&#62;
 * @version <font size=-1>$Id: SplashScreen.java,v 1.1 2003/08/06 16:58:56 luzs Exp $</font>
 * @see  
*/
public class SplashScreen extends JWindow 
{

  private JProgressBar progressBar;
  private JLabel label;

  public SplashScreen (String message, int nosteps) {
    super();
    windowInit();
    // make title, borders, etc invisible    
    //setUndecorated(true);
    Container cp = getContentPane();
    label = new JLabel(message);
    ClassLoader cl = this.getClass().getClassLoader();
    cp.add( new JLabel(new ImageIcon(cl.getResource("modnlp/tec/client/icons/tec.gif"))), 
            BorderLayout.NORTH);
    
    cp.add(label, BorderLayout.CENTER);
    
    progressBar = new JProgressBar(0, nosteps);
    progressBar.setValue(0);
    progressBar.setStringPainted(true);
    progressBar.setString("");
    cp.add(progressBar, BorderLayout.SOUTH);


    // as of 1.4 we can use this to center the splash screen on the window
    // setLocationRelativeTo(null);

    pack();
    setVisible(true);

  }

  public void setProgress( int step ){
    progressBar.setValue(step);
  }

  public void incProgress( ){
    setProgress(progressBar.getValue()+1);
  }

  public void setMessage(String message){
    label.setText(message);
  }

  public void dismiss( ){
    setProgress(progressBar.getMaximum());
    dispose();
  }



}
