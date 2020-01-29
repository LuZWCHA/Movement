package com.nowandfuture.mod.handler;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public interface IKeyListener {
    void onKeyDown();
}
