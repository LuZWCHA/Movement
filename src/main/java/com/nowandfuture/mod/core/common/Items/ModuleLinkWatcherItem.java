package com.nowandfuture.mod.core.common.Items;

import com.nowandfuture.mod.Movement;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.common.util.EnumHelper;

import javax.annotation.Nullable;
import java.util.List;

public class ModuleLinkWatcherItem extends ItemArmor {
    private static final ResourceLocation TEXTURE = new ResourceLocation(Movement.MODID,"textures/items/link_watcher.png");

    public ModuleLinkWatcherItem() {
        super(Material.LINK_WATCHER, 0, EntityEquipmentSlot.HEAD);
        this.setMaxStackSize(1);
        this.setMaxDamage(0);
        this.setHasSubtypes(true);
    }

    public static @Nullable
    ItemStack getLinkWatcher(@Nullable EntityPlayer player) {
        if (player == null)
            return null;
        ItemStack helm = player.getItemStackFromSlot(EntityEquipmentSlot.HEAD);
        if (!helm.isEmpty() && helm.getItem() instanceof ModuleLinkWatcherItem)
            return helm;
        return null;
    }

    public static boolean isPlayerWearing(EntityPlayer player) {
        ItemStack helm = player.getItemStackFromSlot(EntityEquipmentSlot.HEAD);
        return !helm.isEmpty() && helm.getItem() instanceof ModuleLinkWatcherItem;
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);
    }

    @Nullable
    @Override
    public String getArmorTexture(ItemStack stack, Entity entity, EntityEquipmentSlot slot, String type) {
        return TEXTURE.toString();
    }

    public static class Material{
        public static ArmorMaterial LINK_WATCHER = EnumHelper.addArmorMaterial("link_watcher","????",20,new int[]{0,0,0,1},15, SoundEvents.ITEM_ARMOR_EQUIP_LEATHER,0);
    }
}
