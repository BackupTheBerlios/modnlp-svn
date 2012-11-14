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
package WordleConc;

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

  
  public void setParent(Object p){
    parent = (ConcordanceBrowser)p;
    sccsPanel = new SubcorpusCaseStatusPanel(p);
  }

  public void activate() {
    if (guiLayoutDone){
      setVisible(true);
      return;
    }

    JButton dismissButton = new JButton("Quit");
    JButton growTreeButton = new JButton("Show");
    
    dismissButton.addActionListener(new ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
          thisFrame.setVisible(false);
        }});

    growTreeButton.addActionListener(new ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
          //stop();
          current_action = GROW;
          start(); 
          }});


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


    getContentPane().add(pas, BorderLayout.NORTH);
    WordleConcPApplet wapplet = new WordleConcPApplet();
    getContentPane().add(wapplet, BorderLayout.CENTER);
    getContentPane().add(pabottom, BorderLayout.SOUTH);

    embed.init();
    pack();
    setVisible(true);
    guiLayoutDone = true;
    //    growTree(); 

  }

  void PopulateWordle() {
    Tokeniser ss;
    int la = parent.getLanguage();
    switch (la) {
    case modnlp.Constants.LANG_EN:
      ss = new TokeniserRegex("");
      break;
    case modnlp.Constants.LANG_JP:
      ss = new TokeniserJP("");
      break;
    default:
      ss = new TokeniserRegex("");
      break;
    }

    for (Iterator<ConcordanceObject> p = parent.getConcordanceVector().iterator(); p.hasNext(); ){
      ConcordanceObject co = p.next();
      if (co == null)
        break;
      Object[] tkns;
      if (isLeftContext()){
        Object[] t = (ss.split(co.getLeftContext()+" "+parent.getKeywordString())).toArray();
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


  }
