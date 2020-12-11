package eu.openanalytics.rdepot.initializer;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import eu.openanalytics.rdepot.model.Repository;
import eu.openanalytics.rdepot.service.RepositoryService;

@Component
public class RepositoryDataInitializer {
	
	@Resource
	private Environment environment;
	
	@Autowired
	private RepositoryService repositoryService;
	
	@EventListener(ApplicationReadyEvent.class)
	public void createRepositoriesFromConfig() {
		List<Repository> repositories = new ArrayList<>();
    	for(int i=0;;i++) {
    		String repositoryName = environment.getProperty(String.format("repositories[%d].name", i)); 
    		if(repositoryName == null) break;
    		else {
    			Repository repository = new Repository(
    					0,      
    					environment.getProperty(String.format("repositories[%d].publication-uri", i)),
    					environment.getProperty(String.format("repositories[%d].name", i)),
    					environment.getProperty(String.format("repositories[%d].server-address", i)),
    					false,
    					false,
    					new HashSet<>(),
    					new HashSet<>(),
    					new HashSet<>(),
    					new HashSet<>()
    					);   
    			repositories.add(repository);
    		}
    	}
    	repositoryService.createRepositoriesFromConfig(repositories);
	}
}
