/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.zaproxy.zap.extension.automacrobuilder;

/**
 *
 * @author daike
 */
class PathComparator<T extends CookiePathValue> implements java.util.Comparator<T> {
    @Override
    public int compare(T s, T t) {
        String spath = s.getPath();
        String tpath = t.getPath();
        //
        // s > t  : >0
        // s ==t  : =0
        // s < t  : <0
        if(spath != null && tpath == null){
            return 1;
        }
        if(spath == null && tpath != null){
            return -1;
        }

        if(spath != null && tpath != null){
            return spath.compareTo(tpath);
        }
        return 0; 
    }
}

