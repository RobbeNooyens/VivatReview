package net.vivatcreative.review.json;

import net.vivatcreative.core.utils.JsonUtil;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class CommentTest {

    @Test
    public void shoudlTestConversion(){
        Comment comment = new Comment();
        comment.skill = "None";
        comment.creativity = "None";
        comment.quality = "None";
        comment.composition = "None";
        assertEquals(JsonUtil.toJSON(comment), "{\"skill\":\"None\",\"creativity\":\"None\",\"quality\":\"None\",\"composition\":\"None\"}");
    }
}
