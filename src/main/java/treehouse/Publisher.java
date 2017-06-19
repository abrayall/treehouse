package treehouse;

import javax.io.File;
import javax.util.Map;

import treehouse.app.App;
import treehouse.job.Job;

public interface Publisher {
	public Job<Boolean> publish(App app, File artifact, String track, Map<String, String> options) throws Exception;
}
