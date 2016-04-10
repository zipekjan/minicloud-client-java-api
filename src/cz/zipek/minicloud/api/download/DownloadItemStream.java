package cz.zipek.minicloud.api.download;

import cz.zipek.minicloud.api.FileVersion;
import java.io.OutputStream;

/**
 *
 * @author Kamen
 */
public class DownloadItemStream extends DownloadItem {
	
	private final OutputStream stream;
	
	public DownloadItemStream(FileVersion file, OutputStream stream) {
		super(file, null);
		
		this.stream = stream;
	}
	
	@Override
	public OutputStream getStream() {
		return stream;
	}
	
}
