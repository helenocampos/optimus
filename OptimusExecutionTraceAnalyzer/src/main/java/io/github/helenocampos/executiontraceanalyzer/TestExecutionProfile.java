/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.github.helenocampos.executiontraceanalyzer;

import io.github.helenocampos.executiontraceanalyzer.cobertura.CoberturaLine;
import io.github.helenocampos.extractor.model.ClassMethod;
import io.github.helenocampos.extractor.model.Coverage;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author helenocampos This class represents one test method execution profile
 */
public class TestExecutionProfile
{

    private HashMap<String, Integer[]> executionTraces;
    private int totalLinesExecuted;

    public TestExecutionProfile(ClassMethod testMethod, HashMap<String, List<CoberturaLine>> globalExecutionTrace)
    {
        this.executionTraces = new HashMap<>();
        this.totalLinesExecuted = 0;
        processExecutionTraces(testMethod, globalExecutionTrace);
    }

    private void processExecutionTraces(ClassMethod testMethod, HashMap<String, List<CoberturaLine>> globalExecutionTrace)
    {
        Coverage coverage = testMethod.getCoverage();
        if (coverage != null)
        {
            if (coverage.getCoveredLines() != null)
            {
                HashMap<String, String> testCoveredLines = coverage.getCoveredLines();
                for (String classCovered : testCoveredLines.keySet())
                {
                    String coveredLines = testCoveredLines.get(classCovered);
                    if (coveredLines != null)
                    {
                        String[] coveredLinesArray = coveredLines.split(",");
                        classCovered = classCovered.replace("/", ".");
                        List<CoberturaLine> executionTraceLines = globalExecutionTrace.get(classCovered);
                        if (executionTraceLines != null)
                        {
                            Integer[] executionTrace = new Integer[executionTraceLines.size()];
                            int executionTraceIndex = 0;
                            for (CoberturaLine executedLine : executionTraceLines)
                            {
                                if (Arrays.binarySearch(coveredLinesArray,executedLine.getNumber()) >= 0)
                                {
                                    executionTrace[executionTraceIndex++] = Integer.parseInt(executedLine.getHits());
                                    this.totalLinesExecuted+=Integer.parseInt(executedLine.getHits());
                                } else
                                {
                                    executionTrace[executionTraceIndex++] = 0;
                                }
                            }
                            this.executionTraces.put(classCovered, executionTrace);
                        }

                    }
                }
            }

        }
    }
    
    public HashMap<String, Integer[]> getExecutionTraces()
    {
        return executionTraces;
    }

    public int getTotalLinesExecuted()
    {
        return totalLinesExecuted;
    }
}
