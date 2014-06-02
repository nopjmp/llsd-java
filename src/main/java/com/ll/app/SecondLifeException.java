/*
 * LLSDJ - LLSD in Java example
 *
 * Copyright(C) 2008 University of St. Andrews
 */

package uk.ac.stand.llsdj;

public class SecondLifeException extends Exception {
    public  SecondLifeException(final String message) {
        super(message);
    }

    public  SecondLifeException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
