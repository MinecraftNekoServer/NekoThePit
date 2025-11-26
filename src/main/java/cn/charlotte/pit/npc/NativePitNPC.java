package cn.charlotte.pit.npc;

/**
 * 原生NPC和抽象NPC的桥接类
 */
public class NativePitNPC {
    private final NativeNPC nativeNPC;
    private final AbstractPitNPC abstractPitNPC;

    public NativePitNPC(NativeNPC nativeNPC, AbstractPitNPC abstractPitNPC) {
        this.nativeNPC = nativeNPC;
        this.abstractPitNPC = abstractPitNPC;
    }

    public NativeNPC getNativeNPC() {
        return nativeNPC;
    }

    public AbstractPitNPC getAbstractPitNPC() {
        return abstractPitNPC;
    }
}