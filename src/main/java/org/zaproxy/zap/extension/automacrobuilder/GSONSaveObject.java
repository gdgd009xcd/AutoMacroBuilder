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

import java.util.ArrayList;
import java.util.Collection;

/**
 * GSON output class for save configration to file
 *
 * @author gdgd009xcd
 */
public class GSONSaveObject {
    public String VERSION = "1.99"; // configuration file version
    public String LANG;
    public boolean ProxyInScope;
    public boolean IntruderInScope;
    public boolean RepeaterInScope;
    public boolean ScannerInScope;
    public Collection<String> ExcludeMimeTypes;
    public Collection<AppParmsIni_List> AppParmsIni_List;

    GSONSaveObject() {
        CurrentRequest = -1;
        TabIndex = -1;
        ExcludeMimeTypes = new ArrayList<>();
        AppParmsIni_List = new ArrayList<>();
        PRequestResponses = new ArrayList<>();
        PRequestResponseSequences = new ArrayList<>();
    }

    // innner static classes
    static class AppParmsIni_List {
        public String URL;
        public int len;
        public int typeval;
        public int inival;
        public int maxval;
        public String csvname;
        public boolean pause;
        public int TrackFromStep;
        public int SetToStep;
        public String relativecntfilename;
        public Collection<AppValue_List> AppValue_List;

        AppParmsIni_List() {
            AppValue_List = new ArrayList<>();
        }
    }

    static class AppValue_List {
        public String valpart;
        public boolean isEnabled;
        public boolean isNoCount;
        public int csvpos;
        public String value;
        public String resURL;
        public String resRegex;
        public String resValpart;
        public int resRegexPos;
        public String token;
        public boolean urlencode;
        public int fromStepNo;
        public int toStepNo;
        public String TokenType;
        public int condTargetNo;
        public String condRegex;
        public boolean condRegexTargetIsRequest;
        public boolean replaceZeroSize;
    }

    public int CurrentRequest;
    public Collection<GsonPRequestResponse> PRequestResponses;

    static class GsonPRequestResponse {
        public String PRequest64;
        public String PResponse64;
        public String Host;
        public int Port;
        public boolean SSL;
        public String Comments;
        public boolean Disabled;
        public boolean Error;

        GsonPRequestResponse() {
            init();
        }

        public void init() {
            PRequest64 = null;
            PResponse64 = null;
            Host = null;
            Port = 0;
            SSL = false;
            Comments = "";
            Disabled = false;
            Error = false;
        }
    }

    public int TabIndex;
    public Collection<GsonPRequestResponseSequence> PRequestResponseSequences;

    static class GsonPRequestResponseSequence {
        public int MyPageIndex; // position index of MyPage in PRequestResponse list
        public int CurrentRequest; // position index of current selected request in PRequestResponse
        // list
        public Collection<GsonPRequestResponse> PRequestResponses; // RequestResponse sequence list

        GsonPRequestResponseSequence() {
            PRequestResponses = new ArrayList<>();
            init();
        }

        public void init() {
            MyPageIndex = -1;
            CurrentRequest = -1;
            PRequestResponses.clear();
        }
    }
}
