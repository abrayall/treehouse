package treehouse;


import static javax.lang.Try.attempt;
import static javax.util.Map.entry;
import static javax.util.Map.map;

import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import javax.io.File;
import javax.util.Map;

import treehouse.app.App;
import treehouse.sdk.android.Android;
import treehouse.tool.chrome.Chrome;
import treehouse.tool.cordova.Cordova;
import treehouse.tool.cordova.CordovaBuilder;
import treehouse.tool.cordova.CordovaRunner;
import treehouse.tool.fastlane.Fastlane;

public class Engine {
	
	private File directory;
	private Cordova cordova;
	private Android android;
	
	public Engine(File directory) {
		this.directory = directory;
		this.cordova = new Cordova();
		this.android = new Android();
	}

	public Engine run(App app) throws Exception {
		return this.run(app, false);
	}
	
	public Engine run(App app, boolean continous) throws Exception {
		System.out.println("Running app " + " [id: " + app.getId() + "] [version: " + app.getVersion() + "]" + (continous == true ? " [continous mode]" : "") + "...");
		return new CordovaRunner(this, app, this.work("cordova")).start(continous);
	}

	public Engine build(App app) throws Exception {
		return this.build(app, map(
			entry("type", "release"),
			entry("verbose", "false")
		));
	}
	
	public Engine build(App app, Map<String, String> options) throws Exception {
		for (String platform : CordovaBuilder.platforms()) {
			File output = new File("build/" + platform + "/").delete();
			System.out.println("Building " + app.getName() + " [id: " + app.getId() + "] [version: " + app.getVersion() + "] for " + platform + "...");
			CordovaBuilder.builder(platform, this, this.work("cordova")).build(app, options).onComplete(file -> {
				String artifact = (app.getName() + "." + file.name().split("\\.")[1]).replaceAll(" ", "-").toLowerCase();
				attempt(() -> Files.copy(file.toPath(), new File(output, artifact).mkdirs().toPath(), StandardCopyOption.REPLACE_EXISTING));
				System.out.println("Build for " + platform + " complete [" + new File(output, artifact) + "]");
			});
		}
		
		return this;
	}
	
	public File source() {
		return new File(this.directory, "www");
	}

	public File resources() {
		return new File(this.directory, "resources");
	}
	
	public File work() {
		return new File("."); //new File(this.directory, "work");
	}
	
	public File work(String name) {
		return this.work(); //new File(this.work(), name);
	}
	
	public Cordova cordova() throws Exception {
		return this.cordova;
	}

	public Android android() throws Exception {
		return this.android;
	}
	
	public Fastlane fastlane() throws Exception {
		return null;
	}

	public Chrome chrome() throws Exception {
		return new Chrome();
	}
}
