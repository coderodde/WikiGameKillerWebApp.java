package io.github.coderodde.wikipedia.game.killer.model.encoders;

import io.github.coderodde.wikipedia.game.killer.model.Message;
import com.google.gson.Gson;
import javax.websocket.EncodeException;
import javax.websocket.Encoder;
import javax.websocket.EndpointConfig;

public final class MessageEncoder implements Encoder.Text<Message> {
    
    private static final Gson GSON = new Gson();

    public String encode(final Message searchRequest)
            throws EncodeException {
        return GSON.toJson(searchRequest);
    }

    public void init(EndpointConfig ec) {
    
    }

    public void destroy() {
    
    }
}
