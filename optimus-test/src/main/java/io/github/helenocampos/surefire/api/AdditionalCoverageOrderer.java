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

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author helenocampos
 */
public abstract class AdditionalCoverageOrderer<T> extends AdditionalOrderer<T>
{

    private HashMap<String, boolean[]> coveredCode;
    private List<T> currentCoverageSet = new LinkedList<>();

    @Override
    public T getNextTest(List<T> tests, List<T> alreadyOrderedTests)
    {
        T nextTest = getNextTest(tests);
        this.currentCoverageSet.add(nextTest);
        return nextTest;
    }

    public abstract T getNextTest(List<T> tests);

    public HashMap<String, boolean[]> getCoveredCode()
    {
        return this.coveredCode;
    }

    public void setCoveredCode(HashMap<String, boolean[]> coveredCode)
    {
        this.coveredCode = coveredCode;
    }

    public void updateCoveredCode(HashMap<String, boolean[]> newCoveredCode)
    {
        this.coveredCode.putAll(newCoveredCode);
    }

    public void addTestToCurrentCoverage(T test)
    {
        this.currentCoverageSet.add(test);
    }

    public void resetCurrentCoverage()
    {
        this.currentCoverageSet = new LinkedList<>();
    }

    public List<T> getCurrentCoverageSet()
    {
        return this.currentCoverageSet;
    }
}
