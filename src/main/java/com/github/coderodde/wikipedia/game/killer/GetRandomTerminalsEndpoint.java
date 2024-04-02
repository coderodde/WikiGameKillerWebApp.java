package com.github.coderodde.wikipedia.game.killer;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.websocket.OnMessage;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import org.apache.commons.io.IOUtils;

@ServerEndpoint(value = "/randomize")
public final class GetRandomTerminalsEndpoint {
    
    private static final Logger LOGGER = 
            Logger.getLogger(GetRandomTerminalsEndpoint.class.getName());
    
//    https://www.mediawiki.org/w/api.php?action=query&format=json&list=random&formatversion=2&rnnamespace=0&rnlimit=2
    
    private static final String API_URL = 
            "https://en.wikipedia.org/w/api.php?" + 
            "action=query&list=random&rnnamespace=0&rnlimit=2&format=json&utf8=1";
    
    @OnMessage
    public void onMessage(final Session session, final String ignored) {
        
        final Random random = new Random();
        final String languageCode = Utils.getRandomLanguageCode(random);
        final String uri = String.format(API_URL, languageCode);
        
        try {
            final String json = IOUtils.toString(new URI(uri), Charset.forName("utf-8"));
            session.getBasicRemote().sendText(json);
        } catch (URISyntaxException ex) {
            LOGGER.log(
                    Level.SEVERE,
                    "Could not download JSON: {0}.", 
                    ex.getMessage());
            
        } catch (IOException ex) {
            LOGGER.log(
                    Level.SEVERE, 
                    "Could not download JSON: {0}.", 
                    ex.getMessage());
        }
    }
}
