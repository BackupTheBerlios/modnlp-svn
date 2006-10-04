package modnlp.tc.dstruct;
/**
 *  Represent a doc id and the number of times a term (in BVProbabilityModel) occurs in it
 *
 * @author  Saturnino Luz &#60;luzs@acm.org&#62;
 * @version <font size=-1>$Id: IdFrequencyPair.java,v 1.1.1.1 2005/05/26 13:59:30 amaral Exp $</font>
 * @see  BVProbabilityModel
*/
public class IdFrequencyPair implements Comparable 
{

  // this would save memory but limit the corpus and the number of
  // occurrences of a term per document to a maximum of 32726
  //private short docID = 0;
  //private short count = 0;
  private String docID = null;
  private int count = 0;

  public IdFrequencyPair(String s, int c)
  {
    setDocID(s);
    setCount(c);
  }

  /**
   * Get the value of docID.
   * @return value of docID.
   */
  public String getDocID() {
    return docID;
  }
  
  /**
   * Set the value of docID.
   * @param v  Value to assign to docID.
   */
  public void setDocID(String  v) {
    this.docID = v;
  }
  
  public int getCount() {
    return count;
  }
  
  /**
   * Set the value of count.
   * @param v  Value to assign to count.
   */
  public void setCount(int  v) {
    this.count = v;
  }
  
  public int compareTo (Object wfp)
  {
    return compareTo((IdFrequencyPair) wfp);
  }
  public int compareTo (IdFrequencyPair wfp)
  {
    if ( count == wfp.getCount() )
      return 0;
    if ( count > wfp.getCount() )
      return 1;
    return -1;
  }

}
