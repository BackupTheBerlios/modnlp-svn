TOPDIR=
SOURCEDIR=$(TOPDIR)src/
LIBSPATH=$(TOPDIR)lib/je.jar:$(TOPDIR)lib/MinML2.jar:lib/idx.jar
LIBS=$(subst :, ,$(LIBSPATH))
DOCS=$(TOPDIR)doc
DATA=$(TOPDIR)data
MANIFEST=teccli-manifest.txt
SOURCES:= $(shell find $(SOURCEDIR)modnlp/tec/client -name *.java)
TARGETS=$(subst .java,.class,$(SOURCES))
JARREDS=modnlp/tec/client/*.class modnlp/tec/client/gui/*.class \
	modnlp/tec/client/icons/*.{gif,jpg,png} \
	modnlp/tec/client/help/*.{html,jpg} tecli.properties
PRJFILES:=`find $(SOURCEDIR) -name prj.el`
DISTDIR:=modnlp-teccli-`cat VERSION`
DISTFILES=AUTHORS COPYING Makefile README VERSION BUGS INSTALL TODO exclude.txt \
	$(LIBS) $(DOCS) $(DATA) $(SOURCEDIR)tecli.properties* \
	$(SOURCEDIR)modnlp/tec/client $(SOURCEDIR)$(MANIFEST) $(SOURCEDIR)modnlp/tec/prj.el
#JAVA=jamvm
#JAVAC=jikes-classpath
JAVA=java
JAVAC=javac
JAR=jar
JARFLAGS= cvmf $(MANIFEST)
JAVAFLAGS=-classpath $(SOURCEDIR):$(LIBSPATH)
JAVACFLAGS=-classpath $(SOURCEDIR):$(LIBSPATH)
JARTARGET=$(TOPDIR)lib/teccli.jar

BINDISTDIR=$(DISTDIR)-bin
#VPATH=$(SOURCEDIR)

.PHONY: build jar test docs dist

all: build jar test 

%.class: %.java
		$(JAVAC) $(JAVAFLAGS) $?

build: $(TARGETS)

clean:
	find . -iregex '.*\(\.aux\|\.log\|\.dvi\|\.class\|~\|#.*#\b\)'  -type f  -print0 |xargs -0 -e rm -f

jar: $(TARGETS)
	cd $(SOURCEDIR) && $(JAR) $(JARFLAGS) ../$(JARTARGET) $(JARREDS)

docs: $(TARGETS)
	javadoc -d $(DOCS) -sourcepath $(SOURCEDIR) -classpath $(SOURCEDIR):$(LIBSPATH) -subpackages modnlp.tec.client 

## distribution building, the joy of life...
dist: docs
	rm -rf $(DISTDIR)
	mkdir $(DISTDIR)
	cp -aL --parents $(DISTFILES) $(DISTDIR)
	tar cfvz /tmp/$(DISTDIR).tar.gz  --exclude-from=exclude.txt $(DISTDIR)
	rm -rf $(DISTDIR)

bindist: $(TARGETS)
	mkdir $(BINDISTDIR)
	cp README.bin $(BINDISTDIR)/README
	cp $(LIBS) $(JARTARGET) $(BINDISTDIR)
	tar cfvz /tmp/$(BINDISTDIR).tar.gz  --exclude-from=exclude.txt $(BINDISTDIR)
	rm -rf $(BINDISTDIR)