package com.liufr.manager.model;

import javax.xml.bind.annotation.XmlRootElement;

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
        if(this.groupId == null) {
            return true;
        }
        return this.groupId.startsWith("org") || this.groupId.startsWith("spring");
    }

    public Boolean isJD() {
        if(this.groupId == null) {
            return true;
        }
        return this.groupId.startsWith("com.jd");
    }

    @Override
    public String toString() {
        return "Dependency{" +
                "artifactId='" + artifactId + '\'' +
                ", groupId='" + groupId + '\'' +
                ", version='" + version + '\'' +
                '}';
    }
}
