package treehouse.tool.cordova;

import static javax.util.List.list;
import static javax.util.Map.entry;
import static javax.util.Map.map;

import javax.io.File;
import javax.io.Streams;
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
			File certificate = certificate(app);
			List<String> extra = list("--", "--keystore=" + certificate, "--storePassword=" + app.getName().toLowerCase(), "--password=" + app.getName().toLowerCase(), "--alias=" + app.getName().toLowerCase());
			return new ProcessJob<File>(this.engine.cordova().build(app, directory, "android", options, map(
				entry("ANDROID_HOME", this.engine.android().home().toString())
			), extra)).onTerminate(this::finish).onOutput((line, job) -> this.handle(line, job, Boolean.parseBoolean(options.get("verbose", "false"))));
		}
		
		public void handle(String line, ProcessJob<File> job, Boolean verbose) {
			if (verbose)
				System.out.println("  [cordova] " + line);
			
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
		
		protected File certificate(App app) throws Exception {
			File keystore = new File(new File(this.directory, "build/work/ssl"), app.getName().toLowerCase() + ".keystore");
			return keystore.exists() ? keystore : generate(app, keystore);
		}
		
		protected File generate(App app, File keystore) throws Exception {
			System.out.println("Generating key to sign the app...");
			String output = Streams.read(javax.lang.Runtime.process("keytool", 
				"-genkey", 
				"-alias", app.getName().toLowerCase(),
				"-keystore", keystore.mkdirs().toString(),
				"-keypass", app.getName().toLowerCase(),
				"-keypass", app.getName().toLowerCase(),
				"-keyalg", "RSA",
				"-keysize", "2048",
				"-validity", "20000",
				"-storepass", app.getName().toLowerCase(),
				"-dname", "CN=" + app.getName().toLowerCase() + ", OU=, O=, L=, S=, C=US"
			).redirectErrorStream(true).start().getInputStream());
			if (keystore.exists() == false) throw new Exception("Unable to generate keystore: " + output);
			return keystore;
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
