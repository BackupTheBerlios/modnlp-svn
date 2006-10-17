SRCCLASSPATH=.:../lib/MinML2.jar
TOPCLASSPATH=src:lib/MinML2.jar
SOURCES=modnlp/tc/induction/*.java \
	modnlp/tc/classify/*.java \
	modnlp/tc/evaluation/*.java \
	modnlp/util/*.java \
	modnlp/dstruct/*.java \
	modnlp/tc/util/*.java \
	modnlp/tc/dstruct/*.java \
	modnlp/tc/parser/*.java \
	modnlp/tc/tsr/*.java
DOCS=doc
SOURCEDIR=src
DISTDIR=tc-`cat VERSION`
DISTFILES=BUGS COPYING ChangeLog INSTALL Makefile README TODO VERSION AUTHORS\
	compile.sh exclude.txt run.sh doc lib $(subst modnlp/, src/modnlp/, $(SOURCES)) data
#JAVA=jamvm
#JAVAC=jikes-classpath
JAVA=java
JAVAC=javac
JAVAFLAGS=-classpath $(TOPCLASSPATH)
JAVACFLAGS=-classpath $(SRCCLASSPATH)

build:
	cd $(SOURCEDIR) && $(JAVAC) $(JAVACFLAGS) $(SOURCES)

test:
	@echo "Testing TSR..."
	$(JAVA) $(JAVAFLAGS) modnlp.tc.tsr.MakeReducedTermSet data/tinycorpus.lst data/stopwds.txt 10 InfoGain acq  > /tmp/tinycorpus.tsr
	@echo "** TSR scores saved to /tmp/tinycorpus.tsr"
	@echo "Testing ARFF file generation..."
	$(JAVA) $(JAVAFLAGS) -classpath $(TOPCLASSPATH) modnlp.tc.util.MakeARFF data/tinycorpus.lst data/stopwds.txt 10 InfoGain acq tfidf > /tmp/tinycorpus.arff
	@echo "** ARFF file (of TFIDF vectors) saved to /tmp/tinycorpus.arff"
	@echo "Testing Probability Model Induction..."
	$(JAVA) $(JAVAFLAGS) -classpath $(TOPCLASSPATH) modnlp.tc.induction.MakeProbabilityModel data/tinycorpus.lst data/stopwds.txt 10 InfoGain acq /tmp/tinycorpus.pm
	@echo "Probability model saved to /tmp/tinycorpus.pm"
	@echo "Testing Classification..."
	$(JAVA) $(JAVAFLAGS) -classpath $(TOPCLASSPATH) modnlp.tc.classify.BVBayes data/tinycorpus.lst acq /tmp/tinycorpus.pm proportional >/tmp/tinycorpus.log
	@echo "** Classification results saved to /tmp/tinycorpus.log"
	@echo "Testing completed. See lines starting with '**' for location of log files."

docs:
	javadoc -d $(DOCS) -sourcepath $(SOURCEDIR) -subpackages modnlp.tc

dist: docs
	mkdir $(DISTDIR)
	cp -aL --parents $(DISTFILES) $(DISTDIR)
	tar cfvz /tmp/$(DISTDIR).tar.gz  --exclude-from=exclude.txt $(DISTDIR)
	rm -rf $(DISTDIR)
