package net.vivatcreative.review.handlers;

import com.intellectualcrafters.plot.object.Plot;
import net.vivatcreative.core.database.MySQLDatabase;
import net.vivatcreative.core.exceptions.WorldNotFoundException;
import net.vivatcreative.core.messages.Message;
import net.vivatcreative.core.permissions.CorePermission;
import net.vivatcreative.core.utils.DateUtil;
import net.vivatcreative.core.utils.Logger;
import net.vivatcreative.core.utils.VivatWorld;
import net.vivatcreative.review.api.ReviewMessage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.text.ParseException;
import java.util.Date;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class SubmissionHandler {

    private final Plot plot;
    private final Player p;
    private static final String updateQuery = "INSERT INTO `vivat_review_tickets`(`player`,`plot`,`world`,`submission_date`) VALUES ('%s','%s','%s','%s')",
    PENDING_TICKETS = "SELECT COUNT(*) FROM vivat_review_tickets WHERE `player` = '%s' AND `state` = 'SUBMITTED' OR `state` = 'GHOSTED'",
    ACCEPTED_TICKETS_COUNT = "SELECT COUNT(*) FROM vivat_review_tickets WHERE `world` = '%s' AND `plot` = '%s' AND `state` = 'ACCEPTED'",
    TIME_PASSED = "SELECT `review_date` FROM vivat_review_tickets WHERE `world` = '%s' AND `plot` = '%s' ORDER BY `review_date` DESC LIMIT 1";

    public SubmissionHandler(Player player, Plot plot){
        this.p = Objects.requireNonNull(player);
        this.plot = Objects.requireNonNull(plot);
    }

    public void handle(){
        Set<UUID> owners = plot.getOwners();
        String player = owners.contains(p.getUniqueId()) ? p.getUniqueId().toString() : owners.iterator().next().toString();
        MySQLDatabase.update(updateQuery, player, plot.getId().toString(), plot.getWorldName(), DateUtil.YYYY_MM_DD_HH_MM_SS.now());
        Message.send(p, ReviewMessage.PLOT_SUBMITTED);
        Bukkit.getOnlinePlayers().forEach(this::notify);
    }

    public boolean possibleToSubmit(boolean message) {
        try {
            if (!VivatWorld.fromString(plot.getWorldName()).isRanked) return false;
        } catch (WorldNotFoundException e) {
            return false;
        }
        if (!plot.getOwners().contains(p.getUniqueId()) && !CorePermission.VIVAT_STAFF.has(p)) return message && !Message.send(p, ReviewMessage.NOT_PLOTOWNER);
        if (anyPending(p)) return message && !Message.send(p, ReviewMessage.SUBMIT_ALREADY_HAS_SUBMISSION);
        if(reachedLimit()) return message && !Message.send(p, ReviewMessage.REACHED_SUBMISSION_LIMIT);
        if(!enoughTimePassed()) return false;
        return true;
    }

    private boolean anyPending(Player p){
        final boolean[] pending = {true};
        MySQLDatabase.query(PENDING_TICKETS, resultSet -> {
            try {
                if(!resultSet.next()) return;
                pending[0] = (resultSet.getInt(1) != 0);
            } catch (SQLException e) {
                Logger.exception(e);
            }
        }, p.getUniqueId().toString());
        return pending[0];
    }

    private void notify(Player player) {
        if(CorePermission.VIVAT_STAFF.has(player))
            Message.send(player, ReviewMessage.NEW_SUBMIT_NOTIFY, "%player%", p.getName());
    }

    private boolean reachedLimit(){
        boolean[] reached = new boolean[1];
        MySQLDatabase.query(ACCEPTED_TICKETS_COUNT, resultSet -> {
            try {
                if(!resultSet.next()) reached[0] = false;
                else {
                    int amount = resultSet.getInt(1);
                    reached[0] = amount >= 5;
                }
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }, plot.getWorldName(), plot.getId().toString());
        return reached[0];
    }

    private boolean enoughTimePassed(){
        boolean[] reached = new boolean[1];
        MySQLDatabase.query(TIME_PASSED, resultSet -> {
            try {
                if(!resultSet.next()) reached[0] = true;
                else {
                    Date amount = DateUtil.YYYY_MM_DD_HH_MM_SS.fromString(resultSet.getString("review_date"));
                    long length = (new Date()).getTime() - amount.getTime();
                    reached[0] = TimeUnit.MILLISECONDS.toHours(length) >= 4;
                    if(!reached[0]) {
                        int hours = 3 - (int) TimeUnit.MILLISECONDS.toHours(length);
                        int minutes = 59 - ((int) TimeUnit.MILLISECONDS.toMinutes(length) % 60);
                        Message.send(p, ReviewMessage.SUBMIT_COUNTDOWN, "%hours%", String.valueOf(hours), "%minutes%", String.valueOf(minutes));
                    }
                }
            } catch (SQLException | ParseException throwables) {
                throwables.printStackTrace();
            }
        }, plot.getWorldName(), plot.getId().toString());
        return reached[0];
    }
}
