package com.liufr.manager;

import com.liufr.manager.impl.INeo4JUtil;
import com.liufr.manager.model.Neo4jConn;
import org.neo4j.driver.*;

/**
 * @author lfr
 * @date 2020/11/6 23:46
 */
public class Neo4JUtil implements INeo4JUtil {
    private Neo4jConn conn;

    public Neo4JUtil(Neo4jConn conn) {
        this.conn = conn;
    }

    @Override
    public boolean isNeoAvailable() {
        try (Driver driver = GraphDatabase.driver(conn.getServerURL(), AuthTokens.basic(conn.getUserName(), conn.getUserName()));) {
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public void cleanDB() {
        String clearCommand = "MATCH (n) OPTIONAL MATCH (n)-[r]-() DELETE n,r";
        execute(clearCommand);
    }

    @Override
    public String getArtifact(String artifactId) {
        String baseCmd = "MATCH (a) WHERE a.artifactId = \"%s\" RETURN id(a) AS id";
        String getArtifactCommand = String.format(baseCmd, artifactId);

        Record record = execute(getArtifactCommand);
        return (record == null) ? null : record.get("id").toString();
    }

    @Override
    public String createNode(String type, String groupId, String artifactId, String version) {
        String id = getArtifact(artifactId);
        if (id != null) {
            return id;
        }

        String baseCmd = "CREATE (a:%s {groupId: %s, artifactId: %s, version: %s})";
        String createNodeCmd = String.format(baseCmd, type, groupId, artifactId, version);

        Record record = execute(createNodeCmd);
        return (record == null) ? null : record.get("id").toString();
    }

    @Override
    public void addRelationship(String start, String end, String relation) {
        String baseCmd = "MATCH (a),(b) WHERE ID(a)=%s AND ID(b)=%s CREATE (a)-[r:%s]->(b)";
        String addRelationshipCommand = String.format(baseCmd, start, end, relation);
        execute(addRelationshipCommand);
    }

    private Record execute(String command) {
        try (Driver driver = GraphDatabase.driver(conn.getServerURL(), AuthTokens.basic(conn.getUserName(), conn.getPassword()));
             Session session = driver.session();) {
            Result result = session.run(command);

            Record record = null;
            while (result.hasNext()) {
                record = result.next();
                System.out.println(record.toString());
            }
            return record;
        } catch (Exception e) {
            System.out.println(e.toString());
            System.out.println("ERROR!");
        }
        return null;
    }
}
