package com.github.coderodde.wikipedia.game.killer;

import com.google.gson.Gson;
import javax.websocket.DecodeException;
import javax.websocket.Decoder;
import javax.websocket.EndpointConfig;

public final class SearchRequestDecoder implements Decoder.Text<SearchRequest> {

    private static final Gson GSON = new Gson();
    
    public SearchRequest decode(final String string) throws DecodeException {
        return GSON.fromJson(string, SearchRequest.class);
    }

    public boolean willDecode(final String string) {
        return true;
    }

    public void init(EndpointConfig ec) {
    
    }

    public void destroy() {
    
    }
}
