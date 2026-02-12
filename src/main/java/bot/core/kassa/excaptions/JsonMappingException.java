package bot.core.kassa.excaptions;

import com.fasterxml.jackson.core.JsonProcessingException;

public class JsonMappingException extends RuntimeException {
    public JsonMappingException(String message) {
        super(message);
    }

    public JsonMappingException(String message, Throwable e) {
        super(message, e);
    }
}
