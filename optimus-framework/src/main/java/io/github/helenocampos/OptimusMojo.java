package io.github.helenocampos;

/*
 * Copyright 2001-2005 The Apache Software Foundation.
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
import java.io.File;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;

import org.apache.maven.plugins.annotations.Parameter;

import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Plugin;
import org.apache.maven.plugin.BuildPluginManager;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.twdata.maven.mojoexecutor.MojoExecutor;

import static org.twdata.maven.mojoexecutor.MojoExecutor.*;

public abstract class OptimusMojo
        extends AbstractMojo
{

    /**
     * The project currently being build.
     *
     * @parameter expression="${project}"
     * @required
     * @readonly
     */
    @Parameter(defaultValue = "${project}", readonly = true)
    private MavenProject mavenProject;

    /**
     * The current Maven session.
     *
     * @parameter expression="${session}"
     * @required
     * @readonly
     */
    @Parameter(defaultValue = "${session}", readonly = true)
    private MavenSession mavenSession;

    /**
     * The Maven BuildPluginManager component.
     *
     * @component
     * @required
     */
    @Component
    private BuildPluginManager pluginManager;
    
    @Parameter(defaultValue = "", readonly = true)
    private String prioritization;
    
    @Parameter(defaultValue = "", readonly = true)
    private List<String> prioritizationTechniques;
    
    @Parameter(defaultValue = "", readonly = true)
    private List<String> reports;
    
    @Parameter(defaultValue = "", readonly = true)
    private String granularity;
    
    @Parameter(defaultValue = "", readonly = true)
    private String dbPath = "";
    
    @Parameter(property = "experimentOutputDirectory", defaultValue = "")
    private String experimentOutputDirectory = "";
    
    @Parameter(property = "mutation", defaultValue = "")
    private String experimentType = "";
    
    @Parameter(property = "", defaultValue = "")
    private String versionsFolder = "";
    
    private final String jacocoVersion = "0.7.9";
    
    protected void addJacocoPlugin() throws MojoExecutionException
    {
        if (!hasJacocoAgentSet())
        {
            executeMojo(
                    plugin(
                            groupId("org.jacoco"),
                            artifactId("jacoco-maven-plugin"),
                            version(jacocoVersion)
                    ),
                    goal("prepare-agent"),
                    new Xpp3Dom(""),
                    executionEnvironment(
                            mavenProject,
                            mavenSession,
                            pluginManager
                    )
            );            
        }
    }
    
    private boolean hasJacocoAgentSet()
    {
        //only returns true if has jacoco set and it is allowed version
        final Properties projectProperties = mavenProject.getProperties();
        String argLine = projectProperties.getProperty("argLine");
        if(argLine!=null && argLine.contains("jacoco")){
            if(!argLine.contains(jacocoVersion)){
                projectProperties.setProperty("argLine", "");
                return false;
            }else{
                return true;
            }
        }
        return false;
    }
    
    protected void runPitestPlugin()
    {
        Plugin assembly = MojoExecutor.plugin(
                "org.pitest",
                "pitest-maven",
                "1.3.2");
        try
        {
            MojoExecutor.executeMojo(assembly,
                    MojoExecutor.goal("mutationCoverage"),
                    configuration(
                            element(name("outputFormats"), "XML"),
                            element(name("features"), element(name("value"), "+EXPORT"))
                    ),
                    executionEnvironment(
                            mavenProject,
                            mavenSession,
                            pluginManager
                    )
            );
        } catch (MojoExecutionException ex)
        {
            Logger.getLogger(OptimusMojo.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    protected void runFaultInjectionPlugin(String experimentFolder, String injectionId) throws MojoExecutionException
    {
        
        File outputExperimentFolder = new File(new File(getExperimentOutputDirectory(), experimentFolder).getAbsolutePath(), injectionId);
        executeMojo(
                plugin(
                        groupId("io.github.helenocampos"),
                        artifactId("fault-injection-plugin"),
                        version("1.0.0")
                ),
                goal("touch"),
                configuration(
                        element(name("outputDir"), outputExperimentFolder.getAbsolutePath())
                ),
                executionEnvironment(
                        mavenProject,
                        mavenSession,
                        pluginManager
                )
        );
    }
    
    protected void runPrioritizationPlugin(boolean generateReport)
    {
        Dependency dep = new Dependency();
        dep.setGroupId("io.github.helenocampos.surefire");
        dep.setArtifactId("optimus-test");
        dep.setVersion("2.20.1");
        
        Plugin assembly = MojoExecutor.plugin(
                "org.apache.maven.plugins",
                "maven-surefire-plugin",
                "2.20.1");
        
        List<Dependency> dependencies = new LinkedList<Dependency>();
        dependencies.add(dep);
        assembly.setDependencies(dependencies);
        boolean exit = false;
        try
        {
            
            MojoExecutor.executeMojo(assembly, goal("test"),
                    getPrioritizationProperties(generateReport),
                    executionEnvironment(
                            mavenProject,
                            mavenSession,
                            pluginManager
                    )
            );
            
        } catch (MojoExecutionException ex)
        {
            exit = true;
        }finally{
            if(generateReport){
                PrioritizationReport report = new PrioritizationReport(this.getMavenProject().getName(), this.getMavenProject().getBasedir());
            }
            if(exit){
                System.exit(1);
            }
        }
    }
    
    private Xpp3Dom getPrioritizationProperties(boolean generateReport)
    {
        Xpp3Dom configuration = new Xpp3Dom("configuration");
        Xpp3Dom properties = new Xpp3Dom("properties");
        configuration.addChild(properties);
        properties.addChild(createPropertyNode("granularity", this.granularity));
        properties.addChild(createPropertyNode("prioritization", this.prioritization));
        if(generateReport){
            properties.addChild(createPropertyNode("apfd", "true"));
            properties.addChild(createPropertyNode("faultsFile", "true"));
        }
        
        if(!dbPath.equals("")){
            properties.addChild(createPropertyNode("dbPath", this.dbPath));
            properties.addChild(createPropertyNode("projectName", this.mavenProject.getName()));
        }
        
        return configuration;
    }
    
    private Xpp3Dom createPropertyNode(String propertyName, String propertyValue)
    {
        Xpp3Dom property = new Xpp3Dom("property");
        Xpp3Dom name = new Xpp3Dom("name");
        name.setValue(propertyName);
        Xpp3Dom value = new Xpp3Dom("value");
        value.setValue(propertyValue);
        property.addChild(name);
        property.addChild(value);
        return property;
    }
    
    public String getPrioritization()
    {
        return prioritization;
    }
    
    public String getGranularity()
    {
        return granularity;
    }
    
    public String getExperimentOutputDirectory()
    {
        return experimentOutputDirectory;
    }
    
    public MavenProject getMavenProject()
    {
        return this.mavenProject;
    }
    
    public List<String> getPrioritizationTechniques()
    {
        return prioritizationTechniques;
    }
    
    public void setPrioritizationTechniques(List<String> prioritizationTechniques)
    {
        this.prioritizationTechniques = prioritizationTechniques;
    }
    
    public List<String> getReports()
    {
        return reports;
    }
    
    public void setReports(List<String> reports)
    {
        this.reports = reports;
    }

    public String getDbPath()
    {
        return dbPath;
    }

    public void setDbPath(String dbPath)
    {
        this.dbPath = dbPath;
    }

    public String getExperimentType()
    {
        return experimentType;
    }

    public void setExperimentType(String experimentType)
    {
        this.experimentType = experimentType;
    }

    public String getVersionsFolder()
    {
        return versionsFolder;
    }

    public void setVersionsFolder(String versionsFolder)
    {
        this.versionsFolder = versionsFolder;
    }
}
