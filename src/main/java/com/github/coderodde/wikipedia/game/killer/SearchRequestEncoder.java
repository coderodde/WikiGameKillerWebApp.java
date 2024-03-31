package com.github.coderodde.wikipedia.game.killer;

import com.google.gson.Gson;
import javax.websocket.EncodeException;
import javax.websocket.Encoder;
import javax.websocket.EndpointConfig;

public final class SearchRequestEncoder implements Encoder.Text<SearchRequest> {
    
    private static final Gson GSON = new Gson();

    public String encode(final SearchRequest searchRequest)
            throws EncodeException {
        return GSON.toJson(searchRequest);
    }

    public void init(EndpointConfig ec) {
    
    }

    public void destroy() {
    
    }
}
