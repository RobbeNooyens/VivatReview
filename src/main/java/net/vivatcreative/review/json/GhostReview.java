package net.vivatcreative.review.json;

import net.vivatcreative.core.utils.MathUtil;
import net.vivatcreative.core.utils.TextUtil;
import org.bukkit.OfflinePlayer;

public class GhostReview {
    public String uuid;
    public double skill,creativity,quality,composition;

    public GhostReview(OfflinePlayer staff, String scqc){
        String[] parts = scqc.split("-");
        if(parts.length >= 4) {
            skill = MathUtil.round(Double.parseDouble(parts[0]), 1);
            creativity = MathUtil.round(Double.parseDouble(parts[1]), 1);
            quality = MathUtil.round(Double.parseDouble(parts[2]), 1);
            composition = MathUtil.round(Double.parseDouble(parts[3]), 1);
        }
        uuid = staff.getUniqueId().toString();
    }

    public String format(String stringToFormat){
        return TextUtil.replacePlaceholders(stringToFormat, "%skill%", skill,
                "%creativity%", creativity, "%quality%", quality,
                "%composition%", composition);
    }
}
