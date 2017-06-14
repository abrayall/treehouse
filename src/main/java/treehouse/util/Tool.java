package treehouse.util;

import static javax.util.List.list;

import javax.io.File;
import javax.io.Streams;
import javax.lang.Runtime;

public abstract class Tool {
	
	abstract protected String name();
	
	protected String execute(String... parameters) throws Exception {
		return execute(new File("."), parameters);
	}
	
	protected String execute(File directory, String... parameters) throws Exception {
		return validate(Streams.read(process(directory, parameters).start().getInputStream()).trim());
	}
	
	protected ProcessBuilder process(File directory, String... parameters) throws Exception {
		return Runtime.process(directory.toFile(), command(parameters)).redirectErrorStream(true);
	}
	
	protected String[] command(String... parameters) {
		return list(parameters).insert(0, name()).toArray(new String[0]);
	}
	
	protected String validate(String string) throws Exception {
		return string;
	}
}
