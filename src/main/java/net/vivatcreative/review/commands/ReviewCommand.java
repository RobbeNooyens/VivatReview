package net.vivatcreative.review.commands;

import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotPlayer;
import net.vivatcreative.core.VivatCore;
import net.vivatcreative.core.messages.CoreMessage;
import net.vivatcreative.core.messages.Message;
import net.vivatcreative.core.utils.PlayerUtil;
import net.vivatcreative.core.utils.PlotUtil;
import net.vivatcreative.review.VivatReview;
import net.vivatcreative.review.api.ReviewMessage;
import net.vivatcreative.review.api.ReviewPermission;
import net.vivatcreative.review.guis.HistoryGui;
import net.vivatcreative.review.guis.RecentGui;
import net.vivatcreative.review.guis.ReviewListGui;
import net.vivatcreative.review.handlers.*;
import net.vivatcreative.review.utils.QueryUtil;
import org.apache.commons.lang.StringUtils;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * This class handles the /review command.
 *
 * @author Robnoo02
 */
public class ReviewCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player))
            return Message.send(sender, CoreMessage.SHOULD_BE_PLAYER); // Sender should be Player or Console
        if (!cmd.getName().equalsIgnoreCase("review")) return true; // Command starts with /review
        if (args.length == 0) return Message.send(sender, ReviewMessage.REVIEW_HELP); // Subcommand required
        OfflinePlayer target;
        Player p = (Player) sender;
        int ticketId;
        switch (args[0]) {
            case "list": // /review list
                return !ReviewPermission.LIST.hasAndWarn(p) || ReviewListGui.show(p);
            case "credits":
                return Message.send(sender, ReviewMessage.REVIEW_CREDITS, "%plugin_version%",
                        VivatCore.get().getDescription().getVersion());
            case "info": // /review info <id>
                if (!ReviewPermission.INFO.hasAndWarn(p)) break;
                if (args.length == 1 || args[1].equalsIgnoreCase("this")) {
                    Plot plot = PlotUtil.getCurrentPlot((Player) sender);
                    if (plot == null) return Message.send(sender, ReviewMessage.NOT_ON_PLOT);
                    ticketId = QueryUtil.getLastIdFromPlot(plot);
                } else if (!StringUtils.isNumeric(args[1])) {
                    return Message.send(sender, CoreMessage.INVALID_ARGUMENT); // Prevent Cast exception; exits when not a valid number is given
                } else {
                    ticketId = Integer.parseInt(args[1]);
                }
                if (ticketId >= 0)
                    ReviewInfoHandler.handle(p, ticketId);
                else
                    Message.send(p, ReviewMessage.PLOT_NOT_REVIEWED);
                break;
            case "history":
                if (!ReviewPermission.HISTORY.hasAndWarn(p)) break;
                target = (args.length < 2 ? p : PlayerUtil.getOfflinePlayer(args[1]));
                if (target == null)
                    return Message.send(sender, CoreMessage.TARGET_NOT_FOUND);
                return HistoryGui.show(p, target);
            case "score":
                if (!ReviewPermission.SCORE.hasAndWarn(p)) break;
                if (args.length < 3)
                    return Message.send(sender, CoreMessage.COMMAND_USAGE, "%usage%", "/review score <id> <S-C-Q-C>");
                if (isInvalidScore(args[2]))
                    return Message.send(sender, CoreMessage.INVALID_ARGUMENT);
                return (new ReviewHandler(Integer.parseInt(args[1]), p, args[2])).handle();
            case "gscore":
                if (!ReviewPermission.GSCORE.hasAndWarn(p)) break;
                if (args.length < 3)
                    return Message.send(sender, CoreMessage.COMMAND_USAGE, "%usage%", "/review score <id> <S-T-O-C> [comment]");
                if (isInvalidScore(args[2]))
                    return Message.send(sender, CoreMessage.INVALID_ARGUMENT);
                return GhostReviewHandler.handle(Integer.parseInt(args[1]), p, args[2]);
            case "summary":
                if (!ReviewPermission.SUMMARY.hasAndWarn(p)) break;
                new ReviewSummaryHandler(p).handle();
                break;
            case "recent":
                return !ReviewPermission.RECENT.hasAndWarn(p) || RecentGui.show(p);
            case "tp":
            case "teleport":
                if (!ReviewPermission.TELEPORT.hasAndWarn(p)) break;
                if (args.length < 2)
                    return Message.send(sender, CoreMessage.COMMAND_USAGE, "%usage%", "/review tp <id>");
                try {
                    QueryUtil.getPlotFromID(Integer.parseInt(args[1])).teleportPlayer(PlotPlayer.wrap(p));
                } catch (Exception e) {
                    Message.send(p, CoreMessage.INVALID_ARGUMENT);
                }
                break;
            case "comment":
                if (args.length == 2) {
                    if (!ReviewPermission.REVEAL_COMMENT.hasAndWarn(p)) return true;
                    if (!StringUtils.isNumeric(args[1]))
                        return Message.send(sender, CoreMessage.INVALID_ARGUMENT);
                    ticketId = Integer.parseInt(args[1]);
                    CommentHandler.sendEditIfStaff(p, ticketId, QueryUtil.getCommentFromID(ticketId));
                    return true;
                }
                if (args.length < 4)
                    return Message.send(sender, CoreMessage.COMMAND_USAGE, "%usage%", "/review comment <id> <category> <comment>");
                if (!ReviewPermission.WRITE_COMMENT.hasAndWarn(p)) break;
                if (!StringUtils.isNumeric(args[1]))
                    return Message.send(sender, CoreMessage.INVALID_ARGUMENT);
                ticketId = Integer.parseInt(args[1]);
                String category = args[2];
                StringBuilder commentBuilder = new StringBuilder();
                for (int i = 3; i < args.length; i++)
                    commentBuilder.append(args[i]).append(" ");
                commentBuilder.deleteCharAt(commentBuilder.length() - 1);
                Message.send(sender, ReviewMessage.REVIEW_COMMENT_SET);
                String comment = CommentHandler.applyEdit(ticketId, category, commentBuilder.toString());
                CommentHandler.sendEdit(p, ticketId, comment);
                return true;
            case "ghost":
                if (!ReviewPermission.GHOST.hasAndWarn(p)) break;
                if (args.length < 4)
                    return Message.send(sender, CoreMessage.COMMAND_USAGE, "%usage%", "/review ghost <id> <staff> <score>");
                ticketId = Integer.parseInt(args[1]);
                new ReviewHandler(ticketId, PlayerUtil.getOfflinePlayer(args[2]), args[3]).handle();
                break;
            case "rl":
            case "reload":
                if (!ReviewPermission.RELOAD.hasAndWarn(p)) break;
                VivatReview.get().reloadConfig();
                return Message.send(sender, CoreMessage.CONFIG_RELOADED);
            case "recentcache":
                if (!ReviewPermission.RELOAD.hasAndWarn(p)) break;
                RecentGui.refreshCache();
                return Message.send(sender, ReviewMessage.REFRESHED_CACHE);
            case "help":
            default:
                return Message.send(sender, ReviewMessage.REVIEW_HELP);
        }
        return true;
    }

    private boolean isInvalidScore(String score) {
        String[] scores = score.split("-");
        if (scores.length != 4) return true;
        for (String s : scores) {
            if (s == null) return true;
            double d;
            try {
                d = Double.parseDouble(s);
            } catch (ClassCastException e) {
                return true;
            }
            if (d > 10 || d < 0) return true;
        }
        return false;
    }

}
