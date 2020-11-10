package com.liufr.manager;

import com.liufr.manager.model.Dependency;
import com.liufr.manager.service.GraphBuilder;
import com.liufr.manager.model.Project;
import com.liufr.manager.type.DependTowards;
import com.liufr.manager.type.Operation;
import com.liufr.manager.type.Type;
import com.liufr.manager.util.PomFileFinder;
import com.liufr.manager.util.Neo4JConn;
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
    static String operation;
    static String directory;
    static String type;
    static String format;
    static String artifact;
    static String dependTowards;

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
            System.out.println("    <operation>       - The operation you want to select (exportDB/importDB), Default: exportDB.");
            System.out.println("                    - If you use import, you need directory, type, format.");
            System.out.println("                    - If you use export, you need artifact, depend.");
            System.out.println("    <directory/dir> - The directory you want to detect.");
            System.out.println("    <type>          - The type you want to manage (module/project), Default: module.");
            System.out.println("    <format>        - The format you want to match, up to two (*) can be used (e.g. org*, *sql, org*spring*cn).");
            System.out.println("                    - Format can be ignored! Full match will be used by Default.");
            System.out.println("                    - Special reminder: Single full match symbol (*) cannot be used.");
            System.out.println("    <artifact>      - The artifact you want to export dependency (e.g. log4j, spring-test).");
            System.out.println("    <depend>        - The upper-level dependencies or lower-level dependencies you want to export (all/above/below), Default: all.");
            System.out.println("Example: java -jar dependency-manager-0.0.1-SNAPSHOT-jar-with-dependencies.jar url=bolt://localhost:7687 user=neo4j password=123456 artifact=spring-test operation=export depend=above");
            System.exit(0);
        }
        System.out.println("Start!!!!!!!!");
        try {
            HashMap<String, String> parameter = getParameter(args);
            execute(parameter);
        } catch (Exception e) {
            System.err.println(e.toString());
            System.exit(1);
        }
        System.out.println("End!!!!!!!!");
        build.GCNeo4JHandler();
    }

    public static void execute(HashMap<String, String> parameter) throws Exception {
        buildGraph(parameter);
        if (!build.connect()) {
            throw new Exception("ERROR: Failed to connect to Neo4J!");
        }
        operation = parameter.get("operation");

        if ("export".equals(operation)) {
            artifact = parameter.get("artifact");
            dependTowards = parameter.get("depend");
            if (DependTowards.towardsAbove(dependTowards)) {
                List<Dependency> deps = build.getDependency(artifact, DependTowards.above);
                System.out.printf("The following modules depend on %s:%n", artifact);
                for (Dependency dep : deps) {
                    System.out.printf("        %s%n", dep.getArtifactId());
                }
            }
            if (DependTowards.towardsBelow(dependTowards)) {
                List<Dependency> deps = build.getDependency(artifact, DependTowards.below);
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
                exportOnlyProject();
            } else if ("module".equals(type)) {
                exportAll();
            } else {
                System.err.println("ERROR: Wrong type, please input Module or Project, case sensitive.");
                System.exit(1);
            }
        }
    }

    public static void exportAll() throws Exception {
        /* Warning! Clear all data in neo4J! */
        if (!build.cleanup()) {
            return;
        }
        List<Project> projectList = getProjectList(getPaths(directory));
        build.buildProject(projectList);
        for (Project project : projectList) {
            try {
                System.out.println(project.toString());
                build.buildRepoGraph(project);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void exportOnlyProject() throws Exception {
        /* Warning! Clear all data in neo4J! */
        if (!build.cleanup()) {
            return;
        }
        List<Project> projectList = getProjectList(getPaths(directory));

        build.buildProjectRepoGraph(projectList);
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
        if (!parameter.containsKey("operation")) {
            parameter.put("operation", "export");
        }
        if (!Operation.contain(parameter.get("operation"))) {
            throw new Exception("ERROR: Operation information is error! You can use only exportDB/importDB.");
        }
        operation = parameter.get("operation");
        if (Operation.valueOf(operation) == Operation.importDB) {
            if (!parameter.containsKey("dir") && !parameter.containsKey("directory")) {
                throw new Exception("ERROR: Directory information is missing! You can use as follows: D:/workspace/springTest.");
            }
            if (!parameter.containsKey("type")) {
                parameter.put("type", Type.module.toString());
            }
            if (Type.contain(parameter.get("type"))) {
                throw new Exception("ERROR: Type information is error! You can use only project/module.");
            }
            if (!parameter.containsKey("format")) {
                parameter.put("format", "");
            }
        } else if (Operation.valueOf(operation) == Operation.exportDB) {
            if (!parameter.containsKey("artifact")) {
                throw new Exception("ERROR: Artifact information is missing! You can use as follows: log4j, spring-test.");
            }
            if (!parameter.containsKey("depend")) {
                parameter.put("depend", "all");
            }
            dependTowards = parameter.get("depend");
            if (!DependTowards.contain(dependTowards)) {
                throw new Exception("ERROR: Depend information is missing! You can only use all/above/below.");
            }
        }
    }

    private static void buildGraph(HashMap<String, String> parameter) throws Exception {
        url = parameter.get("url");
        username = parameter.get("user");
        password = parameter.get("password");
        build = new GraphBuilder(new Neo4JConn(url, username, password), format);
    }

    private static List<Project> getProjectList(List<Path> paths) throws SAXException, ParserConfigurationException, FileNotFoundException, JAXBException {
        List<Project> projectList = new ArrayList<>();
        XMLConverter converter = new XMLConverter();
        for (Path path : paths) {
            Project project = converter.convertFromXML(path);
            projectList.add(project);
        }
        return projectList;
    }

    private static List<Path> getPaths(String root) {
        PomFileFinder pomFileFinder = new PomFileFinder(root);
        List<Path> paths = pomFileFinder.getAllPOMs();
        System.out.println(paths);
        return paths;
    }
}
