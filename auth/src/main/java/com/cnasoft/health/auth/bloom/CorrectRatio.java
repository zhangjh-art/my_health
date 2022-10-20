package com.cnasoft.health.auth.bloom;


import com.google.common.collect.Lists;

import java.util.List;

/**
 * @Created by lgf on 2022/4/12.
 */
public class CorrectRatio {

    private static final List<Integer> SEEDS = Lists.newArrayList(2, 3, 5, 7, 11, 13, 17, 19, 23, 29, 31, 37, 41,
            43, 47, 53, 59, 61, 67, 71, 73, 79, 83, 89, 97, 101, 103, 107, 109, 113, 127, 131);

    private Integer[] seed = new Integer[0];

    public CorrectRatio(int size) {
        this.seed = SEEDS.subList(0, size).toArray(seed);
    }

    public Integer[] getSeed() {
        return seed;
    }
}

