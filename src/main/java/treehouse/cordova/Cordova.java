package treehouse.cordova;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.InputStreamReader;

import javax.io.File;
import javax.io.Streams;
import javax.lang.Try;
import javax.util.Map;

import treehouse.App;
import treehouse.util.Tool;

public class Cordova extends Tool {

	protected String name() {
		return "cordova";
	}
	
	public String getVersion() throws Exception {
		return Try.attempt(() -> execute("--version"), "0.0.0");
	}
	
	public Cordova install() throws Exception {
		// install node.js
		// install cordova
		return this;
	}
	
	public Cordova create(App app, File directory) throws Exception {
		//System.out.println("Creating new app project '" + app.getName() + "' [" + app.getId() + ", " + directory + "]...");
		//this.execute("create", directory.mkdirs().path(), app.getId(), app.getName());
		
		System.out.println("Adding support for browser...");
		this.execute(directory, "platform", "add", "browser");
		
		//System.out.println("Adding support for android...");
		//this.execute(this.directory, "platform", "add", "android");
		
		//System.out.println("Adding support for ios...");
		//this.execute(this.directory, "platform", "add", "ios");

		return this.patch(directory);
	}

	public Cordova setup(App app, File directory) throws Exception {
		if (directory.exists() == false || new File(directory, "platforms/browser").exists() == false) {
			System.out.println("Adding support for browser...");
			this.execute(directory, "platform", "add", "browser");
		}
		
		//this.write(new File(directory, "config.xml"), app.toConfigXml());
		return this.patch(directory);
	}

	public Cordova build(App app, File directory, String platform, Map<String, String> environment) throws Exception {
		this.setup(app, directory);
		new BufferedReader(new InputStreamReader(this.process(environment, directory, "build").start().getInputStream())).lines().forEach(line -> {
			System.out.println(line);
		});
		
		return this;
	}
	
	public Process run(App app, File directory) throws Exception {
		this.setup(app, directory);
		return javax.lang.Runtime.process(directory.toFile(), new File(directory, "/platforms/browser/cordova/run").toString(), "browser", "--port=8015").start();
	}
	
	protected Cordova patch(File directory) throws Exception {
		File run = new File(directory, "platforms/browser/cordova/run");
		run.write(run.read().replace("args.target || \"chrome\"", "args.target || \"none\"").replace("return cordovaServe", "return args.target == \"none\" ? null : cordovaServe"));
		return this;
	}
	
	protected void write(File file, String contents) throws Exception {
		Streams.write(contents, new FileOutputStream(file.toFile()));
	}
	
	protected String validate(String output) throws Exception {
		if (output.contains("Error:"))
			throw new Exception (output.replace("Error:", ""));
		
		return output;
	}
}
