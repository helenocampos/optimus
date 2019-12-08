package info.heleno.extractor;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import info.heleno.extractor.model.TestMethod;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author helenocampos
 */
public class AttributesExtractor extends VoidVisitorAdapter
{

    private HashMap<String, TestMethod> testMethods;
    private List<String> methodsNames;
    private List<ImportDeclaration> importEntry;

    public AttributesExtractor()
    {
        testMethods = new HashMap<String, TestMethod>();
        importEntry = new LinkedList<ImportDeclaration>();
        methodsNames = new LinkedList<>();
    }

    @Override
    public void visit(MethodDeclaration declaration, Object arg)
    {
        if (isTestMethod(declaration))
        {
            this.testMethods.put(declaration.getNameAsString(), new TestMethod(declaration.getNameAsString()));
        }
        this.getMethodsNames().add(declaration.getNameAsString());
        super.visit(declaration, arg);
    }
    
    @Override
    public void visit(ConstructorDeclaration declaration, Object arg)
    {
        this.getMethodsNames().add(declaration.getNameAsString());
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

    public HashMap<String, TestMethod> getTestMethods()
    {
        return testMethods;
    }

    public void setMethods(HashMap<String, TestMethod> methods)
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

    public List<String> getMethodsNames()
    {
        return methodsNames;
    }
}
