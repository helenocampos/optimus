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
package info.heleno.surefire.ordering.techniques;

import info.heleno.surefire.api.DefaultOrderer;
import info.heleno.surefire.ordering.Strategy;
import info.heleno.testing.AbstractTest;


/**
 *
 * @author helenocampos
 */
public class AlphabeticalOrder extends DefaultOrderer<AbstractTest>
{

    public int compare(AbstractTest o1, AbstractTest o2)
    {
        String thiz = o1.getTestName();
        String that = o2.getTestName();
        return that.compareToIgnoreCase(thiz);
    }
    @Override
    public String getStrategy()
    {
        return Strategy.DEFAULT.getName();
    }
    
}
