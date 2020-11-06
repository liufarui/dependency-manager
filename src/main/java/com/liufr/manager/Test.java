package com.liufr.manager;

import com.liufr.manager.impl.INeo4JUtil;
import com.liufr.manager.model.Neo4jConn;

import java.nio.file.Path;
import java.util.List;

/**
 * @author lfr
 * @date 2020/11/6
 */
public class Test {
    @org.junit.Test
    public void test() {
        Finder finder = new Finder("pom.xml");

        PomFileFinder pomFileFinder = new PomFileFinder("D:\\workspace\\so");
        List<Path> paths = pomFileFinder.getAllPOMs();
        System.out.println(paths);

        Neo4jConn conn = new Neo4jConn("bolt://localhost:7687", "neo4j", "123456");
        INeo4JUtil neo4jUtil = new Neo4JUtil(conn);
//        neo4jUtil.cleanDB(conn);

        System.out.println(neo4jUtil.getArtifact("eclp-po-api"));
        neo4jUtil.addRelationship("233", "234", "hahahah");
    }
}
