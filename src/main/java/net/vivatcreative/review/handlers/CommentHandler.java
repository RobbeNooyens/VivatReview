package net.vivatcreative.review.handlers;

import net.vivatcreative.core.database.MySQLDatabase;
import net.vivatcreative.core.messages.Message;
import net.vivatcreative.core.permissions.CorePermission;
import net.vivatcreative.core.utils.JsonUtil;
import net.vivatcreative.core.utils.Logger;
import net.vivatcreative.review.api.ReviewMessage;
import net.vivatcreative.review.guis.RecentGui;
import net.vivatcreative.review.json.Comment;
import net.vivatcreative.review.utils.QueryUtil;
import org.bukkit.entity.Player;

import java.sql.SQLException;

public class CommentHandler {

    private static final String COMMENT_QUERY = "SELECT comment FROM vivat_review_tickets WHERE `id` = '%s'",
    COMMENT_UPDATE = "UPDATE vivat_review_tickets SET `comment` = '%s' WHERE `id` = '%s'";

    public static void send(Player p, String commentString){
        Comment comment = JsonUtil.fromJSON(commentString, Comment.class);
        Message.send(p, ReviewMessage.REVIEW_COMMENT, "%skill%", comma(comment.skill), "%creativity%",
                comma(comment.creativity), "%quality%", comma(comment.quality), "%composition%", comma(comment.composition));
    }

    public static void sendEdit(Player p, int ticketID, String commentString){
        if(QueryUtil.getPlayerFromID(ticketID).equalsIgnoreCase(p.getUniqueId().toString())) {
            send(p, commentString);
            return;
        }
        Comment comment = JsonUtil.fromJSON(commentString, Comment.class);
        Message.send(p, ReviewMessage.REVIEW_EDIT_COMMENT, "%id%", String.valueOf(ticketID), "%skill%", comma(comment.skill), "%creativity%",
                comma(comment.creativity), "%quality%", comma(comment.quality), "%composition%", comma(comment.composition));
    }

    private static String comma(String s){
        return s.replaceAll("u0027", "'");
    }

    public static void sendEditIfStaff(Player p, int ticketID, String commentString){
        if(CorePermission.VIVAT_STAFF.has(p)) sendEdit(p, ticketID, commentString);
        else send(p, commentString);
    }

    public static String applyEdit(int ticketID, String key, String comment){
        final Comment commentObj = new Comment();
        MySQLDatabase.query(COMMENT_QUERY, (result) -> {
            String[] parts = null;
            try {
                if(!result.next()) return;
                Comment commentObjDb = JsonUtil.fromJSON(result.getString("comment"), Comment.class);
                commentObj.composition = commentObjDb.composition;
                commentObj.skill = commentObjDb.skill;
                commentObj.quality = commentObjDb.quality;
                commentObj.creativity = commentObjDb.creativity;
            } catch (SQLException e) {
                Logger.exception(e);
            }
            switch (key.toLowerCase()){
                case"skill":
                    commentObj.skill = comment;
                    break;
                case"creativity":
                    commentObj.creativity = comment;
                    break;
                case"quality":
                    commentObj.quality = comment;
                    break;
                case"composition":
                    commentObj.composition = comment;
                    break;
            }
        }, ticketID);
        String newComment = JsonUtil.toJSON(commentObj);
        MySQLDatabase.update(COMMENT_UPDATE, newComment, ticketID);
        RecentGui.refreshCache();
        return newComment;
    }
}
