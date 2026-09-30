.PHONY: test clean
test:
	mkdir -p build/classes
	javac -d build/classes src/main/java/dev/vanguard/*.java src/test/java/dev/vanguard/*.java
	java -cp build/classes dev.vanguard.RoboticsTest
clean:
	rm -rf build
