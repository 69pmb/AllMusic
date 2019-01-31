package pmb.music.AllMusic.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.log4j.Logger;
import org.codehaus.plexus.util.FileUtils;

import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.files.WriteMode;

/**
 * Hello world!
 * 
 */
public class DropBoxUtils {

	private static final Logger LOG = Logger.getLogger(DropBoxUtils.class);

	private static DbxClientV2 client = null;

	private static void initClient() {
		LOG.debug("Start initClient");
		// Create Dropbox client
		DbxRequestConfig config = new DbxRequestConfig("dropbox/java-tutorial");
		client = new DbxClientV2(config, Constant.ACCESS_TOKEN);
		LOG.debug("End initClient");
	}

	public static DbxClientV2 getClientDropBox() {
		if (client == null) {
			initClient();
		}
		return client;
	}

	/**
	 * @param pathFile
	 * @param pathDropBox
	 * @param mode
	 * @return
	 * @throws MyException
	 */
	public static FileMetadata uploadFile(File pathFile, String pathDropBox, WriteMode mode) throws MyException {
		LOG.debug("Start uploadFile");
		FileMetadata metadata = null;
		String dropBoxPath = FileUtils.FS + pathDropBox;
		WriteMode writeMode = mode;
		if (writeMode == null) {
			writeMode = WriteMode.ADD;
		}
		try (InputStream in = new FileInputStream(pathFile)) {
			metadata = getClientDropBox().files().uploadBuilder(dropBoxPath).withMode(writeMode).uploadAndFinish(in);
		} catch (IOException | DbxException e) {
			throw new MyException(
					"Erreur lors de l'upload du fichier: " + pathFile + " dans le dossier: " + dropBoxPath, e);
		}
		LOG.debug("End uploadFile");
		return metadata;
	}

	private DropBoxUtils() {
		throw new AssertionError("Must not be used");
	}
}
