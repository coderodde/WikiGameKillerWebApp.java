package com.github.coderodde.wikipedia.game.killer;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import org.apache.commons.io.IOUtils;

@ServerEndpoint(value = "/randomize")
public class GetRandomTerminalsEndpoint {
    
    private static final Logger LOGGER = 
            Logger.getLogger(GetRandomTerminalsEndpoint.class.getName());
    
    private static final String API_URL = 
            "https://en.wikipedia.org/w/api.php?" + 
            "action=query&list=random&rnnamespace=0&rnlimit=2&format=json&utf8=1";
    
    // 15 seconds:
    private static final long CONNECTION_TIMEOUT_MILLIS = 1000 * 15;
    
    @OnOpen
    public void onOpen(final Session session) {
        session.setMaxIdleTimeout(CONNECTION_TIMEOUT_MILLIS);
        LOGGER.log(Level.INFO, "WebSocket connection open.");
    }
    
    @OnMessage
    public void onMessage(final Session session, final String ignored) {
        
        final Random random = new Random();
        final String languageCode = Utils.getRandomLanguageCode(random);
        final String uri = String.format(API_URL, languageCode);
        
        try {
            final String json = 
                    IOUtils.toString(new URI(uri), Charset.forName("utf-8"));
            
            session.getBasicRemote().sendText(json);
        } catch (URISyntaxException | IOException ex) {
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
