package treehouse.job;

import static javax.lang.Threads.thread;
import static javax.lang.Try.attempt;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.function.BiConsumer;

public class ProcessJob<T> extends Job<T> {
	
	protected Process process;
	
	public ProcessJob(Process process) {
		this.process = process;
	}
	
	public ProcessJob<T> onOutput(BiConsumer<String, ProcessJob<T>> handler) {
		new BufferedReader(new InputStreamReader(process.getInputStream())).lines().forEach(line -> {
			handler.accept(line, this);
		});
		
		return this;
	}
	
	public void cancel() {
		this.process.destroyForcibly();
	}
	
	public ProcessJob<T> onTerminate(BiConsumer<Integer, ProcessJob<T>> handler) {
		ProcessJob<T> job = this;
		thread(() -> {
			attempt(() -> process.waitFor());
			handler.accept(process.exitValue(), job);
		}).start();
		return this;
	}
}
