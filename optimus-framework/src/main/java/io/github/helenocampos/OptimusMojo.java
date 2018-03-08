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
    
    @Parameter(property = "experimentOutputDirectory", defaultValue = "")
    private String experimentOutputDirectory = "";
    
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
        final Properties projectProperties = mavenProject.getProperties();
        String argLine = projectProperties.getProperty("argLine");
        return argLine.contains("jacoco");
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
    
    protected void runPrioritizationPlugin()
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
        
        try
        {
            
            MojoExecutor.executeMojo(assembly, goal("test"),
                    getPrioritizationProperties(),
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
    
    private Xpp3Dom getPrioritizationProperties()
    {
        Xpp3Dom configuration = new Xpp3Dom("configuration");
        Xpp3Dom properties = new Xpp3Dom("properties");
        configuration.addChild(properties);
        properties.addChild(createPropertyNode("granularity", this.granularity));
        properties.addChild(createPropertyNode("prioritization", this.prioritization));
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
}
