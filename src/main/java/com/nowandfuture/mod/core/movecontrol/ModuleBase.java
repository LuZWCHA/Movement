package com.nowandfuture.mod.core.movecontrol;

import com.nowandfuture.mod.Movement;
import com.nowandfuture.mod.api.IModule;
import com.nowandfuture.mod.core.transformers.AbstractTransformNode;
import com.nowandfuture.mod.core.transformers.TransformNodeManager;
import com.nowandfuture.mod.core.transformers.RootTransformNode;
import com.nowandfuture.mod.core.transformers.animation.KeyFrame;
import com.nowandfuture.mod.core.transformers.animation.KeyFrameLine;
import com.nowandfuture.mod.core.prefab.AbstractPrefab;
import com.nowandfuture.mod.core.prefab.BasePrefab;
import com.nowandfuture.mod.core.prefab.MultiThreadPrefabWrapper;
import com.nowandfuture.mod.network.NetworkHandler;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nonnull;
import java.nio.FloatBuffer;

public class ModuleBase implements IModule,ITickable, MultiThreadPrefabWrapper.ConstructListener {

    public final String NBT_MODULE_NAME = "ModuleName";
    public final String NBT_AUTHOR = "Author";
    public final String NBT_MODULE_ENABLE = "EnableModule";

    public final String NBT_PREFAB_TAG = "PrefabTag";
    public final String NBT_TRANSFORMERS_TAG = "TransformerTag";
    public final String NBT_KEYFRAMES_LINE_TAG = "TimeLineTag";

    private NBTTagCompound copiedNbtTag;

    private String author = "";
    private String name = "";

    private final KeyFrameLine line;
    private AbstractPrefab prefab;
    private AbstractTransformNode transformerHead;
    private final FloatBuffer modelviewMatrix = GLAllocation.createDirectFloatBuffer(16);

    private boolean enable;

    private World world;

    public ModuleBase(){
        super();
        prefab = new BasePrefab();
        line = new KeyFrameLine();
    }

    public void setPrefab(AbstractPrefab prefab) {
        this.prefab = prefab;
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

    public boolean canRender(double renderPosX,double renderPosY,double renderPosZ){
        return true;
    }

    @Override
    public BlockPos getModulePos() {
        return prefab.getBasePos();
    }

    public void setPart(@Nonnull AbstractTransformNode part){
       transformerHead = part;
    }

    public void removePartIfExit(){
        if(!transformerHead.getClass().equals(RootTransformNode.class))
            transformerHead = TransformNodeManager.INSTANCE.getDefaultAttributeNode();
    }

    public void constructPrefab(){
        MultiThreadPrefabWrapper multiConstruct = new MultiThreadPrefabWrapper(prefab);
        multiConstruct.setConstructListener(this);
        multiConstruct.constructLocalWoldFromActrualWorld();
    }

    @Override
    public final void render(int pass,float p) {
        GlStateManager.pushMatrix();
        prefab.renderPre(p);

        KeyFrameLine.TimeSection section;

        prefab.getModelMatrix().setIdentity();

        for (KeyFrame.KeyFrameType kt:
                KeyFrame.KeyFrameType.values()) {

            section = line.getSection(kt);

            if(section == null || section.isEmpty()) continue;

            transformerHead.transformStart(prefab, (float) line.getSectionProgress(section, p),
                    section.getBegin(),section.getEnd());
        }

        prefab.render(p);

        for (KeyFrame.KeyFrameType kt:
                KeyFrame.KeyFrameType.values()) {

            section = line.getSection(kt);

            if(section == null || section.isEmpty()) continue;

            transformerHead.transformEnd(prefab, (float) line.getSectionProgress(section, p),
                   section.getBegin(),section.getEnd());
        }

        prefab.renderPost(p);
        GlStateManager.popMatrix();
    }

    //for render
    @Override
    public boolean isRenderValid() {
        return isEnable();
    }

    @Override
    public void update() {
        if(!isEnable()) return;
        if(prefab != null)
            prefab.update();
        if(line != null)
            line.update();
    }

    public void setModuleWorld(World worldIn){
        this.world = worldIn;
    };

    public void invalidate() {
        if (prefab != null)
            prefab.invalidRenderList();
    }

    public void readFromNBT(NBTTagCompound compound) {
        Movement.logger.info("readFromNBT !!!");

        readModuleFromNBT(compound);

        if(compound.hasKey(NBT_PREFAB_TAG)) {
            NBTTagCompound recNBT = compound.getCompoundTag(NBT_PREFAB_TAG);

            if(!prefab.isLocalWorldInit())//when first created
                prefab.readFromNBT(recNBT, getModuleWorld());
            else
                prefab.readFromNBT(recNBT);
        }

        if(compound.hasKey(NBT_TRANSFORMERS_TAG)) {
            NBTTagCompound transNBT = compound.getCompoundTag(NBT_TRANSFORMERS_TAG);
            if(transformerHead == null){
                transformerHead = AbstractTransformNode.Builder
                        .newBuilder().
                         buildFromNBTTag(transNBT);
            }else {
                transformerHead.readFromNBT(transNBT);
            }
        }
        if(compound.hasKey(NBT_KEYFRAMES_LINE_TAG)){
            NBTTagCompound keysNBT = compound.getCompoundTag(NBT_KEYFRAMES_LINE_TAG);
            line.deserializeNBT(keysNBT);
        }

        copiedNbtTag = compound;
    }

    public World getModuleWorld(){
        return world;
    };

    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        Movement.logger.info("writeToNBT !!!");
        writeModuleToNBT(compound);
        compound.setTag(NBT_PREFAB_TAG,
                prefab.writeToNBT(new NBTTagCompound()));
        compound.setTag(NBT_TRANSFORMERS_TAG,
                transformerHead.writeToNBT(new NBTTagCompound()));
        compound.setTag(NBT_KEYFRAMES_LINE_TAG,
                line.serializeNBT(new NBTTagCompound()));
        copiedNbtTag = compound;
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


    @Override
    public void onError(Exception e) {
        prefab.setReady(false);

        NetworkHandler.INSTANCE.sendMessage(e.getMessage());
    }

    @Override
    public void onStart() {
        prefab.setReady(false);
        NetworkHandler.INSTANCE.sendMessage("start");
    }

    @Override
    public void onCompleted() {
        prefab.setReady(true);
        NetworkHandler.INSTANCE.sendMessage("complete:"+ this.prefab.getBasePos().toString());
    }

    public KeyFrameLine getLine() {
        return line;
    }

    public NBTTagCompound getCopiedNbtTag() {
        return copiedNbtTag;
    }

}
