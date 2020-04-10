/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.zaproxy.zap.extension.automacrobuilder;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 *
 * @author youtube
 */
public class ParmGenStack<T> extends ArrayDeque<T> {
    T getCurrent(){
        return peekFirst();
    }
    
}
