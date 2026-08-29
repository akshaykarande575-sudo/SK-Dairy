#!/usr/bin/env sh

# Gradient Wrapper Script for *nix

# Standard Gradle setup
APP_BASE_NAME=${0##*/}
APP_HOME=$(dirname "$0")

# Find Java executable
if [ -n "$JAVA_HOME" ] ; then
    JAVACMD="$JAVA_HOME/bin/java"
else
    JAVACMD="java"
fi

# Run Gradle
exec "$JAVACMD" -jar "$APP_HOME/gradle/wrapper/gradle-wrapper.jar" "$@"
