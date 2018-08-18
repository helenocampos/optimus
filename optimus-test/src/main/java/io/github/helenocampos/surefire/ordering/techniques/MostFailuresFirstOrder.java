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
package io.github.helenocampos.surefire.ordering.techniques;

import io.github.helenocampos.optimushistoricalanalyzer.HistoricalAnalyzer;
import io.github.helenocampos.testing.AbstractTest;
import io.github.helenocampos.surefire.ordering.Strategy;
import io.github.helenocampos.surefire.api.DefaultOrderer;
import java.util.Properties;

/**
 *
 * @author helenocampos
 */
public class MostFailuresFirstOrder extends DefaultOrderer<AbstractTest>
{

    private String dbPath;
    private String projectName;
    private HistoricalAnalyzer analyzer;
    private boolean firstVersionExecution = false;

    public MostFailuresFirstOrder()
    {
        Properties properties = System.getProperties();
        this.dbPath = properties.getProperty("dbPath");
        this.projectName = properties.getProperty("projectName");
        this.firstVersionExecution = Boolean.valueOf(properties.getProperty("firstVersionExecution", "false"));
        if (dbPath != null && projectName != null)
        {
            analyzer = new HistoricalAnalyzer(dbPath);
        }
    }

    public int compare(AbstractTest o1, AbstractTest o2)
    {
        if (analyzer != null && !firstVersionExecution)
        {
            float thiz = analyzer.getTestFailureRate(o1.getQualifiedName(), this.projectName);
            float that = analyzer.getTestFailureRate(o2.getQualifiedName(), this.projectName);
            return Float.compare(thiz, that);
        } else
        {
            return 0;
        }

    }

    @Override
    public String getStrategy()
    {
        return Strategy.DEFAULT.getName();
    }
}
