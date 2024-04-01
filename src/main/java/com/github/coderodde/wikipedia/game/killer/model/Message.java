package com.github.coderodde.wikipedia.game.killer.model;

import java.util.ArrayList;
import java.util.List;

public final class Message {

    public static final String SEARCH_ACTION = "search";
    public static final String HALT_ACTION   = "halt";
    public static final String ERROR_ACTION  = "error";

    private String status;
    private String action;
    public SearchParameters searchParameters;
    private List<String> errorMessages = new ArrayList<>();
    private List<String> infoMessages  = new ArrayList<>();

    public SearchParameters getSearchParameters() {
        return searchParameters;
    }

    public void setSearchParameters(SearchParameters searchParameters) {
        this.searchParameters = searchParameters;
    }

    public List<String> getErrorMessages() {
        return errorMessages;
    }

    public void setErrorMessages(List<String> errorMessages) {
        this.errorMessages = errorMessages;
    }

    public List<String> getInfoMessages() {
        return infoMessages;
    }

    public void setInfoMessages(List<String> infoMessages) {
        this.infoMessages = infoMessages;
    }

    public static final class SearchParameters {

        private String sourceUrl;
        private String targetUrl;
        private int numberOfThreads;
        private int expansionDuration;
        private int waitTimeout;
        private int masterTrials;
        private int masterSleepDuration;
        private int slaveSleepDuration;

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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

//    public void addErrorMessage(final String errorMessage) {
//        this.errorMessages.add(errorMessage);
//    }
//
//    public List<String> getErrorMessages() {
//        return errorMessages;
//    }
//
//    public void addInfoMessage(final String infoMessage) {
//        this.infoMessages.add(infoMessage);
//    }
//
//    public List<String> getInfoMessages() {
//        return errorMessages;
//    }
}
