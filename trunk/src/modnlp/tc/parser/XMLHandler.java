package modnlp.tc.parser;
import modnlp.tc.parser.*;
import modnlp.tc.dstruct.*;
import java.io.*;
import org.xml.sax.*;
import java.util.Vector;
import java.util.Enumeration;
import uk.co.wilson.xml.MinML2;
/**
 * Handles events generated by a Reuters file through a SAX parser,
 * and store the results as a ParsedCorpus object.
 * 
 * @author  Saturnino Luz &#60;luzs@acm.org&#62;
 * @version <font size=-1>$Id: XMLHandler.java,v 1.1.1.1 2005/05/26 13:59:30 amaral Exp $</font>
 * @see  NewsParser
*/

public class XMLHandler extends MinML2
{

  private String content = null;
  private String reutersid = null;
  private int error = 0;
  private boolean openCategory = false;
  private boolean openText = false;
  private ParsedDocument parsedNewsItem = new ParsedDocument();
  private ParsedCorpus parsedText = null;
  private String filename;
  /**
   * Categories annotated in REUTERS-21578
   */
  private static final String[] categoryArray =  {"TOPICS","PEOPLE",
                                                  "PLACES","ORGS"};
  private static final String[] textArray =  {"TITLE","BODY"};
  private static final String newsTag = "REUTERS";


  /** 
   *  Set up parser
   */
  public  XMLHandler(String fn) {
    this.filename = fn;
  }
  
  /** 
   * parseNews: Set up parser object, perform parsing
   */
  public void  parse ()
  {
    try {
      FileInputStream in = new FileInputStream(filename);
      InputSource source = new InputSource(in);
      
      source.setEncoding("ISO-8859-1");
      parsedText = new ParsedCorpus();
      parse(source);
      System.err.println("xml parsed ");
    }
    catch (Exception e) 
      {
        System.err.println("Error parsing "+filename);
        e.printStackTrace();
      }
  }

  /**
   * Callback method activated when an XML start element 'event' occurs
   *
   * @see org.xml.sax.HandlerBase
   */
  public void startElement (final String namespaceURI,
                              final String localName,
                              final String name,
                              final Attributes atts) 
  {
    if ( newsItemElement(name) )
      reutersid = atts.getValue("NEWID");
    if ( categoryElement(name) )
      openCategory = true;
    else if ( textElement(name) )
      openText = true;
    else
      content = null;
  }
  
  /**
   * Callback method activated when an XML end element 'event' occurs
   *
   * @see org.xml.sax.HandlerBase
   */
  public void endElement (final String namespaceURI,
                            final String localName,
                            final String name)
  {
    if (name.equals("D") && (content != null) )
      parsedNewsItem.addCategory(new String(content));
    if ( categoryElement(name) )
      {
        openCategory = false;
        if (content != null) 
          parsedNewsItem.addCategory(new String(content));
      }
    if ( textElement(name) )
      {
        openText = false;
        if (content != null) 
          parsedNewsItem.addText(new String(content));
      }
    content = null;
    if ( newsItemElement(name) ) {
      parsedNewsItem.setId(reutersid);
      parsedText.addParsedDocument(parsedNewsItem);
      parsedNewsItem = new ParsedDocument();
    }
  }
  
  
  /**
   * Callback for PCDATA events. Store text content from <code>BODY</code> tags.
   *
   * @param ch[] array containing XML PCDATA (text)
   * @param start where the text starts
   * @param length where it ends
   * @see org.xml.sax.HandlerBase
   */
  public void characters (char ch[], int start, int length)
  { 
    if ( openCategory || openText )
      content = new String(ch,start,length);
  }
  
  /**
   *  Utilities
   */
  private  boolean newsItemElement (String name)
  {
    return name.equals(newsTag);
  }
 
  private  boolean categoryElement (String name) 
  {
    
    for (int i = 0; i < categoryArray.length; i++)
      if ( (name.toUpperCase()).equals(categoryArray[i]) )
        return true;
    return false;
  }

  private  boolean textElement (String name) 
  {
    
    for (int i = 0; i < textArray.length; i++)
      if ( (name.toUpperCase()).equals(textArray[i]) )
        return true;
    return false;
  }
  
  private String fixElementName (String name) {
    
    StringBuffer out = new StringBuffer();
    out.append( Character.toUpperCase(name.charAt(0)) );
    for (int i = 1; i <  name.length(); i++ )  
      if ( Character.isUpperCase(name.charAt(i)) ) 
        out.append(" " + name.charAt(i) );
      else
        out.append( name.charAt(i) );    
    return out.toString();
  }


 /**
   * Print a message for ignorable whitespace.
   *
   * @see org.xml.sax.DocumentHandler#ignorableWhitespace
   */
  public void ignorableWhitespace (char ch[], int start, int length)
  {
    //System.out.print("Ignorable Whitespace found ");
  }
  
  /**
   * Report all warnings, and continue parsing.
   *
   * @see org.xml.sax.ErrorHandler#warning
   */
  public void warning (SAXParseException exception)
  {
    error++;
    System.err.print("Warning: " +
                   exception.getMessage() +
                   " (" +
                   exception.getSystemId() +
                   ':' +
                   exception.getLineNumber() +
                   ',' +
                   exception.getColumnNumber() +
                   ')' + "\n");
  }
  
  
  /**
   * Report all recoverable errors, and try to continue parsing.
   *
   * @see org.xml.sax.ErrorHandler#error
   */
  public void error (SAXParseException exception)
  {
    error++;
    System.err.print("Recoverable Error: " +
                   exception.getMessage() +
                   " (" +
                   exception.getSystemId() +
                   ':' +
                   exception.getLineNumber() +
                   ',' +
                   exception.getColumnNumber() +
                   ')' + "\n");
  }
  
  /**
   * Report all fatal errors, and try to continue parsing.
   *
   * <p>Note: results are no longer reliable once a fatal error has
   * been reported.</p>
   *
   * @see org.xml.sax.ErrorHandler#fatalError
   */
  public void fatalError (SAXParseException exception)
  {
    error++;
    System.err.print("Fatal Error: " +
                   exception.getMessage() +
                   " (" +
                   exception.getSystemId() +
                   ':' +
                   exception.getLineNumber() +
                   ',' +
                   exception.getColumnNumber() +
                   ')' + "\n");
  }


  /**
   * Implement the Parser interface
   */
  public ParsedCorpus getParsedCorpus () {
    if (parsedText == null)
      parse();
    return parsedText;
  }

}

