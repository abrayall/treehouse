package treehouse;

import javax.util.Properties;

import treehouse.browser.Browser;
import treehouse.cordova.Cordova;
import treehouse.fastlane.Fastlane;

public interface Context {
	public String id();
	public String name();
	public Properties configuration();

	public Cordova cordova() throws Exception;
	public Fastlane fastlane() throws Exception;
	public Browser browser() throws Exception;
}
