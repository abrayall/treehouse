package treehouse.tool.fastlane;

import javax.lang.Try;

import treehouse.tool.Tool;

import static javax.util.List.*;

public class Fastlane extends Tool {
	
	protected String name() {
		return "fastlane";
	}
	
	public String getVersion() throws Exception {
		return Try.attempt(() -> list(execute("--version").split("\n")).last().split(" ")[1], "0.0.0");
	}
}
