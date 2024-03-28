JAVAC := javac
OUT_DIR := build/classes
JAVAC_FLAGS := -d $(OUT_DIR)

JAVA := java
JAVA_FLAGS := -cp $(OUT_DIR)


PACKAGE_DIR = .
PACKAGE := server
MAIN_CLASS := Server
MAIN_SOURCE := $(MAIN_CLASS).java
ADDITIONAL_SOURCES := src/*.java


.PHONY: all build run clean open

all: $(OUT_DIR) $(OUT_DIR)/$(MAIN_CLASS).class

$(OUT_DIR):
	mkdir -p $(OUT_DIR)

$(OUT_DIR)/$(MAIN_CLASS).class: $(MAIN_SOURCE) $(ADDITIONAL_SOURCES)
	$(JAVAC) $(JAVAC_FLAGS) $(MAIN_SOURCE) $(ADDITIONAL_SOURCES)

run:
	@$(JAVA) $(JAVA_FLAGS) $(PACKAGE)/$(MAIN_CLASS)
	
open:
	 open http://127.0.0.1:8080

clean:
	@rm -rf build

