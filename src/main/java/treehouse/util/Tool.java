package treehouse.util;

import static javax.util.List.list;

import javax.io.Streams;
import javax.lang.Runtime;

public abstract class Tool {
	
	abstract protected String name();
	
	protected String execute(String... parameters) throws Exception {
		return validate(Streams.read(Runtime.execute(command(parameters)).getInputStream()).trim());
	}
	
	protected String[] command(String... parameters) {
		return list(parameters).insert(0, name()).toArray(new String[0]);
	}
	
	protected String validate(String string) throws Exception {
		if (string.trim().equals(""))
			throw new Exception("Error executing " + name() + " command");
		
		return string;
	}
}
