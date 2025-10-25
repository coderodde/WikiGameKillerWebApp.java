package io.github.coderodde.wikipedia.game.killer;

import io.github.coderodde.wikipedia.json.downloader.WikipediaArticleJsonDownloader;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

@ServerEndpoint(value = "/randomize")
public class GetRandomTerminalsEndpoint {
    
    private static final Logger LOGGER = 
            Logger.getLogger(GetRandomTerminalsEndpoint.class.getName());
    
    // 15 seconds:
    private static final long CONNECTION_TIMEOUT_MILLIS = 1000 * 15;
    
    @OnOpen
    public void onOpen(final Session session) {
        session.setMaxIdleTimeout(CONNECTION_TIMEOUT_MILLIS);
        LOGGER.log(Level.INFO, "WebSocket connection open.");
    }
    
    @OnMessage
    public void onMessage(final Session session, final String ignored) {
        
        try {
            final String json = 
                    new WikipediaArticleJsonDownloader("en")
                            .downloadPairOfRandomArticles();
            
            session.getBasicRemote().sendText(json);
            
        } catch (IOException ex) {
            LOGGER.log(
                    Level.SEVERE,
                    "Could not download JSON: {0}.", 
                    ex.getMessage());
            
        }
    }
    
    @OnClose
    public void onClose(final Session session) {
        LOGGER.log(Level.INFO, "WebSocket connection is closed.");
    }
    
    @OnError
    public void onError(final Session session, final Throwable throwable) {
        LOGGER.log(Level.SEVERE, 
                   "WebSocket threw: {0}.", 
                   throwable.getMessage());
    }
}
