package treehouse;


import static javax.lang.System.*;
import static javax.lang.Try.*;
import static javax.util.List.*;
import static javax.util.Map.*;

import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import javax.io.File;
import javax.util.List;
import javax.util.Map;

import cilantro.io.Console;
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
	private Console console;
	
	private Cordova cordova;
	private Fastlane fastlane;
	private Android android;

	public Engine(File directory, Console console) {
		this.directory = directory;
		this.console = console;
		this.cordova = new Cordova();
		this.android = new Android();
		this.fastlane = new Fastlane();
	}

	public Engine clean(App app) throws Exception {
		return this.clean(app, "*");
	}
	
	public Engine clean(App app, String platform) throws Exception {
		return this.build(app, platform, map());
	}
	
	public Engine clean(App app, String platform, Map<String, String> options) throws Exception {
		return this.clean(app, this.platforms(platform), options);
	}
	
	protected Engine clean(App app, List<String> platforms, Map<String, String> options) throws Exception {
		for (String platform : platforms) {
			File output = new File("build/" + platform + "/");
			if (output.exists()) {
				println("Cleaning " + app.getName() + " [" + app.getId() + "] [v" + app.getVersion() + "] for " + platform + "...");
				output.delete();
			}
		}
		
		println("Clean complete.\n");
		return this;
	}
	
	public Engine run(App app) throws Exception {
		return this.run(app, map());
	}

	public Engine run(App app, Map<String, String> options) throws Exception {
		return this.run(app, false, options);
	}

	public Engine run(App app, boolean continous, Map<String, String> options) throws Exception {
		println("Running " + app.getName() + " [" + app.getId() + "] [v" + app.getVersion() + "]" + (continous == true ? " [continous mode]" : "") + "...");
		return new CordovaRunner(this, app, this.work("cordova")).start(continous, options);
	}

	public Engine build(App app) throws Exception {
		return this.build(app, "*", map());
	}

	public Engine build(App app, String platform, Map<String, String> options) throws Exception {
		List<String> platforms = this.platforms(platform);
		if (platforms.size() == 0)
			error("No supported platform to build " + (platform.equals("*") ? "" : "[" + platform +"]"));

		return this.build(app, platforms, options);
	}
	
	protected Engine build(App app, List<String> platforms, Map<String, String> options) throws Exception {
		for (String platform : platforms) {
			File output = new File("build/" + platform + "/");
			if (output.latest() > this.source().latest())
				println("Build of " + app.getName() + " [" + app.getId() + "] [v" + app.getVersion() + "] for " + platform + " is up to date.\n");
			else {
				output.delete();
				println("Building " + app.getName() + " [" + app.getId() + "] [v" + app.getVersion() + "] for " + platform + "...");
				CordovaBuilder.builder(platform, this, this.work("cordova")).build(app, options).onComplete(file -> {
					String artifact = (app.getName() + "." + file.name().split("\\.")[1]).replaceAll(" ", "-").toLowerCase();
					attempt(() -> Files.copy(file.toPath(), new File(output, artifact).mkdirs().toPath(), StandardCopyOption.REPLACE_EXISTING));
					println("Build for " + platform + " complete [" + new File(output, artifact) + "].\n");
				}).onError(exception -> this.error(exception, options));
			}
		}

		return this;
	}

	public Engine publish(App app, String platform, String track) throws Exception {
		return this.publish(app, platform, track, map());
	}

	public Engine publish(App app, String platform, String track, Map<String, String> options) throws Exception {
		List<String> platforms = this.platforms(platform);
		if (platforms.size() == 0)
			error("No supported platform " + (platform.equals("*") ? "" : "[" + platform +"]."));

		return this.publish(app, platforms, track, options);
	}

	protected Engine publish(App app, List<String> platforms, String track, Map<String, String> options) throws Exception {
		for (String platform : platforms) {
			this.build(app, platform, options);
			println("Publishing " + app.getName() + " [" + app.getId() + "] [v" + app.getVersion() + "] to " + store(platform) + " [" + track + "]...");
			FastlanePublisher.publisher(platform, this).publish(app, new File("build/" + platform + "/" + app.getName().toLowerCase() + "." + (platform.equals("android") ? "apk" : "ipa")), track, options).onComplete(result -> {
				println("Publish to " + store(platform) + " complete.\n");
			}).onError(exception -> this.error(exception, options));
		}

		return this;
	}

	public void error(String message) {
		console.println();
		console.printlnf("${format([Error]:, red, bold)} " + message + "\n");
		System.exit(-1);
	}

	public void error(Throwable error, Map<String, String> options) {
		error(error.getMessage());
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
		List<String> platforms = list();
		if (this.android.installed())
			platforms.add("android");

		if (isMac())
			platforms.add("ios");

		return platforms;
	}

	public List<String> platforms(String filter) {
		return list(this.platforms().stream().filter(string -> string.equals(filter) || filter.equals("*")).toArray(String[]::new));
	}

	public String store(String platform) {
		return platform.equals("android") ? "google" : "apple";
	}
}
