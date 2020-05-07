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

/** @author daike */
public interface DeepClone extends Cloneable {
    //
    //
    // Correct example:
    //
    //    class crazyobject implements DeepClone{
    //
    //    ...
    //        @Override
    //        public crazyobject clone() {// return this Type object which is not java.lang.Object
    // Type.
    //             try {
    //               crazyobject nobj =  (crazyobject)super.clone();//  new clone object is created
    // and  primitive or final member object of this class is also copied
    //               nobj.optlist = ListDeepCopy.listDeepCopy(this.optlist);// member of this class
    // that require deep copy must be explicitly copied.
    //               return nobj;
    //             } catch (CloneNotSupportedException e) {
    //               throw new AssertionError();
    //             }
    //        }
    //
    public Object clone();
}
