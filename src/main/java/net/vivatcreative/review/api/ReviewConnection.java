package net.vivatcreative.review.api;

import net.vivatcreative.core.connections.VivatConnection;
import net.vivatcreative.review.VivatReview;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashSet;
import java.util.Set;

public class ReviewConnection implements VivatConnection {

    Set<String> commands = new HashSet<>();

    @Override
    public Object get(String s, Object... objects) {return null;}
    @Override
    public void set(String s, Object... objects) {}
    @Override
    public JavaPlugin getPlugin() { return VivatReview.get(); }
    @Override
    public void onReload() {}
    @Override
    public void addCommand(String s) { commands.add(s); }
    @Override
    public Set<String> getRegisteredCommands() { return commands; }
}
