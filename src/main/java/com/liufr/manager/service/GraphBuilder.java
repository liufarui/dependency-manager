package com.liufr.manager.service;

import com.liufr.manager.model.Dependency;
import com.liufr.manager.model.IEnum;
import com.liufr.manager.model.Project;

import java.util.List;

/**
 * @author lfr
 * @date 2020/11/6
 */
public interface GraphBuilder {
    Boolean GCNeo4JHandlerImpl();

    Boolean connect() throws Exception;

    Boolean cleanup() throws Exception;

    List<Dependency> getDependency(String artifactId, IEnum.Towards towards) throws Exception;

    void buildProj(List<Project> projList) throws Exception;

    void buildRepoGraph(Project proj) throws Exception;

    void buildProjRepoGraph(List<Project> projList) throws Exception;
}
