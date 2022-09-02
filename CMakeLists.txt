cmake_minimum_required(VERSION 3.10)

find_package(Java REQUIRED)
include(UseJava)

project (Parser)

set(CMAKE_JAVA_COMPILE_FKAGS "-source" "1.8" "-target" "1.8")

add_jar(Parser SampleTester.java
  Action.java Grammar.java NonTerminal.java State.java Terminal.java
  Token.java Validator.java TokenData.java
  MANIFEST "${CMAKE_CURRENT_SOURCE_DIR}/build/MANIFEST.MF")

get_target_property(_jarFile Parser JAR_FILE)
get_target_property(_classDir Parser CLASSDIR)

message(STATUS "Jar file ${_jarFile}")
message(STATUS "Class compiled to ${_classDir}")
