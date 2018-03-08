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
import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseException;
import com.github.javaparser.ast.CompilationUnit;
import com.sun.org.apache.bcel.internal.classfile.ClassParser;
import difflib.DiffUtils;
import difflib.Patch;
import difflib.PatchFailedException;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;

import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.commons.io.FileUtils;
import org.apache.maven.model.Build;
import org.apache.maven.model.Model;
import org.apache.maven.model.Plugin;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.model.io.xpp3.MavenXpp3Writer;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Goal which touches a timestamp file.
 *
 * @deprecated Don't use!
 */
@Mojo(name = "touch", defaultPhase = LifecyclePhase.PROCESS_TEST_CLASSES)
public class MyMojo
        extends AbstractMojo
{

    /**
     * Location of the file.
     */
    @Parameter(defaultValue = "${project.build.directory}", property = "targetDir", required = true)
    private File targetDir;

    @Parameter(property = "outputDir", required = true)
    private File outputDir;

    @Parameter(defaultValue = "${project.basedir}", property = "projectDir", required = true)
    private File projectDir;

    private HashMap<String, ProjectClass> classes;
    private MutationSummary summary;
    private String lastModifiedSummaryPath;
    private long lastModifiedSummaryDate = Integer.MIN_VALUE;

    public void execute()
            throws MojoExecutionException

    {
        classes = new HashMap<String, ProjectClass>();

        if (targetDir != null)
        {
            File pitReportsFolder = null;
            File classesFolder = null;
            for (File f : targetDir.listFiles())
            {
                if (f.getName().equals("pit-reports"))
                {
                    pitReportsFolder = f;

                }
                if (f.getName().equals("classes"))
                {
                    classesFolder = f;
                }

            }
            if (classesFolder != null)
            {
                crawl(classesFolder, false);
            }

            //results folders are named with numbers, 
            if (pitReportsFolder != null)
            {
                crawl(pitReportsFolder, true);

            }

        }
        parseMutationSummary();
        filterClassesMutants();
        mergeMutants();
        copyProjectDirectory();
        substituteSourceFiles();
        modifyNewPom();
        generateFaultsFile();
        searchAndDeleteProjectData();
    }

    private void generateFaultsFile()
    {
        List<String> testsRevealingFaults = new LinkedList<String>();
        for (MutantDetails mutant : this.summary.getKilledMutations())
        {
            if(!testsRevealingFaults.contains(mutant.getKillingTest())){
                testsRevealingFaults.add(mutant.getKillingTest());
            }
            
        }
        Path file = Paths.get("TestsRevealingFaults");
        try
        {
            Files.write(file, testsRevealingFaults, Charset.forName("UTF-8"));
        } catch (IOException ex)
        {
//            Logger.getLogger(MyMojo.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /*
        Generate the fault seeded class by merging mutants from each class
     */
    private void mergeMutants()
    {
        for (ProjectClass clazz : classes.values())
        {
            List<String> original = clazz.getSourceCodeText();
            for (ProjectClassMutant mutant : clazz.getMutants())
            {
                List<String> mutantText = mutant.getSourceCodeText();

                Patch<String> patch = DiffUtils.diff(original, mutantText);

                try
                {
                    original = DiffUtils.patch(original, patch);
                } catch (PatchFailedException ex)
                {

                }
            }
            clazz.setGeneratedMutant(original);
        }
    }

    //search for source files and substitute them with generated mutants
    private void substituteSourceFiles()
    {
        crawl(outputDir);
    }

    private void crawl(File f)
    {
        if (f.isDirectory())
        {
            File[] subFiles = f.listFiles();
            for (File subFile : subFiles)
            {
                crawl(subFile);
            }
        } else
        {
            if (f.getName().contains(".java"))
            {
                String qualifiedName = getSourceClassQualifiedName(f);
                qualifiedName = qualifiedName.replace(".java", "");
                ProjectClass clazz = classes.get(qualifiedName);
                if (clazz != null)
                {
                    if (clazz.getMutants().size() > 0)
                    {
                        Path file = Paths.get(f.getAbsolutePath());
                        try
                        {
                            Files.write(file, clazz.getGeneratedMutant(), Charset.forName("UTF-8"));
                        } catch (IOException ex)
                        {
                            Logger.getLogger(MyMojo.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                }
            }
        }
    }

    private void copyProjectDirectory()
    {
        try
        {
            File newProjectDir = new File(outputDir, projectDir.getName());
            if (newProjectDir.exists())
            {
                FileUtils.deleteDirectory(newProjectDir);
            }
            newProjectDir.mkdir();
            FileUtils.copyDirectory(projectDir, newProjectDir);
        } catch (IOException ex)
        {
            Logger.getLogger(MyMojo.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /*
        This is important in order to guarantee that no wrong paths are in the projectData.xml file. 
    A new projectData.xml file will be generated in the next build of the exported project, with all up to date info.
    */
    private void searchAndDeleteProjectData(){
        File newProjectDir = new File(outputDir, projectDir.getName());
        if(newProjectDir.exists()){
            for(File file: newProjectDir.listFiles()){
                if(file.getName().equals("projectData.xml")){
                    file.delete();
                }
            }
        }
    }

    private void modifyNewPom()
    {
        MavenXpp3Reader reader = new MavenXpp3Reader();
        File newProjectDir = new File(outputDir, projectDir.getName());
        if (newProjectDir.exists())
        {
            File pom = new File(newProjectDir, "pom.xml");
            if (pom.exists())
            {
                try
                {
                    Model model = reader.read(new FileReader(pom));
                    Build build = model.getBuild();
                    build.setPlugins(removePlugin("pitest-maven", build.getPlugins()));
                    build.setPlugins(removePlugin("fault-injection-plugin", build.getPlugins()));
                    MavenXpp3Writer writer = new MavenXpp3Writer();
                    OutputStream output = new FileOutputStream(pom);
                    writer.write(output, model);
                } catch (FileNotFoundException ex)
                {
                    Logger.getLogger(MyMojo.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IOException ex)
                {
                    Logger.getLogger(MyMojo.class.getName()).log(Level.SEVERE, null, ex);
                } catch (XmlPullParserException ex)
                {
                    Logger.getLogger(MyMojo.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }

    }

    private List<Plugin> removePlugin(String artifactId, List<Plugin> plugins)
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

    /*
    Filter mutants to allow only mutants that can be killed by application tests
    and max of 1 mutant per method of the mutated class
     */
    private void filterClassesMutants()
    {
        for (ProjectClass clazz : classes.values())
        {
            //remove mutants that survived the tests
            Iterator<ProjectClassMutant> classMutantsIterator = clazz.getMutants().iterator();
            while (classMutantsIterator.hasNext())
            {
                ProjectClassMutant classMutant = classMutantsIterator.next();
                if (!this.summary.isAKilledMutant(classMutant.getDetails()))
                {
                    classMutantsIterator.remove();
                }
            }

            //allow only one mutant per method of the class. remove any secondary
            //compare each mutant to every other (in terms of which method they mutate)
            List<ProjectClassMutant> toRemove = new LinkedList<ProjectClassMutant>();
            for(ProjectClassMutant mutant: clazz.getMutants()){
                for(ProjectClassMutant anotherMutant: clazz.getMutants()){
                    if (!mutant.equals(anotherMutant) && mutant.getDetails().getMethod().equals(anotherMutant.getDetails().getMethod()))
                    {
                        if(!toRemove.contains(mutant)){
                            toRemove.add(mutant);
                        }
                    }
                }
            }
            clazz.getMutants().removeAll(toRemove);
//            classMutantsIterator = clazz.getMutants().iterator();
//            while (classMutantsIterator.hasNext())
//            {
//                ProjectClassMutant classMutant = classMutantsIterator.next();
//                Iterator<ProjectClassMutant> classMutantsIteratorCompare = clazz.getMutants().iterator();
//                while (classMutantsIteratorCompare.hasNext())
//                {
//                    ProjectClassMutant classMutantCompare = classMutantsIteratorCompare.next();
//                    if (!classMutant.equals(classMutantCompare) && classMutant.getDetails().getMethod().equals(classMutantCompare.getDetails().getMethod()))
//                    {
//                        classMutantsIteratorCompare.remove();
//                    }
//                }
//            }
        }
    }

    private void parseMutationSummary()
    {
        if (lastModifiedSummaryPath != null)
        {
            File f = new File(lastModifiedSummaryPath);
            if (f.exists())
            {
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                factory.setValidating(false);
                factory.setIgnoringElementContentWhitespace(true);
                DocumentBuilder builder;
                try
                {
                    builder = factory.newDocumentBuilder();
                    Document doc = builder.parse(f);
                    NodeList nodeList = doc.getElementsByTagName("mutation");
                    summary = new MutationSummary(lastModifiedSummaryDate);
                    for (int i = 0; i < nodeList.getLength(); i++)
                    {
                        summary.addKilledMutant(getKilledMutation(nodeList.item(i)));
                    }
                } catch (ParserConfigurationException ex)
                {
                    Logger.getLogger(MyMojo.class.getName()).log(Level.SEVERE, null, ex);
                } catch (SAXException ex)
                {
                    Logger.getLogger(MyMojo.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IOException ex)
                {
                    Logger.getLogger(MyMojo.class.getName()).log(Level.SEVERE, null, ex);
                }

            }
        }
    }

    private MutantDetails getKilledMutation(Node node)
    {
        if (node.getNodeType() == Node.ELEMENT_NODE)
        {

            Element element = (Element) node;
            String status = element.getAttribute("status");
            if (status.equalsIgnoreCase("KILLED"))
            {
                String clazz = getTagValue("mutatedClass", element);
                String method = getTagValue("mutatedMethod", element);
                String killingTest = getTagValue("killingTest", element);
//                String indexString = getTagValue("index", element);
                int index = Integer.parseInt(getTagValue("index", element));
                List<Integer> indexes = new LinkedList<Integer>();
                indexes.add(index);
                return new MutantDetails(clazz, method, indexes, killingTest);
            }
        }

        return null;
    }

    private String getTagValue(String tag, Element element)
    {
        NodeList nodeList = element.getElementsByTagName(tag).item(0).getChildNodes();
        Node node = (Node) nodeList.item(0);
        return node.getNodeValue();
    }

    private void crawl(File f, boolean mutants)
    {
        if (f.isDirectory())
        {
            File[] subFiles = f.listFiles();
            for (File subFile : subFiles)
            {
                crawl(subFile, mutants);
            }
        } else
        {
            if (f.getName().contains(".class"))
            {
                try
                {
                    ClassParser parser = new ClassParser(f.getPath());
                    com.sun.org.apache.bcel.internal.classfile.JavaClass cls = parser.parse();
                    String qualifiedName = f.getName().replace(".class", "");
                    if (!mutants)
                    {
                        qualifiedName = cls.getPackageName() + "." + qualifiedName;
                        classes.put(qualifiedName, new ProjectClass(qualifiedName, f.getAbsolutePath(), f));
                    } else
                    {
                        ProjectClass clazz = classes.get(qualifiedName);
                        File mutantDetailsFile = searchMutantDetailsFile(f);
                        MutantDetails mutantDetails = null;
                        if (mutantDetailsFile != null)
                        {
                            mutantDetails = MutantDetails.parseDetails(mutantDetailsFile);
                        }
                        clazz.addMutant(new ProjectClassMutant(qualifiedName, f.getAbsolutePath(), mutantDetails, f));

                    }

                } catch (IOException ex)
                {
//                    Logger.getLogger(LocalProjectCrawler.class.getName()).log(Level.SEVERE, null, ex);
                }

            } else if (f.getName().contains("mutations.xml"))
            {
                if (f.lastModified() > lastModifiedSummaryDate)
                {
                    lastModifiedSummaryDate = f.lastModified();
                    lastModifiedSummaryPath = f.getAbsolutePath();
                }
            }

        }
    }

    private File searchMutantDetailsFile(File classFile)
    {
        if (classFile.exists())
        {
            File parentFolder = classFile.getParentFile();
            for (File file : parentFolder.listFiles())
            {
                if (file.getName().equals("details.txt"))
                {
                    return file;
                }
            }
        }
        return null;
    }

    private String getSourceClassQualifiedName(File file)
    {

        String packageName = getPackageFromSourceFile(file.getAbsolutePath());
        return packageName.concat(".").concat(file.getName());
    }

    private String getPackageFromSourceFile(String file)
    {
        FileInputStream in = null;
        CompilationUnit cu = null;
        try
        {
            in = new FileInputStream(file);
            cu = JavaParser.parse(in);
        } catch (FileNotFoundException ex)
        {
//            Logger.getLogger(CouplingManager.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ParseException ex)
        {

        } finally
        {
            if (in != null)
            {
                try
                {
                    in.close();
                } catch (IOException ex)
                {
//                    Logger.getLogger(CouplingManager.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        if (cu != null)
        {
            return cu.getPackage().getName().toString();
        } else
        {
            return "";
        }
    }

    public File getOutputDir()
    {
        return outputDir;
    }

    public void setOutputDir(File outputDir)
    {
        this.outputDir = outputDir;
    }

    public File getProjectDir()
    {
        return projectDir;
    }

    public void setProjectDir(File projectDir)
    {
        this.projectDir = projectDir;
    }
}
