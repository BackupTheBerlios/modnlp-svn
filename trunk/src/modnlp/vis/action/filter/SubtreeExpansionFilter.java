/**
 *  © 2009 S Luz <luzs@cs.tcd.ie>
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
package modnlp.vis.action.filter;

import java.util.Iterator;

import prefuse.Constants;
import prefuse.Visualization;
import prefuse.action.GroupAction;
import prefuse.data.Graph;
import prefuse.data.Tree;
import prefuse.data.expression.Predicate;
import prefuse.util.PrefuseLib;
import prefuse.visual.EdgeItem;
import prefuse.visual.NodeItem;
import prefuse.visual.VisualItem;
import prefuse.visual.expression.InGroupPredicate;

/**
 *  Expand subtree of focused node and collapse its sister's subtrees 
 *
 * @author  S Luz &#60;luzs@acm.org&#62;
 * @version <font size=-1>$Id: $</font>
 * @see  
*/
public class SubtreeExpansionFilter extends GroupAction {

    private String m_sources;
    private Predicate m_groupP;    
    private NodeItem m_root;
  // TO DO: the next two vars were set just so that the class compiles. 
    private int m_threshold;
    private int m_divisor;
    
    /**
     * Create a new SubtreeExpansionFilter that processes the given group.
     *
     * @param group the data group to process.
     */
    public SubtreeExpansionFilter(String group) {
        super(group);
        m_sources = Visualization.FOCUS_ITEMS;
        m_groupP = new InGroupPredicate(
                PrefuseLib.getGroupName(group, Graph.NODES));
    }
    
    /**
     * Get the name of the group to use as source nodes 
     *
     * @return the source data group
     */
    public String getSources() {
        return m_sources;
    }
    
    /**
     * Set the name of the group
     *
     * @param sources the source data group
     */
    public void setSources(String sources) {
        m_sources = sources;
    }
    
    /**
     * @see prefuse.action.GroupAction#run(double)
     */
    public void run(double frac) {
        Tree tree = ((Graph)m_vis.getGroup(m_group)).getSpanningTree();
        m_root = (NodeItem)tree.getRoot();
        
        // mark the items
        Iterator items = m_vis.visibleItems(m_group);
        while ( items.hasNext() ) {
            VisualItem item = (VisualItem)items.next();
            item.setDOI(Constants.MINIMUM_DOI);
            item.setExpanded(false);
        }
        
        // compute the fisheye over nodes
        Iterator iter = m_vis.items(m_sources, m_groupP);
        while ( iter.hasNext() )
            visitFocus((NodeItem)iter.next(), null);
        visitFocus(m_root, null);

        // mark unreached items
        items = m_vis.visibleItems(m_group);
        while ( items.hasNext() ) {
            VisualItem item = (VisualItem)items.next();
            if ( item.getDOI() == Constants.MINIMUM_DOI )
                PrefuseLib.updateVisible(item, false);
        }
    }

    /**
     * Visit a focus node.
     */
    private void visitFocus(NodeItem n, NodeItem c) {
        if ( n.getDOI() <= -1 ) {
            visit(n, c, 0, 0);
            if ( m_threshold < 0 )                 
                visitDescendants(n, c);
            visitAncestors(n);
        }
    }
    
    /**
     * Visit a specific node and update its degree-of-interest.
     */
    private void visit(NodeItem n, NodeItem c, int doi, int ldist) {
        PrefuseLib.updateVisible(n, true);
        double localDOI = -ldist / Math.min(1000.0, m_divisor);
        n.setDOI(doi+localDOI);
        
        if ( c != null ) {
            EdgeItem e = (EdgeItem)c.getParentEdge();
            e.setDOI(c.getDOI());
            PrefuseLib.updateVisible(e, true);
        }
    }
    
    /**
     * Visit tree ancestors and their other descendants.
     */
    private void visitAncestors(NodeItem n) {
        if ( n == m_root ) return;
        visitFocus((NodeItem)n.getParent(), n);
    }
    
    /**
     * Traverse tree descendents.
     */
    private void visitDescendants(NodeItem p, NodeItem skip) {
        int lidx = ( skip == null ? 0 : p.getChildIndex(skip) );
        
        Iterator children = p.children();
        
        p.setExpanded(children.hasNext());
        
        for ( int i=0; children.hasNext(); ++i ) {
            NodeItem c = (NodeItem)children.next();
            if ( c == skip ) { continue; }             
            
            int doi = (int)(p.getDOI()-1);            
            visit(c, c, doi, Math.abs(lidx-i));      
            if ( doi > m_threshold )
                visitDescendants(c, null);   
        }
    }
    
}

