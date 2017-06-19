package treehouse.sdk.android;


import javax.io.File;
import javax.lang.Try;

import static javax.lang.Try.*;

public class Android {
	
	protected File home;
			
	public Android() {
		this(Try.attempt(() -> locate()));
	}
	
	public Android(File home) {
		this.home = home;
	}
		
	public File home() {
		return this.home;
	}
	
	//TODO: setup sdk
	//TODO: install

	
	public static File locate() throws Exception {
		return locate(new File("/opt"));
	}
	
	public static File locate(File directory) throws Exception {
		//TODO: need to optimize so we stop searching filesystem once we find it
		return directory.search(".*android").filter(file -> {
			return new File(attempt(() -> file.parent()), "emulator").exists();
		}).map(file -> attempt(() -> file.parent().parent())).findFirst().orElse(null);
	}
}
