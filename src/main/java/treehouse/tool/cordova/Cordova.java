package treehouse.tool.cordova;

import javax.lang.Process;
import javax.lang.Runtime;
import static javax.util.List.*;
import static javax.util.Map.*;

import java.io.FileOutputStream;

import javax.io.File;
import javax.io.Streams;
import javax.lang.Try;
import javax.util.List;
import javax.util.Map;

import treehouse.app.App;
import treehouse.tool.Tool;

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

	public Process build(App app, File directory, String platform, Map<String, String> options, Map<String, String> environment, List<String> extra) throws Exception {
		this.setup(app, directory);
		return Runtime.execute(builder(environment, directory, parameters(list("build", platform, "--" + options.get("type", "release"), "--device"), extra)));
	}
	
	public Process run(App app, File directory) throws Exception {
		this.build(app, directory, "browser", map(), map(), list()).waitFor();
		return Runtime.execute(Process.builder(directory.toFile(), new File(directory, "/platforms/browser/cordova/run").toString(), "browser", "--target=none", "--port=8015"));
	}
	
	protected Cordova patch(File directory) throws Exception {
		File run = new File(directory, "platforms/browser/cordova/run");
		run.write(run.read().replace("return cordovaServe", "return args.target == \"none\" ? null : cordovaServe"));
		return this;
	}
	
	protected String[] parameters(List<String> base, List<String> extra) {
		base.addAll(extra);
		return base.toArray(new String[0]);
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
