package unwrittenfun.minecraft.wallteleporters.blocks.tileentities;

import cpw.mods.fml.common.FMLCommonHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import unwrittenfun.minecraft.wallteleporters.blocks.multiblocks.MultiblockWallTeleporter;
import unwrittenfun.minecraft.wallteleporters.handlers.PacketHandler;
import unwrittenfun.minecraft.wallteleporters.info.BlockInfo;

/**
 * Mod: Wall Teleporters
 * Author: UnwrittenFun
 * License: Minecraft Mod Public License (Version 1.0.1)
 */
public class TileEntityWallTeleporter extends TileEntity implements IInventory {
    public  MultiblockWallTeleporter multiblock = new MultiblockWallTeleporter(this);
    public  int[]                    mask       = new int[]{ 0, 0 };
    private boolean                  loaded     = false;

    public void notifyNeighboursOfConnectionChanged() {
        for (ForgeDirection direction : ForgeDirection.VALID_DIRECTIONS) {
            TileEntity tileEntity = worldObj.getBlockTileEntity(xCoord + direction.offsetX, yCoord + direction.offsetY,
                    zCoord + direction.offsetZ);

            if (tileEntity instanceof TileEntityWallTeleporter) {
                ((TileEntityWallTeleporter) tileEntity).onConnectionChangedFromDirection(direction);
            }
        }
    }

    public void onConnectionChangedFromDirection(ForgeDirection direction) {
        ForgeDirection fromDirection = direction.getOpposite();

        TileEntity tileEntity = worldObj.getBlockTileEntity(xCoord + fromDirection.offsetX,
                yCoord + fromDirection.offsetY, zCoord + fromDirection.offsetZ);

        if (tileEntity == null || tileEntity.isInvalid()) { // Block was removed.
            multiblock.recalculate();
        } else { // Block was added
            if (tileEntity instanceof TileEntityWallTeleporter) {
                TileEntityWallTeleporter teleporter = (TileEntityWallTeleporter) tileEntity;

                MultiblockWallTeleporter.merge(multiblock, teleporter.multiblock);
            }
        }
    }

    public void setMask(int id, int meta) {
        if ((!worldObj.isRemote && !multiblock.isLocked()) || worldObj.isRemote) {
            if (id == BlockInfo.WT_ID) {
                id = 0;
            }
            mask[0] = id;
            mask[1] = meta;

            if (!worldObj.isRemote) {
                PacketHandler.sendNewMaskPacket((byte) 0, this, null);
            }
        }
    }

    public void teleportPlayer(EntityPlayerMP player) {
        if (multiblock.hasDestination()) {
            multiblock.teleportToDestination(player);
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);

        compound.setInteger("MaskId", mask[0]);
        compound.setInteger("MaskMeta", mask[1]);

        if (multiblock.isController(this)) {
            multiblock.writeToNBT(compound);
        }
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);

        mask[0] = compound.getInteger("MaskId");
        mask[1] = compound.getInteger("MaskMeta");

        if (compound.hasKey("WTMultiblock")) {
            new MultiblockWallTeleporter(this);
            multiblock.readFromNBT(compound);
        }
    }

    private int     countdown      = 10;
    private boolean startCountdown = false;

    @Override
    public void updateEntity() {
        if (!loaded && hasWorldObj()) {
            loaded = true;

            if (!worldObj.isRemote && multiblock != null) {
                multiblock.init(worldObj);
            } else if (worldObj.isRemote) {
                notifyNeighboursOfConnectionChanged();
                startCountdown = true;
            }
        }

        if (countdown != -1 && startCountdown) {
            countdown--;
        }

        if (countdown == 0 && multiblock.isController(this)) {
            PacketHandler.requestMultiblockInfoPacket(this);
        }
    }

    @Override
    public void validate() {
        super.validate();

        if (FMLCommonHandler.instance().getEffectiveSide().isClient()) {
            PacketHandler.requestMaskPacket((byte) 0, this);
        }
    }

    @Override
    public void invalidate() {
        super.invalidate();

        notifyNeighboursOfConnectionChanged();
    }


    @Override
    public int getSizeInventory() {
        return multiblock.getSizeInventory();
    }

    @Override
    public ItemStack getStackInSlot(int i) {
        return multiblock.getStackInSlot(i);
    }

    @Override
    public ItemStack decrStackSize(int i, int j) {
        return multiblock.decrStackSize(i, j);
    }

    @Override
    public ItemStack getStackInSlotOnClosing(int i) {
        return multiblock.getStackInSlotOnClosing(i);
    }

    @Override
    public void setInventorySlotContents(int i, ItemStack stack) {
        multiblock.setInventorySlotContents(i, stack);
    }

    @Override
    public String getInvName() {
        return multiblock.getInvName();
    }

    @Override
    public boolean isInvNameLocalized() {
        return multiblock.isInvNameLocalized();
    }

    @Override
    public int getInventoryStackLimit() {
        return multiblock.getInventoryStackLimit();
    }

    @Override
    public boolean isUseableByPlayer(EntityPlayer player) {
        return multiblock.isUseableByPlayer(player);
    }

    @Override
    public void openChest() {
    }

    @Override
    public void closeChest() {
    }

    @Override
    public boolean isItemValidForSlot(int i, ItemStack stack) {
        return multiblock.isItemValidForSlot(i, stack);
    }
}
