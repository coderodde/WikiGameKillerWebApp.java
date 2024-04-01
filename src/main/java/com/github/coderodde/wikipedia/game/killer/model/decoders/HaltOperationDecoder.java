package com.github.coderodde.wikipedia.game.killer.model.decoders;

import com.github.coderodde.wikipedia.game.killer.model.HaltOperation;
import com.google.gson.Gson;
import javax.websocket.DecodeException;
import javax.websocket.Decoder;
import javax.websocket.EndpointConfig;

public final class HaltOperationDecoder implements Decoder.Text<HaltOperation> {

    private static final Gson GSON = new Gson();
    
    @Override
    public HaltOperation decode(final String json) throws DecodeException {
        return GSON.fromJson(json, HaltOperation.class);
    }

    @Override
    public boolean willDecode(final String string) {
        return true;
    }

    @Override
    public void init(final EndpointConfig ec) {
        
    }

    @Override
    public void destroy() {
    
    }
}
