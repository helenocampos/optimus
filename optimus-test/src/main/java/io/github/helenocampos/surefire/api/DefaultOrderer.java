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
package io.github.helenocampos.surefire.api;

import java.util.Comparator;
import java.util.Random;

/**
 *
 * @author helenocampos
 */
public abstract class DefaultOrderer<AbstractTest> implements Comparator<AbstractTest>, Orderer {
    public abstract int compare(AbstractTest o1, AbstractTest o2);
    
    protected int compare(float test1, float test2){
        if(test1==test2){
            Random randomizer = new Random();
            int random = randomizer.nextInt(2);
            if(random==1){
                return 1;
            }else{
                return -1;
            }
        }else{
            return Float.compare(test1, test2);
        }
    }
}
