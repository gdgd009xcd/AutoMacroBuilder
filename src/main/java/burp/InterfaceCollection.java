/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package burp;

/**
 *
 * @author youtube
 * @param <T>
 */
public interface InterfaceCollection<T> extends Iterable<T> {
    int size();
    void addToken(int _tokentype, String url, String name, String value, int fcnt);
}