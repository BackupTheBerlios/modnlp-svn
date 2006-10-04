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
import modnlp.util.LogStream;

import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.DatabaseConfig;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseNotFoundException;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.DeadlockException;
import com.sleepycat.je.Environment;

import java.io.IOException;

/**
 *  A template for all table classes (encapsulate berkeley db to hide
 *  complicated stuff from the rest of the system)
 *
 * @author  S Luz &#60;luzs@cs.tcd.ie&#62;
 * @version <font size=-1>$Id: Table.java,v 1.2 2006/05/22 17:26:02 amaral Exp $</font>
 * @see  
*/

abstract class Table  {
  protected Environment environment;
  protected Database database;
  protected String dbname;
  protected LogStream logf;

  public Table (Environment env, String fn, boolean write) {
    try {
      setup(env,fn,write, new LogStream(System.err));
    } catch(IOException e) {
      System.err.println("Error opening log stream: "+e);
    } 
  }

  public Table (Environment env, String fn, boolean write, LogStream l) {
    setup(env,fn,write,l);
  }

  public void setup (Environment env, String fn, boolean write, LogStream l) {
    try {
      environment = env;
      logf = l;
      dbname = fn;
      DatabaseConfig dbc = new DatabaseConfig();
      dbc.setReadOnly(!write);
      dbc.setAllowCreate(write);
      
      database = environment.openDatabase(null,
                                          fn,
                                          dbc);
    } catch (DatabaseNotFoundException e) {
      logf.logMsg("DB file "+fn+" not found: ",e);
    } catch(DatabaseException e) {
      logf.logMsg("Error accessing DB "+fn,e);
    } 
  }

  public void finalize () {
    close();
  }

  public void close () {
    // ignore operation status
    try {
      if (database != null ){
        database.close();
        database = null;
      }
    } catch(DatabaseException e) {
      logf.logMsg("Error closing DB "+dbname,e);
    }
  }

  public void put (DatabaseEntry key, DatabaseEntry val) {
    // ignore operation status
    try {
      database.put(null,key,val);

    } catch(DatabaseException e) {
      logf.logMsg("Error putting into DB "+dbname,e);
    }
  }

  public void remove (DatabaseEntry key) {
    // ignore operation status
    try {
      database.delete(null,key);

    } catch(DeadlockException e) {
      logf.logMsg("Deadlock while removing from DB "+dbname,e);
    }
    catch(DatabaseException e) {
      logf.logMsg("Error removing from DB "+dbname,e);
    }
  }

  public void get (DatabaseEntry key, DatabaseEntry data) {
    // ignore operation status
    try {
      database.get(null,key,data,null);
    } catch(DeadlockException e) {
      logf.logMsg("Deadlock reading from dbname",e);
    }
    catch(DatabaseException e) {
      logf.logMsg("Error reading from DB "+dbname,e);
    }
  }


}
