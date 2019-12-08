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
package info.heleno.surefire.api;

import info.heleno.testing.AbstractTest;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

/**
 *
 * @author helenocampos
 */
public abstract class AdditionalCoverageOrderer<T> extends AdditionalOrderer<T>
{

    private List<T> currentCoverageSet = new LinkedList<>();
    private boolean recursiveLocked = false;

    @Override
    public T getNextTest(List<T> tests, List<T> alreadyOrderedTests)
    {
        setRecursiveLocked(false);
        T nextTest = getNextTest(tests);
        this.currentCoverageSet.add(nextTest);
        return nextTest;
    }

    public abstract T getNextTest(List<T> tests);

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

    protected AbstractTest resolveTies(List<AbstractTest> tiedTests)
    {
        Random randomizer = new Random();
        int random = randomizer.nextInt(tiedTests.size());
        return tiedTests.get(random);
    }

    public boolean isRecursiveLocked()
    {
        return recursiveLocked;
    }

    public void setRecursiveLocked(boolean recursiveLock)
    {
        this.recursiveLocked = recursiveLock;
    }
}
