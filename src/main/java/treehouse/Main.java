package treehouse;

import static javax.lang.System.println;

import treehouse.cordova.Cordova;
import treehouse.fastlane.Fastlane;
import treehouse.version.Version;

public class Main {
	public static void main(String[] arguments) throws Exception {
		println("Treehouse - Mobile App Toolchain v" + Version.getVersion());
		println("-----------------------------------------");
		println("");
		println("Cordova version: " + new Cordova().getVersion());
		println("Fastline version: " + new Fastlane().getVersion());
	}
}
