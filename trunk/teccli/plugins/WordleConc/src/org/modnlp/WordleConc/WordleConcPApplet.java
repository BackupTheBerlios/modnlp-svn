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
import java.awt.Image; 
import java.io.*; 
import java.net.*; 
import java.text.*; 
import java.util.*; 
import java.util.zip.*; 
import java.util.regex.*; 

public class WordleConcPApplet extends PApplet
{

WordCram wc;
Word[] words;

public void setup() {
  colorMode(HSB);
  size(800, 600);
  background(255);
  
  loadNames();
  makeWordCram();
}

public void loadNames() { 
  String[] nameData = loadStrings("/tmp/names.txt");
  words = new Word[nameData.length];
  for (int i = 0; i < words.length; i++) {
    words[i] = parseName(nameData[i]);
  }
}

public void makeWordCram() {
  wc = new WordCram(this)
    .fromWords(words)
    //.withFont(createFont("../../MINYN___.TTF", 1))
    .sizedByWeight(12, 60)
    .withColorer(colorer());
}

public WordColorer colorer() {
  return new WordColorer() {
    public int colorFor(Word name) {
      boolean isFemale = (Boolean)name.getProperty("isFemale");
      
      if (isFemale) {
        return color(0xfff36d91); // pink
      }
      else {
        return color(0xff476dd5); // blue
      }
    }
  };
}

public void draw() {
  if (wc.hasMore()) {
    wc.drawNext();
  }
  else {
    println("done");
    noLoop();
  }
}

public void mouseClicked() {
  background(255);
  makeWordCram();
  loop();
}


// Each row looks like:
// Mary\t1.0\tf
// ...or:
// {name} {tab} {frequency} {tab} {'f' for females, 'm' for males}
public Word parseName(String data) {
  String[] parts = split(data, '\t');
  
  String name = parts[0];
  float frequency = PApplet.parseFloat(parts[1]);
  boolean isFemale = "f".equals(parts[2]);
  
  Word word = new Word(name, frequency);
  
  word.setProperty("isFemale", isFemale);
  
  return word;
}
  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "org.modnlp.WordleConc.WordleConcPApplet" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}
