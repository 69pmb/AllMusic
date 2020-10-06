package pmb.allmusic.utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import pmb.allmusic.exception.MinorException;

/**
 * Handles configuration properties file.
 */
public final class GetProperties {

    private static final Logger LOG = LogManager.getLogger(GetProperties.class);
    private static final String LEVEL_KEY = "level";

    /**
     * {@link Properties} contains application properties.
     */
    private static Properties prop;

    private GetProperties() {
        throw new AssertionError("Must not be used");
    }

    /**
     * Loads properties from configuration file.
     */
    private static void loadProperties() {
        LOG.debug("Start loadProperties");
        try (InputStream input = new FileInputStream(Constant.getConfigPath())) {
            prop = new Properties();
            prop.load(input);
            getProperty(LEVEL_KEY).ifPresent(MiscUtils::setLogLevel);
        } catch (IOException e) {
            throw new MinorException("Error when importing properties", e);
        }
        LOG.debug("End loadProperties");
    }

    /**
     * Refreshs the property file, useful if it changes.
     *
     * @return true if succeed, false otherwise
     */
    public static boolean reloadProperties() {
        loadProperties();
        return prop != null;
    }

    /**
     * Recover a specific property from property file.
     *
     * @param key id of the wanted property
     * @return the property sought
     */
    public static Optional<String> getProperty(String key) {
        if (prop == null) {
            loadProperties();
        }
        if (prop == null) {
            throw new MinorException("Unable to load properties");
        }
        return Optional.ofNullable(prop.getProperty(key)).filter(StringUtils::isNotBlank);
    }
}
