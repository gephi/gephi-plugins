package Helper;

import Helper.CustomDeserializer.TransitionDeserializer;
import SimulationModel.Transition.Transition;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.Version;
import org.codehaus.jackson.map.*;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.codehaus.jackson.map.module.SimpleModule;
import org.codehaus.jackson.node.TextNode;

import java.awt.*;
import java.io.IOException;

public class ObjectMapperHelper {

    public static class ColorSerializer extends JsonSerializer<Color> {
        @Override
        public void serialize(Color value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            gen.writeStartObject();
            gen.writeFieldName("argb");
            gen.writeString(Integer.toHexString(value.getRGB()));
            gen.writeEndObject();
        }
    }

    public static class ColorDeserializer extends JsonDeserializer<Color> {
        @Override
        public Color deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            JsonNode root = p.getCodec().readTree(p);
            TextNode rgba = (TextNode) root.get("argb");
            return new Color(Integer.parseUnsignedInt(rgba.getTextValue(), 16), true);
        }
    }
    public static ObjectMapper CustomObjectMapperCreator() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(JsonSerialize.Inclusion.NON_NULL);
        objectMapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        SimpleModule module =
                new SimpleModule("TransitionCustomDeserializer", new Version(1, 0, 0, null));
        module.addDeserializer(Transition.class, new TransitionDeserializer());
        objectMapper.registerModule(module);

        SimpleModule colorModule = new SimpleModule("ColorDeserializer", new Version(1, 0, 0, null));
        colorModule.addSerializer(Color.class, new ColorSerializer());
        colorModule.addDeserializer(Color.class, new ColorDeserializer());
        objectMapper.registerModule(colorModule);

        return objectMapper;
    }
}
