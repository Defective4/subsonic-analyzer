FROM ubuntu:26.04

RUN mkdir /app
WORKDIR /app
ADD target/subsonic*.jar /app/subsonic.jar
ADD analyzer.py /app/

RUN apt-get update
RUN apt-get install -y wget tar

RUN bash -c "echo Downloading Java runtime, this may take a while...; wget -q -O jre.tar.gz http://raspberry.local/java/OpenJDK21U-jre_x64_linux_hotspot_21.0.11_10.tar.gz"
RUN tar -zvxf jre.tar.gz
RUN rm jre.tar.gz
RUN mv jdk* jre
WORKDIR jre/bin
RUN update-alternatives --install /bin/java java /app/jre/bin/java 1
WORKDIR /app

RUN apt-get install -y python3-venv python3-pip
RUN apt-get clean
RUN python3 -m venv /app/venv

RUN bash -c "echo '#!/bin/java -jar' > /bin/analyzer; cat subsonic.jar >> /bin/analyzer; chmod +x /bin/analyzer; source ./venv/bin/activate; pip install uvicorn essentia-tensorflow fastapi; java -jar subsonic.jar models --update --base-url http://raspberry.local/ml/essentia.upf.edu/models/"

EXPOSE 8000/tcp
ENTRYPOINT ["bash", "-c", "cd /app; source venv/bin/activate; uvicorn analyzer:api"]
