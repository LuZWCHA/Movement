package com.nowandfuture.mod.core.movecontrol;

import com.nowandfuture.mod.api.IModule;
import com.nowandfuture.mod.core.entities.TileEntityMovementModule;
import com.nowandfuture.mod.core.transformers.AbstractTransformNode;
import com.nowandfuture.mod.core.transformers.ScaleTransformNode;
import com.nowandfuture.mod.core.prefab.BasePrefab;
import com.nowandfuture.mod.handler.RenderHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;

import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public enum ModuleManager {
    INSTANCE;
    private final Map<BlockPos,IModule> modules = new HashMap<>();
    private final Deque<IModule> renderModules = new LinkedList<>();

    public void initModules(){

    }

    public void add(BlockPos id,IModule movementModule){
        modules.put(id,movementModule);
    }

    public Deque<IModule> getModules() {
        return renderModules;
    }

    public void constructModule(World world, BlockPos pos, Vec3i size){
        ModuleBase module = new ModuleBase();
        module.setPrefab(new BasePrefab(world,pos,size));
        module.disable();

        add(pos,module);

//        RotationTransformNode part = new RotationTransformNode();
        ScaleTransformNode part1 = new ScaleTransformNode();
//        LinearTransformNode part2 = new LinearTransformNode();

        AbstractTransformNode.Builder.newBuilder()
//                .create(part)
                .parent(part1)
//                .parent(part2)
                .build();

//        module.setTransformNode(part);
        module.constructPrefab();
    }

    public void spawnModule(World world,BlockPos pos){
        renderModules.addFirst(modules.get(pos));
        TileEntityMovementModule module = (TileEntityMovementModule) renderModules.getFirst();
        module.setModulePos(pos);

        //world.setTileEntity(pos,module);

         Minecraft.getMinecraft().world.addTileEntity(module);



        //TileEntitySpecialRenderer render = TileEntityRendererDispatcher.instance.getRenderer(ModuleBase.class);
        module.setPos(pos);

        //Movement.logger.info("render "+ (render instanceof ModuleRender));
        module.enable();
    }

    public void spawnModule(EntityPlayerSP player){
        spawnModule(player.world,player.getPosition());
    }

    public void constructTest(EntityPlayerSP player){
        constructModule(player.world,
                RenderHandler.getAabbSelectArea().getPos(),
                RenderHandler.getAabbSelectArea().getSize());
    }

    public void spawnTest(EntityPlayerSP player){
        spawnModule(player);
    }
}
