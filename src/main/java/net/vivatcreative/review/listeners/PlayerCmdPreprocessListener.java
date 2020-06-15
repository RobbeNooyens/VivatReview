package net.vivatcreative.review.listeners;

import net.vivatcreative.core.messages.Message;
import net.vivatcreative.core.utils.Logger;
import net.vivatcreative.core.utils.PlotUtil;
import net.vivatcreative.review.api.ReviewMessage;
import net.vivatcreative.review.api.ReviewPermission;
import net.vivatcreative.review.managers.SubmissionManager;
import net.vivatcreative.review.utils.QueryUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.util.Arrays;
import java.util.List;

public class PlayerCmdPreprocessListener implements Listener {

    private static final List<String> cmdAliases = Arrays.asList("plot", "p", "2", "plots", "plotsquared", "plot2",
            "p2", "ps", "plotme");
    private static final List<String> subCmdSubmit = Arrays.asList("submit", "done");
    private static final List<String> subCmdDelete = Arrays.asList("delete", "del", "clear");

    @EventHandler(priority = EventPriority.LOWEST)
    public void onCommand(PlayerCommandPreprocessEvent e) {
        Player p = e.getPlayer();
        String cmd = e.getMessage().toLowerCase().replaceAll("/", "");

        try {
            if (SubmissionManager.isSubmitQueued(p) && !cmd.startsWith("submit")) { // Command isn't /submit or /prdebug
                if (!isAlias(cmd, subCmdSubmit)) {
                    SubmissionManager.removeSubmitQueue(p); // Player's UUID will be removed from Submit Queue
                    Message.send(p, ReviewMessage.CANCELLED_SUBMIT); // Confirmation that Submit has been cancelled
                    return;
                }
            }
        } catch (Exception exception) {
            Logger.exception(exception);
            Logger.error("SubmitManager unregistered. Reload needed in order to sync the plugin with the SubmitManager. " +
                            "Please contact a developer or the owner asap!");
        }
        if (isAlias(cmd, subCmdDelete)) { // Prevent deletion of reviewed plots
            int id = QueryUtil.getLastIdFromPlot(PlotUtil.getCurrentPlot(p));
            if (id >= 0 && !ReviewPermission.BYPASS.hasAndWarn(p)) e.setCancelled(true);
        } else if (isAlias(cmd, subCmdSubmit)) { // Forward /plot submit and /plot done to /submit
            e.setCancelled(true);
            Bukkit.dispatchCommand(p, "submit");
        }

    }

    private boolean isAlias(String command, List<String> subCmd) {
        String[] args = command.split(" ");
        String cmd = command.replaceAll("plotsquared:", "");
        return args.length >= 2 && (cmdAliases.contains(cmd) && subCmd.contains(args[1]));
    }
}
