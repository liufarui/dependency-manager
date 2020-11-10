package com.liufr.manager.service;

import com.liufr.manager.model.Dependency;
import com.liufr.manager.model.IEnum;

import java.util.List;

/**
 * @author lfr
 * @date 2020/11/6 23:44
 */
public interface Neo4JHandler {

    Boolean GCNeo4JHandlerImpl();

    Boolean isNeoAvailable();

    Boolean cleanDB();

    String getArtifact(String artifactId);

    String getProject(String artifactId);

    List<Dependency> getDependency(String artifactId, IEnum.Towards towards) throws Exception;

    String createNode(String type, String groupId, String artifactId, String version);

    String getRelationship(String start, String end);

    void addRelationship(String start, String end, String relation);
}