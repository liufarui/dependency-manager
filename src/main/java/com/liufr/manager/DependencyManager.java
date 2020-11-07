package com.liufr.manager;

import com.liufr.manager.service.GraphBuilder;
import com.liufr.manager.service.impl.*;
import com.liufr.manager.model.Project;
import com.liufr.manager.util.PomFileFinder;
import com.liufr.manager.model.Neo4jConn;
import com.liufr.manager.util.XMLConverter;
import org.junit.Test;
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
    static String root;
    static String url;
    static String username;
    static String password;

    public static void main(String[] args) throws Exception {
        switch (args.length) {
            case 0:
                System.out.println("This tool is mainly dedicated to exporting module or product dependencies to graph databases, such as Neo4J.");
                System.out.println("Then we can find out the dependencies between modules and manage them accordingly by observing the graph database.");
                System.out.println("Including finding out circular dependencies, false dependencies, redundant dependencies are a good way.");
                System.out.println("Warning: Your existing graph database will be cleared before using this tool!");
                System.out.println("Hwo to use it?");
                System.out.println("Options:");
                System.out.println("    <Directory> - The directory you want to detect.");
                System.out.println("    <ServerURL> - The address of your graph database (Neo4J).");
                System.out.println("    <Username>  - The username of your graph database (Neo4J).");
                System.out.println("    <Password>  - The password of your graph database (Neo4J).");
                System.out.println("Example: java -jar dependency-manager-0.0.1-SNAPSHOT-jar-with-dependencies.jar D:/workspace/so bolt://localhost:7687 neo4j neo4j");
                System.exit(0);
            case 4:
                root = args[0];
                url = args[1];
                username = args[2];
                password = args[3];
                break;
            default:
                System.err.println("ERROR: Wrong number of arguments.");
                System.err.println("Example: java -jar dependency-manager-0.0.1-SNAPSHOT-jar-with-dependencies.jar D:\\workspace\\so bolt://localhost:7687 neo4j neo4j");
                System.exit(1);
        }
        System.out.println("Start!!!!!!!!");
        Neo4jConn conn = new Neo4jConn(url, username, password);
        build = new GraphBuilderImpl(conn);

        if (!build.connect()) {
            System.exit(1);
        }
        exportAllJD(root);
        System.out.println("End!!!!!!!!");
    }

    @Test
    public void exportAllJD() throws Exception {
        String root = "D:\\workspace\\so";
        String url = "bolt://localhost:7687";
        String username = "neo4j";
        String password = "123456";

        Neo4jConn conn = new Neo4jConn(url, username, password);
        build = new GraphBuilderImpl(conn);

        exportAllJD(root);
    }

    @Test
    public void checkConnect() throws Exception {
        String url = "bolt://localhost:7687";
        String username = "neo4j";
        String password = "123456";

        Neo4jConn conn = new Neo4jConn(url, username, password);
        build = new GraphBuilderImpl(conn);
        Boolean isConnected = build.connect();
        System.out.println("asd");
    }

    public static void exportAllJD(String root) throws Exception {
        /* Warning! Clear all data in neo4J! */
        if (!build.cleanup()) {
            return;
        }
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
