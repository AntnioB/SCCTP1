package scc.utils;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;
import java.time.ZonedDateTime;

public class ZonedDateTimeDeserializer extends StdDeserializer<ZonedDateTime> {
    public ZonedDateTimeDeserializer(){
        this(null);
    }
    protected ZonedDateTimeDeserializer(Class<?> vc) {
        super(vc);
    }

    @Override
    public ZonedDateTime deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JsonProcessingException {
        return ZonedDateTime.parse(jsonParser.getText());
    }
}
