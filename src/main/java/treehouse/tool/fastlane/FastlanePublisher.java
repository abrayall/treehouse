package treehouse.tool.fastlane;

import static javax.util.Map.* ;
import static javax.util.List.*;

import javax.io.File;
import javax.lang.Process;
import javax.util.Map;
import javax.util.concurrent.Future;

import treehouse.App;
import treehouse.Engine;

public abstract class FastlanePublisher {

	protected Engine engine;

	protected boolean result = false;
	protected String reason = "";

	public FastlanePublisher(Engine engine) {
		this.engine = engine;
	}

	public abstract Future<Boolean> publish(App app, File artifact, String track, Map<String, String> options) throws Exception;

	public Future<Boolean> publish(App app, File artifact, String track) throws Exception {
		return this.publish(app, artifact, track, map());
	}

	public void finish(Integer code, Future<Boolean> job) {
		if (this.result)
			job.complete(true);
		else
			job.completeExceptionally(new Exception(this.reason));
	}

	public static FastlanePublisher publisher(String platform, Engine engine) {
		if ("android".equals(platform))
			return new FastlanePlayPublisher(engine);
		else if ("ios".equals(platform))
			return new FastlaneApplePublisher(engine);

		return null;
	}

	public static class FastlanePlayPublisher extends FastlanePublisher {

		protected Engine engine;

		protected boolean result = false;
		protected String reason = "Unknown error";

		public FastlanePlayPublisher(Engine engine) {
			super(engine);
		}

		public Future<Boolean> publish(App app, File artifact, String track, Map<String, String> options) throws Exception {
			return new Process(this.engine.fastlane().supply(app, track, artifact, new File(options.get("key", "resources/android/play.json")), list(
				"--skip_upload_images",
				"--skip_upload_screenshots",
				"--skip_upload_metadata"
			))).future(Boolean.class).onOutput((line, job) -> this.handle(line, job, Boolean.parseBoolean(options.get("verbose", "false")))).onTerminate(this::finish);
		}

		public void handle(String line, Future<Boolean> job, Boolean verbose) {
			//if (verbose)
			//	System.out.println("  [fastlane] " + line);

			if (line.contains("Successfully finished the upload"))
				this.result = true;

			if (line.contains("Google Api Error")) {
				this.result = false;
				this.reason = line.split(":")[2];
			}
		}
	}

	public static class FastlaneApplePublisher extends FastlanePublisher {

		protected boolean error = false;

		public FastlaneApplePublisher(Engine engine) {
			super(engine);
		}

		public Future<Boolean> publish(App app, File artifact, String track, Map<String, String> options) throws Exception {
			return new Process(this.engine.fastlane().deliver(app, artifact, options.get("username", "default@apple.com"), options.get("password", "default"), list(
				"--skip_metadata",
				"--skip_screenshots"
			))).future(Boolean.class).onOutput((line, job) -> this.handle(line, job, Boolean.parseBoolean(options.get("verbose", "false")))).onTerminate(this::finish);
		}

		public void handle(String line, Future<Boolean> job, Boolean verbose) {
			if (verbose)
				System.out.println("  [fastlane] " + line);

			if (line.contains("Successfully uploaded package"))
				this.result = true;

			if (this.error == true && line.startsWith("\t"))
				this.reason = line.trim();

			if (line.contains("[!]")) {
				this.result = false;
				this.error = true;
				this.reason = line.split("\\[\\!\\]")[1].replace("Check out the error above", "");
			}
		}
	}
}
