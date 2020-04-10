/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.zaproxy.zap.extension.automacrobuilder;

/**
 *
 * @author tms783
 */
public interface InterfaceRegex {
    public String getRegex();
    public String getOriginal();
    public void setRegex(String regex);
}
