package gui.delaysetup;

import javafx.animation.AnimationTimer;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.Map.Entry;

@Slf4j
public class DelaySetupManager {
	private static final long INITIAL_DELAY = 0L;

	private static final Map<String, DelaySetup> tasks = new HashMap<>();

	private DelaySetupManager() {
	}

	public static void registerDelaySetup(String key, DelaySetup ds) {
		tasks.put(key, ds);
	}

	public static void registerDelaySetup(String key, DelaySetupHandler dsh) {//Collection<String> dependencies
		tasks.put(key, new DelaySetup(dsh));
	}

	public static void registerDelaySetup(String key, DelaySetupHandler dsh, Collection<String> dependencies) {
		tasks.put(key, new DelaySetup(dsh, dependencies));
	}

	private static void setupAll(Stage stage, long timeoutMilli) {

		//		long deadline = System.currentTimeMillis() + timeoutMilli;
		AnimationTimer at = new AnimationTimer() {
			private long start = 0;
			private long deadline = 0;
			private final List<String> remove = new ArrayList<>();

			@Override
			public void handle(long now) {
				if (this.deadline == 0) {
					// initialize
					this.start = now;
					this.deadline = now + timeoutMilli * 1_000_000 + INITIAL_DELAY;
				}
				if (now - this.start < INITIAL_DELAY) {
					// initial delay
					return;
				}
				if (tasks.isEmpty()) {
					// finish
					this.stop();
				}
				if (this.deadline < now) {
					// timeout
					log.error("Setup timeout");
					System.exit(-1);
				}
				this.remove.clear();
				//
				for (Entry<String, DelaySetup> e : tasks.entrySet()) {
					// 依存先がセットアップ済み(マップに存在しない）かどうかチェック
					if (e.getValue().getDependencies().stream().noneMatch(tasks::containsKey)) {
						log.debug(e.getKey() + " setup");
						e.getValue().getHandler().setup(stage);
						this.remove.add(e.getKey());
					}
				}
				this.remove.forEach(tasks::remove);
			}
		};
		at.start();

	}

	public static void setupAll(Stage stage) {
		setupAll(stage, 5000);
	}
}
