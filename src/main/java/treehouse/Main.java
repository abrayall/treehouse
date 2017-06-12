package treehouse;

import static javax.lang.System.*;

import treehouse.version.Version;

public class Main {
	public static void main(String[] arguments) {
		println("Treehouse - Mobile App Toolchain v" + Version.getVersion());
	}
}
