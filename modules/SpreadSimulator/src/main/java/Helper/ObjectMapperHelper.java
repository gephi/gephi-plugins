package Helper;

import Helper.CustomDeserializer.TransitionDeserializer;
import SimulationModel.Transition.Transition;
import org.codehaus.jackson.Version;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.codehaus.jackson.map.module.SimpleModule;

public class ObjectMapperHelper {
    public static ObjectMapper CustomObjectMapperCreator() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(JsonSerialize.Inclusion.NON_NULL);
        objectMapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        SimpleModule module =
                new SimpleModule("TransitionCustomDeserializer", new Version(1, 0, 0, null));
        module.addDeserializer(Transition.class, new TransitionDeserializer());
        objectMapper.registerModule(module);
        return objectMapper;
    }
}
