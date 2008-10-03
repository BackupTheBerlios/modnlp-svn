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
package modnlp.tec.client;
import java.lang.*;
import java.util.StringTokenizer;
/**
 *  Manage the array of concordances returned by the server.
 *  Consistng mostly of legacy code (inherited from Ralf's earliest
 *  implementation), this class is in urgent need of a major
 *  overhaul.
 * 
 * @author  S. Luz &#60;luzs@acm.org&#62;
 * @version <font size=-1>$Id: ConcArray.java,v 1.3 2003/06/22 13:48:05 luzs Exp $</font>
 * @see ContextClient */
public class ConcArray {

  public static final int arraymax = 15000;
  public int index = 0;
  public String SEPTOKEN = " |'`\"-_,.?!;:<>{}[]=+/\\%$*&()";

  public ConcordanceObject[] concArray = new ConcordanceObject[arraymax];
  public int longFileName = 0;


  public ConcArray () {
    super();
    for(int i = 0; i < arraymax ; i++)
      concArray[i] = null;
  }
  
  public boolean assertElement(String inputString)
  {
    if (index >= arraymax)  // too many concordances!!! 
      return false;
    
    ConcordanceObject co = new ConcordanceObject(inputString);
    
    if( co.concordance.equals("null") )
      {
        return false;
      }
    else {
      co.index = index;
      concArray[index] = co;
      index++;
      return true;
    }
  }
  
  public boolean assertElement(String inputString, int srtctx, int halfctx)
  {
    //System.out.println("HERE|"+inputString);
    if (index >= arraymax)  // too many concordances!!! 
      return false;

		ConcordanceObject co = new ConcordanceObject(inputString);
    if( co.concordance.equals("null") || co.concordance.equals("") )
      {
				return false;
      }
		else {
			co.index = index;
			co.sortContextHorizon = srtctx;
			co.halfConcordance = halfctx;
			concArray[index] = co;
			index++;
			return true;
		}
	}

	public String getConcordance(int i){
		return concArray[i].concordance;
	}

	public String getFilename(int i){
		return concArray[i].filename;
	}

	public int getFilePos(int i){
		return concArray[i].filepos;
	}

  public void setSortContextHorizonFlag(int ctx)
  {
    for(int count = 0; count < arraymax && concArray[count] != null; count++)
      {
				concArray[count].sortContextHorizon = ctx;
      }
  }

  public int getLengthLongestFname()
  {
    int lfn = 0;

    for(int count = 0; count < arraymax && concArray[count] != null; count++)
      {
				if ( concArray[count].filename.length() > lfn )
					lfn = concArray[count].filename.length();
      }
    longFileName = lfn;
    return lfn;
  }

  public int getLengthLongestConc()
  {
    int lcn = 0;
		
    for(int count = 0; count < arraymax && concArray[count] != null; count++)
      {
				if ( concArray[count].concordance.length() > lcn )
					lcn = concArray[count].concordance.length();
      }
    return lcn;
  }

  public int getLengthShortestConc()
  {
    int lcn = this.getLengthLongestConc();
		
    for(int count = 0; count < arraymax && concArray[count] != null; count++)
      {
				if ( concArray[count].concordance.length() < lcn )
					lcn = concArray[count].concordance.length();
      }
    return lcn;
  }

  public int getLengthLongestPrefix(String key, int maxCont)
  {
    int max = 0;
    int[] keyp = this.getWordPosition(key,maxCont);
   
    for(int count = 0; count < arraymax && concArray[count] != null; count++)
      {
				if ( keyp[count] > max )
					max = keyp[count];
      }
    return max;
  }
	
  public int[] getWordPosition(String word, int ctxt)
  {
    int[] pos = new int[index+1];
    for(int count = 0; count < arraymax && concArray[count] != null; count++)
      {
				pos[count] = 0;
				String lowc = concArray[count].concordance.toLowerCase();
				String key  = word.toLowerCase();
				int    keySt = 0;
				int    lastK = 0;
				
				while ( pos[count] == 0 ){
					keySt = lowc.indexOf(" "+key,keySt+1);
					if (keySt > 0){
						lastK = keySt+1;
						StringTokenizer pfx = 
							new StringTokenizer(lowc.substring(0,keySt),SEPTOKEN,false);
						if ( pfx.countTokens() >= ctxt ) 
							pos[count] = keySt+1;
					}
					else {
						pos[count] = -1;
					}  
				}
				if (pos[count] == -1 ){
					System.out.println("Warning: bad concordance found #"+count);
					pos[count] = lastK;
				}
      }
    return pos;
  }
	
	
  private void dumpValues()
  {
		
    for(int count = 0; count < arraymax && concArray[count] != null; count++)
      {
				System.out.println("Concordance: "+ concArray[count].concordance);
				System.out.println("Filename: "+ concArray[count].filename);
				System.out.println("Filepos: "+ concArray[count].filepos);
      }
		
    return;
  }
	
	
	
  public static void main (String args[])
  {
    ConcArray example = new ConcArray();
    String inputString = "                to a storage house or further away |ngugeffy.092|521";
		
    example.assertElement(inputString);
    example.dumpValues();
		
  }

}
