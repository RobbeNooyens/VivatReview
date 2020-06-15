package net.vivatcreative.review.guis;

import com.intellectualcrafters.plot.object.PlotPlayer;
import net.vivatcreative.core.database.MySQLDatabase;
import net.vivatcreative.core.exceptions.WorldNotFoundException;
import net.vivatcreative.core.gui.BlueprintGui;
import net.vivatcreative.core.gui.GuiItem;
import net.vivatcreative.core.ranks.BuildRank;
import net.vivatcreative.core.utils.*;
import net.vivatcreative.review.handlers.CommentHandler;
import net.vivatcreative.review.handlers.State;
import net.vivatcreative.review.json.GhostReview;
import net.vivatcreative.review.managers.StaffMembersManager;
import net.vivatcreative.review.utils.QueryUtil;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;

public class RecentGui extends BlueprintGui {

	private static final GuiItem[] cachedItems = new GuiItem[36];
	private static final String query = "SELECT * FROM vivat_review_tickets WHERE `state` = 'ACCEPTED' OR `state` = 'DENIED' OR `state` = 'GHOSTED' ORDER BY review_date DESC LIMIT 36";

	public RecentGui(Player p) {
		super(p);
		this.setTitle("&8Recent Tickets");
		this.setItems(cachedItems);
	}

	public static boolean show(Player p) {
		Objects.requireNonNull(p);
		new RecentGui(p).open();
		return true;
	}

	public static void refreshCache(){
		MySQLDatabase.query(query, (result) -> {
			int index = 0;
			try {
				while (result.next()) {
					cachedItems[index++] = getRecentItem(result);
				}
			} catch(SQLException | WorldNotFoundException e){
				Logger.exception(e);
			}
		});
	}

	private static GuiItem getRecentItem(ResultSet rs) throws SQLException, WorldNotFoundException {
		OfflinePlayer target = PlayerUtil.getOfflinePlayer(rs.getString("player"));
		BuildRank rank = QueryUtil.getPlayerBuildRank(target);
		int ticketID = rs.getInt("id");
		String world = rs.getString("world");
		String plot = rs.getString("plot");
		String comment = rs.getString("comment");
		return new GuiItem.Builder().name(rank.getName(true) + " &7" + target.getName())
				.lore(getItemLore(ticketID, rs))
				.leftClick((p) -> PlotUtil.getPlot(world, plot).teleportPlayer(PlotPlayer.wrap(p)))
				.rightClick((p) -> {
					CommentHandler.sendEditIfStaff(p, ticketID, comment);
					p.closeInventory();
				}).playerSkull(target.getName()).build();
	}

	private static String[] getItemLore(int ticketId, ResultSet rs) throws SQLException, WorldNotFoundException {
		final String ghost = rs.getString("ghost");
		final String date = rs.getString("submission_date");
		final String worldName = rs.getString("world");
		final String staff = rs.getString("staff");
		final State reviewState = State.valueOf(rs.getString("state"));

		boolean hasGhost = ghost != null;
		final String ghostString = hasGhost ? getGhostLine(ghost) : "";
		String world = VivatWorld.fromString(worldName).coloured;

		if (reviewState == State.ACCEPTED || reviewState == State.DENIED) {
			return new String[]{ "&3(" + ticketId + ")",
					"&7State: " + (reviewState == State.ACCEPTED ? "&aACCEPTED" : "&cDENIED"),
					"&7World: &f" + world,
					"&7Staff: " + StaffMembersManager.getFormattedStaff(staff),
					/*"&7STOC: &f" + ColorGrade.get((double) info.get(TicketDoubleField.STRUCTURE_SCORE), true) + "&f-"
							+ ColorGrade.get((double) info.get(TicketDoubleField.TERRAIN_SCORE), true) + "&f-"
							+ ColorGrade.get((double) info.get(TicketDoubleField.ORGANICS_SCORE), true) + "&f-"
							+ ColorGrade.get((double) info.get(TicketDoubleField.COMPOSITION_SCORE), true),*/
					"&7Date: &f" + date, ghostString, "&7> Left-click to teleport.",
					"&7> Right-click to reveal comment." };
		} else if(reviewState == State.GHOSTED){
			return new String[]{ "&3(" + ticketId + ")","&7State: &eGHOSTED","&7World: &f" + world,"&7Date: &f" + date,ghostString};
		} else {
			return new String[]{ "&3(" + ticketId + ")","&7World: &f" + world,"&7Date: &f" + date};
		}
	}

	private static String getGhostLine(String ghost){
		GhostReview r = JsonUtil.fromJSON(ghost, GhostReview.class);
		String ghostStaff = StaffMembersManager.getFormattedStaff(r.uuid);
		return String.format("&7Ghost: %s &7> %s-%s-%s-%s", ghostStaff, r.skill, r.creativity, r.quality, r.composition);
	}

	@Override
	public void updateGui() {}
	
	

}
