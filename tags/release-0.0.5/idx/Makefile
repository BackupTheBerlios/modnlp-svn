TOPDIR=
SOURCEDIR=$(TOPDIR)src/
LIBSPATH=$(TOPDIR)lib/gnu-regexp.jar:$(TOPDIR)lib/je.jar:$(TOPDIR)lib/antlr-2.7.6.jar:$(TOPDIR)lib/commons-pool-1.2.jar:$(TOPDIR)lib/exist-modules.jar:$(TOPDIR)lib/exist.jar:$(TOPDIR)lib/jgroups-all.jar:$(TOPDIR)lib/log4j-1.2.14.jar:$(TOPDIR)lib/resolver.jar:$(TOPDIR)lib/sunxacml.jar:$(TOPDIR)lib/xmldb.jar:$(TOPDIR)lib/xmlrpc-1.2-patched.jar
LIBS=$(subst :, ,$(LIBSPATH))
LIBLICENSES=$(TOPDIR)lib/COPYING-libs
DOCS=$(TOPDIR)doc
DATA=$(TOPDIR)data
SOURCES=$(SOURCEDIR)modnlp/Constants.java \
	$(SOURCEDIR)modnlp/util/PrintUtil.java \
	$(SOURCEDIR)modnlp/util/LogStream.java \
	$(SOURCEDIR)modnlp/util/Tokeniser.java \
	$(SOURCEDIR)modnlp/util/IOUtil.java \
	$(SOURCEDIR)modnlp/util/XMLSplitter.java \
	$(SOURCEDIR)modnlp/dstruct/CorpusList.java \
	$(SOURCEDIR)modnlp/dstruct/CorpusFile.java \
	$(SOURCEDIR)modnlp/dstruct/IntegerSet.java \
	$(SOURCEDIR)modnlp/dstruct/IntOffsetArray.java \
	$(SOURCEDIR)modnlp/dstruct/PositionSet.java \
	$(SOURCEDIR)modnlp/dstruct/SubcorpusDelimPair.java	\
	$(SOURCEDIR)modnlp/dstruct/SubcorpusMap.java	\
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
	$(SOURCEDIR)modnlp/idx/database/SubcorpusDelimPairBinding.java \
	$(SOURCEDIR)modnlp/idx/database/SubcorpusKeyCreator.java \
	$(SOURCEDIR)modnlp/idx/database/Table.java \
	$(SOURCEDIR)modnlp/idx/database/TPosTable.java \
	$(SOURCEDIR)modnlp/idx/database/WordFileTable.java \
	$(SOURCEDIR)modnlp/idx/database/WordPositionTable.java \
	$(SOURCEDIR)modnlp/idx/database/SubcorpusDirectory.java \
	$(SOURCEDIR)modnlp/idx/database/SubcorpusTable.java \
	$(SOURCEDIR)modnlp/idx/headers/HeaderDBManager.java \
	$(SOURCEDIR)modnlp/idx/headers/ConstraintCache.java \
	$(SOURCEDIR)modnlp/idx/inverted/SubcorpusIndexer.java \
	$(SOURCEDIR)modnlp/idx/inverted/TokeniserGNU.java \
	$(SOURCEDIR)modnlp/idx/inverted/TokeniserRegex.java \
	$(SOURCEDIR)modnlp/idx/ConsumerQuery.java \
	$(SOURCEDIR)modnlp/idx/Dump.java \
	$(SOURCEDIR)modnlp/idx/MakeTECIndex.java \
	$(SOURCEDIR)modnlp/idx/MakeSectionIndex.java \
	$(SOURCEDIR)modnlp/idx/Query.java \
	$(SOURCEDIR)modnlp/idx/IndexManager.java \
	$(SOURCEDIR)modnlp/idx/IndexManagerProperties.java \
	$(SOURCEDIR)modnlp/idx/RemoveFileFromIndex.java \
	$(SOURCEDIR)modnlp/idx/gui/CorpusChooser.java \
	$(SOURCEDIR)modnlp/idx/gui/CorpusFilesChooser.java \
	$(SOURCEDIR)modnlp/idx/gui/HeaderURLChooser.java \
	$(SOURCEDIR)modnlp/idx/gui/IndexConfigChooser.java \
	$(SOURCEDIR)modnlp/idx/gui/IndexManagerUI.java \
	$(SOURCEDIR)modnlp/idx/query/Horizon.java \
	$(SOURCEDIR)modnlp/idx/query/PrepContextQuery.java \
	$(SOURCEDIR)modnlp/idx/query/SubcorpusConstraints.java	\
	$(SOURCEDIR)modnlp/idx/query/WordQuery.java \
	$(SOURCEDIR)modnlp/idx/query/WordQueryException.java
TARGETS=$(subst .java,.class,$(SOURCES))
MAINJARS=$(subst $(SOURCEDIR),,$(TARGETS)) # doesn't work by itself: need to jar inner classess as well
JARREDS=$(MAINJARS) \
	eXistConf.xml \
	'modnlp/idx/ConsumerQuery$$ConcordanceProducer.class' \
	'modnlp/idx/IndexManager$$DeindexingThread.class' \
	'modnlp/idx/IndexManager$$IndexingThread.class'
DISTDIR:=modnlp-idx-`cat VERSION`
BINDISTDIR=$(DISTDIR)-bin
BINDISTXTRAS=$(LIBLICENSES)
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
	cp src/dictionary.properties.ISO8859_1 /tmp/tec/index/dictionary.properties
	@echo "** Indexing datamodnlptest/short.lst"
	$(JAVA) $(JAVAFLAGS) modnlp.idx.MakeTECIndex /tmp/tec/index $(TOPDIR)data/short.lst 
	@echo "** Dumping index..."
	$(JAVA) $(JAVAFLAGS) modnlp.idx.Dump /tmp/tec/index
	@echo "** Querying concordance for word 'that'..."
	$(JAVA) $(JAVAFLAGS) modnlp.idx.Query /tmp/tec/index -l that
	@echo "** Testing completed"

docs: $(TARGETS)
	javadoc -d $(DOCS) -sourcepath $(SOURCEDIR) -classpath $(SOURCEDIR):$(LIBSPATH) -subpackages modnlp.idx

dist: clean
	rm -rf $(DISTDIR)
	mkdir $(DISTDIR)
	cp -aL --parents $(DISTFILES) $(DISTDIR)
	cp $(TOPDIR)src/idxmgr.properties.ecpc $(DISTDIR)/idxmgr.properties
	cp $(TOPDIR)src/dictionary.properties.ISO8859_1 $(DISTDIR)/dictionary.properties
	tar cfvz /tmp/$(DISTDIR).tar.gz  --exclude-from=exclude.txt $(DISTDIR)
	rm -rf $(DISTDIR)

bindist: jar
	mkdir $(BINDISTDIR)
	cp README.bin $(BINDISTDIR)/README
	cp $(TOPDIR)src/idxmgr.properties $(BINDISTDIR)/idxmgr.properties
	cp $(LIBS) $(JARTARGET) $(BINDISTXTRAS) $(BINDISTDIR)
	tar cfvz /tmp/$(BINDISTDIR).tar.gz  --exclude-from=exclude.txt $(BINDISTDIR)
	rm -rf $(BINDISTDIR)

bindisthc: jar
	mkdir $(BINDISTDIR)-hc
	cp README.bin $(BINDISTDIR)-hc/README
	cp $(TOPDIR)src/idxmgr.properties.ecpc-hc $(BINDISTDIR)-hc/idxmgr.properties
	cp $(LIBS) $(JARTARGET) $(BINDISTXTRAS) $(BINDISTDIR)-hc
	tar cfvz /tmp/$(BINDISTDIR)-hc.tar.gz  --exclude-from=exclude.txt $(BINDISTDIR)-hc
	rm -rf $(BINDISTDIR)-hc

bindistep: jar
	mkdir $(BINDISTDIR)-ep
	cp README.bin $(BINDISTDIR)-ep/README
	cp $(TOPDIR)src/idxmgr.properties.ecpc-ep $(BINDISTDIR)-ep/idxmgr.properties
	cp $(LIBS) $(JARTARGET) $(BINDISTXTRAS) $(BINDISTDIR)-ep
	tar cfvz /tmp/$(BINDISTDIR)-ep.tar.gz  --exclude-from=exclude.txt $(BINDISTDIR)-ep
	rm -rf $(BINDISTDIR)-ep
