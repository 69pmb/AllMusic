package pmb.music.AllMusic.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.codehaus.plexus.util.FileUtils;

import com.dropbox.core.DbxDownloader;
import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.files.ListFolderResult;
import com.dropbox.core.v2.files.Metadata;
import com.dropbox.core.v2.files.WriteMode;
import com.dropbox.core.v2.users.FullAccount;

/**
 * Hello world!
 * 
 */
public class DropBoxUtils {

	private static final Logger LOG = Logger.getLogger(DropBoxUtils.class);

	private static DbxClientV2 client = null;

	private DropBoxUtils() {
		LOG.debug("Start DropBox");
		// Create Dropbox client
		DbxRequestConfig config = new DbxRequestConfig("dropbox/java-tutorial");
		client = new DbxClientV2(config, Constant.ACCESS_TOKEN);
		LOG.debug("End DropBox");
	}

	public static FullAccount getAccountInfo() throws MyException {
		LOG.debug("Start getAccountInfo");
		FullAccount account = null;
		// Get current account info
		if (client != null) {
			try {
				account = getClientDropBox().users().getCurrentAccount();
				LOG.debug(account.getName().getDisplayName());
			} catch (DbxException e) {
				throw new MyException("Error making API call", e);
			}
		}
		LOG.debug("End getAccountInfo");
		return account;
	}

	public static DbxClientV2 getClientDropBox() throws MyException {
		if (client == null) {
			new DropBoxUtils();
		}
		return client;
	}

	public static List<String> getFilesAndFolder(String folder, boolean recursive) throws MyException {
		LOG.debug("Start getFilesAndFolder");
		List<String> files = new ArrayList<>();
		// Get files and folder metadata from Dropbox root directory
		ListFolderResult result;
		String folderToInspect = "/" + folder;
		try {
			result = getClientDropBox().files().listFolderBuilder(folderToInspect).withRecursive(recursive).start();
			while (true) {
				for (Metadata metadata : result.getEntries()) {
					files.add(StringUtils.substringAfter(metadata.getPathDisplay(), "/"));
				}
				if (!result.getHasMore()) {
					break;
				}
			}
		} catch (MyException | DbxException e) {
			throw new MyException("Erreur dans getFilesAndFolder", e);
		}
		LOG.debug("End getFilesAndFolder");
		return files;
	}

	/**
	 * @param folder
	 * @return
	 * @throws MyException
	 */
	public static boolean isFolderExistRoot(String folder) throws MyException {
		LOG.debug("Start isFolderExistRoot");
		ListFolderResult result = null;
		try {
			result = getClientDropBox().files().listFolder("");
		} catch (DbxException e) {
			throw new MyException("Impossible de lister les fichiers de la dropbox", e);
		}
		for (Metadata metadata : result.getEntries()) {
			if (folder.equals(metadata.getName())) {
				LOG.debug("End isFolderExistRoot, existe");
				return true;
			}
		}
		LOG.debug("End isFolderExistRoot, n'existe pas");
		return false;
	}

	/**
	 * @param folder
	 * @throws MyException
	 */
	public static void createFolder(String folder) throws MyException {
		LOG.debug("Start createFolder");
		String folderToCreate = "/" + folder;
		try {
			if (!isFolderExistRoot(folderToCreate)) {
				LOG.debug("Le dossier " + folderToCreate + " n'existe pas, on le crée");
				getClientDropBox().files().createFolder(folderToCreate);
			}
		} catch (MyException | DbxException e) {
			throw new MyException("Erreur lors de la création du dossier: " + folderToCreate, e);
		}
		LOG.debug("End createFolder");
	}

	/**
	 * @param dropboxPath chemin absolu du fichier sur la dropbox
	 * @param absPathFile chemin absolu du fichier sur le disque
	 * @return
	 * @throws MyException
	 */
	public static File downloadAndCopy(String dropboxPath, String absPathFile) throws MyException {
		LOG.debug("Start downloadAndCopy");
		LOG.debug("dropboxPath: " + dropboxPath);
		LOG.debug("absPathFile: " + absPathFile);
		File fichier;
		if (!FileUtils.fileExists(absPathFile)) {
			try {
				InputStream downloadFile = downloadFile(dropboxPath, absPathFile);
				FichierUtils.createFolderIfNotExists(StringUtils.substringBeforeLast(absPathFile, "\\"));
				fichier = new File(absPathFile);
				Files.copy(downloadFile, fichier.toPath(), StandardCopyOption.REPLACE_EXISTING);
				IOUtils.closeQuietly(downloadFile);
			} catch (MyException | IOException e1) {
				throw new MyException("Erreur lors du téléchargement du fichier: " + absPathFile, e1);
			}
		} else {
			fichier = new File(absPathFile);
		}
		LOG.debug("End downloadAndCopy");
		return fichier;
	}

	/**
	 * Télécharge sur la dropbox un fichier XML.
	 * 
	 * @param folder le chemin sur la dropbox du dossier
	 * @param absPath le chemin absolu du fichier XML
	 * @return {@link InputStream} le fichier voulu
	 * @throws MyException
	 */
	public static InputStream downloadFile(String folder, String absPath) throws MyException {
		LOG.debug("Start downloadFile");
		String uri;
		if (StringUtils.contains(absPath, Constant.getXmlPath())) {
			uri = "/" + folder + "/" + StringUtils.substringAfter(absPath, Constant.getXmlPath());
		} else {
			uri = "/" + folder + "/" + StringUtils.substringAfter(absPath, Constant.getMusicAbsDirectory());
		}
		uri = StringUtils.replace(uri, "\\", "/");
		LOG.debug(uri);
		InputStream in = null;
		try {
			DbxDownloader<FileMetadata> download = getClientDropBox().files().download(uri);
			in = download.getInputStream();
		} catch (DbxException e) {
			throw new MyException("Erreur lors du telechargement du fichier: " + uri, e);
		}
		LOG.debug("End downloadFile");
		return in;
	}

	/**
	 * @param folderSource
	 * @param folderDestination
	 * @throws MyException
	 */
	public static void importFolder(String folderSource, String folderDestination) throws MyException {
		if (StringUtils.isNotBlank(folderSource) && isFolderExistRoot(folderSource)) {
			List<String> files = getFilesAndFolder(folderSource, true);
			for (String file : files) {
				if (!StringUtils.contains(file, ".")) {
					String[] split = file.split("/");
					String path = split[0];
					FichierUtils.createFolderIfNotExists(Constant.getResourcesDir() + path);
					for (int i = 1; i < split.length; i++) {
						path += "/" + split[i];
						FichierUtils.createFolderIfNotExists(Constant.getResourcesDir() + path);
					}
				} else {
					String replace = StringUtils.replace(file, "/", "\\");
					downloadAndCopy(folderSource, folderDestination + StringUtils.substringAfter(replace, "\\"));
				}
			}
		}
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
		String dropBoxPath = "/" + pathDropBox;
		WriteMode writeMode = mode;
		if (writeMode == null) {
			writeMode = WriteMode.ADD;
		}
		try (InputStream in = new FileInputStream(pathFile)) {
			metadata = getClientDropBox().files().uploadBuilder(dropBoxPath).withMode(writeMode).uploadAndFinish(in);
		} catch (MyException | IOException | DbxException e) {
			throw new MyException(
					"Erreur lors de l'upload du fichier: " + pathFile + " dans le dossier: " + dropBoxPath, e);
		}
		LOG.debug("End uploadFile");
		return metadata;
	}
}
