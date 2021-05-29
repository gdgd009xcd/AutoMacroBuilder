/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package burp;

import java.awt.Component;
import java.util.ArrayList;

import org.zaproxy.zap.extension.automacrobuilder.generated.MacroBuilderUI;
import org.zaproxy.zap.extension.automacrobuilder.PRequestResponse;
import org.zaproxy.zap.extension.automacrobuilder.ParmGenMacroTraceProvider;

/**
 *
 * @author daike
 */
public class MacroBuilder implements ITab{
    MacroBuilderUI ui = null;

    public MacroBuilder(ParmGenMacroTraceProvider pmtProvider){
        ui = new MacroBuilderUI(pmtProvider);
    }

    @Override
    public String getTabCaption() {
        return "MacroBuilder";
    }

    @Override
    public Component getUiComponent() {
        return ui;
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    // 選択したリクエストを新規追加
    public void addNewRequests(ArrayList <PRequestResponse> _rlist){
        if(ui!=null){
            ui.addNewRequests(_rlist);

        }
    }

    //カレントリクエストをRequest/Responseエリアに表示
    public void updateCurrentSelectedRequestListDisplayContents(){
        ui.updateCurrentSelectedRequestListDisplayContents();
    }
    
    /**
     * get tabindex of current top Tab pane on RequestList TabbedPane
     *
     * @return 
     */
    public int getMacroRequestListTabsSelectedIndex() {
        return ui.getMacroRequestListTabsCurrentIndex();
    }

}
