package eu.openanalytics.rdepot.repo.model;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import org.apache.logging.log4j.util.Strings;

public class ArchiveInfo {

	public final String path;

	public final double size;

	public final int isdir = 0;

	// octal 
	// int 436 --> 101 101 001 --> mode 664
	public final int mode;

	// time in R is represented with an integer (unix epoch)
	// but when serialized it is stored as a 64bit floating point
	// when deserialized, it is rounded 
	public final double mtime;
	public final double ctime;
	public final double atime;

	public final int uid;
	public final int gid;
	public final String uname;
	public final String grname;

	public ArchiveInfo(String path, double size, int mode, double mtime, double ctime, double atime,
			int uid, int gid, String uname, String grname) {
	    assert(!Strings.isBlank(path));
		this.path = path;
		this.size = size;
		this.mode = mode;
		this.mtime = mtime;
		this.ctime = ctime;
		this.atime = atime;
		this.uid = uid;
		this.gid = gid;
		this.uname = uname == null ? "root" : uname;
		this.grname = grname == null ? "root" : grname;
	}

	public ArchiveInfo(String path, int size, int mode, LocalDateTime mtime, LocalDateTime ctime, LocalDateTime atime,
			int uid, int gid, String uname, String grname) {
		this(path,
				size,
				mode,
				(double) (mtime == null ? LocalDateTime.now(ZoneId.systemDefault()).toEpochSecond(ZoneOffset.UTC) : mtime.atZone(ZoneId.systemDefault()).toEpochSecond()),
				(double) (ctime == null ? LocalDateTime.now(ZoneId.systemDefault()).toEpochSecond(ZoneOffset.UTC) : ctime.atZone(ZoneId.systemDefault()).toEpochSecond()),
				(double) (atime == null ? LocalDateTime.now(ZoneId.systemDefault()).toEpochSecond(ZoneOffset.UTC) : atime.atZone(ZoneId.systemDefault()).toEpochSecond()),
				uid,
				gid,
				uname,
				grname);
	}

}
