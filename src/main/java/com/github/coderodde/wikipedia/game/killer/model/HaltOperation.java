package com.github.coderodde.wikipedia.game.killer.model;

public final class HaltOperation {
    
    private String status;
    private long duration;
    private int numberOfExpandedNodes;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public int getNumberOfExpandedNodes() {
        return numberOfExpandedNodes;
    }

    public void setNumberOfExpandedNodes(int numberOfExpandedNodes) {
        this.numberOfExpandedNodes = numberOfExpandedNodes;
    }
}
