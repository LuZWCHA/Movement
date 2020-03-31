package com.nowandfuture.mod.core.common.gui.mygui.api;

import com.nowandfuture.mod.core.common.gui.mygui.DynamicInventory;
import com.nowandfuture.mod.core.common.gui.mygui.network.ClickDynInventoryCMessage;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import javax.annotation.Nonnull;

public interface IDynInventoryHolder<T extends IDynamicInventory,R extends SerializeWrapper>{
    IDynInventoryHolder EMPTY = new EMPTY();

    /**
     * @return current dyn-inventory
     * for multi-inventory you should return a suitable inventory
     */
    @Nonnull
    T getDynInventory();

    /**
     * @return the id to find the inventory holder in minceraft world
     * default is a wrapped POSITION object
     */
    R getHolderId();


    /**
     * @return the id of the inventory for multi-inventory
     */
    String getInventoryId();

    /**
     * @return custom way to update all inventories in the holder,when sync failed.
     * {@link com.nowandfuture.mod.core.common.gui.mygui.network.ClickDynInventoryCMessageHandler#onMessage(ClickDynInventoryCMessage, MessageContext)}
     */
    NBTTagCompound getFullUpdateTag();

    enum Type{
        TILE,
        CUSTOM
    }

    class EMPTY implements IDynInventoryHolder{
        private EMPTY(){}

        @Nonnull
        @Override
        public IDynamicInventory getDynInventory() {
            return new DynamicInventory();
        }

        @Override
        public SerializeWrapper getHolderId() {
            return null;
        }

        @Override
        public String getInventoryId() {
            return "NULL";
        }

        @Override
        public NBTTagCompound getFullUpdateTag() {
            return new NBTTagCompound();
        }
    }

    static IDynInventoryHolder getHolder(World world,BlockPos id){
        if(id != null) {
            TileEntity tileEntity = world.getTileEntity(id);
            if (tileEntity instanceof IDynInventoryHolder) {
                return (IDynInventoryHolder) tileEntity;
            }
        }

        return IDynInventoryHolder.EMPTY;
    }
}
