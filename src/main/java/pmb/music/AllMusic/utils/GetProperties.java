package pmb.music.AllMusic.utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.log4j.Logger;

/**
 * Gère le fichier properties.
 * 
 * @author pmbroca
 */
public class GetProperties {

	private static final Logger LOG = Logger.getLogger(GetProperties.class);

	/**
	 * {@link Properties} contient les properties.
	 */
	private static Properties prop = null;

	/**
	 * Récupère le fichier properties.
	 */
	private GetProperties() {
		LOG.debug("Start GetProperties");
		try (InputStream input = new FileInputStream(Constant.getConfigPath());) {
			prop = new Properties();
			prop.load(input);
		} catch (IOException e) {
			LOG.error("Erreur lors de l'import des properties: ", e);
			java.lang.System.exit(0);
		}
		LOG.debug("End GetProperties");
	}

	/**
	 * Récupère une donnée du fichier properties.
	 * 
	 * @param key la clé de la donnée
	 * @return la donnée
	 */
	public static String getProperty(String key) {
		if (prop == null) {
			new GetProperties();
		}
		if (prop == null) {
			LOG.error("GetProperties returns null");
			return "";
		}
		return prop.getProperty(key);
	}
}
