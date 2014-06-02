/*
 * LLSDJ - LLSD in Java example
 *
 * Copyright(C) 2008 University of St. Andrews
 */

package uk.ac.stand.llsdj;

/**
 * Represents an error while parsing/generating LLSD.
 */
public class LLSDException extends SecondLifeException {
    public  LLSDException(final String message) {
        super(message);
    }

    public  LLSDException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
