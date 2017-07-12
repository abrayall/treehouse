package treehouse;

import static javax.io.File.*;
import static javax.util.Map.*;
import static javax.util.Properties.*;

import javax.io.File;
import javax.util.List;
import javax.util.Map;

import treehouse.version.Version;

public class Main extends cilantro.Main {
	public Integer execute(List<String> parameters, Map<String, String> options) throws Exception {
		return run(parameters, merge(options, 
			file(System.getProperty("user.home", ".")),
			file(".")
		));
	}
	
	public Integer run(List<String> parameters, Map<String, String> options) throws Exception {
		println("---------------------------------------");
		println("${format(Treehouse - Mobile App Toolchain, blue, bold)} ${format(v" + Version.getVersion() + ", yellow)}");
		println("---------------------------------------");

		if (options.get("verbose", "false").equals("true")) {
			println();
			println("  [parameters]: " + parameters.toString().replace("[", "").replace("]", ""));
			println("     [options]: " + options.map((key, value) -> key.equals("password") ? value.replaceAll(".", "*") : value).toString().replace("{", "").replace("}", "") + "\n");
		}

		File config = new File("config.xml");
		if (config.exists() == false)
			error("Not a valid mobile app project directory");
		
		App app = App.fromConfigXml(config);
		Engine engine = new Engine(new File("."), console);
		if (matches(parameters, empty()) || matches(parameters, "run"))
			engine.run(app, options);
		else if (matches(parameters, "clean", ".*"))
			engine.clean(app, parameters.get(1, "*"), options);
		else if (matches(parameters, "run", "continous", ".*") || matches(parameters, "dev.*", ".*"))
			engine.run(app, true, options);
		else if (matches(parameters, "build", ".*"))
			engine.build(app, parameters.get(1, "*"), options);
		else if (matches(parameters, "publish", ".*"))
			engine.publish(app, parameters.get(1, "*"), options.get("track", "production"), options);
		else if (matches(parameters, "list"))
			platforms(engine);
		else if (matches(parameters, "set", ".*"))
			set(parameters.get(1), "", options);		
		else if (matches(parameters, "set", ".*", ".*"))
			set(parameters.get(1), parameters.get(2), options);
		else
			help();
		
		return null;
	}

	public void help() {
		println();
		println("Usage: treehouse [command]");
		println("  commands: ");
		println("    - list:    lists the supported platforms");
		println("    - clean:   cleans the build work area");
		println("    - run:     runs the app in browser");
		println("    - develop: runs the app in browser and reloads on code changes");
		println("    - build:   builds the app for all supported platforms");
		println();
	}

	public void platforms(Engine engine) throws Exception {
		println();
		println("Supported platforms:");
		for (String platform : engine.platforms())
			println("  - " + platform + " " + (platform.equals("android") ? "[" + engine.android().home() + "]" : ""));

		println();
	}

	public void error(String message) {
		exit("${format([Error]:, red, bold)} " + message, -1);
	}

	public void exit(String message) {
		exit(message, 1);
	}

	public void exit(String message, int code) {
		println();
		println(message);
		println();
		System.exit(code);
	}

	public void set(String key, String value, Map<String, String> options) throws Exception {
		String scope = options.get("scope", "local").toLowerCase();
		if (scope.equals("local") == false && scope.equals("global") == false)
			scope = "local";
		
		set(file(scope.equals("local") ? file(".") : file(System.getProperty("user.home", ".")), ".treehouse/settings").mkdirs(), key, value);
	}
	
	public void set(File settings, String key, String value) throws Exception {
		properties(settings).set(key, value).store(settings.outputStream());
		println("\nSet " + key + " to " + value + " in file " + settings + "\n");
	}
	
	protected boolean matches(List<String> parameters, String... values) {
		if (parameters.size() > values.length)
			return false;

		for (int i = 0; i < values.length; i++) {
			String parameter = parameters.get(i, "");
			if (parameter.equals(values[i]) == false && parameter.matches(values[i]) == false)
				return false;
		}

		return true;
	}

	protected String[] empty() {
		return new String[0];
	}
	
	protected Map<String, String> merge(Map<String, String> options, File... locations) {
		Map<String, String> merged = map();
		for (File location : locations) {
			properties(file(location, ".treehouse/settings"), properties()).each((key, value) -> {
				merged.put(key.toString(), value.toString());
			});
		}
		
		return merged.put(options);
	}
	
	public static void main(String[] arguments) throws Exception {
		main(Main.class, arguments);
	}
}