package treehouse.tool;

import static javax.util.Map.*;
import static javax.util.List.*;

import javax.io.File;
import javax.io.Streams;
import javax.lang.Process;
import javax.util.Map;

public abstract class Tool {
	
	abstract protected String name();
	
	protected String execute(String... parameters) throws Exception {
		return execute(new File("."), parameters);
	}
	
	protected String execute(File directory, String... parameters) throws Exception {
		return execute(map(), directory, parameters);
	}
	
	protected String execute(Map<String, String> environment, File directory, String... parameters) throws Exception {
		return validate(Streams.read(builder(environment, directory, parameters).start().getInputStream()).trim());
	}
	
	protected ProcessBuilder process(File directory, String... parameters) throws Exception {
		return builder(map(), directory, parameters);
	}
	
	protected ProcessBuilder builder(Map<String, String> environment, File directory, String... parameters) throws Exception {
		return Process.builder(environment, directory.toFile(), command(parameters)).redirectErrorStream(true);
	}
	
	protected String[] command(String... parameters) {
		return list(parameters).insert(0, name()).toArray(new String[0]);
	}
	
	protected String validate(String string) throws Exception {
		return string;
	}
}
