package com.liufr.manager.model;

import org.neo4j.driver.Record;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

/**
 * @author lfr
 * @date 2020/11/6
 */
@XmlRootElement()
public class Dependency {
    private String artifactId;
    private String groupId;
    private String version;

    public String getArtifactId() {
        return artifactId;
    }

    public void setArtifactId(String artifactId) {
        this.artifactId = artifactId;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public Boolean isOpen() {
        if (this.groupId == null) {
            return true;
        }
        return this.groupId.startsWith("org") || this.groupId.startsWith("spring");
    }

    public Boolean isFormat(String[] fix) {
        if (this.artifactId == null) {
            return true;
        }
        if (fix[0] != null && !fix[0].isEmpty() && !this.artifactId.startsWith(fix[0])) {
            return false;
        }
        if (fix[1] != null && !fix[1].isEmpty() && !this.artifactId.contains(fix[1])) {
            return false;
        }
        if (fix[2] != null && !fix[2].isEmpty() && !this.artifactId.endsWith(fix[2])) {
            return false;
        }
        if (fix[3] != null && !fix[3].isEmpty() && !this.artifactId.equals(fix[3])) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "Dependency{" +
                "artifactId='" + artifactId + '\'' +
                ", groupId='" + groupId + '\'' +
                ", version='" + version + '\'' +
                '}';
    }

    public static List<Dependency> convertFromRecords(List<Record> records) {
        List<Dependency> deps = new ArrayList<>();
        for (Record record : records) {
            Dependency dep = new Dependency();
            dep.setArtifactId(record.get("artifactId").asString());
            dep.setGroupId(record.get("groupId").asString());
            dep.setVersion(record.get("version").asString());
            deps.add(dep);
        }

        return deps;
    }
}
