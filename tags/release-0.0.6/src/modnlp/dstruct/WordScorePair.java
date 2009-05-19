package modnlp.dstruct;
/**
 *  Represent a term and its double-precision flot score
 *
 * @author  Saturnino Luz &#60;luzs@acm.org&#62;
 * @version <font size=-1>$Id: WordScorePair.java,v 1.1.1.1 2005/05/26 13:59:30 amaral Exp $</font>
 * @see  
*/
public class WordScorePair implements Comparable 
{

  private String word = null;
  private double score = 0;

  public WordScorePair(String s, double c)
  {
    setWord(s);
    setScore(c);
  }

  /**
   * Get the value of word.
   * @return value of word.
   */
  public String getWord() {
    return word;
  }
  
  /**
   * Set the value of word.
   * @param v  Value to assign to word.
   */
  public void setWord(String  v) {
    this.word = v;
  }
  

  public double getScore() {
    return score;
  }

  public Double getDoubleScore() {
    return new Double(score);
  }


  public String toString(){
    return word+" = "+score;
  }
  
  /**
   * Set the value of score.
   * @param v  Value to assign to score.
   */
  public void setScore(double  v) {
    this.score = v;
  }
  
  public int compareTo (Object wfp)
  {
    return compareTo((WordScorePair) wfp);
  }
  public int compareTo (WordScorePair wfp)
  {
    return (new Double(score)).compareTo(wfp.getDoubleScore());
  }

}
