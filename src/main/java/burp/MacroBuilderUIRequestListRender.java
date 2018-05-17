/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package burp;

import java.awt.Color;
import java.awt.Component;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JLabel;
import javax.swing.JList;

/**
 *
 * @author daike
 */
public class MacroBuilderUIRequestListRender extends DefaultListCellRenderer{
    ParmGenMacroTrace pmt;
    
    MacroBuilderUIRequestListRender(MacroBuilderUI _ui){
        pmt = _ui.getParmGenMacroTrace();
    }
    
    @Override
    public Component getListCellRendererComponent(JList list, Object value,
            int index, boolean isSelected, boolean cellHasFocus) {
        JLabel label = (JLabel) super.getListCellRendererComponent(list, value,
                index, isSelected, cellHasFocus);

        
            if (list.isSelectedIndex(index)) {
                // 選択行はデフォルトの色
                label.setBackground(list.getSelectionBackground());
            }else{
                label.setBackground(list.getBackground());
            }
            if(pmt.isDisabledRequest(index)){
                label.setForeground(Color.LIGHT_GRAY);
            }else if(pmt.isCurrentRequest(index)){
                label.setForeground(Color.BLUE);
            }else if(pmt.isError(index)){
                label.setForeground(Color.RED);
            }else{
                label.setForeground(list.getForeground());
            }
        return label;
    }
}
