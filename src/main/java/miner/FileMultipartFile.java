package miner;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import org.springframework.web.multipart.MultipartFile;

public class FileMultipartFile implements MultipartFile {

    private Path path;

    public FileMultipartFile(Path path) {
        this.path = path;
    }

    @Override
    public byte[] getBytes() throws IOException {
        return Files.readAllBytes(this.path);
    }

    @Override
    public String getContentType() {
        return "application/octet-stream";
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return Files.newInputStream(this.path);
    }

    @Override
    public String getName() {
        return this.path.getFileName().toString();
    }

    @Override
    public String getOriginalFilename() {
        return this.getName();
    }

    @Override
    public long getSize() {
        try {
            return Files.size(this.path);
        } catch (final IOException e) {
            System.err.println(e.getMessage());
            return 0;
        }
    }

    @Override
    public boolean isEmpty() {
        return this.getSize() == 0;
    }

    @Override
    public void transferTo(File dest) throws IOException, IllegalStateException {
        this.transferTo(dest.toPath());
    }

    @Override
    public void transferTo(Path dest) throws IOException, IllegalStateException {
        Files.copy(this.path, dest);
    }

}
