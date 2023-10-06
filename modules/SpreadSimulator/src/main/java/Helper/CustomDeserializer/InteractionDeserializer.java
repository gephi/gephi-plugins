package Helper.CustomDeserializer;

import SimulationModel.Interaction.*;
import lombok.SneakyThrows;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.ObjectCodec;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.deser.std.StdDeserializer;

import java.io.IOException;

public class InteractionDeserializer extends StdDeserializer<Interaction> {

    public InteractionDeserializer() {
        this(null);
    }

    public InteractionDeserializer(Class<?> vc) {
        super(vc);
    }

    @SneakyThrows
    @Override
    public Interaction deserialize(JsonParser parser, DeserializationContext deserializer) throws IOException {
        ObjectCodec codec = parser.getCodec();
        JsonNode node = codec.readTree(parser);

        InteractionType interactionType = getTransitionType(node);

        var mapper = new ObjectMapper();

        switch (interactionType) {
            case All:
                return mapper.readValue(node, AllInteraction.class);
            case Relative:
                return mapper.readValue(node, RelativeInteraction.class);
            case RelativeFree:
                return mapper.readValue(node, RelativeFreeInteraction.class);
            default:
                return mapper.readValue(node, Interaction.class);
        }
    }

    private static InteractionType getTransitionType(JsonNode node) {
        JsonNode interactionTypeNode = node.get("interactionType");
        String interactionTypeText = interactionTypeNode.asText();
        return InteractionType.valueOf(interactionTypeText);
    }
}
