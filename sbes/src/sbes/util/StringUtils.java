/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package sbes.util;

import java.text.Normalizer;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * <p>Operations on {@link java.lang.String} that are
 * {@code null} safe.</p>
 *
 * <ul>
 *  <li><b>IsEmpty/IsBlank</b>
 *      - checks if a String contains text</li>
 *  <li><b>Trim/Strip</b>
 *      - removes leading and trailing whitespace</li>
 *  <li><b>Equals</b>
 *      - compares two strings null-safe</li>
 *  <li><b>startsWith</b>
 *      - check if a String starts with a prefix null-safe</li>
 *  <li><b>endsWith</b>
 *      - check if a String ends with a suffix null-safe</li>
 *  <li><b>IndexOf/LastIndexOf/Contains</b>
 *      - null-safe index-of checks
 *  <li><b>IndexOfAny/LastIndexOfAny/IndexOfAnyBut/LastIndexOfAnyBut</b>
 *      - index-of any of a set of Strings</li>
 *  <li><b>ContainsOnly/ContainsNone/ContainsAny</b>
 *      - does String contains only/none/any of these characters</li>
 *  <li><b>Substring/Left/Right/Mid</b>
 *      - null-safe substring extractions</li>
 *  <li><b>SubstringBefore/SubstringAfter/SubstringBetween</b>
 *      - substring extraction relative to other strings</li>
 *  <li><b>Split/Join</b>
 *      - splits a String into an array of substrings and vice versa</li>
 *  <li><b>Remove/Delete</b>
 *      - removes part of a String</li>
 *  <li><b>Replace/Overlay</b>
 *      - Searches a String and replaces one String with another</li>
 *  <li><b>Chomp/Chop</b>
 *      - removes the last part of a String</li>
 *  <li><b>AppendIfMissing</b>
 *      - appends a suffix to the end of the String if not present</li>
 *  <li><b>PrependIfMissing</b>
 *      - prepends a prefix to the start of the String if not present</li>
 *  <li><b>LeftPad/RightPad/Center/Repeat</b>
 *      - pads a String</li>
 *  <li><b>UpperCase/LowerCase/SwapCase/Capitalize/Uncapitalize</b>
 *      - changes the case of a String</li>
 *  <li><b>CountMatches</b>
 *      - counts the number of occurrences of one String in another</li>
 *  <li><b>IsAlpha/IsNumeric/IsWhitespace/IsAsciiPrintable</b>
 *      - checks the characters in a String</li>
 *  <li><b>DefaultString</b>
 *      - protects against a null input String</li>
 *  <li><b>Reverse/ReverseDelimited</b>
 *      - reverses a String</li>
 *  <li><b>Abbreviate</b>
 *      - abbreviates a string using ellipsis</li>
 *  <li><b>Difference</b>
 *      - compares Strings and reports on their differences</li>
 *  <li><b>LevenshteinDistance</b>
 *      - the number of changes needed to change one String into another</li>
 * </ul>
 *
 * <p>The {@code StringUtils} class defines certain words related to
 * String handling.</p>
 *
 * <ul>
 *  <li>null - {@code null}</li>
 *  <li>empty - a zero-length string ({@code ""})</li>
 *  <li>space - the space character ({@code ' '}, char 32)</li>
 *  <li>whitespace - the characters defined by {@link Character#isWhitespace(char)}</li>
 *  <li>trim - the characters &lt;= 32 as in {@link String#trim()}</li>
 * </ul>
 *
 * <p>{@code StringUtils} handles {@code null} input Strings quietly.
 * That is to say that a {@code null} input will return {@code null}.
 * Where a {@code boolean} or {@code int} is being returned
 * details vary by method.</p>
 *
 * <p>A side effect of the {@code null} handling is that a
 * {@code NullPointerException} should be considered a bug in
 * {@code StringUtils}.</p>
 *
 * <p>Methods in this class give sample code to explain their operation.
 * The symbol {@code *} is used to indicate any input including {@code null}.</p>
 *
 * <p>#ThreadSafe#</p>
 * @see java.lang.String
 * @since 1.0
 * @version $Id$
 */
//@Immutable
public class StringUtils {

	/**
	 * A String for a space character.
	 *
	 * @since 3.2
	 */
	public static final String SPACE = " ";

	/**
	 * The empty String {@code ""}.
	 * @since 2.0
	 */
	public static final String EMPTY = "";

	/**
	 * A String for linefeed LF ("\n").
	 *
	 * @see <a href="http://docs.oracle.com/javase/specs/jls/se7/html/jls-3.html#jls-3.10.6">JLF: Escape Sequences
	 *      for Character and String Literals</a>
	 * @since 3.2
	 */
	public static final String LF = "\n";

	/**
	 * A String for carriage return CR ("\r").
	 *
	 * @see <a href="http://docs.oracle.com/javase/specs/jls/se7/html/jls-3.html#jls-3.10.6">JLF: Escape Sequences
	 *      for Character and String Literals</a>
	 * @since 3.2
	 */
	public static final String CR = "\r";

	/**
	 * Represents a failed index search.
	 * @since 2.1
	 */
	public static final int INDEX_NOT_FOUND = -1;

	/**
	 * <p>The maximum size to which the padding constant(s) can expand.</p>
	 */
	private static final int PAD_LIMIT = 8192;

	/**
	 * <p>{@code StringUtils} instances should NOT be constructed in
	 * standard programming. Instead, the class should be used as
	 * {@code StringUtils.trim(" foo ");}.</p>
	 *
	 * <p>This constructor is public to permit tools that require a JavaBean
	 * instance to operate.</p>
	 */
	public StringUtils() {
		super();
	}

	// Empty checks
	//-----------------------------------------------------------------------
	/**
	 * <p>Checks if a CharSequence is empty ("") or null.</p>
	 *
	 * <pre>
	 * StringUtils.isEmpty(null)      = true
	 * StringUtils.isEmpty("")        = true
	 * StringUtils.isEmpty(" ")       = false
	 * StringUtils.isEmpty("bob")     = false
	 * StringUtils.isEmpty("  bob  ") = false
	 * </pre>
	 *
	 * <p>NOTE: This method changed in Lang version 2.0.
	 * It no longer trims the CharSequence.
	 * That functionality is available in isBlank().</p>
	 *
	 * @param cs  the CharSequence to check, may be null
	 * @return {@code true} if the CharSequence is empty or null
	 * @since 3.0 Changed signature from isEmpty(String) to isEmpty(CharSequence)
	 */
	public static boolean isEmpty(final CharSequence cs) {
		return cs == null || cs.length() == 0;
	}

	/**
	 * <p>Checks if a CharSequence is not empty ("") and not null.</p>
	 *
	 * <pre>
	 * StringUtils.isNotEmpty(null)      = false
	 * StringUtils.isNotEmpty("")        = false
	 * StringUtils.isNotEmpty(" ")       = true
	 * StringUtils.isNotEmpty("bob")     = true
	 * StringUtils.isNotEmpty("  bob  ") = true
	 * </pre>
	 *
	 * @param cs  the CharSequence to check, may be null
	 * @return {@code true} if the CharSequence is not empty and not null
	 * @since 3.0 Changed signature from isNotEmpty(String) to isNotEmpty(CharSequence)
	 */
	public static boolean isNotEmpty(final CharSequence cs) {
		return !isEmpty(cs);
	}

	/**
	 * <p>Checks if a CharSequence is whitespace, empty ("") or null.</p>
	 *
	 * <pre>
	 * StringUtils.isBlank(null)      = true
	 * StringUtils.isBlank("")        = true
	 * StringUtils.isBlank(" ")       = true
	 * StringUtils.isBlank("bob")     = false
	 * StringUtils.isBlank("  bob  ") = false
	 * </pre>
	 *
	 * @param cs  the CharSequence to check, may be null
	 * @return {@code true} if the CharSequence is null, empty or whitespace
	 * @since 2.0
	 * @since 3.0 Changed signature from isBlank(String) to isBlank(CharSequence)
	 */
	public static boolean isBlank(final CharSequence cs) {
		int strLen;
		if (cs == null || (strLen = cs.length()) == 0) {
			return true;
		}
		for (int i = 0; i < strLen; i++) {
			if (Character.isWhitespace(cs.charAt(i)) == false) {
				return false;
			}
		}
		return true;
	}

	/**
	 * <p>Checks if a CharSequence is not empty (""), not null and not whitespace only.</p>
	 *
	 * <pre>
	 * StringUtils.isNotBlank(null)      = false
	 * StringUtils.isNotBlank("")        = false
	 * StringUtils.isNotBlank(" ")       = false
	 * StringUtils.isNotBlank("bob")     = true
	 * StringUtils.isNotBlank("  bob  ") = true
	 * </pre>
	 *
	 * @param cs  the CharSequence to check, may be null
	 * @return {@code true} if the CharSequence is
	 *  not empty and not null and not whitespace
	 * @since 2.0
	 * @since 3.0 Changed signature from isNotBlank(String) to isNotBlank(CharSequence)
	 */
	public static boolean isNotBlank(final CharSequence cs) {
		return !isBlank(cs);
	}

	// Trim
	//-----------------------------------------------------------------------
	/**
	 * <p>Removes control characters (char &lt;= 32) from both
	 * ends of this String, handling {@code null} by returning
	 * {@code null}.</p>
	 *
	 * <p>The String is trimmed using {@link String#trim()}.
	 * Trim removes start and end characters &lt;= 32.
	 * To strip whitespace use {@link #strip(String)}.</p>
	 *
	 * <p>To trim your choice of characters, use the
	 * {@link #strip(String, String)} methods.</p>
	 *
	 * <pre>
	 * StringUtils.trim(null)          = null
	 * StringUtils.trim("")            = ""
	 * StringUtils.trim("     ")       = ""
	 * StringUtils.trim("abc")         = "abc"
	 * StringUtils.trim("    abc    ") = "abc"
	 * </pre>
	 *
	 * @param str  the String to be trimmed, may be null
	 * @return the trimmed string, {@code null} if null String input
	 */
	public static String trim(final String str) {
		return str == null ? null : str.trim();
	}

	/**
	 * <p>Removes control characters (char &lt;= 32) from both
	 * ends of this String returning {@code null} if the String is
	 * empty ("") after the trim or if it is {@code null}.
	 *
	 * <p>The String is trimmed using {@link String#trim()}.
	 * Trim removes start and end characters &lt;= 32.
	 * To strip whitespace use {@link #stripToNull(String)}.</p>
	 *
	 * <pre>
	 * StringUtils.trimToNull(null)          = null
	 * StringUtils.trimToNull("")            = null
	 * StringUtils.trimToNull("     ")       = null
	 * StringUtils.trimToNull("abc")         = "abc"
	 * StringUtils.trimToNull("    abc    ") = "abc"
	 * </pre>
	 *
	 * @param str  the String to be trimmed, may be null
	 * @return the trimmed String,
	 *  {@code null} if only chars &lt;= 32, empty or null String input
	 * @since 2.0
	 */
	public static String trimToNull(final String str) {
		final String ts = trim(str);
		return isEmpty(ts) ? null : ts;
	}

	/**
	 * <p>Removes control characters (char &lt;= 32) from both
	 * ends of this String returning an empty String ("") if the String
	 * is empty ("") after the trim or if it is {@code null}.
	 *
	 * <p>The String is trimmed using {@link String#trim()}.
	 * Trim removes start and end characters &lt;= 32.
	 * To strip whitespace use {@link #stripToEmpty(String)}.</p>
	 *
	 * <pre>
	 * StringUtils.trimToEmpty(null)          = ""
	 * StringUtils.trimToEmpty("")            = ""
	 * StringUtils.trimToEmpty("     ")       = ""
	 * StringUtils.trimToEmpty("abc")         = "abc"
	 * StringUtils.trimToEmpty("    abc    ") = "abc"
	 * </pre>
	 *
	 * @param str  the String to be trimmed, may be null
	 * @return the trimmed String, or an empty String if {@code null} input
	 * @since 2.0
	 */
	public static String trimToEmpty(final String str) {
		return str == null ? EMPTY : str.trim();
	}

	// Stripping
	//-----------------------------------------------------------------------
	/**
	 * <p>Strips whitespace from the start and end of a String.</p>
	 *
	 * <p>This is similar to {@link #trim(String)} but removes whitespace.
	 * Whitespace is defined by {@link Character#isWhitespace(char)}.</p>
	 *
	 * <p>A {@code null} input String returns {@code null}.</p>
	 *
	 * <pre>
	 * StringUtils.strip(null)     = null
	 * StringUtils.strip("")       = ""
	 * StringUtils.strip("   ")    = ""
	 * StringUtils.strip("abc")    = "abc"
	 * StringUtils.strip("  abc")  = "abc"
	 * StringUtils.strip("abc  ")  = "abc"
	 * StringUtils.strip(" abc ")  = "abc"
	 * StringUtils.strip(" ab c ") = "ab c"
	 * </pre>
	 *
	 * @param str  the String to remove whitespace from, may be null
	 * @return the stripped String, {@code null} if null String input
	 */
	public static String strip(final String str) {
		return strip(str, null);
	}

	/**
	 * <p>Strips whitespace from the start and end of a String  returning
	 * {@code null} if the String is empty ("") after the strip.</p>
	 *
	 * <p>This is similar to {@link #trimToNull(String)} but removes whitespace.
	 * Whitespace is defined by {@link Character#isWhitespace(char)}.</p>
	 *
	 * <pre>
	 * StringUtils.stripToNull(null)     = null
	 * StringUtils.stripToNull("")       = null
	 * StringUtils.stripToNull("   ")    = null
	 * StringUtils.stripToNull("abc")    = "abc"
	 * StringUtils.stripToNull("  abc")  = "abc"
	 * StringUtils.stripToNull("abc  ")  = "abc"
	 * StringUtils.stripToNull(" abc ")  = "abc"
	 * StringUtils.stripToNull(" ab c ") = "ab c"
	 * </pre>
	 *
	 * @param str  the String to be stripped, may be null
	 * @return the stripped String,
	 *  {@code null} if whitespace, empty or null String input
	 * @since 2.0
	 */
	public static String stripToNull(String str) {
		if (str == null) {
			return null;
		}
		str = strip(str, null);
		return str.isEmpty() ? null : str;
	}

	/**
	 * <p>Strips whitespace from the start and end of a String  returning
	 * an empty String if {@code null} input.</p>
	 *
	 * <p>This is similar to {@link #trimToEmpty(String)} but removes whitespace.
	 * Whitespace is defined by {@link Character#isWhitespace(char)}.</p>
	 *
	 * <pre>
	 * StringUtils.stripToEmpty(null)     = ""
	 * StringUtils.stripToEmpty("")       = ""
	 * StringUtils.stripToEmpty("   ")    = ""
	 * StringUtils.stripToEmpty("abc")    = "abc"
	 * StringUtils.stripToEmpty("  abc")  = "abc"
	 * StringUtils.stripToEmpty("abc  ")  = "abc"
	 * StringUtils.stripToEmpty(" abc ")  = "abc"
	 * StringUtils.stripToEmpty(" ab c ") = "ab c"
	 * </pre>
	 *
	 * @param str  the String to be stripped, may be null
	 * @return the trimmed String, or an empty String if {@code null} input
	 * @since 2.0
	 */
	public static String stripToEmpty(final String str) {
		return str == null ? EMPTY : strip(str, null);
	}

	/**
	 * <p>Strips any of a set of characters from the start and end of a String.
	 * This is similar to {@link String#trim()} but allows the characters
	 * to be stripped to be controlled.</p>
	 *
	 * <p>A {@code null} input String returns {@code null}.
	 * An empty string ("") input returns the empty string.</p>
	 *
	 * <p>If the stripChars String is {@code null}, whitespace is
	 * stripped as defined by {@link Character#isWhitespace(char)}.
	 * Alternatively use {@link #strip(String)}.</p>
	 *
	 * <pre>
	 * StringUtils.strip(null, *)          = null
	 * StringUtils.strip("", *)            = ""
	 * StringUtils.strip("abc", null)      = "abc"
	 * StringUtils.strip("  abc", null)    = "abc"
	 * StringUtils.strip("abc  ", null)    = "abc"
	 * StringUtils.strip(" abc ", null)    = "abc"
	 * StringUtils.strip("  abcyx", "xyz") = "  abc"
	 * </pre>
	 *
	 * @param str  the String to remove characters from, may be null
	 * @param stripChars  the characters to remove, null treated as whitespace
	 * @return the stripped String, {@code null} if null String input
	 */
	public static String strip(String str, final String stripChars) {
		if (isEmpty(str)) {
			return str;
		}
		str = stripStart(str, stripChars);
		return stripEnd(str, stripChars);
	}

	/**
	 * <p>Strips any of a set of characters from the start of a String.</p>
	 *
	 * <p>A {@code null} input String returns {@code null}.
	 * An empty string ("") input returns the empty string.</p>
	 *
	 * <p>If the stripChars String is {@code null}, whitespace is
	 * stripped as defined by {@link Character#isWhitespace(char)}.</p>
	 *
	 * <pre>
	 * StringUtils.stripStart(null, *)          = null
	 * StringUtils.stripStart("", *)            = ""
	 * StringUtils.stripStart("abc", "")        = "abc"
	 * StringUtils.stripStart("abc", null)      = "abc"
	 * StringUtils.stripStart("  abc", null)    = "abc"
	 * StringUtils.stripStart("abc  ", null)    = "abc  "
	 * StringUtils.stripStart(" abc ", null)    = "abc "
	 * StringUtils.stripStart("yxabc  ", "xyz") = "abc  "
	 * </pre>
	 *
	 * @param str  the String to remove characters from, may be null
	 * @param stripChars  the characters to remove, null treated as whitespace
	 * @return the stripped String, {@code null} if null String input
	 */
	public static String stripStart(final String str, final String stripChars) {
		int strLen;
		if (str == null || (strLen = str.length()) == 0) {
			return str;
		}
		int start = 0;
		if (stripChars == null) {
			while (start != strLen && Character.isWhitespace(str.charAt(start))) {
				start++;
			}
		} else if (stripChars.isEmpty()) {
			return str;
		} else {
			while (start != strLen && stripChars.indexOf(str.charAt(start)) != INDEX_NOT_FOUND) {
				start++;
			}
		}
		return str.substring(start);
	}

	/**
	 * <p>Strips any of a set of characters from the end of a String.</p>
	 *
	 * <p>A {@code null} input String returns {@code null}.
	 * An empty string ("") input returns the empty string.</p>
	 *
	 * <p>If the stripChars String is {@code null}, whitespace is
	 * stripped as defined by {@link Character#isWhitespace(char)}.</p>
	 *
	 * <pre>
	 * StringUtils.stripEnd(null, *)          = null
	 * StringUtils.stripEnd("", *)            = ""
	 * StringUtils.stripEnd("abc", "")        = "abc"
	 * StringUtils.stripEnd("abc", null)      = "abc"
	 * StringUtils.stripEnd("  abc", null)    = "  abc"
	 * StringUtils.stripEnd("abc  ", null)    = "abc"
	 * StringUtils.stripEnd(" abc ", null)    = " abc"
	 * StringUtils.stripEnd("  abcyx", "xyz") = "  abc"
	 * StringUtils.stripEnd("120.00", ".0")   = "12"
	 * </pre>
	 *
	 * @param str  the String to remove characters from, may be null
	 * @param stripChars  the set of characters to remove, null treated as whitespace
	 * @return the stripped String, {@code null} if null String input
	 */
	public static String stripEnd(final String str, final String stripChars) {
		int end;
		if (str == null || (end = str.length()) == 0) {
			return str;
		}

		if (stripChars == null) {
			while (end != 0 && Character.isWhitespace(str.charAt(end - 1))) {
				end--;
			}
		} else if (stripChars.isEmpty()) {
			return str;
		} else {
			while (end != 0 && stripChars.indexOf(str.charAt(end - 1)) != INDEX_NOT_FOUND) {
				end--;
			}
		}
		return str.substring(0, end);
	}

	// StripAll
	//-----------------------------------------------------------------------
	/**
	 * <p>Strips whitespace from the start and end of every String in an array.
	 * Whitespace is defined by {@link Character#isWhitespace(char)}.</p>
	 *
	 * <p>A new array is returned each time, except for length zero.
	 * A {@code null} array will return {@code null}.
	 * An empty array will return itself.
	 * A {@code null} array entry will be ignored.</p>
	 *
	 * <pre>
	 * StringUtils.stripAll(null)             = null
	 * StringUtils.stripAll([])               = []
	 * StringUtils.stripAll(["abc", "  abc"]) = ["abc", "abc"]
	 * StringUtils.stripAll(["abc  ", null])  = ["abc", null]
	 * </pre>
	 *
	 * @param strs  the array to remove whitespace from, may be null
	 * @return the stripped Strings, {@code null} if null array input
	 */
	public static String[] stripAll(final String... strs) {
		return stripAll(strs, null);
	}

	/**
	 * <p>Strips any of a set of characters from the start and end of every
	 * String in an array.</p>
	 * <p>Whitespace is defined by {@link Character#isWhitespace(char)}.</p>
	 *
	 * <p>A new array is returned each time, except for length zero.
	 * A {@code null} array will return {@code null}.
	 * An empty array will return itself.
	 * A {@code null} array entry will be ignored.
	 * A {@code null} stripChars will strip whitespace as defined by
	 * {@link Character#isWhitespace(char)}.</p>
	 *
	 * <pre>
	 * StringUtils.stripAll(null, *)                = null
	 * StringUtils.stripAll([], *)                  = []
	 * StringUtils.stripAll(["abc", "  abc"], null) = ["abc", "abc"]
	 * StringUtils.stripAll(["abc  ", null], null)  = ["abc", null]
	 * StringUtils.stripAll(["abc  ", null], "yz")  = ["abc  ", null]
	 * StringUtils.stripAll(["yabcz", null], "yz")  = ["abc", null]
	 * </pre>
	 *
	 * @param strs  the array to remove characters from, may be null
	 * @param stripChars  the characters to remove, null treated as whitespace
	 * @return the stripped Strings, {@code null} if null array input
	 */
	public static String[] stripAll(final String[] strs, final String stripChars) {
		int strsLen;
		if (strs == null || (strsLen = strs.length) == 0) {
			return strs;
		}
		final String[] newArr = new String[strsLen];
		for (int i = 0; i < strsLen; i++) {
			newArr[i] = strip(strs[i], stripChars);
		}
		return newArr;
	}

	/**
	 * <p>Removes diacritics (~= accents) from a string. The case will not be altered.</p>
	 * <p>For instance, '&agrave;' will be replaced by 'a'.</p>
	 * <p>Note that ligatures will be left as is.</p>
	 *
	 * <pre>
	 * StringUtils.stripAccents(null)                = null
	 * StringUtils.stripAccents("")                  = ""
	 * StringUtils.stripAccents("control")           = "control"
	 * StringUtils.stripAccents("&eacute;clair")     = "eclair"
	 * </pre>
	 *
	 * @param input String to be stripped
	 * @return input text with diacritics removed
	 *
	 * @since 3.0
	 */
	// See also Lucene's ASCIIFoldingFilter (Lucene 2.9) that replaces accented characters by their unaccented equivalent (and uncommitted bug fix: https://issues.apache.org/jira/browse/LUCENE-1343?focusedCommentId=12858907&page=com.atlassian.jira.plugin.system.issuetabpanels%3Acomment-tabpanel#action_12858907).
	public static String stripAccents(final String input) {
		if(input == null) {
			return null;
		}
		final Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");//$NON-NLS-1$
		final String decomposed = Normalizer.normalize(input, Normalizer.Form.NFD);
		// Note that this doesn't correctly remove ligatures...
		return pattern.matcher(decomposed).replaceAll("");//$NON-NLS-1$
	}

	// Substring
	//-----------------------------------------------------------------------
	/**
	 * <p>Gets a substring from the specified String avoiding exceptions.</p>
	 *
	 * <p>A negative start position can be used to start {@code n}
	 * characters from the end of the String.</p>
	 *
	 * <p>A {@code null} String will return {@code null}.
	 * An empty ("") String will return "".</p>
	 *
	 * <pre>
	 * StringUtils.substring(null, *)   = null
	 * StringUtils.substring("", *)     = ""
	 * StringUtils.substring("abc", 0)  = "abc"
	 * StringUtils.substring("abc", 2)  = "c"
	 * StringUtils.substring("abc", 4)  = ""
	 * StringUtils.substring("abc", -2) = "bc"
	 * StringUtils.substring("abc", -4) = "abc"
	 * </pre>
	 *
	 * @param str  the String to get the substring from, may be null
	 * @param start  the position to start from, negative means
	 *  count back from the end of the String by this many characters
	 * @return substring from start position, {@code null} if null String input
	 */
	public static String substring(final String str, int start) {
		if (str == null) {
			return null;
		}

		// handle negatives, which means last n characters
		if (start < 0) {
			start = str.length() + start; // remember start is negative
		}

		if (start < 0) {
			start = 0;
		}
		if (start > str.length()) {
			return EMPTY;
		}

		return str.substring(start);
	}

	/**
	 * <p>Gets a substring from the specified String avoiding exceptions.</p>
	 *
	 * <p>A negative start position can be used to start/end {@code n}
	 * characters from the end of the String.</p>
	 *
	 * <p>The returned substring starts with the character in the {@code start}
	 * position and ends before the {@code end} position. All position counting is
	 * zero-based -- i.e., to start at the beginning of the string use
	 * {@code start = 0}. Negative start and end positions can be used to
	 * specify offsets relative to the end of the String.</p>
	 *
	 * <p>If {@code start} is not strictly to the left of {@code end}, ""
	 * is returned.</p>
	 *
	 * <pre>
	 * StringUtils.substring(null, *, *)    = null
	 * StringUtils.substring("", * ,  *)    = "";
	 * StringUtils.substring("abc", 0, 2)   = "ab"
	 * StringUtils.substring("abc", 2, 0)   = ""
	 * StringUtils.substring("abc", 2, 4)   = "c"
	 * StringUtils.substring("abc", 4, 6)   = ""
	 * StringUtils.substring("abc", 2, 2)   = ""
	 * StringUtils.substring("abc", -2, -1) = "b"
	 * StringUtils.substring("abc", -4, 2)  = "ab"
	 * </pre>
	 *
	 * @param str  the String to get the substring from, may be null
	 * @param start  the position to start from, negative means
	 *  count back from the end of the String by this many characters
	 * @param end  the position to end at (exclusive), negative means
	 *  count back from the end of the String by this many characters
	 * @return substring from start position to end position,
	 *  {@code null} if null String input
	 */
	public static String substring(final String str, int start, int end) {
		if (str == null) {
			return null;
		}

		// handle negatives
		if (end < 0) {
			end = str.length() + end; // remember end is negative
		}
		if (start < 0) {
			start = str.length() + start; // remember start is negative
		}

		// check length next
		if (end > str.length()) {
			end = str.length();
		}

		// if start is greater than end, return ""
		if (start > end) {
			return EMPTY;
		}

		if (start < 0) {
			start = 0;
		}
		if (end < 0) {
			end = 0;
		}

		return str.substring(start, end);
	}

	// Left/Right/Mid
	//-----------------------------------------------------------------------
	/**
	 * <p>Gets the leftmost {@code len} characters of a String.</p>
	 *
	 * <p>If {@code len} characters are not available, or the
	 * String is {@code null}, the String will be returned without
	 * an exception. An empty String is returned if len is negative.</p>
	 *
	 * <pre>
	 * StringUtils.left(null, *)    = null
	 * StringUtils.left(*, -ve)     = ""
	 * StringUtils.left("", *)      = ""
	 * StringUtils.left("abc", 0)   = ""
	 * StringUtils.left("abc", 2)   = "ab"
	 * StringUtils.left("abc", 4)   = "abc"
	 * </pre>
	 *
	 * @param str  the String to get the leftmost characters from, may be null
	 * @param len  the length of the required String
	 * @return the leftmost characters, {@code null} if null String input
	 */
	public static String left(final String str, final int len) {
		if (str == null) {
			return null;
		}
		if (len < 0) {
			return EMPTY;
		}
		if (str.length() <= len) {
			return str;
		}
		return str.substring(0, len);
	}

	/**
	 * <p>Gets the rightmost {@code len} characters of a String.</p>
	 *
	 * <p>If {@code len} characters are not available, or the String
	 * is {@code null}, the String will be returned without an
	 * an exception. An empty String is returned if len is negative.</p>
	 *
	 * <pre>
	 * StringUtils.right(null, *)    = null
	 * StringUtils.right(*, -ve)     = ""
	 * StringUtils.right("", *)      = ""
	 * StringUtils.right("abc", 0)   = ""
	 * StringUtils.right("abc", 2)   = "bc"
	 * StringUtils.right("abc", 4)   = "abc"
	 * </pre>
	 *
	 * @param str  the String to get the rightmost characters from, may be null
	 * @param len  the length of the required String
	 * @return the rightmost characters, {@code null} if null String input
	 */
	public static String right(final String str, final int len) {
		if (str == null) {
			return null;
		}
		if (len < 0) {
			return EMPTY;
		}
		if (str.length() <= len) {
			return str;
		}
		return str.substring(str.length() - len);
	}

	/**
	 * <p>Gets {@code len} characters from the middle of a String.</p>
	 *
	 * <p>If {@code len} characters are not available, the remainder
	 * of the String will be returned without an exception. If the
	 * String is {@code null}, {@code null} will be returned.
	 * An empty String is returned if len is negative or exceeds the
	 * length of {@code str}.</p>
	 *
	 * <pre>
	 * StringUtils.mid(null, *, *)    = null
	 * StringUtils.mid(*, *, -ve)     = ""
	 * StringUtils.mid("", 0, *)      = ""
	 * StringUtils.mid("abc", 0, 2)   = "ab"
	 * StringUtils.mid("abc", 0, 4)   = "abc"
	 * StringUtils.mid("abc", 2, 4)   = "c"
	 * StringUtils.mid("abc", 4, 2)   = ""
	 * StringUtils.mid("abc", -2, 2)  = "ab"
	 * </pre>
	 *
	 * @param str  the String to get the characters from, may be null
	 * @param pos  the position to start from, negative treated as zero
	 * @param len  the length of the required String
	 * @return the middle characters, {@code null} if null String input
	 */
	public static String mid(final String str, int pos, final int len) {
		if (str == null) {
			return null;
		}
		if (len < 0 || pos > str.length()) {
			return EMPTY;
		}
		if (pos < 0) {
			pos = 0;
		}
		if (str.length() <= pos + len) {
			return str.substring(pos);
		}
		return str.substring(pos, pos + len);
	}

	// Nested extraction
	//-----------------------------------------------------------------------

	// Delete
	//-----------------------------------------------------------------------
	/**
	 * <p>Deletes all whitespaces from a String as defined by
	 * {@link Character#isWhitespace(char)}.</p>
	 *
	 * <pre>
	 * StringUtils.deleteWhitespace(null)         = null
	 * StringUtils.deleteWhitespace("")           = ""
	 * StringUtils.deleteWhitespace("abc")        = "abc"
	 * StringUtils.deleteWhitespace("   ab  c  ") = "abc"
	 * </pre>
	 *
	 * @param str  the String to delete whitespace from, may be null
	 * @return the String without whitespaces, {@code null} if null String input
	 */
	public static String deleteWhitespace(final String str) {
		if (isEmpty(str)) {
			return str;
		}
		final int sz = str.length();
		final char[] chs = new char[sz];
		int count = 0;
		for (int i = 0; i < sz; i++) {
			if (!Character.isWhitespace(str.charAt(i))) {
				chs[count++] = str.charAt(i);
			}
		}
		if (count == sz) {
			return str;
		}
		return new String(chs, 0, count);
	}

	// Remove
	//-----------------------------------------------------------------------
	/**
	 * <p>Removes a substring only if it is at the beginning of a source string,
	 * otherwise returns the source string.</p>
	 *
	 * <p>A {@code null} source string will return {@code null}.
	 * An empty ("") source string will return the empty string.
	 * A {@code null} search string will return the source string.</p>
	 *
	 * <pre>
	 * StringUtils.removeStart(null, *)      = null
	 * StringUtils.removeStart("", *)        = ""
	 * StringUtils.removeStart(*, null)      = *
	 * StringUtils.removeStart("www.domain.com", "www.")   = "domain.com"
	 * StringUtils.removeStart("domain.com", "www.")       = "domain.com"
	 * StringUtils.removeStart("www.domain.com", "domain") = "www.domain.com"
	 * StringUtils.removeStart("abc", "")    = "abc"
	 * </pre>
	 *
	 * @param str  the source String to search, may be null
	 * @param remove  the String to search for and remove, may be null
	 * @return the substring with the string removed if found,
	 *  {@code null} if null String input
	 * @since 2.1
	 */
	public static String removeStart(final String str, final String remove) {
		if (isEmpty(str) || isEmpty(remove)) {
			return str;
		}
		if (str.startsWith(remove)){
			return str.substring(remove.length());
		}
		return str;
	}

	/**
	 * <p>Removes a substring only if it is at the end of a source string,
	 * otherwise returns the source string.</p>
	 *
	 * <p>A {@code null} source string will return {@code null}.
	 * An empty ("") source string will return the empty string.
	 * A {@code null} search string will return the source string.</p>
	 *
	 * <pre>
	 * StringUtils.removeEnd(null, *)      = null
	 * StringUtils.removeEnd("", *)        = ""
	 * StringUtils.removeEnd(*, null)      = *
	 * StringUtils.removeEnd("www.domain.com", ".com.")  = "www.domain.com"
	 * StringUtils.removeEnd("www.domain.com", ".com")   = "www.domain"
	 * StringUtils.removeEnd("www.domain.com", "domain") = "www.domain.com"
	 * StringUtils.removeEnd("abc", "")    = "abc"
	 * </pre>
	 *
	 * @param str  the source String to search, may be null
	 * @param remove  the String to search for and remove, may be null
	 * @return the substring with the string removed if found,
	 *  {@code null} if null String input
	 * @since 2.1
	 */
	public static String removeEnd(final String str, final String remove) {
		if (isEmpty(str) || isEmpty(remove)) {
			return str;
		}
		if (str.endsWith(remove)) {
			return str.substring(0, str.length() - remove.length());
		}
		return str;
	}

	/**
	 * <p>Removes all occurrences of a substring from within the source string.</p>
	 *
	 * <p>A {@code null} source string will return {@code null}.
	 * An empty ("") source string will return the empty string.
	 * A {@code null} remove string will return the source string.
	 * An empty ("") remove string will return the source string.</p>
	 *
	 * <pre>
	 * StringUtils.remove(null, *)        = null
	 * StringUtils.remove("", *)          = ""
	 * StringUtils.remove(*, null)        = *
	 * StringUtils.remove(*, "")          = *
	 * StringUtils.remove("queued", "ue") = "qd"
	 * StringUtils.remove("queued", "zz") = "queued"
	 * </pre>
	 *
	 * @param str  the source String to search, may be null
	 * @param remove  the String to search for and remove, may be null
	 * @return the substring with the string removed if found,
	 *  {@code null} if null String input
	 * @since 2.1
	 */
	public static String remove(final String str, final String remove) {
		if (isEmpty(str) || isEmpty(remove)) {
			return str;
		}
		return replace(str, remove, EMPTY, -1);
	}

	/**
	 * <p>Removes all occurrences of a character from within the source string.</p>
	 *
	 * <p>A {@code null} source string will return {@code null}.
	 * An empty ("") source string will return the empty string.</p>
	 *
	 * <pre>
	 * StringUtils.remove(null, *)       = null
	 * StringUtils.remove("", *)         = ""
	 * StringUtils.remove("queued", 'u') = "qeed"
	 * StringUtils.remove("queued", 'z') = "queued"
	 * </pre>
	 *
	 * @param str  the source String to search, may be null
	 * @param remove  the char to search for and remove, may be null
	 * @return the substring with the char removed if found,
	 *  {@code null} if null String input
	 * @since 2.1
	 */
	public static String remove(final String str, final char remove) {
		if (isEmpty(str) || str.indexOf(remove) == INDEX_NOT_FOUND) {
			return str;
		}
		final char[] chars = str.toCharArray();
		int pos = 0;
		for (int i = 0; i < chars.length; i++) {
			if (chars[i] != remove) {
				chars[pos++] = chars[i];
			}
		}
		return new String(chars, 0, pos);
	}

	// Replacing
	//-----------------------------------------------------------------------
	/**
	 * <p>Replaces a String with another String inside a larger String, once.</p>
	 *
	 * <p>A {@code null} reference passed to this method is a no-op.</p>
	 *
	 * <pre>
	 * StringUtils.replaceOnce(null, *, *)        = null
	 * StringUtils.replaceOnce("", *, *)          = ""
	 * StringUtils.replaceOnce("any", null, *)    = "any"
	 * StringUtils.replaceOnce("any", *, null)    = "any"
	 * StringUtils.replaceOnce("any", "", *)      = "any"
	 * StringUtils.replaceOnce("aba", "a", null)  = "aba"
	 * StringUtils.replaceOnce("aba", "a", "")    = "ba"
	 * StringUtils.replaceOnce("aba", "a", "z")   = "zba"
	 * </pre>
	 *
	 * @see #replace(String text, String searchString, String replacement, int max)
	 * @param text  text to search and replace in, may be null
	 * @param searchString  the String to search for, may be null
	 * @param replacement  the String to replace with, may be null
	 * @return the text with any replacements processed,
	 *  {@code null} if null String input
	 */
	public static String replaceOnce(final String text, final String searchString, final String replacement) {
		return replace(text, searchString, replacement, 1);
	}

	/**
	 * Replaces each substring of the source String that matches the given regular expression with the given
	 * replacement using the {@link Pattern#DOTALL} option. DOTALL is also know as single-line mode in Perl. This call
	 * is also equivalent to:
	 * <ul>
	 * <li>{@code source.replaceAll(&quot;(?s)&quot; + regex, replacement)}</li>
	 * <li>{@code Pattern.compile(regex, Pattern.DOTALL).matcher(source).replaceAll(replacement)}</li>
	 * </ul>
	 *
	 * @param source
	 *            the source string
	 * @param regex
	 *            the regular expression to which this string is to be matched
	 * @param replacement
	 *            the string to be substituted for each match
	 * @return The resulting {@code String}
	 * @see String#replaceAll(String, String)
	 * @see Pattern#DOTALL
	 * @since 3.2
	 */
	public static String replacePattern(final String source, final String regex, final String replacement) {
		return Pattern.compile(regex, Pattern.DOTALL).matcher(source).replaceAll(replacement);
	}

	/**
	 * Removes each substring of the source String that matches the given regular expression using the DOTALL option.
	 *
	 * @param source
	 *            the source string
	 * @param regex
	 *            the regular expression to which this string is to be matched
	 * @return The resulting {@code String}
	 * @see String#replaceAll(String, String)
	 * @see Pattern#DOTALL
	 * @since 3.2
	 */
	public static String removePattern(final String source, final String regex) {
		return replacePattern(source, regex, StringUtils.EMPTY);
	}

	/**
	 * <p>Replaces all occurrences of a String within another String.</p>
	 *
	 * <p>A {@code null} reference passed to this method is a no-op.</p>
	 *
	 * <pre>
	 * StringUtils.replace(null, *, *)        = null
	 * StringUtils.replace("", *, *)          = ""
	 * StringUtils.replace("any", null, *)    = "any"
	 * StringUtils.replace("any", *, null)    = "any"
	 * StringUtils.replace("any", "", *)      = "any"
	 * StringUtils.replace("aba", "a", null)  = "aba"
	 * StringUtils.replace("aba", "a", "")    = "b"
	 * StringUtils.replace("aba", "a", "z")   = "zbz"
	 * </pre>
	 *
	 * @see #replace(String text, String searchString, String replacement, int max)
	 * @param text  text to search and replace in, may be null
	 * @param searchString  the String to search for, may be null
	 * @param replacement  the String to replace it with, may be null
	 * @return the text with any replacements processed,
	 *  {@code null} if null String input
	 */
	public static String replace(final String text, final String searchString, final String replacement) {
		return replace(text, searchString, replacement, -1);
	}

	/**
	 * <p>Replaces a String with another String inside a larger String,
	 * for the first {@code max} values of the search String.</p>
	 *
	 * <p>A {@code null} reference passed to this method is a no-op.</p>
	 *
	 * <pre>
	 * StringUtils.replace(null, *, *, *)         = null
	 * StringUtils.replace("", *, *, *)           = ""
	 * StringUtils.replace("any", null, *, *)     = "any"
	 * StringUtils.replace("any", *, null, *)     = "any"
	 * StringUtils.replace("any", "", *, *)       = "any"
	 * StringUtils.replace("any", *, *, 0)        = "any"
	 * StringUtils.replace("abaa", "a", null, -1) = "abaa"
	 * StringUtils.replace("abaa", "a", "", -1)   = "b"
	 * StringUtils.replace("abaa", "a", "z", 0)   = "abaa"
	 * StringUtils.replace("abaa", "a", "z", 1)   = "zbaa"
	 * StringUtils.replace("abaa", "a", "z", 2)   = "zbza"
	 * StringUtils.replace("abaa", "a", "z", -1)  = "zbzz"
	 * </pre>
	 *
	 * @param text  text to search and replace in, may be null
	 * @param searchString  the String to search for, may be null
	 * @param replacement  the String to replace it with, may be null
	 * @param max  maximum number of values to replace, or {@code -1} if no maximum
	 * @return the text with any replacements processed,
	 *  {@code null} if null String input
	 */
	public static String replace(final String text, final String searchString, final String replacement, int max) {
		if (isEmpty(text) || isEmpty(searchString) || replacement == null || max == 0) {
			return text;
		}
		int start = 0;
		int end = text.indexOf(searchString, start);
		if (end == INDEX_NOT_FOUND) {
			return text;
		}
		final int replLength = searchString.length();
		int increase = replacement.length() - replLength;
		increase = increase < 0 ? 0 : increase;
		increase *= max < 0 ? 16 : max > 64 ? 64 : max;
		final StringBuilder buf = new StringBuilder(text.length() + increase);
		while (end != INDEX_NOT_FOUND) {
			buf.append(text.substring(start, end)).append(replacement);
			start = end + replLength;
			if (--max == 0) {
				break;
			}
			end = text.indexOf(searchString, start);
		}
		buf.append(text.substring(start));
		return buf.toString();
	}

	/**
	 * <p>
	 * Replaces all occurrences of Strings within another String.
	 * </p>
	 *
	 * <p>
	 * A {@code null} reference passed to this method is a no-op, or if
	 * any "search string" or "string to replace" is null, that replace will be
	 * ignored. This will not repeat. For repeating replaces, call the
	 * overloaded method.
	 * </p>
	 *
	 * <pre>
	 *  StringUtils.replaceEach(null, *, *)        = null
	 *  StringUtils.replaceEach("", *, *)          = ""
	 *  StringUtils.replaceEach("aba", null, null) = "aba"
	 *  StringUtils.replaceEach("aba", new String[0], null) = "aba"
	 *  StringUtils.replaceEach("aba", null, new String[0]) = "aba"
	 *  StringUtils.replaceEach("aba", new String[]{"a"}, null)  = "aba"
	 *  StringUtils.replaceEach("aba", new String[]{"a"}, new String[]{""})  = "b"
	 *  StringUtils.replaceEach("aba", new String[]{null}, new String[]{"a"})  = "aba"
	 *  StringUtils.replaceEach("abcde", new String[]{"ab", "d"}, new String[]{"w", "t"})  = "wcte"
	 *  (example of how it does not repeat)
	 *  StringUtils.replaceEach("abcde", new String[]{"ab", "d"}, new String[]{"d", "t"})  = "dcte"
	 * </pre>
	 *
	 * @param text
	 *            text to search and replace in, no-op if null
	 * @param searchList
	 *            the Strings to search for, no-op if null
	 * @param replacementList
	 *            the Strings to replace them with, no-op if null
	 * @return the text with any replacements processed, {@code null} if
	 *         null String input
	 * @throws IllegalArgumentException
	 *             if the lengths of the arrays are not the same (null is ok,
	 *             and/or size 0)
	 * @since 2.4
	 */
	public static String replaceEach(final String text, final String[] searchList, final String[] replacementList) {
		return replaceEach(text, searchList, replacementList, false, 0);
	}

	/**
	 * <p>
	 * Replaces all occurrences of Strings within another String.
	 * </p>
	 *
	 * <p>
	 * A {@code null} reference passed to this method is a no-op, or if
	 * any "search string" or "string to replace" is null, that replace will be
	 * ignored.
	 * </p>
	 *
	 * <pre>
	 *  StringUtils.replaceEachRepeatedly(null, *, *) = null
	 *  StringUtils.replaceEachRepeatedly("", *, *) = ""
	 *  StringUtils.replaceEachRepeatedly("aba", null, null) = "aba"
	 *  StringUtils.replaceEachRepeatedly("aba", new String[0], null) = "aba"
	 *  StringUtils.replaceEachRepeatedly("aba", null, new String[0]) = "aba"
	 *  StringUtils.replaceEachRepeatedly("aba", new String[]{"a"}, null) = "aba"
	 *  StringUtils.replaceEachRepeatedly("aba", new String[]{"a"}, new String[]{""}) = "b"
	 *  StringUtils.replaceEachRepeatedly("aba", new String[]{null}, new String[]{"a"}) = "aba"
	 *  StringUtils.replaceEachRepeatedly("abcde", new String[]{"ab", "d"}, new String[]{"w", "t"}) = "wcte"
	 *  (example of how it repeats)
	 *  StringUtils.replaceEachRepeatedly("abcde", new String[]{"ab", "d"}, new String[]{"d", "t"}) = "tcte"
	 *  StringUtils.replaceEachRepeatedly("abcde", new String[]{"ab", "d"}, new String[]{"d", "ab"}) = IllegalStateException
	 * </pre>
	 *
	 * @param text
	 *            text to search and replace in, no-op if null
	 * @param searchList
	 *            the Strings to search for, no-op if null
	 * @param replacementList
	 *            the Strings to replace them with, no-op if null
	 * @return the text with any replacements processed, {@code null} if
	 *         null String input
	 * @throws IllegalStateException
	 *             if the search is repeating and there is an endless loop due
	 *             to outputs of one being inputs to another
	 * @throws IllegalArgumentException
	 *             if the lengths of the arrays are not the same (null is ok,
	 *             and/or size 0)
	 * @since 2.4
	 */
	public static String replaceEachRepeatedly(final String text, final String[] searchList, final String[] replacementList) {
		// timeToLive should be 0 if not used or nothing to replace, else it's
		// the length of the replace array
		final int timeToLive = searchList == null ? 0 : searchList.length;
		return replaceEach(text, searchList, replacementList, true, timeToLive);
	}

	/**
	 * <p>
	 * Replace all occurrences of Strings within another String.
	 * This is a private recursive helper method for {@link #replaceEachRepeatedly(String, String[], String[])} and
	 * {@link #replaceEach(String, String[], String[])}
	 * </p>
	 *
	 * <p>
	 * A {@code null} reference passed to this method is a no-op, or if
	 * any "search string" or "string to replace" is null, that replace will be
	 * ignored.
	 * </p>
	 *
	 * <pre>
	 *  StringUtils.replaceEach(null, *, *, *, *) = null
	 *  StringUtils.replaceEach("", *, *, *, *) = ""
	 *  StringUtils.replaceEach("aba", null, null, *, *) = "aba"
	 *  StringUtils.replaceEach("aba", new String[0], null, *, *) = "aba"
	 *  StringUtils.replaceEach("aba", null, new String[0], *, *) = "aba"
	 *  StringUtils.replaceEach("aba", new String[]{"a"}, null, *, *) = "aba"
	 *  StringUtils.replaceEach("aba", new String[]{"a"}, new String[]{""}, *, >=0) = "b"
	 *  StringUtils.replaceEach("aba", new String[]{null}, new String[]{"a"}, *, >=0) = "aba"
	 *  StringUtils.replaceEach("abcde", new String[]{"ab", "d"}, new String[]{"w", "t"}, *, >=0) = "wcte"
	 *  (example of how it repeats)
	 *  StringUtils.replaceEach("abcde", new String[]{"ab", "d"}, new String[]{"d", "t"}, false, >=0) = "dcte"
	 *  StringUtils.replaceEach("abcde", new String[]{"ab", "d"}, new String[]{"d", "t"}, true, >=2) = "tcte"
	 *  StringUtils.replaceEach("abcde", new String[]{"ab", "d"}, new String[]{"d", "ab"}, *, *) = IllegalStateException
	 * </pre>
	 *
	 * @param text
	 *            text to search and replace in, no-op if null
	 * @param searchList
	 *            the Strings to search for, no-op if null
	 * @param replacementList
	 *            the Strings to replace them with, no-op if null
	 * @param repeat if true, then replace repeatedly
	 *       until there are no more possible replacements or timeToLive < 0
	 * @param timeToLive
	 *            if less than 0 then there is a circular reference and endless
	 *            loop
	 * @return the text with any replacements processed, {@code null} if
	 *         null String input
	 * @throws IllegalStateException
	 *             if the search is repeating and there is an endless loop due
	 *             to outputs of one being inputs to another
	 * @throws IllegalArgumentException
	 *             if the lengths of the arrays are not the same (null is ok,
	 *             and/or size 0)
	 * @since 2.4
	 */
	private static String replaceEach(
			final String text, final String[] searchList, final String[] replacementList, final boolean repeat, final int timeToLive) {

		// mchyzer Performance note: This creates very few new objects (one major goal)
		// let me know if there are performance requests, we can create a harness to measure

		if (text == null || text.isEmpty() || searchList == null ||
				searchList.length == 0 || replacementList == null || replacementList.length == 0) {
			return text;
		}

		// if recursing, this shouldn't be less than 0
		if (timeToLive < 0) {
			throw new IllegalStateException("Aborting to protect against StackOverflowError - " +
					"output of one loop is the input of another");
		}

		final int searchLength = searchList.length;
		final int replacementLength = replacementList.length;

		// make sure lengths are ok, these need to be equal
		if (searchLength != replacementLength) {
			throw new IllegalArgumentException("Search and Replace array lengths don't match: "
					+ searchLength
					+ " vs "
					+ replacementLength);
		}

		// keep track of which still have matches
		final boolean[] noMoreMatchesForReplIndex = new boolean[searchLength];

		// index on index that the match was found
		int textIndex = -1;
		int replaceIndex = -1;
		int tempIndex = -1;

		// index of replace array that will replace the search string found
		// NOTE: logic duplicated below START
		for (int i = 0; i < searchLength; i++) {
			if (noMoreMatchesForReplIndex[i] || searchList[i] == null ||
					searchList[i].isEmpty() || replacementList[i] == null) {
				continue;
			}
			tempIndex = text.indexOf(searchList[i]);

			// see if we need to keep searching for this
			if (tempIndex == -1) {
				noMoreMatchesForReplIndex[i] = true;
			} else {
				if (textIndex == -1 || tempIndex < textIndex) {
					textIndex = tempIndex;
					replaceIndex = i;
				}
			}
		}
		// NOTE: logic mostly below END

		// no search strings found, we are done
		if (textIndex == -1) {
			return text;
		}

		int start = 0;

		// get a good guess on the size of the result buffer so it doesn't have to double if it goes over a bit
		int increase = 0;

		// count the replacement text elements that are larger than their corresponding text being replaced
		for (int i = 0; i < searchList.length; i++) {
			if (searchList[i] == null || replacementList[i] == null) {
				continue;
			}
			final int greater = replacementList[i].length() - searchList[i].length();
			if (greater > 0) {
				increase += 3 * greater; // assume 3 matches
			}
		}
		// have upper-bound at 20% increase, then let Java take over
		increase = Math.min(increase, text.length() / 5);

		final StringBuilder buf = new StringBuilder(text.length() + increase);

		while (textIndex != -1) {

			for (int i = start; i < textIndex; i++) {
				buf.append(text.charAt(i));
			}
			buf.append(replacementList[replaceIndex]);

			start = textIndex + searchList[replaceIndex].length();

			textIndex = -1;
			replaceIndex = -1;
			tempIndex = -1;
			// find the next earliest match
			// NOTE: logic mostly duplicated above START
			for (int i = 0; i < searchLength; i++) {
				if (noMoreMatchesForReplIndex[i] || searchList[i] == null ||
						searchList[i].isEmpty() || replacementList[i] == null) {
					continue;
				}
				tempIndex = text.indexOf(searchList[i], start);

				// see if we need to keep searching for this
				if (tempIndex == -1) {
					noMoreMatchesForReplIndex[i] = true;
				} else {
					if (textIndex == -1 || tempIndex < textIndex) {
						textIndex = tempIndex;
						replaceIndex = i;
					}
				}
			}
			// NOTE: logic duplicated above END

		}
		final int textLength = text.length();
		for (int i = start; i < textLength; i++) {
			buf.append(text.charAt(i));
		}
		final String result = buf.toString();
		if (!repeat) {
			return result;
		}

		return replaceEach(result, searchList, replacementList, repeat, timeToLive - 1);
	}

	// Replace, character based
	//-----------------------------------------------------------------------
	/**
	 * <p>Replaces all occurrences of a character in a String with another.
	 * This is a null-safe version of {@link String#replace(char, char)}.</p>
	 *
	 * <p>A {@code null} string input returns {@code null}.
	 * An empty ("") string input returns an empty string.</p>
	 *
	 * <pre>
	 * StringUtils.replaceChars(null, *, *)        = null
	 * StringUtils.replaceChars("", *, *)          = ""
	 * StringUtils.replaceChars("abcba", 'b', 'y') = "aycya"
	 * StringUtils.replaceChars("abcba", 'z', 'y') = "abcba"
	 * </pre>
	 *
	 * @param str  String to replace characters in, may be null
	 * @param searchChar  the character to search for, may be null
	 * @param replaceChar  the character to replace, may be null
	 * @return modified String, {@code null} if null string input
	 * @since 2.0
	 */
	public static String replaceChars(final String str, final char searchChar, final char replaceChar) {
		if (str == null) {
			return null;
		}
		return str.replace(searchChar, replaceChar);
	}

	/**
	 * <p>Replaces multiple characters in a String in one go.
	 * This method can also be used to delete characters.</p>
	 *
	 * <p>For example:<br>
	 * <code>replaceChars(&quot;hello&quot;, &quot;ho&quot;, &quot;jy&quot;) = jelly</code>.</p>
	 *
	 * <p>A {@code null} string input returns {@code null}.
	 * An empty ("") string input returns an empty string.
	 * A null or empty set of search characters returns the input string.</p>
	 *
	 * <p>The length of the search characters should normally equal the length
	 * of the replace characters.
	 * If the search characters is longer, then the extra search characters
	 * are deleted.
	 * If the search characters is shorter, then the extra replace characters
	 * are ignored.</p>
	 *
	 * <pre>
	 * StringUtils.replaceChars(null, *, *)           = null
	 * StringUtils.replaceChars("", *, *)             = ""
	 * StringUtils.replaceChars("abc", null, *)       = "abc"
	 * StringUtils.replaceChars("abc", "", *)         = "abc"
	 * StringUtils.replaceChars("abc", "b", null)     = "ac"
	 * StringUtils.replaceChars("abc", "b", "")       = "ac"
	 * StringUtils.replaceChars("abcba", "bc", "yz")  = "ayzya"
	 * StringUtils.replaceChars("abcba", "bc", "y")   = "ayya"
	 * StringUtils.replaceChars("abcba", "bc", "yzx") = "ayzya"
	 * </pre>
	 *
	 * @param str  String to replace characters in, may be null
	 * @param searchChars  a set of characters to search for, may be null
	 * @param replaceChars  a set of characters to replace, may be null
	 * @return modified String, {@code null} if null string input
	 * @since 2.0
	 */
	public static String replaceChars(final String str, final String searchChars, String replaceChars) {
		if (isEmpty(str) || isEmpty(searchChars)) {
			return str;
		}
		if (replaceChars == null) {
			replaceChars = EMPTY;
		}
		boolean modified = false;
		final int replaceCharsLength = replaceChars.length();
		final int strLength = str.length();
		final StringBuilder buf = new StringBuilder(strLength);
		for (int i = 0; i < strLength; i++) {
			final char ch = str.charAt(i);
			final int index = searchChars.indexOf(ch);
			if (index >= 0) {
				modified = true;
				if (index < replaceCharsLength) {
					buf.append(replaceChars.charAt(index));
				}
			} else {
				buf.append(ch);
			}
		}
		if (modified) {
			return buf.toString();
		}
		return str;
	}

	// Overlay
	//-----------------------------------------------------------------------
	/**
	 * <p>Overlays part of a String with another String.</p>
	 *
	 * <p>A {@code null} string input returns {@code null}.
	 * A negative index is treated as zero.
	 * An index greater than the string length is treated as the string length.
	 * The start index is always the smaller of the two indices.</p>
	 *
	 * <pre>
	 * StringUtils.overlay(null, *, *, *)            = null
	 * StringUtils.overlay("", "abc", 0, 0)          = "abc"
	 * StringUtils.overlay("abcdef", null, 2, 4)     = "abef"
	 * StringUtils.overlay("abcdef", "", 2, 4)       = "abef"
	 * StringUtils.overlay("abcdef", "", 4, 2)       = "abef"
	 * StringUtils.overlay("abcdef", "zzzz", 2, 4)   = "abzzzzef"
	 * StringUtils.overlay("abcdef", "zzzz", 4, 2)   = "abzzzzef"
	 * StringUtils.overlay("abcdef", "zzzz", -1, 4)  = "zzzzef"
	 * StringUtils.overlay("abcdef", "zzzz", 2, 8)   = "abzzzz"
	 * StringUtils.overlay("abcdef", "zzzz", -2, -3) = "zzzzabcdef"
	 * StringUtils.overlay("abcdef", "zzzz", 8, 10)  = "abcdefzzzz"
	 * </pre>
	 *
	 * @param str  the String to do overlaying in, may be null
	 * @param overlay  the String to overlay, may be null
	 * @param start  the position to start overlaying at
	 * @param end  the position to stop overlaying before
	 * @return overlayed String, {@code null} if null String input
	 * @since 2.0
	 */
	public static String overlay(final String str, String overlay, int start, int end) {
		if (str == null) {
			return null;
		}
		if (overlay == null) {
			overlay = EMPTY;
		}
		final int len = str.length();
		if (start < 0) {
			start = 0;
		}
		if (start > len) {
			start = len;
		}
		if (end < 0) {
			end = 0;
		}
		if (end > len) {
			end = len;
		}
		if (start > end) {
			final int temp = start;
			start = end;
			end = temp;
		}
		return new StringBuilder(len + start - end + overlay.length() + 1)
		.append(str.substring(0, start))
		.append(overlay)
		.append(str.substring(end))
		.toString();
	}

	// Chomping
	//-----------------------------------------------------------------------
	/**
	 * <p>Removes one newline from end of a String if it's there,
	 * otherwise leave it alone.  A newline is &quot;{@code \n}&quot;,
	 * &quot;{@code \r}&quot;, or &quot;{@code \r\n}&quot;.</p>
	 *
	 * <p>NOTE: This method changed in 2.0.
	 * It now more closely matches Perl chomp.</p>
	 *
	 * <pre>
	 * StringUtils.chomp(null)          = null
	 * StringUtils.chomp("")            = ""
	 * StringUtils.chomp("abc \r")      = "abc "
	 * StringUtils.chomp("abc\n")       = "abc"
	 * StringUtils.chomp("abc\r\n")     = "abc"
	 * StringUtils.chomp("abc\r\n\r\n") = "abc\r\n"
	 * StringUtils.chomp("abc\n\r")     = "abc\n"
	 * StringUtils.chomp("abc\n\rabc")  = "abc\n\rabc"
	 * StringUtils.chomp("\r")          = ""
	 * StringUtils.chomp("\n")          = ""
	 * StringUtils.chomp("\r\n")        = ""
	 * </pre>
	 *
	 * @param str  the String to chomp a newline from, may be null
	 * @return String without newline, {@code null} if null String input
	 */
	public static String chomp(final String str) {
		if (isEmpty(str)) {
			return str;
		}

		if (str.length() == 1) {
			final char ch = str.charAt(0);
			if (ch == CharUtils.CR || ch == CharUtils.LF) {
				return EMPTY;
			}
			return str;
		}

		int lastIdx = str.length() - 1;
		final char last = str.charAt(lastIdx);

		if (last == CharUtils.LF) {
			if (str.charAt(lastIdx - 1) == CharUtils.CR) {
				lastIdx--;
			}
		} else if (last != CharUtils.CR) {
			lastIdx++;
		}
		return str.substring(0, lastIdx);
	}

	// Chopping
	//-----------------------------------------------------------------------
	/**
	 * <p>Remove the last character from a String.</p>
	 *
	 * <p>If the String ends in {@code \r\n}, then remove both
	 * of them.</p>
	 *
	 * <pre>
	 * StringUtils.chop(null)          = null
	 * StringUtils.chop("")            = ""
	 * StringUtils.chop("abc \r")      = "abc "
	 * StringUtils.chop("abc\n")       = "abc"
	 * StringUtils.chop("abc\r\n")     = "abc"
	 * StringUtils.chop("abc")         = "ab"
	 * StringUtils.chop("abc\nabc")    = "abc\nab"
	 * StringUtils.chop("a")           = ""
	 * StringUtils.chop("\r")          = ""
	 * StringUtils.chop("\n")          = ""
	 * StringUtils.chop("\r\n")        = ""
	 * </pre>
	 *
	 * @param str  the String to chop last character from, may be null
	 * @return String without last character, {@code null} if null String input
	 */
	public static String chop(final String str) {
		if (str == null) {
			return null;
		}
		final int strLen = str.length();
		if (strLen < 2) {
			return EMPTY;
		}
		final int lastIdx = strLen - 1;
		final String ret = str.substring(0, lastIdx);
		final char last = str.charAt(lastIdx);
		if (last == CharUtils.LF && ret.charAt(lastIdx - 1) == CharUtils.CR) {
			return ret.substring(0, lastIdx - 1);
		}
		return ret;
	}

	// Conversion
	//-----------------------------------------------------------------------

	// Padding
	//-----------------------------------------------------------------------
	/**
	 * <p>Repeat a String {@code repeat} times to form a
	 * new String.</p>
	 *
	 * <pre>
	 * StringUtils.repeat(null, 2) = null
	 * StringUtils.repeat("", 0)   = ""
	 * StringUtils.repeat("", 2)   = ""
	 * StringUtils.repeat("a", 3)  = "aaa"
	 * StringUtils.repeat("ab", 2) = "abab"
	 * StringUtils.repeat("a", -2) = ""
	 * </pre>
	 *
	 * @param str  the String to repeat, may be null
	 * @param repeat  number of times to repeat str, negative treated as zero
	 * @return a new String consisting of the original String repeated,
	 *  {@code null} if null String input
	 */
	public static String repeat(final String str, final int repeat) {
		// Performance tuned for 2.0 (JDK1.4)

		if (str == null) {
			return null;
		}
		if (repeat <= 0) {
			return EMPTY;
		}
		final int inputLength = str.length();
		if (repeat == 1 || inputLength == 0) {
			return str;
		}
		if (inputLength == 1 && repeat <= PAD_LIMIT) {
			return repeat(str.charAt(0), repeat);
		}

		final int outputLength = inputLength * repeat;
		switch (inputLength) {
		case 1 :
			return repeat(str.charAt(0), repeat);
		case 2 :
			final char ch0 = str.charAt(0);
			final char ch1 = str.charAt(1);
			final char[] output2 = new char[outputLength];
			for (int i = repeat * 2 - 2; i >= 0; i--, i--) {
				output2[i] = ch0;
				output2[i + 1] = ch1;
			}
			return new String(output2);
		default :
			final StringBuilder buf = new StringBuilder(outputLength);
			for (int i = 0; i < repeat; i++) {
				buf.append(str);
			}
			return buf.toString();
		}
	}

	/**
	 * <p>Repeat a String {@code repeat} times to form a
	 * new String, with a String separator injected each time. </p>
	 *
	 * <pre>
	 * StringUtils.repeat(null, null, 2) = null
	 * StringUtils.repeat(null, "x", 2)  = null
	 * StringUtils.repeat("", null, 0)   = ""
	 * StringUtils.repeat("", "", 2)     = ""
	 * StringUtils.repeat("", "x", 3)    = "xxx"
	 * StringUtils.repeat("?", ", ", 3)  = "?, ?, ?"
	 * </pre>
	 *
	 * @param str        the String to repeat, may be null
	 * @param separator  the String to inject, may be null
	 * @param repeat     number of times to repeat str, negative treated as zero
	 * @return a new String consisting of the original String repeated,
	 *  {@code null} if null String input
	 * @since 2.5
	 */
	public static String repeat(final String str, final String separator, final int repeat) {
		if(str == null || separator == null) {
			return repeat(str, repeat);
		}
		// given that repeat(String, int) is quite optimized, better to rely on it than try and splice this into it
		final String result = repeat(str + separator, repeat);
		return removeEnd(result, separator);
	}

	/**
	 * <p>Returns padding using the specified delimiter repeated
	 * to a given length.</p>
	 *
	 * <pre>
	 * StringUtils.repeat('e', 0)  = ""
	 * StringUtils.repeat('e', 3)  = "eee"
	 * StringUtils.repeat('e', -2) = ""
	 * </pre>
	 *
	 * <p>Note: this method doesn't not support padding with
	 * <a href="http://www.unicode.org/glossary/#supplementary_character">Unicode Supplementary Characters</a>
	 * as they require a pair of {@code char}s to be represented.
	 * If you are needing to support full I18N of your applications
	 * consider using {@link #repeat(String, int)} instead.
	 * </p>
	 *
	 * @param ch  character to repeat
	 * @param repeat  number of times to repeat char, negative treated as zero
	 * @return String with repeated character
	 * @see #repeat(String, int)
	 */
	public static String repeat(final char ch, final int repeat) {
		final char[] buf = new char[repeat];
		for (int i = repeat - 1; i >= 0; i--) {
			buf[i] = ch;
		}
		return new String(buf);
	}

	/**
	 * <p>Right pad a String with spaces (' ').</p>
	 *
	 * <p>The String is padded to the size of {@code size}.</p>
	 *
	 * <pre>
	 * StringUtils.rightPad(null, *)   = null
	 * StringUtils.rightPad("", 3)     = "   "
	 * StringUtils.rightPad("bat", 3)  = "bat"
	 * StringUtils.rightPad("bat", 5)  = "bat  "
	 * StringUtils.rightPad("bat", 1)  = "bat"
	 * StringUtils.rightPad("bat", -1) = "bat"
	 * </pre>
	 *
	 * @param str  the String to pad out, may be null
	 * @param size  the size to pad to
	 * @return right padded String or original String if no padding is necessary,
	 *  {@code null} if null String input
	 */
	public static String rightPad(final String str, final int size) {
		return rightPad(str, size, ' ');
	}

	/**
	 * <p>Right pad a String with a specified character.</p>
	 *
	 * <p>The String is padded to the size of {@code size}.</p>
	 *
	 * <pre>
	 * StringUtils.rightPad(null, *, *)     = null
	 * StringUtils.rightPad("", 3, 'z')     = "zzz"
	 * StringUtils.rightPad("bat", 3, 'z')  = "bat"
	 * StringUtils.rightPad("bat", 5, 'z')  = "batzz"
	 * StringUtils.rightPad("bat", 1, 'z')  = "bat"
	 * StringUtils.rightPad("bat", -1, 'z') = "bat"
	 * </pre>
	 *
	 * @param str  the String to pad out, may be null
	 * @param size  the size to pad to
	 * @param padChar  the character to pad with
	 * @return right padded String or original String if no padding is necessary,
	 *  {@code null} if null String input
	 * @since 2.0
	 */
	public static String rightPad(final String str, final int size, final char padChar) {
		if (str == null) {
			return null;
		}
		final int pads = size - str.length();
		if (pads <= 0) {
			return str; // returns original String when possible
		}
		if (pads > PAD_LIMIT) {
			return rightPad(str, size, String.valueOf(padChar));
		}
		return str.concat(repeat(padChar, pads));
	}

	/**
	 * <p>Right pad a String with a specified String.</p>
	 *
	 * <p>The String is padded to the size of {@code size}.</p>
	 *
	 * <pre>
	 * StringUtils.rightPad(null, *, *)      = null
	 * StringUtils.rightPad("", 3, "z")      = "zzz"
	 * StringUtils.rightPad("bat", 3, "yz")  = "bat"
	 * StringUtils.rightPad("bat", 5, "yz")  = "batyz"
	 * StringUtils.rightPad("bat", 8, "yz")  = "batyzyzy"
	 * StringUtils.rightPad("bat", 1, "yz")  = "bat"
	 * StringUtils.rightPad("bat", -1, "yz") = "bat"
	 * StringUtils.rightPad("bat", 5, null)  = "bat  "
	 * StringUtils.rightPad("bat", 5, "")    = "bat  "
	 * </pre>
	 *
	 * @param str  the String to pad out, may be null
	 * @param size  the size to pad to
	 * @param padStr  the String to pad with, null or empty treated as single space
	 * @return right padded String or original String if no padding is necessary,
	 *  {@code null} if null String input
	 */
	public static String rightPad(final String str, final int size, String padStr) {
		if (str == null) {
			return null;
		}
		if (isEmpty(padStr)) {
			padStr = SPACE;
		}
		final int padLen = padStr.length();
		final int strLen = str.length();
		final int pads = size - strLen;
		if (pads <= 0) {
			return str; // returns original String when possible
		}
		if (padLen == 1 && pads <= PAD_LIMIT) {
			return rightPad(str, size, padStr.charAt(0));
		}

		if (pads == padLen) {
			return str.concat(padStr);
		} else if (pads < padLen) {
			return str.concat(padStr.substring(0, pads));
		} else {
			final char[] padding = new char[pads];
			final char[] padChars = padStr.toCharArray();
			for (int i = 0; i < pads; i++) {
				padding[i] = padChars[i % padLen];
			}
			return str.concat(new String(padding));
		}
	}

	/**
	 * <p>Left pad a String with spaces (' ').</p>
	 *
	 * <p>The String is padded to the size of {@code size}.</p>
	 *
	 * <pre>
	 * StringUtils.leftPad(null, *)   = null
	 * StringUtils.leftPad("", 3)     = "   "
	 * StringUtils.leftPad("bat", 3)  = "bat"
	 * StringUtils.leftPad("bat", 5)  = "  bat"
	 * StringUtils.leftPad("bat", 1)  = "bat"
	 * StringUtils.leftPad("bat", -1) = "bat"
	 * </pre>
	 *
	 * @param str  the String to pad out, may be null
	 * @param size  the size to pad to
	 * @return left padded String or original String if no padding is necessary,
	 *  {@code null} if null String input
	 */
	public static String leftPad(final String str, final int size) {
		return leftPad(str, size, ' ');
	}

	/**
	 * <p>Left pad a String with a specified character.</p>
	 *
	 * <p>Pad to a size of {@code size}.</p>
	 *
	 * <pre>
	 * StringUtils.leftPad(null, *, *)     = null
	 * StringUtils.leftPad("", 3, 'z')     = "zzz"
	 * StringUtils.leftPad("bat", 3, 'z')  = "bat"
	 * StringUtils.leftPad("bat", 5, 'z')  = "zzbat"
	 * StringUtils.leftPad("bat", 1, 'z')  = "bat"
	 * StringUtils.leftPad("bat", -1, 'z') = "bat"
	 * </pre>
	 *
	 * @param str  the String to pad out, may be null
	 * @param size  the size to pad to
	 * @param padChar  the character to pad with
	 * @return left padded String or original String if no padding is necessary,
	 *  {@code null} if null String input
	 * @since 2.0
	 */
	public static String leftPad(final String str, final int size, final char padChar) {
		if (str == null) {
			return null;
		}
		final int pads = size - str.length();
		if (pads <= 0) {
			return str; // returns original String when possible
		}
		if (pads > PAD_LIMIT) {
			return leftPad(str, size, String.valueOf(padChar));
		}
		return repeat(padChar, pads).concat(str);
	}

	/**
	 * <p>Left pad a String with a specified String.</p>
	 *
	 * <p>Pad to a size of {@code size}.</p>
	 *
	 * <pre>
	 * StringUtils.leftPad(null, *, *)      = null
	 * StringUtils.leftPad("", 3, "z")      = "zzz"
	 * StringUtils.leftPad("bat", 3, "yz")  = "bat"
	 * StringUtils.leftPad("bat", 5, "yz")  = "yzbat"
	 * StringUtils.leftPad("bat", 8, "yz")  = "yzyzybat"
	 * StringUtils.leftPad("bat", 1, "yz")  = "bat"
	 * StringUtils.leftPad("bat", -1, "yz") = "bat"
	 * StringUtils.leftPad("bat", 5, null)  = "  bat"
	 * StringUtils.leftPad("bat", 5, "")    = "  bat"
	 * </pre>
	 *
	 * @param str  the String to pad out, may be null
	 * @param size  the size to pad to
	 * @param padStr  the String to pad with, null or empty treated as single space
	 * @return left padded String or original String if no padding is necessary,
	 *  {@code null} if null String input
	 */
	public static String leftPad(final String str, final int size, String padStr) {
		if (str == null) {
			return null;
		}
		if (isEmpty(padStr)) {
			padStr = SPACE;
		}
		final int padLen = padStr.length();
		final int strLen = str.length();
		final int pads = size - strLen;
		if (pads <= 0) {
			return str; // returns original String when possible
		}
		if (padLen == 1 && pads <= PAD_LIMIT) {
			return leftPad(str, size, padStr.charAt(0));
		}

		if (pads == padLen) {
			return padStr.concat(str);
		} else if (pads < padLen) {
			return padStr.substring(0, pads).concat(str);
		} else {
			final char[] padding = new char[pads];
			final char[] padChars = padStr.toCharArray();
			for (int i = 0; i < pads; i++) {
				padding[i] = padChars[i % padLen];
			}
			return new String(padding).concat(str);
		}
	}

	/**
	 * Gets a CharSequence length or {@code 0} if the CharSequence is
	 * {@code null}.
	 *
	 * @param cs
	 *            a CharSequence or {@code null}
	 * @return CharSequence length or {@code 0} if the CharSequence is
	 *         {@code null}.
	 * @since 2.4
	 * @since 3.0 Changed signature from length(String) to length(CharSequence)
	 */
	public static int length(final CharSequence cs) {
		return cs == null ? 0 : cs.length();
	}

	// Centering
	//-----------------------------------------------------------------------
	/**
	 * <p>Centers a String in a larger String of size {@code size}
	 * using the space character (' ').</p>
	 *
	 * <p>If the size is less than the String length, the String is returned.
	 * A {@code null} String returns {@code null}.
	 * A negative size is treated as zero.</p>
	 *
	 * <p>Equivalent to {@code center(str, size, " ")}.</p>
	 *
	 * <pre>
	 * StringUtils.center(null, *)   = null
	 * StringUtils.center("", 4)     = "    "
	 * StringUtils.center("ab", -1)  = "ab"
	 * StringUtils.center("ab", 4)   = " ab "
	 * StringUtils.center("abcd", 2) = "abcd"
	 * StringUtils.center("a", 4)    = " a  "
	 * </pre>
	 *
	 * @param str  the String to center, may be null
	 * @param size  the int size of new String, negative treated as zero
	 * @return centered String, {@code null} if null String input
	 */
	public static String center(final String str, final int size) {
		return center(str, size, ' ');
	}

	/**
	 * <p>Centers a String in a larger String of size {@code size}.
	 * Uses a supplied character as the value to pad the String with.</p>
	 *
	 * <p>If the size is less than the String length, the String is returned.
	 * A {@code null} String returns {@code null}.
	 * A negative size is treated as zero.</p>
	 *
	 * <pre>
	 * StringUtils.center(null, *, *)     = null
	 * StringUtils.center("", 4, ' ')     = "    "
	 * StringUtils.center("ab", -1, ' ')  = "ab"
	 * StringUtils.center("ab", 4, ' ')   = " ab "
	 * StringUtils.center("abcd", 2, ' ') = "abcd"
	 * StringUtils.center("a", 4, ' ')    = " a  "
	 * StringUtils.center("a", 4, 'y')    = "yayy"
	 * </pre>
	 *
	 * @param str  the String to center, may be null
	 * @param size  the int size of new String, negative treated as zero
	 * @param padChar  the character to pad the new String with
	 * @return centered String, {@code null} if null String input
	 * @since 2.0
	 */
	public static String center(String str, final int size, final char padChar) {
		if (str == null || size <= 0) {
			return str;
		}
		final int strLen = str.length();
		final int pads = size - strLen;
		if (pads <= 0) {
			return str;
		}
		str = leftPad(str, strLen + pads / 2, padChar);
		str = rightPad(str, size, padChar);
		return str;
	}

	/**
	 * <p>Centers a String in a larger String of size {@code size}.
	 * Uses a supplied String as the value to pad the String with.</p>
	 *
	 * <p>If the size is less than the String length, the String is returned.
	 * A {@code null} String returns {@code null}.
	 * A negative size is treated as zero.</p>
	 *
	 * <pre>
	 * StringUtils.center(null, *, *)     = null
	 * StringUtils.center("", 4, " ")     = "    "
	 * StringUtils.center("ab", -1, " ")  = "ab"
	 * StringUtils.center("ab", 4, " ")   = " ab "
	 * StringUtils.center("abcd", 2, " ") = "abcd"
	 * StringUtils.center("a", 4, " ")    = " a  "
	 * StringUtils.center("a", 4, "yz")   = "yayz"
	 * StringUtils.center("abc", 7, null) = "  abc  "
	 * StringUtils.center("abc", 7, "")   = "  abc  "
	 * </pre>
	 *
	 * @param str  the String to center, may be null
	 * @param size  the int size of new String, negative treated as zero
	 * @param padStr  the String to pad the new String with, must not be null or empty
	 * @return centered String, {@code null} if null String input
	 * @throws IllegalArgumentException if padStr is {@code null} or empty
	 */
	public static String center(String str, final int size, String padStr) {
		if (str == null || size <= 0) {
			return str;
		}
		if (isEmpty(padStr)) {
			padStr = SPACE;
		}
		final int strLen = str.length();
		final int pads = size - strLen;
		if (pads <= 0) {
			return str;
		}
		str = leftPad(str, strLen + pads / 2, padStr);
		str = rightPad(str, size, padStr);
		return str;
	}

	// Case conversion
	//-----------------------------------------------------------------------
	/**
	 * <p>Converts a String to upper case as per {@link String#toUpperCase()}.</p>
	 *
	 * <p>A {@code null} input String returns {@code null}.</p>
	 *
	 * <pre>
	 * StringUtils.upperCase(null)  = null
	 * StringUtils.upperCase("")    = ""
	 * StringUtils.upperCase("aBc") = "ABC"
	 * </pre>
	 *
	 * <p><strong>Note:</strong> As described in the documentation for {@link String#toUpperCase()},
	 * the result of this method is affected by the current locale.
	 * For platform-independent case transformations, the method {@link #lowerCase(String, Locale)}
	 * should be used with a specific locale (e.g. {@link Locale#ENGLISH}).</p>
	 *
	 * @param str  the String to upper case, may be null
	 * @return the upper cased String, {@code null} if null String input
	 */
	public static String upperCase(final String str) {
		if (str == null) {
			return null;
		}
		return str.toUpperCase();
	}

	/**
	 * <p>Converts a String to upper case as per {@link String#toUpperCase(Locale)}.</p>
	 *
	 * <p>A {@code null} input String returns {@code null}.</p>
	 *
	 * <pre>
	 * StringUtils.upperCase(null, Locale.ENGLISH)  = null
	 * StringUtils.upperCase("", Locale.ENGLISH)    = ""
	 * StringUtils.upperCase("aBc", Locale.ENGLISH) = "ABC"
	 * </pre>
	 *
	 * @param str  the String to upper case, may be null
	 * @param locale  the locale that defines the case transformation rules, must not be null
	 * @return the upper cased String, {@code null} if null String input
	 * @since 2.5
	 */
	public static String upperCase(final String str, final Locale locale) {
		if (str == null) {
			return null;
		}
		return str.toUpperCase(locale);
	}

	/**
	 * <p>Converts a String to lower case as per {@link String#toLowerCase()}.</p>
	 *
	 * <p>A {@code null} input String returns {@code null}.</p>
	 *
	 * <pre>
	 * StringUtils.lowerCase(null)  = null
	 * StringUtils.lowerCase("")    = ""
	 * StringUtils.lowerCase("aBc") = "abc"
	 * </pre>
	 *
	 * <p><strong>Note:</strong> As described in the documentation for {@link String#toLowerCase()},
	 * the result of this method is affected by the current locale.
	 * For platform-independent case transformations, the method {@link #lowerCase(String, Locale)}
	 * should be used with a specific locale (e.g. {@link Locale#ENGLISH}).</p>
	 *
	 * @param str  the String to lower case, may be null
	 * @return the lower cased String, {@code null} if null String input
	 */
	public static String lowerCase(final String str) {
		if (str == null) {
			return null;
		}
		return str.toLowerCase();
	}

	/**
	 * <p>Converts a String to lower case as per {@link String#toLowerCase(Locale)}.</p>
	 *
	 * <p>A {@code null} input String returns {@code null}.</p>
	 *
	 * <pre>
	 * StringUtils.lowerCase(null, Locale.ENGLISH)  = null
	 * StringUtils.lowerCase("", Locale.ENGLISH)    = ""
	 * StringUtils.lowerCase("aBc", Locale.ENGLISH) = "abc"
	 * </pre>
	 *
	 * @param str  the String to lower case, may be null
	 * @param locale  the locale that defines the case transformation rules, must not be null
	 * @return the lower cased String, {@code null} if null String input
	 * @since 2.5
	 */
	public static String lowerCase(final String str, final Locale locale) {
		if (str == null) {
			return null;
		}
		return str.toLowerCase(locale);
	}

	/**
	 * <p>Capitalizes a String changing the first letter to title case as
	 * per {@link Character#toTitleCase(char)}. No other letters are changed.</p>
	 *
	 * <p>For a word based algorithm, see {@link org.apache.commons.lang3.text.WordUtils#capitalize(String)}.
	 * A {@code null} input String returns {@code null}.</p>
	 *
	 * <pre>
	 * StringUtils.capitalize(null)  = null
	 * StringUtils.capitalize("")    = ""
	 * StringUtils.capitalize("cat") = "Cat"
	 * StringUtils.capitalize("cAt") = "CAt"
	 * </pre>
	 *
	 * @param str the String to capitalize, may be null
	 * @return the capitalized String, {@code null} if null String input
	 * @see org.apache.commons.lang3.text.WordUtils#capitalize(String)
	 * @see #uncapitalize(String)
	 * @since 2.0
	 */
	public static String capitalize(final String str) {
		int strLen;
		if (str == null || (strLen = str.length()) == 0) {
			return str;
		}

		final char firstChar = str.charAt(0);
		if (Character.isTitleCase(firstChar)) {
			// already capitalized
			return str;
		}

		return new StringBuilder(strLen)
		.append(Character.toTitleCase(firstChar))
		.append(str.substring(1))
		.toString();
	}

	/**
	 * <p>Uncapitalizes a String changing the first letter to title case as
	 * per {@link Character#toLowerCase(char)}. No other letters are changed.</p>
	 *
	 * <p>For a word based algorithm, see {@link org.apache.commons.lang3.text.WordUtils#uncapitalize(String)}.
	 * A {@code null} input String returns {@code null}.</p>
	 *
	 * <pre>
	 * StringUtils.uncapitalize(null)  = null
	 * StringUtils.uncapitalize("")    = ""
	 * StringUtils.uncapitalize("Cat") = "cat"
	 * StringUtils.uncapitalize("CAT") = "cAT"
	 * </pre>
	 *
	 * @param str the String to uncapitalize, may be null
	 * @return the uncapitalized String, {@code null} if null String input
	 * @see org.apache.commons.lang3.text.WordUtils#uncapitalize(String)
	 * @see #capitalize(String)
	 * @since 2.0
	 */
	public static String uncapitalize(final String str) {
		int strLen;
		if (str == null || (strLen = str.length()) == 0) {
			return str;
		}

		final char firstChar = str.charAt(0);
		if (Character.isLowerCase(firstChar)) {
			// already uncapitalized
			return str;
		}

		return new StringBuilder(strLen)
		.append(Character.toLowerCase(firstChar))
		.append(str.substring(1))
		.toString();
	}

	/**
	 * <p>Swaps the case of a String changing upper and title case to
	 * lower case, and lower case to upper case.</p>
	 *
	 * <ul>
	 *  <li>Upper case character converts to Lower case</li>
	 *  <li>Title case character converts to Lower case</li>
	 *  <li>Lower case character converts to Upper case</li>
	 * </ul>
	 *
	 * <p>For a word based algorithm, see {@link org.apache.commons.lang3.text.WordUtils#swapCase(String)}.
	 * A {@code null} input String returns {@code null}.</p>
	 *
	 * <pre>
	 * StringUtils.swapCase(null)                 = null
	 * StringUtils.swapCase("")                   = ""
	 * StringUtils.swapCase("The dog has a BONE") = "tHE DOG HAS A bone"
	 * </pre>
	 *
	 * <p>NOTE: This method changed in Lang version 2.0.
	 * It no longer performs a word based algorithm.
	 * If you only use ASCII, you will notice no change.
	 * That functionality is available in org.apache.commons.lang3.text.WordUtils.</p>
	 *
	 * @param str  the String to swap case, may be null
	 * @return the changed String, {@code null} if null String input
	 */
	public static String swapCase(final String str) {
		if (StringUtils.isEmpty(str)) {
			return str;
		}

		final char[] buffer = str.toCharArray();

		for (int i = 0; i < buffer.length; i++) {
			final char ch = buffer[i];
			if (Character.isUpperCase(ch)) {
				buffer[i] = Character.toLowerCase(ch);
			} else if (Character.isTitleCase(ch)) {
				buffer[i] = Character.toLowerCase(ch);
			} else if (Character.isLowerCase(ch)) {
				buffer[i] = Character.toUpperCase(ch);
			}
		}
		return new String(buffer);
	}

	// Defaults
	//-----------------------------------------------------------------------
	/**
	 * <p>Returns either the passed in String,
	 * or if the String is {@code null}, an empty String ("").</p>
	 *
	 * <pre>
	 * StringUtils.defaultString(null)  = ""
	 * StringUtils.defaultString("")    = ""
	 * StringUtils.defaultString("bat") = "bat"
	 * </pre>
	 *
	 * @see ObjectUtils#toString(Object)
	 * @see String#valueOf(Object)
	 * @param str  the String to check, may be null
	 * @return the passed in String, or the empty String if it
	 *  was {@code null}
	 */
	public static String defaultString(final String str) {
		return str == null ? EMPTY : str;
	}

	/**
	 * <p>Returns either the passed in String, or if the String is
	 * {@code null}, the value of {@code defaultStr}.</p>
	 *
	 * <pre>
	 * StringUtils.defaultString(null, "NULL")  = "NULL"
	 * StringUtils.defaultString("", "NULL")    = ""
	 * StringUtils.defaultString("bat", "NULL") = "bat"
	 * </pre>
	 *
	 * @see ObjectUtils#toString(Object,String)
	 * @see String#valueOf(Object)
	 * @param str  the String to check, may be null
	 * @param defaultStr  the default String to return
	 *  if the input is {@code null}, may be null
	 * @return the passed in String, or the default if it was {@code null}
	 */
	public static String defaultString(final String str, final String defaultStr) {
		return str == null ? defaultStr : str;
	}

	/**
	 * <p>Returns either the passed in CharSequence, or if the CharSequence is
	 * whitespace, empty ("") or {@code null}, the value of {@code defaultStr}.</p>
	 *
	 * <pre>
	 * StringUtils.defaultIfBlank(null, "NULL")  = "NULL"
	 * StringUtils.defaultIfBlank("", "NULL")    = "NULL"
	 * StringUtils.defaultIfBlank(" ", "NULL")   = "NULL"
	 * StringUtils.defaultIfBlank("bat", "NULL") = "bat"
	 * StringUtils.defaultIfBlank("", null)      = null
	 * </pre>
	 * @param <T> the specific kind of CharSequence
	 * @param str the CharSequence to check, may be null
	 * @param defaultStr  the default CharSequence to return
	 *  if the input is whitespace, empty ("") or {@code null}, may be null
	 * @return the passed in CharSequence, or the default
	 * @see StringUtils#defaultString(String, String)
	 */
	public static <T extends CharSequence> T defaultIfBlank(final T str, final T defaultStr) {
		return isBlank(str) ? defaultStr : str;
	}

	/**
	 * <p>Returns either the passed in CharSequence, or if the CharSequence is
	 * empty or {@code null}, the value of {@code defaultStr}.</p>
	 *
	 * <pre>
	 * StringUtils.defaultIfEmpty(null, "NULL")  = "NULL"
	 * StringUtils.defaultIfEmpty("", "NULL")    = "NULL"
	 * StringUtils.defaultIfEmpty(" ", "NULL")   = " "
	 * StringUtils.defaultIfEmpty("bat", "NULL") = "bat"
	 * StringUtils.defaultIfEmpty("", null)      = null
	 * </pre>
	 * @param <T> the specific kind of CharSequence
	 * @param str  the CharSequence to check, may be null
	 * @param defaultStr  the default CharSequence to return
	 *  if the input is empty ("") or {@code null}, may be null
	 * @return the passed in CharSequence, or the default
	 * @see StringUtils#defaultString(String, String)
	 */
	public static <T extends CharSequence> T defaultIfEmpty(final T str, final T defaultStr) {
		return isEmpty(str) ? defaultStr : str;
	}

	// Reversing
	//-----------------------------------------------------------------------
	/**
	 * <p>Reverses a String as per {@link StringBuilder#reverse()}.</p>
	 *
	 * <p>A {@code null} String returns {@code null}.</p>
	 *
	 * <pre>
	 * StringUtils.reverse(null)  = null
	 * StringUtils.reverse("")    = ""
	 * StringUtils.reverse("bat") = "tab"
	 * </pre>
	 *
	 * @param str  the String to reverse, may be null
	 * @return the reversed String, {@code null} if null String input
	 */
	public static String reverse(final String str) {
		if (str == null) {
			return null;
		}
		return new StringBuilder(str).reverse().toString();
	}

	/**
	 * <p>
	 * Similar to <a
	 * href="http://www.w3.org/TR/xpath/#function-normalize-space">http://www.w3.org/TR/xpath/#function-normalize
	 * -space</a>
	 * </p>
	 * <p>
	 * The function returns the argument string with whitespace normalized by using
	 * <code>{@link #trim(String)}</code> to remove leading and trailing whitespace
	 * and then replacing sequences of whitespace characters by a single space.
	 * </p>
	 * In XML Whitespace characters are the same as those allowed by the <a
	 * href="http://www.w3.org/TR/REC-xml/#NT-S">S</a> production, which is S ::= (#x20 | #x9 | #xD | #xA)+
	 * <p>
	 * Java's regexp pattern \s defines whitespace as [ \t\n\x0B\f\r]
	 *
	 * <p>For reference:</p>
	 * <ul>
	 * <li>\x0B = vertical tab</li>
	 * <li>\f = #xC = form feed</li>
	 * <li>#x20 = space</li>
	 * <li>#x9 = \t</li>
	 * <li>#xA = \n</li>
	 * <li>#xD = \r</li>
	 * </ul>
	 *
	 * <p>
	 * The difference is that Java's whitespace includes vertical tab and form feed, which this functional will also
	 * normalize. Additionally <code>{@link #trim(String)}</code> removes control characters (char &lt;= 32) from both
	 * ends of this String.
	 * </p>
	 *
	 * @see Pattern
	 * @see #trim(String)
	 * @see <a
	 *      href="http://www.w3.org/TR/xpath/#function-normalize-space">http://www.w3.org/TR/xpath/#function-normalize-space</a>
	 * @param str the source String to normalize whitespaces from, may be null
	 * @return the modified string with whitespace normalized, {@code null} if null String input
	 *
	 * @since 3.0
	 */
	public static String normalizeSpace(final String str) {
		// LANG-1020: Improved performance significantly by normalizing manually instead of using regex
		// See https://github.com/librucha/commons-lang-normalizespaces-benchmark for performance test
		if (isEmpty(str)) {
			return str;
		}
		final int size = str.length();
		final char[] newChars = new char[size];
		int count = 0;
		int whitespacesCount = 0;
		boolean startWhitespaces = true;
		for (int i = 0; i < size; i++) {
			char actualChar = str.charAt(i);
			boolean isWhitespace = Character.isWhitespace(actualChar);
			if (!isWhitespace) {
				startWhitespaces = false;
				newChars[count++] = (actualChar == 160 ? 32 : actualChar);
				whitespacesCount = 0;
			} else {
				if (whitespacesCount == 0 && !startWhitespaces) {
					newChars[count++] = SPACE.charAt(0);
				}
				whitespacesCount++;
			}
		}
		if (startWhitespaces) {
			return EMPTY;
		}
		return new String(newChars, 0, count - (whitespacesCount > 0 ? 1 : 0));
	}

	/**
	 * <p>
	 * Wraps a string with a char.
	 * </p>
	 * 
	 * <pre>
	 * StringUtils.wrap(null, *)        = null
	 * StringUtils.wrap("", *)          = ""
	 * StringUtils.wrap("ab", '\0')     = "ab"
	 * StringUtils.wrap("ab", 'x')      = "xabx"
	 * StringUtils.wrap("ab", '\'')     = "'ab'"
	 * StringUtils.wrap("\"ab\"", '\"') = "\"\"ab\"\""
	 * </pre>
	 * 
	 * @param str
	 *            the string to be wrapped, may be {@code null}
	 * @param wrapWith
	 *            the char that will wrap {@code str}
	 * @return the wrapped string, or {@code null} if {@code str==null}
	 * @since 3.4
	 */
	public static String wrap(final String str, final char wrapWith) {

		if (isEmpty(str) || wrapWith == '\0') {
			return str;
		}

		return wrapWith + str + wrapWith;
	}

	/**
	 * <p>
	 * Wraps a String with another String.
	 * </p>
	 * 
	 * <p>
	 * A {@code null} input String returns {@code null}.
	 * </p>
	 * 
	 * <pre>
	 * StringUtils.wrap(null, *)         = null
	 * StringUtils.wrap("", *)           = ""
	 * StringUtils.wrap("ab", null)      = "ab"
	 * StringUtils.wrap("ab", "x")       = "xabx"
	 * StringUtils.wrap("ab", "\"")      = "\"ab\""
	 * StringUtils.wrap("\"ab\"", "\"")  = "\"\"ab\"\""
	 * StringUtils.wrap("ab", "'")       = "'ab'"
	 * StringUtils.wrap("'abcd'", "'")   = "''abcd''"
	 * StringUtils.wrap("\"abcd\"", "'") = "'\"abcd\"'"
	 * StringUtils.wrap("'abcd'", "\"")  = "\"'abcd'\""
	 * </pre>
	 * 
	 * @param str
	 *            the String to be wrapper, may be null
	 * @param wrapWith
	 *            the String that will wrap str
	 * @return wrapped String, {@code null} if null String input
	 * @since 3.4
	 */
	public static String wrap(final String str, final String wrapWith) {

		if (isEmpty(str) || isEmpty(wrapWith)) {
			return str;
		}

		return wrapWith.concat(str).concat(wrapWith);
	}
}