#
# Makefile for the project
# prepared by Hyunwook Shin
#
VERSION := 1.0
TARGET_DIR:= $(CURDIR)/lghr-camel/target
JAR_FILE := $(TARGET_DIR)/lghr-camel-$(VERSION).jar

test: $(JAR_FILE)
	true

run: $(JAR_FILE)
	# Run Twitter Service for testing
	java -cp lghr-camel/target/lghr-camel-1.0.jar camel.TwitterService

$(JAR_FILE):
	# Builds the jar under lght-camel
	mvn clean install

clean:
	rm -rf lghr-camel/target/*
