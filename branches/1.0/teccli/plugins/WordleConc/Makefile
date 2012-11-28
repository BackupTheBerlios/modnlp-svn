all: sign
.PHONY: lib clean sign

TARGET_JAR=lib/wordleconc.jar


jar:
	java -jar ../autojar.jar -o $(TARGET_JAR) -vc lib/cue.language.jar:lib/gluegen-rt.jar:lib/jogl-all.jar:lib/jsoup-1.3.3.jar:lib/WordCram.jar:lib/core.jar:../../lib/idx.jar:../../lib/teccli.jar:classes -b  classes org/modnlp/wordleconc/*.class cue/lang/stop/* processing/opengl/*


sign: jar
	jarsigner -storepass `cat luzs.cer.pass` $(TARGET_JAR) luzs

