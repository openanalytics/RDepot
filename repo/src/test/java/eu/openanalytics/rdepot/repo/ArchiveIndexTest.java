package eu.openanalytics.rdepot.repo;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import eu.openanalytics.rdepot.repo.model.ArchiveIndex;
import eu.openanalytics.rdepot.repo.model.ArchiveInfo;

public class ArchiveIndexTest {

	@Test
	public void testSerialize() throws IOException {
		
		LocalDateTime time = LocalDateTime.now();
		
		Map<String, List<ArchiveInfo>> archives = new HashMap<>();
		
		ArrayList<ArchiveInfo> fooArchives = new ArrayList<>();
		fooArchives.add(new ArchiveInfo("foo/foo.tar.gz", 24803, 436, time, time, time, 1000, 1000, "einstein", "scientists"));
		
		archives.put("foo", fooArchives);

		ArrayList<ArchiveInfo> barArchives = new ArrayList<>();
		barArchives.add(new ArchiveInfo("bar/bar_1.0.0.tar.gz", 24803, 436, time, time, time, 1000, 1000, "einstein", "scientists"));
		barArchives.add(new ArchiveInfo("bar/bar_2.0.0.tar.gz", 24803, 436, time, time, time, 1000, 1000, "einstein", "scientists"));
		
		archives.put("bar", barArchives);
		
		ArchiveIndex archiveIndex = new ArchiveIndex(archives);
		
		File f = File.createTempFile("test-", "-archive.rds");
		// System.out.println(f.getAbsolutePath());
		f.deleteOnExit();
		
		assertFalse(f.length() > 0);
		archiveIndex.serialize(new FileOutputStream(f));
		assertTrue(f.length() > 0);
		
	}

}
