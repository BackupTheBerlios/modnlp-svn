package modnlp.tc.evaluation;
/**
 *  Store maximum and minimum CSVs for a CSVTable
 *
 * @author  S Luz &#60;luzs@cs.tcd.ie&#62;
 * @version <font size=-1>$Id: MaxMinCSV.java,v 1.1.1.1 2005/05/26 13:59:30 amaral Exp $</font>
 * @see  
*/
public class MaxMinCSV {

  public double max;
  public double min;

  public MaxMinCSV(double mx, double mn){
    max = mx;
    min = mn;
  }
}
