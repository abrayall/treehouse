package treehouse.tool.cordova;

import static javax.util.List.list;
import static javax.util.Map.entry;
import static javax.util.Map.map;

import javax.io.File;
import javax.util.List;
import javax.util.Map;

import treehouse.Builder;
import treehouse.Engine;
import treehouse.app.App;
import treehouse.job.Job;
import treehouse.job.ProcessJob;

public abstract class CordovaBuilder implements Builder {
	
	protected Engine engine;
	protected File directory;
	protected File artifact;
	
	protected String state = "unknown";

	public CordovaBuilder(Engine engine, File directory) {
		this.engine = engine;
		this.directory = directory;
	}
	
	public static class CordovaAndroidBuilder extends CordovaBuilder {

		public CordovaAndroidBuilder(Engine engine, File directory) {
			super(engine, directory);
		}
		
		public Job<File> build(App app, Map<String, String> options) throws Exception {
			return new ProcessJob<File>(this.engine.cordova().build(app, directory, "android", map(
				entry("ANDROID_HOME", this.engine.android().home().toString())
			))).onTerminate(this::finish).onOutput(this::handle);
		}
		
		public void handle(String line, ProcessJob<File> job) {
			if (line.contains("BUILD SUCCESSFUL"))
				this.state = "success";
			
			if (line.contains(".apk"))
				this.artifact = new File(line.trim());
		}
		
		public void finish(Integer code, ProcessJob<File> job) {
			if ("success".equalsIgnoreCase(this.state) && this.artifact != null && this.artifact.exists())
				job.complete(this.artifact);
			else
				job.completeExceptionally(new Exception("Error building"));
		}
	}
	
	public static List<String> platforms() {
		return list("android");
	}
	
	public static CordovaBuilder builder(String platform, Engine engine, File directory) {
		if (platform.equals("android"))
			return new CordovaAndroidBuilder(engine, directory);
		
		return null;
	}
}
