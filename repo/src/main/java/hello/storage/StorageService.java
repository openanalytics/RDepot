package hello.storage;

import org.springframework.web.multipart.MultipartFile;

public interface StorageService {

    void init();

    void store(MultipartFile[] files, String repository);

}
