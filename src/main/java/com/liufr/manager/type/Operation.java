package com.liufr.manager.type;

/**
 * @author lfr
 * @date 2020/11/10 14:07
 */
public enum Operation {
    exportDB,
    importDB;

    public static Boolean contain(String operation) {
        return operation.equals(Operation.exportDB.toString()) || operation.equals(Operation.importDB.toString());
    }
}
