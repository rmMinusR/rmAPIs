package rmMinusR.mc.plugins.apis;

import org.bukkit.configuration.file.FileConfiguration;

public final class Config {

    //Disable instantiation
    private Config() {}

    //There has to be a better way of doing this.
    private static final String KEY_ARMOR_STAND_RENDER_DIST = "hologram.render_distance.armor_stand";
    private static final int DEFAULT_ARMOR_STAND_RENDER_DIST = 64;
    public static int armorStandRenderDistance;

    public static void Load() {
        RmApisPlugin.INSTANCE.reloadConfig();
        FileConfiguration cfg = RmApisPlugin.INSTANCE.getConfig();

        armorStandRenderDistance = cfg.getKeys(true).contains(KEY_ARMOR_STAND_RENDER_DIST) ? cfg.getInt(KEY_ARMOR_STAND_RENDER_DIST) : DEFAULT_ARMOR_STAND_RENDER_DIST;
    }

    public static void Save() {
        FileConfiguration cfg = RmApisPlugin.INSTANCE.getConfig();

        cfg.set(KEY_ARMOR_STAND_RENDER_DIST, armorStandRenderDistance);

        RmApisPlugin.INSTANCE.saveConfig();
    }
}
