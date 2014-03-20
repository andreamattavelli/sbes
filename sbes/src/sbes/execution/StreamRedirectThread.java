package sbes.execution;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;

/**
 * StreamRedirectThread is a thread which copies its input to
 * its output and terminates when it completes.
 */
public class StreamRedirectThread extends Thread {

	private final Reader in;
	private final Writer out;

	private static final int BUFFER_SIZE = 2048;

	/**
	 * Set up for copy.
	 * @param name  Name of the thread
	 * @param in    Stream to copy from
	 * @param out   Stream to copy to
	 */
	public StreamRedirectThread(final String name, final InputStream in, final OutputStream out) {
		super(name);
		this.in = new InputStreamReader(in);
		this.out = new OutputStreamWriter(out);
		setPriority(Thread.MAX_PRIORITY-1);
	}

	/**
	 * Copy.
	 */
	@Override
	public void run() {
		try {
			char[] cbuf = new char[BUFFER_SIZE];
			int count;
			while ((count = in.read(cbuf, 0, BUFFER_SIZE)) >= 0) {
				out.write(cbuf, 0, count);
			}
			out.flush();
		} catch(IOException exc) {
			System.err.println("Child I/O Transfer - " + exc);
		}
	}
}
