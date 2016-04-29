package com.cyberwalkabout.common.util;


/**
 * The ParseException indicates a failure parsing GData request content.
 *
 * 
 */
public class ParseException extends Exception {

  /**
	 * 
	 */
	private static final long serialVersionUID = 8120262931570969604L;

public ParseException(String message) {
    super(message);
  }

  public ParseException(String message,
                        Throwable cause) {
    super(message, cause);
  }

  public ParseException(Throwable cause) {
    super(cause);
  }
}
