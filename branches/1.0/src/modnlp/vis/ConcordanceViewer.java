/**
 *  © 2008 S Luz <luzs@acm.org>
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
package modnlp.vis;

import modnlp.vis.component.ConcordanceTree;

import modnlp.idx.inverted.StringSplitter;
import modnlp.tec.client.ConcordanceObject;
import modnlp.tec.client.ConcordanceVector;
import modnlp.tec.client.Upload;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.MouseEvent;

import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.swing.SwingConstants;
import javax.swing.JTextField;
import javax.swing.JFileChooser;

import java.util.Vector;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;

import prefuse.Constants;
import prefuse.controls.ControlAdapter;
import prefuse.data.Table;
import prefuse.data.Tree;
import prefuse.data.Tuple;
import prefuse.data.Node;
import prefuse.data.Edge;
import prefuse.data.SpanningTree;
import prefuse.util.ui.JFastLabel;
import prefuse.util.FontLib;
import prefuse.visual.VisualItem;




/**
 *  Basic concordance tree generator. A concordance tree is a prefix
 *  tree (trie) encoding the context to the right of a concordance; or a
 *  (i.e. a right-to-left prefix tree) encoding the left
 *  context of a concordance
 *
 * @author  S Luz &#60;luzs@acm.org&#62;
 * @version <font size=-1>$Id: $</font>
 * @see  
*/

public class ConcordanceViewer extends JFrame
  implements Runnable
{
  
  //String tknregexp  = "\\{L}[\\p{L}-.]*'?s?";

  public static final int GROW = 1;
  public static final int PRUNE = 2;

  private int current_action = GROW;
  private double prune_cutoff = 0.05;

  private boolean case_sensitive = false;
  /**
   * Describe left_context here.
   */
  private boolean left_context = false;

  private Thread thread;
  private JFrame thisFrame = null;
  private Tree tree = null;


  private JProgressBar progressBar;

  JPanel tpanel = new JPanel(new BorderLayout());

  private ConcordanceVector concVector =  null;
  private ConcordanceTree conc_tree =  null;

  private static String title = new String("ConcordanceViewer 0.1"); 
  private boolean guiLayoutDone = false;



  public ConcordanceViewer() {
    super(title);
    thisFrame = this;
  }

  // plugin interface method
  public void activate() {
    if (guiLayoutDone){
      setVisible(true);
      return;
    }

    JButton dismissButton = new JButton("Quit");
    JButton growTreeButton = new JButton("Grow tree ->");
    JButton growTreeLeftButton = new JButton("<- Grow tree");
    JButton uldButton = new JButton("Read concordances...");
    
    dismissButton.addActionListener(new ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
          thisFrame.setVisible(false);
          System.exit(0);
        }});

    growTreeButton.addActionListener(new ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
          stop();
          setLeftContext(false);
          current_action = GROW;
          start(); 
          }});

    growTreeLeftButton.
      addActionListener(new ActionListener() {
          public void actionPerformed(java.awt.event.ActionEvent evt) {
            setLeftContext(true);
            current_action = GROW;
            stop(); start();
          }});



    JPanel cop = new JPanel();
    cop.setBorder(javax.swing.BorderFactory.createEtchedBorder());
    final JTextField cutoff = new JTextField("0.05", 4);
    JButton pruneTreeButton = new JButton("Prune tree");
    cop.add(pruneTreeButton);
    cop.add(cutoff);

    pruneTreeButton.
      addActionListener(new ActionListener() {
          public void actionPerformed(java.awt.event.ActionEvent evt) {
            setPruneCutoff((new Double(cutoff.getText())).doubleValue());
            current_action = PRUNE;
            stop(); start();
          }});

    uldButton.addActionListener(new ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
          try
            {
              JFileChooser filedial = new JFileChooser();
              int returnVal = filedial.showDialog(null, "Read concordances from...");
              if (returnVal == JFileChooser.APPROVE_OPTION)
                {
                  File file = filedial.getSelectedFile();
                  //System.out.println(file.getName());
                  Upload ulf =
                    new Upload(file);
                  ulf.readConcordances();
                  concVector = ulf.getConcordanceVector();
                }
            }
          catch (java.io.IOException e){
            alertWindow("Error downloading concordances\n!"+e);
          }}}
      );



    JPanel pas = new JPanel();
    pas.add(uldButton);
    pas.add(growTreeLeftButton);
    pas.add(growTreeButton);
    pas.add(dismissButton);
    pas.add(cop);

    //getContentPane().add(pan, BorderLayout.NORTH);
    //getContentPane().add(scrollPane, BorderLayout.CENTER);
    

    //addFocusListener(this);
    //textArea.setFont(new Font("Courier", Font.PLAIN, parent.getFontSize()));
    growTreeButton.setEnabled(true);
    
    progressBar = new JProgressBar(0,800);
    progressBar.setStringPainted(true);

    JPanel pabottom = new JPanel();

    pabottom.setLayout(new BorderLayout());
    JPanel pa2 = new JPanel(new FlowLayout(FlowLayout.LEFT));
    JPanel pa3 = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    pa2.add(progressBar);
    //pa2.add(statsLabel);
    //statsLabel.setSize(450,statsLabel.getHeight());

    //pa3.add(sccsPanel);

    pabottom.add(pa2,BorderLayout.WEST);
    pabottom.add(pa3,BorderLayout.EAST);

    
    //JPanel tpanel = new JPanel();
    //tpanel.setSize(ConcordanceTree.WIDTH, ConcordanceTree.HEIGHT);
    //setSize(ConcordanceTree.WIDTH+10, ConcordanceTree.HEIGHT+10);

    // set visualisation display
    
    setDisplay(new ConcordanceTree(),1);


    getContentPane().add(pas, BorderLayout.NORTH);
    getContentPane().add(tpanel, BorderLayout.CENTER);
    getContentPane().add(pabottom, BorderLayout.SOUTH);

    pack();
    setVisible(true);
    guiLayoutDone = true;
    //    growTree(); 
  }

  /**
   * Get the <code>Left_context</code> value.
   *
   * @return a <code>boolean</code> value
   */
  public final boolean isLeftContext() {
    return left_context;
  }

  /**
   * Set the <code>Left_context</code> value.
   *
   * @param newLeft_context The new Left_context value.
   */
  public final void setLeftContext(final boolean newLeft_context) {
    this.left_context = newLeft_context;
  }


  public void start(){
    if ( thread == null ){
      thread = new Thread(this);
      thread.setPriority (Thread.MIN_PRIORITY);
      thread.start();
    }
  }

  public void stop() {
    if ( thread != null ){
      thread.stop();
      
      thread = null;
    }
  }

  public void run(){
    switch(current_action){
    case GROW:
      growTree(); break;
    case PRUNE:
      pruneTree(); break;
    default:
      growTree(); break;
    }
  }


  public void pruneTree(){
    Tree pt = getPrunedTree(conc_tree.getTree());
    //setDisplay(pt.getRoot().getInt(ConcordanceTree.ROWCOUNT));
  }

  public void growTree() {
    try
      {
        //ConcordanceTree ct = new ConcordanceTree();

        Tree tree = null; 
        //sccsPanel.updateStatus();
        //ConcordanceObject[] va =  parent.getConcArray().concArray;

        Vector columns = new Vector();
        StringSplitter ss = new StringSplitter();
        //int[] colcounts = new int[MAXCOLS];
        //Arrays.fill(colcounts,0);
        int c = 0;
        progressBar.setString("Growing tree...");
        int nrows = concVector.size();
        progressBar.setMaximum(nrows);
        progressBar.setValue(c++);
        for (Iterator<ConcordanceObject> p = concVector.iterator(); p.hasNext(); ){
          ConcordanceObject co = p.next();
          progressBar.setValue(c++);
          if (co == null)
            break;
          Object[] tkns;
          if (isLeftContext()){
            Object[] t = ss.split(co.getLeftContext()+" "+co.indexOfKeyword().string).toArray();
            tkns = new Object[t.length];
            int j = t.length-1;
            for(int i=0; i<t.length; i++)
              tkns[j-i] = t[i];
          }
          else
            tkns = (ss.split(co.getKeywordAndRightContext())).toArray();
          Node cnode = null;
          String ctoken = (String)tkns[0];
          if (!case_sensitive)
            ctoken = ctoken.toLowerCase();

          // initialise (keyword/root node)
          if (tree == null){
            conc_tree.resetTree();
            conc_tree.setRowCount(nrows);
            conc_tree.setMinFreqRatio(1f/nrows);
            tree = conc_tree.getTree();
            cnode = tree.getRoot();
            cnode.setString(ConcordanceTree.NAME,ctoken);
            cnode.setInt(ConcordanceTree.NODECOUNT, 1);
            cnode.setInt(ConcordanceTree.ROWCOUNT, nrows);
            //colcounts[0]++;
          }
          else { 
            // update root node frequencies
            cnode = tree.getRoot();
            cnode.setInt(ConcordanceTree.NODECOUNT, cnode.getInt(ConcordanceTree.NODECOUNT)+1);
            //colcounts[0]++;
            //cnode.setInt(ROWCOUNT, cnode.getInt(ROWCOUNT)+1);
          }
          //System.err.println("ctoken = "+ctoken);
          // update trie by reading string from left to right 
          for (int i = 1; i < tkns.length; i++){
            ctoken = (String)tkns[i];

            if (!case_sensitive)
              ctoken = ctoken.toLowerCase();

            boolean found = false;
            //colcounts[i]++;

            for (Iterator<Node> children = cnode.children(); children.hasNext();){
              Node n = children.next();
              // --
              //System.err.println(new String(ch)+"c = "+n.getString(NAME));
              // ---
              if(ctoken.equals(n.getString(ConcordanceTree.NAME))){// found a matching child
                cnode = n;
                cnode.setInt(ConcordanceTree.NODECOUNT, cnode.getInt(ConcordanceTree.NODECOUNT)+1);
                //cnode.setInt(ROWCOUNT, cnode.getInt(ROWCOUNT)+1);
                //System.err.println(new String(ch)+"c++ = "+n.getString(NAME)+" ct="+cnode.getInt(NODECOUNT));
                found = true;
                break;
              }
            } // end iteration 

            if (found)
              continue;
            // couldn't find a matching child; make one
            Node n = tree.addChild(cnode);
            n.setString(ConcordanceTree.NAME,ctoken);
            n.setInt(ConcordanceTree.NODECOUNT, 1);
            n.setInt(ConcordanceTree.ROWCOUNT, nrows);
            //Edge e = tree.addChildEdge(cnode,n);
            //System.err.println(new String(ch)+"c = "+n.getString(NAME)+" e ="+e);
            cnode = n;
          } // end updtate trie for words to the right of kw
        } // end update trie for whole sentence
        progressBar.setString("Done");
        if (isLeftContext())
          conc_tree.setOrientation(Constants.ORIENT_RIGHT_LEFT);
        else
          conc_tree.setOrientation(Constants.ORIENT_LEFT_RIGHT);
        conc_tree.initialView();

        //setDisplay(ct, nrows);

      } // end try
    catch (Exception ex) {
      ex.printStackTrace(System.err);
      JOptionPane.showMessageDialog(null, "Error creating concordance tree" + ex,
                                    "Error!", JOptionPane.ERROR_MESSAGE);
    }
  }


  private Tree updateColCounts(Tree tree, int[] c) {
    for (Iterator<Tuple> spant = tree.tuples(); spant.hasNext();) {
      Tuple n = spant.next();

      if (!(n instanceof Node) )
        continue;

      int d = ((Node)n).getDepth();
      ((Node)n).setInt(ConcordanceTree.ROWCOUNT, c[d]);
      //System.err.println(new String(ch)+"c++ = "+n.getString(NAME)+" ct="+cnode.getInt(NODECOUNT));
    }
    return tree;
  }


 private Tree getPrunedTree(Tree tree) {
   Node r = tree.getRoot();
   Node[] togo = new Node[r.getChildCount()];
   Arrays.fill(togo,null);
   int i = 0;
   for (Iterator<Node> spant = r.children(); spant.hasNext();) {
      Node n = spant.next();
      if (((double)n.getInt(ConcordanceTree.NODECOUNT)/n.getInt(ConcordanceTree.ROWCOUNT)) < prune_cutoff){
        togo[i++] = n;
      }
   }
   for (int k = 0; k < togo.length; k++)
     if (togo[k] != null){
       //tree.removeChildEdge(togo[k].getParentEdge());
       tree.removeChild(togo[k]);
     }
   System.err.println("-------------valid tree? "+tree.isValidTree());
   for (Iterator<Node> spant = r.children(); spant.hasNext();) {
     Node n = spant.next();
   }
   
   return tree;
 }


  private void setDisplay(ConcordanceTree ct, int rc){
    
    ct.setRowCount(rc);
    ct.setMinFreqRatio(1f/rc);
    if (isLeftContext())
      ct.setOrientation(Constants.ORIENT_RIGHT_LEFT);
    ct.setDefaultTreeFont(FontLib.getFont("Tahoma", Font.PLAIN, 10));
    tpanel.removeAll();
    ct.setBackground(Color.WHITE);
    ct.setForeground(Color.BLACK);
    tpanel.add(ct, BorderLayout.CENTER);
    conc_tree = ct;
    //System.err.println("set tree"+conc_tree.getDefaultTreeFont());

  }

  private void setPruneCutoff(double c){
    prune_cutoff = c;
  }

 public void alertWindow (String msg)
  {
    JOptionPane.showMessageDialog(null,
                                  msg,
                                  "alert",
                                  JOptionPane.ERROR_MESSAGE);
  }  

  public static void main(String[] args){
    try{
      ConcordanceViewer cv = new ConcordanceViewer();
      if (args[0] != null){
        Upload ulf = new Upload(new File(args[0]));
        ulf.readConcordances();
        cv.concVector = ulf.getConcordanceVector();
      }
      cv.activate();
    }
    catch(Exception e){
      System.err.println("Error reading "+args[0]+"\n"+e);
    }
  }


}

