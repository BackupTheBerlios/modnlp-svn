TOPDIR=
SOURCEDIR=$(TOPDIR)src/
LIBSPATH=$(TOPDIR)lib/gnu-regexp-1.1.4.jar:$(TOPDIR)lib/je.jar:$(TOPDIR)lib/MinML2.jar
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
JAVACFLAGS=-classpath $(SOURCEDIR):$(LIBSPATH)

#VPATH=$(SOURCEDIR)

.PHONY: build jar test docs dist

all: build jar test dist

%.class: %.java
		$(JAVAC) $(JAVAFLAGS) $?

build: $(TARGETS)

clean:
	find . -iregex '.*\(\.aux\|\.log\|\.dvi\|\.class\|~\|#.*#\b\)'  -type f  -print0 |xargs -0 -e rm -f

jar: $(TARGETS)
	cd $(SOURCEDIR) && $(JAR) cfv ../$(TOPDIR)lib/modnlp.jar $(JARREDS)

docs: $(TARGETS)
	javadoc -d $(DOCS) -sourcepath $(SOURCEDIR) -classpath $(SOURCEDIR):$(LIBSPATH) -subpackages modnlp

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
