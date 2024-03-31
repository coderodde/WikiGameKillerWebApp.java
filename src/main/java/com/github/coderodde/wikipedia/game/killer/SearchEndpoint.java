package com.github.coderodde.wikipedia.game.killer;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;

@ServerEndpoint(value = "/search")
public final class SearchEndpoint {
    
    private static final long CONNECTION_TIMEOUT_MILLIS = 1000 * 120; // 120 s.
    
    private static final Logger LOGGER = 
            Logger.getLogger(SearchEndpoint.class.getSimpleName());
    
    @OnOpen
    public void onOpen(/*final @PathParam("source") String source, 
                       final @PathParam("target") String target,*/
                       final Session session) {
        session.setMaxIdleTimeout(CONNECTION_TIMEOUT_MILLIS);
//        final String msg = 
//                String.format(
//                        "Source <%s>, target <%s>",
//                        source, 
//                        target);
        
        try {
            session.getBasicRemote().sendText("hello"); 
            LOGGER.log(Level.INFO, "Successfully sent a message.");
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE,
                       "Could not send a message.", 
                       ex);
        }
    }
}
