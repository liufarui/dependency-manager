package com.liufr.manager.model;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;

/**
 * @author lfr
 * @date 2020/11/6
 */
@XmlRootElement()
public class Project {
    @XmlElementWrapper(name = "dependencies")
    @XmlElement(name = "dependency")
    private ArrayList<Dependency> depList;
    private String artifactId;
    private String groupId;
    private String version;

    public ArrayList<Dependency> getDeps() {
        return depList;
    }

    public void setDepList(ArrayList<Dependency> depList) {
        this.depList = depList;
    }

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

    @Override
    public String toString() {
        int artifactCount = (depList == null) ? 0 : depList.size();
        return "Project{" +
                ", artifactId='" + artifactId + '\'' +
                ", groupId='" + groupId + '\'' +
                ", version='" + version + '\'' +
                "artifactList count=" + artifactCount +
                '}';
    }
}
