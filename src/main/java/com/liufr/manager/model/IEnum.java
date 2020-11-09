package com.liufr.manager.model;

/**
 * @author lfr
 * @date 2020/11/9 14:54
 */
public class IEnum {
    public enum Towards{
        above,
        below;
    }

    public static boolean towardsAbove(String check) {
        return check.equals(Towards.above.toString());
    }
    public static boolean towardsBelow(String check) {
        return check.equals(Towards.below.toString());
    }
}
