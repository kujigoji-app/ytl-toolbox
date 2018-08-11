package gui.delaysetup;

import lombok.Getter;

import java.util.Collection;
import java.util.Collections;

@Getter
public class DelaySetup {
	private final DelaySetupHandler handler;
	private final Collection<String> dependencies;

	public DelaySetup(DelaySetupHandler handler, Collection<String> dependencies) {
		this.handler = handler;
		this.dependencies = dependencies;
	}

	public DelaySetup(DelaySetupHandler handler) {
		this.handler = handler;
		this.dependencies = Collections.emptyList();
	}

}
