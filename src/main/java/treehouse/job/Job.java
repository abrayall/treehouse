package treehouse.job;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class Job<T> extends CompletableFuture<T> {
	public Job<T> onComplete(Consumer<T> handler) {
		this.thenAccept(handler);
		return this;
	}
	
	public Job<T> onError(Consumer<Throwable> handler) {
		this.exceptionally(exception -> {
			handler.accept(exception);
			return null;
		});
		
		return this;
	}
}


