package server;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class ResponseEntity {
    public String response;
    public JsonElement value;
    public String reason;
    public String file;

    public ResponseEntity() {
    }
}
