/* Copyright (c) 2008 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package com.google.gdata.util.common.net;

import com.google.gdata.util.common.base.CharEscapers;
import com.google.gdata.util.common.base.CharMatcher;
import com.google.gdata.util.common.base.Charsets;
import com.google.gdata.util.httputil.FastURLEncoder;
import static com.google.gdata.util.common.base.Preconditions.checkNotNull;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.Charset;

/**
 * Implements <a href="http://en.wikipedia.org/wiki/Percent-encoding"
 * >percent-encoding</a>, specifying how to encode non-US-ASCII and reserved
 * characters in URIs.
 *
 * <p>Per Section 2.1 of <a href="http://tools.ietf.org/html/rfc3986">RFC
 * 3986</a>, URIs should contain only characters that are part of US-ASCII, and
 * some characters are further reserved to delimit components or subcomponents;
 * therefore, characters that are outside the allowed set need to be encoded.
 * This is done using the escape sequence "%<i>XX</i>" where <i>XX</i> is the
 * hexadecimal value of the bytewise representation of the character.
 *
 * <p>This encoding format is used for the application/x-www-form-urlencoded
 * content type, as defined by section 17.13.4 of the W3C's <a
 * href="http://www.w3.org/TR/REC-html40/interact/forms.html#h-17.13.4.1">HTML
 * 4.01 Specification</a>.
 *
 * <p>For example, the Unicode string "flamb&#xe9;" is represented as the byte
 * sequence {@code [0x66, 0x6c, 0x61, 0x6d, 0x62, 0xe9]} in ISO-8859-1. In
 * UTF-8, it is represented as {@code [0x66, 0x6c, 0x61, 0x6d, 0x62, 0xc3,
 * 0xa9]}. The first five characters are unreserved and do not require encoding,
 * but the last character is not, so the URI representation is "flamb%E9" in
 * ISO-8859-1 and "flamb%C3%A9" in UTF-8. Escape sequences are not
 * case-sensitive.
 *
 * @see Uri
 * 
 */
public final class UriEncoder {
  private UriEncoder() {}

  /**
   * The default character encoding, UTF-8, per Section 2.5 of <a
   * href="http://tools.ietf.org/html/rfc3986">RFC 3986</a>.
   *
   * @see Charsets
   */
  public static final Charset DEFAULT_ENCODING = Charsets.UTF_8;

  /** CharMatcher to match '%' or '+'. */
  private static final CharMatcher PERCENT_OR_PLUS = CharMatcher.anyOf("%+");

  /**
   * Percent-encodes a Unicode string into a US-ASCII string. The {@link
   * #DEFAULT_ENCODING}, UTF-8, is used to determine how non-US-ASCII and
   * reserved characters should be represented as consecutive sequences of the
   * form "%<i>XX</i>".
   *
   * <p>This replaces '&nbsp;' with '+'.  So this method should not be
   * used for non application/x-www-form-urlencoded strings such as
   * host and path.
   *
   * @param string a Unicode string
   * @return a percent-encoded US-ASCII string
   * @throws NullPointerException if {@code string} is null
   */
  public static String encode(String string) {
    return CharEscapers.uriEscaper().escape(string);
  }

  /**
   * Percent-encodes a Unicode string into a US-ASCII string. The specified
   * encoding is used to determine how non-US-ASCII and reserved characters
   * should be represented as consecutive sequences of the form "%<i>XX</i>".
   *
   * <p>This replaces '&nbsp;' with '+'.  So this method should not be
   * used for non application/x-www-form-urlencoded strings such as
   * host and path.
   *
   * @param string a Unicode string
   * @param encoding a character encoding
   * @return a percent-encoded US-ASCII string
   * @throws NullPointerException if any argument is null
   */
  public static String encode(String string, Charset encoding) {
    checkNotNull(string);
    checkNotNull(encoding);
    // encoding parameter
    if (encoding.equals(DEFAULT_ENCODING)) {
      return encode(string);
    } else {
      try {
        return FastURLEncoder.encode(string, encoding.name());
      } catch (UnsupportedEncodingException impossible) {
        // We know we have a valid encoding name since we got it from a Charset
        // instance
        throw new AssertionError(impossible);
      }
    }
  }

  /**
   * Percent-decodes a US-ASCII string into a Unicode string. The {@link
   * #DEFAULT_ENCODING}, UTF-8, is used to determine what characters are
   * represented by any consecutive sequences of the form "%<i>XX</i>".
   *
   * <p>This replaces '+' with '&nbsp;'.  So this method should not be
   * used for non application/x-www-form-urlencoded strings such as
   * host and path.
   *
   * @param string a percent-encoded US-ASCII string
   * @return a Unicode string
   * @throws NullPointerException if {@code string} is null
   */
  public static String decode(String string) {
    return decode(string, DEFAULT_ENCODING);
  }

  /**
   * Percent-decodes a US-ASCII string into a Unicode string. The specified
   * encoding is used to determine what characters are represented by any
   * consecutive sequences of the form "%<i>XX</i>". This is the strict
   * kind of decoding, that will throw an exception if any "%XX" sequence
   * encountered is invalid (for example, "%HH").
   *
   * <p>This replaces '+' with '&nbsp;'.  So this method should not be
   * used for non application/x-www-form-urlencoded strings such as
   * host and path.
   *
   * @param string a percent-encoded US-ASCII string
   * @param encoding a character encoding
   * @return a Unicode string
   * @throws NullPointerException if any argument is null
   * @throws RuntimeException if any the decoding failed because some %
   *         sequence above is invalid (for example, "%HH")
   */
  public static String decode(String string, Charset encoding) {
    checkNotNull(string);
    checkNotNull(encoding);
    try {
      return URLDecoder.decode(string, encoding.name());
    } catch (UnsupportedEncodingException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Percent-decodes a US-ASCII string into a Unicode string. The specified
   * encoding is used to determine what characters are represented by any
   * consecutive sequences of the form "%<i>XX</i>". This is the lenient
   * kind of decoding that will simply ignore and copy as-is any "%XX"
   * sequence that is invalid (for example, "%HH"). This code was lifted
   * as-is from {@link java.net.URLDecoder#decode(String,String)}, and
   * slightly modified.
   *
   * @param string a percent-encoded US-ASCII string
   * @param encoding a character encoding
   * @param decodePlus boolean to indicate whether to decode '+' as ' '
   * @return a Unicode string
   * @throws NullPointerException if any argument is null
   */
  public static String lenientDecode(
      String string, Charset encoding, boolean decodePlus) {
    checkNotNull(encoding);
    int i;
    if (decodePlus) {
      i = PERCENT_OR_PLUS.indexIn(string);
    } else {
      i = string.indexOf('%');
    }
    if (i < 0) {
      return string;
    }

    int length = string.length();
    StringBuilder buffer =
        new StringBuilder(length > 500 ? length / 2 : length);
    if (i > 0) {
      buffer.append(string.substring(0, i));
    }
    byte[] bytes = null;

    while (i < length) {
      char c = string.charAt(i);
      switch (c) {
        case '+':
          if (decodePlus) {
            buffer.append(' ');
          } else {
            buffer.append(c);
          }
          i++;
          break;
        case '%':
          /*
           * Starting with this instance of %, process a substring by
           * the end of the string, a non-ASCII letter, or '+'.  Each
           * substring %xy will yield a byte.  An ASCII character will
           * also yield a byte.  Convert all consecutive bytes
           * obtained this way to whatever character(s) they represent
           * in the provided encoding. If the conversion fails for any
           * reason, ignore and skip over this section.  This is the
           * key difference between this version of decode and the
           * URLEncode.decode() version upon which it is based.
           */
          if (bytes == null) {
            bytes = new byte[length - i];
          }

          int position = 0;
          // Iterate by
          //  - the end of the string,
          //  - non-ASCII letter, or
          //  - '+' if decodePlus
          while (i < length) {
            c = string.charAt(i);
            if (decodePlus && c == '+' || c >= 0x80) {
              break;
            } else if (c == '%' && i + 2 < length) {
              try {
                bytes[position] = (byte) Integer.parseInt(
                    string.substring(i + 1, i + 3), 16);
                position++;
                i += 3;
              } catch (NumberFormatException nfe) {
                // If we have an incomplete or invalid byte encoding
                // such as "%F" or "%FH", then treat everything as
                // characters
                bytes[position++] = (byte) '%';
                i++;
              }
            } else {
              // Unescaped ASCII letter
              bytes[position++] = (byte) c;
              i++;
            }
          }

          // We decoded at least one byte
          if (position > 0) {
            try {
              buffer.append(new String(bytes, 0, position, encoding.name()));
            } catch (UnsupportedEncodingException e) {
              throw new RuntimeException(e);
            }
          }
          break;

        default:
          buffer.append(c);
          i++;
          break;
      }
    }

    return buffer.toString();
  }
}