#!/bin/bash
cd "$(dirname "$0")"
echo "🚀 Starte auth-service lokal mit Profile 'julius'"
./gradlew bootRun --args='--spring.profiles.active=julius'
