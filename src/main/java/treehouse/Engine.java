package treehouse;


import static javax.lang.System.*;
import static javax.lang.Try.*;
import static javax.util.Map.*;
import static javax.util.List.*;

import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import javax.io.File;
import javax.util.Map;
import javax.util.List;

import treehouse.sdk.android.Android;
import treehouse.tool.chrome.Chrome;
import treehouse.tool.cordova.Cordova;
import treehouse.tool.cordova.CordovaBuilder;
import treehouse.tool.cordova.CordovaRunner;
import treehouse.tool.fastlane.Fastlane;
import treehouse.tool.fastlane.FastlanePublisher;

//TODO: Merge Cordova, CordovaBuilder and CordovaRunner
//TODO: Merge Fastlane, FastlanePublisher
//TODO: Remove Tool base class

public class Engine {

	private File directory;
	private Cordova cordova;
	private Fastlane fastlane;
	private Android android;

	public Engine(File directory) {
		this.directory = directory;
		this.cordova = new Cordova();
		this.android = new Android();
		this.fastlane = new Fastlane();
	}

	public Engine run(App app) throws Exception {
		return this.run(app, false);
	}

	public Engine run(App app, boolean continous) throws Exception {
		println("Running app " + " [id: " + app.getId() + "] [version: " + app.getVersion() + "]" + (continous == true ? " [continous mode]" : "") + "...");
		return new CordovaRunner(this, app, this.work("cordova")).start(continous);
	}

	public Engine build(App app) throws Exception {
		return this.build(app, map(
			entry("type", "release"),
			entry("verbose", "false")
		));
	}

	public Engine build(App app, Map<String, String> options) throws Exception {
		for (String platform : this.platforms()) {
			File output = new File("build/" + platform + "/").delete();
			//TODO: only build if source is newer and output if it exists
			println("Building " + app.getName() + " [" + app.getId() + "] [v" + app.getVersion() + "] for " + platform + "...");
			CordovaBuilder.builder(platform, this, this.work("cordova")).build(app, options).onComplete(file -> {
				String artifact = (app.getName() + "." + file.name().split("\\.")[1]).replaceAll(" ", "-").toLowerCase();
				attempt(() -> Files.copy(file.toPath(), new File(output, artifact).mkdirs().toPath(), StandardCopyOption.REPLACE_EXISTING));
				println("Build for " + platform + " complete [" + new File(output, artifact) + "]");
			}).onError(exception -> this.error(exception, options));
		}

		return this;
	}

	public Engine publish(App app, String track) throws Exception {
		return this.publish(app, track, map());
	}

	public Engine publish(App app, String track, Map<String, String> options) throws Exception {
		this.build(app);
		for (String platform : this.platforms()) {
			println("Publishing " + app.getName() + " [" + app.getId() + "] [v" + app.getVersion() + "] to " + store(platform) + " [" + track + "]...");
			FastlanePublisher.publisher(platform, this).publish(app, new File("build/" + platform + "/" + app.getName().toLowerCase() + "." + (platform.equals("android") ? "apk" : "ipa")), track, options).onComplete(result -> {
				println("Publish to " + store(platform) + " complete");
			}).onError(exception -> this.error(exception, options));
		}

		return this;
	}

	public void error(Throwable error, Map<String, String> options) {
		println();
		println("[Error]: " + error.getMessage());
		System.exit(-1);
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
		return this.fastlane;
	}

	public Chrome chrome() throws Exception {
		return new Chrome();
	}

	public List<String> platforms() {
		List<String> platforms = list("android");
		if (isMac())
			platforms.add("ios");

		return platforms;
	}

	public String store(String platform) {
		return platform.equals("android") ? "google" : "apple";
	}
}
