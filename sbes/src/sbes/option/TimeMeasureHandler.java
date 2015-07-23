package sbes.option;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.OptionDef;
import org.kohsuke.args4j.spi.OneArgumentOptionHandler;
import org.kohsuke.args4j.spi.Setter;

import sbes.logging.Level;
import sbes.logging.Logger;
import sbes.stoppingcondition.TimeMeasure;

public class TimeMeasureHandler extends OneArgumentOptionHandler<TimeMeasure> {

	public TimeMeasureHandler(final CmdLineParser parser, final OptionDef option, final Setter<? super TimeMeasure> setter) {
		super(parser, option, setter);
	}

	@Override
	protected TimeMeasure parse(String argument) throws NumberFormatException, CmdLineException {
		argument = argument.toLowerCase();

		if (argument.equals("cputime")) {
			return TimeMeasure.CPUTIME;
		} else if (argument.equals("globaltime")) {
			Logger.setLevel(Level.INFO);
			return TimeMeasure.GLOBALTIME;
		}

		throw new CmdLineException(this.owner, null, "Wrong parameter for the option " + this.option.toString() + ".");
	}

}
