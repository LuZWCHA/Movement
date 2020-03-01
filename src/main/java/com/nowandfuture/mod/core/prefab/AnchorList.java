package com.nowandfuture.mod.core.prefab;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class AnchorList {
    private List<Anchor> list;

    public AnchorList(){
        list = new ArrayList<>();
    }

    public void addAnchor(@Nonnull Anchor anchor){
        list.add(anchor);
    }

    public void deleteAnchor(@Nonnull Anchor anchor){
        list.remove(anchor);
    }

    public void each(Consumer<Anchor> consumer){
        list.forEach(consumer);
    }

}
