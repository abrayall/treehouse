package treehouse.cordova;

import javax.lang.Try;

import treehouse.util.Tool;

public class Cordova extends Tool {

	protected String name() {
		return "cordova";
	}
	
	public String getVersion() throws Exception {
		return Try.attempt(() -> execute("--version"), "0.0.0");
	}

}
