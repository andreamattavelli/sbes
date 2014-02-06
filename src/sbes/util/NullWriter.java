package sbes.util;

import java.io.Writer;

public class NullWriter extends Writer {
	
	public static final NullWriter NULL_WRITER = new NullWriter();

	public NullWriter() {
	}

	@Override
	public Writer append(final char c) {
		//to /dev/null
		return this;
	}

	@Override
	public Writer append(final CharSequence csq, final int start, final int end) {
		//to /dev/null
		return this;
	}

	@Override
	public Writer append(final CharSequence csq) {
		//to /dev/null
		return this;
	}

	@Override
	public void write(final int idx) {
		//to /dev/null
	}

	@Override
	public void write(final char[] chr) {
		//to /dev/null
	}

	@Override
	public void write(final char[] chr, final int st, final int end) {
		//to /dev/null
	}

	@Override
	public void write(final String str) {
		//to /dev/null
	}

	@Override
	public void write(final String str, final int st, final int end) {
		//to /dev/null
	}

	@Override
	public void flush() {
		//to /dev/null
	}

	@Override
	public void close() {
		//to /dev/null
	}

}