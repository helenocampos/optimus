package io.github.helenocampos.extractor;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseException;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import io.github.helenocampos.extractor.model.ClassMethod;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author helenocampos
 */
public class AttributesExtractor extends VoidVisitorAdapter
{

    private HashMap<String, ClassMethod> testMethods;
    private List<ImportDeclaration> importEntry;

    public AttributesExtractor()
    {
        testMethods = new HashMap<String, ClassMethod>();
        importEntry = new ArrayList<ImportDeclaration>();
    }

    @Override
    public void visit(MethodDeclaration declaration, Object arg)
    {
        if (isTestMethod(declaration))
        {
            this.testMethods.put(declaration.getNameAsString(), new ClassMethod(declaration.getNameAsString()));
        }
        super.visit(declaration, arg);
    }

    private boolean isTestMethod(MethodDeclaration declaration)
    {
        List<AnnotationExpr> annotations = declaration.getAnnotations();
        for (AnnotationExpr annotation : annotations)
        {
            if (annotation.getNameAsString().equals("Test"))
            { // junit 4 test
                return true;
            }
        }
        //junit 3 test has "Test" in its name
        return StringUtils.containsIgnoreCase(declaration.getNameAsString(),"Test");
    }

    @Override
    public void visit(ImportDeclaration importEntry, Object arg)
    {
        getImportEntry().add(importEntry);
        super.visit(importEntry, arg);
    }

    public HashMap<String, ClassMethod> getMethods()
    {
        return testMethods;
    }

    public void setMethods(HashMap<String, ClassMethod> methods)
    {
        this.testMethods = methods;
    }

    public List<ImportDeclaration> getImportEntry()
    {
        return importEntry;
    }

    public void setImportEntry(List<ImportDeclaration> importEntry)
    {
        this.importEntry = importEntry;
    }

    public static AttributesExtractor parseClass(String classPath)
    {
        FileInputStream in = null;
        CompilationUnit cu = null;
        AttributesExtractor extractor = new AttributesExtractor();
        try
        {
            in = new FileInputStream(classPath);
            cu = JavaParser.parse(in);
        } catch (FileNotFoundException ex)
        {
//            Logger.getLogger(CouplingManager.class.getName()).log(Level.SEVERE, null, ex);
        } 

        finally
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
        return extractor;
    }
}
