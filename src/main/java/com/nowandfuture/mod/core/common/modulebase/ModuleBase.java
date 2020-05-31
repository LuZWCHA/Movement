package com.nowandfuture.mod.core.common.modulebase;

import com.nowandfuture.mod.api.IModule;
import com.nowandfuture.mod.api.Unstable;
import com.nowandfuture.mod.core.prefab.AbstractPrefab;
import com.nowandfuture.mod.core.prefab.NormalPrefab;
import com.nowandfuture.mod.core.transformers.*;
import com.nowandfuture.mod.core.transformers.animation.KeyFrame;
import com.nowandfuture.mod.core.transformers.animation.KeyFrameLine;
import com.nowandfuture.mod.utils.math.Matrix4f;
import joptsimple.internal.Strings;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import java.util.List;

public class ModuleBase implements IModule {

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
    private Matrix4f transMatrix;

    private boolean enable;

    private World world;

    public ModuleBase(){
        super();
        prefab = new NormalPrefab();
        line = new KeyFrameLine();
        transMatrix = new Matrix4f();
    }

    public void createDefaultTransformer(){
        LocationTransformNode first = new LocationTransformNode();
        RotationTransformNode middle = new RotationTransformNode();
        ScaleTransformNode last = new ScaleTransformNode();

        AbstractTransformNode.Builder.newBuilder()
                .create(last)
                .parent(middle)
                .parent(first)
                .build();

        setTransformNode(last);
    }

    public AbstractTransformNode getTransformerHead() {
        return transformerHead;
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

    @Override
    public void doTransform(double p, Matrix4f matrix4f) {
        transformPre(p,matrix4f);
        transformPost(p,matrix4f);
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

    public void transformPre(double p, Matrix4f matrix4f){
        KeyFrameLine.TimeSection section;

        if(transformerHead != null)
            for (KeyFrame.KeyFrameType kt:
                    KeyFrame.KeyFrameType.values()) {

                section = line.getSection(kt);

                if(section == null || section.isEmpty()) continue;

                transformerHead.initLine(line);

                transformerHead.transformStart(matrix4f, (float) line.getSectionProgress(section, (float) p),
                        section.getBegin(),section.getEnd());
            }
    }

    public void transformPost(double p,Matrix4f matrix4f){
        KeyFrameLine.TimeSection section;

        if(transformerHead != null)
            for (KeyFrame.KeyFrameType kt:
                    KeyFrame.KeyFrameType.values()) {

                section = line.getSection(kt);

                if(section == null || section.isEmpty()) continue;

                transformerHead.transformEnd(matrix4f, (float) line.getSectionProgress(section, (float) p),
                        section.getBegin(),section.getEnd());
            }
    }

    public void setUseFixLight(boolean enable){
        prefab.useFixSkyLight(enable);
    }

    @Override
    public void update() {
        if(line != null) {
            updateEntities();
            updateMatrix();
        }
    }

    public void updateEntities(){
        if(isEnable() && prefab != null)
            prefab.update();
    }

    public void updateMatrix(){
        if(isEnable()) {
            transMatrix.setIdentity();
            doTransform(0, transMatrix);
        }
    }

    public AxisAlignedBB getMinAABB(){
        return prefab.getMinAABB();
    }

    @Unstable
    public void collectAABBsWithin(List<AxisAlignedBB> list,AxisAlignedBB area){
        prefab.collectAABBs(list,area);
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

    @SideOnly(Side.CLIENT)
    public void clearGLResource() {
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

        if(compound.hasKey(NBT_KEYFRAMES_LINE_TAG)){
            NBTTagCompound keysNBT = compound.getCompoundTag(NBT_KEYFRAMES_LINE_TAG);
            line.deserializeNBT(keysNBT);
        }

        if(compound.hasKey(NBT_TRANSFORMERS_TAG)){
            NBTTagCompound tfNBT = compound.getCompoundTag(NBT_TRANSFORMERS_TAG);
            transformerHead.readFromNBT(tfNBT);
        }

    }

    public Matrix4f getTransMatrix() {
        return transMatrix;
    }

    public World getModuleWorld(){
        return world;
    };

    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        writeModuleToNBT(compound);

        if(prefab.isLocalWorldInit())
            compound.setTag(NBT_PREFAB_TAG,
                prefab.writeToNBT(new NBTTagCompound()));

        compound.setTag(NBT_KEYFRAMES_LINE_TAG,
                line.serializeNBT(new NBTTagCompound()));

        compound.setTag(NBT_TRANSFORMERS_TAG,transformerHead.writeToNBT(new NBTTagCompound()));

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


    public void setInterpolationAlgorithm(int type,int algorithmId){
        AbstractTransformNode temp = transformerHead;

        while (temp != null){
            if(temp.getTypeId() == type){
                temp.setArithmeticId(algorithmId);
                break;
            }
            temp = temp.getNext();
        }
    }

    public int getInterpolationAlgorithm(int type){
        AbstractTransformNode temp = transformerHead;

        while (temp != null){
            if(temp.getTypeId() == type){
                return temp.getArithmeticId();
            }
            temp = temp.getNext();
        }
        return -1;
    }

    public void setTimeInterpolation(TimeInterpolation.Type type){
        AbstractTransformNode temp = transformerHead;

        while (temp != null){
            temp.setInterpolation(type);
            temp = temp.getNext();
        }
    }

    public TimeInterpolation.Type getTimeInterpolation(){
        if(transformerHead == null) return TimeInterpolation.Type.LINEAR;
        return transformerHead.getInterpolation().getType();
    }

}
