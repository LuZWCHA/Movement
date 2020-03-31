package com.nowandfuture.mod.core.common.gui.mygui.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.inventory.ClickType;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.common.util.PacketUtil;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.io.IOException;

public class ClickDynInventoryCMessage implements IMessage {

    private String inventoryId;
    /** The id of the window which was clicked. 0 for player inventory. */
    private int windowId;
    /** Id of the clicked slot */
    private int slotId;
    /** Button used */
    private int packedClickData;
    /** A unique number for the action, used for transaction handling */
    private short actionNumber;
    /** The item stack present in the slot */
    private ItemStack clickedItem = ItemStack.EMPTY;
    /** Inventory operation mode */
    private ClickType mode;

    public ClickDynInventoryCMessage()
    {
    }

    @SideOnly(Side.CLIENT)
    public ClickDynInventoryCMessage(int windowIdIn, String inventoryId, ItemStack clickedItem, int slotIdIn, int usedButtonIn, ClickType modeIn, short actionNumberIn)
    {
        this.inventoryId = inventoryId;
        this.windowId = windowIdIn;
        this.slotId = slotIdIn;
        this.packedClickData = usedButtonIn;
        this.clickedItem = clickedItem.copy();
        this.actionNumber = actionNumberIn;
        this.mode = modeIn;
    }

    public int getWindowId() {
        return windowId;
    }

    public int getSlotId() {
        return slotId;
    }

    public int getPackedClickData() {
        return packedClickData;
    }

    public short getActionNumber() {
        return actionNumber;
    }


    public ClickType getMode() {
        return mode;
    }

    @Override
    public void fromBytes(ByteBuf buf){
        PacketBuffer packetBuffer = new PacketBuffer(buf);
        this.windowId = packetBuffer.readByte();
        this.slotId = packetBuffer.readShort();
        this.packedClickData = packetBuffer.readByte();
        this.actionNumber = packetBuffer.readShort();
        this.mode = packetBuffer.readEnumValue(ClickType.class);
        this.inventoryId = packetBuffer.readString(4096);
        try {
            this.clickedItem = packetBuffer.readItemStack();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        PacketBuffer packetBuffer = new PacketBuffer(buf);
        packetBuffer.writeByte(this.windowId);
        packetBuffer.writeShort(this.slotId);
        packetBuffer.writeByte(this.packedClickData);
        packetBuffer.writeShort(this.actionNumber);
        packetBuffer.writeEnumValue(this.mode);
        packetBuffer.writeString(this.inventoryId);
        PacketUtil.writeItemStackFromClientToServer(packetBuffer, this.clickedItem);
    }

    public String getInventoryId() {
        return inventoryId;
    }

    public ItemStack getClickedItem() {
        return clickedItem;
    }
}
