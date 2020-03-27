/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ambuilder;

import java.awt.Component;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.table.TableCellRenderer;

/**
 *
 * @author tms783
 */

class LineWrapRenderer extends JTextArea implements TableCellRenderer {
  LineWrapRenderer() {
    super();
    setLineWrap(true);
  }
  @Override public Component getTableCellRendererComponent(
        JTable table, Object value, boolean isSelected, boolean hasFocus,
        int row, int column) {
    if(isSelected) {
      setForeground(table.getSelectionForeground());
      setBackground(table.getSelectionBackground());
    }else{
      setForeground(table.getForeground());
      setBackground(table.getBackground());
    }
    setText((value == null) ? "" : value.toString());
    return this;
  }
}