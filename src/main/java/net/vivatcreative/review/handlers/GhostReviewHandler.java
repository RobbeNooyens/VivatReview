package net.vivatcreative.review.handlers;


import net.vivatcreative.core.database.MySQLDatabase;
import net.vivatcreative.core.messages.Message;
import net.vivatcreative.core.utils.DateUtil;
import net.vivatcreative.core.utils.JsonUtil;
import net.vivatcreative.core.utils.Logger;
import net.vivatcreative.core.utils.PlayerUtil;
import net.vivatcreative.review.api.ReviewMessage;
import net.vivatcreative.review.json.GhostReview;
import net.vivatcreative.review.utils.QueryUtil;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.sql.SQLException;

public class GhostReviewHandler {

    public static boolean handle(final int ticketID, final OfflinePlayer staff, final String scqcString){
        if(QueryUtil.getPlotFromID(ticketID).getOwners().contains(staff.getUniqueId()))
            return !staff.isOnline() || Message.send((Player) staff, "cant_review_own_plot");
        final GhostReview ghostReview = new GhostReview(staff, scqcString);
        final String query = "UPDATE vivat_review_tickets SET `ghost` = '%s', `review_date` = '%s', `state` = 'GHOSTED' WHERE `id` = '%s'";
        MySQLDatabase.update(query, JsonUtil.toJSON(ghostReview), DateUtil.YYYY_MM_DD_HH_MM_SS.now(), ticketID);
        if(staff.isOnline()) {
            Message.send((Player) staff, ReviewMessage.STAFF_REVIEWED_PLOT);
            MySQLDatabase.query("SELECT `comment`, `player`, `ghost` FROM vivat_review_tickets WHERE `id`='%s'", resultSet -> {
                try {
                    if (!resultSet.next()) return;
                    CommentHandler.sendEdit((Player) staff, ticketID, resultSet.getString("comment"));
                    OfflinePlayer target = PlayerUtil.getOfflinePlayer(resultSet.getString("player"));
                    if(!target.isOnline()) return;
                    GhostReview review = JsonUtil.fromJSON(resultSet.getString("ghost"), GhostReview.class);
                    String reviewFormatted = review.format("%skill%-%creativity%-%quality%-%composition%");
                    Message.send((Player) target, ReviewMessage.ONLINE_GHOST_REVIEWED, "%staff_name%", staff.getName(), "%score%", reviewFormatted);
                } catch (SQLException e) {
                    Logger.exception(e);
                }
            }, ticketID);
        }
        return true;
    }
}
