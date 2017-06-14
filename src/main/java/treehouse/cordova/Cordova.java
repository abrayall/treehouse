package treehouse.cordova;

import java.io.FileOutputStream;

import javax.io.File;
import javax.io.Streams;
import javax.lang.Try;
import javax.util.Properties;

import treehouse.Context;
import treehouse.util.Tool;

public class Cordova extends Tool {

	protected File directory;
	protected Context context;
	
	protected String name() {
		return "cordova";
	}
	
	public Cordova(File directory, Context context) {
		this.directory = directory;
		this.context = context;
	}
	
	public String getVersion() throws Exception {
		return Try.attempt(() -> execute("--version"), "0.0.0");
	}
	
	public Cordova initialize() throws Exception {
		// install node.js
		// install cordova
		this.create();
		this.patch();
		return this;
	}
	
	public Cordova create() throws Exception {
		if (this.directory.exists() == true)
			return this;

		System.out.println("Creating new app project '" + this.context.name() + "' [" + this.context.id() + ", " + this.directory + "]...");
		this.directory.mkdirs();
		this.execute("create", this.directory.path(), this.context.id(), this.context.name());
		
		System.out.println("Adding support for browser...");
		this.execute(this.directory, "platform", "add", "browser");
		
		//System.out.println("Adding support for android...");
		//this.execute(this.directory, "platform", "add", "android");
		
		//System.out.println("Adding support for ios...");
		//this.execute(this.directory, "platform", "add", "ios");

		return this;
	}

	public Cordova setup() throws Exception {
		if (this.directory.exists() == false)
			this.create();
		
		this.write(new File(this.directory, "config.xml"), this.config(this.context.configuration()));
		return this;
	}

	public Cordova build() throws Exception {
		this.setup();
		System.out.println(this.execute(this.directory, "build"));
		return this;
	}
	
	public Process run() throws Exception {
		this.setup();
		return javax.lang.Runtime.process(this.directory.toFile(), "/tmp/treehouse/work/cordova/platforms/browser/cordova/run", "browser", "--port=8015").start();
	}

	
	protected void patch() throws Exception {
		File run = new File(directory, "platforms/browser/cordova/run");
		run.write(run.read().replace("args.target || \"chrome\"", "args.target || \"none\"").replace("return cordovaServe", "return args.target == \"none\" ? null : cordovaServe"));
	}

	public File www() {
		return new File(this.directory, "www");
	}
	
	protected String config(Properties properties) {
		return new Config(properties).toXml();
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
