package modnlp.tc.dstruct;
import java.util.Vector;
import java.util.Enumeration;

/**
 *  Store text as an array of booleans, its id and categories (also as a vector)
 *
 * @author  Saturnino Luz &#60;luzs@acm.org&#62;
 * @version <font size=-1>$Id: NewsItemAsBooleanVector.java,v 1.1.1.1 2005/05/26 13:59:30 amaral Exp $</font>
 * @see  
*/
public class NewsItemAsBooleanVector {

  private Vector categs = null;
  private String id = null;
  private boolean[] tvect;


  public NewsItemAsBooleanVector (Vector categs, boolean[] tvect, String id)
  {
    super();
    this.id = id;
    this.categs = categs;
    this.tvect = tvect;
  }

  public void addCategory (String topic)
  {
    categs.add(topic);
  }

  public Enumeration getCategories ()
  {
    return categs.elements();
  }

  public Vector getCategVector ()
  {
    return categs;
  }

  public boolean[] getBooleanTextArray ()
  {
    return  tvect;
  }

  /**
   * Get the value of id.
   * @return value of id.
   */
  public String getId() {
    return id;
  }
  
  /**
   * Set the value of id.
   * @param v  Value to assign to id.
   */
  public void setId(String  v) {
    this.id = v;
  }
}
