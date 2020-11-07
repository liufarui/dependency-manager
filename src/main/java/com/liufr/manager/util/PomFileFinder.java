package com.liufr.manager.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

/**
 * @author lfr
 * @date 2020/11/6
 */
public class PomFileFinder {

    private final File repoDir;

    public PomFileFinder(String folderName) {
        this.repoDir = new File(folderName);;
    }

    public List<Path> getAllPOMs() {
        Finder finder = new Finder("pom.xml");
        try {
            Files.walkFileTree(repoDir.toPath(), Collections.<FileVisitOption> emptySet(), 20, finder);
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        List<Path> files = finder.done();
        return files;
    }
}
