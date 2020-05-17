package com.nowandfuture.mod.core.transformers;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public enum TransformNodeManager {
    INSTANCE;
    Long startType;
    Map<Long,Class<? extends AbstractTransformNode>> nodes;

    TransformNodeManager(){
        startType = 0L;
        nodes = new HashMap<>();
        registerDefault();
    }

    private void registerDefault(){
        register(RootTransformNode.class);//0

        register(LocationTransformNode.class);//1
        register(RotationTransformNode.class);//2
        register(ScaleTransformNode.class);//3
    }

    @Nonnull
    public final AbstractTransformNode getDefaultAttributeNode(){
        return RootTransformNode.create();
    }

    public void register(Class<? extends AbstractTransformNode> part){
        nodes.put(startType++,part);
    }

    public Optional<AbstractTransformNode> getTransformNode(Long type){
        if(type.equals(RootTransformNode.DEFAULT_TYPE)) return Optional.of(RootTransformNode.create());

        Class<? extends AbstractTransformNode> partClazz = nodes.get(type);
        return getTransformerInstance(partClazz);
    }

    private Optional<AbstractTransformNode> getTransformerInstance(Class<? extends AbstractTransformNode> transClazz){
        AbstractTransformNode part = null;
        if(transClazz != null) {
            try {
                part = transClazz.newInstance();
                //part.setTypeId(startType++);
                part.setName(transClazz.getSimpleName());
            } catch (InstantiationException | IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return Optional.ofNullable(part);
    }

    public Optional<AbstractTransformNode> getFirstNodeByClazzName(String name){
        for (Map.Entry<Long, Class<? extends AbstractTransformNode>> entries:
            nodes.entrySet()){
            if(entries.getValue().getSimpleName().equals(name))
                return getTransformerInstance(entries.getValue());
        }
        return Optional.empty();
    }
}
