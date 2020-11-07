package com.liufr.manager.service.impl;

import com.liufr.manager.model.Dependency;
import com.liufr.manager.model.Neo4jConn;
import com.liufr.manager.model.Project;
import com.liufr.manager.service.GraphBuilder;
import com.liufr.manager.service.Neo4JHandler;

/**
 * @author lfr
 * @date 2020/11/7 14:13
 */
public class GraphBuilderImpl implements GraphBuilder {
    Neo4JHandler handler;

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
                if (dep.isJD()) {
                    handler.addRelationship(projId,
                            handler.createNode("Artifact", dep.getGroupId(), dep.getArtifactId(), dep.getVersion()),
                            "depend");
                }
            }
        }
    }
}
