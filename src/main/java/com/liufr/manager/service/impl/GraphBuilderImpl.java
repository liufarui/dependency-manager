package com.liufr.manager.service.impl;

import com.liufr.manager.model.Dependency;
import com.liufr.manager.model.Neo4jConn;
import com.liufr.manager.model.Project;
import com.liufr.manager.service.GraphBuilder;
import com.liufr.manager.service.Neo4JHandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @author lfr
 * @date 2020/11/7 14:13
 */
public class GraphBuilderImpl implements GraphBuilder {
    Neo4JHandler handler;
    /*
    * fix[0] is prefix
    * fix[1] is infix
    * fix[2] is suffix
    * fix[3] is whole string
    * */
    String[] fix = new String[4];

    public GraphBuilderImpl(Neo4jConn conn, String fix) throws Exception {
        this.handler = new Neo4JHandlerImpl(conn);
        if (fix == null || fix.isEmpty()) {
            return;
        }
        String[] temp = fix.split("\\*");
        System.out.println(temp.length);
        List<String> fixes = new ArrayList<>();
        for (String s : temp) {
            if(s!=null && !s.isEmpty()) {
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

    public GraphBuilderImpl(Neo4jConn conn) {
        this.handler = new Neo4JHandlerImpl(conn);
    }

    public Boolean connect() throws Exception {
        boolean isNeoAvailable = handler.isNeoAvailable();
        if (isNeoAvailable) {
            System.out.println("Successful! Connected to Neo4J!");
            return true;
        }

        System.out.println("ERROR! Failed to connect to Neo4J!");
        return false;
    }

    public Boolean cleanup() throws Exception {
        if (handler.cleanDB()) {
            System.out.println("Success to clear DB!");
            return true;
        } else {
            System.out.println("ERROR! The Neo4J is not available!");
            return false;
        }
    }

    public void buildRepoGraph(Project proj) throws Exception {
        if (!handler.isNeoAvailable()) {
            System.out.println("ERROR! The Neo4J is not available!");
            return;
        }
        String artifactId = handler.getArtifact(proj.getArtifactId());
        String projId = handler.createNode("Project", proj.getGroupId(), proj.getArtifactId(), proj.getVersion());
        if (!projId.isEmpty()) {
            System.out.println("The project has been Depended!");
            handler.addRelationship(artifactId, projId, "is");
        }

        if (null != proj.getDeps()) {
            for (Dependency dep : proj.getDeps()) {
                /* If Artifact exited, return */
                if (dep.isFormat(this.fix)) {
                    handler.addRelationship(projId,
                            handler.createNode("Artifact", dep.getGroupId(), dep.getArtifactId(), dep.getVersion()),
                            "depend");
                }
            }
        }
    }

    public void buildProjRepoGraph(List<Project> projList) throws Exception {
        if (!handler.isNeoAvailable()) {
            System.out.println("ERROR! The Neo4J is not available!");
            return;
        }
        HashMap<String, String> projGraph = new HashMap<>();

        for (Project proj : projList) {
            String id = handler.createNode("Project", proj.getGroupId(), proj.getArtifactId(), proj.getVersion());
            projGraph.put(proj.getArtifactId(), id);
        }
        for (Project proj : projList) {
            if (null != proj.getDeps()) {
                for (Dependency dep : proj.getDeps()) {
                    if (projGraph.containsKey(dep.getArtifactId())) {
                        handler.addRelationship(handler.getProject(proj.getArtifactId()), projGraph.get(dep.getArtifactId()), "depend");
                    }
                }
            }
        }
    }
}
