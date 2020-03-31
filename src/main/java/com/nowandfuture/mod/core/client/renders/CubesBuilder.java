package com.nowandfuture.mod.core.client.renders;

import com.creativemd.creativecore.client.mods.optifine.OptifineHelper;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.nowandfuture.asm.Utils;
import com.nowandfuture.mod.core.prefab.AbstractPrefab;
import com.nowandfuture.mod.core.prefab.LocalWorld;
import com.nowandfuture.mod.core.selection.OBBox;
import com.nowandfuture.mod.utils.math.Matrix4f;
import com.nowandfuture.mod.utils.math.Vector3f;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.chunk.RenderChunk;
import net.minecraft.client.renderer.chunk.VisGraph;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3i;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

//arithmetic: some from minecraft such as flooding algorithm;
public class CubesBuilder {
    public static int CUBE_SIZE = 16;
    //unused
    public static int overDistanceThreshold = 256;//chunks sq


    //because of prefab's size is custom size,spilt it to cubes witch size is CUBE_SIZE * CUBE_SIZE * CUBE_SIZE;
    public static Vec3i reMapPrefab(Map<BlockPos,RenderCube> cubeMap,AbstractPrefab prefab,CubesRenderer renderer){
        if(cubeMap == null) cubeMap = Maps.newHashMap();

        boolean ready = prefab.isLocalWorldInit();
        if(!ready) return null;

        Vec3i size = prefab.getSize();

        double x = size.getX();
        double y = size.getY();
        double z = size.getZ();

        if(x > 0 && y > 0 && z > 0){

            final int remapXSize = (int) Math.ceil(x / CUBE_SIZE);
            final int remapYSize = (int) Math.ceil(y / CUBE_SIZE);
            final int remapZSize = (int) Math.ceil(z / CUBE_SIZE);

            Vec3i cubesSize = new Vec3i(remapXSize, remapYSize, remapZSize);

            for(int i = 0;i < remapXSize;i++){

                for (int j = 0;j < remapYSize;j++){

                    for (int k = 0;k < remapZSize;k++){
                        RenderCube cube = new RenderCube(renderer,prefab.getLocalWorld(),new BlockPos(i,j,k));
                        cubeMap.put(cube.getPos(),cube);
                    }
                }
            }

            return cubesSize;
        }

        return Vec3i.NULL_VECTOR;
    }

    //create map to store each face's visible cube list(cull cubes invisible)
    public static Map<EnumFacing,Set<RenderCube>> createVisibleCubesForEachFace(Map<BlockPos,RenderCube> cubes,Vec3i size){

        Map<EnumFacing,Set<RenderCube>>  facingListMap = new HashMap<>(6);

        for (EnumFacing face :
                EnumFacing.values()) {
            facingListMap.put(face, new HashSet<>());
        }

        //init each face's cubes(outermost layer)
        cubes.forEach(new BiConsumer<BlockPos, RenderCube>() {
            @Override
            public void accept(BlockPos pos, RenderCube cube) {
                if(cube.getPos().getX() == 0 ){
                    facingListMap.get(EnumFacing.WEST).add(cube);
                }
                if(cube.getPos().getX() == size.getX() - 1){
                    facingListMap.get(EnumFacing.EAST).add(cube);
                }
                if(cube.getPos().getY() == 0){
                    facingListMap.get(EnumFacing.DOWN).add(cube);
                }
                if(cube.getPos().getY() == size.getY() - 1){
                    facingListMap.get(EnumFacing.UP).add(cube);
                }
                if(cube.getPos().getZ() == 0){
                    facingListMap.get(EnumFacing.NORTH).add(cube);
                }
                if(cube.getPos().getZ() == size.getZ() - 1){
                    facingListMap.get(EnumFacing.SOUTH).add(cube);
                }
            }
        });

        return facingListMap;
    }

    //start BFS and not search back,(like what minecraft do on renderChunk's searching)
    public static Set<RenderCube> getVisibleRenderCubesFromFacing(Map<BlockPos,RenderCube> cubes, Map<EnumFacing,Set<RenderCube>> facingListMap, EnumFacing visitFace, Function<RenderCube,Boolean> filter,int renderFrame){

        //the pair store cube and it's inside-face(path)
        Queue<CubesRenderer.RenderCubeInformation> queue = new LinkedList<>();
        Set<RenderCube> visibleCubes = new HashSet<>(cubes.size());

        facingListMap.get(visitFace).forEach(new Consumer<RenderCube>() {
            @Override
            public void accept(RenderCube cube) {
                if(filter != null){
                    if(filter.apply(cube)) {
                        CubesRenderer.RenderCubeInformation cubeInformation =
                                new CubesRenderer.RenderCubeInformation(cube,visitFace.getOpposite(),0);
                        cubeInformation.setDirection(visitFace.getOpposite());

                        queue.add(cubeInformation);
                    }
                }else{
                    CubesRenderer.RenderCubeInformation cubeInformation =
                            new CubesRenderer.RenderCubeInformation(cube,visitFace.getOpposite(),0);
                    cubeInformation.setDirection(visitFace.getOpposite());

                    queue.add(cubeInformation);
                }
            }
        });

        visitFromCubes(visibleCubes,cubes,queue,filter,renderFrame);

        return visibleCubes;
    }


    public static Set<RenderCube> getVisibleRenderCubesFromCube(Vector3f lookVec,Map<BlockPos,RenderCube> cubes,RenderCube startCube,Function<RenderCube,Boolean> filter,int renderFrame){

        Queue<CubesRenderer.RenderCubeInformation> queue = new LinkedList<>();

//        EnumFacing backFace = null;
//        float min = Float.MIN_VALUE;
//        for (EnumFacing testFace:
//             EnumFacing.values()) {
//
//            OBBox.Facing facing = OBBox.Facing.createFromAABBounding(testFace, startCube.getBounding());
//
//            if(facing != null) {
//                facing.mulMatrix(startCube.getCubesRenderer().getModelMatrix());
//                float f = facing.dotDirectionVec(lookVec);
//
//                if (f < min) {
//                    min = f;
//                    backFace = testFace;
//                }
//            }
//        }

        Set<RenderCube> visibleCubes = new HashSet<>(cubes.size());

        CubesRenderer.RenderCubeInformation cubeInformation =
                new CubesRenderer.RenderCubeInformation(startCube,null,0);

        queue.add(cubeInformation);
        visitFromCubes(visibleCubes, cubes, queue, filter,renderFrame);

        return visibleCubes;
    }

    private static void visitFromCubes(Set<RenderCube> visibleCubes, Map<BlockPos,RenderCube> cubes, Queue<CubesRenderer.RenderCubeInformation> queue, Function<RenderCube,Boolean> filter,int renderFrame){

        while (!queue.isEmpty()){
            CubesRenderer.RenderCubeInformation cubeInformation = queue.poll();
            RenderCube cube = cubeInformation.renderCube;
            EnumFacing from = cubeInformation.facing;
            visibleCubes.add(cube);
            for(EnumFacing to :
                    EnumFacing.values()){
                if(!cubeInformation.hasDirection(to.getOpposite()) && (from == null || cube.isVisible(from.getOpposite(),to))){
                    BlockPos nextCubePos = cube.getPos().offset(to);
                    RenderCube nextCube = cubes.get(nextCubePos);

                    if(nextCube != null && nextCube.setRenderFrame(renderFrame) && filter.apply(nextCube)) {
                        CubesRenderer.RenderCubeInformation nextInformation =
                                new CubesRenderer.RenderCubeInformation(nextCube, to,cubeInformation.routerCount + 1);
                        nextInformation.setDirection(cubeInformation.setFacing,to);
                        queue.add(nextInformation);
                    }

                }
            }
        }
    }

    //compute each face's visible face
    public static void computeVisibleMapInCube(RenderCube cube){
        VisGraph visGraph = new VisGraph();
        LocalWorld world = cube.getWorld();

        if(world != null){
            world.streamCubes(cube.getPos(),new LocalWorld.LocalWorldSearch() {
                @Override
                public void search(BlockPos blockPos, IBlockState blockState, TileEntity tileEntity) {
                    if(blockState.isOpaqueCube())
                        visGraph.setOpaqueCube(blockPos);
                }
            });

        }
        cube.setSetVisibility(visGraph.computeVisibility());
    }

    //check is the renderchunks are render(which chunks are contain the cube)
    @Deprecated
    public static boolean checkRenderChunkIsRender(RenderCube cube,BlockPos basePos) throws NoSuchFieldException, IllegalAccessException {
        OBBox bounding = cube.getTransformedOBBounding();
        Map<BlockPos,RenderChunk> map = Utils.getRenderChunkMap();

        if(map != null) {
            for (Vector3f vex :
                    bounding.asArray()) {
                if (map.containsKey(
                        transferToRenderChunkPos(vex.x + basePos.getX(), vex.y + basePos.getY(), vex.z + basePos.getZ()))) {
                    return true;
                }
            }
        } else
            return true;
        return OptifineHelper.isActive();
    }

    //get the pos's(d3,d4,d5) render-chunk-pos in the world
    public static BlockPos transferToRenderChunkPos(float d3, float d4, float d5){
        return new BlockPos(MathHelper.floor(d3 / 16.0D) * 16, MathHelper.floor(d4 / 16.0D) * 16, MathHelper.floor(d5 / 16.0D) * 16);
    }

    //do faces cull
    public static Set<EnumFacing> getVisibleFaces(Vector3f visitorPos, CubesRenderer renderer){
        final Set<EnumFacing> set = Sets.newHashSet();
        final OBBox bounding = new OBBox(new AxisAlignedBB(0,0,0,renderer.getSize().getX(),renderer.getSize().getY(),renderer.getSize().getZ()));
        Vector3f temp;
        OBBox.Facing face;

        AbstractPrefab prefab = renderer.getPrefab();
        for (EnumFacing enumFacing :
                EnumFacing.values()) {

            face = OBBox.Facing.createFromAABBounding(enumFacing,bounding);
            if(face != null){
                face.mulMatrix(renderer.getModelMatrix());
                temp = new Vector3f(face.getV0());
                Vector3f lookVec = Vector3f.sub(
                        temp.translate(prefab.getBasePos().getX(),prefab.getBasePos().getY(),prefab.getBasePos().getZ()),
                        visitorPos,
                        new Vector3f()
                );
                if(face.isFrontBy(lookVec))
                    set.add(enumFacing);
            }
        }
        return set;
    }

    //copy from RenderGlobal
    public static Vector3f getViewVector(Entity entityIn, double partialTicks)
    {
        float f = (float)((double)entityIn.prevRotationPitch + (double)(entityIn.rotationPitch - entityIn.prevRotationPitch) * partialTicks);
        float f1 = (float)((double)entityIn.prevRotationYaw + (double)(entityIn.rotationYaw - entityIn.prevRotationYaw) * partialTicks);

        if (Minecraft.getMinecraft().gameSettings.thirdPersonView == 2) {
            f += 180.0F;
        }

        float f2 = MathHelper.cos(-f1 * 0.017453292F - (float)Math.PI);
        float f3 = MathHelper.sin(-f1 * 0.017453292F - (float)Math.PI);
        float f4 = -MathHelper.cos(-f * 0.017453292F);
        float f5 = MathHelper.sin(-f * 0.017453292F);
        return new Vector3f(f3 * f4, f5, f2 * f4);
    }

    public static BlockPos getVisitorCubePos(CubesRenderer cubesRenderer,BlockPos visitorPos){
        return getVisitorCubePos(cubesRenderer,new Vector3f(visitorPos.getX(),visitorPos.getY(),visitorPos.getZ()));
    }

    public static BlockPos getVisitorCubePos(CubesRenderer cubesRenderer,Vector3f visitorPos){
        if(!cubesRenderer.isBuilt()) return null;
        AbstractPrefab prefab = cubesRenderer.getPrefab();
        if(!prefab.isLocalWorldInit()) return null;
        Vector3f localPos = getLocalPos(visitorPos,cubesRenderer);

        if(localPos.getX() < 0 || localPos.getY() < 0 || localPos.getZ() < 0) return null;

        final int cubeX = (int) (localPos.getX() / CUBE_SIZE);
        final int cubeY = (int) (localPos.getY() / CUBE_SIZE);
        final int cubeZ = (int) (localPos.getZ() / CUBE_SIZE);

        if(cubeX < cubesRenderer.getSize().getX() && cubeY < cubesRenderer.getSize().getY() && cubeZ < cubesRenderer.getSize().getZ()){
            return new BlockPos(cubeX,cubeY,cubeZ);
        }
        return null;
    }

    //from GL model-coordinate to prefab-coordinate
    public static Vector3f getLocalPos(BlockPos visitorPos,CubesRenderer renderer){
        return getLocalPos(new Vector3f(visitorPos.getX(),visitorPos.getY(),visitorPos.getZ()),renderer);
    }

    //from GL model-coordinate to prefab-coordinate
    public static Vector3f getLocalPos(Vector3f visitorPos,CubesRenderer renderer){
        Matrix4f invertMatrix = new Matrix4f();
        Matrix4f.invert(renderer.getModelMatrix(),invertMatrix);
        return OBBox.transform(visitorPos,invertMatrix);
    }

    public static Vector3f getTransformPos(Vector3f pos,CubesRenderer renderer){
       return OBBox.transform(pos,renderer.getModelMatrix());
    }
}
