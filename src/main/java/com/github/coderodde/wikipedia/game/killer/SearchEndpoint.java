package com.github.coderodde.wikipedia.game.killer;

import com.github.coderodde.graph.pathfinding.delayed.impl.ThreadPoolBidirectionalBFSPathFinder;
import com.github.coderodde.graph.pathfinding.delayed.impl.ThreadPoolBidirectionalBFSPathFinderBuilder;
import java.io.IOException;
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
    
    private static final long CONNECTION_TIMEOUT_MILLIS = 1000 * 120; // 120 s.
    
    private static final Map<Session, 
                             ThreadPoolBidirectionalBFSPathFinder<String>> 
            STATE_MAP = new ConcurrentHashMap<>();
    
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
                search(searchRequest);
                break;
                
            case SearchRequest.HALT_ACTION:
                halt();
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
        STATE_MAP.remove(session);
    }
    
    @OnError
    public void onError(final Session session, final Throwable throwable) {
        LOGGER.log(Level.SEVERE, 
                   "Error in {}: {}", 
                   new Object[]{ SearchEndpoint.this.getClass().getName(), 
                                 throwable.getMessage() 
                   });
        
        STATE_MAP.remove(session);
    }
    
    private void search(final SearchRequest searchRequest) {
        
    }
    
    private static final class SearchThread extends Thread {
        private final Session session;
        private final ThreadPoolBidirectionalBFSPathFinder<String> finder;
        private final SearchRequest searchRequest;
        private final String languageCodeSource;
        private final String languageCodeTarget;
        private final String sourceTitle;
        private final String targetTitle;
        
        SearchThread(final Session session, 
                     final SearchRequest searchRequest) {
            this.session = session;
            this.searchRequest = searchRequest;
            
            sourceTitle = extractArticleTitle(searchRequest.getSourceUrl().trim());
            targetTitle = extractArticleTitle(searchRequest.getTargetUrl().trim());
            languageCodeSource = getLanguageCode(sourceTitle);
            languageCodeTarget = getLanguageCode(targetTitle);
            
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
            
            this.sourceTitle = extractArticleTitle(searchRequest.
        }
        
        @Override
        public void run() {
            final List<String> result
        }
        
        void halt() {
            final Lis
        }
    }
    
    private static String extractArticleTitle(final String url) {
        return url.substring(url.lastIndexOf('/') + 1);
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
