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

import java.io.PrintWriter;

import com.sleepycat.je.Environment;
import com.sleepycat.bind.tuple.StringBinding;
import com.sleepycat.bind.tuple.IntegerBinding;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Cursor;
import com.sleepycat.je.OperationStatus;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.LockMode;
import com.sleepycat.je.SecondaryDatabase;
import com.sleepycat.je.SecondaryConfig;
import com.sleepycat.je.SecondaryCursor;


/**
 *  Store a (case-sensitive) word form and the frequency
 *  with which it occurs
 *
 *  <pre>  
 *       KEY        |  DATA
 *   ---------------|-------------------
 *      wordform    | no_of_occurrences
 *  </pre>
 *
 * @author  S Luz &#60;luzs@cs.tcd.ie&#62;
 * @version <font size=-1>$Id: FreqTable.java,v 1.2 2006/05/22 17:26:02 amaral Exp $</font>
 * @see  
*/
public class FreqTable extends Table {

  SecondaryDatabase freqKeyDatabase = null;


  public FreqTable (Environment env, String fn, boolean write) {
    super(env,fn,write);
    try {     
      // use a secondary db to keep records sorted by frequency
      FreqKeyCreator fkc = new FreqKeyCreator();
      SecondaryConfig sc = new SecondaryConfig();
      sc.setKeyCreator(fkc);
      sc.setReadOnly(!write);
      sc.setAllowCreate(write);
      sc.setSortedDuplicates(true);
      // setting the comparator sometimes causes je to throw a null pointer exception
      // investigate why,
      sc.setBtreeComparator(DescIntComparator.class);
      String scname = "secfqtable.db";
      freqKeyDatabase = env.openSecondaryDatabase(null, 
                                                  scname, 
                                                  database,
                                                  sc); } 
    catch (DatabaseException e) {
      logf.logMsg("Error opening secondary FreqTable", e);
      try {       
        if (freqKeyDatabase != null) 
          freqKeyDatabase.close();
        if (database != null)
          database.close();         
      }
      catch (DatabaseException se) {
        logf.logMsg("Error trying to close secondary FreqTable", se);
      } 
    } 
  }

 public int getFrequency(String sik) {
    DatabaseEntry key = new DatabaseEntry();
    DatabaseEntry data = new DatabaseEntry();
    int freq = 0;
    StringBinding.stringToEntry(sik, key);
    try {
      if (database.get(null, key, data, LockMode.DEFAULT) ==
          OperationStatus.SUCCESS) 
        freq = IntegerBinding.entryToInt(data);
    }
    catch (DatabaseException e) {
      logf.logMsg("Error reading FreqTable" , e);
    }
    return freq;
 }

  public int put(String sik, int noccur) {
    DatabaseEntry key = new DatabaseEntry();
    DatabaseEntry data = new DatabaseEntry();
    int freq = noccur;
    StringBinding.stringToEntry(sik, key);
    try {
      if (database.get(null, key, data, LockMode.DEFAULT) ==
          OperationStatus.SUCCESS) {
        freq = IntegerBinding.entryToInt(data)+noccur;
      }
      IntegerBinding.intToEntry(freq, data);
      put(key,data);
    }
    catch (DatabaseException e) {
      logf.logMsg("Error reading FreqTable" , e);
    }
    return freq;
  }

  public int remove(String sik, int noccur) {
    DatabaseEntry key = new DatabaseEntry();
    DatabaseEntry data = new DatabaseEntry();
    int freq = 0;
    StringBinding.stringToEntry(sik, key);
    try {
      if (database.get(null, key, data, LockMode.DEFAULT) ==
          OperationStatus.SUCCESS) {
        freq = IntegerBinding.entryToInt(data)-noccur;
        if (freq == 0)
          remove(key);
        else {
          IntegerBinding.intToEntry(freq, data);
          put(key,data);
        }
      }
    }
    catch (DatabaseException e) {
      logf.logMsg("Error reading FreqTable" , e);
    }
    return freq;
  }

  public void  dump () {
    try {
      Cursor c = database.openCursor(null, null);
      DatabaseEntry key = new DatabaseEntry();
      DatabaseEntry data = new DatabaseEntry();
      while (c.getNext(key, data, LockMode.DEFAULT) == 
             OperationStatus.SUCCESS) {
        String sik = StringBinding.entryToString(key);
        int freq  = IntegerBinding.entryToInt(data);
        System.out.println(sik+" = "+freq);
      }
      c.close();
    }
    catch (DatabaseException e) {
      logf.logMsg("Error accessing FreqTable" , e);
    }
  }

  public void printSortedFreqList (PrintWriter os) {
    try {
      SecondaryCursor c = freqKeyDatabase.openSecondaryCursor(null, null);
      DatabaseEntry key = new DatabaseEntry();
      DatabaseEntry skey = new DatabaseEntry();
      DatabaseEntry data = new DatabaseEntry();
      while (c.getNext(skey, key, data, LockMode.DEFAULT) == 
             OperationStatus.SUCCESS) {
        String sik = StringBinding.entryToString(key);
        int freq  = IntegerBinding.entryToInt(data);
        os.println(sik+": "+freq);
      }
      c.close();
      os.flush();
    }
    catch (DatabaseException e) {
      logf.logMsg("Error accessing secondary cursor for FreqTable" , e);
    }     
  } 

  public void close () {
    // ignore operation status
    try {
      freqKeyDatabase.close();
      super.close();
    } catch(DatabaseException e) {
      logf.logMsg("Error closing DB "+dbname,e);
      e.printStackTrace(System.err);
    }
  }

}
