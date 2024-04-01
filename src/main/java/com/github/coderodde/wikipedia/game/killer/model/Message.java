package com.github.coderodde.wikipedia.game.killer.model;

import java.util.ArrayList;
import java.util.List;

public final class Message {

    public static final String SEARCH_ACTION = "search";
    public static final String HALT_ACTION   = "halt";
    public static final String ERROR_ACTION  = "error";

    public String status;
    public String action;
    public SearchParameters searchParameters;
    public List<String> errorMessages = new ArrayList<>();
    public List<String> infoMessages  = new ArrayList<>();

    public static final class SearchParameters {

        public String sourceUrl;
        public String targetUrl;
        public int numberOfThreads;
        public int expansionDuration;
        public int waitTimeout;
        public int masterTrials;
        public int masterSleepDuration;
        public int slaveSleepDuration;
    }
}
