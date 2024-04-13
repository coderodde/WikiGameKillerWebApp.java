package com.github.coderodde.wikipedia.game.killer;

import com.github.coderodde.wikipedia.game.killer.model.decoders.MessageDecoder;
import com.github.coderodde.wikipedia.game.killer.model.encoders.MessageEncoder;
import com.github.coderodde.wikipedia.game.killer.model.Message;
import com.github.coderodde.graph.pathfinding.delayed.AbstractNodeExpander;
import com.github.coderodde.graph.pathfinding.delayed.impl.ThreadPoolBidirectionalBFSPathFinder;
import com.github.coderodde.graph.pathfinding.delayed.impl.ThreadPoolBidirectionalBFSPathFinderBuilder;
import com.github.coderodde.graph.pathfinding.delayed.impl.ThreadPoolBidirectionalBFSPathFinderSearchBuilder;
import com.github.coderodde.wikipedia.graph.expansion.BackwardWikipediaGraphNodeExpander;
import com.github.coderodde.wikipedia.graph.expansion.ForwardWikipediaGraphNodeExpander;
import com.google.gson.Gson;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import javax.enterprise.context.ApplicationScoped;
import javax.websocket.EncodeException;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

@ServerEndpoint(value = "/search", decoders = MessageDecoder.class, encoders = MessageEncoder.class )
public class SearchEndpoint {
    
    /**
     * The Wikipedia URL format.
     */
    private static final String WIKIPEDIA_URL_FORMAT =
            "^((http:\\/\\/)|(https:\\/\\/))?..\\.wikipedia\\.org\\/wiki\\/.+$";
    
    /**
     * The Wikipedia URL regular expression pattern object.
     */
    private static final Pattern WIKIPEDIA_URL_FORMAT_PATTERN = 
            Pattern.compile(WIKIPEDIA_URL_FORMAT);

    private static final long CONNECTION_TIMEOUT_MILLIS = 1000 * 120; // 120 s.
    
    private static final Map<Session, SearchThread> SESSION_TO_THRED_MAP = 
                new ConcurrentHashMap<>();
    
    private static final Logger LOGGER = 
            Logger.getLogger(SearchEndpoint.class.getSimpleName());
    
    private static final Gson GSON = new Gson();
    
    @OnOpen
    public void onOpen(final Session session) throws IOException, 
                                                     EncodeException {
        
        session.setMaxIdleTimeout(CONNECTION_TIMEOUT_MILLIS);
        
        LOGGER.log(Level.INFO, 
                   "Successfully connected to {0}.", 
                   SearchEndpoint.this.getClass().getName());
    }
    
    @OnMessage
    public void onMessage(final Session session, 
                          final Message incomingMessage) 
            throws IOException, EncodeException {
        
        final SearchThread searchThread = SESSION_TO_THRED_MAP.get(session);
        final String action = incomingMessage.action;
        
        switch (action) {
            case Message.HALT_ACTION:
                if (searchThread == null) {
                    final Message message = new Message();
                    message.status = "error";
                    message.errorMessages.add(
                            "Trying to halt a search process that does not" + 
                            " exist. Please stop hacking.");
                    
                    session.getBasicRemote().sendText(GSON.toJson(message));
                    
                    LOGGER.log(
                            Level.WARNING, 
                            "Halting while there is no current search process.");
                } else {
                    searchThread.finder.halt();
                    
                    try {
                        searchThread.join();
                    } catch (final InterruptedException ex) {
                        
                    }
                    
                    final Message message = new Message();
                    
                    message.status = "halted";
                    message.duration = searchThread.finder.getDuration();
                    message.numberOfExpandedNodes =
                            searchThread
                                    .finder
                                    .getNumberOfExpandedNodes();
                    
                    message.searchParameters = new Message.SearchParameters();
                    message.searchParameters.sourceUrl =
                            incomingMessage
                            .searchParameters
                            .sourceUrl;
                    
                    message.searchParameters.targetUrl = 
                            incomingMessage
                            .searchParameters
                            .targetUrl;
                    
                    message.infoMessages.add(
                        String.format(
                            "Successfully halted the search from \"%s\" to \"%s\".", 
                            incomingMessage.searchParameters.sourceUrl,
                            incomingMessage.searchParameters.targetUrl));
                    
                    session.getBasicRemote().sendText(GSON.toJson(message));
                    
                    LOGGER.log(
                            Level.INFO, 
                            "Halting the search process from \"{0}\" to \"{1}\".",
                            new Object[]{
                                incomingMessage.searchParameters.sourceUrl,
                                incomingMessage.searchParameters.targetUrl,
                            });
                }
                
                return;
                
            case Message.SEARCH_ACTION:
                if (searchThread == null) {
                    LOGGER.log(
                            Level.INFO,
                            "Beginning search from \"{0}\" to \"{1}\".",
                            new Object[]{
                                incomingMessage.searchParameters.sourceUrl,
                                incomingMessage.searchParameters.targetUrl,
                            });
                    
                    final SearchThread thread = 
                            new SearchThread(session,
                                             incomingMessage);
                    
                    if (thread.isCorrect()) {
                        SESSION_TO_THRED_MAP.put(session, thread);

                        thread.start();
                    }
                } else {
                    final Message message = new Message();
                    message.status = "error";
                    message.errorMessages
                           .add("Cannot run a new search while the previous did not finish.");
                    
                    LOGGER.log(
                            Level.WARNING,
                            "A search process is still running. " + 
                                    "Please stop hacking.");
                    
                    session.getBasicRemote().sendText(GSON.toJson(message));
                }
                
                return;
                
            default:
                throw new IllegalStateException("Should not get here.");
        }
    }
    
    @OnClose
    public void onClose(final Session session) throws IOException,
                                                      EncodeException {
        LOGGER.log(Level.WARNING, "Session closed.");
        SESSION_TO_THRED_MAP.remove(session);
    }
    
    @OnError
    public void onError(final Session session, final Throwable throwable) {
        LOGGER.log(Level.SEVERE, 
                   "Error in {0}: {1}", 
                   new Object[]{ SearchEndpoint.this.getClass().getName(), 
                                 throwable.getMessage() 
                   });
        
        SESSION_TO_THRED_MAP.remove(session);
    }
    
    private void halt(final Session session) throws IOException {
        final SearchThread searchThread = SESSION_TO_THRED_MAP.get(session);
        
        if (searchThread == null) {
            LOGGER.log(Level.INFO, "No thread for session {0}.", session);
            return;
        }
        
        searchThread.finder.halt();
        
        try {
            searchThread.join();
        } catch (final InterruptedException ex) {
            LOGGER.log(Level.WARNING, 
                       "Search thread interrupted: {0}.", 
                       ex.getMessage());
        }
        
        SESSION_TO_THRED_MAP.remove(session);
        
        final HaltJsonObject haltJsonObject = new HaltJsonObject();
        
        haltJsonObject.status = "halt";
        haltJsonObject.duration = searchThread.finder.getDuration();
        haltJsonObject.numberOfExpandedNodes = 
                searchThread.finder.getNumberOfExpandedNodes();
        
        session.getBasicRemote().sendText(new Gson().toJson(haltJsonObject));
    }
    
    private void search(final Session session, 
                        final Message searchRequest) throws IOException {
        
        final SearchThread searchThread =
                new SearchThread(session,
                                 searchRequest);
        
        SESSION_TO_THRED_MAP.put(session, searchThread);
        
        searchThread.start();
    }
    
    private static final class SearchThread extends Thread {
        private boolean isCorrect = false;
        private final Session session;
        private ThreadPoolBidirectionalBFSPathFinder<String> finder;
        private AbstractNodeExpander<String> forwardNodeExpander;
        private AbstractNodeExpander<String> backwardNodeExpander;
        private String sourceLanguageCode;
        private String targetLanguageCode;
        private String sourceTitle;
        private String targetTitle;
        
        SearchThread(final Session session, 
                     final Message message) throws IOException {
            
            this.session = session;
            
            final String sourceUrl = message.searchParameters.sourceUrl.trim();
            final String targetUrl = message.searchParameters.targetUrl.trim();
            final List<Exception> exceptionList = new ArrayList<>();
            final Message responseMessage = new Message();
            
            try {
                checkSourceUrl(sourceUrl);
            } catch (final IllegalArgumentException ex) {
                exceptionList.add(ex);
            }
            
            try {
                checkTargetUrl(targetUrl);
            } catch (final IllegalArgumentException ex) {
                exceptionList.add(ex);
            }
            
            if (!exceptionList.isEmpty()) {
                responseMessage.status = "error";
                responseMessage.errorMessages = toErrorMessages(exceptionList);
                session.getBasicRemote().sendText(GSON.toJson(responseMessage));
                return;
            }
            
            sourceTitle = extractArticleTitle(sourceUrl);
            targetTitle = extractArticleTitle(targetUrl);
            
            boolean sourceArticleValid = true;
            boolean targetArticleValid = true;
            
            sourceLanguageCode = getLanguageCode(sourceUrl);
            targetLanguageCode = getLanguageCode(targetUrl);
            
            forwardNodeExpander  = new ForwardLinkExpander (sourceLanguageCode);
            backwardNodeExpander = new BackwardLinkExpander(targetLanguageCode);
            
            try {
                if (!forwardNodeExpander.isValidNode(sourceTitle)) {
                    sourceArticleValid = false;
                }
            } catch (final Exception ex) {
                sourceArticleValid = false;
            }
            
            if (sourceArticleValid == false) {
                responseMessage.errorMessages.add(
                        String.format(
                            "The source article \"%s\" was rejected by " + 
                            "Wikipedia API.", 
                            sourceUrl));
            }
            
            try {
                if (!backwardNodeExpander.isValidNode(targetTitle)) {
                    targetArticleValid = false;
                }
            } catch (final Exception ex) {
                targetArticleValid = false;
            }
            
            if (targetArticleValid == false) {
                responseMessage.errorMessages.add(
                        String.format(
                            "The target article \"%s\" was rejected by " + 
                            "Wikipedia API.", 
                            targetUrl));
            }
            
            if (sourceUrl.equals(targetUrl)) {
                responseMessage.errorMessages.add(
                        String.format(
                            "The source and target article URLs are same: "
                            + "\"%s\".", 
                            sourceUrl));
            }
            
            if (!sourceLanguageCode.equals(targetLanguageCode)) {
                responseMessage.errorMessages.add(
                        String.format(
                            "Different language codes: \"%s\" vs \"%s\".", 
                            sourceLanguageCode,
                            targetLanguageCode));
            }
            
            if (!responseMessage.errorMessages.isEmpty()) {
                responseMessage.status = "error";
                session.getBasicRemote().sendText(GSON.toJson(responseMessage));
                return;
            }
            
            final Message.SearchParameters searchParameters = 
                    message.searchParameters;
            
            this.finder = 
                ThreadPoolBidirectionalBFSPathFinderBuilder.
                    <String>begin()
                    .withNumberOfRequestedThreads        (searchParameters.numberOfThreads)
                    .withExpansionDurationMillis         (searchParameters.expansionDuration)
                    .withLockWaitMillis                  (searchParameters.waitTimeout)
                    .withNumberOfMasterTrials            (searchParameters.masterTrials)
                    .withMasterThreadSleepDurationMillis (searchParameters.masterSleepDuration)
                    .withSlaveThreadSleepDurationMillis  (searchParameters.slaveSleepDuration)
                    .end();
            
            isCorrect = true;
        }
        
        @Override
        public void run() {
            if (finder == null  ) {
                // Once here, parameters are invalid and no search should 
                // happen:
                return;
            }
            
            System.out.println(finder.getExpansionJoinDurationMillis());
            System.out.println(finder.getLockWaitDurationMillis());
            System.out.println(finder.getMasterThreadSleepDurationMillis());
            System.out.println(finder.getMasterThreadTrials());
            System.out.println(finder.getNumberOfThreads());
            System.out.println(finder.getSlaveThreadSleepDurationMillis());
            
            final List<String> path = 
                    ThreadPoolBidirectionalBFSPathFinderSearchBuilder
                    .<String>withPathFinder(finder)
                    .withSourceNode(sourceTitle)
                    .withTargetNode(targetTitle)
                    .withForwardNodeExpander(forwardNodeExpander)
                    .withBackwardNodeExpander(backwardNodeExpander)
                    .search();
            
            LOGGER.log(
                    Level.INFO, 
                    "Found a path from \"{0}\" to \"{1}\": {2}.", 
                    new Object[]{
                        sourceTitle, 
                        targetTitle, 
                        path 
                    });
            
            final Message responseMessage = new Message();
            
            if (finder.wasHalted()) {
                responseMessage.status = "halted";
                responseMessage.duration = finder.getDuration();
                responseMessage.numberOfExpandedNodes =
                        finder.getNumberOfExpandedNodes();
                
                responseMessage.searchParameters = 
                        new Message.SearchParameters();
                
                responseMessage.searchParameters.sourceUrl =
                        buildUrl(sourceLanguageCode, sourceTitle);
                
                responseMessage.searchParameters.targetUrl = 
                        buildUrl(targetLanguageCode, targetTitle);
            } else {
                responseMessage.status = "solutionFound";
                responseMessage.urlPath = path;
                responseMessage.languageCode = sourceLanguageCode;
                responseMessage.duration = finder.getDuration();
                responseMessage.numberOfExpandedNodes =
                        finder.getNumberOfExpandedNodes();    
            }
            
            try {
                session.getBasicRemote().sendText(GSON.toJson(responseMessage));
            } catch (final IOException ex) {
                LOGGER.log(
                        Level.SEVERE, 
                        "Could not send existing path: {0}.", 
                        path.toString());
            }
        }
        
        boolean isCorrect() {
            return isCorrect;
        }
    }
    
    private static List<String> 
        toErrorMessages(final List<Exception> exceptionList) {
        final List<String> errorMessageList = new ArrayList<>();
        
        for (final Exception exception : exceptionList) {
            errorMessageList.add(exception.getMessage());
        }
        
        return errorMessageList;
    }
    
    /**
     * This class implements the forward link expander.
     */
    private static final class ForwardLinkExpander 
            extends AbstractNodeExpander<String> {

        private final ForwardWikipediaGraphNodeExpander expander;
        
        public ForwardLinkExpander(final String languageCode) {
            this.expander = new ForwardWikipediaGraphNodeExpander(languageCode);
        }
        
        /**
         * Generate all the links that this article links to.
         * 
         * @param article the source article of each link.
         * 
         * @return all the article titles that {@code article} links to.
         */
        @Override
        public List<String> generateSuccessors(final String article) {
            try {
                return extractArticleListTitles(expander.getNeighbors(article));
            } catch (Exception ex) {
                return Collections.<String>emptyList();
            }
        }

        /**
         * {@inheritDoc }
         */
        @Override
        public boolean isValidNode(final String article) {
            try {
                return expander.isValidNode(article);
            } catch (Exception ex) {
                return false;
            }
        }
    }
    
    /**
     * This class implements the backward link expander. 
     */
    private static final class BackwardLinkExpander 
            extends AbstractNodeExpander<String> {

        private final BackwardWikipediaGraphNodeExpander expander;
        
        public BackwardLinkExpander(final String languageCode) {
            this.expander = 
                    new BackwardWikipediaGraphNodeExpander(languageCode);
        }
        
        /**
         * Generate all the links pointing to the article {@code article}.
         * 
         * @param article the target article of each link.
         * 
         * @return all the article titles linking to {@code article}.
         * 
         * @throws java.lang.Exception if something fails.
         */
        @Override
        public List<String> generateSuccessors(final String article) {
            try {
                return extractArticleListTitles(expander.getNeighbors(article));
            } catch (Exception ex) {
                return Collections.<String>emptyList();
            }
        }
        
        /**
         * {@inheritDoc }
         */
        @Override
        public boolean isValidNode(final String article) {
            try {
                return expander.isValidNode(article);
            } catch (Exception ex) {
                return false;
            }
        }
    }
    
    private static final class HaltJsonObject {
        String status;
        long duration;
        int numberOfExpandedNodes;
    }
    
    private static final class SolutionJsonObject {
        String status;
        long duration;
        int numberOfExpandedNodes;
        List<String> path;
    }
    
    private static final class ErrorJsonObject {
        String status;
        String errorMessage;
    }
    
    private static String extractArticleTitle(final String url) {
        return url.substring(url.lastIndexOf('/') + 1);
    }
    
    private static List<String> 
        extractArticleListTitles(final List<String> urls) {
        
        final List<String> titleList = new ArrayList<>(urls.size());
        
        for (final String url : urls) {
            titleList.add(extractArticleTitle(url));
        }
        
        return titleList;
    }
    
    private static String getLanguageCode(String url) {
        if (url.startsWith("https://")) {
            url = url.substring("https://".length());
        } else if (url.startsWith("http://")) {
            url = url.substring("http://".length());
        }
            
        return url.substring(0, 2);
    }
    
    /**
     * Checks that the source article URL conforms to a Wikipedia URL regular 
     * language.
     * 
     * @param sourceUrl the source article URL.
     */
    private static void checkSourceUrl(final String sourceUrl) {
        if (!WIKIPEDIA_URL_FORMAT_PATTERN.matcher(sourceUrl).find()) {
            throw new IllegalArgumentException(
                    String.format(
                            "The source URL \"%s\" is invalid.", 
                            sourceUrl));
        }
    }
    
    /**
     * Checks that the source article URL conforms to a Wikipedia URL regular 
     * language.
     * 
     * @param targetUrl the source article URL.
     */
    private static void checkTargetUrl(final String targetUrl) {
        if (!WIKIPEDIA_URL_FORMAT_PATTERN.matcher(targetUrl).find()) {
            throw new IllegalArgumentException(
                    String.format(
                            "The target URL \"%s\" is invalid.", 
                            targetUrl));
        }
    }

    private static final class MultipleException extends RuntimeException {
        
        private final List<Exception> exceptionList = new ArrayList<>();
        
        void addException(final Exception exception) {
            exceptionList.add(exception);
        }
        
        List<Exception> getExceptionList() {
            return exceptionList;
        }
    }

    private static List<String> convertExceptionsToErrorMessages(
            final List<Exception> exceptionList) {
        
        final List<String> errorMessageList = 
                new ArrayList<>(exceptionList.size());
    
        for (final Exception exception : exceptionList) {
            errorMessageList.add(exception.getMessage());
        }
        
        return errorMessageList;
    }
    
    private static String buildUrl(final String languageCode, 
                                   final String title) {
        return String.format("https://%s.wikipedia.org/wiki/%s", 
                             languageCode, 
                             title);
    }
}
