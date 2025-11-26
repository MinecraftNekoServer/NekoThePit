package cn.charlotte.pit.util.hologram.reflection;

import java.lang.reflect.Field;

public abstract class NMUClass {

    public static Class<?> gnu_trove_map_TIntObjectMap;
    public static Class<?> gnu_trove_map_hash_TIntObjectHashMap;
    public static Class<?> gnu_trove_impl_hash_THash;
    public static Class<?> io_netty_channel_Channel;
    private static boolean initialized = false;

    static {
        if (!initialized) {
            for (Field f : NMUClass.class.getDeclaredFields()) {
                if (f.getType().equals(Class.class)) {
                    try {
                        String name = f.getName().replace("_", ".");
                        Class<?> clazz;
                        
                        if (Reflection.getVersion().contains("1_8")) {
                            clazz = Class.forName(name);
                        } else {
                            try {
                                // 首先尝试加载 net.minecraft.util 下的类
                                clazz = Class.forName("net.minecraft.util." + name);
                            } catch (ClassNotFoundException e) {
                                // 如果失败，尝试直接加载类（用于较老的版本）
                                clazz = Class.forName(name);
                            }
                        }
                        
                        f.set(null, clazz);
                    } catch (Exception e) {
                        // 静默处理类找不到的情况，不打印异常信息
                        try {
                            f.set(null, null); // 设置为 null 表示该类不可用
                        } catch (IllegalAccessException ex) {
                            // 如果无法设置字段，忽略
                        }
                    }
                }
            }
            initialized = true;
        }
    }

}
