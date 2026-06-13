#!/bin/sh
mvn clean package
docker build -t subsonic-analyzer:latest .
