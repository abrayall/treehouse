package treehouse;

import static javax.lang.System.println;
import static javax.util.List.*;

import javax.io.File;
import treehouse.version.Version;

public class Main {
	public static void main(String[] arguments) throws Exception {
		println("Treehouse - Mobile App Toolchain v" + Version.getVersion());
		println("-----------------------------------------");
		println("");
		//println("Cordova version: " + new Cordova().getVersion());
		//println("Fastline version: " + new Fastlane().getVersion());
		
		File current = new File(".");
		if (new File(current, "app.conf").exists() == false)
			println("Not a treehouse project directory");
		else {
			Project project = new Project(current);
			if (arguments.length > 0 && "create".equals(list(arguments).get(0)))
				project.load();
			else	
				project.run();
		}
	}
}
