package treehouse.job;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class Job<T> extends CompletableFuture<T> {
	public Job<T> onComplete(Consumer<T> handler) {
		this.thenAccept(handler);
		return this;
	}
}


