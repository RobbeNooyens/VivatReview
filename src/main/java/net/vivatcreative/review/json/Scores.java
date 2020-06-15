package net.vivatcreative.review.json;

import net.vivatcreative.core.utils.MathUtil;
import net.vivatcreative.core.utils.TextUtil;

public class Scores {
    public double skill,creativity,quality,composition,total;

    public Scores(){}
    public Scores(String scqc){
        String[] parts = scqc.split("-");
        if(parts.length >= 4) {
            skill = MathUtil.round(Double.parseDouble(parts[0]), 1);
            creativity = MathUtil.round(Double.parseDouble(parts[1]), 1);
            quality = MathUtil.round(Double.parseDouble(parts[2]), 1);
            composition = MathUtil.round(Double.parseDouble(parts[3]), 1);
            total = MathUtil.round(skill + creativity + quality + composition, 1);
        }
    }

    public String format(String stringToFormat, double threshold){
        return TextUtil.replacePlaceholders(stringToFormat, "%skill%", getColoured(skill, threshold),
                "%creativity%", getColoured(creativity, threshold), "%quality%", getColoured(quality, threshold),
                "%composition%", getColoured(composition, threshold));
    }

    private String getColoured(double val, double threshold){
        return val >= threshold ? "&a" + val : "&c" + val;
    }
}
