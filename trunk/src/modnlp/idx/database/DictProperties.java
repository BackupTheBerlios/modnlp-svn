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
package modnlp.idx.database;

import java.io.FileInputStream;
import java.io.FileOutputStream;
/**
 *  Load dictionary defaults
 *
 * @author  S Luz &#60;luzs@cs.tcd.ie&#62;
 * @version <font size=-1>$Id: DictProperties.java,v 1.2 2006/05/22 17:26:02 amaral Exp $</font>
 * @see  
*/
public class DictProperties extends java.util.Properties{

  public static String PROP_FNAME = "dictionary.properties";
  private static String PS = java.io.File.separator;
  String envHome = "/tmp/tec/index/";   // a very unsafe default;
  String wFilTableName = "wfindex.db";  // word -> [fileno1, fileno2, ...]
  String caseTableName = "caindex.db";  // canonicalform -> [form1, form2, ...]
  String freqTableName = "fqtable.db";  // word -> noofoccurrences
  String fileTableName = "fitable.db";  // fileno -> fileuri
  String tPosTableName = "tptable.db";  // fileno -> [offset1, offset2, ...]
  String corpusDir = ""; // the default directory for relative fileTableNames 

	public DictProperties (String cd) {
    envHome = cd;
    init();
  }

	public DictProperties (){
    init();
  }

  private void init(){
    String pf = envHome+PS+PROP_FNAME;
    try {
      ClassLoader cl = this.getClass().getClassLoader();
      //InputStream fis = ((cl.getResource(pf))
      //                   .openConnection()).getInputStream();
       //FileInputStream fis = new FileInputStream(new File("tecli.properties"));
      this.load(new FileInputStream(pf));
    }
    catch (Exception e) {
	    System.err.println("Error reading property file "+pf+": "+e);
	    System.err.println("Using defaults in DictProperties.java");
      setProperty("dictionaty.environment.home",envHome);
      setProperty("wfile.table.name", wFilTableName);
      setProperty("case.table.name", caseTableName);
      setProperty("frequency.table.name", freqTableName);
      setProperty("file.table.name", fileTableName);
      setProperty("tpos.table.name", tPosTableName);
      setProperty("corpus.data.directory", corpusDir);
      save();
		}
	}

  public void save () {
    try {
      store(new FileOutputStream(envHome+PS+PROP_FNAME), 
            "Dictionary's properties");
    }
    catch (Exception e){
      System.err.println("Error writing property file "+envHome+PS+PROP_FNAME+": "+e);
    }
  }

  protected void finalize () throws java.lang.Throwable {
    save();
    super.finalize();
  }


  public String getEnvHome () {
    return getProperty("dictionaty.environment.home");
  }

  public String getTPosTableName () {
    return getProperty("tpos.table.name");
  }

  public String getWFilTableName () {
    return getProperty("wfile.table.name");
  }

  public String getCaseTableName () {
    return getProperty("case.table.name");
  }

  public String getFreqTableName () {
    return getProperty("frequency.table.name");
  }

  public String getFileTableName () {
    return getProperty("file.table.name");
  }

  public String getCorpusDir () {
    return getProperty("corpus.data.directory");
  }

}
