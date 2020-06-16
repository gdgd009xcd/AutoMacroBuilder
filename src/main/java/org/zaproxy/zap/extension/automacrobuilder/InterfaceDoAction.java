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

import java.util.List;

/**
 * InterfaceDoAction
 *
 * @author daike
 */
public interface InterfaceDoAction {
    //
    // start action (synchronized)
    // This function is called by where:
    //             synchronized OneThreadProcessor getProcess(InterfaceDoActionProvider provider)
    // new Instance:
    //        public OneThreadProcessor(ThreadManager tm, Thread th, InterfaceDoAction doaction)
    // RECYCLED    :
    //        public void setNewThread(Thread th)
    //
    // before action: do  initiatize or copy fields... and return acttion list.
    // action list:  actions can be performed by specifying the list number
    // at the appropriate entry point.
    List<InterfaceAction> startAction(ThreadManager tm, OneThreadProcessor otp);
    //
    //
    // {@code List<InterfaceAction>} getActionList();
    //
    // end action (synchronized)
    // This function is called by where:
    //     synchronized void endProcess(OneThreadProcessor p, InterfaceDoAction action)
    //
    // after action: do some result save/update etc...
    //
    InterfaceEndAction endAction(ThreadManager tm, OneThreadProcessor otp);
}
