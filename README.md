llsd-java
=========

This is a java implementation of [LLSD](http://wiki.secondlife.com/wiki/LLSD) originally by [@Xugumad](https://github.com/Xugumad) and adopted by [@jacobilinden](https://github.com/jacobilinden).
The source for the package is hosted on [Github](https://github.com/jacobilinden/llsd-java).

This package can be modifed by git cloning it and tweaking it.  Use ```mvn package``` to build your jars and ```mvn deploy``` to
upload it github as a valid maven repository. 

INSTALL
=======
[![Clojars Project](http://clojars.org/lindenlab/llsd/latest-version.svg)](http://clojars.org/lindenlab/llsd)

USAGE
=====

Beware that llsd+notation is not supported and [@Xugumad](https://github.com/Xugumad) has philosophical objections to the binary representations of LLSD in xml:
```java
throw new LLSDException("\"binary\" node type not implemented because it's a stupid idea that breaks how XML works. In specific, XML has a character set, binary data does not, and mixing the two is a recipe for disaster. Linden Labs should have used base 64 encode if they absolutely must, or attached binary content using a MIME multipart type.");
```
