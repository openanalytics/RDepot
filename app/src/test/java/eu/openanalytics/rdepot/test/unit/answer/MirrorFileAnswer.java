package eu.openanalytics.rdepot.test.unit.answer;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

public class MirrorFileAnswer implements Answer<File> {

	public static final Map<String, String> urls = new HashMap<>() {/**
		 * 
		 */
		private static final long serialVersionUID = -735580383274037138L;

	{
		put("https://cran.r-project.org", "mirror_a");
		put("https://bioconductor.org/packages/3.12/bioc", "mirror_b");
		put("https://bioconductor.org/packages/release/bioc", "mirror_c");
	}};
	
	private static final String PREFIX = "src/test/resources/unit/test_files/test_mirroring";
	private File temporaryFolder;
	
	public MirrorFileAnswer(File temporaryFolder) {
		this.temporaryFolder = temporaryFolder;
	}
	
	@Override
	public File answer(InvocationOnMock invocation) throws Throwable {
		String url = invocation.getArgument(0);
		
		for(String key : urls.keySet()) {
			if(url.startsWith(key)) {
				String[] tokens = url.split("/");
				String filename = tokens[tokens.length - 1];
				Boolean archive = tokens[tokens.length - 3].equals("Archive");
				
				if(archive) {
					filename = "Archive" + "/" + tokens[tokens.length - 2] + "/" + filename;
				}
				
				String actualDirectory = urls.get(key);
				
				File file = new File(PREFIX + "/" + actualDirectory + "/" + filename);
				
				if(!file.exists())
					throw new FileNotFoundException();
				
				FileUtils.copyFileToDirectory(file, temporaryFolder);
				
				return temporaryFolder.toPath().resolve(file.getName()).toFile();
			}
		}
		
		throw new FileNotFoundException();
	}

}
