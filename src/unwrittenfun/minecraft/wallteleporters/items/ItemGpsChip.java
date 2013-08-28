package unwrittenfun.minecraft.wallteleporters.items;

import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChatMessageComponent;
import net.minecraft.util.Icon;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import unwrittenfun.minecraft.wallteleporters.client.gui.creativetabs.WTCreativeTabs;
import unwrittenfun.minecraft.wallteleporters.info.ItemInfo;
import unwrittenfun.minecraft.wallteleporters.info.ModInfo;

import java.util.List;

/**
 * Mod: Wall Teleporters
 * Author: UnwrittenFun
 * License: Minecraft Mod Public License (Version 1.0.1)
 */
public class ItemGpsChip extends Item {
    private static Icon linkedIcon;

    public ItemGpsChip(int id) {
        super(id);

        setMaxStackSize(1);
        setCreativeTab(WTCreativeTabs.wtTab);
    }

    @Override
    public void registerIcons(IconRegister register) {
        itemIcon = register.registerIcon(ModInfo.TEXTURE_LOCATION + ":" + ItemInfo.GPS_CHIP_ICON);
        linkedIcon = register.registerIcon(ModInfo.TEXTURE_LOCATION + ":" + ItemInfo.GPS_CHIP_LINKED_ICON);
    }

    @Override
    public Icon getIconFromDamage(int damage) {
        if (damage != 0) {
            return linkedIcon;
        }

        return itemIcon;
    }

    @Override
    public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {
        if (!world.isRemote) {
            if (stack.getItemDamage() == 1) {
                if (player.isSneaking()) {
                    stack.getTagCompound().removeTag("LocationData");
                    stack.setItemDamage(0);

                    player.sendChatToPlayer(ChatMessageComponent.func_111066_d("GPS Chip unlinked."));
                }
            } else if (stack.getItemDamage() == 0) {
                NBTTagCompound stackCompound = stack.hasTagCompound() ? stack.getTagCompound() : new NBTTagCompound();

                NBTTagCompound compound = new NBTTagCompound();
                compound.setInteger("worldId", player.worldObj.provider.dimensionId);
                compound.setInteger("locationX", (int) player.posX);
                compound.setInteger("locationY", (int) player.posY);
                compound.setInteger("locationZ", (int) player.posZ);
                stackCompound.setCompoundTag("LocationData", compound);

                stack.setTagCompound(stackCompound);
                stack.setItemDamage(1);

                player.sendChatToPlayer(ChatMessageComponent.func_111066_d("GPS Chip linked."));
            }
        }

        return stack;
    }

    @Override
    public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean par4) {
        if (hasTeleportData(stack)) {
            NBTTagCompound teleportCompound = stack.getTagCompound().getCompoundTag("LocationData");
            list.add("World Id: " + teleportCompound.getInteger("worldId"));
            list.add("X: " + teleportCompound.getInteger("locationX"));
            list.add("Y: " + teleportCompound.getInteger("locationY"));
            list.add("Z: " + teleportCompound.getInteger("locationZ"));
        }
    }

    @Override
    public String getUnlocalizedName() {
        return "item." + ItemInfo.GPS_CHIP_UNLOCAL_NAME + "1";
    }

    @Override
    public String getUnlocalizedName(ItemStack stack) {
        return "item." + ItemInfo.GPS_CHIP_UNLOCAL_NAME + stack.getItemDamage();
    }

    public boolean hasTeleportData(ItemStack stack) {
        return stack.hasTagCompound() && stack.getTagCompound().hasKey("LocationData");
    }
}
