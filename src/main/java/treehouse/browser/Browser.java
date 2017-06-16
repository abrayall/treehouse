package treehouse.browser;

import static javax.lang.System.*;
import static javax.util.List.*;

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

public class Browser extends Launcher {

	protected Process process;
	protected SessionFactory factory = new SessionFactory();
	protected BrowserWatcher watcher;

	public static String DEV_TOOLS_ENABLED = "--auto-open-devtools-for-tabs";

	public Browser launch(URL url, String... options) throws Exception {
		this.process = this.execute(list("--disable-web-security", "--test-type", "--no-sandbox").append(options).append(url.toString()).toArray(new String[0]));
		return this;
	}

	public Process execute(String... arguments) throws Exception {
        String chromePath = findChrome();
        Path remoteProfileData = Paths.get(System.getProperty("java.io.tmpdir")).resolve("remote-profile");

        List<String> list = new ArrayList<>();
        list.add(chromePath);
        list.add(String.format("--remote-debugging-port=%d", factory.getPort()));
        //list.add(String.format("--user-data-dir=%s", remoteProfileData.toString()));
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

	public Browser close() {
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

	public Browser closed(Consumer<Long> handler) {
		this.watcher = new BrowserWatcher(this, handler).watch();
		return this;
	}

	public Browser cancel() {
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

	private class BrowserWatcher extends Thread {

		private Browser browser;
		private Consumer<Long> handler;
		private boolean running = false;

		public BrowserWatcher(Browser browser, Consumer<Long> handler) {
			this.browser = browser;
			this.handler = handler;
		}

		public BrowserWatcher watch() {
			this.start();
			return this;
		}

		public void run() {
			this.running = true;
			while (this.running == true && this.browser.closed() == false)
				Time.sleep(100, TimeUnit.MILLISECONDS);

			if (this.running == true && this.handler != null) this.handler.accept(now());
		}

		public void halt() {
			this.running = false;
		}
	}

	public static void main(String[] arguments) throws Exception {
	//	Browser browser = new Browser().launch("http://localhost:8015/index.html", Browser.DEV_TOOLS_ENABLED);
		//Thread.sleep(3000);
		//SessionFactory browser = , "http://localhost:8015/index.html");
		//SessionFactory factory = new SessionFactory();

	   // Thread.sleep(5000);
   		//Session window = browser.get("http://localhost:8015/index.html");
        //window.waitDocumentReady();
        //window.activate();
        //window.getCommand().getEmulation().setVisibleSize(375, 667);

      //  browser.close();
        //try (Session session = factory.create()) {
        //    session.navigate("http://localhost:8015/index.html");
        //    session.waitDocumentReady();
            // activate the tab/session before capturing the screenshot
        //    session.activate();
        //   Streams.write(session.captureScreenshot(), new FileOutputStream(file.toFile()));

        //    Thread.sleep(10000000);
        //}
    }
}
