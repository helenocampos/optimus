/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package info.heleno.optimusmodificationsanalyzer;

import com.github.difflib.DiffUtils;
import com.github.difflib.algorithm.DiffException;
import com.github.difflib.patch.Delta;
import com.github.difflib.patch.Patch;
import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import info.heleno.extractor.model.ModificationsGranularity;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author helenocampos
 */
public class ProgramElement
{

    String name;
    String oldVersionPath;
    String newVersionPath;
    ModificationsGranularity granularity;

    public ProgramElement(String name, String oldVersionPath, String newVersionPath, ModificationsGranularity granularity)
    {
        this.name = name;
        this.oldVersionPath = oldVersionPath;
        this.newVersionPath = newVersionPath;
        this.granularity = granularity;
    }

    private List<String> getCurrentVersionElementText()
    {
        return getElementText(this.newVersionPath);
    }

    private List<String> getOldVersionElementText()
    {
        return getElementText(this.oldVersionPath);
    }

    private List<String> getElementText(String path)
    {
        List<String> elementText = new LinkedList<>();
        try
        {
            if (granularity.equals(ModificationsGranularity.CLASS))
            {
                elementText = Files.readAllLines(new File(path).toPath());
            } else if (granularity.equals(ModificationsGranularity.METHOD))
            {
                elementText = getMethodText(path, name);
            }
        } catch (IOException ex)
        {
            Logger.getLogger(ProgramElement.class.getName()).log(Level.SEVERE, null, ex);
        }
        return elementText;
    }

    private List<String> getMethodText(String classPath, String methodName)
    {
        FileInputStream in = null;
        CompilationUnit cu = null;
        AttributesExtractor extractor = new AttributesExtractor(methodName);
        try
        {
            in = new FileInputStream(classPath);
            cu = JavaParser.parse(in);
        } catch (FileNotFoundException ex)
        {
//            Logger.getLogger(CouplingManager.class.getName()).log(Level.SEVERE, null, ex);
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
            cu.accept(extractor, cu);
        }
        List<String> methodTextList = new LinkedList<>();
        if (!extractor.methodText.equals(""))
        {
            methodTextList = Arrays.asList(extractor.methodText.split("\n"));
        }
        return methodTextList;
    }

    public List<Delta<String>> getDeltas()
    {
        List<Delta<String>> deltas = new LinkedList<>();
        try
        {
            List<String> currentVersionText = getCurrentVersionElementText();
            List<String> oldVersionText = getOldVersionElementText();
            if (!currentVersionText.isEmpty() && !oldVersionText.isEmpty())
            {
                Patch<String> patch = DiffUtils.diff(currentVersionText, oldVersionText);
                deltas = patch.getDeltas();
            }
        } catch (DiffException ex)
        {
            Logger.getLogger(ModificationsAnalyzer.class.getName()).log(Level.SEVERE, null, ex);
        }
        return deltas;
    }

    private class AttributesExtractor extends VoidVisitorAdapter
    {

        String methodText;
        String methodName;

        public AttributesExtractor(String methodName)
        {
            this.methodText = "";
            this.methodName = methodName;
        }

        @Override
        public void visit(MethodDeclaration declaration, Object arg)
        {
            if (declaration.getNameAsString().equals(this.methodName))
            {
                methodText = declaration.toString();
            }
            super.visit(declaration, arg);
        }

        @Override
        public void visit(ConstructorDeclaration declaration, Object arg)
        {
            if (declaration.getNameAsString().equals(this.methodName))
            {
                methodText = declaration.toString();
            }
            super.visit(declaration, arg);
        }
    }
}
