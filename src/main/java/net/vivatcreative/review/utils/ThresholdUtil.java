package net.vivatcreative.review.utils;

import net.vivatcreative.core.utils.VivatWorld;

public class ThresholdUtil {

    public static double getThresholdFromworld(VivatWorld world){
        switch (world){
            case BRONZE:
                return 4;
            case SILVER:
                return 4.5;
            case GOLD:
                return 5;
            case DIAMOND:
                return 5.5;
            case EMERALD:
                return 6;
            case MASTER:
                return 6.5;
            default:
                return 10;
        }
    }

}
