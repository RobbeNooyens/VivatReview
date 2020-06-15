package net.vivatcreative.review.guis;

import com.intellectualcrafters.plot.object.PlotPlayer;
import net.vivatcreative.core.database.MySQLDatabase;
import net.vivatcreative.core.exceptions.WorldNotFoundException;
import net.vivatcreative.core.gui.BlueprintGui;
import net.vivatcreative.core.gui.GuiItem;
import net.vivatcreative.core.players.Users;
import net.vivatcreative.core.ranks.BuildRank;
import net.vivatcreative.core.utils.*;
import net.vivatcreative.review.handlers.CommentHandler;
import net.vivatcreative.review.handlers.State;
import net.vivatcreative.review.json.Scores;
import net.vivatcreative.review.managers.StaffMembersManager;
import net.vivatcreative.review.utils.QueryUtil;
import net.vivatcreative.review.utils.ThresholdUtil;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class HistoryGui extends BlueprintGui {

    private final OfflinePlayer target;
    private final BuildRank targetBuildRank;

    public HistoryGui(Player p, OfflinePlayer target) {
        super(p);
        this.setTitle("&8Review History: &3" + target.getName());
        this.target = target;
        this.targetBuildRank = Users.get(target).getBuildRank();
        this.setItems(getItems());
    }

    public static boolean show(Player p, OfflinePlayer target) {
        Objects.requireNonNull(p);
        Objects.requireNonNull(target);
        new HistoryGui(p, target).open();
        return true;
    }

    private GuiItem[] getItems() {
        List<GuiItem> list = new ArrayList<>();
        MySQLDatabase.query("SELECT * FROM `vivat_review_tickets` WHERE `player` = '%s'", (set) -> {
            try {
                while(set.next())
                list.add(getHistoryItem(set.getInt("id"), set.getString("state"), set.getString("world"),
                        set.getString("plot"), set.getString("comment"), set.getString("staff"),
                        set.getString("submission_date"), set.getString("score")));
            } catch (SQLException e) {
                Logger.exception(e);
            }
        }, target.getUniqueId().toString());
        return list.toArray(new GuiItem[0]);
    }

    private GuiItem getHistoryItem(int ticketID, String state, String world, String plot, String comment, String staff, String date, String score) {
        int matData = itemColor(State.valueOf(state));
        Scores scores = JsonUtil.fromJSON(score, Scores.class);
        return new GuiItem.Builder()
                .name(targetBuildRank.getName(true) + " &7" + target.getName())
                .lore(getItemLore(ticketID, state, world, staff, date, scores))
                .leftClick((p) -> PlotUtil.getPlot(world, plot).teleportPlayer(PlotPlayer.wrap(p)))
                .rightClick((p) -> {
                    CommentHandler.sendEditIfStaff(p, ticketID, comment);
                    p.closeInventory();
                }).material(Material.CONCRETE).data(matData).build();
    }

    private int itemColor(State state) {
        switch (state) {
            case ACCEPTED:
                return 5;
            case DENIED:
                return 14;
            case GHOSTED:
            case SUBMITTED:
                return 4;
            default:
                return 0;
        }
    }

    private String[] getItemLore(int ticketId, String state, String world, String staffUUID, String date, Scores scores) {
        VivatWorld vivatWorld;
        try {
            vivatWorld = VivatWorld.fromString(world);
        } catch (WorldNotFoundException e) {
            Logger.exception(e);
            vivatWorld = VivatWorld.BRONZE;
        }
        State reviewState = State.valueOf(state);
        if (reviewState == State.ACCEPTED || reviewState == State.DENIED) {
            OfflinePlayer staff = PlayerUtil.getOfflinePlayer(staffUUID);
            Objects.requireNonNull(staff);
            return new String[]{"&3(" + ticketId + ")",
                    "&7World: &f" + vivatWorld.coloured,
                    "&7SCQC: &f" + scores.format("%skill%&7-%creativity%&7-%quality%&7-%composition%", ThresholdUtil.getThresholdFromworld(vivatWorld)),
                    "&7Staff: " + StaffMembersManager.getFormattedStaff(staffUUID),
                    "&7Date: &f" + date, "&7> Left-click to teleport.",
                    "&7> Right-click to reveal comment."};
        } else if (reviewState == State.SUBMITTED || reviewState == State.GHOSTED) {
            return new String[]{"&3(" + ticketId + ")", "&7World: &f" + vivatWorld.coloured};
        } else {
            return new String[]{};
        }
    }

    @Override
    public void updateGui() {
    }

}
