package helper.customDeserializer;

import simulationModel.transition.*;
import lombok.SneakyThrows;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.ObjectCodec;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.deser.std.StdDeserializer;

import java.io.IOException;

public class TransitionDeserializer extends StdDeserializer<Transition> {

    public TransitionDeserializer() {
        this(null);
    }

    public TransitionDeserializer(Class<?> vc) {
        super(vc);
    }

    @SneakyThrows
    @Override
    public Transition deserialize(JsonParser parser, DeserializationContext deserializer) throws IOException {
        ObjectCodec codec = parser.getCodec();
        JsonNode node = codec.readTree(parser);

        TransitionType transitionType = getTransitionType(node);

        var mapper = new ObjectMapper();

        switch (transitionType) {
            case conditionProbability:
                return mapper.readValue(node, TransitionCondition.class);
            case noConditionProbability:
                return mapper.readValue(node, TransitionNoCondition.class);
            case timeDependentProbabilityFunction:
                return mapper.readValue(node, TransitionTimeDependentFunction.class);
            default:
                return mapper.readValue(node, Transition.class);

        }
    }

    private static TransitionType getTransitionType(JsonNode node) {
        JsonNode transitionTypeNode = node.get("transitionType");
        String transitionTypeNodeText = transitionTypeNode.asText();
        return TransitionType.valueOf(transitionTypeNodeText);
    }
}
