all: jnlp
.PHONY: lib clean sign

jnlp:
	cp ../lib/capte.jar .
#	jar ufv capt.jar teclipluginlist.txt 
clean: 
	rm -f *~ *.jar

sign:   jnlp
	jarsigner -storepass `cat luzs.cer.pass` capte.jar luzs
	jarsigner -storepass `cat luzs.cer.pass` ../lib/commons-io-2.0.1.jar luzs
	jarsigner -storepass `cat luzs.cer.pass` ../lib/commons-logging-1.1.1.jar luzs
	jarsigner -storepass `cat luzs.cer.pass` ../lib/commons-codec-1.2.jar luzs

