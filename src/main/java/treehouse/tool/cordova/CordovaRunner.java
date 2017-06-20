package treehouse.tool.cordova;

import static javax.lang.System.*;
import static javax.lang.Try.*;
import static javax.net.Urls.*;

import javax.io.File;
import javax.io.File.FileWatcher;

import treehouse.Engine;
import treehouse.app.App;
import treehouse.tool.chrome.Chrome;

public class CordovaRunner {

	private Engine engine;
	private Process cordova;
	private Chrome chrome;
	private FileWatcher watcher;

	private App app;
	private File directory;

	public CordovaRunner(Engine engine, App app, File directory) {
		this.engine = engine;
		this.app = app;
		this.directory = directory;
	}

	public Engine run() throws Exception {
		return this.start(false);
	}

	public Engine start() throws Exception {
		return this.start(true);
	}

	public Engine start(boolean continous) throws Exception {
		this.cordova = this.engine.cordova().run(this.app, this.directory);
		println("  - Started cordova server");

		Thread.sleep(2000);
		this.chrome = this.engine.chrome().launch(url("http://localhost:8015/index.html"), Chrome.DEV_TOOLS_ENABLED).closed(time -> {
			this.stop();
		});

		println("  - Started chrome browser");

		if (continous) {
			println("  - Watching source files for changes...");
			this.watcher = this.engine.source().watcher((file, operation) -> {
				attempt(() -> this.reload());
			}).watch();
		}

		return this.engine;
	}

	public void stop() {
		attempt(() -> this.watcher.halt());
		if (this.chrome.closed() == false) {
			println("  - Stopping chrome browser...");
			this.chrome.cancel().close();
		}

		if (this.cordova.isAlive()) {
			println("  - Stopping cordova server...");
			attempt(() -> this.cordova.destroyForcibly().waitFor());
		}
	}

	public void reload() throws Exception {
		println("  - Reloading because source files changed...");
		this.stop();
		this.sync();
		this.start();
	}

	public void sync() throws Exception {
		//this.engine.source().synchronizer(this.engine.work("cordova/www")).synchronize(false);
	}
}
