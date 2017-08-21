package hello;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import hello.storage.StorageFileNotFoundException;
import hello.storage.StorageService;

@Controller
public class FileUploadController {

    private final StorageService storageService;

    @Autowired
    public FileUploadController(StorageService storageService) {
        this.storageService = storageService;
    }
    
    @PostMapping("/")
    @ResponseBody
    public ResponseEntity<String> handleFileUpload(@RequestParam("files") MultipartFile[] files) 
    {
        return handleFileUpload("", files);
    }

    @PostMapping("/{repository:.+}")
    @ResponseBody
    public ResponseEntity<String> handleFileUpload(@PathVariable String repository, @RequestParam("files") MultipartFile[] files)
    {
        storageService.store(files, repository);
        return ResponseEntity
                .ok()
                .body("OK");
    }

    @ExceptionHandler(StorageFileNotFoundException.class)
    public ResponseEntity<?> handleStorageFileNotFound(StorageFileNotFoundException exc) {
        return ResponseEntity.notFound().build();
    }

}
