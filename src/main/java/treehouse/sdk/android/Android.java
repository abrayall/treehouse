package treehouse.sdk.android;


import javax.io.File;
import javax.lang.Try;
import javax.util.Map;

import static javax.lang.Try.*;

public class Android {

	protected File sdk;
	protected File studio;

	public Android(Map<String, String> options) {
		this.sdk = options.containsKey("android.sdk") == true ? new File(options.get("android.sdk")) : Try.attempt(() -> locate());
		this.studio = options.containsKey("android.studio") == true ? new File(options.get("android.sdk")) : null;
	}

	public Android(File sdk) {
		this.sdk = sdk;
	}

	public File sdk() {
		return this.sdk;
	}

	public File studio() {
		return this.studio != null ? this.studio : new File(this.sdk.toFile().getParentFile(), "studio");
	}

	public boolean installed() {
		return this.sdk != null && this.sdk.exists();
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
