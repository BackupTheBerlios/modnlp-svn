package org.modnlp.WordleConc;

import processing.core.*; 
import processing.data.*; 
import processing.opengl.*; 

import wordcram.*; 

import java.applet.*; 
import java.awt.Dimension; 
import java.awt.Frame; 
import java.awt.event.MouseEvent; 
import java.awt.event.KeyEvent; 
import java.awt.event.FocusEvent; 
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.Image; 
import java.io.*; 
import java.net.*; 
import java.text.*; 
import java.util.*; 
import java.util.zip.*; 
import java.util.regex.*; 

public class WordleConcPApplet extends PApplet implements ActionListener{


  WordCram wc;
  Word[] words;
  int drawcount = 0;
  WordleConc parent;
  boolean waiting = true;

  public WordleConcPApplet(WordleConc p){
    super();
    this.parent = p;
  }
  
  public void setup() {
    size(800, 600);
    noLoop();
    background(255);
    drawcount = 0;
    this.setWords(parent.populateWordle());
    
    //frame.setResizable(true);
  }

  public void startLoop(){
    background(255);
    redraw();
    loop();
    waiting = false;
  }

  public void setWords(Word[] w){
    words = w;
    wc = new WordCram(this).fromWords(words); //.sizedByWeight(12, 60);
  }

  
  public void draw() {
    if (wc == null || waiting){
      noLoop();
      return;
    }

    if (wc.hasMore()) {
      wc.drawNext();
      print(","+drawcount++);
    }
    else {
      println("=====done=====");
      noLoop();
      Word[] w = wc.getWords();
      for (int i =0; i < w.length; i++)
        System.out.println(w[i]);
    }
  }
  
  public void actionPerformed(ActionEvent evt) {
    if (evt.getActionCommand().equals("Show")) {
      println("=====start loop===");
      startLoop();
      redraw();
    } else {
      println("actionPerformed(): can't handle " +evt.getActionCommand());
    }
  }


  /*public void mouseClicked() {
    background(255);
    redraw();
    loop();
    }*/
  
  

  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "org.modnlp.WordleConc.WordleConcPApplet" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}

