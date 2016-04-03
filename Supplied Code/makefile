

JAVA_CLASSPATH = -classpath "/usr/local/lib/antlr-4.5.1-complete.jar:$$CLASSPATH"


ANTRL4_GENERATED_FILES=Goo.tokens GooBaseVisitor.java GooLexer.java GooLexer.tokens GooParser.java GooVisitor.java


build:
	mkdir -p bin
	java -Xmx500M $(JAVA_CLASSPATH) org.antlr.v4.Tool -visitor -no-listener Goo.g4
	javac $(JAVA_CLASSPATH) -g -d bin *.java


run:
	java -classpath "/usr/local/lib/antlr-4.5.1-complete.jar:bin:$$CLASSPATH" GooMain -dsym


clean:
	rm -rf bin $(ANTRL4_GENERATED_FILES) *~


rebuild: clean build

