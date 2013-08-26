package unwrittenfun.minecraft.wallteleporters.blocks.multiblocks;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagIntArray;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import unwrittenfun.minecraft.wallteleporters.blocks.tileentities.TileEntityWallTeleporter;

import java.util.ArrayList;

/**
 * Mod: Wall Teleporters
 * Author: UnwrittenFun
 * License: Minecraft Mod Public License (Version 1.0.1)
 */
public class MultiblockWallTeleporter {
    public ArrayList<TileEntityWallTeleporter> teleporters;
    public TileEntityWallTeleporter            controller;

    private ArrayList<int[]> blocks;

    public MultiblockWallTeleporter() {
        teleporters = new ArrayList<TileEntityWallTeleporter>();
    }

    public MultiblockWallTeleporter(TileEntityWallTeleporter teleporter) {
        this();

        add(teleporter);
    }

    public boolean hasController() {
        return controller != null;
    }

    public boolean isController(TileEntityWallTeleporter teleporter) {
        return controller == teleporter;
    }

    public int count() {
        return teleporters.size();
    }

    public void add(TileEntityWallTeleporter teleporter) {
        if (!hasController()) {
            controller = teleporter;
        }

        if (!teleporters.contains(teleporter)) {
            teleporters.add(teleporter);
        }

        teleporter.multiblock = this;
    }

    public void recalculate() {
        for (TileEntityWallTeleporter teleporter : teleporters) {
            new MultiblockWallTeleporter(teleporter);
        }

        for (TileEntityWallTeleporter teleporter : teleporters) {
            teleporter.notifyNeighboursOfConnectionChanged();
        }
    }

    public void writeToNBT(NBTTagCompound compound) {
        NBTTagCompound wtCompound = new NBTTagCompound();

        NBTTagCompound wtBlocksCompound = new NBTTagCompound();
        for (TileEntityWallTeleporter teleporter : teleporters) {
            wtBlocksCompound.setIntArray("block" + teleporters.indexOf(teleporter),
                    new int[]{ teleporter.xCoord, teleporter.yCoord, teleporter.zCoord });
        }
        wtCompound.setCompoundTag("WTBlocks", wtBlocksCompound);

        compound.setCompoundTag("WTMultiblock", wtCompound);
    }

    public void readFromNBT(NBTTagCompound compound) {
        NBTTagCompound wtCompound = compound.getCompoundTag("WTMultiblock");

        NBTTagCompound wtBlocksCompound = wtCompound.getCompoundTag("WTBlocks");
        blocks = new ArrayList<int[]>();
        for (Object tag : wtBlocksCompound.getTags()) {
            if (tag instanceof NBTTagIntArray) {
                blocks.add(((NBTTagIntArray) tag).intArray);
            }
        }
    }

    public void init(World world) {
        if (blocks != null) {
            for (int[] blockCoords : blocks) {
                TileEntity tileEntity = world.getBlockTileEntity(blockCoords[0], blockCoords[1], blockCoords[2]);

                if (tileEntity instanceof TileEntityWallTeleporter) {
                    TileEntityWallTeleporter teleporter = (TileEntityWallTeleporter) tileEntity;

                    add(teleporter);
                    blocks = null;
                }
            }
        }
    }

    public static void merge(MultiblockWallTeleporter multiblock1, MultiblockWallTeleporter multiblock2) {
        if (multiblock1 != multiblock2) {
            for (TileEntityWallTeleporter teleporter : multiblock2.teleporters) {
                multiblock1.add(teleporter);
            }
        }
    }
}
