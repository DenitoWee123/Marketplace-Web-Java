package bg.uni.isn.localmarketplace.service.contract;

import bg.uni.isn.localmarketplace.exception.file.InvalidFileFormatException;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import bg.uni.isn.localmarketplace.exception.file.FileServiceException;

import java.nio.file.Path;

public interface FileService {

    /**
     * Saves or replaces the provided multipart file at the specified relative path.
     *
     * <p>The provided file is stored under the configured root directory. Any
     * necessary parent directories are created and existing content is overwritten if the file already exists.</p>
     *
     * @param file            the uploaded file to store
     * @param targetDirectory the relative path under the service root where the file should be saved
     * @param customName      a custom name for the file. If {@code null}, a random UUID will be generated and used as a name
     * @return the path to the saved file
     * @throws InvalidFileFormatException if the file is not in acceptable format
     * @throws FileServiceException       if the file is empty, the path is outside the configured root, the provided custom file name is invalid, or the file cannot be written
     */
    String saveOrReplaceJpg(MultipartFile file, Path targetDirectory, String customName);

    /**
     * Loads the requested file as a Spring {@link Resource}.
     *
     * @param path the filesystem path of the file to load
     * @return the loaded file as a {@link Resource}
     * @throws FileServiceException if the file is not readable or the path cannot be resolved
     */
    Resource getFile(String path);

    /**
     * Deletes a file
     *
     * @param relativePath the path of the file
     * @throws FileServiceException if the file is not readable or the path cannot be resolved
     */
    void deleteFile(Path relativePath);
}
