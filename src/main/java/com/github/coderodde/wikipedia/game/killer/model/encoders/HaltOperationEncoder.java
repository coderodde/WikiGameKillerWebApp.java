package com.github.coderodde.wikipedia.game.killer.model.encoders;

import com.github.coderodde.wikipedia.game.killer.model.HaltOperation;
import com.google.gson.Gson;
import javax.websocket.EncodeException;
import javax.websocket.Encoder;
import javax.websocket.EndpointConfig;

public final class HaltOperationEncoder implements Encoder.Text<HaltOperation> {

    private static final Gson GSON = new Gson();
    
    @Override
    public String encode(final HaltOperation haltOperation)
            throws EncodeException {
        return GSON.toJson(haltOperation);
    }

    @Override
    public void init(final EndpointConfig ec) {
    
    }

    @Override
    public void destroy() {
    
    }
}
