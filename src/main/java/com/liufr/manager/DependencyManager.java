package com.liufr.manager;

import com.liufr.manager.model.Dependency;
import com.liufr.manager.model.IEnum;
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
import java.util.HashMap;
import java.util.List;

/**
 * @author lfr
 * @date 2020/11/6
 */
public class DependencyManager {
    static GraphBuilder build;
    static String url;
    static String username;
    static String password;
    static String operate;
    static String directory;
    static String type;
    static String format;
    static String artifact;
    static String depend;

    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
            System.out.println("This tool is mainly dedicated to exporting module or product dependencies to graph databases, such as Neo4J.");
            System.out.println("Then we can find out the dependencies between modules and manage them accordingly by observing the graph database.");
            System.out.println("Including finding out circular dependencies, false dependencies, redundant dependencies are a good way.");
            System.out.println("Warning: Your existing graph database will be cleared before using this tool!");
            System.out.println("Hwo to use it?");
            System.out.println("Options:");
            System.out.println("    <url>           - The address of your graph database (Neo4J).");
            System.out.println("    <user>          - The username of your graph database (Neo4J).");
            System.out.println("    <password>      - The password of your graph database (Neo4J).");
            System.out.println("    <operate>       - The operation you want to select (export/import), Default: export.");
            System.out.println("                    - If you use import, you need directory, type, format.");
            System.out.println("                    - If you use export, you need artifact, depend.");
            System.out.println("    <directory/dir> - The directory you want to detect.");
            System.out.println("    <type>          - The type you want to manage (module/project), Default: module.");
            System.out.println("    <format>        - The format you want to match, up to two (*) can be used (e.g. org*, *sql, org*spring*cn).");
            System.out.println("                    - Format can be ignored! Full match will be used by Default.");
            System.out.println("                    - Special reminder: Single full match symbol (*) cannot be used.");
            System.out.println("    <artifact>      - The artifact you want to export dependency (e.g. log4j, spring-test).");
            System.out.println("    <depend>        - The upper-level dependencies or lower-level dependencies you want to export (all/above/below), Default: all.");
            System.out.println("Example: java -jar dependency-manager-0.0.1-SNAPSHOT-jar-with-dependencies.jar url=bolt://localhost:7687 user=neo4j password=123456 artifact=spring-test operate=export depend=above");
            System.exit(0);
        }
        System.out.println("Start!!!!!!!!");
        try {
            HashMap<String, String> parameter = getParameter(args);
            bus(parameter);
        } catch (Exception e) {
            System.err.println(e.toString());
            System.exit(1);
        }
        System.out.println("End!!!!!!!!");
        build.GCNeo4JHandlerImpl();
    }

    public static void bus(HashMap<String, String> parameter) throws Exception {
        buildGraph(parameter);
        if (!build.connect()) {
            throw new Exception("ERROR: Failed to connect to Neo4J!");
        }
        operate = parameter.get("operate");

        if ("export".equals(operate)) {
            artifact = parameter.get("artifact");
            depend = parameter.get("depend");
            if (IEnum.towardsAbove(depend)) {
                List<Dependency> deps = build.getDependency(artifact, IEnum.Towards.above);
                System.out.printf("The following modules depend on %s:%n", artifact);
                for (Dependency dep : deps) {
                    System.out.printf("        %s%n", dep.getArtifactId());
                }
            }
            if (IEnum.towardsBelow(depend)) {
                List<Dependency> deps = build.getDependency(artifact, IEnum.Towards.below);
                System.out.printf("%s module depend on:%n", artifact);
                for (Dependency dep : deps) {
                    System.out.printf("        %s%n", dep.getArtifactId());
                }
            }
        } else {
            directory = parameter.containsKey("directory") ? parameter.get("directory") : parameter.get("dir");
            type = parameter.get("type");
            format = parameter.get("format");
            if ("project".equals(type)) {
                exportOnlyProj();
            } else if ("module".equals(type)) {
                exportAll();
            } else {
                System.err.println("ERROR: Wrong type, please input Module or Project, case sensitive.");
                System.exit(1);
            }
        }
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

    @Test
    public void test() throws Exception {
        init();
        build.buildProj(null);
    }

    @Test
    public void getDependency() throws Exception {
        init();
        List<Dependency> deps = build.getDependency("log4j", IEnum.Towards.valueOf("above"));
        System.out.println(deps);
    }

    public static void init() throws Exception {
        directory = "D:\\workspace\\so";
        Neo4jConn conn = new Neo4jConn("bolt://localhost:7687", "neo4j", "123456");
        build = new GraphBuilderImpl(conn, "*");
    }

    public static void exportAll() throws Exception {
        /* Warning! Clear all data in neo4J! */
        if (!build.cleanup()) {
            return;
        }
        List<Project> projList = getProjList(getPaths(directory));
        build.buildProj(projList);
        for (Project proj : projList) {
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
        List<Project> projList = getProjList(getPaths(directory));

        build.buildProjRepoGraph(projList);
    }

    private static HashMap<String, String> getParameter(String[] args) throws Exception {
        HashMap<String, String> parameter = new HashMap<>();
        for (String arg : args) {
            String[] node = arg.split("=");
            parameter.put(node[0], node[1]);
        }
        checkParameter(parameter);
        return parameter;
    }

    private static void checkParameter(HashMap<String, String> parameter) throws Exception {
        if (!parameter.containsKey("url")) {
            throw new Exception("ERROR: Neo4J information is missing! Url must be provided.");
        }
        if (!parameter.containsKey("user")) {
            throw new Exception("ERROR: Neo4J information is missing! User must be provided.");
        }
        if (!parameter.containsKey("password")) {
            throw new Exception("ERROR: Neo4J information is missing! Password must be provided.");
        }
        if (!parameter.containsKey("operate")) {
            parameter.put("operate", "export");
        }
        if ("import".equals(parameter.get("operate"))) {
            if (!parameter.containsKey("dir") && !parameter.containsKey("directory")) {
                throw new Exception("ERROR: Directory information is missing! You can use as follows: D:/workspace/springTest.");
            }
            if (!parameter.containsKey("type")) {
                parameter.put("type", "module");
            }
            if (!"project".equals(parameter.get("type")) && !"module".equals(parameter.get("type"))) {
                throw new Exception("ERROR: Type information is error! You can use only project/module.");
            }
            if (!parameter.containsKey("format")) {
                parameter.put("format", "");
            }
        } else if ("export".equals(parameter.get("operate"))) {
            if (!parameter.containsKey("artifact")) {
                throw new Exception("ERROR: Artifact information is missing! You can use as follows: log4j, spring-test.");
            }
            if (!parameter.containsKey("depend")) {
                parameter.put("depend", "all");
            }
            if (!"all".equals(parameter.get("depend")) && !"above".equals(parameter.get("depend")) && !"below".equals(parameter.get("depend"))) {
                throw new Exception("ERROR: Depend information is missing! You can only use all/above/below.");
            }
        }
    }

    private static void buildGraph(HashMap<String, String> parameter) throws Exception {
        url = parameter.get("url");
        username = parameter.get("user");
        password = parameter.get("password");
        build = new GraphBuilderImpl(new Neo4jConn(url, username, password), format);
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
