package unwrittenfun.minecraft.wallteleporters.handlers;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import com.sun.istack.internal.Nullable;
import cpw.mods.fml.common.network.IPacketHandler;
import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.common.network.Player;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.packet.Packet250CustomPayload;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import unwrittenfun.minecraft.wallteleporters.blocks.tileentities.TileEntityWallTeleporter;
import unwrittenfun.minecraft.wallteleporters.info.ModInfo;

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

    public static void sendNewMaskPacket(byte id, TileEntityWallTeleporter teleporter, @Nullable Player player) {
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
}
