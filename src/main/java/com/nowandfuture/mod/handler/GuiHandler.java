package com.nowandfuture.mod.handler;

import com.nowandfuture.mod.core.common.entities.TileEntityConstructor;
import com.nowandfuture.mod.core.common.entities.TileEntityCoreModule;
import com.nowandfuture.mod.core.common.entities.TileEntitySimplePlayer;
import com.nowandfuture.mod.core.common.entities.TileEntityTimelineEditor;
import com.nowandfuture.mod.core.common.gui.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;

import javax.annotation.Nullable;

public class GuiHandler implements IGuiHandler {
    @Nullable
    @Override
    public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        TileEntity tile = world.getTileEntity(new BlockPos(x, y, z));
        if(tile != null){
            switch (ID){
                case GuiConstructor.GUI_ID:
                    return new ContainerConstructor(player.inventory, (TileEntityConstructor) tile);
                case GuiTimelineEditor.GUI_ID:
                    return new ContainerAnmEditor(player.inventory, (TileEntityTimelineEditor) tile);
                case GuiModule.GUI_ID:
                    return new ContainerModule(player.inventory, (TileEntityCoreModule) tile);
                case GuiMediaPlayer.GUI_ID:
                    return new ContainerSimplePlayer();
            }
        }
        return null;
    }

    @Nullable
    @Override
    public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        TileEntity tile = world.getTileEntity(new BlockPos(x, y, z));
        if(tile != null){
            switch (ID){
                case GuiConstructor.GUI_ID:
                    return new GuiConstructor(player.inventory, (TileEntityConstructor) tile);
                case GuiTimelineEditor.GUI_ID:
                    return new GuiTimelineEditor(player.inventory, (TileEntityTimelineEditor) tile);
                case GuiModule.GUI_ID:
                    return new GuiModule(player.inventory, (TileEntityCoreModule) tile);
                case GuiMediaPlayer.GUI_ID:
                    return new GuiMediaPlayer((TileEntitySimplePlayer) tile);
            }
        }
        return null;
    }
}
