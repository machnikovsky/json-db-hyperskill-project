package server;

import java.util.List;

public class JSONEntity {

    List<JSONEntity> values;


    public JSONEntity(List<JSONEntity> values) {
        this.values = values;
    }

    public List<JSONEntity> getValues() {
        return values;
    }

    public void setValues(List<JSONEntity> values) {
        this.values = values;
    }
}
