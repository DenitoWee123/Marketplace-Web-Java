package bg.uni.isn.localmarketplace.utils;

import java.nio.file.Path;

public class FileServiceRoot {
    private Path root;

    public FileServiceRoot(Path root) {
        this.root = root;
    }

    public Path getRoot() {
        return root;
    }

    public void setRoot(Path root) {
        this.root = root;
    }
}
