package treehouse.app;

import static javax.util.Properties.properties;
import static javax.util.Properties.property;

import javax.io.File;
import javax.lang.Strings;
import javax.util.Properties;

public class App {

	private String id = "";
	private String name = "";
	private String description = "";
	
	//private List<String> platforms = list("browser", "ios", "android");
	//private List<String> plugins = list();
	
	public String getId() {
		return this.id;
	}
	
	public App setId(String id) {
		this.id = id;
		return this;
	}
	
	public String getName() {
		return this.name;
	}
	
	public App setName(String name) {
		this.name = name;
		return this;
	}
	
	public String getDescription() {
		return this.description;
	}
	
	public App setDescription(String description) {
		this.description = description;
		return this;
	}
	
	protected App set(Properties properties) {
		this.setId(properties.getProperty("id", this.getId()));
		this.setName(properties.getProperty("name", this.getName()));
		this.setDescription(properties.getProperty("description", this.getDescription()));
		return this;
	}
	
	public Properties get() {
		return properties(
			property("id", this.id),
			property("name", this.name),
			property("description", this.description)
		);
	}
	
	public String toConfigXml() {
		Properties properties = this.get();
		String template = TEMPLATE.toString();
		for (String token : Strings.extract(TEMPLATE, "{", "}")) 
			template = template.replace("{" + token + "}", properties.getProperty(token, ""));
		
		return template;
	}
	
	public static App load(File file) throws Exception {
		return load(properties(file.toFile()));
	}
	
	public static App load(Properties properties) {
		return new App().set(properties);
	}
	
	protected static String TEMPLATE = 	
		"<?xml version='1.0' encoding='utf-8'?>\n" + 
			"<widget id=\"{id}\" version=\"{version}\" xmlns=\"http://www.w3.org/ns/widgets\" xmlns:cdv=\"http://cordova.apache.org/ns/1.0\">\n" + 
			"\t<name>{name}</name>\n" + 
			"\t<description>{description}</description>\n" + 
			"\t<author email=\"{email}\" href=\"{url}\">{author}</author>\n" + 
			"\t<content src=\"index.html\" />\n" + 
			"\t<plugin name=\"cordova-plugin-whitelist\" spec=\"1\" />\n" + 
			"\t<access origin=\"*\" />\n" + 
		    "\t<allow-intent href=\"http://*/*\" />\n" +
		    "\t<allow-intent href=\"https://*/*\" />\n" + 
		    "\t<allow-intent href=\"tel:*\" />\n" + 
		    "\t<allow-intent href=\"sms:*\" />\n" + 
		    "\t<allow-intent href=\"mailto:*\" />\n" + 
		    "\t<allow-intent href=\"geo:*\" />\n" + 
		    "\t<platform name=\"android\">\n" + 
		        "\t\t<allow-intent href=\"market:*\" />\n" + 
		    "\t</platform>\n" + 
		    "\t<platform name=\"ios\">\n" + 
		        "\t\t<allow-intent href=\"itms:*\" />\n" + 
		        "\t\t<allow-intent href=\"itms-apps:*\" />\n" +
		    "\t</platform>\n" + 
		"</widget>";

}
