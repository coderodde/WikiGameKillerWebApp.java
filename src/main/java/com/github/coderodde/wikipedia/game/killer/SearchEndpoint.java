package com.github.coderodde.wikipedia.game.killer;

import com.github.coderodde.graph.pathfinding.delayed.AbstractNodeExpander;
import com.github.coderodde.graph.pathfinding.delayed.impl.ThreadPoolBidirectionalBFSPathFinder;
import com.github.coderodde.graph.pathfinding.delayed.impl.ThreadPoolBidirectionalBFSPathFinderBuilder;
import com.github.coderodde.graph.pathfinding.delayed.impl.ThreadPoolBidirectionalBFSPathFinderSearchBuilder;
import com.github.coderodde.wikipedia.graph.expansion.BackwardWikipediaGraphNodeExpander;
import com.github.coderodde.wikipedia.graph.expansion.ForwardWikipediaGraphNodeExpander;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.websocket.EncodeException;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

@ServerEndpoint(value = "/search",
                decoders = SearchRequestDecoder.class,
                encoders = SearchRequestEncoder.class)
public final class SearchEndpoint {
    
    private static final String SOURCE_TARGET_SAME_JSON = 
            """
            {
                "status":"sourceTargetEqual",
                "source":"%s"
            }
            """;
    
    private static final String LANGUAGE_CODES_DIFFERENT = 
            """
            {
                "status":"differentLanguageCodes",
                "sourceLanguage":"%s",
                "targetLanguage":"%s"
            }
            """;
    
    private static final String HALT_JSON = 
            """
            {
                "status":"halt"
            }
            """;
    
    private static final long CONNECTION_TIMEOUT_MILLIS = 1000 * 120; // 120 s.
    
    private static final Map<Session, SearchThread> SESSION_TO_THRED_MAP = 
                new ConcurrentHashMap<>();
    
    private static final Logger LOGGER = 
            Logger.getLogger(SearchEndpoint.class.getSimpleName());
    
    @OnOpen
    public void onOpen(/*final @PathParam("source") String source, 
                       final @PathParam("target") String target,*/
                       final Session session) throws IOException, 
                                                     EncodeException {
        
        session.setMaxIdleTimeout(CONNECTION_TIMEOUT_MILLIS);
        
        LOGGER.log(Level.INFO, 
                   "Successfully connected to {0}.", 
                   SearchEndpoint.this.getClass().getName());
    }
    
    @OnMessage
    public void onMessage(final Session session, 
                          final SearchRequest searchRequest)
            throws IOException, EncodeException {
        
        switch (searchRequest.getAction()) {
            case SearchRequest.SEARCH_ACTION:
                search(session, searchRequest);
                break;
                
            case SearchRequest.HALT_ACTION:
                halt(session);
                break;
                
            default:
                throw new IllegalStateException("Should not happen.");
        }
        
//        System.out.println(searchRequest.getExpansionDuration());
//        System.out.println(searchRequest.getMasterSleepDuration());
//        System.out.println(searchRequest.getMasterTrials());
//        System.out.println(searchRequest.getNumberOfThreads());
//        System.out.println(searchRequest.getSlaveSleepDuration());
//        System.out.println(searchRequest.getSourceUrl());
//        System.out.println(searchRequest.getTargetUrl());
//        System.out.println(searchRequest.getWaitTimeout());
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
        session.getBasicRemote().sendText(HALT_JSON);
    }
    
    private void search(final Session session, 
                        final SearchRequest searchRequest) throws IOException {
        
        final SearchThread searchThread =
                new SearchThread(session,
                                 searchRequest);
        
        SESSION_TO_THRED_MAP.put(session, searchThread);
        
        searchThread.start();
    }
    
    private static final class SearchThread extends Thread {
        private ThreadPoolBidirectionalBFSPathFinder<String> finder;
        private AbstractNodeExpander<String> forwardNodeExpander;
        private AbstractNodeExpander<String> backwardNodeExpander;
        private final SearchRequest searchRequest;
        private String sourceLanguageCode;
        private String targetLanguageCode;
        private String sourceTitle;
        private String targetTitle;
        
        SearchThread(final Session session, 
                     final SearchRequest searchRequest) throws IOException {
            
            this.searchRequest = searchRequest;
            
            final String sourceUrl = searchRequest.getSourceUrl().trim();
            final String targetUrl = searchRequest.getTargetUrl().trim();
            
            if (sourceUrl.equals(targetUrl)) {
                session.getBasicRemote()
                       .sendText(
                               String.format(
                                       SOURCE_TARGET_SAME_JSON, 
                                       sourceUrl));
                return;
            }
            
            sourceTitle = extractArticleTitle(sourceUrl);
            targetTitle = extractArticleTitle(targetUrl);
            
            sourceLanguageCode = getLanguageCode(sourceTitle);
            targetLanguageCode = getLanguageCode(targetTitle);
            
            if (!sourceLanguageCode.equals(targetLanguageCode)) {
                session.getBasicRemote()
                       .sendText(
                               String.format(
                                       LANGUAGE_CODES_DIFFERENT,
                                       sourceLanguageCode,
                                       targetLanguageCode));
                return;
            }
            
            forwardNodeExpander  = new ForwardLinkExpander(sourceLanguageCode);
            backwardNodeExpander = new BackwardLinkExpander(targetLanguageCode);
            
            if (sourceTitle.equals(targetTitle)) {
                session.getBasicRemote().sendText("{\"status\"}");
            }
            
            this.finder = 
                ThreadPoolBidirectionalBFSPathFinderBuilder.
                    <String>begin()
                    .withNumberOfMasterTrials            (searchRequest.getNumberOfThreads())
                    .withJoinDurationMillis              (searchRequest.getExpansionDuration())
                    .withLockWaitMillis                  (searchRequest.getWaitTimeout())
                    .withNumberOfMasterTrials            (searchRequest.getMasterTrials())
                    .withMasterThreadSleepDurationMillis (searchRequest.getMasterSleepDuration())
                    .withSlaveThreadSleepDurationMillis  (searchRequest.getSlaveSleepDuration())
                    .end();
        }
        
        @Override
        public void run() {
            final List<String> result = 
                    ThreadPoolBidirectionalBFSPathFinderSearchBuilder
                    .<String>withPathFinder(finder)
                    .withSourceNode(sourceTitle)
                    .withTargetNode(targetTitle)
                    .withForwardNodeExpander(forwardNodeExpander)
                    .withBackwardNodeExpander(backwardNodeExpander)
                    .search();
            
                    
        }
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
}
