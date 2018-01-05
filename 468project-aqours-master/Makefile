export CLASSPATH=classes/:lib/antlr-4.7-complete.jar
ANTLR_SCRIPT := Micro.g4

all: team compiler

team:
	@echo "Team: Aqours"
	@echo ""
	@echo "Yi Yang"
	@echo "yang858"
	@echo ""
	@echo "Hengyi Lin"
	@echo "lin483"
compiler:
	rm -rf build
	mkdir build
	java -cp $(CLASSPATH) org.antlr.v4.Tool -o build $(ANTLR_SCRIPT)
	rm -rf classes
	mkdir classes
	javac -bootclasspath /usr/lib/jvm/jre-1.7.0/lib/rt.jar -source 1.7 -target 1.7 -cp $(CLASSPATH) -d classes src/*.java build/*.java
clean:
	rm -rf classes build

.PHONY: all team compiler clean
