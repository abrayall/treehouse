package treehouse;


import javax.io.File;
import javax.io.File.FileWatcher;
import javax.util.Map;

import static javax.lang.Try.*;
import static javax.util.Map.*;
import static javax.net.Urls.url;

import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import treehouse.app.App;
import treehouse.sdk.android.Android;
import treehouse.tool.chrome.Chrome;
import treehouse.tool.cordova.Cordova;
import treehouse.tool.cordova.CordovaBuilder;
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
	
	public Engine build(App app) throws Exception {
		return this.build(app, map(
			entry("type", "release"),
			entry("verbose", "false")
		));
	}
	
	public Engine build(App app, Map<String, String> options) throws Exception {
		for (String platform : CordovaBuilder.platforms()) {
			File output = new File("build/" + platform + "/").delete();
			System.out.println("Building " + app.getName() + " [" + app.getId() + "] for " + platform + "...");
			CordovaBuilder.builder(platform, this, this.work("cordova")).build(app, options).onComplete(file -> {
				String artifact = (app.getName() + "." + file.name().split("\\.")[1]).replaceAll(" ", "-").toLowerCase();
				attempt(() -> Files.copy(file.toPath(), new File(output, artifact).mkdirs().toPath(), StandardCopyOption.REPLACE_EXISTING));
				System.out.println("Build for " + platform + " complete [" + new File(output, artifact) + "]");
			});
		}
		
		return this;
	}
	
	public Engine run(App app) throws Exception {
		return this.run(app, false);
	}
	
	public Engine run(App app, boolean continous) throws Exception {
		return new Runner(this, app, this.work("cordova")).start(continous);
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
		
	private class Runner {
		
		private Engine engine;
		private Process cordova;
		private Chrome chrome;
		private FileWatcher watcher;
		
		private App app;
		private File directory;
		
		public Runner(Engine engine, App app, File directory) {
			this.engine = engine;
			this.app = app;
			this.directory = directory;
		}
		
		@SuppressWarnings("unused")
		public Engine run() throws Exception {
			return this.start(false);
		}
		
		public Engine start() throws Exception {
			return this.start(true);
		}
		
		public Engine start(boolean continous) throws Exception {
			System.out.println("Running app" + (continous == true ? " [continous mode]" : "") + "...");
			this.cordova = this.engine.cordova().run(this.app, this.directory);
			System.out.println("Started cordova server");
			
			Thread.sleep(2000);
			this.chrome = this.engine.chrome().launch(url("http://localhost:8015/index.html"), Chrome.DEV_TOOLS_ENABLED).closed(time -> {
				this.stop();
			});
			
			System.out.println("Started chrome browser");
			
			if (continous) {
				System.out.println("Watching source files for changes...");
				this.watcher = this.engine.source().watcher((file, operation) -> {
					attempt(() -> this.reload());
				}).watch();
			}
			
			return this.engine;
		}
		
		public void stop() {
			attempt(() -> this.watcher.halt());
			if (this.chrome.closed() == false) {
				System.out.println("Stopping chrome browser...");
				this.chrome.cancel().close();
			}
			
			if (this.cordova.isAlive()) {
				System.out.println("Stopping cordova server...");
				attempt(() -> this.cordova.destroyForcibly().waitFor());
			}
		}

		public void reload() throws Exception {
			System.out.println("Reloading because source files changed...");
			this.stop();
			this.sync();
			this.start();
		}
		
		public void sync() throws Exception {
			//this.engine.source().synchronizer(this.engine.work("cordova/www")).synchronize(false);
		}
	}
}
