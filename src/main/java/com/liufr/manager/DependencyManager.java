package com.liufr.manager;

import com.liufr.manager.service.GraphBuilder;
import com.liufr.manager.service.impl.*;
import com.liufr.manager.model.Project;
import com.liufr.manager.util.PomFileFinder;
import com.liufr.manager.model.Neo4jConn;
import com.liufr.manager.util.XMLConverter;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * @author lfr
 * @date 2020/11/6
 */
public class DependencyManager {
    static GraphBuilder build;

    public static void main(String[] args) throws Exception {
        System.out.println("Start!!!!!!!!");
        String root = "D:\\workspace\\so";
        String url = "bolt://localhost:7687";
        String username = "neo4j";
        String password = "123456";

        Neo4jConn conn = new Neo4jConn(url, username, password);
        exportAllJD(conn, root);
        System.out.println("End!!!!!!!!");
    }

    @org.junit.Test
    public void exportAllJD() throws Exception {
        String root = "D:\\workspace\\so";
        String url = "bolt://localhost:7687";
        String username = "neo4j";
        String password = "123456";

        Neo4jConn conn = new Neo4jConn(url, username, password);
        exportAllJD(conn, root);
    }

    public static void exportAllJD(Neo4jConn conn, String root) throws Exception {
        build = new GraphBuilderImpl(conn);
        /* Warning! Clear all data in neo4J! */
        build.cleanup();
        for (Project proj : getProjList(getPaths(root))) {
            try {
                System.out.println(proj.toString());
                build.buildRepoGraph(proj);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static List<Project> getProjList(List<Path> paths) throws SAXException, ParserConfigurationException, FileNotFoundException, JAXBException {
        List<Project> projList = new ArrayList<>();
        XMLConverter converter = new XMLConverter();
        for (Path path : paths) {
            Project proj = converter.convertFromXML(path);
            projList.add(proj);
        }
        return projList;
    }

    private static List<Path> getPaths(String root) {
        PomFileFinder pomFileFinder = new PomFileFinder(root);
        List<Path> paths = pomFileFinder.getAllPOMs();
        System.out.println(paths);
        return paths;
    }
}
