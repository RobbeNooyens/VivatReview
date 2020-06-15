package net.vivatcreative.review.guis;

import net.vivatcreative.core.database.MySQLDatabase;
import net.vivatcreative.core.exceptions.WorldNotFoundException;
import net.vivatcreative.core.gui.BlueprintGui;
import net.vivatcreative.core.gui.GuiItem;
import net.vivatcreative.core.messages.CoreMessage;
import net.vivatcreative.core.messages.Message;
import net.vivatcreative.core.utils.*;
import net.vivatcreative.review.api.ReviewMessage;
import net.vivatcreative.review.api.ReviewPermission;
import net.vivatcreative.review.handlers.State;
import net.vivatcreative.review.json.GhostReview;
import net.vivatcreative.review.json.Scores;
import net.vivatcreative.review.managers.StaffMembersManager;
import net.vivatcreative.review.utils.QueryUtil;
import net.vivatcreative.review.utils.ThresholdUtil;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import com.intellectualcrafters.plot.object.PlotPlayer;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class ReviewListGui extends BlueprintGui {

	private static final String LIST_QUERY = "SELECT * FROM vivat_review_tickets WHERE `state` = 'SUBMITTED' OR `state` = 'GHOSTED' ORDER BY `submission_date` DESC",
	RESUBMISSION = "SELECT `id`, `staff`, `score` FROM vivat_review_tickets WHERE `world` = '%s' AND `plot` = '%s' AND (`state` = 'ACCEPTED' OR `state` = 'DENIED') ORDER BY `review_date` DESC LIMIT 1";

	public ReviewListGui(Player p) {
		super(p);
		GuiItem[] items = getReviewHeads();
		this.setTitle("&8Review List &e[" + items.length + "]");
		this.setItems(items);
	}

	public static boolean show(Player p) {
		if (p == null) return false;
		new ReviewListGui(p).open();
		return true;
	}

	/**
	 * @return array containing all heads which should be putted into the Gui
	 */
	private GuiItem[] getReviewHeads() {
        List<GuiItem> items = new ArrayList<>();
        MySQLDatabase.query(LIST_QUERY, (resultSet -> {
            try {
                while (resultSet.next())
                    items.add(getReviewListItem(resultSet));
            } catch(SQLException | WorldNotFoundException e){
                Logger.exception(e);
            }
        }));
        return items.toArray(new GuiItem[]{});
    }

    private GuiItem getReviewListItem(ResultSet rs) throws SQLException, WorldNotFoundException {
	    String ghostJSON = rs.getString("ghost");
	    GhostReview ghostReview = ghostJSON == null ? null : JsonUtil.fromJSON(ghostJSON, GhostReview.class);
	    String ghost = ghostReview == null ? "" : getGhostLine(ghostReview);
	    String date = rs.getString("submission_date");
	    OfflinePlayer target = PlayerUtil.getOfflinePlayer(rs.getString("player"));
	    int ticketID = rs.getInt("id");
        VivatWorld world = VivatWorld.fromString(rs.getString("world"));
        String plot = rs.getString("plot");

	    return new GuiItem.Builder().name(QueryUtil.getPlayerBuildRank(target).getName(true) + " &7" + target.getName())
                .lore("&3(" + ticketID + ")", "&7World: &f" + world.coloured,
                        "&7Date: &f" + date, ghost,
                        "&7> Left-click to teleport.",
                        "&7> Right-click to rate this plot.")
                .leftClick((p) -> {
                	PlotUtil.getPlot(world.officialName, plot).teleportPlayer(PlotPlayer.wrap(p));
                	showScoreIfResubmission(p, world, plot);
                    getClickable(ticketID, ghostReview, p).accept(p);
                }).rightClick((p) -> getClickable(ticketID, ghostReview, p).accept(p)).playerSkull(target.getName()).build();
    }

    private String getGhostLine(GhostReview r){
        String ghostStaff = StaffMembersManager.getFormattedStaff(r.uuid);
        return String.format("&7Ghost: %s &7> %s-%s-%s-%s", ghostStaff, r.skill, r.creativity, r.quality, r.composition);
    }

    private void showScoreIfResubmission(Player p, VivatWorld world, String plot){
		MySQLDatabase.query(RESUBMISSION, rs -> {
			try {
				if(!rs.next()) return;
				String id = String.valueOf(rs.getInt("id"));
				String staff = StaffMembersManager.getFormattedStaff(rs.getString("staff"));
				String scores = JsonUtil.fromJSON(rs.getString("score"), Scores.class)
						.format("%skill%&7-%creativity%&7-%quality%&7-%composition%", ThresholdUtil.getThresholdFromworld(world));
				Message.send(p, ReviewMessage.RESUBMISSION_NOTIFICATION, "%id%", id, "%staff%", staff, "%score%", TextUtil.toColor(scores));
			} catch (SQLException e) {
				Logger.exception(e);
			}
		}, world.officialName, plot);
	}

	private Consumer<Player> getClickable(int reviewId, GhostReview ghost, Player player) {
		if (ghost == null) {
			if (ReviewPermission.SCORE.has(player))
				return (p) -> Message.send(p, ReviewMessage.CLICK_TO_REVIEW, "%ticket_id%",
						String.valueOf(reviewId));
			else
				return (p) -> Message.send(p, ReviewMessage.CLICK_TO_REVIEW_GHOST, "%ticket_id%",
						String.valueOf(reviewId));
		} else {
			if (ReviewPermission.SCORE.has(player))
				return (p) -> Message.send(p, ReviewMessage.CONFIRM_GHOST, "%id%", String.valueOf(reviewId),
						"%staff%", TextUtil.removeColor(StaffMembersManager.getFormattedStaff(ghost.uuid)), "%score%", formatScore(ghost));
			else
				return (p) -> Message.send(p, CoreMessage.NO_PERM);
		}

	}

	private String formatScore(GhostReview review){
	    return String.format("%s-%s-%s-%s", review.skill, review.creativity, review.quality, review.composition);
    }

	@Override
	public void updateGui() {
	}

}
