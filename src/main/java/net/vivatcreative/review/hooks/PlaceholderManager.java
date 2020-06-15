package net.vivatcreative.review.hooks;

import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotPlayer;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import net.vivatcreative.core.players.Users;
import net.vivatcreative.core.players.VivatPlayer;
import net.vivatcreative.review.VivatReview;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

public class PlaceholderManager extends PlaceholderExpansion {

	private VivatReview plugin;

	public PlaceholderManager(VivatReview plugin) {
		this.plugin = plugin;
	}

	@Override
	public boolean persist() {
		return true;
	}

	@Override
	public boolean canRegister() {
		return true;
	}

	@Override
	public String getIdentifier() {
		return "review";
	}

	@Override
	public String getAuthor() {
		return plugin.getDescription().getAuthors().toString();
	}

	@Override
	public String getVersion() {
		return plugin.getDescription().getVersion();
	}

	@Override
	public String onPlaceholderRequest(Player player, String identifier) {
		
		VivatPlayer vP = Users.get(player);
		Plot plot = PlotPlayer.wrap(player).getCurrentPlot();
		
		switch (identifier.toLowerCase()) {
		case "state":
			// TODO: implement placeholders
			return "Not implemented";
		}

		return null; // Invalid placeholder (f.e. %rt_placeholder3%)
	}
}
