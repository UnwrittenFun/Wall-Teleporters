package unwrittenfun.minecraft.wallteleporters.handlers;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.network.IPacketHandler;
import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.common.network.Player;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.packet.Packet250CustomPayload;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import unwrittenfun.minecraft.wallteleporters.blocks.multiblocks.MultiblockWallTeleporter;
import unwrittenfun.minecraft.wallteleporters.blocks.tileentities.TileEntityWallTeleporter;
import unwrittenfun.minecraft.wallteleporters.gui.containers.ContainerWallTeleporter;
import unwrittenfun.minecraft.wallteleporters.info.ModInfo;
import unwrittenfun.minecraft.wallteleporters.items.ItemDebugger;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Mod: Wall Teleporters
 * Author: UnwrittenFun
 * License: Minecraft Mod Public License (Version 1.0.1)
 */
public class PacketHandler implements IPacketHandler {
    @Override
    public void onPacketData(INetworkManager manager, Packet250CustomPayload packet, Player player) {
        ByteArrayDataInput reader = ByteStreams.newDataInput(packet.data);
        EntityPlayer entityPlayer = (EntityPlayer) player;
        byte packetId = reader.readByte();

        switch (packetId) {
            case 0:
                onNewMaskPacket(reader, entityPlayer.worldObj);
                break;
            case 1:
                onRequestMaskPacket(reader, entityPlayer.worldObj, player);
                break;
            case 2:
                onDebuggerUsePacket(reader, entityPlayer);
                break;
            case 3:
                onDestinationPacket(reader, entityPlayer);
                break;
            case 4:
                onRequestMultiblockInfoPacket(reader, player);
                break;
            case 5:
                onButtonPacket(reader, entityPlayer);
                break;
            case 6:
                onLockedPacket(reader, entityPlayer);
                break;
        }
    }

    public static void requestMaskPacket(byte id, TileEntityWallTeleporter teleporter) {
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        DataOutputStream dataStream = new DataOutputStream(byteStream);

        try {
            dataStream.writeByte((byte) 1);
            dataStream.writeByte(id);
            dataStream.writeInt(teleporter.xCoord);
            dataStream.writeInt(teleporter.yCoord);
            dataStream.writeInt(teleporter.zCoord);

            PacketDispatcher.sendPacketToServer(PacketDispatcher.getPacket(ModInfo.CHANNEL, byteStream.toByteArray()));
        } catch (IOException ex) {
            System.err.append("[Wall Teleporters] Failed to request mask (" + id + ") packet");
        }
    }

    public void onRequestMaskPacket(ByteArrayDataInput reader, World world, Player player) {
        int id = reader.readByte();
        int x = reader.readInt();
        int y = reader.readInt();
        int z = reader.readInt();
        TileEntity tileEntity = world.getBlockTileEntity(x, y, z);

        switch (id) {
            case 0:
                if (tileEntity instanceof TileEntityWallTeleporter) {
                    TileEntityWallTeleporter teleporter = ((TileEntityWallTeleporter) tileEntity);

                    sendNewMaskPacket((byte) 0, teleporter, player);
                }
                break;
        }
    }

    public static void sendNewMaskPacket(byte id, TileEntityWallTeleporter teleporter, Player player) {
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        DataOutputStream dataStream = new DataOutputStream(byteStream);

        try {
            dataStream.writeByte((byte) 0);
            dataStream.writeByte(id);
            dataStream.writeInt(teleporter.mask[0]);
            dataStream.writeByte(teleporter.mask[1]);
            dataStream.writeInt(teleporter.xCoord);
            dataStream.writeInt(teleporter.yCoord);
            dataStream.writeInt(teleporter.zCoord);

            if (player == null) {
                PacketDispatcher.sendPacketToAllInDimension(
                        PacketDispatcher.getPacket(ModInfo.CHANNEL, byteStream.toByteArray()),
                        teleporter.worldObj.provider.dimensionId);
            } else {
                PacketDispatcher.sendPacketToPlayer(
                        PacketDispatcher.getPacket(ModInfo.CHANNEL, byteStream.toByteArray()), player);
            }
        } catch (IOException ex) {
            System.err.append("[Wall Teleporters] Failed to send new mask (" + id + ") packet");
        }
    }

    public void onNewMaskPacket(ByteArrayDataInput reader, World world) {
        int id = reader.readByte();
        int maskId = reader.readInt();
        int maskMeta = reader.readByte();
        int x = reader.readInt();
        int y = reader.readInt();
        int z = reader.readInt();
        TileEntity tileEntity = world.getBlockTileEntity(x, y, z);

        switch (id) {
            case 0:
                if (tileEntity instanceof TileEntityWallTeleporter) {
                    TileEntityWallTeleporter teleporter = ((TileEntityWallTeleporter) tileEntity);

                    teleporter.setMask(maskId, maskMeta);
                    world.markBlockForRenderUpdate(x, y, z);
                }
                break;
        }
    }

    public void onDebuggerUsePacket(ByteArrayDataInput reader, EntityPlayer player) {
        int x = reader.readInt();
        int y = reader.readInt();
        int z = reader.readInt();
        TileEntity tileEntity = player.worldObj.getBlockTileEntity(x, y, z);

        if (tileEntity instanceof TileEntityWallTeleporter) {
            TileEntityWallTeleporter teleporter = ((TileEntityWallTeleporter) tileEntity);

            ItemDebugger
                    .chatMultiblockInfo(player, teleporter, FMLCommonHandler.instance().getEffectiveSide().isClient());
        }
    }

    public static void sendDebuggerUsePacket(int x, int y, int z) {
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        DataOutputStream dataStream = new DataOutputStream(byteStream);

        try {
            dataStream.writeByte((byte) 2);
            dataStream.writeInt(x);
            dataStream.writeInt(y);
            dataStream.writeInt(z);

            PacketDispatcher.sendPacketToServer(PacketDispatcher.getPacket(ModInfo.CHANNEL, byteStream.toByteArray()));
        } catch (IOException ex) {
            System.err.append("[Wall Teleporters] Failed to send debugger use packet");
        }
    }

    public void onDestinationPacket(ByteArrayDataInput reader, EntityPlayer player) {
        int cWorldId = reader.readInt();
        int cX = reader.readInt();
        int cY = reader.readInt();
        int cZ = reader.readInt();
        int dWorldId = reader.readInt();
        int dX = reader.readInt();
        int dY = reader.readInt();
        int dZ = reader.readInt();

        if (player.worldObj.provider.dimensionId == cWorldId) {
            TileEntity tileEntity = player.worldObj.getBlockTileEntity(cX, cY, cZ);

            if (tileEntity instanceof TileEntityWallTeleporter) {
                TileEntityWallTeleporter teleporter = ((TileEntityWallTeleporter) tileEntity);

                teleporter.multiblock.setDestination(dWorldId, dX, dY, dZ);
            }
        }
    }

    public static void sendDestinationPacket(MultiblockWallTeleporter multiblock, Player player) {
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        DataOutputStream dataStream = new DataOutputStream(byteStream);

        try {
            dataStream.writeByte((byte) 3);
            dataStream.writeInt(multiblock.controller.worldObj.provider.dimensionId);
            dataStream.writeInt(multiblock.controller.xCoord);
            dataStream.writeInt(multiblock.controller.yCoord);
            dataStream.writeInt(multiblock.controller.zCoord);
            dataStream.writeInt(multiblock.destinationWorldId);
            dataStream.writeInt(multiblock.destinationX);
            dataStream.writeInt(multiblock.destinationY);
            dataStream.writeInt(multiblock.destinationZ);

            if (player == null) {
                PacketDispatcher.sendPacketToAllInDimension(
                        PacketDispatcher.getPacket(ModInfo.CHANNEL, byteStream.toByteArray()),
                        multiblock.controller.worldObj.provider.dimensionId);
            } else {
                PacketDispatcher.sendPacketToPlayer(
                        PacketDispatcher.getPacket(ModInfo.CHANNEL, byteStream.toByteArray()), player);
            }
        } catch (IOException ex) {
            System.err.append("[Wall Teleporters] Failed to send destination packet");
        }
    }

    public void onRequestMultiblockInfoPacket(ByteArrayDataInput reader, Player player) {
        int worldId = reader.readInt();
        World world = DimensionManager.getWorld(worldId);
        int x = reader.readInt();
        int y = reader.readInt();
        int z = reader.readInt();

        TileEntity tileEntity = world.getBlockTileEntity(x, y, z);

        if (tileEntity instanceof TileEntityWallTeleporter) {
            TileEntityWallTeleporter teleporter = ((TileEntityWallTeleporter) tileEntity);

            sendDestinationPacket(teleporter.multiblock, player);
            sendLockedPacket(teleporter.multiblock, player);
        }
    }

    public static void requestMultiblockInfoPacket(TileEntityWallTeleporter teleporter) {
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        DataOutputStream dataStream = new DataOutputStream(byteStream);

        try {
            dataStream.writeByte((byte) 4);
            dataStream.writeInt(teleporter.worldObj.provider.dimensionId);
            dataStream.writeInt(teleporter.xCoord);
            dataStream.writeInt(teleporter.yCoord);
            dataStream.writeInt(teleporter.zCoord);

            PacketDispatcher.sendPacketToServer(PacketDispatcher.getPacket(ModInfo.CHANNEL, byteStream.toByteArray()));
        } catch (IOException ex) {
            System.err.append("[Wall Teleporters] Failed to send destination packet");
        }
    }

    public void onButtonPacket(ByteArrayDataInput reader, EntityPlayer player) {
        int guiId = reader.readByte();
        int buttonId = reader.readByte();
        Container container = player.openContainer;

        switch (guiId) {
            case 0:
                switch (buttonId) {
                    case 0:
                        if (container instanceof ContainerWallTeleporter) {
                            ((ContainerWallTeleporter) container).multiblock.clearDestination();
                        }
                        break;
                    case 1:
                        if (container instanceof ContainerWallTeleporter) {
                            ((ContainerWallTeleporter) container).multiblock.setLocked(false);
                        }
                        break;
                    case 2:
                        if (container instanceof ContainerWallTeleporter) {
                            ((ContainerWallTeleporter) container).multiblock.setLocked(true);
                        }
                        break;
                }
                break;
        }
    }

    public static void sendButtonPacket(byte guiId, byte buttonId) {
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        DataOutputStream dataStream = new DataOutputStream(byteStream);

        try {
            dataStream.writeByte((byte) 5);
            dataStream.writeByte(guiId);
            dataStream.writeByte(buttonId);

            PacketDispatcher.sendPacketToServer(PacketDispatcher.getPacket(ModInfo.CHANNEL, byteStream.toByteArray()));
        } catch (IOException ex) {
            System.err.append("[Wall Teleporters] Failed to send button (" + guiId + ", " + buttonId + ") packet");
        }
    }

    public void onLockedPacket(ByteArrayDataInput reader, EntityPlayer player) {
        int cWorldId = reader.readInt();
        int cX = reader.readInt();
        int cY = reader.readInt();
        int cZ = reader.readInt();
        boolean locked = reader.readBoolean();

        if (player.worldObj.provider.dimensionId == cWorldId) {
            TileEntity tileEntity = player.worldObj.getBlockTileEntity(cX, cY, cZ);

            if (tileEntity instanceof TileEntityWallTeleporter) {
                TileEntityWallTeleporter teleporter = ((TileEntityWallTeleporter) tileEntity);

                teleporter.multiblock.setLocked(locked);
            }
        }
    }

    public static void sendLockedPacket(MultiblockWallTeleporter multiblock, Player player) {
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        DataOutputStream dataStream = new DataOutputStream(byteStream);

        try {
            dataStream.writeByte((byte) 6);
            dataStream.writeInt(multiblock.controller.worldObj.provider.dimensionId);
            dataStream.writeInt(multiblock.controller.xCoord);
            dataStream.writeInt(multiblock.controller.yCoord);
            dataStream.writeInt(multiblock.controller.zCoord);
            dataStream.writeBoolean(multiblock.isLocked());

            if (player == null) {
                PacketDispatcher.sendPacketToAllInDimension(
                        PacketDispatcher.getPacket(ModInfo.CHANNEL, byteStream.toByteArray()),
                        multiblock.controller.worldObj.provider.dimensionId);
            } else {
                PacketDispatcher.sendPacketToPlayer(
                        PacketDispatcher.getPacket(ModInfo.CHANNEL, byteStream.toByteArray()), player);
            }
        } catch (IOException ex) {
            System.err.append("[Wall Teleporters] Failed to send locked packet");
        }
    }
}
