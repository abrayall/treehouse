package treehouse;

import javax.io.File;
import javax.io.File.FileWatcher;
import javax.lang.Try;
import javax.util.Properties;

import treehouse.browser.Browser;
import treehouse.cordova.Cordova;
import treehouse.fastlane.Fastlane;

import static javax.net.Urls.*;

public class Project implements Context {
	
	private File directory;
	private Properties configuration = new Properties();
	private Cordova cordova;
	
	public Project(File directory) {
		this.directory = directory;
	}

	public Project load() throws Exception {
		this.configuration = Properties.properties(new File(this.directory, "app.conf").inputStream());
		this.cordova = new Cordova(this.work("cordova"), this).initialize();
		return this;
	}
	
	public String id() {
		return this.configuration.getProperty("id", "sample.app");
	}
	
	public String name() {
		return this.configuration.getProperty("name", "Sample App");
	}
	
	public Properties configuration() {
		return this.configuration;
	}

	public File source() {
		return new File(this.directory, "src");
	}
	
	public Project build() throws Exception {
		this.load();
		this.source().synchronizer(this.cordova().www()).synchronize(false);
		this.cordova().build();
		return this;
	}
	
	public Project run() throws Exception {
		this.load();
		new Runner(this).start();
		return this;
	}
	
	public File resources() {
		return new File(this.directory, "resources");
	}
	
	public File work() {
		return new File(this.directory, "work");
	}
	
	public File work(String name) {
		return new File(this.work(), name);
	}
	
	public Cordova cordova() throws Exception {
		return this.cordova;
	}

	public Fastlane fastlane() throws Exception {
		return null;
	}

	public Browser browser() throws Exception {
		return new Browser();
	}
		
	private class Runner {
		
		private Project project;
		private Process cordova;
		private Browser browser;
		private FileWatcher watcher;
		
		public Runner(Project project) {
			this.project = project;
		}
		
		public void start() throws Exception {
			this.cordova = this.project.cordova().run();
			System.out.println("Started cordova server");
			
			Thread.sleep(2000);
			this.browser = this.project.browser().launch(url("http://localhost:8015/index.html"), Browser.DEV_TOOLS_ENABLED).closed(time -> {
				this.stop();
			});
			
			System.out.println("Started chrome browser");
			System.out.println("Watching source files for changes...");
			this.watcher = this.project.source().watcher((file, operation) -> {
				Try.attempt(() -> this.reload());
			}).watch();
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
			this.project.source().synchronizer(this.project.cordova().www()).synchronize(false);
		}
	}
}
