package treehouse.tool.fastlane;

import static javax.util.List.list;
import static javax.util.Map.entry;
import static javax.util.Map.map;
import static javax.util.concurrent.Future.future;

import javax.io.File;
import javax.util.Map;
import javax.util.concurrent.Future;
import javax.util.concurrent.Future.*;

import treehouse.Engine;
import treehouse.app.App;

public abstract class FastlanePublisher {

	public static FastlanePublisher publisher(String platform, Engine engine) {
		if ("android".equals(platform))
			return new FastlanePlayPublisher(engine);
		
		return null;
	}
	
	public abstract Future<Boolean> publish(App app, File artifact, String track, Map<String, String> options) throws Exception;
	
	public static class FastlanePlayPublisher extends FastlanePublisher {
		
		protected Engine engine;
		
		protected boolean result = false;
		protected String reason = "";
		
		public FastlanePlayPublisher(Engine engine) {
			this.engine = engine;
		}
		
		public Future<Boolean> publish(App app, File artifact, String track) throws Exception {
			return this.publish(app, artifact, track, map(
				entry("verbose", "true")
			));
		}
		
		public Future<Boolean> publish(App app, File artifact, String track, Map<String, String> options) throws Exception {
			return future(this.engine.fastlane().supply(app, track, artifact, new File("resources/android/play.json"), list(
				"--skip_upload_images", 
				"--skip_upload_screenshots", 
				"--skip_upload_metadata"
			)), Boolean.class).onOutput((line, job) -> this.handle(line, job, Boolean.parseBoolean(options.get("verbose", "false")))).onTerminate(this::finish);
		}
		
		public void handle(String line, ProcessFuture<Boolean> job, Boolean verbose) {
			//if (verbose)
			//	System.out.println("  [fastlane] " + line);
			
			if (line.contains("Successfully finished the upload to Google Play"))
				this.result = true;
			
			if (line.contains("Google Api Error")) {
				this.result = false;
				this.reason = line.split(":")[2];
			}
		}
		
		public void finish(Integer code, ProcessFuture<Boolean> job) {
			if (this.result)
				job.complete(true);
			else
				job.completeExceptionally(new Exception(this.reason));
		}
	}
}
