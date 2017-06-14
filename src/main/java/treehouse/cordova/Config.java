package treehouse.cordova;

import javax.lang.Strings;
import javax.util.Properties;

public class Config {
	
	protected Properties properties = new Properties();
	
	protected static String TEMPLATE = 	"<?xml version='1.0' encoding='utf-8'?>\n" + 
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
	
	
	public Config(Properties properties) {
		this.properties = properties;
	}
	
	public Config put(Properties properties) {
		this.properties.putAll(properties);
		return this;
	}
	
	public Config set(Properties properties) {
		this.properties = properties;
		return this;
	}
	
	public String toXml() {
		String template = TEMPLATE.toString();
		for (String token : Strings.extract(TEMPLATE, "{", "}")) 
			template = template.replace("{" + token + "}", this.properties.getProperty(token, ""));
		
		return template;
	}
}
