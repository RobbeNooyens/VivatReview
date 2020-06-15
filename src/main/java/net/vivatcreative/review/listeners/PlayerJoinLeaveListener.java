package net.vivatcreative.review.listeners;

import net.vivatcreative.core.database.MySQLDatabase;
import net.vivatcreative.core.messages.Message;
import net.vivatcreative.core.players.Users;
import net.vivatcreative.core.players.VivatPlayer;
import net.vivatcreative.core.utils.Logger;
import net.vivatcreative.review.VivatReview;
import net.vivatcreative.review.api.ReviewMessage;
import net.vivatcreative.review.api.ReviewPermission;
import net.vivatcreative.review.managers.StaffMembersManager;
import net.vivatcreative.review.managers.SubmissionManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.sql.SQLException;

public class PlayerJoinLeaveListener implements Listener {

    private static final String LATEST_TICKET_ID = "SELECT `staff` FROM vivat_review_tickets WHERE player = '%s' ORDER BY review_date DESC LIMIT 1",
    UNREVIEWED_TICKETS_COUNT = "SELECT COUNT(*) FROM vivat_review_tickets WHERE `state` = 'SUBMITTED' OR `state` = 'GHOSTED'";

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onJoin(PlayerJoinEvent event) {
        Player p = event.getPlayer();
        Bukkit.getScheduler().scheduleSyncDelayedTask(VivatReview.get(), () -> { checkForNewReview(p); }, 60L);
        if(ReviewPermission.LIST.has(p))
            Bukkit.getScheduler().scheduleSyncDelayedTask(VivatReview.get(), () -> { checkReviewStaffNotification(p); }, 40L);
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent e) {
        SubmissionManager.removeSubmitQueue(e.getPlayer());
    }

    private void checkForNewReview(Player p){
        if (!p.isOnline()) return;
        VivatPlayer player = Users.get(p);
        if(!player.getReviewAvailable()) return;
        if(player.getRankupAvailable()) {
            Message.send(p, ReviewMessage.PLAYER_RANKED_UP_OFFLINE, "%rank%", player.getBuildRank().getName(true));
            player.setRankupAvailable(false);
        }
        final String[] staff = new String[1];
        MySQLDatabase.query(LATEST_TICKET_ID, (resultSet) -> {
            try {
                if(!resultSet.next()) return;
                staff[0] = resultSet.getString("staff");
            } catch (SQLException e) {
                Logger.exception(e);
            }
        }, p.getUniqueId().toString());
        Message.send(p, ReviewMessage.OFFLINE_REVIEWED, "%staff%", StaffMembersManager.getFormattedStaff(staff[0]));
        player.setReviewAvailable(false);
    }

    private void checkReviewStaffNotification(Player p){
        final int[] size = new int[1];
        MySQLDatabase.query(UNREVIEWED_TICKETS_COUNT, resultSet -> {
            try {
                if(resultSet.next())
                    size[0] = resultSet.getInt(1);
            } catch (SQLException e) {
                Logger.exception(e);
            }
        });
        if (size[0] > 0) Message.send(p, ReviewMessage.NEW_SUBMIT_WHILE_OFFLINE, "%amount%", String.valueOf(size[0]));
    }
}
