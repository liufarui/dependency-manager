package com.liufr.manager.type;

/**
 * @author lfr
 * @date 2020/11/10 14:03
 */
public enum DependTowards {
    all,
    above,
    below;

    public static boolean towardsAbove(String check) {
        return check.equals(DependTowards.above.toString()) || check.equals(DependTowards.all.toString());
    }

    public static boolean towardsBelow(String check) {
        return check.equals(DependTowards.below.toString()) || check.equals(DependTowards.all.toString());
    }

    public static Boolean contain(String dependTowards) {
        return dependTowards.equals(DependTowards.all.toString())
                || dependTowards.equals(DependTowards.above.toString())
                || dependTowards.equals(DependTowards.below.toString());
    }
}
