package com.github.coderodde.wikipedia.game.killer;

import com.github.coderodde.graph.pathfinding.delayed.impl.ThreadPoolBidirectionalBFSPathFinder;
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
        
        System.out.println(searchRequest.getExpansionDuration());
        System.out.println(searchRequest.getMasterSleepDuration());
        System.out.println(searchRequest.getMasterTrials());
        System.out.println(searchRequest.getNumberOfThreads());
        System.out.println(searchRequest.getSlaveSleepDuration());
        System.out.println(searchRequest.getSourceUrl());
        System.out.println(searchRequest.getTargetUrl());
        System.out.println(searchRequest.getWaitTimeout());
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
}
