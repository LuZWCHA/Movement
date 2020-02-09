package com.nowandfuture.mod.core.transformers.animation;

import com.nowandfuture.mod.utils.math.MathHelper;
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
        super();
        init();
    }

    public KeyFrameLine clone(){
        KeyFrameLine clone = new KeyFrameLine();
        clone.setEnable(isEnable());
        clone.setTotalTick(getTotalTick());
        clone.setTick(getTick());
        clone.setStep(getStep());
        clone.setMode(getMode());
        keyFrames.forEach((type, keyFrames) ->
                keyFrames.forEach(keyFrame -> {
            clone.addKeyFrame(type,keyFrame.clone());
        }));
        return clone;
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

    public Optional<KeyFrame> getKeyFrame(KeyFrame.KeyFrameType keyType ,long time){
        for (KeyFrame kf :
                getKeyFrames(keyType)) {
            if (time == kf.getBeginTick()){
                return Optional.of(kf);
            }
        }
        return Optional.empty();
    }

    public Optional<KeyFrame> getKeyFrameNearest(KeyFrame.KeyFrameType keyType ,long time,float acc){
        for (KeyFrame kf :
                getKeyFrames(keyType)) {
            if (MathHelper.approximate(time,kf.getBeginTick(),acc)){
                return Optional.of(kf);
            }
        }
        return Optional.empty();
    }

    public Optional<KeyFrame> getPreFrame(KeyFrame frame){
        Optional<KeyFrame> pre = Optional.empty();
        for (KeyFrame kf :
                getKeyFrames(KeyFrame.KeyFrameType.as(frame.type))) {
            if(frame.equals(kf))
                return pre;
            pre = Optional.of(kf);
        }
        return Optional.empty();
    }

    public Optional<KeyFrame> getNextFrame(KeyFrame frame){
        boolean reach = false;
        for (KeyFrame kf :
                getKeyFrames(KeyFrame.KeyFrameType.as(frame.type))) {
            if(reach){
                return Optional.of(kf);
            }
            if(frame.equals(kf)) {
                reach = true;
            }
        }
        return Optional.empty();
    }

    //------------------------------------------------------------------------------------------------------------------

    public KeyFrameLine addKeyFrame(KeyFrame.KeyFrameType keyType , KeyFrame... keyFrames){
        for (KeyFrame kf :
                keyFrames) {
            fixKeyFrame(kf);

            getKeyFrames(keyType).add(kf);
        }
        updateSections(true);
        return this;
    }

    public KeyFrameLine addKeyFrame(KeyFrame.KeyFrameType keyType , KeyFrame keyFrame){
        fixKeyFrame(keyFrame);

        getKeyFrames(keyType).add(keyFrame);
        updateSections(true);
        return this;
    }

    public KeyFrameLine addKeyFrame(KeyFrame.KeyFrameType keyType , KeyFrame keyFrame, long time){
        keyFrame.setBeginTick(time);
        fixKeyFrame(keyFrame);

        getKeyFrames(keyType).add(keyFrame);
        updateSections(true);
        return this;
    }

    public KeyFrameLine deleteKeyFrame(KeyFrame.KeyFrameType keyType ,KeyFrame keyFrame){
        getKeyFrames(keyType).remove(keyFrame);
        updateSections(true);
        return this;
    }

    public KeyFrameLine deleteKeyFrame(KeyFrame.KeyFrameType keyType ,long time){
        getKeyFrames(keyType).removeIf(new Predicate<KeyFrame>() {
            @Override
            public boolean test(KeyFrame keyFrame) {
                return keyFrame.getBeginTick() == time;
            }
        });
        updateSections(true);
        return this;
    }

    public KeyFrameLine deleteKeyFrameNearest(KeyFrame.KeyFrameType keyType ,long time,float acc){

        getKeyFrames(keyType)
                .removeIf(new Predicate<KeyFrame>() {
                    @Override
                    public boolean test(KeyFrame keyFrame) {
                        return MathHelper.approximate(keyFrame.getBeginTick(),time,acc);
                    }
                });
        updateSections(true);
        return this;
    }

    public KeyFrameLine deleteAll(KeyFrame.KeyFrameType keyType){
        getKeyFrames(keyType).clear();
        updateSections(true);
        return this;
    }

    public void reset(){
        init();
    }

    private void fixKeyFrame(KeyFrame keyFrame){
        if(keyFrame.getBeginTick() < 0 ) keyFrame.setBeginTick(0);
        if(keyFrame.getBeginTick() > getTotalTick()) keyFrame.setBeginTick(getTotalTick());
    }

    //-----------------------------------------------------------------------------------------------------------

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

        updateSections(true);
    }

    @Override
    public boolean update() {
        boolean change = super.update();

        if(change) {
            updateSections(false);
            return true;
        }
        return false;
    }

    public boolean update(long tick) {

        if(tick != getTick()) {
            setTick(tick);
            updateSections(false);
            return true;
        }
        return false;
    }

    private void updateSections(boolean force){ ;
        TimeSection section;
        for (KeyFrame.KeyFrameType kt :
                KeyFrame.KeyFrameType.values()) {
            section = sections.get(kt);
            if(force || section == null || !section.isInSection(getTick())){
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
