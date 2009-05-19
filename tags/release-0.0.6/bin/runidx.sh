#!/bin/sh
cd ../lib
java -Xms400m -cp .:idx.jar:antlr-2.7.6.jar:commons-pool-1.2.jar:exist-modules.jar:exist.jar:gnu-regexp.jar:idx.jar:je.jar:jgroups-all.jar:log4j-1.2.14.jar:resolver.jar:sunxacml.jar:xmldb.jar:xmlrpc-1.2-patched.jar modnlp.idx.IndexManager $@
