package treehouse;

import static javax.lang.System.println;

import java.util.Arrays;

import javax.io.File;

import treehouse.app.App;
import treehouse.version.Version;

public class Main {
	public static void main(String[] arguments) throws Exception {
		println("---------------------------------------");
		println("Treehouse - Mobile App Toolchain v" + Version.getVersion());
		println("---------------------------------------");

		File config = new File("config.xml");
		if (config.exists() == false)
			error("Not a valid mobile app project directory");

		App app = App.fromConfigXml(config);
		Engine engine = new Engine(new File("."));
		if (matches(arguments, empty()) || matches(arguments, "run"))
			engine.run(app);
		else if (matches(arguments, "run", "continous") || matches(arguments, "develop") || matches(arguments, "dev"))
			engine.run(app, true);
		else if (matches(arguments, "build"))
			engine.build(app);
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

	protected static boolean matches(String[] arguments, String... values) {
		return Arrays.equals(arguments, values);
	}

	protected static String[] empty() {
		return new String[0];
	}
}


//File configuration = new File(current, "app.conf");

//if (configuration.exists() == false)
//	error("Not a treehouse project directory");

//Project project = new Project(current);
//if (arguments.length > 0 && "create".equals(list(arguments).get(0)))
//	project.load();
//else
//	project.run();

//App app = App.load(configuration);
