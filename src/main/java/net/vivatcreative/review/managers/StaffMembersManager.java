package net.vivatcreative.review.managers;

import net.vivatcreative.core.utils.TextUtil;

import java.util.HashMap;
import java.util.Map;

public class StaffMembersManager {
    public final Map<String, String> staff = new HashMap<>();
    private static final StaffMembersManager INSTANCE = new StaffMembersManager();

    private StaffMembersManager(){}

    static {
        INSTANCE.staff.put("c7a48d91-709d-4e60-9ba4-d4c37447c0ce", "&cRobnoo02");
        INSTANCE.staff.put("10d634f2-8a87-4508-b013-610306707adf", "&cMirass");
        INSTANCE.staff.put("a09cd4de-0f59-4417-b777-69826a911f2f", "&cgmaine");
        INSTANCE.staff.put("1349c99d-07f0-45d3-9fd6-3c3eb8bfb9fd", "&2Spzrk");
        INSTANCE.staff.put("4beb9306-7482-4a39-ad37-4ef156f4c0b0", "&8Iress");
    }

    public static String getFormattedStaff(String uuid){
        return TextUtil.toColor(INSTANCE.staff.get(uuid));
    }
}
