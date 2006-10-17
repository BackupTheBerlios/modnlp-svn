TOPDIR=
SOURCEDIR=$(TOPDIR)src/
LIBSPATH=$(TOPDIR)lib/gnu-regexp-1.1.4.jar:$(TOPDIR)lib/je.jar
LIBS=$(subst :, ,$(LIBSPATH))
DOCS=$(TOPDIR)doc
DATA=$(TOPDIR)data
SOURCES=$(SOURCEDIR)modnlp/idx/MakeTECIndex.java \
	$(SOURCEDIR)modnlp/idx/ConsumerQuery.java \
	$(SOURCEDIR)modnlp/idx/Dump.java \
	$(SOURCEDIR)modnlp/idx/Query.java \
	$(SOURCEDIR)modnlp/idx/RemoveFileFromIndex.java \
	$(SOURCEDIR)modnlp/util/PrintUtil.java \
	$(SOURCEDIR)modnlp/util/LogStream.java \
	$(SOURCEDIR)modnlp/util/Tokeniser.java \
	$(SOURCEDIR)modnlp/dstruct/CorpusList.java \
  $(SOURCEDIR)modnlp/dstruct/StringSet.java \
	$(SOURCEDIR)modnlp/dstruct/WordForms.java \
	$(SOURCEDIR)modnlp/dstruct/WordFrequencyPair.java \
	$(SOURCEDIR)modnlp/dstruct/CorpusFile.java \
	$(SOURCEDIR)modnlp/dstruct/IntegerSet.java \
	$(SOURCEDIR)modnlp/dstruct/StringSet.java \
	$(SOURCEDIR)modnlp/dstruct/PositionSet.java \
	$(SOURCEDIR)modnlp/dstruct/TokenMap.java \
	$(SOURCEDIR)modnlp/dstruct/IntOffsetArray.java \
	$(SOURCEDIR)modnlp/idx/database/AlreadyIndexedException.java \
  $(SOURCEDIR)modnlp/idx/database/CaseTable.java \
	$(SOURCEDIR)modnlp/idx/database/DescIntComparator.java \
	$(SOURCEDIR)modnlp/idx/database/DictProperties.java \
	$(SOURCEDIR)modnlp/idx/database/Dictionary.java \
	$(SOURCEDIR)modnlp/idx/database/FileTable.java \
	$(SOURCEDIR)modnlp/idx/database/FreqKeyCreator.java \
	$(SOURCEDIR)modnlp/idx/database/FreqTable.java \
	$(SOURCEDIR)modnlp/idx/database/IntegerSetBinding.java \
	$(SOURCEDIR)modnlp/idx/database/IntOffsetArrayBinding.java \
	$(SOURCEDIR)modnlp/idx/database/NotIndexedException.java \
	$(SOURCEDIR)modnlp/idx/database/StringIntKey.java \
	$(SOURCEDIR)modnlp/idx/database/StringIntKeyBinding.java \
	$(SOURCEDIR)modnlp/idx/database/StringSetBinding.java \
	$(SOURCEDIR)modnlp/idx/database/Table.java \
	$(SOURCEDIR)modnlp/idx/database/TPosTable.java \
	$(SOURCEDIR)modnlp/idx/database/WordFileTable.java \
	$(SOURCEDIR)modnlp/idx/database/WordPositionTable.java \
	$(SOURCEDIR)modnlp/idx/inverted/TokeniserGNU.java \
	$(SOURCEDIR)modnlp/idx/inverted/TokeniserRegex.java \
	$(SOURCEDIR)modnlp/idx/query/WordQuery.java \
	$(SOURCEDIR)modnlp/idx/query/Horizon.java \
	$(SOURCEDIR)modnlp/idx/query/PrepContextQuery.java \
	$(SOURCEDIR)modnlp/idx/query/WordQueryException.java
TARGETS=$(subst .java,.class,$(SOURCES))
JARREDS=$(subst $(SOURCEDIR),,$(TARGETS))
DISTDIR:=idx-`cat VERSION`
DISTFILES=AUTHORS COPYING Makefile README VERSION BUGS INSTALL TODO exclude.txt \
	$(LIBS) $(DOCS) $(DATA) $(SOURCES) $(SOURCEDIR)modnlp/idx/prj.el
#JAVA=jamvm
#JAVAC=jikes-classpath
JAVA=java
JAVAC=javac
JAR=jar
JAVAFLAGS=-classpath $(SOURCEDIR):$(LIBSPATH) -Dfile.encoding=ISO8859_1 
JAVACFLAGS=-classpath $(SOURCEDIR):$(LIBSPATH)

#VPATH=$(SOURCEDIR)

.PHONY: build jar test docs dist


build: $(TARGETS)

all: build jar test dist

%.class: %.java
		$(JAVAC) $(JAVACFLAGS) $?

clean:
	find . -iregex '.*\(\.aux\|\.log\|\.dvi\|\.class\|~\|#.*#\b\)'  -type f  -print0 |xargs -0 -e rm -f

jar: $(TARGETS)
	cd $(SOURCEDIR) && $(JAR) cfv ../$(TOPDIR)lib/idx.jar $(JARREDS)

test: $(TARGETS)
	@echo "Testing Indexer..."
	-rm -rf /tmp/tec/index ; mkdir -p /tmp/tec/index 
	@echo "** Indexing data/short.lst"
	$(JAVA) $(JAVAFLAGS) modnlp.idx.MakeTECIndex $(TOPDIR)data/short.lst 
	@echo "** Dumping index..."
	$(JAVA) $(JAVAFLAGS) modnlp.idx.Dump 
	@echo "** Testing completed"

docs: $(TARGETS)
	javadoc -d $(DOCS) -sourcepath $(SOURCEDIR) -classpath $(SOURCEDIR):$(LIBSPATH) -subpackages modnlp.idx

dist: docs clean
	rm -rf $(DISTDIR)
	mkdir $(DISTDIR)
	cp -aL --parents $(DISTFILES) $(DISTDIR)
	tar cfvz /tmp/$(DISTDIR).tar.gz  --exclude-from=exclude.txt $(DISTDIR)
	rm -rf $(DISTDIR)
