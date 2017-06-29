package treehouse.tool.cordova;

import static javax.lang.System.*;
import static javax.lang.Try.*;
import static javax.util.Map.*;
import static javax.net.Urls.*;

import javax.lang.Process;
import javax.io.File;
import javax.io.File.FileWatcher;
import javax.util.Map;

import treehouse.App;
import treehouse.Engine;
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
		return this.start(false, map());
	}

	public Engine run(Map<String, String> options) throws Exception {
		return this.start(false, options);
	}

	public Engine start() throws Exception {
		return this.start(true, map());
	}

	public Engine start(Map<String, String> options) throws Exception {
		return this.start(true, options);
	}

	public Engine start(boolean continous) throws Exception {
		return this.start(continous, map());
	}

	public Engine start(boolean continous, Map<String, String> options) throws Exception {
		return this.start(
			continous && (options.get("watch", "both").equals("both") || options.get("watch", "").equals("source")),
			continous && (options.get("watch", "both").equals("both") || options.get("watch", "").equals("browser")),
			options
		);
	}

	protected Engine start(boolean watchSource, boolean watchBrowser, Map<String, String> options) throws Exception {
		this.cordova = this.engine.cordova().run(this.app, this.directory);
		println("  - Started cordova server");
		
		//this.cordova.future().onOutput((line, process) -> {
		//	if (options.get("verbose", "false").equals("true"))
		//		println("    [cordova]: " + line);
		//});
		

		Thread.sleep(2000);
		this.chrome = this.engine.chrome().launch(url("http://localhost:8015/index.html"), Chrome.DEV_TOOLS_ENABLED).closed(time -> {
			if (watchBrowser)
				attempt(() -> this.reload(options));
			else
				attempt(() -> this.stop(options));
		});

		println("  - Started chrome browser");
		if (watchBrowser)
			println("  - Watching chrome browser...");

		if (watchSource) {
			println("  - Watching for source changes...");
			this.watcher = this.engine.source().watcher((file, operation) -> {
				attempt(() -> this.reload(options));
			}).watch();
		}

		return this.engine;
	}

	public void stop(Map<String, String> options) {
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

	public void reload(Map<String, String> options) throws Exception {
		println("  - Reloading...");
		this.stop(options);
		this.sync(options);
		this.start(options);
	}

	public void sync(Map<String, String> options) throws Exception {
		//this.engine.source().synchronizer(this.engine.work("cordova/www")).synchronize(false);
	}
}
