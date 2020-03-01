package com.nowandfuture.mod.core.prefab;

import com.nowandfuture.mod.api.IModule;
import com.nowandfuture.mod.utils.math.Vector3f;
import net.minecraft.util.math.BlockPos;

import java.util.Objects;

public class AnchorObj{
    public Type type;
    //point of world or module
    public Object obj;

    private AnchorObj(Type type,Object obj){
        this.type = type;
        this.obj = obj;
    }

    private AnchorObj(){
        this.type = Type.NULL;
        this.obj = null;
    }

    public enum Type{
        WORLD_OBJ,
        MODULE_OBJ,
        NULL
    }

    boolean isNull(){
        return type == Type.NULL;
    }

    public static AnchorObj createWorldObj(BlockPos pos){
        return new AnchorObj(Type.WORLD_OBJ,pos);
    }

    public static AnchorObj createModuleObj(IModule module){
        return new AnchorObj(Type.MODULE_OBJ,module);
    }

    public static AnchorObj createNullObj(){
        return new AnchorObj();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AnchorObj anchorObj = (AnchorObj) o;
        return type == anchorObj.type &&
                Objects.equals(obj, anchorObj.obj);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, obj);
    }
}
