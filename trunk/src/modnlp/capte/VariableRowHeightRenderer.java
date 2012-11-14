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

import java.awt.Component;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;

public class VariableRowHeightRenderer extends JLabel implements TableCellRenderer {
  public VariableRowHeightRenderer() {
    super();
    setOpaque(true);
    setHorizontalAlignment(JLabel.CENTER);
  }

  public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
      boolean hasFocus, int row, int column) {
    if (isSelected) {
      setBackground(UIManager.getColor("Table.selectionBackground"));
    }

    if (hasFocus) {
      setBorder(UIManager.getBorder("Table.focusCellHighlightBorder"));

      if (table.isCellEditable(row, column)) {
        super.setForeground(UIManager.getColor("Table.focusCellForeground"));
        super.setBackground(UIManager.getColor("Table.focusCellBackground"));
      }
    } else {
      setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
    }
    setText((String) (value));
    table.setRowHeight(row, getPreferredSize().height + row * 10);
    return this;
  }
}

