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
    public List<String> urlPath;
    public long duration;
    public int numberOfExpandedNodes;
    public String languageCode;
    public List<String> errorMessages = new ArrayList<>();
    public List<String> infoMessages  = new ArrayList<>();

    public static final class SearchParameters {

        public String sourceUrl;
        public String targetUrl;
        public int numberOfThreads;
        public int expansionDuration;
        public long waitTimeout;
        public int masterTrials;
        public long masterSleepDuration;
        public long slaveSleepDuration;
    }
}
