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

import modnlp.idx.query.WordQuery;

import modnlp.util.LogStream;
import modnlp.util.PrintUtil;
import modnlp.dstruct.WordForms;
import modnlp.dstruct.CorpusFile;
import modnlp.dstruct.IntegerSet;
import modnlp.dstruct.TokenMap;
import modnlp.dstruct.IntOffsetArray;

import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.bind.tuple.TupleBinding;
import com.sleepycat.je.DatabaseEntry;
 
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.NumberFormat;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeSet;
import java.util.Vector;
/**
 *  Mediate access to all databases (called Dictionary for
 *  'historical' reasons; see tec-server)
 *
 * @author  S Luz &#60;luzs@cs.tcd.ie&#62;
 * @version <font size=-1>$Id: Dictionary.java,v 1.2 2006/05/22 17:26:02 amaral Exp $</font>
 * @see  
*/
public class Dictionary {

  public static DictProperties dictProps = new DictProperties();
  LogStream logf;
  // main tables 
  // WordPositionTable wPosTable;          // word -> [pos1, pos2, ...]  
  // (one table per fileno; to appear as local variables)
  WordFileTable wFilTable;       // word -> [fileno1, fileno2, ...]
  CaseTable caseTable;           // canonicalform -> [form1, form2, ...]
  FreqTable freqTable;           // word -> noofoccurrences
  FileTable fileTable;           // fileno -> filenameOrUri
  TPosTable tposTable;           // fileno -> (offset) positions of each token in file
  Environment environment;

  protected boolean verbose = false; 


  /**
   * Open a new <code>Dictionary</code> in read-only mode.
   *
   */
  public Dictionary(){
    init(false);
  }


  /**
   * Open a new <code>Dictionary</code>.
   *
   * @param write a <code>boolean</code> value: false opens the
   * dictionary in read-only mode; true opens it for writing (enabling
   * creation of new tables)
   */
  public Dictionary (boolean write){
    init(write);
  }

  public void init (boolean write){
    try {
      logf = new LogStream(System.err);
      EnvironmentConfig envConfig = new EnvironmentConfig();
      envConfig.setReadOnly(!write);
      envConfig.setAllowCreate(write);
      environment = new Environment(new File(dictProps.getEnvHome()), 
                                    envConfig);
      wFilTable = new WordFileTable(environment, 
                                    dictProps.getWFilTableName(), 
                                    write);
      caseTable = new CaseTable(environment, 
                                dictProps.getCaseTableName(), 
                                write);
      freqTable = new FreqTable(environment, 
                                dictProps.getFreqTableName(), 
                                write);
      fileTable = new FileTable(environment, 
                                dictProps.getFileTableName(), 
                                write);
      tposTable = new TPosTable(environment, 
                                dictProps.getTPosTableName(), 
                                write);
      
    } catch (Exception e) {
      logf.logMsg("Error opening Dictionaries: "+e);
    }
  }

  
  /**
   * Add each token in tm (extracted from fou) to the index
   *
   * N.B.: currently, addToDictionary operations aren't atomic; if the
   * program crashes the index could be left in an inconsistent
   * state. In future, implement it using JE transactions
   *
   * @param tm a <code>TokenMap</code>: multiset of tokens
   * @param fou a <code>String</code>: the file whose <code>TokenMap</code> is tm 
   * @exception AlreadyIndexedException if an error occurs
   */
  public void addToDictionary(TokenMap tm, String fou) 
    throws AlreadyIndexedException 
  {
    // check if file already exists in corpus; if so, quit and warn user
    NumberFormat nf = NumberFormat.getInstance();
    nf.setMaximumFractionDigits(4);
    int founo = fileTable.getKey(fou);
    if (founo >= 0) { // file has already been indexed
      logf.logMsg("Dictionary: file or URI already indexed "+fou);
      throw new AlreadyIndexedException(fou);
    }
    else 
      founo = -1*founo;
    fileTable.put(founo,fou);
    WordPositionTable wPosTable = new WordPositionTable(environment, 
                                      ""+founo,
                                      true);
    // store sorted set of positions (worst-case for basic operations
    // O(ln(n)) which should be better than storing in a vector and
    // standard merge sorting, which is O(n^2 ln(n))) [SL: run tests to check that]
    TreeSet poss = new TreeSet();
    int ct = 1;
    for (Iterator e = tm.entrySet().iterator(); e.hasNext() ;)
			{
        if (verbose)
          PrintUtil.printNoMove("Indexing ...",ct++);
        Map.Entry kv = (Map.Entry) e.next();
        String word = (String)kv.getKey();
        caseTable.put(word);
        wFilTable.put(word,founo);
        //StringIntKey sik = new StringIntKey(word, founo);
        IntegerSet set = (IntegerSet) kv.getValue();
        wPosTable.put(word, set);
        freqTable.put(word,set.size());
        poss.addAll(set);
      }
    if (verbose)
      PrintUtil.donePrinting();
    wPosTable.close();
    //System.err.println(poss);
    int [] posa = new int[poss.size()];
    int i = 0;
    for (Iterator e = poss.iterator(); e.hasNext() ;)
      posa[i++] = ((Integer) e.next()).intValue();
    tposTable.put(founo,new IntOffsetArray(posa));
    System.out.println("Cumulative compression ratio = "+
                       nf.format(tposTable.getCompressionRatio())+
                       " (read "+nf.format(tposTable.getBytesReceived())+
                       " and wrote "+
                       nf.format(tposTable.getBytesWritten())+" bytes)");
    //tposTable.dump();
  }
  

  /**
   * <code>removeFromDictionary</code> de-indexes file or URL
   * <code>fou</code>
   *
   * N.B.: currently, removeFromDictionary operations aren't atomic;
   * if the program crashes the index could be left in an inconsistent
   * state. In future, implement it using JE transactions
   *
   * @param fou a <code>String</code> value
   * @exception NotIndexedException if an error occurs
   */
  public void removeFromDictionary(String fou) 
    throws NotIndexedException 
  {
    // check if file already exists in corpus; if so, quit and warn user
    int founo = fileTable.getKey(fou);
    if (founo < 0) { // file was not indexed
      logf.logMsg("Dictionary: file or URI not indexed "+fou);
      throw new NotIndexedException(fou);
    }
    WordPositionTable wPosTable = new WordPositionTable(environment, 
                                                        ""+founo,
                                                        true);
    TokenMap tm = wPosTable.removeFile();
    for (Iterator e = tm.entrySet().iterator(); e.hasNext() ;)
			{
        Map.Entry kv = (Map.Entry) e.next();
        String word = (String)kv.getKey();
        tposTable.remove(founo);
        wFilTable.remove(word,founo);
        IntegerSet set = (IntegerSet) kv.getValue();
        if (freqTable.remove(word,set.size()) == 0)
          caseTable.remove(word);
      }
    fileTable.remove(founo);
  }

  /**
   * Check if file or URI <code>fou</code> is in the index. 
   *
   * @param fou a <code>String</code> value
   * @return a <code>boolean</code> value
   */
  public boolean indexed(String fou) {
    // check if file already exists in corpus; if so, quit and warn user
    int founo = fileTable.getKey(fou);
    if (founo >= 0)  // file has already been indexed
      return true;
    else 
      return false;
  }

  // kwa elements can also be wildcards (e.g. "test*", matching "test", "tested", etc)
  public WordForms getLeastFrequentWord (String [] kwa, boolean cs ){
    //String word = null;
    int freq = 0;
    WordForms wformsout = new WordForms();
    for (int i = 0; i < kwa.length ; i++ ) {
      WordForms wforms = getWordForms(kwa[i], cs);
      int fqaux = getFrequency(wforms);
      if (fqaux == 0)
        return wforms;
      if (freq == 0 || fqaux < freq){
        freq = fqaux;
        wformsout = wforms;
      }
    }
    return wformsout;
  }

	public int getFrequency (WordForms wforms)
	{
		int tf = 0;
    if (wforms == null)
      return 0;
    for (Iterator e = wforms.iterator(); e.hasNext() ;)
      {
				String key = (String)e.next();
				tf = tf + freqTable.getFrequency(key);
			}
		return tf;
	}

  

	public WordForms getWordForms (String key, boolean csensitive)
	{
		WordForms wforms = new WordForms(key);
    if ( WordQuery.isWildcard(key) )
      return caseTable.getAllPrefixMatches(key, csensitive);
    
		if (csensitive) {
      wforms.addElement(key);
      return wforms;
    }	
		else
		  return caseTable.getAllCases(key);
	}



	/** Return a vector containing all filenames where KEY 
	 *  occurs in the corpus.
   * @param  key   the keyword to search for 
	 */
	public Vector getAllFileNames (String key){
		Vector filenames = new Vector();
    IntegerSet fset = wFilTable.fetch(key);
    for (Iterator f = fset.iterator(); f.hasNext() ;){
      int fno = ((Integer)f.next()).intValue();
      filenames.addElement(fileTable.getFileName(fno));
    }
		return filenames;
	} 

  public void printSortedFreqList (PrintWriter os) {
    freqTable.printSortedFreqList(os);
  }
  
  public String getCorpusDir() {
    return dictProps.getCorpusDir();
  }

  public void printCorcordances(WordQuery query, int ctx, boolean ignx, PrintWriter os) 
  {
    WordForms wforms = query.getWordForms();
    if (wforms == null) {
      os.println(0);
      os.flush();
      return;
    }
    String key =  query.getKeyword();
    int frequency = getFrequency(wforms);
    String cdir = dictProps.getCorpusDir();
    // tell client how many lines we're sending
    os.println(frequency);
    os.flush();
    for (Iterator w = wforms.iterator(); w.hasNext(); ) {
      String word = (String)w.next();
      IntegerSet files =  wFilTable.fetch(word);
      int posl = 0;
      try {
        for (Iterator f = files.iterator(); f.hasNext(); ) {
          Integer fno = (Integer)f.next();
          String fn = fileTable.getFileName(fno.intValue());
          CorpusFile fh = new CorpusFile(cdir+fn);
          fh.setIgnoreSGML(ignx);
          WordPositionTable wpt = new WordPositionTable(environment, 
                                                        ""+fno,
                                                        false);
          IntegerSet pos  = wpt.fetch(word);
          if (pos == null)
            continue;
          //System.err.println("pos="+pos);
          for (Iterator p = pos.iterator(); p.hasNext(); ) {
            Integer bp = (Integer)p.next();
            String ot = fh.getWordInContext(bp, key, ctx);
            if (  query.matchConcordance(ot,ctx) )
              {
                //System.err.println(fn+"|"+bp+"|"+ot);
                os.println(fn+"|"+bp+"|"+ot);
                os.flush();
              }
            if (os.checkError()) {
              logf.logMsg("Dictionary.printCorcordances: connection closed prematurely by client");
              fh.close();
              return;
            }
          }
          fh.close();
          wpt.close();
        }
      }
      catch (IOException e) {
        logf.logMsg("Error reading corpus file ", e);
      }
    }    
    // os.close();
  }

  public String getExtract(String fn, int ctx, long offset, boolean ignx)
  {
    String pre = null;
    String kw = null;
    String pos = null;
    try {
      CorpusFile fh = new CorpusFile(dictProps.getCorpusDir()+fn);
      fh.setIgnoreSGML(ignx);
      pre = fh.getPreContext(offset,ctx);
      pos = fh.getPosContext(offset,ctx);
      int se = pos.length();
      for (int i = 0; i < se; i++)
        if (Character.isWhitespace(pos.charAt(i))) {
          se = i;
          break;
        }
      kw = "<font color='red'><b>"+pos.substring(0,se)+"</b></font>";
      pos = pos.substring(se);
    }
    catch (IOException e) {
      logf.logMsg("Error reading corpus file ", e);
    }
    return pre+kw+pos;
  }    

  public void  dump () {
    System.out.println("===========\n FileTable:\n===========");
    fileTable.dump();
    System.out.println("===========\n Word-File Table:\n===========");
    wFilTable.dump();
    System.out.println("===========\n CaseTable:\n===========");
    caseTable.dump();
    System.out.println("===========\n FreqTable:\n===========");
    freqTable.dump();
    System.out.println("===========\n FileTable:\n===========");
    fileTable.dump();
    System.out.println("===========\n TPosTable:\n===========");
    tposTable.dump();
    int fnos [] = fileTable.getKeys();
    for (int i = 0 ; i < fnos.length; i++){
      System.out.println("===========\n WordPositionTable for "+
                         fileTable.getFileName(fnos[i])+":\n=============");
      WordPositionTable wPosTable = new WordPositionTable(environment, 
                                                          ""+fnos[i],
                                                          false);
      wPosTable.dump();
      wPosTable.close();
    }
  }

  public void close () {
    try {
      tposTable.close();
      freqTable.close();
      wFilTable.close();
      //wPosTable.close();
      caseTable.close();
      fileTable.close();
      environment.close();
    } catch(Exception e) {
      logf.logMsg("Error closing environment: "+e);
    }
  }

  public void finalize () {
    close();
  }

  public boolean getVerbose() {
    return verbose;
  }

  public void setVerbose(boolean v) {
    verbose = v;
  }

}
