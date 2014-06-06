/*
 * LLSDJ - LLSD in Java example
 *
 * Copyright(C) 2008 University of St. Andrews
 */

package lindenlab.llsd;

/**
 * Represents an undefined value in LLSD. From my reading of the docs, this is
 * different to null (no value), in that it says nothing about the value at
 * all, rather than that there is no value, or that the value is 0.
 *
 * See http://wiki.secondlife.com/w/index.php?title=LLSD&oldid=65579#undefined
 * for more information.
 */
public enum LLSDUndefined {
    BOOLEAN,
    BINARY,
    DATE,
    INTEGER,
    REAL,
    STRING,
    URI,
    UUID
}
