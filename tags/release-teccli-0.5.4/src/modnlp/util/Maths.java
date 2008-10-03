package modnlp.util;
/**
 *  Wrapper class maths
 *
 * @author  S Luz &#60;luzs@cs.tcd.ie&#62;
 * @version <font size=-1>$Id: Maths.java,v 1.1.1.1 2005/05/26 13:59:30 amaral Exp $</font>
 * @see  
*/
public class Maths {

  public static double log2 (double ln) {
    return Math.log(ln)/Math.log(2D);
  }

  public static double safeLog2 (double ln) {
    if ( ln == 0 || Double.isNaN(ln) || Double.isInfinite(ln) )
      return 0;
    return Math.log(ln)/Math.log(2D);
  }

  public static double log (double ln, double base) {
    return Math.log(ln)/Math.log(base);
  }

  public static double safediv (double dividend, double divisor) {
    return divisor == 0 ? 0 : dividend / divisor;
  }


  /**
   * Safe  "x * log y"  with 0 log 0 =def 0,  for use computing info theoretic metrics 
   */
  public static double xTimesLog2y(double x, double y) {  
    if ( y == 0 || Double.isNaN(y))
      return 0;
    return x * log2(y);
  }
}
