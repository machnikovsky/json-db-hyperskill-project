package server;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class DbEntity {
    String key;
    JsonElement value;

    public DbEntity() {
    }

    public DbEntity(String key, JsonElement value) {
        this.key = key;
        this.value = value;
    }
}
