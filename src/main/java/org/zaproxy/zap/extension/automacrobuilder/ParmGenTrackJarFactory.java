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

import java.util.HashMap;

/** @author daike */
public class ParmGenTrackJarFactory {

    static HashMap<Integer, ParmGenTrackingParam> trackjar =
            null; // Integer: unique key(ascend order.) ParmGenTrackingParam: tracking value
    static Integer keymax = -1;

    static void clear() {
        keymax = 0;
        trackjar = new HashMap<Integer, ParmGenTrackingParam>();
    }

    // create new unique key. tracking value is null.
    static int create() {
        ParmGenTrackingParam tkparam = new ParmGenTrackingParam();
        trackjar.put(keymax, tkparam);
        return keymax++;
    }

    // save tracking value with unique key.
    static void put(Integer key, ParmGenTrackingParam tkparam) {
        trackjar.put(key, tkparam);
        // ParmVars.plog.debuglog(0, "TrackJar put key:" + key);
    }

    // get tracking value with unique key.
    static ParmGenTrackingParam get(Integer key) {
        // ParmVars.plog.debuglog(0, "TrackJar get key:" + key);
        return trackjar.get(key);
    }

    static void remove(Integer key) {
        trackjar.remove(key);
    }

    static {
        clear();
    }
}
