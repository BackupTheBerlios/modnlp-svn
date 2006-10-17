TOPDIR=
SOURCEDIR=$(TOPDIR)src/
LIBSPATH=$(TOPDIR)lib/je.jar:$(TOPDIR)lib/MinML2.jar:lib/idx.jar
LIBS=$(subst :, ,$(LIBSPATH))
DOCS=$(TOPDIR)doc
DATA=$(TOPDIR)data
MANIFEST=teccli-manifest.txt
SOURCES:= $(shell find $(SOURCEDIR)modnlp/tec/client -name *.java)
TARGETS=$(subst .java,.class,$(SOURCES))
JARREDS=modnlp/tec/client/*.class	modnlp/tec/client/icons \
	modnlp/tec/client/help tecli.properties
PRJFILES:=`find $(SOURCEDIR) -name prj.el`
DISTDIR:=teccli-`cat VERSION`
DISTFILES=AUTHORS COPYING Makefile README VERSION BUGS INSTALL TODO exclude.txt \
	$(LIBS) $(DOCS) $(DATA) $(SOURCEDIR)tecli.properties* \
	$(SOURCEDIR)modnlp/tec/client $(SOURCEDIR)$(MANIFEST) $(SOURCEDIR)modnlp/tec/prj.el
#JAVA=jamvm
#JAVAC=jikes-classpath
JAVA=java
JAVAC=javac
JAR=jar
JARFLAGS= cvmf $(MANIFEST)
JAVAFLAGS=-classpath $(SOURCEDIR):$(LIBSPATH) -Dfile.encoding=ISO8859_1
JAVACFLAGS=-classpath $(SOURCEDIR):$(LIBSPATH)

#VPATH=$(SOURCEDIR)

.PHONY: build jar test docs dist

build: $(TARGETS)

all: build jar test 

%.class: %.java
		$(JAVAC) $(JAVACFLAGS) $?

clean:
	find . -iregex '.*\(\.aux\|\.log\|\.dvi\|\.class\|~\|#.*#\b\)'  -type f  -print0 |xargs -0 -e rm -f

jar: $(TARGETS)
	cd $(SOURCEDIR) && $(JAR) $(JARFLAGS) ../$(TOPDIR)lib/teccli.jar $(JARREDS)

docs: $(TARGETS)
	javadoc -d $(DOCS) -sourcepath $(SOURCEDIR) -classpath $(SOURCEDIR):$(LIBSPATH) -subpackages modnlp.tec.client 

test: $(TARGETS)
	@echo "Running stand-alone browser (/tmp/tec/index must have been created by idx)..."
	$(JAVA) $(JAVAFLAGS) modnlp.tec.client.Browser -standalone 

dist: docs
	rm -rf $(DISTDIR)
	mkdir $(DISTDIR)
	cp -aL --parents $(DISTFILES) $(DISTDIR)
	tar cfvz /tmp/$(DISTDIR).tar.gz  --exclude-from=exclude.txt $(DISTDIR)
	rm -rf $(DISTDIR)
