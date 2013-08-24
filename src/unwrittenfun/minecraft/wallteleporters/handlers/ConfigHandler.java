package unwrittenfun.minecraft.wallteleporters.handlers;

import net.minecraftforge.common.Configuration;

import java.io.File;

/**
 * Mod: Wall Teleporters
 * Author: UnwrittenFun
 * License: Minecraft Mod Public License (Version 1.0.1)
 */
public class ConfigHandler {
    public static void init(File file) {
        Configuration config = new Configuration(file);

        config.load();
        config.save();
    }
}
