package com.liufr.manager.service;

import com.liufr.manager.model.Project;

/**
 * @author lfr
 * @date 2020/11/6
 */
public interface GraphBuilder {
    Boolean connect() throws Exception;
    Boolean cleanup() throws Exception;
    void buildRepoGraph(Project proj) throws Exception;
}
