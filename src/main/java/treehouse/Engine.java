package treehouse;

import static javax.net.Urls.url;
import static javax.util.Map.*;

import javax.io.File;
import javax.io.File.FileWatcher;
import javax.lang.Try;

import treehouse.android.Android;
import treehouse.browser.Browser;
import treehouse.cordova.Cordova;
import treehouse.fastlane.Fastlane;

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
		return new Builder(this, app, this.work("cordova")).start();
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

	public Browser browser() throws Exception {
		return new Browser();
	}
		
	private class Runner {
		
		private Engine engine;
		private Process cordova;
		private Browser browser;
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
			this.browser = this.engine.browser().launch(url("http://localhost:8015/index.html"), Browser.DEV_TOOLS_ENABLED).closed(time -> {
				this.stop();
			});
			
			System.out.println("Started chrome browser");
			
			if (continous) {
				System.out.println("Watching source files for changes...");
				this.watcher = this.engine.source().watcher((file, operation) -> {
					Try.attempt(() -> this.reload());
				}).watch();
			}
			
			return this.engine;
		}
		
		public void stop() {
			Try.attempt(() -> this.watcher.halt());
			if (this.browser.closed() == false) {
				System.out.println("Stopping chrome browser...");
				this.browser.cancel().close();
			}
			
			if (this.cordova.isAlive()) {
				System.out.println("Stopping cordova server...");
				Try.attempt(() -> this.cordova.destroyForcibly().waitFor());
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
	
	private class Builder {

		//MAybe Android builder, AppleBuilder, etc...
		private App app;
		private File directory;
		private Engine engine;

		public Builder(Engine engine, App app, File directory) {
			this.engine = engine;
			this.app = app;
			this.directory = directory;
		}
		
		public Engine start() throws Exception {
			System.out.println("Building app...");
			this.engine.cordova().build(this.app, this.directory, "", map(
				entry("ANDROID_HOME", this.engine.android().home().toString())
			));
			
			return engine;
		}
	}
}
