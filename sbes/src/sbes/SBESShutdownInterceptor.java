package sbes;

import sbes.logging.Logger;
import sbes.result.EquivalenceRepository;
import sun.misc.Signal;
import sun.misc.SignalHandler;

public class SBESShutdownInterceptor implements SignalHandler {

	private static final Logger logger = new Logger(SBESShutdownInterceptor.class);
	
	private static boolean interrupted = false;
	
	@Override
	public void handle(final Signal signal) {
		System.out.println();
		logger.info("User requested search stop!");
		
		if (interrupted) {
			logger.info("Terminating the process");
			System.exit(-1);
		}
		
		interrupted = true;
		
		EquivalenceRepository.getInstance().printEquivalences();
		
		System.exit(-1);
	}

	public static boolean isInterrupted() {
		return interrupted;
	}
	
}
