package com.nowandfuture.mod.utils.collision;

import com.nowandfuture.mod.api.Unstable;

import java.util.Comparator;

@Unstable
public class CollisionHelper {
    private static Comparator<CollisionInfo> comparator;
    static {
        comparator = new Comparator<CollisionInfo>() {
            @Override
            public int compare(CollisionInfo o1, CollisionInfo o2) {
                return Double.compare(o1.getImpactTime(), o2.getImpactTime());
            }
        };
    }

    public static Comparator<CollisionInfo> getComparator() {
        return comparator;
    }
}
