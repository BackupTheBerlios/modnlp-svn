/**
 *   Copyright (c) 2011 G Lynch. All Rights Reserved.
 *                 2012 G Lynch, S Luz
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

import javax.swing.table.AbstractTableModel;
import java.util.*;
import java.io.*;
public class ExampleTableModel extends AbstractTableModel
{
  private Vector <String> columnNames; 
  private Vector <Object []> data; 
     
  public ExampleTableModel(Vector<String> cols, Vector<Object []> dat){

    columnNames = cols;
    data = dat;

  }

  public Class getColumnClass( int column ) 
  {
    return getValueAt(0, column).getClass();
  }
    

  public boolean isCellEditable(int row, int col) {
    // Note that the data/cell address is constant,

    // no matter where the cell appears onscreen.

    // return false;
    boolean edit = true;
    Boolean b = (Boolean)getValueAt(row,3);
    if(col >= 2){
      edit = false;
    }
    if((b.booleanValue() == true)){
      edit = false;
    }
    return edit;
  }
  public int getColumnCount() 
  {
    return columnNames.size();
  }
  public String getColumnName( int column ) 
  {
    return columnNames.get(column);
  }
  public int getRowCount() 
  {
    return data.size();
  }
  public void removeRow(int row) {
    data.removeElementAt(row);
    fireTableDataChanged();
    updateIndexes();
  }
  public void insertRow(Object [] r, int row){
    data.add(row,r);
    fireTableRowsInserted(row,row);
    updateIndexes();


  }

  public void moveSegmentUp(int row1){

    Object [] r1 = data.get(row1);
    Object [] r2 = data.get(row1 -1);
    data.set(row1 -1,r1);
    data.set(row1,r2);
    fireTableDataChanged();
    updateIndexes();

  }

  public void moveSegmentDown(int row1){

    Object [] r1 = data.get(row1);
    Object [] r2 = data.get(row1 + 1);
    data.set(row1 + 1,r1);
    data.set(row1, r2);
    fireTableDataChanged();
    updateIndexes();
  }







  public void mergeRows(int row1, int row2){
    Object [] r1 = data.get(row1);
    Object [] r2 = data.get(row2);
    Object [] r3 = new Object[r1.length];
    String firstsource = (String)r1[0] ;
    String secondsource = (String)r2[0];
    String firsttarget = (String)r1[1];
    String secondtarget = (String)r2[1] ;
       
    r3[0] = firstsource + secondsource;
    r3[1] = firsttarget + secondtarget;
    removeRow(row2);
    removeRow(row1);
       
    r3[2] = "0.0";
    insertRow(r3,row1);
    updateIndexes();

  }
  public void mergeSource(int row1, int row2){
    Object [] r1 = data.get(row1);
    Object [] r2 = data.get(row2);
    r1[0] = r1[0].toString() + r2[0].toString();
    r2[0] = "";
    r2[2] = "0.0";
    r1[2] = "0.0";
    updateRow(r1,row1);
    updateRow(r2,row2);
    fireTableRowsUpdated(row1,row2);
    updateIndexes();


  }
  public void mergeTarget(int row1,int row2){
    Object [] r1 = data.get(row1);
    Object [] r2 = data.get(row2);
    r1[1] = r1[1].toString() + r2[1].toString();
    r2[1] = "";
    r2[2] = "0.0";
    r1[2] = "0.0";
    updateRow(r1,row1);
    updateRow(r2,row2);
    fireTableRowsUpdated(row1,row2);
    updateIndexes();

  }
  public void updateRow(Object [] s,int row){
    data.set(row,s);
     

  }
  public void updateIndexes(){
    String is;
    Object[] o;
    	
    for(int i = 0;i < data.size();i++){
      o = data.get(i);
      is = (String)o[4];
      is = new String ("" + i);
      o[4] = is;
      data.set(i,o);
    		
    }
    	
  }
  public void lockRow(int row){

    Object [] a = data.get(row);
    Boolean b = new Boolean(true);
    a[3] = b;
    data.set(row,a);
    fireTableCellUpdated(row,3);
  }
  public void unlockRow(int row){

    Object [] a = data.get(row);
    Boolean b = new Boolean(false);
    a[3] = b;
    data.set(row,a);
    fireTableCellUpdated(row,3);
  }
  public Object [] StringArrayToObject(String [] sa){

    Object [] oa = new Object [sa.length];

    for(int i = 0;i < sa.length;i++){

      oa[i] = sa[i];

    }
     
    return oa;

  }
  public void setValueAt(Object o, int row, int column){
    Object [] r = data.get(row);
         
    r[column] = (String) o;
          
    data.set(row,r);
         
    fireTableCellUpdated(row,column);
  }
  public Object getValueAt( int row, int column ) 
  {
    Object [] sa = data.get(row);
    return sa[column];
  }
  public Object [] getRow(int row){
    return data.get(row);
  }
  public void flush(){
    data.clear();
    fireTableDataChanged();
  }
}

