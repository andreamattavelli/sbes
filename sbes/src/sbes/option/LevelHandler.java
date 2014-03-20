package sbes.option;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.OptionDef;
import org.kohsuke.args4j.spi.OneArgumentOptionHandler;
import org.kohsuke.args4j.spi.Setter;

import sbes.logging.Level;
import sbes.logging.Logger;

public class LevelHandler extends OneArgumentOptionHandler<Level> {

	public LevelHandler(final CmdLineParser parser, final OptionDef option, final Setter<? super Level> setter) {
		super(parser, option, setter);
	}

	@Override
	protected Level parse(String argument) throws NumberFormatException, CmdLineException {
		argument = argument.toLowerCase();

		if (argument.equals("debug")) {
			Logger.setLevel(Level.DEBUG);
			return Level.DEBUG;
		} else if (argument.equals("info")) {
			Logger.setLevel(Level.INFO);
			return Level.INFO;
		} else if (argument.equals("warn")) {
			Logger.setLevel(Level.WARN);
			return Level.WARN;
		} else if (argument.equals("error")) {
			Logger.setLevel(Level.ERROR);
			return Level.ERROR;
		} else if (argument.equals("fatal")) {
			Logger.setLevel(Level.FATAL);
			return Level.FATAL;
		}

		throw new CmdLineException(this.owner, "Wrong parameter for the option " + this.option.toString() + ".");
	}

}
