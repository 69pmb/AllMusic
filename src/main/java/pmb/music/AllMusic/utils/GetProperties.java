package pmb.music.AllMusic.utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Gère le fichier properties.
 * 
 */
public final class GetProperties {

    private static final Logger LOG = LogManager.getLogger(GetProperties.class);
    private static final String LEVEL_KEY = "level";

    /**
     * {@link Properties} contient les properties.
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
            MiscUtils.setLogLevel(getProperty(LEVEL_KEY));
        } catch (IOException e) {
            LOG.error("Erreur lors de l'import des properties: ", e);
            java.lang.System.exit(0);
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
     * Récupère une donnée du fichier properties.
     * 
     * @param key la clé de la donnée
     * @return la donnée
     */
    public static String getProperty(String key) {
        if (prop == null) {
            loadProperties();
        }
        if (prop == null) {
            LOG.error("loadProperties returns null");
            return "";
        }
        return prop.getProperty(key);
    }
}
