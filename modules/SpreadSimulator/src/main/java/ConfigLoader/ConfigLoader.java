package ConfigLoader;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class ConfigLoader {
    private static final Properties prop = new Properties();

    public static String getProperty(String name) {
        try (FileInputStream config = new FileInputStream("simulation.properties")) {
            prop.load(config);
            return prop.getProperty(name);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }


}
