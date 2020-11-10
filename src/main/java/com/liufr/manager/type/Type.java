package com.liufr.manager.type;

/**
 * @author lfr
 * @date 2020/11/10 14:06
 */
public enum Type {
    module,
    project;

    public static Boolean contain(String type) {
        return type.equals(Type.module.toString()) || type.equals(Type.project.toString());
    }
}
