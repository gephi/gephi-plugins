package helper.customDeserializer;

import simulationModel.interaction.*;
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
            case RelativeNodes:
                return mapper.readValue(node, RelativeNodesInteraction.class);
            case RelativeFreeNodes:
                return mapper.readValue(node, RelativeFreeNodesInteraction.class);
            case RelativeEdges:
                return mapper.readValue(node, RelativeEdgesInteraction.class);
            case RelativeFreeEdges:
                return mapper.readValue(node, RelativeFreeEdgesInteraction.class);
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
