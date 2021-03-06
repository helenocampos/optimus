/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package info.heleno;

import info.heleno.PrioritizationConfig;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.text.Normalizer;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import org.apache.maven.model.Build;
import org.apache.maven.model.Contributor;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Developer;
import org.apache.maven.model.Model;
import org.apache.maven.model.Plugin;
import org.apache.maven.model.PluginExecution;
import org.apache.maven.model.io.DefaultModelWriter;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.twdata.maven.mojoexecutor.MojoExecutor;

/**
 *
 * @author helenocampos
 */
public class PomManager {

    /**
     * @return the excludes
     */
    public Xpp3Dom[] getExcludes() {
        return excludes;
    }

    private Model originalPom;
    private Xpp3Dom[] excludes;
    private Xpp3Dom[] includes;

    public PomManager(String projectFolder) {
        this.originalPom = readPom(projectFolder);
        this.excludes = getSurefireExcludesConfig(originalPom);
        this.includes = getSurefireIncludesConfig(originalPom);
    }

    public static Model readPom(String projectFolder) {
        MavenXpp3Reader reader = new MavenXpp3Reader();
        File newProjectDir = new File(projectFolder);
        Model pomModel = null;
        if (newProjectDir.exists()) {
            File pom = new File(newProjectDir, "pom.xml");
            if (pom.exists()) {
                try {
                    pomModel = reader.read(new FileReader(pom));
                } catch (Exception ex) {
//                    Logger.getLogger(MyMojo.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        return pomModel;
    }

    public static String getProjectId(String projectFolder) {
        Model model = readPom(projectFolder);
        if (model != null) {
            return model.getArtifactId();
        } else {
            return "";
        }
    }

    public void writePom(Model model, String projectFolder) {
        File newProjectDir = new File(projectFolder);
        if (newProjectDir.exists()) {
            File pom = new File(newProjectDir, "pom.xml");
            if (pom.exists()) {
                try {
                    DefaultModelWriter writer = new DefaultModelWriter();
                    OutputStream output = new FileOutputStream(pom);
                    replaceSpecialCharacters(model);
                    writer.write(output, null, model);
                    output.close();
                } catch (FileNotFoundException ex) {
//                    Logger.getLogger(MyMojo.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IOException ex) {
//                    Logger.getLogger(MyMojo.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }

    private void replaceSpecialCharacters(Model model) {
        List<Contributor> contributors = model.getContributors();
        if (contributors != null) {
            for (Contributor contributor : contributors) {
                String name = contributor.getName();
                if (name != null) {
                    contributor.setName(Normalizer.normalize(name, Normalizer.Form.NFD).replaceAll("\\p{InCombiningDiacriticalMarks}", ""));
                }
            }
        }
        List<Developer> developers = model.getDevelopers();
        if (developers != null) {
            for (Developer developer : developers) {
                String name = developer.getName();
                if (name != null) {
                    developer.setName(Normalizer.normalize(name, Normalizer.Form.NFD).replaceAll("\\p{InCombiningDiacriticalMarks}", ""));
                }
            }
        }
    }

    public void setupPrioritizationPlugin(PrioritizationConfig config) {
        Model model = readPom(config.getProjectFolder());
        Build build = model.getBuild();
        if (build == null) {
            build = new Build();
            build.setPlugins(new LinkedList<Plugin>());
            model.setBuild(build);
        }
        addJacocoPlugin(build.getPlugins());
        addPrioritizationPlugin(build.getPlugins(), config);
        writePom(model, config.getProjectFolder());
    }

    private Xpp3Dom[] getSurefireExcludesConfig(Model model) {
        if (model != null) {
            Build build = model.getBuild();
            if (build != null) {
                Plugin surefire = getPluginByArtifactId(build.getPlugins(), "maven-surefire-plugin");
                if (surefire != null) {
                    Xpp3Dom configuration = (Xpp3Dom) surefire.getConfiguration();
                    if (configuration != null) {
                        return configuration.getChildren("excludes");
                    }
                }
            }
        }
        return null;
    }

    private Xpp3Dom[] getSurefireIncludesConfig(Model model) {
        if (model != null) {
            Build build = model.getBuild();
            if (build != null) {
                Plugin surefire = getPluginByArtifactId(build.getPlugins(), "maven-surefire-plugin");
                if (surefire != null) {
                    Xpp3Dom configuration = (Xpp3Dom) surefire.getConfiguration();
                    if (configuration != null) {
                        return configuration.getChildren("includes");
                    }
                }
            }
        }
        return null;
    }

    private Plugin getPluginByArtifactId(List<Plugin> plugins, String artifactId) {
        for (Plugin plugin : plugins) {
            if (plugin.getArtifactId().equals(artifactId)) {
                return plugin;
            }
        }
        return null;
    }

    public void removeFramework(String projectFolder) {
        Model model = readPom(projectFolder);
        Build build = model.getBuild();
        if (build != null) {
            build.setPlugins(removePlugin("optimus-framework", build.getPlugins()));
            build.setPlugins(removePlugin("jacoco-maven-plugin", build.getPlugins()));
            build.setPlugins(removePlugin("maven-surefire-plugin", build.getPlugins()));
        }
        writePom(model, projectFolder);
    }

    private List<Plugin> removePlugin(String artifactId, List<Plugin> plugins) {
        Iterator<Plugin> pluginsIterator = plugins.iterator();
        while (pluginsIterator.hasNext()) {
            Plugin plugin = pluginsIterator.next();
            if (plugin.getArtifactId().equals(artifactId)) {
                pluginsIterator.remove();
            }
        }
        return plugins;
    }

    private void addPrioritizationPlugin(List<Plugin> plugins, PrioritizationConfig config) {
        Dependency dep = new Dependency();
        dep.setGroupId("info.heleno");
        dep.setArtifactId("optimus-test");
        dep.setVersion("0.0.1");

        Plugin assembly = MojoExecutor.plugin(
                "org.apache.maven.plugins",
                "maven-surefire-plugin",
                "2.20.1");

        List<Dependency> dependencies = new LinkedList<Dependency>();
        dependencies.add(dep);
        assembly.setDependencies(dependencies);

        assembly.setConfiguration(getPrioritizationProperties(config));
        plugins.add(assembly);

    }

    private Xpp3Dom getPrioritizationProperties(PrioritizationConfig config) {
        Xpp3Dom configuration = new Xpp3Dom("configuration");
        if (getExcludes() != null) {
            for (Xpp3Dom tag : getExcludes()) {
                configuration.addChild(tag);
            }
        }
        if (getIncludes() != null) {
            for (Xpp3Dom tag : getIncludes()) {
                configuration.addChild(tag);
            }
        }

        configuration.addChild(createConfigurationNode("argLine", "${argLine}"));
        configuration.addChild(createConfigurationNode("reuseForks", "true"));
        configuration.addChild(createConfigurationNode("forkCount", "1"));
        configuration.addChild(createConfigurationNode("threadCount", "1"));
        configuration.addChild(createConfigurationNode("parallel", "none"));
        configuration.addChild(createConfigurationNode("parallelOptimized", "false"));
        Xpp3Dom properties = new Xpp3Dom("properties");
        configuration.addChild(properties);
        properties.addChild(createPropertyNode("granularity", config.getGranularity()));
        properties.addChild(createPropertyNode("prioritization", config.getTechnique()));
        properties.addChild(createPropertyNode("apfd", Boolean.toString(config.isCalcAPFD())));
        properties.addChild(createPropertyNode("faultsFile", Boolean.toString(config.isGenerateFaultsFile())));
        properties.addChild(createPropertyNode("collectCoverageData", Boolean.toString(config.isCollectCoverageData())));
        properties.addChild(createPropertyNode("clustersAmount", config.getClustersAmount()));
        properties.addChild(createPropertyNode("simulateExecution", Boolean.toString(config.isSimulateExecution())));
        properties.addChild(createPropertyNode("firstVersionExecution", Boolean.toString(config.isFirstVersionExecution())));

        if (!config.getDbPath().equals("")) {
            properties.addChild(createPropertyNode("dbPath", config.getDbPath()));
            properties.addChild(createPropertyNode("projectName", config.getProjectName()));
        }
        return configuration;
    }

    private Xpp3Dom createConfigurationNode(String nodeName, String nodeValue) {
        Xpp3Dom node = new Xpp3Dom(nodeName);
        node.setValue(nodeValue);
        return node;
    }

    private Xpp3Dom createPropertyNode(String propertyName, String propertyValue) {
        Xpp3Dom property = new Xpp3Dom("property");
        Xpp3Dom name = new Xpp3Dom("name");
        name.setValue(propertyName);
        Xpp3Dom value = new Xpp3Dom("value");
        value.setValue(propertyValue);
        property.addChild(name);
        property.addChild(value);
        return property;
    }

    private void addJacocoPlugin(List<Plugin> plugins) {
        Plugin plugin = MojoExecutor.plugin(
                "org.jacoco",
                "jacoco-maven-plugin",
                OptimusMojo.jacocoVersion);

        PluginExecution execution = new PluginExecution();
        execution.addGoal("prepare-agent");
        plugin.addExecution(execution);
        plugins.add(plugin);
    }

    public Xpp3Dom[] getIncludes() {
        return includes;
    }

    private boolean hasSubmodules(String projectPath) {
        boolean hasSubmodules = false;
        Model pom = readPom(projectPath);
        if (pom != null) {
            hasSubmodules = !pom.getModules().isEmpty();
        }
        return hasSubmodules;
    }

    public List<File> getSubModules(String projectPath) {
        return getLeavesSubModules(projectPath, true);
    }

    public static boolean isMavenProject(String projectPath) {
        File projectFolder = new File(projectPath);
        if (projectFolder.exists()) {
            for (File subFile : projectFolder.listFiles()) {
                if (subFile.getName().equals("pom.xml")) {
                    return true;
                }
            }
        }
        return false;
    }

    private List<File> getLeavesSubModules(String projectPath, boolean topLevelProject) {
        List<File> submodules = new LinkedList<File>();
        File projectFolder = new File(projectPath);
        if (projectFolder.exists()) {
            if (hasSubmodules(projectPath)) {
                for (File subFile : projectFolder.listFiles()) {
                    if (subFile.isDirectory() && isMavenProject(subFile.getAbsolutePath())) {
                        submodules.addAll(getLeavesSubModules(subFile.getAbsolutePath(), false));
                    }
                }
            } else {
                if (!topLevelProject) {
                    submodules.add(projectFolder);
                }
            }
        }
        return submodules;
    }

    public String getSubModulePath(String subModuleName, String projectPath) {
        List<File> modules = getSubModules(projectPath);
        for (File module : modules) {
            if (module.getName().equals(subModuleName)) {
                return module.getAbsolutePath();
            }
        }
        return null;
    }
}
