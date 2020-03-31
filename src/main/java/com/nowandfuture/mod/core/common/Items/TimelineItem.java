package com.nowandfuture.mod.core.common.Items;

import com.nowandfuture.mod.core.transformers.animation.Timeline;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;

public class TimelineItem extends Item {

    public TimelineItem(){
        super();
        this.setMaxStackSize(1);
        this.setMaxDamage(0);
        this.setHasSubtypes(true);
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);
        NBTTagCompound compound = stack.getTagCompound();
        if(compound != null){
            if(compound.hasKey(Timeline.NBT_ANM_LINE_ENABLE))
                tooltip.add(I18n.format("movement.tooltip.timeline.enable") + compound.getBoolean(Timeline.NBT_ANM_LINE_ENABLE));
            if(compound.hasKey(Timeline.NBT_ANM_LINE_MODE))
                tooltip.add(I18n.format("movement.tooltip.timeline.mode") + compound.getInteger(Timeline.NBT_ANM_LINE_MODE));
            if(compound.hasKey(Timeline.NBT_ANM_LINE_TOTAL))
                tooltip.add(I18n.format("movement.tooltip.timeline.duration") + compound.getLong(Timeline.NBT_ANM_LINE_TOTAL));
            if(compound.hasKey(Timeline.NBT_ANM_LINE_STEP))
                tooltip.add(I18n.format("movement.tooltip.timeline.step") + compound.getInteger(Timeline.NBT_ANM_LINE_STEP));

        }
    }

    @Override
    public void setDamage(ItemStack stack, int damage) {
        super.setDamage(stack, 0);
    }

    @Override
    public boolean canDestroyBlockInCreative(World world, BlockPos pos, ItemStack stack, EntityPlayer player) {
        return false;
    }
}
