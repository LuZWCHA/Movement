package com.nowandfuture.mod.core.movecontrol;

import com.nowandfuture.mod.api.IModule;
import com.nowandfuture.mod.core.client.renders.CubesRenderer;
import com.nowandfuture.mod.core.prefab.AbstractPrefab;
import com.nowandfuture.mod.core.prefab.EmptyPrefab;
import com.nowandfuture.mod.core.transformers.*;
import com.nowandfuture.mod.core.transformers.animation.KeyFrame;
import com.nowandfuture.mod.core.transformers.animation.KeyFrameLine;
import joptsimple.internal.Strings;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;

public class ModuleBase implements IModule,ITickable {

    public final String NBT_MODULE_NAME = "ModuleName";
    public final String NBT_AUTHOR = "Author";
    public final String NBT_MODULE_ENABLE = "EnableModule";

    public final String NBT_PREFAB_TAG = "PrefabTag";
    public final String NBT_TRANSFORMERS_TAG = "TransformerTag";
    public final String NBT_KEYFRAMES_LINE_TAG = "TimeLineTag";

    private String author = Strings.EMPTY;
    private String name = Strings.EMPTY;

    private final KeyFrameLine line;

    private final Object lock = new Object();
    private AbstractPrefab prefab;
    private AbstractTransformNode transformerHead;

    private boolean enable;

    private World world;

    public ModuleBase(){
        super();
        prefab = new EmptyPrefab();
        line = new KeyFrameLine();

        //all animation only create on client
    }

    public void createDefaultTransformer(){
        LinearTransformNode node = new LinearTransformNode();
        ScaleTransformNode node1 = new ScaleTransformNode();
        RotationTransformNode node2 = new RotationTransformNode();

        node.setInterpolation(TimeInterpolation.Type.HIGHER_POWER_DOWN);

        AbstractTransformNode.Builder.newBuilder()
                .create(node)
                .parent(node1)
                .parent(node2)
                .build();

        setTransformNode(node);
    }

    public void setPrefab(AbstractPrefab prefab) {
        synchronized (lock) {
            if(this.prefab != null && prefab != this.prefab && this.prefab.isLocalWorldInit()){
                this.prefab.invalid();
            }
            this.prefab = prefab;
        }
    }

    public AbstractPrefab getPrefab() {
        return prefab;
    }

    public void setModulePos(BlockPos posIn) {
        prefab.setBaseLocation(posIn);
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void enable(){
        enable = true;
    }

    public void disable(){
        enable = false;
    }

    public boolean isEnable() {
        return enable && prefab.isReady();
    }


    @Override
    public BlockPos getModulePos() {
        return prefab.getBasePos();
    }

    public void setTransformNode(@Nonnull AbstractTransformNode part){
        synchronized (lock) {
            transformerHead = part;
        }
    }

    public void removePartIfExit(){
        if(!transformerHead.getClass().equals(RootTransformNode.class))
            transformerHead = TransformNodeManager.INSTANCE.getDefaultAttributeNode();
    }

    @SideOnly(Side.CLIENT)
    public void transformPre(double p, CubesRenderer renderer){
        KeyFrameLine.TimeSection section;

        if(transformerHead != null)
            for (KeyFrame.KeyFrameType kt:
                    KeyFrame.KeyFrameType.values()) {

                section = line.getSection(kt);

                if(section == null || section.isEmpty()) continue;

                transformerHead.transformStart(renderer, (float) line.getSectionProgress(section, (float) p),
                        section.getBegin(),section.getEnd());
            }
    }

    @SideOnly(Side.CLIENT)
    public void transformPost(double p,CubesRenderer renderer){
        KeyFrameLine.TimeSection section;

        if(transformerHead != null)
            for (KeyFrame.KeyFrameType kt:
                    KeyFrame.KeyFrameType.values()) {

                section = line.getSection(kt);

                if(section == null || section.isEmpty()) continue;

                transformerHead.transformEnd(renderer, (float) line.getSectionProgress(section, (float) p),
                        section.getBegin(),section.getEnd());
            }
    }

    public void renderForGui(float p, float rotAngel){

    }

    public void setUseFixLight(boolean enable){
        prefab.useFixSkyLight(enable);
    }

    @Override
    public void update() {
        if(!isEnable()) return;
        if(prefab != null && line != null) {
            prefab.update();
        }
    }

    public AxisAlignedBB getMinAABB(){
        return prefab.getMinAABB();
    }

    public boolean updateLine(){
        if(line != null && line.isEnable()){
            return line.update();
        }
        else
            return false;
    }

    public void setModuleWorld(World worldIn){
        this.world = worldIn;
    };

    public void invalid() {
        if(prefab != null && getModuleWorld().isRemote){
            prefab.invalid();
        }
    }

    public void readFromNBT(NBTTagCompound compound) {
        readModuleFromNBT(compound);

        if(compound.hasKey(NBT_PREFAB_TAG)) {
            NBTTagCompound recNBT = compound.getCompoundTag(NBT_PREFAB_TAG);

            if(!prefab.isLocalWorldInit())//when first created
                prefab.readFromNBT(recNBT, getModuleWorld());
            else
                prefab.readFromNBT(recNBT);
        }
//
//        if(compound.hasKey(NBT_TRANSFORMERS_TAG) && world.isRemote) {
//            NBTTagCompound transNBT = compound.getCompoundTag(NBT_TRANSFORMERS_TAG);
//            if(transformerHead == null){
//                transformerHead = AbstractTransformNode.Builder.newBuilder().
//                         buildFromNBTTag(transNBT);
//            }else {
//                transformerHead.readFromNBT(transNBT);
//            }
//        }

        if(compound.hasKey(NBT_KEYFRAMES_LINE_TAG)){
            NBTTagCompound keysNBT = compound.getCompoundTag(NBT_KEYFRAMES_LINE_TAG);
            line.deserializeNBT(keysNBT);
        }

    }

    public World getModuleWorld(){
        return world;
    };

    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        writeModuleToNBT(compound);

        if(prefab.isLocalWorldInit())
            compound.setTag(NBT_PREFAB_TAG,
                prefab.writeToNBT(new NBTTagCompound()));

//        if(world.isRemote && transformerHead != null)
//            compound.setTag(NBT_TRANSFORMERS_TAG,
//                transformerHead.writeToNBT(new NBTTagCompound()));

        compound.setTag(NBT_KEYFRAMES_LINE_TAG,
                line.serializeNBT(new NBTTagCompound()));

        return compound;
    }

    @Override
    public void readModuleFromNBT(NBTTagCompound compound) {
        name = compound.getString(NBT_MODULE_NAME);
        author = compound.getString(NBT_AUTHOR);
        enable = compound.getBoolean(NBT_MODULE_ENABLE);
    }

    @Override
    public NBTTagCompound writeModuleToNBT(NBTTagCompound compound) {
        compound.setString(NBT_AUTHOR,author);
        compound.setString(NBT_MODULE_NAME,name);
        compound.setBoolean(NBT_MODULE_ENABLE,enable);
        return compound;
    }

    public KeyFrameLine getLine() {
        return line;
    }

}
