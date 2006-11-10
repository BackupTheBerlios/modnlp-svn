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
import java.awt.event.*;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import javax.swing.*;

//import java.awt.*;
import java.lang.*;
/**
 *  This class implements the object on which the output
 *  of a concordance interaction (list of lines centered
 *  on a given word) is displyed
 *
 * @author  S. Luz &#60;luzs@acm.org&#62;
 * @version <font size=-1>$Id: ListDisplay.java,v 1.4 2003/06/22 13:48:05 luzs Exp $</font>
 * @see  ContextClient
*/
public class ListDisplay extends JPanel
 implements ActionListener, ComponentListener, 
						ConcordanceDisplayListener, TecDefaultChangeListener {
	public JList list;
	public JScrollPane jscroll;
  public ConcArray conc;
	public Browser parentFrame;
	public Font font; 
  public static int MAXCOL = 180;
  public static int OFFSET = 40;
  public static String HSEP = " | "; 
	/** Width of the scroll pane and list (to be used in resizing events etc) */
	public static int LWIDTH  = 888;
	/** Height of the scroll pane and list (to be used in resizing events etc) */
	public static int LHEIGHT = 473;
	/** Height of the scroll button panel  */
	public static int BWIDTH  = 32;
	/** Height of the scroll button panel  */
	public static int BHEIGHT = 400;
	public int NOROWS = 25;
	// distance to beginning of array 
	public int  concArrayOffset = 0;
	public int  nowDisplayingfrom = 0;
	private DefaultListModel listModel;
  private JButton ffwdb = null;
	private JButton rwndb = null;
	private JButton sfwdb = null;
	private JButton swndb = null;
	private ListDisplayRenderer renderer = new ListDisplayRenderer();
	private boolean resizePending = false;
	private JProgressBar scrollProgress;
	ListSelectionModel listSelectionModel;

  public ListDisplay(Browser parent) 
  {
    super();
		parentFrame = parent;
    ClassLoader cl = this.getClass().getClassLoader();
    ffwdb = new JButton(new ImageIcon(cl.getResource("modnlp/tec/client/icons/ffwd.gif")));
    rwndb = new JButton(new ImageIcon(cl.getResource("modnlp/tec/client/icons/rwnd.gif")));
    sfwdb = new JButton(new ImageIcon(cl.getResource("modnlp/tec/client/icons/sfwd.gif")));
    swndb = new JButton(new ImageIcon(cl.getResource("modnlp/tec/client/icons/swnd.gif")));

		font = new Font("Courier", Font.PLAIN, parentFrame.getFontSize());
		//setPreferredSize(new Dimension(LWIDTH+50, LHEIGHT+30));
		ffwdb.setPreferredSize(new Dimension(30,60));
		rwndb.setPreferredSize(new Dimension(30,60));
		listModel = new DefaultListModel();
		list = new JList(listModel);
		list.setCellRenderer(renderer);
    renderer.setFont(font);
		jscroll = new JScrollPane();
		jscroll.getViewport().setView(list);
		jscroll.setPreferredSize(new Dimension(LWIDTH, LHEIGHT));
		jscroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		JPanel bp = new JPanel();
		bp.setLayout(new BoxLayout(bp, BoxLayout.Y_AXIS));
		bp.setPreferredSize(new Dimension(BWIDTH, LHEIGHT-20));
		rwndb.setToolTipText("Page Up");
		ffwdb.setToolTipText("Page Down");
		rwndb.addActionListener(this);
		ffwdb.addActionListener(this);
		swndb.setToolTipText("Line Up");
		sfwdb.setToolTipText("Line Down");
		swndb.addActionListener(this);
		sfwdb.addActionListener(this);
		rwndb.setEnabled(false);
		ffwdb.setEnabled(false);
	  swndb.setEnabled(false);
		sfwdb.setEnabled(false);
		scrollProgress = new JProgressBar(0, 100);
		scrollProgress.setOrientation(JProgressBar.VERTICAL);
		//scrollProgress.setAlignmentX(Component.CENTER_ALIGNMENT);
		scrollProgress.
			setBorder(BorderFactory.
								createCompoundBorder(BorderFactory.createEmptyBorder(2,14,2,0),
																		 scrollProgress.getBorder()));
		bp.add(rwndb);
		bp.add(swndb);
		bp.add(scrollProgress);
		bp.add(sfwdb);
		bp.add(ffwdb);
		add(bp);
		add(jscroll);
		setNOROWS();
		listSelectionModel = list.getSelectionModel();
		listSelectionModel.addListSelectionListener(parent);
	}
	
	
  public void actionPerformed(ActionEvent evt)
	{
    if(evt.getSource() == sfwdb)
			displayArraySegment(parentFrame.concThread.conc, concArrayOffset-NOROWS+1);
    if(evt.getSource() == swndb)
			displayArraySegment(parentFrame.concThread.conc, concArrayOffset-NOROWS-1);
    if(evt.getSource() == ffwdb)
			displayArraySegment(parentFrame.concThread.conc, concArrayOffset);
		if(evt.getSource() == rwndb){
			int from = concArrayOffset - 2 * NOROWS;
			displayArraySegment(parentFrame.concThread.conc, from);
		}
	}

	private void initialiseList(){
		for(int count = concArrayOffset; count < NOROWS ; count++)
      {
        listModel.addElement(null);
			}
	}

	/**
	 * Display conc array (providind for aligment with respect to filenames)
	 * @param conc     the concordance array
	 */
  public void displayArraySegment (ConcArray conc, int from)
  {
    int lfn_size = conc.getLengthLongestFname();
		//		String[] coa = new String[NOROWS];
		ConcordanceObject[] coa = new ConcordanceObject[NOROWS];
		if (from == 0)
			setNumberOfFoundDisplay();
		ffwdb.setEnabled(true);
		sfwdb.setEnabled(true);
		if (from <= 0) {
			from = 0;
			rwndb.setEnabled(false);
			swndb.setEnabled(false);
		}
		else{
			rwndb.setEnabled(true);
			swndb.setEnabled(true);
		}
		concArrayOffset = from;
		nowDisplayingfrom = from;
		int to = from + NOROWS;
		//System.out.println("Displayin till: "+from );
    if ( conc == null )
      return;
    for(int count = concArrayOffset; count < to ; count++)
      {				
				if (conc.concArray[count] == null 
						|| 
						count >= parentFrame.concThread.noFound
						|| count >= conc.arraymax)
					{
						//coa[count-concArrayOffset] = "";
						ffwdb.setEnabled(false);
						sfwdb.setEnabled(false);
					}
				else
					{
						ConcordanceObject co = conc.concArray[count];
						coa[count-concArrayOffset] = co; //co.textConcLine(lfn_size);
					}
				//				listModel.addElement(co.textConcLine(lfn_size));
      }
		list.setListData(coa);
		concArrayOffset = to > parentFrame.getNoFound()? parentFrame.getNoFound() : to;
		parentFrame.updateStatusLabelScroll("  (displaying "+
																				concArrayOffset+"/"+
																				scrollProgress.getMaximum()+")");
	  scrollProgress.setValue(to);
  }

	public void setNumberOfFoundDisplay ()
	{
		scrollProgress.setMaximum(parentFrame.getNoFound());
	}
 /**
   * Display conc array (providind for aligment with respect to filenames)
   * @param conc     the concordance array
   */
  public void displayArraySegment (ConcArray conc)
  {
		displayArraySegment(conc, 0);
  }

  /**
   * Display conc array (providind for aligment with respect to filenames)
	 * start displaying from the first element of parent frame's ConcArray.
   */
  public void displayArraySegment ()
  {
		if ( hasDisplayableConcArray() ) 
			displayArraySegment(parentFrame.concThread.conc, 0);
  }
	public void redisplayArraySegment ()
  {
		if ( hasDisplayableConcArray() ) {
			int index = list.getSelectedIndex();
			displayArraySegment(getFirstDisplayedIndex());
			list.setSelectedIndex(index);
		}
	}

	public int getFirstDisplayedIndex() 
	{
		return nowDisplayingfrom;
	}

	public int getLastDisplayedIndex() 
	{
		return concArrayOffset;
	}

	public boolean hasDisplayableConcArray ()
	{
		return parentFrame != null && parentFrame.concArrayExists();
	}
  /**
   * Display conc array (providind for aligment with respect to filenames)
	 * start displaying from FROM.
	 * @param from  the first index to be displayed.
   */
  public void displayArraySegment (int from)
  {
		displayArraySegment(parentFrame.concThread.conc, from);
  }

	/**
	 * Implement ConcordanceDisplayListener.
	 */
  public void concordanceChanged(ConcordanceDisplayEvent e)
	{
		//System.out.println("Displaying "+e.getFirstIndex() );		
		displayArraySegment(e.getFirstIndex());
  }

  public void concordanceChanged(ConcordanceListSizeEvent e)
	{
		setNumberOfFoundDisplay();
		parentFrame.updateStatusLabelScroll("  (displaying "+
																				concArrayOffset+"/"+
																				e.getNoFound()+")");

	}

	public void addItem (Object ob){
		listModel.addElement(ob);
	}

	public void removeAll() 
	{
		//listModel.clear();
		String[] clear = {};
		list.setListData(clear);
	}


	public ConcordanceObject getSelectedValue () {

		return (ConcordanceObject) list.getSelectedValue();

	}

	public int getSelectedIndex () 
	{
		
		int index = list.getSelectedIndex();
		return index+concArrayOffset-NOROWS;
		
	}

  /** Resets font when user preferences change
   *  @return <code>true</code> if font has been reset 
   */
  public boolean resetFontIfChanged(float nsz){

    if ( renderer.getFont().getSize() != nsz ){
			font = font.deriveFont(nsz);
      renderer.setFont( font );
			setNOROWS();
      return true;
    }
    else 
      return false;
  }

	// The TEC default change interface
  public void defaultChanged(DefaultChangeEvent e)
	{
		//System.err.println("Default change"+preferenceFrame.getFontSize());
	}
  public void defaultChanged(SortHorizonChangeEvent e)
	{
		//System.err.println("New sort"+preferenceFrame.getFontSize());
	}

  public void defaultChanged(FontSizeChangeEvent e)
	{
		//System.err.println("New font"+preferenceFrame.getFontSize());
		if ( resetFontIfChanged(e.getNewSize()) )
			redisplayArraySegment();
	}


	public void setNOROWS () 
	{	
		int th = (list.getHeight() == 0)? (LHEIGHT-16): list.getHeight(); 
		int ch = (int) renderer.getFontMetrics(font).getHeight();
		NOROWS =  th / ch; 

	}

	public void setNOROWS (Dimension d) 
	{	
		int ch = (int) renderer.getFontMetrics(font).getHeight();
		NOROWS =  d.height-16 / ch; 

	}


  public String adjustPrefixString(String pfx, int size)
  {
    int dif =   pfx.length() - size;
		
    if ( dif > 0 ){
      return pfx.substring(dif);
    }
    else if ( dif < 0 ) {
      char[] auxA = new char[dif*-1];
      for(int count = 0; count < (dif*-1) ; count++)
				auxA[count] = ' ';
      String npfx = new String(auxA);
      return npfx.concat(pfx);
    }
    else {
      return pfx;
    }		
  }
		
  public String adjustOffSet(int maxs, int size){
		
    char[] auxA = new char[maxs-size];
    for(int i = 0; i < (maxs-size) ; i++)
      auxA[i] = ' ';
    
    return new String(auxA);
  }

	// list resizing stuff
	public void componentResized(ComponentEvent e) {
		resizePending = true;
		Component ec = e.getComponent();
		if (! isShowing() )
			return;
		int w = ec.getWidth();
		int h = ec.getHeight();
		Dimension dlist = new Dimension(w-60, h-150);
		setPreferredSize(new Dimension(w, h-120));
		remove(jscroll);
		jscroll.setPreferredSize(dlist);
		jscroll.getViewport().setView(list);
		add(jscroll);
		revalidate();
		repaint();
	}


	public void componentHidden(ComponentEvent e) {
	}
	public void componentMoved(ComponentEvent e) {
	}
	public void componentShown(ComponentEvent e) {		
	}

	public void paintComponent (Graphics g){
		if ( resizePending ){
			setNOROWS();
			redisplayArraySegment();
			resizePending = false;
		}
	}


}
