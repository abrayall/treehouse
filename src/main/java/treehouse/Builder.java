package treehouse;

import javax.io.File;
import javax.util.Map;

import treehouse.app.App;
import treehouse.job.Job;

public interface Builder {
	public Job<File> build(App app, Map<String, String> options) throws Exception;
}
