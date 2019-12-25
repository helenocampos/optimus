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

import info.heleno.extractor.model.CoverageGranularity;

/**
 *
 * @author helenocampos
 */
public class MaxMaxMethodART extends ART
{
    @Override
    public CoverageGranularity getCoverageGranularity()
    {
        return CoverageGranularity.METHOD;
    }

    @Override
    public SelectionFunction getSelectionFunction()
    {
        return SelectionFunction.MAXMAX;
    }
}