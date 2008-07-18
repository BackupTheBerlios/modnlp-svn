TOPDIR=
SOURCEDIR=$(TOPDIR)src/
LIBSPATH=$(TOPDIR)lib/gnu-regexp.jar:$(TOPDIR)lib/je.jar:$(TOPDIR)lib/MinML2.jar:$(TOPDIR)lib/jung.jar:$(TOPDIR)lib/antlr-2.7.6.jar:$(TOPDIR)lib/commons-pool-1.2.jar:$(TOPDIR)lib/exist-modules.jar:$(TOPDIR)lib/exist.jar:$(TOPDIR)lib/jgroups-all.jar:$(TOPDIR)lib/log4j-1.2.14.jar:$(TOPDIR)lib/resolver.jar:$(TOPDIR)lib/sunxacml.jar:$(TOPDIR)lib/xmldb.jar:$(TOPDIR)lib/xmlrpc-1.2-patched.jar
LIBS=$(subst :, ,$(LIBSPATH))
DOCS=$(TOPDIR)doc
DATA=$(TOPDIR)data
SOURCES:= $(shell find $(SOURCEDIR) -name *.java)
TARGETS=$(subst .java,.class,$(SOURCES))
JARREDS=$(subst $(SOURCEDIR),,$(TARGETS))
PRJFILES:=`find $(SOURCEDIR) -name prj.el`
DISTDIR:=modnlp-`cat VERSION`
BINDISTDIR=$(DISTDIR)-bin
DISTFILES=AUTHORS COPYING Makefile README VERSION BUGS INSTALL TODO exclude.txt \
	$(LIBS) $(DOCS) $(DATA) $(SOURCES) 
BINDISTFILES=$(LIBS) lib/teccli.jar lib/tecser.jar lib/idx.jar lib/tc.jar tecser/lib/server.properties \
	src/teclipluginlist.txt
#JAVA=jamvm
#JAVAC=jikes-classpath
JAVA=java
JAVAC=javac
JAR=jar
JAVAFLAGS=-classpath $(SOURCEDIR):$(LIBSPATH)
JAVACFLAGS=-encoding ISO-8859-1 -classpath $(SOURCEDIR):$(LIBSPATH)

#VPATH=$(SOURCEDIR)

.PHONY: build jar test docs dist

all: build jar test dist

%.class: %.java
		$(JAVAC) $(JAVACFLAGS) $?

build: $(TARGETS)

clean:
	find . -iregex '.*\(\.aux\|\.log\|\.dvi\|\.class\|~\|#.*#\b\)'  -type f  -print0 |xargs -0 -e rm -f

jar: $(TARGETS)
	cd $(SOURCEDIR) && $(JAR) cfv ../$(TOPDIR)lib/modnlp.jar $(JARREDS)

docs: $(TARGETS)
	javadoc -encoding "ISO-8859-1" -d $(DOCS) -sourcepath $(SOURCEDIR) -classpath $(SOURCEDIR):$(LIBSPATH) -subpackages modnlp

dist: clean
	rm -rf $(DISTDIR)
	mkdir $(DISTDIR)
	cp -aL --parents $(DISTFILES) $(DISTDIR)
	tar cfvz /tmp/$(DISTDIR).tar.gz  --exclude-from=exclude.txt $(DISTDIR)
	rm -rf $(DISTDIR)

bindist:
	cd idx/ && make jar
	cd tecser/ && make jar
	cd teccli/ && make jar
	cd tc/ && make jar
	mkdir $(BINDISTDIR)
	cp README.bin $(BINDISTDIR)/README
	cp $(BINDISTFILES) $(BINDISTDIR)
	tar cfvz /tmp/$(BINDISTDIR).tar.gz  --exclude-from=exclude.txt $(BINDISTDIR)
	rm -rf $(BINDISTDIR)



nosvn: clean
        tar cfvz /tmp/modnlp.tgz --exclude-from=exclude.txt .
