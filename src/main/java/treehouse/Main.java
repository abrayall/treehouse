package treehouse;

import static javax.io.File.*;
import static javax.util.Map.*;
import static javax.util.Properties.*;

import javax.io.File;
import javax.lang.Try;
import javax.util.List;
import javax.util.Map;
import javax.util.Properties;

import treehouse.version.Version;

public class Main extends cilantro.Main {
	
	protected static Map<String, File> scopes = map(
		entry("local",  file(".treehouse/settings")),
		entry("user",   file(file(System.getProperty("user.home", "~")), ".treehouse/settings")),
		entry("global", file("/etc/treehouse/settings"))
	);
	
	public Integer execute(List<String> parameters, Map<String, String> options) throws Exception {
		return run(parameters, merge(options, scopes.get("user"), scopes.get("local")));
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
		else if (matches(parameters, "settings"))
			settings();
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
		println("    - list:     lists the supported platforms");
		println("    - clean:    cleans the build work area");
		println("    - run:      runs the app in browser");
		println("    - develop:  runs the app in browser and reloads on code changes");
		println("    - build:    builds the app for all supported platforms");
		println("    - settings: list all the current settings in all scopes");
		println("    - set:      sets a setting in a given scope");
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

	public void settings() throws Exception {
		println("Settings:");
		settings("global");
		settings("user");
		settings("local");
	}
	
	public void settings(String scope) throws Exception {
		settings(scope, Try.attempt(() -> properties(scopes.get(scope)), properties()), scopes.get(scope));
	}
	
	public void settings(String scope, Properties properties, File file) {
		println("  " + scope + " [" + file + "]");
		for (Object key : properties.keySet())
			println("    - " + key + "=" + properties.get(key));
		
		println();
	}
	
	public void set(String key, String value, Map<String, String> options) throws Exception {
		set(scopes.get(options.get("scope", "local").toLowerCase(), scopes.get("local")).mkdirs(), key, value);
	}
	
	public void set(File settings, String key, String value) throws Exception {
		Properties properties = properties(settings);
		if (value.equals("") == false)
			set(settings, properties, key, value);
		else
			delete(settings, properties, key);
	}

	protected void set(File settings, Properties properties, String key, String value) throws Exception {
		properties.set(key, value).store(settings.outputStream());
		println("\nSet '" + key + "' to '" + value + "' [file " + settings + "].\n");
	}

	protected void delete(File settings, Properties properties, String key) throws Exception {
		properties.delete(key).store(settings.outputStream());
		println("\nDeleted '" + key + "' [file " + settings + "].\n");	
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
			properties(location, properties()).each((key, value) -> {
				merged.put(key.toString(), value.toString());
			});
		}
		
		return merged.put(options);
	}
	
	public static void main(String[] arguments) throws Exception {
		main(Main.class, arguments);
	}
}