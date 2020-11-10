package com.liufr.manager.model;

/**
 * @author lfr
 * @date 2020/11/9 14:54
 */
public class IEnum {
    public enum Towards{
        all,
        above,
        below;
    }

    public static boolean towardsAbove(String check) {
        return check.equals(Towards.above.toString()) || check.equals(Towards.all.toString());
    }
    public static boolean towardsBelow(String check) {
        return check.equals(Towards.below.toString()) || check.equals(Towards.all.toString());
    }
}
