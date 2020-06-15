package net.vivatcreative.review.handlers;

import net.vivatcreative.core.database.MySQLDatabase;
import net.vivatcreative.core.exceptions.WorldNotFoundException;
import net.vivatcreative.core.messages.Message;
import net.vivatcreative.core.utils.*;
import net.vivatcreative.review.api.ReviewMessage;
import net.vivatcreative.review.json.GhostReview;
import net.vivatcreative.review.managers.StaffMembersManager;
import net.vivatcreative.review.utils.QueryUtil;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.sql.SQLException;

public class ReviewInfoHandler {

    private static final String ALL_INFO = "SELECT * FROM vivat_review_tickets WHERE `id`='%s' ORDER BY `submission_date` DESC LIMIT 1";

    public static void handle(Player p, int ticketId){
        MySQLDatabase.query(ALL_INFO, (rs) -> {
            try {
                if (!rs.next()) {
                    Message.send(p, ReviewMessage.PLOT_NOT_REVIEWED);
                    return;
                }
                OfflinePlayer target = PlayerUtil.getOfflinePlayer(rs.getString("player"));
                State state = State.valueOf(rs.getString("state"));
                String staff;
                switch (state){
                    case ACCEPTED:
                    case DENIED:
                        staff = StaffMembersManager.getFormattedStaff(rs.getString("staff"));
                        break;
                    case GHOSTED:
                        staff = StaffMembersManager.getFormattedStaff(JsonUtil.fromJSON(rs.getString("ghost"), GhostReview.class).uuid);
                        break;
                    default:
                        staff = "&7None";
                }
                Message.send(p, ReviewMessage.REVIEW_INFO, "%ticket_id%", String.valueOf(ticketId), //
                        "%rank%", QueryUtil.getPlayerBuildRank(target).getName(true), //
                        "%reviewee_name%", target.getName(), //
                        "%date%", rs.getString("submission_date"), //
                        "%world%", VivatWorld.fromString(rs.getString("world")).coloured, //
                        "%plot_id%", rs.getString("plot"),
                        "%state%", TextUtil.toColor(state.toColor()),
                        "%staff%", TextUtil.toColor(staff));
            } catch (SQLException | WorldNotFoundException e) {
                Logger.exception(e);
            }
        }, String.valueOf(ticketId));
    }
}
