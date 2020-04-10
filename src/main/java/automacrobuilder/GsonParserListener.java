/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package automacrobuilder;

/**
 * 
 * @author daike
 */
public interface GsonParserListener {
    public boolean receiver(GsonIterator git, GsonParser.EventType etype, String keyname, Object value, int level);
}
