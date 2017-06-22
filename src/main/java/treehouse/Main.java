package treehouse;

import static javax.lang.System.println;
import static javax.util.Map.*;
import static javax.util.List.*;

import java.util.Arrays;
import javax.io.File;
import javax.util.Map;
import javax.util.List;

import treehouse.version.Version;

public class Main {
	public static void main(String[] arguments) throws Exception {
		println("---------------------------------------");
		println("Treehouse - Mobile App Toolchain v" + Version.getVersion());
		println("---------------------------------------");

		List<String> parameters = parameters(arguments);
		Map<String, String> options = options(arguments);

		File config = new File("config.xml");
		if (config.exists() == false)
			error("Not a valid mobile app project directory");

		App app = App.fromConfigXml(config);
		Engine engine = new Engine(new File("."));
		if (matches(parameters, empty()) || matches(parameters, "run"))
			engine.run(app);
		else if (matches(parameters, "run", "continous") || matches(parameters, "develop") || matches(parameters, "dev"))
			engine.run(app, true);
		else if (matches(parameters, "build"))
			engine.build(app, options);
		else if (matches(parameters, "publish"))
			engine.publish(app, "production", options);
		else
			help();
	}

	public static void help() {
		println();
		println("Usage:");
		println("treehouse [command]");
		println("  commands: ");
		println("    - run:     runs the app in browser");
		println("    - develop: runs the app in browser and reloads on code changes");
		println("    - build:   builds the app for all supported platforms");
		println();
	}

	public static void error(String message) {
		exit("Error: " + message, -1);
	}

	public static void exit(String message) {
		exit(message, 1);
	}

	public static void exit(String message, int code) {
		println(message);
		println();
		System.exit(code);
	}

	protected static List<String> parameters(String[] arguments) {
		List<String> parameters = list();
		for (String argument : arguments) {
			if (argument.startsWith("-") == false)
				parameters.add(argument);
		}

		return parameters;
	}

	protected static Map<String, String> options(String[] arguments) {
		Map<String, String> options = map();
		for (String argument : arguments) {
			if (argument.startsWith("-") && argument.contains("="))
				options.put(argument.split("=")[0].replaceAll("-", ""), argument.split("=")[1]);
		}

		return options;
	}

	protected static boolean matches(List<String> parameters, String... values) {
		if (parameters.size() != values.length)
			return false;

		for (int i = 0; i < values.length; i++) {
			if (parameters.get(i).equals(values[i]) == false && parameters.get(i).matches(values[i]) == false)
				return false;
		}

		return true;
	}

	protected static String[] empty() {
		return new String[0];
	}
}
