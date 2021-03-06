package treehouse.tool.chrome;

import static javax.lang.System.*;
import static javax.util.List.list;

import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import javax.lang.Time;
import javax.lang.Try;

import io.webfolder.cdp.Launcher;
import io.webfolder.cdp.session.Session;
import io.webfolder.cdp.session.SessionFactory;
import io.webfolder.cdp.session.SessionInfo;

public class Chrome extends Launcher {

	protected Process process;
	protected SessionFactory factory = new SessionFactory();
	protected ChromeWatcher watcher;

	public static String DEV_TOOLS_ENABLED = "--auto-open-devtools-for-tabs";

	public Chrome launch(URL url, String... options) throws Exception {
		this.process = this.execute(list("--disable-web-security", "--test-type", "--no-sandbox").append(options).append(url.toString()).toArray(new String[0]));
		return this;
	}

	public Process execute(String... arguments) throws Exception {
        String chromePath = findChrome();
        Path remoteProfileData = Paths.get(System.getProperty("java.io.tmpdir")).resolve("remote-profile");

        List<String> list = new ArrayList<>();
        list.add(chromePath);
        list.add(String.format("--remote-debugging-port=%d", factory.getPort()));
        list.add(String.format("--user-data-dir=%s", remoteProfileData.toString()));
        list.add(String.format("--remote-debugging-address=%s", factory.getHost()));
        list.add("--disable-translate");
        list.add("--disable-extensions");
        list.add("--no-default-browser-check");
        list.add("--disable-plugin-power-saver");
        list.add("--disable-sync");
        list.add("--no-first-run");
        list.add("--safebrowsing-disable-auto-update");
        list.add("--disable-popup-blocking");
        if (arguments != null)
            list.addAll(Arrays.asList(arguments));

        Process process = Runtime.getRuntime().exec(list.toArray(new String[0]));
        process.getOutputStream().close();
        process.getInputStream().close();
    	return process;
    }

	public List<SessionInfo> sessions() {
		return this.process != null ? this.factory.list() : list();
	}

	public Session get(String url) {
		for (SessionInfo session : this.sessions()) {
			if (session.getUrl().equals(url))
			    return this.factory.connect(session.getId());
		}

		return null;
	}

	public Session open(String url) {
		return this.factory.create().navigate(url);
	}

	public Chrome close() {
		if (this.factory != null) {
			Try.attempt(() -> this.factory.close());
			this.factory = null;
		}

		if (this.process != null) {
			this.process.destroyForcibly();
			this.process = null;
		}

		return this;
	}

	public boolean closed() {
		return this.process == null || this.process.isAlive() == false;
	}

	public Chrome closed(Consumer<Long> handler) {
		this.watcher = new ChromeWatcher(this, handler).watch();
		return this;
	}

	public Chrome cancel() {
		if (this.watcher != null) {
			this.watcher.halt();
			while (this.watcher.isAlive() == true)
				Time.sleep(100, TimeUnit.MILLISECONDS);
		}

		return this;
	}

	public String findChrome() {
		return isMac() ? "/Applications/Google Chrome.app/Contents/MacOS/Google Chrome" : super.findChrome();
	}

	private class ChromeWatcher extends Thread {

		private Chrome chrome;
		private Consumer<Long> handler;
		private boolean running = false;

		public ChromeWatcher(Chrome chrome, Consumer<Long> handler) {
			this.chrome = chrome;
			this.handler = handler;
		}

		public ChromeWatcher watch() {
			this.start();
			return this;
		}

		public void run() {
			this.running = true;
			while (this.running == true && this.chrome.closed() == false)
				Time.sleep(100, TimeUnit.MILLISECONDS);

			if (this.running == true && this.handler != null) this.handler.accept(now());
		}

		public void halt() {
			this.running = false;
		}
	}
}
