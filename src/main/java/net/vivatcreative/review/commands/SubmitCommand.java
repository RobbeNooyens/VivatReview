package net.vivatcreative.review.commands;

import com.intellectualcrafters.plot.object.Plot;
import net.vivatcreative.core.messages.CoreMessage;
import net.vivatcreative.core.messages.Message;
import net.vivatcreative.core.utils.PlotUtil;
import net.vivatcreative.review.api.ReviewMessage;
import net.vivatcreative.review.api.ReviewPermission;
import net.vivatcreative.review.handlers.SubmissionHandler;
import net.vivatcreative.review.managers.SubmissionManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * This class handles the /submit command. A submit will be saved in a queue on
 * /submit. A submit can be confirmed with /submit confirm and cancelled with
 * /submit cancel. Submit will be removed from queue after
 * confirming/cancelling.
 * 
 * @author Robnoo02
 *
 */
public class SubmitCommand implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (!(sender instanceof Player))
			return Message.send(sender, CoreMessage.SHOULD_BE_PLAYER);
		Player p = (Player) sender;
		if(!ReviewPermission.SUBMIT.hasAndWarn(p)) return true;
		if (!cmd.getName().equalsIgnoreCase("submit"))
			return true;
		Plot plot = PlotUtil.getCurrentPlot(p);
		if(plot == null) return Message.send(p, ReviewMessage.NOT_ON_PLOT);
		SubmissionHandler handler = new SubmissionHandler(p, plot);
		if (SubmissionManager.isSubmitQueued(p)) {
			if (args.length == 0)
				return Message.send(sender, ReviewMessage.CONFIRM_OR_CANCEL_SUBMIT);
			String confirmCancel = args[0];
			if (confirmCancel.equalsIgnoreCase("confirm"))
				handler.handle();
			else if (confirmCancel.equalsIgnoreCase("cancel"))
				Message.send(sender, ReviewMessage.CANCELLED_SUBMIT);
			else
				return Message.send(sender, ReviewMessage.CONFIRM_OR_CANCEL_SUBMIT);
			SubmissionManager.removeSubmitQueue(p);
			return true;
		} else {
			if(!handler.possibleToSubmit(true))
				return true;
			if (args.length > 0 && args[0].equalsIgnoreCase("cancel")) return true;
			SubmissionManager.addSubmitQueue(p);
			return Message.send(sender, ReviewMessage.SUBMIT_PLOT);
		}
	}

}
