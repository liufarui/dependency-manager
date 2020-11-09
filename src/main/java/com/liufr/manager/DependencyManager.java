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
    static String type;
    static String fix;

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
                System.out.println("    <Type>      - The type you want to manage (Project/Module).");
                System.out.println("    </Format>   - The format you want to match, up to two (*) can be used (e.g. org*, *sql, org*spring*cn).");
                System.out.println("                - Format can be ignored! Full match will be used by default.");
                System.out.println("                - Special reminder: Single full match symbol (*) cannot be used.");
                System.out.println("Example: java -jar dependency-manager-0.0.1-SNAPSHOT-jar-with-dependencies.jar D:/workspace/so bolt://localhost:7687 neo4j neo4j Module org.spring*");
                System.exit(0);
            case 5:
            case 6:
                root = args[0];
                url = args[1];
                username = args[2];
                password = args[3];
                type = args[4];
                if (args.length == 6) {
                    fix = args[5];
                }
                break;
            default:
                System.err.println("ERROR: Wrong number of arguments, your number of arguments is " + args.length);
                System.err.println("Example: java -jar dependency-manager-0.0.1-SNAPSHOT-jar-with-dependencies.jar D:\\workspace\\so bolt://localhost:7687 neo4j neo4j Module org.spring*");
                System.exit(1);
        }
        System.out.println("Start!!!!!!!!");
        build = new GraphBuilderImpl(new Neo4jConn(url, username, password), fix);

        if (!build.connect()) {
            System.exit(1);
        }
        if ("Project".equals(type)) {
            exportOnlyProj();
        } else if ("Module".equals(type)) {
            exportAll();
        } else {
            System.err.println("ERROR: Wrong type, please input Module or Project, case sensitive.");
            System.exit(1);
        }
        System.out.println("End!!!!!!!!");
    }

    @Test
    public void exportAllTest() throws Exception {
        init();
        exportAll();
    }

    @Test
    public void checkConnectTest() throws Exception {
        init();
        build.connect();
    }

    @Test
    public void exportOnlyProjTest() throws Exception {
        init();
        exportOnlyProj();
    }

    public static void init() throws Exception {
        root = "D:\\workspace\\so";
        Neo4jConn conn = new Neo4jConn("bolt://localhost:7687", "neo4j", "123456");
        build = new GraphBuilderImpl(conn, "*");
    }

    public static void exportAll() throws Exception {
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

    public static void exportOnlyProj() throws Exception {
        /* Warning! Clear all data in neo4J! */
        if (!build.cleanup()) {
            return;
        }
        List<Project> projList = getProjList(getPaths(root));

        build.buildProjRepoGraph(projList);
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
