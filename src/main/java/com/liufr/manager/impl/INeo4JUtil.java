package com.liufr.manager.impl;

import com.liufr.manager.model.Neo4jConn;

/**
 * @author lfr
 * @date 2020/11/6 23:44
 */
public interface INeo4JUtil {

    boolean isNeoAvailable();

    void cleanDB();

    String getArtifact(String artifactId);

    String createNode(String type, String groupId, String artifactId, String version);

    void addRelationship(String start, String end, String relation);
}
