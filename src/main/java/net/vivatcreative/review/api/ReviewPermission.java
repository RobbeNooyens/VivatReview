package net.vivatcreative.review.api;

import net.vivatcreative.core.permissions.VivatPermission;

public enum ReviewPermission implements VivatPermission {

    /**
     * Players with this permission can review their own plot
     * and delete/clear their plot after a submission.
     */
    BYPASS("review.bypass"),
    /**
     * Players with this permission can use /review list.
     */
    LIST("review.list"),
    /**
     * Players with this permission can use /review info.
     */
    INFO("review.info"),
    /**
     * Players with this permission can use /review history.
     */
    HISTORY("review.history"),
    /**
     * Players with this permission can use /review score.
     */
    SCORE("review.score"),
    /**
     * Players with this permission can use /review gscore.
     */
    GSCORE("review.gscore"),
    /**
     * Players with this permission can use /review summary.
     */
    SUMMARY("review.summary"),
    /**
     * Players with this permission can use /review recent.
     */
    RECENT("review.recent"),
    /**
     * Players with this permission can use /review comment.
     */
    REVEAL_COMMENT("review.comment.read"),
    /**
     * Players with this permission can use /review comment.
     */
    WRITE_COMMENT("review.comment.write"),
    /**
     * Players with this permission can use /review ghost.
     */
    GHOST("review.ghost"),
    /**
     * Players with this permission can use /review reload.
     */
    RELOAD("review.reload"),
    /**
     * Teleport to review plot.
     */
    TELEPORT("review.teleport"),
    /**
     * Players with this permission can submit their plot.
     */
    SUBMIT("review.submit");

    private final String node;

    ReviewPermission(String node){ this.node = node; }

    @Override
    public String getNode() { return node; }
}