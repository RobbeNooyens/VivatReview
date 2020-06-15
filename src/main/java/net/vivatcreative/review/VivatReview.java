package net.vivatcreative.review;

import net.vivatcreative.core.connections.ConnectionManager;
import net.vivatcreative.core.files.FileManager;
import net.vivatcreative.core.messages.MessageHelper;
import net.vivatcreative.review.api.ReviewConnection;
import net.vivatcreative.review.commands.ReviewCommand;
import net.vivatcreative.review.commands.SubmitCommand;
import net.vivatcreative.review.guis.RecentGui;
import net.vivatcreative.review.hooks.PlaceholderManager;
import net.vivatcreative.review.listeners.PlayerCmdPreprocessListener;
import net.vivatcreative.review.listeners.PlayerJoinLeaveListener;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class VivatReview extends JavaPlugin {

    public void onEnable() {
        // Hooks
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null)
            (new PlaceholderManager(this)).register();

        // Vivat API
        ReviewConnection reviewConnection = new ReviewConnection();
        reviewConnection.registerCommand("review", new ReviewCommand());
        reviewConnection.registerCommand("submit", new SubmitCommand());
        ConnectionManager.register(reviewConnection);

        MessageHelper.register(FileManager.getFile(this, "messages.yml", false));

        // Setup classes implementing Listener
        Bukkit.getPluginManager().registerEvents(new PlayerCmdPreprocessListener(), this);
        Bukkit.getPluginManager().registerEvents(new PlayerJoinLeaveListener(), this);

        // Cache
        RecentGui.refreshCache();
    }

    public static VivatReview get() {
        return JavaPlugin.getPlugin(VivatReview.class);
    }

    public void onDisable() {
        FileManager.removeFile(this, "messages.yml", null);
    }

}