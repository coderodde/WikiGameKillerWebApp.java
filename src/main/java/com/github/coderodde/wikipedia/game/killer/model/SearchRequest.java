package com.github.coderodde.wikipedia.game.killer.model;

public final class SearchRequest {
    
    public static final String SEARCH_ACTION = "search";
    public static final String HALT_ACTION = "halt";
    
    private String action;
    private String sourceUrl;
    private String targetUrl;
    private int numberOfThreads;
    private int expansionDuration;
    private int waitTimeout;
    private int masterTrials;
    private int masterSleepDuration;
    private int slaveSleepDuration;

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }
    
    public String getSourceUrl() {
        return sourceUrl;
    }

    public void setSourceUrl(String sourceUrl) {
        this.sourceUrl = sourceUrl;
    }

    public String getTargetUrl() {
        return targetUrl;
    }

    public void setTargetUrl(String targetUrl) {
        this.targetUrl = targetUrl;
    }

    public int getNumberOfThreads() {
        return numberOfThreads;
    }

    public void setNumberOfThreads(int numberOfThreads) {
        this.numberOfThreads = numberOfThreads;
    }

    public int getExpansionDuration() {
        return expansionDuration;
    }

    public void setExpansionDuration(int expansionDuration) {
        this.expansionDuration = expansionDuration;
    }

    public int getWaitTimeout() {
        return waitTimeout;
    }

    public void setWaitTimeout(int waitTimeout) {
        this.waitTimeout = waitTimeout;
    }

    public int getMasterTrials() {
        return masterTrials;
    }

    public void setMasterTrials(int masterTrials) {
        this.masterTrials = masterTrials;
    }

    public int getMasterSleepDuration() {
        return masterSleepDuration;
    }

    public void setMasterSleepDuration(int masterSleepDuration) {
        this.masterSleepDuration = masterSleepDuration;
    }

    public int getSlaveSleepDuration() {
        return slaveSleepDuration;
    }

    public void setSlaveSleepDuration(int slaveSleepDuration) {
        this.slaveSleepDuration = slaveSleepDuration;
    }
}
