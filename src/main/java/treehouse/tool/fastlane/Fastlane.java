package treehouse.tool.fastlane;

import static javax.util.List.list;

import javax.io.File;
import javax.lang.Try;
import javax.util.List;

import treehouse.App;
import treehouse.tool.Tool;

public class Fastlane extends Tool {
	
	protected String name() {
		return "fastlane";
	}
	
	public String getVersion() throws Exception {
		return Try.attempt(() -> list(execute("--version").split("\n")).last().split(" ")[1], "0.0.0");
	}
	
	public Process supply(App app, String track, File artifact, File key, List<String> extra) throws Exception {
		return this.process(new File("."), arguments(extra, "supply", "--apk", artifact.toString(), "--track", track, "--package_name", app.getId(), "--json_key", key.toString())).start();
	}
	
	protected String[] arguments(List<String> extra, String... parameters) {
		List<String> arguments = list(parameters);
		arguments.addAll(extra);
		return arguments.toArray(new String[0]);
	}
}
