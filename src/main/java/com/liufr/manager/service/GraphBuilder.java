package com.liufr.manager.service;

import com.liufr.manager.model.Dependency;
import com.liufr.manager.type.DependTowards;
import com.liufr.manager.util.Neo4JConn;
import com.liufr.manager.model.Project;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @author lfr
 * @date 2020/11/7 14:13
 */
public class GraphBuilder {
    Neo4JEngine neo4JEngine;
    /*
     * fix[0] is prefix
     * fix[1] is infix
     * fix[2] is suffix
     * fix[3] is whole string
     * */
    String[] fix = new String[4];

    public GraphBuilder(Neo4JConn conn, String fix) throws Exception {
        this.neo4JEngine = new Neo4JEngine(conn);
        if (fix == null || fix.isEmpty()) {
            return;
        }
        String[] temp = fix.split("\\*");
        List<String> fixes = new ArrayList<>();
        for (String s : temp) {
            if (s != null && !s.isEmpty()) {
                fixes.add(s);
            }
        }
        switch (fixes.size()) {
            case 0:
                break;
            case 1:
                if (fix.startsWith("*") && fix.endsWith("*")) {
                    /*    *abc*     */
                    this.fix[1] = fixes.get(0);
                } else if (fix.startsWith("*")) {
                    /*    *abc     */
                    this.fix[2] = fixes.get(0);
                } else if (fix.endsWith("*")) {
                    /*    abc*     */
                    this.fix[0] = fixes.get(0);
                } else {
                    /*    abc     */
                    this.fix[3] = fixes.get(0);
                }
                break;
            case 2:
                if (fix.startsWith("*")) {
                    /*    *abc*def     */
                    this.fix[1] = fixes.get(0);
                    this.fix[2] = fixes.get(1);
                } else if (fix.endsWith("*")) {
                    /*    abc*def*     */
                    this.fix[0] = fixes.get(0);
                    this.fix[1] = fixes.get(1);
                } else {
                    System.out.println("ERROR! Format contains too many *!");
                    throw new Exception("Format contains too many *!");
                }
                break;
            case 3:
                if (!fix.startsWith("*") && !fix.endsWith("*")) {
                    /*    abc*def*hij     */
                    this.fix[0] = fixes.get(0);
                    this.fix[1] = fixes.get(1);
                    this.fix[2] = fixes.get(2);
                } else {
                    System.out.println("ERROR! Format contains too many *!");
                    throw new Exception("Format contains too many *!");
                }
                break;
            default:
                System.out.println("ERROR! Format contains too many *!");
                throw new Exception("Format contains too many *!");
        }
    }

    public GraphBuilder(Neo4JConn conn) {
        this.neo4JEngine = new Neo4JEngine(conn);
    }

    public Boolean GCNeo4JHandler() {
        return this.neo4JEngine.closeSession();
    }

    public Boolean connect() throws Exception {
        boolean isNeoAvailable = neo4JEngine.isNeoAvailable();
        if (isNeoAvailable) {
            System.out.println("Successful! Connected to Neo4J!");
            return true;
        }

        return false;
    }

    public Boolean cleanup() throws Exception {
        if (neo4JEngine.cleanDB()) {
            System.out.println("Success to clear DB!");
            return true;
        } else {
            System.out.println("ERROR! The Neo4J is not available!");
            return false;
        }
    }

    public List<Dependency> getDependency(String artifactId, DependTowards dependTowards) throws Exception {
        if (!neo4JEngine.isNeoAvailable()) {
            System.out.println("ERROR! The Neo4J is not available!");
            return null;
        }
        return neo4JEngine.getDependency(artifactId, dependTowards);
    }

    public void buildProject(List<Project> projectList) throws Exception {
        for (Project project : projectList) {
            neo4JEngine.createNode("Project", project.getGroupId(), project.getArtifactId(), project.getVersion());
        }
    }

    public void buildRepoGraph(Project project) throws Exception {
        if (!neo4JEngine.isNeoAvailable()) {
            System.out.println("ERROR! The Neo4J is not available!");
            return;
        }
        String projectId = neo4JEngine.getProject(project.getArtifactId());

        if (null != project.getDeps()) {
            for (Dependency dep : project.getDeps()) {
                /* If Artifact exited, return */
                if (dep.isFormat(this.fix)) {
                    String artifactId = neo4JEngine.createNode("Artifact", dep.getGroupId(), dep.getArtifactId(), dep.getVersion());
                    neo4JEngine.addRelationship(projectId, artifactId, "depend");
                    String artifactProjectId = neo4JEngine.getProject(dep.getArtifactId());
                    if (artifactProjectId != null && !artifactProjectId.isEmpty()) {
                        neo4JEngine.addRelationship(artifactId, artifactProjectId, "is");
                    }
                }
            }
        }
    }

    public void buildProjectRepoGraph(List<Project> projectList) throws Exception {
        if (!neo4JEngine.isNeoAvailable()) {
            System.out.println("ERROR! The Neo4J is not available!");
            return;
        }
        HashMap<String, String> projectGraph = new HashMap<>();

        for (Project project : projectList) {
            String id = neo4JEngine.createNode("Project", project.getGroupId(), project.getArtifactId(), project.getVersion());
            projectGraph.put(project.getArtifactId(), id);
        }
        for (Project project : projectList) {
            if (null != project.getDeps()) {
                for (Dependency dep : project.getDeps()) {
                    if (projectGraph.containsKey(dep.getArtifactId())) {
                        neo4JEngine.addRelationship(neo4JEngine.getProject(project.getArtifactId()), projectGraph.get(dep.getArtifactId()), "depend");
                    }
                }
            }
        }
    }
}
