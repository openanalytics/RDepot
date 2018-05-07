package hello.storage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileSystemStorageService implements StorageService {

    private final Path rootLocation;

    @Autowired
    public FileSystemStorageService(StorageProperties properties) {
        this.rootLocation = Paths.get(properties.getLocation());
    }

    @Override
    public void store(MultipartFile[] files, String repository) 
    {
    	Path saveLocation = ((repository != null) && (!repository.trim().isEmpty())) ? this.rootLocation.resolve(repository) : this.rootLocation;
    	saveLocation = saveLocation.resolve("src").resolve("contrib");
    	System.out.println("Saving to location " + saveLocation.toString());
		try {
			final Path contrib = Files.createDirectories(saveLocation);
			Files.walk(contrib)
				 .filter(i -> !contrib.equals(i))
			     .map(Path::toFile)
			     .forEach(File::delete);
		}
		catch (IOException e) {
            throw new StorageException("Failed to create directory " + saveLocation.getFileName(), e);
        }
    	for(MultipartFile file : files)
    	{
	        try 
	        {
	            Files.copy(file.getInputStream(), saveLocation.resolve(file.getOriginalFilename()), StandardCopyOption.REPLACE_EXISTING);
	        } catch (IOException e) {
	            throw new StorageException("Failed to store file " + file.getOriginalFilename(), e);
	        }
    	}
    }

    @Override
    public void init() {
        try {
            if(!Files.exists(rootLocation))
            {
            	Files.createDirectory(rootLocation);
            }
        } catch (IOException e) {
            throw new StorageException("Could not initialize storage", e);
        }
    }
}
