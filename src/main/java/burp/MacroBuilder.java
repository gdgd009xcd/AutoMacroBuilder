/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package burp;

import java.awt.Component;
import java.util.ArrayList;

/**
 *
 * @author daike
 */
public class MacroBuilder implements ITab{
    MacroBuilderUI ui = null;
    ParmGenMacroTrace pmt = null;

    MacroBuilder(ParmGenMacroTrace _pmt){
        pmt = _pmt;
        ui = new MacroBuilderUI(pmt);
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
    void addNewRequests(ArrayList <PRequestResponse> _rlist){
        if(ui!=null){
            ui.addNewRequests(_rlist);

        }
    }

    //カレントリクエストをRequest/Responseエリアに表示
    void updateCurrentReqRes(){
        ui.updateCurrentReqRes();
    }

}
