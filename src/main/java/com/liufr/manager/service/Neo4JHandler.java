package com.liufr.manager.service;

/**
 * @author lfr
 * @date 2020/11/6 23:44
 */
public interface Neo4JHandler {

    boolean isNeoAvailable();

    void cleanDB();

    String getArtifact(String artifactId);

    String getProject(String artifactId);

    String createNode(String type, String groupId, String artifactId, String version);

    void addRelationship(String start, String end, String relation);
}
