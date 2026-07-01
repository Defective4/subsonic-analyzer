#!/bin/sh
mvn clean package
docker build -t subsonic-analyzer:0.0.2 .
