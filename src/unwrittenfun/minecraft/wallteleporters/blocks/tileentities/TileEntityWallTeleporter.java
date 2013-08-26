package unwrittenfun.minecraft.wallteleporters.blocks.tileentities;

import cpw.mods.fml.common.FMLCommonHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
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
    public MultiblockWallTeleporter multiblock;
    public  int[]   mask   = new int[]{ 0, 0 };
    private boolean loaded = false;

    public void notifyNeighboursOfConnectionChanged() {
        boolean alone = true;
        for (ForgeDirection direction : ForgeDirection.VALID_DIRECTIONS) {
            TileEntity tileEntity = worldObj.getBlockTileEntity(xCoord + direction.offsetX, yCoord + direction.offsetY,
                    zCoord + direction.offsetZ);

            if (tileEntity instanceof TileEntityWallTeleporter) {
                alone = false;
                ((TileEntityWallTeleporter) tileEntity).onConnectionChangedFromDirection(direction);
            }
        }

        if (alone) {
            new MultiblockWallTeleporter(this);
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

                if (teleporter.multiblock == null) {
                    multiblock.add(teleporter);
                } else {
                    MultiblockWallTeleporter.merge(multiblock, teleporter.multiblock);
                }
            }
        }
    }

    public void setMask(int id, int meta) {
        if (id == BlockInfo.WT_ID) {
            id = 0;
        }
        mask[0] = id;
        mask[1] = meta;

        if (!worldObj.isRemote) {
            PacketHandler.sendNewMaskPacket((byte) 0, this, null);
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);

        compound.setInteger("MaskId", mask[0]);
        compound.setInteger("MaskMeta", mask[1]);

        NBTTagList items = new NBTTagList();

        for (int i = 0; i < getSizeInventory(); i++) {
            ItemStack stack = getStackInSlot(i);

            if (stack != null) {
                NBTTagCompound item = new NBTTagCompound();
                item.setByte("Slot", (byte) i);
                stack.writeToNBT(item);
                items.appendTag(item);
            }
        }

        compound.setTag("Items", items);

        if (multiblock.isController(this)) {
            multiblock.writeToNBT(compound);
        }
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);

        mask[0] = compound.getInteger("MaskId");
        mask[1] = compound.getInteger("MaskMeta");

        NBTTagList items = compound.getTagList("Items");

        for (int i = 0; i < items.tagCount(); i++) {
            NBTTagCompound item = (NBTTagCompound) items.tagAt(i);
            int slot = item.getByte("Slot");

            if (slot >= 0 && slot < getSizeInventory()) {
                setInventorySlotContents(slot, ItemStack.loadItemStackFromNBT(item));
            }
        }

        if (compound.hasKey("WTMultiblock")) {
            new MultiblockWallTeleporter(this);
            multiblock.readFromNBT(compound);
        }
    }

    @Override
    public void updateEntity() {
        if (!loaded && hasWorldObj() && !worldObj.isRemote) {
            loaded = true;

            if (multiblock != null) {
                multiblock.init(worldObj);
            }
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

        if (!worldObj.isRemote) {
            notifyNeighboursOfConnectionChanged();
        }
    }

//    @Override
//    public void onChunkUnload() {
//        Chunk chunk = worldObj.getChunkFromBlockCoords(xCoord, zCoord);
//        System.out.println("Chunk loaded: " + worldObj.getChunkProvider().loadChunk(chunk.xPosition, chunk.zPosition).isChunkLoaded);
//    }


    // Inventory implementation

    private ItemStack[] items = new ItemStack[2];

    @Override
    public int getSizeInventory() {
        return items.length;
    }

    @Override
    public ItemStack getStackInSlot(int i) {
        return items[i];
    }

    @Override
    public ItemStack decrStackSize(int i, int count) {
        ItemStack stack = getStackInSlot(i);

        if (stack != null) {
            if (stack.stackSize <= count) {
                setInventorySlotContents(i, null);
            } else {
                stack = stack.splitStack(count);
                onInventoryChanged();
            }
        }

        return stack;
    }

    @Override
    public ItemStack getStackInSlotOnClosing(int i) {
        ItemStack stack = getStackInSlot(i);
        setInventorySlotContents(i, null);
        return stack;
    }

    @Override
    public void setInventorySlotContents(int i, ItemStack itemstack) {
        items[i] = itemstack;
        onInventoryChanged();
    }

    @Override
    public String getInvName() {
        return BlockInfo.WT_NAME;
    }

    @Override
    public boolean isInvNameLocalized() {
        return true;
    }

    @Override
    public int getInventoryStackLimit() {
        return 1;
    }

    @Override
    public boolean isUseableByPlayer(EntityPlayer player) {
        return player.getDistanceSq(xCoord + 0.5, yCoord + 0.5, zCoord + 0.5) <= 64;
    }

    @Override
    public void openChest() {
    }

    @Override
    public void closeChest() {
    }

    @Override
    public boolean isItemValidForSlot(int i, ItemStack stack) {
        return true;
    }
}
