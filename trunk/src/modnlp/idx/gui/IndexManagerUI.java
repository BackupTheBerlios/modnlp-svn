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
package modnlp.idx.gui;

import modnlp.idx.IndexManager;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JTextArea;
import javax.swing.JScrollPane;
import javax.swing.JPanel;
import javax.swing.DefaultComboBoxModel;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import java.io.File;
/**
 *  Main IndexManager frame, from which most of IndexManager is controlled
 *
 * @author  S Luz &#60;luzs@cs.tcd.ie&#62;
 * @version <font size=-1>$Id: $</font>
 * @see  
*/

public class IndexManagerUI extends JFrame 
  implements ActionListener 
{
  IndexManager parent;

  private JTextArea textArea;
  private JButton dismissButton = new JButton("QUIT");
  private JButton loadButton = new JButton("Load Files");
  private JButton newCorpusButton = new JButton("New index");
  private JButton clearButton = new JButton("Clear screen");
  private JButton deindexButton = new JButton("De-index selected files");
  private JList corpusList;
  private String currentDir = null;
  boolean debug = false;

  public IndexManagerUI (IndexManager p) {
   super("Corpus and index manager");
   parent = p;
   textArea = new JTextArea(40,80);
   textArea.setLineWrap(true);
   textArea.setWrapStyleWord(true);
   JScrollPane scrollPane = new JScrollPane(textArea);
   scrollPane.setPreferredSize(new Dimension(600, 300));
   dismissButton.addActionListener(this);
   loadButton.setToolTipText("Select a set of files for indexing");
   loadButton.addActionListener(this);
   newCorpusButton.setToolTipText("Select or create a new location for a corpus index");
   newCorpusButton.addActionListener(this);
   clearButton.setToolTipText("Clear the log window");
   clearButton.addActionListener(this);
    
   JPanel pa = new JPanel();
   pa.add(loadButton);
   pa.add(clearButton);
   //pa.add(validateCheckBox);
   pa.add(dismissButton);
   //pa.add(validateCheckBox);
   getContentPane().add(pa, BorderLayout.NORTH);
   JPanel spa = new JPanel(new BorderLayout());
   spa.add(new JLabel(" Indexing log:"), BorderLayout.NORTH);
   spa.add(scrollPane, BorderLayout.CENTER);
   spa.add(new JLabel("      "), BorderLayout.SOUTH);

   getContentPane().add(spa, BorderLayout.CENTER);

   JPanel lpa = new JPanel(new BorderLayout());
   lpa.add(new JLabel(" Currently indexed files"), BorderLayout.NORTH);
   corpusList = new JList();
   //corpusList.setVisibleRowCount(7);
   JScrollPane clsp = new JScrollPane(corpusList);
   
   clsp.setPreferredSize(new Dimension(600,150));
   lpa.add(clsp, BorderLayout.CENTER);
   lpa.add(deindexButton, BorderLayout.SOUTH);
   getContentPane().add(lpa, BorderLayout.SOUTH);
  }

  public void setCorpusListData(String [] ifn) {
    corpusList.setModel(new DefaultComboBoxModel(ifn));
  }

  public void actionPerformed(ActionEvent evt)
  {
    if(evt.getSource() == dismissButton)
			parent.exit(0);
    else if(evt.getSource() == clearButton)
      textArea.setText(null);
    else if(evt.getSource() == newCorpusButton)
      parent.chooseNewCorpus();
    else if(evt.getSource() == loadButton)
      {
        CorpusFilesChooser filedial = (currentDir == null ) ? 
          new CorpusFilesChooser(): new CorpusFilesChooser(currentDir);
        int returnVal = filedial.showOpenDialog(this);//filedial.showDialog(this, "Select files");
        if (returnVal == filedial.APPROVE_OPTION)
          {
            File[] files = filedial.getSelectedFiles();
            currentDir = files[0].getParent();
            parent.indexSelectedFiles(files);
          }
      }
  }

  public void setCurrentDir(String cd){
    currentDir = cd;
  }

  public void print (String s){
    textArea.append(s);
    if (debug)
      System.err.println(s);    
  }

}
