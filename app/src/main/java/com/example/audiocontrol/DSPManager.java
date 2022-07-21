package com.example.audiocontrol;

import java.util.HashMap;
import java.util.Map;

public class DSPManager {

    private class LevelSet {
        public Integer minLevel;
        public Integer maxLevel;
        public Integer currentLevel;

        public LevelSet(Integer minLevel, Integer maxLevel, Integer currentLevel) {
            this.minLevel = minLevel;
            this.maxLevel = maxLevel;
            this.currentLevel = currentLevel;
        }
    }
    private final Map<Integer, LevelSet> levelSetMap = new HashMap<>();
    private final Integer defaultVolumeMinLevel = 0;
    private final Integer defaultVolumeMaxLevel = 100;
    private final Integer defaultVolumeCurrentLevel = 0;
    private final Integer defaultToneMinLevel = -50;
    private final Integer defaultToneMaxLevel = 50;
    private final Integer defaultToneCurrentLevel = 0;

    private LevelSet createVolumeDefaultLevelSet() {
        return new LevelSet(defaultVolumeMinLevel, defaultVolumeMaxLevel, defaultVolumeCurrentLevel);
    }

    private LevelSet createToneDefaultLevelSet() {
        return new LevelSet(defaultToneMinLevel, defaultToneMaxLevel, defaultToneCurrentLevel);
    }

    private LevelSet createDefaultLevelSet(int type) {
        if (type == AudioType.VOLUME.nameId) {
            return createVolumeDefaultLevelSet();
        } else {
            return createToneDefaultLevelSet();
        }
    }

    private LevelSet getOrCreateLevelSet(int type) {
        if (levelSetMap.containsKey(type)) {
            return levelSetMap.get(type);
        } else {
            LevelSet levelSet =  createDefaultLevelSet(type);
            levelSetMap.put(type, levelSet);
            return levelSet;
        }
    }

    public void setCurrentLevel(int type, int level) {
        LevelSet levelSet = getOrCreateLevelSet(type);
        levelSet.currentLevel = level;
   }

    public int getMaxLevel(int type) {
        LevelSet levelSet = getOrCreateLevelSet(type);
        return levelSet.maxLevel;
    }

    public int getMinLevel(int type) {
        LevelSet levelSet = getOrCreateLevelSet(type);
        return levelSet.minLevel;
    }

    public int getCurrentLevel(int type) {
        LevelSet levelSet = getOrCreateLevelSet(type);
        return levelSet.currentLevel;
    }
}
