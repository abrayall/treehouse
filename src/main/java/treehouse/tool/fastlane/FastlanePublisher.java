package treehouse.tool.fastlane;

import static javax.util.List.*;
import static javax.util.Map.*;

import javax.io.File;
import javax.util.Map;

import treehouse.Engine;
import treehouse.Publisher;
import treehouse.app.App;
import treehouse.job.Job;
import treehouse.job.ProcessJob;

public abstract class FastlanePublisher implements Publisher {

	public static Publisher publisher(String platform, Engine engine) {
		if ("android".equals(platform))
			return new FastlanePlayPublisher(engine);
		
		return null;
	}
	
	public static class FastlanePlayPublisher extends FastlanePublisher {
		
		protected Engine engine;
		
		protected boolean result = false;
		protected String reason = "";
		
		public FastlanePlayPublisher(Engine engine) {
			this.engine = engine;
		}
		
		public Job<Boolean> publish(App app, File artifact, String track) throws Exception {
			return this.publish(app, artifact, track, map(
				entry("verbose", "true")
			));
		}
		
		public Job<Boolean> publish(App app, File artifact, String track, Map<String, String> options) throws Exception {
			return new ProcessJob<Boolean>(this.engine.fastlane().supply(app, track, artifact, new File("resources/android/play.json"), list(
				"--skip_upload_images", 
				"--skip_upload_screenshots", 
				"--skip_upload_metadata"
			))).onOutput((line, job) -> this.handle(line, job, Boolean.parseBoolean(options.get("verbose", "false")))).onTerminate(this::finish);
		}
		
		public void handle(String line, ProcessJob<Boolean> job, Boolean verbose) {
			//if (verbose)
			//	System.out.println("  [fastlane] " + line);
			
			if (line.contains("Successfully finished the upload to Google Play"))
				this.result = true;
			
			if (line.contains("Google Api Error")) {
				this.result = false;
				this.reason = line.split(":")[2];
			}
		}
		
		public void finish(Integer code, ProcessJob<Boolean> job) {
			if (this.result)
				job.complete(true);
			else
				job.completeExceptionally(new Exception(this.reason));
		}
	}
}
