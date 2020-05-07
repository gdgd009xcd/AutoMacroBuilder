/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2020 The ZAP Development Team
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.zaproxy.zap.extension.automacrobuilder;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;

/** @author daike */
//
// class FileReadLine
//
public class FileReadLine {
    String csvfile;
    String seekfile;
    RandomAccessFile raf = null;
    long seekp;
    int current_line;
    boolean saveseekp;
    ArrayList<String> columns;

    public FileReadLine(String _filepath, boolean _saveseekp) {
        csvfile = _filepath;
        seekfile = csvfile + "_L_";
        raf = null;
        seekp = 0;
        saveseekp = _saveseekp;
        current_line = 0;
        columns = null;
    }

    public String getFileName() {
        return csvfile;
    }

    public void rewind() {
        if (saveseekp) {
            try {
                FileWriter filewriter = new FileWriter(seekfile, false);
                String s1 = String.valueOf(0);
                filewriter.write(s1);
                filewriter.close();
            } catch (Exception e) {
                //
                ParmVars.plog.printException(e);
            }
        }
        seekp = 0;
        current_line = 0;
    }

    /**
     * <code>RandomAccessFile.read</code>で読み込んだバイト配列を _encエンコードした<code>String</code>で返します。
     * EOFに達するとnullを返します。
     *
     * @param f
     * @return
     * @throws IOException
     */
    String readLineRandomAccessFileCharset(RandomAccessFile f) throws IOException {
        ParmGenBinUtil barray = new ParmGenBinUtil();
        byte[] onebyte = new byte[1];
        int c = -1;
        boolean eol = false;
        while (!eol) {
            switch (c = f.read()) {
                case -1: // EOFに達した場合
                case '\n':
                    eol = true;
                    break;
                case '\r':
                    eol = true;
                    long cur = f.getFilePointer();
                    if ((f.read()) != '\n') {
                        f.seek(cur);
                    }
                    break;
                default:
                    onebyte[0] = (byte) c;
                    barray.concat(onebyte);
                    break;
            }
        }

        if ((c == -1) && (barray.length() == 0)) {
            return null;
        }

        return new String(barray.getBytes(), ParmVars.enc.getIANACharset());
    }

    public ArrayList<String> readColumns() {
        if (columns == null) {
            columns = new ArrayList<String>();
        }
        columns.clear();
        String dummy = readLine(0, 99999, null);
        if (columns.size() > 0) {
            return columns;
        }
        return null;
    }

    int skipLine(int l) {
        if (l >= 0) {
            rewind();
            columns = null;
            while (l-- > 0) {
                String dummy = readLine(0, 1, null);
                if (dummy == null) {
                    break;
                }
            }
        } else {
            return -1;
        }
        return current_line;
    }

    String readLine(int _valparttype, int _pos, AppParmsIni _parent) {
        if (saveseekp) {
            seekp = 0;
            current_line = 0;
        }
        String line = null;
        try {
            if (saveseekp) { // seekp保存
                try {
                    // seekfile読み込み
                    FileReader fr = new FileReader(seekfile);
                    BufferedReader br = new BufferedReader(fr);
                    String rdata;
                    String alldata = "";
                    while ((rdata = br.readLine()) != null) {
                        rdata = rdata.replace("\r", "");
                        rdata = rdata.replace("\n", "");
                        alldata += rdata;
                    }
                    String[] slvalue = alldata.split(":");
                    seekp = Long.valueOf(slvalue[0]);
                    if (slvalue.length > 1) {
                        current_line = Integer.parseInt(slvalue[1]);
                    }
                    fr.close();
                } catch (Exception e) {
                    // NOP
                }
            }

            // csvファイルseek
            raf = new RandomAccessFile(csvfile, "r");

            if (raf.length() <= seekp) {
                ParmVars.plog.debuglog(1, "seekp reached EOF\n");
                raf.close();
                raf = null;
                return null;
            }

            raf.seek(seekp);

            // csvファイル１レコード読み込み
            // line = raf.readLine();
            line = readLineRandomAccessFileCharset(raf);
            String _col = line;

            CSVParser.Parse(line);

            CSVParser.CSVFields csvf = new CSVParser.CSVFields();
            while (CSVParser.getField(csvf)) {
                _col = csvf.field;
                if (columns != null) {
                    String _c = _col;
                    _c = _c.replace("\r", "");
                    _c = _c.replace("\n", "");
                    columns.add(_c);
                }
                if (_pos-- <= 0) break;
            }
            line = _col;
            line = line.replace("\r", "");
            line = line.replace("\n", "");
            if (((_valparttype & AppValue.C_NOCOUNT) == AppValue.C_NOCOUNT)
                    || (_parent != null && _parent.ispaused())) {
                // debuglog(1, " no seek forward:" + Long.toString(seekp));
            } else {
                ParmVars.plog.debuglog(1, " seek forward:" + Long.toString(seekp));
                // 現在のseek値取得
                seekp = raf.getFilePointer();
                current_line++;
                // seekfileにseek値保存
                if (saveseekp) {
                    try {
                        FileWriter filewriter = new FileWriter(seekfile, false);
                        String s1 = String.valueOf(seekp) + ":" + String.valueOf(current_line);
                        filewriter.write(s1);
                        filewriter.close();
                    } catch (Exception e) {
                        //
                        ParmVars.plog.printlog(
                                "FileReadLine::readLine failed  ERR:" + e.toString(), true);
                    }
                }
            }
        } catch (IOException e) {
            ParmVars.plog.printlog(
                    "FileReadLine::readLine failed csvfile:" + csvfile + " ERR:" + e.toString(),
                    true);
        } finally {
            if (raf != null) {
                try {
                    raf.close();
                } catch (Exception e) {
                    //
                }
                raf = null;
            }
        }
        return line;
    }
}
