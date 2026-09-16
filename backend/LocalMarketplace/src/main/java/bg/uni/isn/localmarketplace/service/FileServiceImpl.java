package bg.uni.isn.localmarketplace.service;

import bg.uni.isn.localmarketplace.exception.file.FileServiceException;
import bg.uni.isn.localmarketplace.exception.file.InvalidFileFormatException;
import bg.uni.isn.localmarketplace.service.contract.FileService;
import bg.uni.isn.localmarketplace.utils.FileServiceRoot;
import bg.uni.isn.localmarketplace.utils.ValidationUtils;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileServiceImpl implements FileService {

    private final FileServiceRoot root;

    public FileServiceImpl(FileServiceRoot root) {
        this.root = root;
    }

    @Override
    public String saveOrReplaceJpg(MultipartFile file, Path targetDirectory, String customName) {
        if (file.isEmpty()) {
            throw new FileServiceException("Empty file");
        }

        if (!ValidationUtils.isJpgFile(file)) {
            throw new InvalidFileFormatException("Not a JPG file");
        }

        if (customName != null && !ValidationUtils.isValidFilename(customName)) {
            throw new FileServiceException("Invalid custom name");
        }

        String filename = (customName != null ? customName : UUID.randomUUID()) + ".jpg";

        Path destinationFile = root.getRoot().resolve(targetDirectory).resolve(filename).normalize().toAbsolutePath();

        if (!destinationFile.getParent().startsWith(root.getRoot().toAbsolutePath())) {
            throw new FileServiceException("Unable to store outside of root");
        }

        try (InputStream inputStream = file.getInputStream()) {
            Files.createDirectories(destinationFile.getParent());
            Files.copy(inputStream, destinationFile, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new FileServiceException("Cannot write file", e);
        }

        return filename;
    }

    @Override
    public Resource getFile(String path) {
        try {
            Path filePath = root.getRoot().resolve(path).normalize().toAbsolutePath();

            if (!filePath.startsWith(root.getRoot().toAbsolutePath())) {
                throw new FileServiceException("Invalid file path");
            }

            Resource resource = new UrlResource(filePath.toUri());

            if (!resource.exists() || !resource.isReadable()) {
                throw new FileServiceException("File not found or not readable: " + path);
            }

            return resource;

        } catch (MalformedURLException e) {
            throw new FileServiceException("Invalid file path: " + path, e);
        }
    }

    @Override
    public void deleteFile(Path relativePath) {
        Path filePath = root.getRoot()
            .resolve(relativePath)
            .normalize()
            .toAbsolutePath();

        if (!filePath.startsWith(root.getRoot().toAbsolutePath())) {
            throw new FileServiceException("Invalid file path");
        }

        try {
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            throw new FileServiceException("Cannot delete file: " + filePath, e);
        }
    }
}
