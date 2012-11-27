
/**
 *  © 2012 S Luz <luzs@cs.tcd.ie>
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
package org.modnlp.wordleconc;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Dimension;
import java.util.Iterator;

import java.util.Map.Entry;


import cue.lang.Counter;
import cue.lang.WordIterator;
import cue.lang.stop.StopWords;

import processing.core.PApplet;

import modnlp.tec.client.Plugin;
import modnlp.tec.client.ConcordanceBrowser;
import modnlp.tec.client.ConcordanceObject;
import modnlp.tec.client.gui.SubcorpusCaseStatusPanel;
import modnlp.idx.inverted.TokeniserRegex;
import modnlp.idx.inverted.TokeniserJP;
import modnlp.util.Tokeniser;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import wordcram.Word;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JFrame;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import modnlp.Constants;


/**
 *  Display word frequencies for left and right contexts of current concordance
 *
 * @author  S Luz &#60;luzs@acm.org&#62;
 * @version <font size=-1>$Id: $</font>
 * @see  
*/

public class WordleConc  extends JFrame
  implements // Runnable, 
Plugin 
{

  SubcorpusCaseStatusPanel sccsPanel;
  private static String title = new String("MODNLP Plugin: WordleConc v 0.1"); 
  JLabel statsLabel = new JLabel("                            ");
  int left_colour = 0x1B9E77;
  int right_colour = 0xD95F02;
  WordleConcPApplet wapplet;
  JPanel papplet = new JPanel(new BorderLayout());
  private boolean case_sensitive;
  ConcordanceBrowser parent;
  private boolean guiLayoutDone = false;
  private WordleConc thisFrame = null;
  private StopWords cueStopWords = StopWords.English;
  Word[] words;
  JButton dismissButton = new JButton("Quit");
  JButton growTreeButton = new JButton("Show");
  String keyword;

  public WordleConc() {
    super(title);
    thisFrame = this;
    wapplet = new WordleConcPApplet(this);
    wapplet.init();
    //size(900, 700);
  }


  public void setParent(Object p){
    parent = (ConcordanceBrowser)p;
    case_sensitive = parent.isCaseSensitive();
    sccsPanel = new SubcorpusCaseStatusPanel(p);
  }

  public void activate() {
    if (guiLayoutDone){
      setVisible(true);
      return;
    }

    
    dismissButton.addActionListener(new ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
          papplet.remove(wapplet);
          wapplet.dispose();
          wapplet = new WordleConcPApplet(thisFrame);
          wapplet.init();
          papplet.add(wapplet,BorderLayout.CENTER);
          validate();
          growTreeButton.addActionListener(wapplet);
          thisFrame.setVisible(false);
        }});

    growTreeButton.addActionListener(wapplet);

    /*
   growTreeButton.addActionListener(new ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
          //stop();
          //current_action = GROW;
          //start(); 

          //javax.swing.SwingUtilities.invokeLater(new Runnable() {
          //  public void run() {
              papplet.remove(wapplet);
              wapplet.dispose();
              wapplet = new WordleConcPApplet(thisFrame);
              papplet.add(wapplet,BorderLayout.CENTER);
              validate();
              populateWordle();
              //   }
              // });
        }});
    */

    JPanel pas = new JPanel();
    pas.add(growTreeButton);
    pas.add(dismissButton);
    growTreeButton.setEnabled(true);
    
    //progressBar = new JProgressBar(0,800);
    //progressBar.setStringPainted(true);

    JPanel pabottom = new JPanel();

    pabottom.setLayout(new BorderLayout());
    JPanel pa2 = new JPanel(new FlowLayout(FlowLayout.LEFT));
    JPanel pa3 = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    //pa2.add(progressBar);
    pa2.add(statsLabel);
    statsLabel.setSize(450,statsLabel.getHeight());

    pa3.add(sccsPanel);

    pabottom.add(pa2,BorderLayout.WEST);
    pabottom.add(pa3,BorderLayout.EAST);

    
    //wapplet.setup();
    getContentPane().add(pas, BorderLayout.NORTH);
    papplet.setPreferredSize(new Dimension(800, 600));
    papplet.add(wapplet,BorderLayout.CENTER);
    getContentPane().add(papplet, BorderLayout.CENTER);
    getContentPane().add(pabottom, BorderLayout.SOUTH);
   

    pack();
    setVisible(true);
    guiLayoutDone = true;
    //    growTree(); 

  }

  public Word[] populateWordle() {
    Tokeniser ss;
    int la = parent.getLanguage();
    switch (la) {
    case modnlp.Constants.LANG_EN:
      ss = new TokeniserRegex("");
      cueStopWords = StopWords.English;
      break;
    case modnlp.Constants.LANG_JP:
      ss = new TokeniserJP("");
      break;
    default:
      ss = new TokeniserRegex("");
      break;
    }

    StringBuffer lc = new StringBuffer("");
    StringBuffer rc = new StringBuffer("");
    keyword = parent.getKeywordString().toLowerCase();
    int keywdlength = keyword.length();

    for (Iterator<ConcordanceObject> p = parent.getConcordanceVector().iterator(); p.hasNext(); ){
      ConcordanceObject co = p.next();
      if (co == null)
        break;
      lc.append(co.getLeftContext());
      String r = co.getKeywordAndRightContext();
      rc.append(r.substring(r.toLowerCase().indexOf(keyword)+keywdlength));
    }


    Word[] lwa = countWords(lc+"",wapplet.color(255,0,0));//left_colour));
    Word[] rwa = countWords(rc+"",wapplet.color(0,0,0));//right_colour));
    int tlength = lwa.length+rwa.length;
    words = new Word[tlength];

    
    for (int i = 0; i < lwa.length; i++) {
      words[i] = lwa[i];
    }

    //System.out.println("\n---\n");
    for (int i = 0; i < rwa.length; i++) {
      //System.out.println(rwa[i]);
      words[i+lwa.length] = rwa[i];
    } 

    return words;

    // wapplet.setWords(words);

    //wapplet.startLoop();         

    //validate();
    //String[] appletArgs = new String[] { "org.modnlp.WordleConc.WordleConcPApplet" };
    //PApplet.main(appletArgs);

  }

  public String getKeyword(){
    return keyword;
  }


  private Word[] countWords(String text, int colour) {
    Counter<String> counter = new Counter<String>();
    
    for (String word : new WordIterator(text)) {
      // ignore stop words and numbers by default
      if ( ! (cueStopWords != null && cueStopWords.isStopWord(word))
           && !isNumeric(word) ) 
        {
          counter.note(word);
        }
    }
    
    List<Word> words = new ArrayList<Word>();
    
    for (Entry<String, Integer> entry : counter.entrySet()) {
      Word w = new Word(entry.getKey(), (int)entry.getValue());
      w.setColor(colour);
      words.add(w);
    }
    
    return words.toArray(new Word[0]);
  }

  private boolean isNumeric(String word) {
    try {
      Double.parseDouble(word);
      return true;
    }
    catch (NumberFormatException x) {
      return false;
    }
  }



}
