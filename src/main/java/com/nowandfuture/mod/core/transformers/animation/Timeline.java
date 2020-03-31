package com.nowandfuture.mod.core.transformers.animation;

import com.nowandfuture.mod.core.transformers.TimeInterpolation;
import net.minecraft.nbt.NBTTagCompound;

import java.io.Serializable;

public class Timeline {

    public static final String NBT_ANM_LINE_ENABLE = "LineEnable";
    public static final String NBT_ANM_LINE_TOTAL = "TotalTick";
    public static final String NBT_ANM_LINE_TICK = "Tick";
    public static final String NBT_ANM_LINE_STEP = "Step";
    public static final String NBT_ANM_LINE_MODE = "Mode";

    private boolean enable;

    public final static int DEFAULT_STEP = 1;

    private long totalTick;
    private long tick;
    private int step;
    private Mode mode;//Cycle 0:restart 1:back || Not Cycle 2:once 3:one cycle 4:do nothing

    public enum Mode implements Serializable{
        CYCLE_RESTART(0),
        CYCLE_BACK(1),
        ONE_TIME(2),
        ONE_CYCLE(3),
        STOP(4);

        public int modeValue;

        Mode(int modeValue){
            this.modeValue = modeValue;
        }

        public Mode getMode(int modeValue){
            switch (modeValue){
                case 0: return CYCLE_RESTART;
                case 1: return CYCLE_BACK;
                case 2: return ONE_TIME;
                case 3: return ONE_CYCLE;
                default:
                    return STOP;
            }
        }
    }

    @Override
    public Timeline clone(){
        return new Timeline(this);
    }

    private Timeline(Timeline timeLine) {
        this.enable = timeLine.enable;
        this.totalTick = timeLine.totalTick;
        this.tick = timeLine.tick;
        this.step = timeLine.step;
        this.mode = timeLine.mode;
    }

    public Timeline(){
        enable = false;
        step = DEFAULT_STEP;
        mode = Mode.CYCLE_RESTART;
        tick = 0;
        totalTick = 100;
    }

    public double getProgress(float p){
        if(mode == Mode.STOP || step == 0 || !enable) return (float)tick/(float)totalTick;

        long nextTick = update(tick,totalTick,step,mode,true);
        if(mode == Mode.CYCLE_RESTART && nextTick < tick)
            nextTick += totalTick;
        return (tick + (nextTick - tick) * p) / (float)totalTick;
    }

    public double getFixedTick(float p){
        if(mode == Mode.STOP || step == 0 || !enable) return tick;

        long nextTick = update(tick,totalTick,step,mode,true);

        if(mode == Mode.CYCLE_RESTART && nextTick < tick)
            nextTick += totalTick;

        return tick + (nextTick - tick) * p;
    }

    public double getProgress(TimeInterpolation interpolation, float p){
        if(interpolation != null)
            return interpolation.interpolate(getProgress(p));
        return getProgress(p);
    }

    public boolean update(){
        long temp = this.tick;
        if(enable)
            this.tick = update(tick,totalTick,step,mode,false);
        return temp != this.tick;
    }

    private long update(long tick , long totalTick , int step, Mode mode, boolean test){
        if(mode == Mode.STOP || step == 0 || !enable) return tick;

        switch (mode){
            case CYCLE_RESTART:
                if(tick + step >= totalTick)
                    tick += step - totalTick;
                else{
                    tick += step;
                }
                break;
            case CYCLE_BACK:
                if(tick + step >= totalTick) {
                    tick = 2 * totalTick - tick - step;
                    if(!test)
                        this.step = -step;
                }else if(tick + step <= 0){
                    tick = - tick - step;
                    if(!test)
                        this.step = - step;
                }else{
                    tick += step;
                }
                break;
            case ONE_TIME:
                if(tick + step >= totalTick) {
                    tick = totalTick;
                    if(!test) {
                        this.enable = false;
                        this.step = -step;
                    }
                }else if(tick + step <= 0){
                    tick = 0;
                    if(!test) {
                        this.enable = false;
                        this.step = -step;
                    }
                }else{
                    tick += step;
                }
                break;
            case ONE_CYCLE:
                if(tick + step >= totalTick) {
                    tick = 2 * totalTick - tick - step;
                    if(!test)
                        this.step = - step;
                }else if(tick + step <= 0){
                    tick = 0;
                    if(!test) {
                        this.step = -step;
                        this.enable = false;
                    }
                }else{
                    tick += step;
                }
                break;
        }

        return tick;
    }

    public long getTotalTick() {
        return totalTick;
    }

    public Timeline setTotalTick(long totalTick) {
        if(totalTick <= 0) totalTick = 100;
        this.totalTick = totalTick;
        return this;
    }

    public long getTick() {
        return tick;
    }

    public Timeline setTick(long tick) {
        if(tick < 0) tick = 0;
        if(tick > totalTick) tick = totalTick;
        this.tick = tick;
        return this;
    }

    public void resetTick(){
        this.tick = 0;
    }

    public void reset(){
        step = 1;
        resetTick();
        enable = false;
    }

    public void toMaxTick(){
        this.tick = totalTick;
    }

    public Timeline setStep(int step) {
        if(step > totalTick)
            step = (int) totalTick;
        else if(step < -totalTick){
            step = (int) - totalTick;
        }
        this.step = step;
        return this;
    }

    public int getStep() {
        return step;
    }

    public Timeline setMode(Mode mode) {
        if(Mode.ONE_CYCLE == mode){
            resetTick();
        }
        this.mode = mode;
        return this;
    }

    public Mode getMode() {
        return mode;
    }

    public void start(){
        if(mode == Mode.STOP)
            mode = Mode.CYCLE_RESTART;
        enable = true;
    }

    public void stop(){
        enable = false;
    }

    public void restart(){
        //reset ONE_TIME step to origin step
        if(Mode.ONE_TIME == this.mode) this.step = Math.abs(this.step);
        resetTick();
        start();
    }

    public boolean isEnable() {
        return enable;
    }

    public Timeline setEnable(boolean enable) {
        this.enable = enable;
        return this;
    }

    public void deserializeNBT(NBTTagCompound compound){
        tick = compound.getLong(NBT_ANM_LINE_TICK);
        totalTick = compound.getLong(NBT_ANM_LINE_TOTAL);
        step = compound.getInteger(NBT_ANM_LINE_STEP);
        enable = compound.getBoolean(NBT_ANM_LINE_ENABLE);
        mode = mode.getMode(compound.getInteger(NBT_ANM_LINE_MODE));
    }

    public NBTTagCompound serializeNBT(NBTTagCompound compound){
        compound.setLong(NBT_ANM_LINE_TICK,tick);
        compound.setLong(NBT_ANM_LINE_TOTAL,totalTick);
        compound.setInteger(NBT_ANM_LINE_STEP,step);
        compound.setBoolean(NBT_ANM_LINE_ENABLE,enable);
        compound.setInteger(NBT_ANM_LINE_MODE,mode.modeValue);
        return compound;
    }



}
