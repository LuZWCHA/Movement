package com.nowandfuture.mod.core.common.gui.mygui.api;

import com.nowandfuture.mod.core.common.gui.mygui.DynamicInventory;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nonnull;

public interface IDynInventoryHolder<T extends IDynamicInventory,R extends SerializeWrapper>{
    IDynInventoryHolder EMPTY = new EMPTY();

    @Nonnull
    T getDynInventory();

    R getHolderId();

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
