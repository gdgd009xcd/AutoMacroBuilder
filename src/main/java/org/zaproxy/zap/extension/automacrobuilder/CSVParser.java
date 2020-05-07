/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.zaproxy.zap.extension.automacrobuilder;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author daike
 */
public class CSVParser {
    static Pattern pattern = ParmGenUtil.Pattern_compile("(\"[^\"]*(?:\"\"[^\"]*)*\"|[^,\"]*)[ \t]*?,");
	static Matcher matcher = null;
	static String term = "A-----fd43214234897234----------~Terminator_---------89432091842390fdsaf---Z";

	static void Parse(String rdata){
		rdata = rdata.replaceAll("(?:\\x0D\\x0A|[\\x0D\\x0A])?$", ",")  + term;
		matcher = pattern.matcher(rdata);
	}

	static boolean getField(CSVFields csvf){
		if(matcher.find()) {
			csvf.field = matcher.group(1);
			csvf.field = csvf.field.trim();
			csvf.field = csvf.field.replaceAll("^\"(.*)\"$", "$1");
			csvf.field = csvf.field.replaceAll("\"\"", "\"");
			if ( csvf.field.equals(term)){
				return false;
			}
			return true;
		}

		return false;
	}
        
        static class CSVFields{
            public String field;
        }
}
