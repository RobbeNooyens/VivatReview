package net.vivatcreative.review.managers;

import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class SubmissionManager {
    private final Set<UUID> submitQueue = new HashSet<>();
    private static final SubmissionManager instance = new SubmissionManager();

    private SubmissionManager() {}

    public static boolean isSubmitQueued(Player p) {
        return instance.submitQueue.contains(p.getUniqueId());
    }

    public static void addSubmitQueue(Player p) {
        instance.submitQueue.add(p.getUniqueId());
    }

    public static void removeSubmitQueue(Player p) {
        instance.submitQueue.remove(p.getUniqueId());
    }
}
