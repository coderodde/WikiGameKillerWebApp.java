package io.github.coderodde.wikipedia.game.killer.model.decoders;

import io.github.coderodde.wikipedia.game.killer.model.Message;
import com.google.gson.Gson;
import javax.websocket.DecodeException;
import javax.websocket.Decoder;
import javax.websocket.EndpointConfig;

public final class MessageDecoder implements Decoder.Text<Message> {

    private static final Gson GSON = new Gson();
    
    public Message decode(final String string) throws DecodeException {
        final Message message = GSON.fromJson(string, Message.class);
        return message;
    }

    public boolean willDecode(final String string) {
        return string != null;
    }

    public void init(EndpointConfig ec) {
    
    }

    public void destroy() {
    
    }
}
