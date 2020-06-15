package net.vivatcreative.review.utils;

import com.intellectualcrafters.plot.object.Plot;
import net.vivatcreative.core.database.MySQLDatabase;
import net.vivatcreative.core.ranks.BuildRank;
import net.vivatcreative.core.utils.Logger;
import net.vivatcreative.core.utils.PlotUtil;
import org.bukkit.OfflinePlayer;

import java.sql.SQLException;

public class QueryUtil {

    private static final String LATEST_ID = "SELECT `id` FROM vivat_review_tickets WHERE `world` = '%s' AND `plot` = '%s' ORDER BY `submission_date` DESC LIMIT 1",
            PLOT_FROM_ID = "SELECT `world`, `plot` FROM vivat_review_tickets WHERE `id` = %s",
            PLAYER_FROM_ID = "SELECT `player` FROM vivat_review_tickets WHERE `id` = %s",
            COMMENT_FROM_ID = "SELECT `comment` FROM vivat_review_tickets WHERE `id` = %s";

    public static int getLastIdFromPlot(String world, String plotID){
        final int[] id = {-1};
        MySQLDatabase.query(LATEST_ID, resultSet -> {
            try {
                if(!resultSet.next()) return;
                id[0] = resultSet.getInt("id");
            } catch (SQLException e) {
                Logger.exception(e);
            }
        }, world, plotID);
        return id[0];
    }

    public static int getLastIdFromPlot(Plot plot){
        return getLastIdFromPlot(plot.getWorldName(), plot.getId().toString());
    }

    public static Plot getPlotFromID(int id){
        final Plot[] plot = new Plot[1];
        MySQLDatabase.query(PLOT_FROM_ID, resultSet -> {
            try {
                if(!resultSet.next()) return;
                plot[0] = PlotUtil.getPlot(resultSet.getString("world"), resultSet.getString("plot"));
            } catch (SQLException e) {
                Logger.exception(e);
            }
        }, id);
        return plot[0];
    }

    public static BuildRank getPlayerBuildRank(OfflinePlayer p){
        final BuildRank[] rank = new BuildRank[1];
        MySQLDatabase.query("SELECT build_rank FROM vivat_users WHERE `id` = '%s'", resultSet -> {
            try {
                if(!resultSet.next())
                    rank[0] = BuildRank.DEFAULT;
                else
                    rank[0] = BuildRank.fromString(resultSet.getString("build_rank"));
            } catch (SQLException e) {
                Logger.exception(e);
            }
        }, p.getUniqueId().toString());
        return rank[0];
    }

    public static String getPlayerFromID(int ticketID){
        final String[] uuid = new String[1];
        MySQLDatabase.query(PLAYER_FROM_ID, resultSet -> {
            try {
                if(!resultSet.next()) return;
                uuid[0] = resultSet.getString("player");
            } catch (SQLException e) {
                Logger.exception(e);
            }
        }, ticketID);
        return uuid[0];
    }

    public static String getCommentFromID(int ticketID){
        final String[] comment = new String[1];
        MySQLDatabase.query(COMMENT_FROM_ID, resultSet -> {
            try {
                if(!resultSet.next()) return;
                comment[0] = resultSet.getString("comment");
            } catch (SQLException e) {
                Logger.exception(e);
            }
        }, ticketID);
        return comment[0];
    }
}
