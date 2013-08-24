package unwrittenfun.minecraft.wallteleporters.blocks;

import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.registry.LanguageRegistry;
import unwrittenfun.minecraft.wallteleporters.info.BlockInfo;

/**
 * Mod: Wall Teleporters
 * Author: UnwrittenFun
 * License: Minecraft Mod Public License (Version 1.0.1)
 */
public class WTBlocks {

    public static BlockWallTeleporter wallTeleporter;

    public static void init() {
        wallTeleporter = new BlockWallTeleporter(BlockInfo.WT_ID);

        GameRegistry.registerBlock(wallTeleporter, BlockInfo.WT_KEY);
    }

    public static void addNames() {
        LanguageRegistry.addName(wallTeleporter, BlockInfo.WT_NAME);
    }

    public static void registerTileEntity() {
    }
}
