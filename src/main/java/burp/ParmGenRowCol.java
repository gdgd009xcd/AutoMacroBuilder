/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package burp;

import java.util.Objects;

/**
 *
 * @author daike
 */
public class ParmGenRowCol {
    private int row = -1;
    private int  col = -1;
    
    
    ParmGenRowCol(int r, int c){
        row = r;
        col = c;
    }
    
    int getRow(){
        return row;
    }
    
    int getCol(){
        return col;
    }
    
    // HashMap
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ParmGenRowCol) {
            ParmGenRowCol key = (ParmGenRowCol) obj;
            return this.row == key.row && this.col == key.col;
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(row, col);
    }
}
