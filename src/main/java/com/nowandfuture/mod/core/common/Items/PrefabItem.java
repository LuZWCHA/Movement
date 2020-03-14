package com.nowandfuture.mod.core.common.Items;

import com.nowandfuture.mod.core.prefab.AbstractPrefab;
import joptsimple.internal.Strings;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.IItemPropertyGetter;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;

import static com.nowandfuture.mod.Movement.MODID;

public class PrefabItem extends Item {

    public PrefabItem() {
        super();
        this.setMaxStackSize(1);
        this.setMaxDamage(0);
        this.setHasSubtypes(true);
        addPropertyOverride(new ResourceLocation(MODID, "ready"), new IItemPropertyGetter() {
            @Override
            public float apply(ItemStack stack, @Nullable World worldIn, @Nullable EntityLivingBase entityIn) {
                return isReady(stack.getTagCompound()) ? 1:0;//1 - isReady
            }
        });
    }

    @Override
    public String getTranslationKey(ItemStack stack) {
        NBTTagCompound nbt = stack.getTagCompound();

        boolean isReady = isReady(nbt);
        return super.getTranslationKey() + (isReady ? ".complete":".empty");
    }

    public boolean isReady(NBTTagCompound nbt){
        boolean isReady = nbt != null &&
                nbt.hasKey(AbstractPrefab.NBT_CONSTRUCT_READY) &&
                nbt.getBoolean(AbstractPrefab.NBT_CONSTRUCT_READY);
        return isReady;
    }

    public static String getPrefabName(ItemStack stack){
        NBTTagCompound compound = stack.getTagCompound();
        if(compound != null && compound.hasKey(AbstractPrefab.NBT_PREFAB_NAME)){
            return compound.getString(AbstractPrefab.NBT_PREFAB_NAME);
        }
        return Strings.EMPTY;
    }

    public static void setPrefabName(ItemStack stack,String name){

        if(stack.getTagCompound() == null) {
            NBTTagCompound compound = new NBTTagCompound();
            compound.setString(AbstractPrefab.NBT_PREFAB_NAME, name);
            stack.setTagCompound(compound);
        }else {
            stack.getTagCompound().setString(AbstractPrefab.NBT_PREFAB_NAME, name);
        }
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        NBTTagCompound nbt = stack.getTagCompound();
        String name = Strings.EMPTY;
        if(nbt != null){
            if(nbt.hasKey(AbstractPrefab.NBT_PREFAB_NAME)){
                name = nbt.getString(AbstractPrefab.NBT_PREFAB_NAME);
            }
            tooltip.add(name.isEmpty() ? "NoName" : name);

            if(nbt.hasKey(AbstractPrefab.NBT_SIZE_X)) {
                Vec3i size = new Vec3i(
                        nbt.getInteger(AbstractPrefab.NBT_SIZE_X),
                        nbt.getInteger(AbstractPrefab.NBT_SIZE_Y),
                        nbt.getInteger(AbstractPrefab.NBT_SIZE_Z)
                );
                tooltip.add(I18n.format("movement.tooltip.prefab.size")+
                        ":"+
                        size.getX() + "x" + size.getY() + "x" + size.getZ());
            }
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
