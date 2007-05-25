TOPDIR=
SOURCEDIR=$(TOPDIR)src/
LIBSPATH=$(TOPDIR)lib/gnu-regexp.jar:$(TOPDIR)lib/je.jar
LIBS=$(subst :, ,$(LIBSPATH))
LIBLICENSES=$(subst .jar,-COPYING ,$(LIBS))
DOCS=$(TOPDIR)doc
DATA=$(TOPDIR)data
SOURCES=$(SOURCEDIR)modnlp/util/PrintUtil.java \
	$(SOURCEDIR)modnlp/util/LogStream.java \
	$(SOURCEDIR)modnlp/util/Tokeniser.java \
	$(SOURCEDIR)modnlp/util/IOUtil.java \
	$(SOURCEDIR)modnlp/util/XMLSplitter.java \
	$(SOURCEDIR)modnlp/dstruct/CorpusList.java \
	$(SOURCEDIR)modnlp/dstruct/CorpusFile.java \
	$(SOURCEDIR)modnlp/dstruct/IntegerSet.java \
	$(SOURCEDIR)modnlp/dstruct/IntOffsetArray.java \
	$(SOURCEDIR)modnlp/dstruct/PositionSet.java \
	$(SOURCEDIR)modnlp/dstruct/StopWordList.java \
	$(SOURCEDIR)modnlp/dstruct/StringSet.java \
	$(SOURCEDIR)modnlp/dstruct/TokenMap.java \
	$(SOURCEDIR)modnlp/dstruct/WordForms.java \
	$(SOURCEDIR)modnlp/dstruct/WordFrequencyPair.java \
	$(SOURCEDIR)modnlp/idx/database/AlreadyIndexedException.java \
	$(SOURCEDIR)modnlp/idx/database/CaseTable.java \
	$(SOURCEDIR)modnlp/idx/database/DescIntComparator.java \
	$(SOURCEDIR)modnlp/idx/database/DictProperties.java \
	$(SOURCEDIR)modnlp/idx/database/Dictionary.java \
	$(SOURCEDIR)modnlp/idx/database/EmptyFileException.java \
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
	$(SOURCEDIR)modnlp/idx/ConsumerQuery.java \
	$(SOURCEDIR)modnlp/idx/Dump.java \
	$(SOURCEDIR)modnlp/idx/MakeTECIndex.java \
	$(SOURCEDIR)modnlp/idx/Query.java \
	$(SOURCEDIR)modnlp/idx/IndexManager.java \
	$(SOURCEDIR)modnlp/idx/IndexManagerProperties.java \
	$(SOURCEDIR)modnlp/idx/RemoveFileFromIndex.java \
	$(SOURCEDIR)modnlp/idx/gui/CorpusChooser.java \
	$(SOURCEDIR)modnlp/idx/gui/CorpusFilesChooser.java \
	$(SOURCEDIR)modnlp/idx/gui/HeaderURLChooser.java \
	$(SOURCEDIR)modnlp/idx/gui/IndexManagerUI.java \
	$(SOURCEDIR)modnlp/idx/query/Horizon.java \
	$(SOURCEDIR)modnlp/idx/query/PrepContextQuery.java \
	$(SOURCEDIR)modnlp/idx/query/WordQuery.java \
	$(SOURCEDIR)modnlp/idx/query/WordQueryException.java
TARGETS=$(subst .java,.class,$(SOURCES))
MAINJARS=$(subst $(SOURCEDIR),,$(TARGETS)) # doesn't work by itself: need to jar inner classess as well
JARREDS=$(MAINJARS) \
	'modnlp/idx/ConsumerQuery$$ConcordanceProducer.class' \
	'modnlp/idx/IndexManager$$DeindexingThread.class' \
	'modnlp/idx/IndexManager$$IndexingThread.class'
DISTDIR:=modnlp-idx-`cat VERSION`
BINDISTDIR=$(DISTDIR)-bin
BINDISTXTRAS=$(LIBLICENSES) $(TOPDIR)*COPYING*
DISTFILES=AUTHORS COPYING Makefile README VERSION BUGS INSTALL TODO exclude.txt \
	$(LIBS) $(DOCS) $(DATA) $(SOURCES) $(SOURCEDIR)modnlp/idx/prj.el $(SOURCEDIR)idx-manifest.txt
#JAVA=jamvm
JAVAC=jikes-classpath
JAVA=java
#JAVAC=javac
JAR=jar
JAVAFLAGS=-classpath $(SOURCEDIR):$(LIBSPATH)
JAVACFLAGS=-classpath $(SOURCEDIR):$(LIBSPATH)
MANIFEST=idx-manifest.txt
JARFLAGS= cvmf $(MANIFEST)
JARTARGET=$(TOPDIR)lib/idx.jar
#VPATH=$(SOURCEDIR)

.PHONY: build jar test docs dist

all: build jar test

%.class: %.java
		$(JAVAC) $(JAVAFLAGS) $?

build: $(TARGETS)

clean:
	find -L . -iregex '.*\(\.aux\|\.log\|\.dvi\|\.class\|~\|#.*#\b\)' -type f -print0 |xargs -0 -e rm -f

jar: $(TARGETS)
	cd $(SOURCEDIR) && $(JAR)  $(JARFLAGS) ../$(JARTARGET) $(JARREDS)

test: $(TARGETS)
	@echo "Testing Indexer..."
	-mkdir /tmp/datamodnlptest
	-cp -a data/* /tmp/datamodnlptest/
	-rm -rf /tmp/tec/index ; mkdir -p /tmp/tec/index 
	@echo "** Indexing datamodnlptest/short.lst"
	$(JAVA) $(JAVAFLAGS) modnlp.idx.MakeTECIndex /tmp/tec/index $(TOPDIR)data/short.lst 
	@echo "** Dumping index..."
	$(JAVA) $(JAVAFLAGS) modnlp.idx.Dump /tmp/tec/index
	@echo "** Testing completed"

docs: $(TARGETS)
	javadoc -d $(DOCS) -sourcepath $(SOURCEDIR) -classpath $(SOURCEDIR):$(LIBSPATH) -subpackages modnlp.idx

dist: clean
	rm -rf $(DISTDIR)
	mkdir $(DISTDIR)
	cp -aL --parents $(DISTFILES) $(DISTDIR)
	tar cfvz /tmp/$(DISTDIR).tar.gz  --exclude-from=exclude.txt $(DISTDIR)
	rm -rf $(DISTDIR)

bindist: jar
	mkdir $(BINDISTDIR)
	cp README.bin $(BINDISTDIR)/README
	cp $(TOPDIR)src/idxmgr.properties.ecpc $(BINDISTDIR)/idxmgr.properties
	cp $(LIBS) $(JARTARGET) $(BINDISTXTRAS) $(BINDISTDIR)
	tar cfvz /tmp/$(BINDISTDIR).tar.gz  --exclude-from=exclude.txt $(BINDISTDIR)
	rm -rf $(BINDISTDIR)
