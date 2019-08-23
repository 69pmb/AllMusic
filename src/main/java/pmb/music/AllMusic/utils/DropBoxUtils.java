package pmb.music.AllMusic.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.files.WriteMode;

/**
 * Utility class for handling dropbox.
 */
public final class DropBoxUtils {

	private static final Logger LOG = LogManager.getLogger(DropBoxUtils.class);

	private static DbxClientV2 client;

	private DropBoxUtils() {
		throw new AssertionError("Must not be used");
	}

	private static void initClient() {
		LOG.debug("Start initClient");
		// Create Dropbox client
		DbxRequestConfig config = new DbxRequestConfig("dropbox/java-tutorial");
		client = new DbxClientV2(config, Constant.ACCESS_TOKEN);
		LOG.debug("End initClient");
	}

	private static DbxClientV2 getClientDropBox() {
		if (client == null) {
			initClient();
		}
		return client;
	}

	/**
	 * Upload a file to dropbox.
	 * 
	 * @param pathFile the file path in the file system, the source path
	 * @param pathDropBox file path in dropbox, destination path
	 * @param mode {@link WriteMode} the write mode
	 * @return the file metadata of the uploaded file
	 * @throws MyException if an error occured when uploading the file
	 */
	public static FileMetadata uploadFile(File pathFile, String pathDropBox, WriteMode mode) throws MyException {
		LOG.debug("Start uploadFile");
		FileMetadata metadata = null;
		String dropBoxPath = "/" + pathDropBox;
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
}
