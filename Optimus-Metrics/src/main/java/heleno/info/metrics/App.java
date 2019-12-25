/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package info.heleno.metrics;

import info.heleno.extractor.LocalProjectCrawler;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.apache.commons.io.FilenameUtils;

/**
 *
 * @author helenocampos
 */
public class App
{

    static int totalLines = 0;
    static int totalClassFiles = 0;

    public static void main(String[] args)
    {
        if (args != null)
        {
            String projectFolder = args[0];
            System.out.println(projectFolder);
            crawl(new File(projectFolder));
            System.out.println("Total LOC:"+totalLines);
            System.out.println("Total Class files:"+totalClassFiles);
            if(args.length>1){
                LocalProjectCrawler crawler = new LocalProjectCrawler(projectFolder);
                System.out.println("Test classes: "+crawler.getProjectData().getTestClassesTotal());
                System.out.println("Test methods: "+crawler.getProjectData().getTestMethodsTotal());
            }

        }
    }

    private static void crawl(File f)
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
            String fileExtension = FilenameUtils.getExtension(f.getName());
            if (fileExtension.equals("java"))
            {
                Path path = Paths.get(f.getAbsolutePath());
                try
                {
                    totalClassFiles++;
                    totalLines+= Files.lines(path).count();
                } catch (IOException ex)
                {
//                    Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }
}
