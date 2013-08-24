package unwrittenfun.minecraft.wallteleporters.blocks;

import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;
import unwrittenfun.minecraft.wallteleporters.client.gui.creativetabs.WTCreativeTabs;
import unwrittenfun.minecraft.wallteleporters.info.BlockInfo;
import unwrittenfun.minecraft.wallteleporters.info.ModInfo;

/**
 * Mod: Wall Teleporters
 * Author: UnwrittenFun
 * License: Minecraft Mod Public License (Version 1.0.1)
 */
public class BlockWallTeleporter extends BlockContainer {
    public BlockWallTeleporter(int id) {
        super(id, Material.iron);

        setUnlocalizedName(BlockInfo.WT_UNLOCAL_NAME);
        setHardness(2F);
        setCreativeTab(WTCreativeTabs.wtTab);
    }

    @Override
    public void registerIcons(IconRegister register) {
        blockIcon = register.registerIcon(ModInfo.TEXTURE_LOCATION + ":" + BlockInfo.WT_ICON_NAME);
    }

    @Override
    public TileEntity createNewTileEntity(World world) {
        return null;
    }

    @Override
    public AxisAlignedBB getCollisionBoundingBoxFromPool(World world, int x, int y, int z) {
        return AxisAlignedBB.getAABBPool().getAABB(0, 0, 0, 0, 0, 0);
    }

    @Override
    public void onEntityCollidedWithBlock(World world, int x, int y, int z, Entity entity) {
        if (!world.isRemote) {
            if (entity instanceof EntityPlayerMP) {
                EntityPlayerMP player = (EntityPlayerMP) entity;
                player.playerNetServerHandler.setPlayerLocation(0, 60, 0, player.rotationYaw, player.rotationPitch);
            }
        }
    }
}
