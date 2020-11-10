package com.liufr.manager.service;

import com.liufr.manager.model.Dependency;
import com.liufr.manager.model.IEnum;
import com.liufr.manager.model.Neo4jConn;
import org.neo4j.driver.*;

import java.util.ArrayList;
import java.util.List;

/**
 * @author lfr
 * @date 2020/11/6 23:46
 */
public class Neo4JHandler {
    private final Session session;
    private final Driver driver;

    public Neo4JHandler(Neo4jConn conn) {
        driver = GraphDatabase.driver(conn.getServerURL(), AuthTokens.basic(conn.getUserName(), conn.getPassword()));
        session = driver.session();
    }

    public Boolean closeSession() {
        try {
            if (session != null) {
                session.close();
            }
            if (driver != null) {
                driver.close();
            }
            return true;
        } catch (Exception e) {
            System.out.println("Session failed to close");
            return false;
        }
    }

    public Boolean isNeoAvailable() {
        session.run("MATCH (n) RETURN n LIMIT 5");
        return true;
    }

    public Boolean cleanDB() {
        try {
            String clearCommand = "MATCH (n) OPTIONAL MATCH (n)-[r]-() DELETE n,r";
            getRecord(clearCommand);
            return true;
        } catch (Exception e) {
            return false;
        }

    }

    public String getProject(String artifactId) {
        String baseCmd = "MATCH (a:Project) WHERE a.artifactId = \"%s\" RETURN id(a) AS id";
        String getArtifactCommand = String.format(baseCmd, artifactId);

        Record record = getRecord(getArtifactCommand);
        return (record == null) ? null : record.get("id").toString();
    }

    public List<Dependency> getDependency(String artifactId, IEnum.Towards towards) throws Exception {
        String baseCmd;
        if (IEnum.Towards.below == towards) {
            baseCmd = "MATCH (a)-[r:depend]->(x) WHERE a.artifactId=\"%s\" RETURN x.artifactId as artifactId, x.groupId as groupId, x.version as version";
        } else if (IEnum.Towards.above == towards) {
            baseCmd = "MATCH (x)-[r:depend]->(a) WHERE a.artifactId=\"%s\" RETURN x.artifactId as artifactId, x.groupId as groupId, x.version as version";
        } else {
            throw new Exception();
        }
        String getArtifactCommand = String.format(baseCmd, artifactId);

        List<Record> records = getRecords(getArtifactCommand);
        return Dependency.convertFromRecords(records);
    }

    public String getArtifact(String artifactId) {
        String baseCmd = "MATCH (a:Artifact) WHERE a.artifactId = \"%s\" RETURN id(a) AS id";
        String getArtifactCommand = String.format(baseCmd, artifactId);

        Record record = getRecord(getArtifactCommand);
        return (record == null) ? null : record.get("id").toString();
    }

    public String createNode(String type, String groupId, String artifactId, String version) {
        String id = getArtifact(artifactId);
        if ("Artifact".equals(type) && id != null) {
            return id;
        }

        String baseCmd = "CREATE (a:%s {groupId: \"%s\", artifactId: \"%s\", version: \"%s\"}) RETURN id(a) AS id";
        String createNodeCmd = String.format(baseCmd, type, groupId, artifactId, version);

        Record record = getRecord(createNodeCmd);
        return (record == null) ? "" : record.get("id").toString();
    }

    public String getRelationship(String start, String end) {
        String baseCmd = "MATCH (a)-[r]->(b) WHERE ID(a)=%s AND ID(b)=%s RETURN type(r) AS type";
        String addRelationshipCommand = String.format(baseCmd, start, end);
        Record record = getRecord(addRelationshipCommand);
        return (record == null) ? "" : record.get("type").toString().replace("\"", "");
    }

    public void addRelationship(String start, String end, String relation) {
        String oldRelation = getRelationship(start, end);
        if (!oldRelation.isEmpty() && relation.equals(oldRelation)) {
            return;
        }
        String baseCmd = "MATCH (a),(b) WHERE ID(a)=%s AND ID(b)=%s CREATE (a)-[r:%s]->(b)";
        String addRelationshipCommand = String.format(baseCmd, start, end, relation);
        getRecord(addRelationshipCommand);
    }

    private Record getRecord(String command) {
        List<Record> records = getRecords(command);
        return records.size() == 0 ? null : records.get(0);
    }

    private List<Record> getRecords(String command) {
        List<Record> records = new ArrayList<>();
        Result result = session.run(command);
        while (result.hasNext()) {
            records.add(result.next());
        }
        return records;
    }
}
