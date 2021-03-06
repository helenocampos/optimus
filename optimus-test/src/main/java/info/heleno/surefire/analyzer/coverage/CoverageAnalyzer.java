/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package info.heleno.surefire.analyzer.coverage;

import info.heleno.extractor.AttributesExtractor;
import info.heleno.extractor.model.TestMethod;
import info.heleno.extractor.model.Coverage;
import info.heleno.extractor.model.CoverageGranularity;
import info.heleno.extractor.model.JavaSourceCodeClass;
import info.heleno.extractor.model.JavaTestClass;
import info.heleno.extractor.model.ModificationsGranularity;
import info.heleno.extractor.model.ProjectData;
import info.heleno.testing.AbstractTest;
import info.heleno.testing.TestGranularity;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 *
 * @author helenocampos
 */
public class CoverageAnalyzer {

    private ProjectData projectData;

    public CoverageAnalyzer() {
        this.projectData = ProjectData.getProjectDataFromFile();
    }

    public float getTotalTestCoverage(AbstractTest test, CoverageGranularity granularity) {
        float score = 0;

        if (this.getProjectData() != null) {
            JavaTestClass testClass = this.getProjectData().getTestClassByName(test.getTestClass().getName());
            if (testClass != null) {
                if (test.getTestGranularity().equals(TestGranularity.METHOD)) {
                    TestMethod method = testClass.getMethodByName(test.getTestName());
                    score = getTestMethodTotalCoverage(method, granularity);
                } else if (test.getTestGranularity().equals(TestGranularity.CLASS)) {
                    for (TestMethod method : testClass.getMethods().values()) {
                        score += getTestMethodTotalCoverage(method, granularity);
                    }
                }
            }
        }

        return score;
    }

    public float getTotalDiffTestCoverage(AbstractTest test, ModificationsGranularity modificationsGranularity, Set<String> modifiedElements) {
        float score = 0;

        if (this.getProjectData() != null) {
            JavaTestClass testClass = this.getProjectData().getTestClassByName(test.getTestClass().getName());
            if (testClass != null) {
                if (test.getTestGranularity().equals(TestGranularity.METHOD)) {
                    TestMethod method = testClass.getMethodByName(test.getTestName());
                    score = getTestMethodTotalDiffCoverage(method, modificationsGranularity, modifiedElements);
                } else if (test.getTestGranularity().equals(TestGranularity.CLASS)) {
                    for (TestMethod method : testClass.getMethods().values()) {
                        score += getTestMethodTotalDiffCoverage(method, modificationsGranularity, modifiedElements);
                    }
                }
            }
        }

        return score;
    }

    private float getTestMethodTotalDiffCoverage(TestMethod method, ModificationsGranularity modificationsGranularity, Set<String> modifiedElements) {
        float score = 0;
        if (method != null) {
            if (method.getCoverage() != null) {
                modifiedElements = normalizeCoverageStrings(modifiedElements);
                Set<String> elementsCovered = normalizeCoverageStrings(getElementsCovered(method.getCoverage().getCoverage(CoverageGranularity.METHOD, false), modificationsGranularity));
                modifiedElements.retainAll(elementsCovered);
                score = modifiedElements.size();
            }

        }
        return score;
    }

    private Set<String> getElementsCovered(LinkedHashMap<String, String> coverageData, ModificationsGranularity modificationsGranularity) {
        Set<String> coveredElements = new HashSet<>();
        if (modificationsGranularity.equals(ModificationsGranularity.CLASS)) {
            coveredElements = getCoveredClassesNames(coverageData);
        } else if (modificationsGranularity.equals(ModificationsGranularity.METHOD)) {
            coveredElements = getCoveredMethodsNames(coverageData);
        }
        return coveredElements;
    }

    private Set<String> getAdditionalElementsCovered(LinkedHashMap<String, String> coverageData, ModificationsGranularity modificationsGranularity, Set<String> elementsAlreadyCovered) {
        Set<String> coveredElements = new HashSet<>();
        if (modificationsGranularity.equals(ModificationsGranularity.CLASS)) {
            coveredElements = normalizeCoverageStrings(getCoveredClassesNames(coverageData));
        } else if (modificationsGranularity.equals(ModificationsGranularity.METHOD)) {
            coveredElements = normalizeCoverageStrings(getCoveredMethodsNames(coverageData));
        }
        coveredElements.removeAll(elementsAlreadyCovered);
        return coveredElements;
    }

    private Set<String> getCoveredMethodsNames(LinkedHashMap<String, String> coverageData) {
        Set<String> coveredMethods = new HashSet<>();
        for (String key : coverageData.keySet()) {
            String coverageString = coverageData.get(key);
            if (coverageString != null && !coverageString.equals("")) {
                JavaSourceCodeClass clazz = getProjectData().getClassByCoverageName(key);
                if (clazz != null) {
                    List<String> clazzMethods = AttributesExtractor.parseClass(clazz.getPath()).getMethodsNames();
                    if (clazzMethods.size() == coverageString.length()) {
                        int i = 0;
                        for (String method : clazzMethods) {
                            if (coverageString.charAt(i++) == '1') {
                                coveredMethods.add(clazz.getQualifiedName() + "." + method);
                            }
                        }
                    }
                }

            }
        }
        return coveredMethods;
    }

    private Set<String> getCoveredClassesNames(LinkedHashMap<String, String> coverageData) {
        Set<String> coveredClasses = new HashSet<>();
        for (String key : coverageData.keySet()) {
            String coverageString = coverageData.get(key);
            if (coverageString.contains("1")) {
                coveredClasses.add(key);
            }
        }

        return coveredClasses;
    }

    public float getAdditionalTestCoverage(AbstractTest test, CoverageGranularity granularity, Coverage coveredCode) {
        float score = 0;

        if (this.getProjectData() != null) {
            JavaTestClass testClass = this.getProjectData().getTestClassByName(test.getTestClass().getName());
            if (testClass != null) {
                if (test.getTestGranularity().equals(TestGranularity.METHOD)) {
                    TestMethod method = testClass.getMethodByName(test.getTestName());
                    score = getTestMethodAdditionalCoverage(method, granularity, coveredCode, false);
                } else if (test.getTestGranularity().equals(TestGranularity.CLASS)) {
                    for (TestMethod method : testClass.getMethods().values()) {
                        score += getTestMethodAdditionalCoverage(method, granularity, coveredCode, true);
                    }
                }
            }
        }

        return score;
    }

    public float getAdditionalDiffTestCoverage(AbstractTest test, ModificationsGranularity modificationsGranularity, Coverage alreadyCoveredCode, Set<String> modifiedElements) {
        float score = 0;

        if (this.getProjectData() != null) {
            JavaTestClass testClass = this.getProjectData().getTestClassByName(test.getTestClass().getName());
            if (testClass != null) {
                Set<String> alreadyCoveredElements = normalizeCoverageStrings(getElementsCovered(alreadyCoveredCode.getCoverage(CoverageGranularity.METHOD, false), modificationsGranularity));
                if (test.getTestGranularity().equals(TestGranularity.METHOD)) {
                    TestMethod method = testClass.getMethodByName(test.getTestName());
                    score = getTestMethodAdditionalDiffCoverage(method, modificationsGranularity, alreadyCoveredElements, modifiedElements);
                } else if (test.getTestGranularity().equals(TestGranularity.CLASS)) {
                    for (TestMethod method : testClass.getMethods().values()) {
                        score += getTestMethodAdditionalDiffCoverage(method, modificationsGranularity, alreadyCoveredElements, modifiedElements);
                    }
                }
            }
        }

        return score;
    }

    private float getTestMethodAdditionalDiffCoverage(TestMethod method, ModificationsGranularity diffGranularity, Set<String> alreadyCoveredElements, Set<String> modifiedElements) {
        float additionalScore = 0;
        if (method != null) {
            if (method.getCoverage() != null) {
                modifiedElements = normalizeCoverageStrings(modifiedElements);
                Set<String> elementsCovered = normalizeCoverageStrings(getAdditionalElementsCovered(method.getCoverage().getCoverage(CoverageGranularity.METHOD, false), diffGranularity, alreadyCoveredElements));
                alreadyCoveredElements.addAll(elementsCovered);
                modifiedElements.retainAll(elementsCovered);
                additionalScore = modifiedElements.size();
            }
        }
        return additionalScore;
    }

    private float getTestMethodTotalCoverage(TestMethod method, CoverageGranularity granularity) {
        float score = 0;
        if (method != null) {
            if (method.getCoverage() != null) {
                for (String data : method.getCoverage().getCoverage(granularity, false).values()) {
                    for (int x = 0; x < data.length(); x++) {
                        if (data.charAt(x) == '1') {
                            score++;
                        }
                    }
                }
            }

        }
        return score;
    }

    private float getTestMethodAdditionalCoverage(TestMethod method, CoverageGranularity granularity, Coverage candidatesCoveredCode, boolean updateCoverage) {
        float additionalScore = 0;
        if (method != null) {
            if (method.getCoverage() != null) {
                Coverage coverage = method.getCoverage();
                for (String className : coverage.getCoverage(granularity, false).keySet()) {
                    String coveredElements = coverage.getCoverage(granularity, false).get(className); //elements covered by the test contained in className
                    String candidatesCoveredElements = candidatesCoveredCode.getCoverage(granularity, false).get(className); //elements already covered

                    if (candidatesCoveredElements == null) {
                        candidatesCoveredElements = getClearedCoverage(coveredElements.length());
                    }
                    StringBuilder alreadyCoveredElements = new StringBuilder(candidatesCoveredElements);
                    if (coveredElements.length() == candidatesCoveredElements.length()) {
                        for (int x = 0; x < coveredElements.length(); x++) {
                            if (coveredElements.charAt(x) == '1') {
                                if (alreadyCoveredElements.charAt(x) == '0') {
                                    //element not yet covered by candidates tests, compute one Score
                                    alreadyCoveredElements.setCharAt(x, '1');
                                    additionalScore++;

                                }
                            }
                        }
                        if (updateCoverage) {
                            candidatesCoveredCode.getCoverage(granularity, false).put(className, alreadyCoveredElements.toString());
                        }
                    }

                }
            }
        }
        return additionalScore;
    }

    private String getClearedCoverage(int size) {
        StringBuilder clearedCoverage = new StringBuilder();

        for (int i = 0; i < size; i++) {
            clearedCoverage.append("0");
        }
        return clearedCoverage.toString();
    }

    private String replaceCoverage(int index, String coverageString, char newValue) {
        char[] stringChars = coverageString.toCharArray();
        stringChars[index] = newValue;
        return String.valueOf(stringChars);
    }

    public Coverage getCoverageFromTests(List<AbstractTest> tests) {
        Coverage testsCoverage = new Coverage();
        for (AbstractTest test : tests) {
            Coverage testCoverage = new Coverage();
            testCoverage.setStatements(getTestCoverage(test, CoverageGranularity.STATEMENT));
            testCoverage.setMethods(getTestCoverage(test, CoverageGranularity.METHOD));
            testCoverage.setBranches(getTestCoverage(test, CoverageGranularity.BRANCH));
            testsCoverage = Coverage.merge(testsCoverage, testCoverage);
        }
        return testsCoverage;
    }

    private LinkedHashMap<String, String> getTestCoverage(AbstractTest test, CoverageGranularity granularity) {
        JavaTestClass testClass = this.getProjectData().getTestClassByName(test.getTestClass().getName());
        LinkedHashMap<String, String> testCoverage = new LinkedHashMap<>();
        if (testClass != null) {
            if (test.getTestGranularity().equals(TestGranularity.METHOD)) {
                TestMethod method = testClass.getMethodByName(test.getTestName());
                if (method != null) {
                    testCoverage.putAll(method.getCoverage().getCoverage(granularity, false));
                }
            } else if (test.getTestGranularity().equals(TestGranularity.CLASS)) {
                for (TestMethod method : testClass.getMethods().values()) {
                    LinkedHashMap<String, String> methodCoverage = method.getCoverage().getCoverage(granularity, false);
                    testCoverage = Coverage.merge(methodCoverage, testCoverage);
                }
            }
        }
        return testCoverage;
    }

    public String getTestCoverageString(AbstractTest test, CoverageGranularity coverageGranularity) {
        String coverageString = "";
        JavaTestClass testClass = getProjectData().getTestClassByName(test.getTestClass().getCanonicalName());
        if (testClass != null) {
            if (test.getTestGranularity().equals(TestGranularity.CLASS)) {
                LinkedHashMap<String, String> testClassCoverage = new LinkedHashMap<>();
                for (TestMethod method : testClass.getMethods().values()) {
                    LinkedHashMap<String, String> testMethodCoverage = method.getCoverage().getCoverage(coverageGranularity, true);
                    testClassCoverage = Coverage.merge(testMethodCoverage, testClassCoverage);
                }
                coverageString = Coverage.getCoverageString(testClassCoverage);
            } else {
                TestMethod method = testClass.getMethodByName(test.getTestName());
                if (method != null && method.getCoverage() != null) {
                    coverageString = method.getCoverage().getCoverageString(coverageGranularity);
                }
            }
        }

        return coverageString;
    }

    private Set<String> normalizeCoverageStrings(Set<String> elements) {
        Set<String> normalizedStrings = new HashSet<>();
        for (String element : elements) {
            normalizedStrings.add(element.replaceAll("/", "\\."));
        }
        return normalizedStrings;
    }

    public ProjectData getProjectData() {
        return projectData;
    }
}
