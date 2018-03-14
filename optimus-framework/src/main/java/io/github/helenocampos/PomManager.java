/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.github.helenocampos;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import org.apache.maven.model.Build;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.apache.maven.model.Plugin;
import org.apache.maven.model.PluginExecution;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.model.io.xpp3.MavenXpp3Writer;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.twdata.maven.mojoexecutor.MojoExecutor;

/**
 *
 * @author helenocampos
 */
public class PomManager
{

    public static void setupPrioritizationPlugin(String granularity, String technique, String projectFolder, String dbPath, String projectName)
    {
        MavenXpp3Reader reader = new MavenXpp3Reader();
        File newProjectDir = new File(projectFolder);
        if (newProjectDir.exists())
        {
            File pom = new File(newProjectDir, "pom.xml");
            if (pom.exists())
            {
                try
                {
                    Model model = reader.read(new FileReader(pom));
                    Build build = model.getBuild();
                    addJacocoPlugin(build.getPlugins());
                    addPrioritizationPlugin(build.getPlugins(), granularity, technique, dbPath, projectName);
                    MavenXpp3Writer writer = new MavenXpp3Writer();
                    OutputStream output = new FileOutputStream(pom);
                    writer.write(output, model);
                } catch (FileNotFoundException ex)
                {
//                    Logger.getLogger(MyMojo.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IOException ex)
                {
//                    Logger.getLogger(MyMojo.class.getName()).log(Level.SEVERE, null, ex);
                } catch (XmlPullParserException ex)
                {
//                    Logger.getLogger(MyMojo.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }

    //a tests run to gather coverage data only
    public static void setupFirstRun(String projectFolder)
    {
        MavenXpp3Reader reader = new MavenXpp3Reader();
        File newProjectDir = new File(projectFolder);
        if (newProjectDir.exists())
        {
            File pom = new File(newProjectDir, "pom.xml");
            if (pom.exists())
            {
                try
                {
                    Model model = reader.read(new FileReader(pom));
                    Build build = model.getBuild();
                    addJacocoPlugin(build.getPlugins());
                    MavenXpp3Writer writer = new MavenXpp3Writer();
                    OutputStream output = new FileOutputStream(pom);
                    writer.write(output, model);
                } catch (FileNotFoundException ex)
                {
//                    Logger.getLogger(MyMojo.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IOException ex)
                {
//                    Logger.getLogger(MyMojo.class.getName()).log(Level.SEVERE, null, ex);
                } catch (XmlPullParserException ex)
                {
//                    Logger.getLogger(MyMojo.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }

    public static void removeFramework(String projectFolder)
    {
        MavenXpp3Reader reader = new MavenXpp3Reader();
        File newProjectDir = new File(projectFolder);
        if (newProjectDir.exists())
        {
            File pom = new File(newProjectDir, "pom.xml");
            if (pom.exists())
            {
                try
                {
                    Model model = reader.read(new FileReader(pom));
                    Build build = model.getBuild();
                    build.setPlugins(removePlugin("optimus-framework", build.getPlugins()));
                    build.setPlugins(removePlugin("jacoco-maven-plugin", build.getPlugins()));
                    build.setPlugins(removePlugin("maven-surefire-plugin", build.getPlugins()));
                    MavenXpp3Writer writer = new MavenXpp3Writer();
                    OutputStream output = new FileOutputStream(pom);
                    writer.write(output, model);
                } catch (FileNotFoundException ex)
                {
//                    Logger.getLogger(MyMojo.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IOException ex)
                {
//                    Logger.getLogger(MyMojo.class.getName()).log(Level.SEVERE, null, ex);
                } catch (XmlPullParserException ex)
                {
//                    Logger.getLogger(MyMojo.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }

    }

    private static List<Plugin> removePlugin(String artifactId, List<Plugin> plugins)
    {
        Iterator<Plugin> pluginsIterator = plugins.iterator();
        while (pluginsIterator.hasNext())
        {
            Plugin plugin = pluginsIterator.next();
            if (plugin.getArtifactId().equals(artifactId))
            {
                pluginsIterator.remove();
            }
        }
        return plugins;
    }

    private static void addPrioritizationPlugin(List<Plugin> plugins, String granularity, String technique, String dbPath, String projectName)
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

        assembly.setConfiguration(getPrioritizationProperties(granularity, technique, dbPath, projectName));
        plugins.add(assembly);

    }

    private static Xpp3Dom getPrioritizationProperties(String granularity, String prioritization, String dbPath, String projectName)
    {
        Xpp3Dom configuration = new Xpp3Dom("configuration");
        Xpp3Dom argLine = new Xpp3Dom("argLine");
        argLine.setValue("@{argLine}");
        configuration.addChild(argLine);
        Xpp3Dom properties = new Xpp3Dom("properties");
        configuration.addChild(properties);
        properties.addChild(createPropertyNode("granularity", granularity));
        properties.addChild(createPropertyNode("prioritization", prioritization));
        properties.addChild(createPropertyNode("apfd", "true"));

        if (!dbPath.equals(""))
        {
            properties.addChild(createPropertyNode("dbPath", dbPath));
            properties.addChild(createPropertyNode("projectName", projectName));
        }

        return configuration;
    }

    private static Xpp3Dom createPropertyNode(String propertyName, String propertyValue)
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

    private static void addJacocoPlugin(List<Plugin> plugins)
    {
        Plugin plugin = MojoExecutor.plugin(
                "org.jacoco",
                "jacoco-maven-plugin",
                "0.7.9");

        PluginExecution execution = new PluginExecution();
        execution.addGoal("prepare-agent");
        plugin.addExecution(execution);
        plugins.add(plugin);
    }
}
