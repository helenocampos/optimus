/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.github.helenocampos.optimusmodificationsanalyzer;

import com.github.difflib.DiffUtils;
import com.github.difflib.algorithm.DiffException;
import com.github.difflib.patch.Delta;
import com.github.difflib.patch.Patch;
import io.github.helenocampos.extractor.model.ModificationsGranularity;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

/**
 *
 * @author helenocampos
 */
public class App
{

    public static void main(String args[]) throws IOException, DiffException
    {
//        String ORIGINAL = "/Users/helenocampos/Documents/game-of-life/gameoflife-core/src/main/java/com/wakaleo/gameoflife/domain/Cell.java";
//        String REVISED = "/Users/helenocampos/Documents/out/160943-10052018/1/gameoflife-core/src/main/java/com/wakaleo/gameoflife/domain/Cell.java";
//        List<String> original = Files.readAllLines(new File(ORIGINAL).toPath());
//        List<String> revised = Files.readAllLines(new File(REVISED).toPath());
//
////compute the patch: this is the diffutils part
//        Patch<String> patch = DiffUtils.diff(original, revised);
//
////simple output the computed patch to console
//        for (Delta<String> delta : patch.getDeltas())
//        {
//            System.out.println(delta);
//        }
        ModificationsAnalyzer analyzer = new ModificationsAnalyzer();
        analyzer.getModifiedElements(ModificationsGranularity.METHOD);
        
    }
}
