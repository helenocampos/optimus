/*
 * Copyright 2017 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.helenocampos.surefire.ordering.methods;

import io.github.helenocampos.surefire.api.MethodsOrderer;

/**
 *
 * @author helenocampos
 */
public class MethodsAlphabeticalOrder implements MethodsOrderer<String> {

    private String extractMethodName(String fullName) {
        String[] d = fullName.split("#");
        return d[1];
    }

    private boolean isValidName(String name) {
        if (name != null) {
            String[] d = name.split("#");
            if (d.length == 2) {
                if (d[0].length() > 1 && d[1].length() > 1) {
                    return true;
                }
            }
        }
        return false;
    }

    public int compare(String o1, String o2) {
        String thiz = o1;
        String that = o2;

        if (isValidName(thiz)) {
            if (isValidName(that)) {
                return extractMethodName(that).compareToIgnoreCase(extractMethodName(thiz));
            }
            return 1;
        } else {
            if (isValidName(that)) {
                return -1;
            }
        }
        return 0;
    }
}
