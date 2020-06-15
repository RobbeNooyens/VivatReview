package net.vivatcreative.review.handlers;

import com.intellectualcrafters.plot.object.Plot;
import net.vivatcreative.core.database.MySQLDatabase;
import net.vivatcreative.core.exceptions.WorldNotFoundException;
import net.vivatcreative.core.messages.CoreMessage;
import net.vivatcreative.core.messages.Message;
import net.vivatcreative.core.players.Users;
import net.vivatcreative.core.players.VivatPlayer;
import net.vivatcreative.core.utils.*;
import net.vivatcreative.review.api.ReviewMessage;
import net.vivatcreative.review.api.ReviewPermission;
import net.vivatcreative.review.guis.RecentGui;
import net.vivatcreative.review.json.Scores;
import net.vivatcreative.review.utils.ThresholdUtil;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.sql.SQLException;

public class ReviewHandler {

    private final int ticketID;
    private final OfflinePlayer staff;
    private OfflinePlayer target = null;
    private final String scqcString;
    private final String date;

    private Scores scores;
    private String comment;
    private Plot plot;

    public ReviewHandler(final int ticketID, final OfflinePlayer staff, final String scqc) {
        this.ticketID = ticketID;
        this.scqcString = TextUtil.removeColor(scqc);
        this.staff = staff;
        this.date = DateUtil.YYYY_MM_DD_HH_MM_SS.now();
        MySQLDatabase.query("SELECT `player` FROM vivat_review_tickets WHERE `id` = '%s'", resultSet -> {
            try {
                if (resultSet.next())
                    this.target = PlayerUtil.getOfflinePlayer(resultSet.getString("player"));
            } catch (SQLException e) {
                Logger.exception(e);
            }
        }, ticketID);
    }

    public boolean handle() {
        // plot, player, comment
        MySQLDatabase.query("SELECT `world`,`plot`,`player`,`comment` FROM vivat_review_tickets WHERE `id` = '%s'", rs -> {
            try {
                if (!rs.next()) return;
                plot = PlotUtil.getPlot(rs.getString("world"), rs.getString("plot"));
                target = PlayerUtil.getOfflinePlayer(rs.getString("player"));
                comment = rs.getString("comment");
            } catch (SQLException e) {
                Logger.exception(e);
            }
        }, ticketID);

        if (staff.isOnline()) {
            Player onlineStaff = (Player) staff;
            if (plot == null || plot.getOwners() == null)
                return Message.send(onlineStaff, CoreMessage.INVALID_ARGUMENT);
            if (plot.getOwners().contains(staff.getUniqueId()) && !ReviewPermission.BYPASS.has(onlineStaff))
                return Message.send(onlineStaff, ReviewMessage.CANT_REVIEW_OWN_PLOT);
        } else if (plot == null || plot.getOwners() == null || plot.getOwners().contains(staff.getUniqueId())) {
            return true;
        }

        scores = new Scores(scqcString);
        boolean passes = passes();
        State state = passes ? State.ACCEPTED : State.DENIED;

        final String updateTickets = "UPDATE vivat_review_tickets SET `state`='%s', `staff`='%s', `score`='%s', `review_date`='%s' WHERE `id` = '%s'";
        MySQLDatabase.update(updateTickets, state.toString(), staff.getUniqueId().toString(), JsonUtil.toJSON(scores), date, ticketID);

        if (target == null) return true;
        VivatPlayer t = Users.get(target);
        if (passes)
            new RankupHandler(target, t).handle();
        if (!target.isOnline())
            t.setReviewAvailable(true);

        if (staff.isOnline()) {
            Message.send((Player) staff, ReviewMessage.STAFF_REVIEWED_PLOT);
            CommentHandler.sendEdit((Player) staff, ticketID, comment);
        }

        if(target.isOnline())
            Message.send((Player) target, ReviewMessage.ONLINE_REVIEWED, "%staff_name%", staff.getName());

        MySQLDatabase.query("SELECT COUNT(*) FROM vivat_review_tickets WHERE `world` = '%s' AND `plot` = '%s'", resultSet -> {
            try {
                if(!resultSet.next()) return;
                int amount = resultSet.getInt(1);
                if(amount != 1) return;
                VivatWorld world = VivatWorld.fromString(plot.getWorldName());
                int oldAmount = t.getPlotcount(world);
                t.setPlotcount(world, oldAmount + 1);
            } catch (SQLException | WorldNotFoundException e) {
                Logger.exception(e);
            }
        }, plot.getWorldName(), plot.getId().toString());

        RecentGui.refreshCache();
        return true;
    }

    private boolean passes() {
        int passedCategories = 0;
        double threshold = getPassThreshold();
        passedCategories += (scores.skill >= threshold ? 1 : 0);
        passedCategories += (scores.creativity >= threshold ? 1 : 0);
        passedCategories += (scores.quality >= threshold ? 1 : 0);
        passedCategories += (scores.composition >= threshold ? 1 : 0);
        return passedCategories >= 3;
    }

    private double getPassThreshold() {
        try {
            VivatWorld world = VivatWorld.fromString(plot.getWorldName());
            return ThresholdUtil.getThresholdFromworld(world);
        } catch (WorldNotFoundException e) {
            Logger.exception(e);
            return 10;
        }
    }

}
