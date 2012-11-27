/**
 *   Copyright (c) 2011-12 G Lynch. All Rights Reserved.
 *
 *   This program  is free software; you can redistribute it and/or
 *   modify it under the terms of the GNU General Public License
 *   as published by the Free Software Foundation; either version 2
 *   of the License, or (your option) any later version.
 *   
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
     
 *   You should have received a copy of the GNU General Public License
 *   along with this program; if not, write to the Free Software
 *   Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 **/ 


package modnlp.capte;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;
import java.util.Vector;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.httpclient.methods.multipart.StringPart;

public class AlignerUtils {
  /*This method should create a new filename given a filename, extension and postfix string
   *	Used for creating tmp files etc
   * 
   */
	
  public static void convertLineEndings(String inputfile, String outputfile){
    /* This is a quick hackaround to convert the line endings from
     *  Windows files into Unix line endings so that the sentence splitter will work
     *  Seems to work, but hasn't been extensively tested.
     */
    try{
      File input = new File(inputfile);
      File output = new File(outputfile);
      BufferedReader bb = new BufferedReader( new InputStreamReader(new FileInputStream(inputfile), "UTF8"));
      PrintWriter bv = new PrintWriter(new OutputStreamWriter(new FileOutputStream(outputfile),"UTF8"));
      String l = "";
      while(bb.ready()){
        l = bb.readLine();
        bv.write(l + "\n");
      }

      bb.close();
      bv.close();
    }
    catch(IOException e){
      e.printStackTrace();
    }
  }

  /*Turn the two columns of the data vector into two new output
   * files ready to be sent to the aligner.
   */
  public static void reWriteAlignment(String sourcename, String targetname, Vector<Object[]> d){
		
    try{
      FileOutputStream op = new FileOutputStream(sourcename);
      Writer out = new OutputStreamWriter(op, "UTF-8");
      FileOutputStream sp = new FileOutputStream(targetname);
      Writer sout = new OutputStreamWriter(sp, "UTF-8");
      String source = "";
      String target = "";
      Object [] stemp ;
      for(int i =0;i<d.size();i++){
        stemp = d.get(i);
        source = (String) stemp[0];
        target = (String) stemp[1];
        source = clean(source);
        target = clean(target);
        out.write(source);
        out.write("\n");
        sout.write(target);
        sout.write("\n");
        System.out.println(source);
        System.out.println(target);

      }
      out.close();
      sout.close();
    }
    catch(IOException e){
      e.printStackTrace();
    } 
  }

  @SuppressWarnings("deprecation")
  /* This method creates the HTTP connection to the server 
   * and sends the POST request with the two files for alignment,
   * 
   * The server returns the aligned file as the response to the post request, 
   * which is delimited by the tag <beginalignment> which separates the file
   * from the rest of the POST request
   * 
   * 
   * 
   */
    public static String MultiPartFileUpload(String source, String target) throws IOException{
    String response = "";
    String serverline =  System.getProperty("capte.align.server"); //"http://ronaldo.cs.tcd.ie:80/~gplynch/aling.cgi";   
    PostMethod filePost = new PostMethod(serverline);
    // Send any XML file as the body of the POST request
    File f1 = new File(source);
    File f2 = new File(target);
    System.out.println(f1.getName());
    System.out.println(f2.getName());
    System.out.println("File1 Length = " + f1.length());
    System.out.println("File2 Length = " + f2.length());
    Part[] parts = {
	        		
      new StringPart("param_name", "value"),
      new FilePart(f2.getName(),f2),
      new FilePart(f1.getName(),f1)
    };
    filePost.setRequestEntity(
                              new MultipartRequestEntity(parts, filePost.getParams())
                              );
    HttpClient client = new HttpClient();
    int status = client.executeMethod(filePost);
    String res = "";
    InputStream is = filePost.getResponseBodyAsStream();
    Header [] heads = filePost.getResponseHeaders();
    Header [] feet = filePost.getResponseFooters();
    BufferedInputStream bis = new BufferedInputStream(is);
	        
    String datastr = null;
    StringBuffer sb = new StringBuffer();
    byte[] bytes = new byte[ 8192 ]; // reading as chunk of 8192
    int count = bis.read(bytes);
    while( count != -1 && count <= 8192 )
      {
        datastr = new String(bytes,"UTF8");
	                         
        sb.append(datastr);
        count = bis.read( bytes );
      }
	        
    bis.close();
    res = sb.toString();
	              
    System.out.println("----------------------------------------");
    System.out.println("Status is:" + status);
    //System.out.println(res);
    /* for debugging
    for(int i = 0 ;i < heads.length ;i++){
      System.out.println(heads[i].toString());
	        
    }
    for(int j = 0 ;j < feet.length ;j++){
      System.out.println(feet[j].toString());
	        
    }
    */
    filePost.releaseConnection();
    String [] handle = res.split("<beginalignment>");
    //Check for errors in the header
    // System.out.println(handle[0]);
    //Return the required text
    String ho = "";
    if(handle.length < 2){
      System.out.println("Server error during alignment, check file encodings");
      ho = "error";
    }
    else{
      ho = handle[1];
    }
    return ho;
	    
  }
	
  public static String createNewFileName(String postfix,String orig,String extension)
  {		
    int offset = 0;
    String sub = "";
    if(orig.indexOf(extension) >  -1){

      offset = orig.indexOf(extension);
      sub = orig.substring(0,offset);
      sub = sub + postfix;
      sub = sub + extension;

    }
    else{
      sub = orig + postfix;
    }
    return sub;
  }
	
  /*
   * This method converts the raw string from the HTTP POST request reply into
   * a data structure that can be displayed in the editor window.
   * TODO: Check for artifacts introduced during this process.
   */
  public static Vector<Object []> StringToData(String conv,boolean isRe,int number){
    String alpha = "abcdefghikjlmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ23456789";
    int counter = 0;
    Vector<Object[]> va  = new Vector<Object[]>();

    String [] lines = conv.split("\n");

    Object [] oa;
    String [] sa;
    for(int i = 0; i < lines.length;i++){
      //System.out.println(lines[i] + "<END>");
      if(lines[i].indexOf("\t") > -1){
	sa = lines[i].split("\t");

	if(sa.length == 3){
          sa[0] = removeLongWhiteSpace(sa[0]);
          sa[1] = removeLongWhiteSpace(sa[1]);
          sa[2] = removeLongWhiteSpace(sa[2]);
          oa = new Object[6];
          oa[0] = sa[0];
          oa[1] = sa[1];
          oa[2] = sa[2];
          oa[3] = new Boolean(false);
          oa[4] = Integer.toString(counter);
          if(isRe){
            if(number < alpha.length()){
              oa[5] = Integer.toString(counter) + " " + "(" + alpha.charAt(number - 1) + ")" ;
            }
            else{
              oa[5] = Integer.toString(counter) + " " + "(" + alpha.charAt((int)Math.round(Math.random())) + ")" ;	
            }
          }
          else{
            oa[5] = Integer.toString(counter)	;
          }
          va.add(oa);
          counter++;
	}
	else{
          for(int j = 0;j <sa.length;j++){
            System.out.println(sa[j] + " " + j);
          }
	}
      }
    }
    return va;
  }
	
  /*
   * This method removes long stretches of whitespace in the text returned by hunalign
   *  Also tries to remove non-UTF8 characters
   */
  public static String removeLongWhiteSpace(String s) {
    s = s.trim();
    while(s.contains("  ")) // two white spaces
      {
        s = s.replaceAll("  "," "); // the first arg should contain 2 spaces, the second only 1
      }
    //System.out.println("Length before UTF8 cleaning " + s.length());
    CharsetDecoder utf8Decoder = Charset.forName("UTF-8").newDecoder();
    utf8Decoder.onMalformedInput(CodingErrorAction.IGNORE);
    utf8Decoder.onUnmappableCharacter(CodingErrorAction.IGNORE);
    try{
      ByteBuffer bytes ;
      bytes = ByteBuffer.wrap(s.getBytes());
      CharBuffer parsed = utf8Decoder.decode(bytes);
      s = parsed.toString();		
    }catch(Exception e){
      e.printStackTrace();
    }
    //This code removes all of the bytes with value of zero
    // Gerard 21st March 2012: Solved, bug with random spaces in segments
    byte [] b = s.getBytes();
    Vector <Byte> vb = new Vector() ;
		
    for(int i = 0;i < b.length;i++)
      {
        if(b[i] != 0){
          vb.add(b[i]);
        }
      }
    Object [] ba = vb.toArray();
    Byte [] bya = new Byte[ba.length];
    byte [] byta = new byte[ba.length];
    for(int i = 0;i < ba.length;i++){
      bya[i] = (Byte) ba[i];
      byta[i] = bya[i].byteValue();
    }
    s = new String(byta);
    return s;
  }
	
  /*
   * Method for removing markup added by hunalign
   * when exporting to files for realignment
   *
   */
  public static String clean(String s){

    if(s != null && s != ""){

      s = s.replaceAll("~~~","");
      //s = s.replaceAll("***","");
      s = s.replaceAll("<P>","");

    }
    else{

      s = "";
    }
    return s;

  }
	
  public static void writeAlignment(String sourcename, Vector<Object[]> d){
    //Output the current alignment as a tab separated file
    String sp = "" ;	

    BufferedWriter pw;
    try {
      pw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(sourcename),"UTF-8"));

      //PrintWriter tp = new PrintWriter(new OutputStreamWriter(new FileOutputStream(targetname),"UTF8"));
      String source = "";
      String target = "";
      Object [] stemp ;

      for(int i =0;i<d.size();i++){
        stemp = d.get(i);
        source = (String) stemp[0];
        target = (String) stemp[1];
        source = clean(source);
        target = clean(target);
        sp += source + "\t" + target + "\n";

      }

      pw.write(new String(UnicodeUtil.convert(sp.getBytes(), "UTF-8")));
      pw.close();
      //return sp;
    }
    catch(Exception e){
      e.printStackTrace();
    }
  }
}
