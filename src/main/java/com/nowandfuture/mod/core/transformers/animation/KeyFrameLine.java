package com.nowandfuture.mod.core.transformers.animation;

import com.nowandfuture.mod.Movement;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

import java.util.*;
import java.util.function.Predicate;

public class KeyFrameLine extends TimeLine {
    //sorted
    public final static String NBT_KEY_FRAMES = "KeyFrames";
    public final static String NBT_KEY_TYPE = "KeyFrameType";

    private Map<KeyFrame.KeyFrameType,SortedSet<KeyFrame>> keyFrames;

    //cur section
    private Map<KeyFrame.KeyFrameType,TimeSection> sections;

    public KeyFrameLine(){
        init();
    }

    private void init(){
        keyFrames = new HashMap<>();
        sections = new HashMap<>();

        for (KeyFrame.KeyFrameType kt :
                KeyFrame.KeyFrameType.values()) {
            SortedSet<KeyFrame> keyFrames = new TreeSet<>();
            this.keyFrames.put(kt,keyFrames);

            sections.put(kt,null);
        }
    }

    public TimeSection getSection(KeyFrame.KeyFrameType keyType){
//        TimeSection section = sections.get(keyType);
//        if(section == null || !section.isInSection(getTick())){
//            sections.put(keyType,getCurSection(keyType));
//        }
        return sections.get(keyType);
    }

    private TimeSection getCurSection(KeyFrame.KeyFrameType keyType){
        return getCurSection(getTick(),getKeyFrames(keyType));
    }

    public double getSectionProgress(KeyFrame.KeyFrameType key, float p){
        TimeSection section = sections.get(key);
        return getSectionProgress(section,p);
    }

    public double getSectionProgress(TimeSection timeSection, float p){
        if(timeSection == null) return 0d;

        final double cur = getFixedTick(p);
        final long begin = timeSection.begin == null ? 0 : timeSection.begin.getBeginTick();
        final long end = timeSection.end == null ? getTotalTick() : timeSection.end.getBeginTick();

        return (cur - begin)/(end - begin);
    }

    public SortedSet<KeyFrame> getKeyFrames(KeyFrame.KeyFrameType keyType){
        return keyFrames.get(keyType);
    }

    public void addKeyFrames(KeyFrame.KeyFrameType keyType ,KeyFrame keyFrame){
        fixKeyFrame(keyFrame);

        getKeyFrames(keyType).add(keyFrame);
    }

    public void addKeyFrames(KeyFrame.KeyFrameType keyType ,KeyFrame keyFrame,long time){
        keyFrame.setBeginTick(time);
        fixKeyFrame(keyFrame);

        getKeyFrames(keyType).add(keyFrame);
    }

    public void deleteKeyFrame(KeyFrame.KeyFrameType keyType ,KeyFrame keyFrame){
        getKeyFrames(keyType).remove(keyFrame);
    }

    public void deleteKeyFrame(KeyFrame.KeyFrameType keyType ,long time){
        getKeyFrames(keyType).removeIf(new Predicate<KeyFrame>() {
            @Override
            public boolean test(KeyFrame keyFrame) {
                return keyFrame.getBeginTick() == time;
            }
        });
    }

    public void deleteAll(KeyFrame.KeyFrameType keyType){
        getKeyFrames(keyType).clear();
    }

    private void fixKeyFrame(KeyFrame keyFrame){
        if(keyFrame.getBeginTick() < 0 ) keyFrame.setBeginTick(0);
        if(keyFrame.getBeginTick() > getTotalTick()) keyFrame.setBeginTick(getTotalTick());
    }

    private TimeSection getCurSection(long curTick,SortedSet<KeyFrame> list){

        Iterator<KeyFrame> iterable = list.iterator();
        KeyFrame pre = null,now = null;

        while (iterable.hasNext()){
            now = iterable.next() ;
            if(now.getBeginTick() > curTick){
                break;
            }
            pre = now;
        }

        if(pre == null && now == null){
            return TimeSection.EMPTY();
        }

        return new TimeSection(pre,now);
    }

    @Override
    public NBTTagCompound serializeNBT(NBTTagCompound compound) {

        NBTTagCompound map = new NBTTagCompound();

        for (KeyFrame.KeyFrameType kt :
                KeyFrame.KeyFrameType.values()) {

            SortedSet<KeyFrame> keyFrames = getKeyFrames(kt);

            NBTTagList tagList = new NBTTagList();
            for (KeyFrame kf :
                    keyFrames) {
                tagList.appendTag(kf.writeParametersToNBT(new NBTTagCompound()));
            }
            map.setTag(NBT_KEY_TYPE.concat(kt.name()),tagList);
        }

        compound.setTag(NBT_KEY_FRAMES,map);

        return super.serializeNBT(compound);
    }

    @Override
    public void deserializeNBT(NBTTagCompound compound) {

        init();
        NBTTagCompound map = compound.getCompoundTag(NBT_KEY_FRAMES);

        if(!map.hasNoTags()){
            for (KeyFrame.KeyFrameType kt :
                    KeyFrame.KeyFrameType.values()) {

                NBTTagList nbtTagList =
                        map.getTagList(NBT_KEY_TYPE.concat(kt.name()),10);

                if(!nbtTagList.hasNoTags())
                    for(int i = 0;i < nbtTagList.tagCount();i++) {
                        NBTTagCompound tagCompound = nbtTagList.getCompoundTagAt(i);
                        KeyFrame keyFrame = KeyFrame.Factory.create(kt,tagCompound);
                        getKeyFrames(kt).add(keyFrame);
                    }
            }
        }

        super.deserializeNBT(compound);
    }

    @Override
    public boolean update() {
        super.update();

        if(isEnable())
            updateSections();
        return isEnable();
    }

    private void updateSections(){
        TimeSection section;
        for (KeyFrame.KeyFrameType kt :
                KeyFrame.KeyFrameType.values()) {
            section = sections.get(kt);
            if(section == null || !section.isInSection(getTick())){
                sections.put(kt,getCurSection(kt));
            }
        }
    }

    public static class TimeSection{

        private KeyFrame begin;
        private KeyFrame end;

        public static TimeSection EMPTY(){
            return new TimeSection();
        }

        public boolean isEmpty(){
            return begin == null && end == null;
        }

        private TimeSection(){
        }

        public boolean isInSection(double cur){
            if(begin == null && end != null)
                return cur <= end.getBeginTick();
            else if(begin != null && end == null)
                return cur >= begin.getBeginTick();
            else if(begin != null)
                return cur >= begin.getBeginTick() && cur <= end.getBeginTick();
            else
                return true;
        }

        //allow only one nullable
        public TimeSection(KeyFrame begin, KeyFrame end) {
            this.begin = begin;
            this.end = end;
        }

        public KeyFrame getBegin() {
            return begin;
        }

        public void setBegin(KeyFrame begin) {
            this.begin = begin;
        }

        public KeyFrame getEnd() {
            return end;
        }

        public void setEnd(KeyFrame end) {
            this.end = end;
        }
    }
}
