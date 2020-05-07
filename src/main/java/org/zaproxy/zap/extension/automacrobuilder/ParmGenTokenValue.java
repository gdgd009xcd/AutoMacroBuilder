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

import java.util.Objects;

/** @author daike */
public class ParmGenTokenValue {
    private String url; // url is LOCATION header's URL "VALUE". It is Not a key value.  LOCATION:
    // http://brah.com/xxx...
    private String value;
    private Boolean b;

    public ParmGenTokenValue(String _url, String _value, Boolean _b) {
        url = _url;
        value = _value;
        b = _b;
    }

    ParmGenTokenValue(ParmGenTokenValue tv) {
        url = new String(tv.url);
        value = new String(tv.value);
        b = tv.b;
    }

    public String getValue() {
        return value;
    }

    public String getURL() {
        return url;
    }

    public Boolean getBoolean() {
        return b;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ParmGenTokenValue) {
            ParmGenTokenValue v = (ParmGenTokenValue) obj;
            // name is case-sensitive.
            return this.url.equals(v.url)
                    && this.value.equals(v.value)
                    && Objects.equals(this.b, v.b);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {

        int hash = Objects.hash(this.url, this.value, this.b);
        return hash;
    }
}
