package net.vivatcreative.review.handlers;

import net.vivatcreative.core.database.MySQLDatabase;
import net.vivatcreative.core.messages.Message;
import net.vivatcreative.core.utils.Logger;
import net.vivatcreative.review.api.ReviewMessage;
import org.bukkit.entity.Player;

import java.sql.SQLException;

public class ReviewSummaryHandler {

    private final Player p;
    private String state;


    public ReviewSummaryHandler(Player p){
        this.p = p;
    }

    public void handle(){
        MySQLDatabase.query("SELECT `state`, `staff`, `id` FROM vivat_review_tickets WHERE `player` = '%s' ORDER BY `review_date` DESC LIMIT 1", resultSet -> {
            try {
                if(!resultSet.next())
                    Message.send(p, ReviewMessage.NO_PENDING_REVIEWS);
                state = resultSet.getString("state");
                if(state.equalsIgnoreCase("accepted")){
                    Message.send(p, ReviewMessage.REVIEW_SUMMARY_ACCEPTED, "%id%", resultSet.getString("id"));
                } else if(state.equalsIgnoreCase("denied")){
                    Message.send(p, ReviewMessage.REVIEW_SUMMARY_DENIED, "%id%", resultSet.getString("id"));
                } else {
                    Message.send(p, ReviewMessage.NO_PENDING_REVIEWS);
                }

            } catch (SQLException e) {
                Logger.exception(e);
            }
        }, p.getUniqueId().toString());
    }
}
