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
package modnlp.dstruct;

import java.io.*;
/**
 *  General class for random, read-only access to corpus files
 *  (e.g. for accessing context, concordances etc)
 *
 * @author  S Luz &#60;luzs@cs.tcd.ie&#62;
 * @version <font size=-1>$Id: CorpusFile.java,v 1.1 2006/05/22 17:26:02 amaral Exp $</font>
 * @see  
*/
public class CorpusFile extends RandomAccessFile {



  /**
   * The <code>ignoreSGML</code> flag controls whether
   * (SGML/XML-style) markup will be output. If <code>true</code>
   * force character reading methods to skip tags of the form '<.*>'
   * @see setSGMLFlag
   * @see readNextChar
   * @see readPreviousChar
   */
  protected boolean ignoreSGML = true;

	public static final byte WHITESPACE = 32; // code for ws char

  public CorpusFile(String fname) throws IOException{
    super(fname,"r");
  }

  /** get a number (<code>ctx</code>) of characters surrounding 
   *  the word (<code>wrd</code>) strating at <code>position</code>
   *
   * @param wrd   the keyword
   * @param pos   a (randomly-accessible) position in this file
   * @param ctx   the number of characters before and after 
   *              <code>wrd</code> to be returned
   * @return a string of size <code>ctx + ctx + wrd.length()</code> 
   *         (Note: control characters found in the file will be 
   *          replaced by whitespaces and SGML tags will be ignored
   *          if <code>ignoreSGML</code> is <code>true</code>) 
   * @see ignoreSGML
   * @see readPreviousChar
   * @see readBack
   * @see readNextChar
   * @see readByteNoControl
   */
  public String getWordInContext(Integer pos, String wrd, int ctx)
    throws IOException{ 
		
    long filepos = pos.longValue();
    int chaft = ctx+wrd.length();
    byte [] bef = new byte[ctx];
    bef[0] = WHITESPACE; // fill in byte left by backward reading algorithm
    byte [] aft = new byte[chaft];
    String eflag = "";
    seek((filepos));
    for (int i = 1;  i < ctx; i++){ // read backward
      try{
				bef[ctx-i] = readPreviousByte();
      }
      catch (IOException e){ 
				// BOF reached: fill the remaining with spaces
				for (int j = i;  j < ctx; j++)
					bef[ctx-j] = WHITESPACE;
				i = ctx;
      }
    }
    seek((filepos));
    try{      // read from position till end
      for (int i = 0; i < chaft; i++)
				aft[i] = readNextByte();
    }
    catch (EOFException e) {
      eflag = "<eof>";
    }
    String outStr = 
      byteArrayToString(bef)+""+byteArrayToString(aft)+eflag;
    return outStr;
  }
	
  /** get a number (<code>ctx</code>) of characters before
   *  a position
   *
   * @param pos   a (randomly-accessible) position in this file
   * @param ctx   the number of characters before  
   *              <code>position</code> to be returned
   * @return a string of size <code>ctx</code> 
   */
  public String getPreContext(Integer pos, int ctx)
    throws IOException{
    return getPreContext(pos.longValue(),ctx);
  }

  public String getPreContext(long offset, int ctx)
    throws IOException{ 
    long filepos = offset;
    byte [] bef = new byte[ctx];
    for (int j = 0; j < ctx ; j++)// init array 
      bef[j] = WHITESPACE; 
    byte c;
    // read up to position
    seek((filepos));
    for (int i = 1;  i < (ctx-1); i++){
      try 
				{
					bef[ctx-i] = (byte) readPreviousByte();
				}
      catch (IOException e)
				{
					bef[ctx-i] = WHITESPACE;//readPreviousByte();
					i = ctx;
				}
    }
    return byteArrayToString(bef);
  }
	
  /** get a number (<code>ctx</code>) of characters after
   *  a position (offset)
   *
   * @param pos   a (randomly-accessible) position in this file
   * @param ctx   the number of characters after
   *              <code>position</code> to be returned
   * @return a string of size <code>ctx</code>
   * @deprecated  
   */
  public String getPosContext(Integer pos, int ctx)
    throws IOException{
    return getPosContext(pos.intValue(),ctx);
  }

  public String getPosContext(long offset, int ctx)
    throws IOException{ 

    long filepos = offset;
    byte [] aft = new byte[ctx];
    String eflag = "";
    try{
      // read from to position
      seek(filepos);
      for (int i = 0; i < ctx; i++)
				aft[i] = readNextByte();
    }
    catch (EOFException e) {
      eflag = "<eof>";
    }
    
    //String outStr = 
    return ""+byteArrayToString(aft)+eflag;
  }
	
  /** Read next byte in this <code>CorpusFile</code> and 
   *  if it is a control byte, replace it by a whitespace 
   */
  private byte readByteNoControl() 
    throws EOFException, IOException {
		
    byte red = readByte();
    if ( Character.isISOControl((char)red) ) 
      return WHITESPACE;
    else 
      return red;
  }
	
  /** Read previous char in this <code>CorpusFile</code> and 
   *  if <code>ignoreSGML</code> is <code>true</code> skip
   *  areas of the form '<.*>'
   */
  public byte readPreviousByte() 
    throws EOFException, IOException 
  {
    if (! ignoreSGML ){
      return readBack();
    }
    else{
      byte red = readBack();
      boolean ignore;
      if (red == '>') 
				ignore = true;
      else 
				ignore = false;
      while (ignore){
				if ( (red = readBack()) == '<' ){
					//System.err.print("!"+red+"!");
					red = readBack();
					if (red != '>') 
						ignore = false;
				}
      }
      return red;
    }
  }
	
  /** Read the byteacter before the one current pointed
   *  at by FilePointer and move the pointer backwards
   *  1 position
   */
  public byte readBack ()
    throws EOFException, IOException 
  {
    //System.err.print("Point:"+getFilePointer());
		seek(getFilePointer()-1);
		byte red = readByteNoControl();
		seek(getFilePointer()-1);
		return red;
  }
	
  /** Read next byte in this <code>CorpusFile</code> and 
   *  if <code>ignoreSGML</code> is <code>true</code> skip
   *  areas of the form '<.*>'
   */
  public byte readNextByte() 
    throws EOFException, IOException 
  {
    if (! ignoreSGML ){
      return readByteNoControl();
    }
    else{
      byte red = readByteNoControl();
      boolean ignore;
      if (red == '<') 
				ignore = true;
      else 
				ignore = false;
      while (ignore){
				if ( (red = readByteNoControl()) == '>' ){
					//System.err.print("!"+red+"!");
					red = readByteNoControl();
					if (red != '<') 
						ignore = false;
				}
      }
      return red;
    }
  }
	
  /** Set <code>ignoreSGML</code>. Default is <code>false</code>
   * @see ignoreSGML
   */
  public void setSGMLFlag(String yn){
    if (yn.equalsIgnoreCase("no") )
      ignoreSGML = true;
    else
      ignoreSGML = false;
  } 

  public void setIgnoreSGML (boolean v){
    ignoreSGML = v;
  }

  public boolean getIgnoreSGML (){
    return ignoreSGML;
  }


  /** Convert a byte array into a (n internationalized) string 
   */
  public String byteArrayToString (byte [] by)
    throws IOException
  { 
    ByteArrayInputStream  bi = new ByteArrayInputStream(by);
    BufferedReader br
      = new BufferedReader(new InputStreamReader(bi));
    String os = "";
    String tm = null;
    while ( (tm = br.readLine()) != null)
      os += tm;
    return os;
  }
	
	
  protected void finalize() 
    throws Throwable {
    close();
    super.finalize();
  }

}

