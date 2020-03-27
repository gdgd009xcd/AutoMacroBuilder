/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ambuilder;

import ambuilder.ParmGenToken;
import java.util.ArrayList;

/**
 *
 * @author youtube
 */
public class ParmGenArrayList extends ArrayList<ParmGenToken> implements InterfaceCollection<ParmGenToken>{

    
    
    
    public void addToken(AppValue.TokenTypeNames _tokentype, String url, String name, String value, Boolean b, int fcnt){
        ParmGenToken tkn = new ParmGenToken( _tokentype,  url,  name,  value, b, fcnt);
        add(tkn);
    }
    
    @Override
    public int size(){
        return super.size();
    }
    
}
