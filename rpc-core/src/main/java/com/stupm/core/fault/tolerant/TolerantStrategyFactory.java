package com.stupm.core.fault.tolerant;

import com.stupm.core.utils.SpiLoader;

public class TolerantStrategyFactory {
    static{
        SpiLoader.load(TolerantStrategy.class);
    }

    private static final TolerantStrategy DEFAULT = new FailFastTolerantStrategy();

    public static TolerantStrategy getInstance(String key){
        return SpiLoader.getInstance(TolerantStrategy.class, key);
    }
}
