package net.vivatcreative.review.handlers;

import net.vivatcreative.core.database.MySQLDatabase;
import net.vivatcreative.core.exceptions.WorldNotFoundException;
import net.vivatcreative.core.messages.Message;
import net.vivatcreative.core.players.VivatPlayer;
import net.vivatcreative.core.ranks.BuildRank;
import net.vivatcreative.core.utils.Logger;
import net.vivatcreative.core.utils.PlotUtil;
import net.vivatcreative.core.utils.VivatWorld;
import net.vivatcreative.review.api.ReviewMessage;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class RankupHandler {

    private final OfflinePlayer p;
    private final VivatPlayer vivatPlayer;
    private final BuildRank currentRank;
    private String world;
    private int weight;

    public RankupHandler(OfflinePlayer p, VivatPlayer vivatPlayer) {
        this.p = Objects.requireNonNull(p);
        this.vivatPlayer = Objects.requireNonNull(vivatPlayer);
        this.currentRank = Objects.requireNonNull(vivatPlayer.getBuildRank());
    }

    public void handle() {
        final Set<String> preventDupes = new HashSet<>();
        MySQLDatabase.query("SELECT * FROM vivat_review_tickets WHERE `player` = '%s' AND `state` = 'ACCEPTED'", rs -> {
            try {
                while (rs.next()) {
                    world = rs.getString("world");
                    String plot = rs.getString("plot");
                    String plotLocation = PlotUtil.formatPlot(PlotUtil.getPlot(world, plot));
                    if (!preventDupes.contains(plotLocation)) {
                        weight += getWorldWeight();
                        preventDupes.add(plotLocation);
                    }
                }
            } catch (SQLException | WorldNotFoundException e) {
                Logger.exception(e);
            }
        }, p.getUniqueId().toString());

        BuildRank newRank = getNewRank();
        if(newRank.getWeight() > currentRank.getWeight())
            handleRankingUp(newRank);
    }

    private void handleRankingUp(BuildRank rank){
        if(p.isOnline()) {
            Bukkit.getOnlinePlayers().forEach(pl -> broadcastRankup(pl, rank));
        }else {
            vivatPlayer.setRankupAvailable(true);
        }
        vivatPlayer.setBuildRank(rank);
        VivatWorld world = getWorldFromRank(rank);
        if(world == null) return;
        int currentPlots = vivatPlayer.getPlotcount(world);
        vivatPlayer.setPlotcount(world, currentPlots + 1);
        // TODO: Send message that player unlocked plot in new world
    }

    private VivatWorld getWorldFromRank(BuildRank rank){
        switch (rank){
            case BRONZE:
                return VivatWorld.SILVER;
            case SILVER:
                return VivatWorld.GOLD;
            case GOLD:
                return VivatWorld.DIAMOND;
            case DIAMOND:
                return VivatWorld.EMERALD;
            case EMERALD:
                return VivatWorld.MASTER;
            default:
                return null;
        }
    }

    private void broadcastRankup(Player player, BuildRank rank){
        if(player.getUniqueId().equals(p.getUniqueId()))
            Message.send(player, ReviewMessage.RANKED_UP, "%new_rank%", rank.getName(true));
        else
            Message.send(player, ReviewMessage.PLAYER_RANKED_UP, "%player%", p.getName(), "%rank%", rank.getName(true));
    }

    private int getWorldWeight() throws WorldNotFoundException { // next = previous * 3
        VivatWorld w = VivatWorld.fromString(world);
        switch (w) {
            case BRONZE:
                return 1;
            case SILVER:
                return 3;
            case GOLD:
                return 9;
            case DIAMOND:
                return 27;
            case EMERALD:
                return 81;
            case MASTER:
                return 271;
            default:
                return 0;
        }
    }

    private BuildRank getNewRank(){ // next = sum all previous
        if(weight >= 392) return BuildRank.MASTER;
        if(weight >= 121) return BuildRank.EMERALD;
        if(weight >= 40) return BuildRank.DIAMOND;
        if(weight >= 13) return BuildRank.GOLD;
        if(weight >= 4) return BuildRank.SILVER;
        if(weight >= 1) return BuildRank.BRONZE;
        return BuildRank.DEFAULT;
    }
}
