package com.liufr.manager.model;

/**
 * @author lfr
 * @date 2020/11/6
 */
public class Neo4jConn {
    private String serverURL;
    private String userName;
    private String password;

    public Neo4jConn(String serverURL, String userName, String password) {
        this.serverURL = serverURL;
        this.userName = userName;
        this.password = password;
    }

    public String getServerURL() {
        return serverURL;
    }

    public void setServerURL(String serverURL) {
        this.serverURL = serverURL;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
