# 🎵 Subsonic Music Analyzer
This project is a music analyzer inspired by and based on [craiglush/navidrome-mood-plugin](https://github.com/craiglush/navidrome-mood-plugin).  
It also uses Essentia Tensorflow with the same models, meaning that this tool will yield similar results.  
This tool, however, is <u>standalone</u>, and does not require physical access to the music library, nor an account with administrator access!

# 🌟 Features
- 🔌 Compatible with **any** Subsonic/Navidrome instance. No physical address/admin privileges required
- ⚙️ CLI interface for all tasks
- 🧪 Analyzes parts of, or a whole library:
  - Includes analysis of track moods, genre, main instrument, tempo, and vocality
- 🗒️ Generates or replaces existing playlists based on:
  - Composite moods
  - Any analyzed attributes (genre, tempo, mood, etc.)
  - Similarity to a reference song
- 🦾 Manages and updates ML models
- ✅ Keeps track of analyzed songs, with the ability to generate reports, re-analyze failed songs, or the whole library.

# 📁 Installation
There are several methods of installing Subsonic Music Analyzer, all listed below.
## 🐳 Docker (recommended)

### Requirements
- Git
- Docker, Docker CLI, and Docker Compose
- Java Development Kit 21+
- Maven

### Setup
```bash
$ git clone https://github.com/Defective4/subsonic-analyzer.git && cd subsonic-analyzer
$ mvn -DbuildNative clean package
$ mvn dependency:copy-dependencies
$ docker compose up
```
This will build the Docker image and start the analyzer server.  You can start the tool any time you want by typing `docker compose up` in the project directory.

### Usage
To use the tool, simply run:
```bash
docker container exec subsonic-analyzer-analyzer-1 analyzer [command] [args...]
```

## 📦 Debian package (for Debian-based distributions)

### Requirements
- An amd64 Debian 13 (or Debian-based) distribution

### Setup
- Download the latest Debian package from [releases](https://github.com/Defective4/subsonic-analyzer/releases/latest)
- Install the package:
```bash
VERSION=0.0.2
dpkg -i subsonic-analyzer-$VERSION.deb
```
- Create a dedicated operating directory and download required models:
```bash
DIR=~/subsonic-analyzer
mkdir -p $DIR && cd $DIR
subsonic-analysis-tool models --update
```

### Usage
- Start the analyzer server (necessary for the tool's `analyze` command only)
```bash
# ALL commands MUST be run inside the operation directory
$ DIR=~/subsonic-analyzer
$ cd $DIR
$ subsonic-analysis-service
```
- In a second terminal, use the tool:
```bash
# ALL commands MUST be run inside the operation directory
$ DIR=~/subsonic-analyzer
$ cd $DIR
$ subsonic-analysis-tool [command] [args...]
```

## ❓ Manual installation

### Requirements
- Java Runtime Environment 21+
- Python3 with `python3-venv` and `python3-pip`
- bash

### Setup
- Download the jar archive from [releases](https://github.com/Defective4/subsonic-analyzer/releases/latest)
- Download [analyzer.py](https://raw.githubusercontent.com/Defective4/subsonic-analyzer/refs/heads/master/analyzer.py)
- Create an operating directory and put both of the files there.
- In the operating directory, create and set up the Python virtual environment:
```bash
python3 -m venv venv
source ./venv/bin/activate
pip3 install essentia-tensorflow fastapi uvicorn
```
- Download required Essentia models:
```bash
java -jar subsonic-analyzer.jar models --update
```

### Usage
- In one terminal, start the analysis server:
```bash
$ DIR=/path/to/downloaded/files
$ source ./venv/bin/activate
$ uvicorn analyzer:api
```
- In another terminal window: run the tool:
```bash
$ java -jar subsonic-analyzer.jar [command] [args...]
```

# 🔧 Using Subsonic Analyzer
All operations with Subsonic Analyzer are performed in Command Line Interface.  
To learn how to use the tool, see [the wiki project](https://github.com/Defective4/subsonic-analyzer/wiki)

# ✏️ Credits
- [craiglush](https://github.com/craiglush) for inspiration
- [https://essentia.upf.edu](https://essentia.upf.edu/) for awesome models and tools

# 🐛 Reporting an issue
You can report issues in the [project's issue tracker.](https://github.com/Defective4/subsonic-analyzer/issues)

# ❤️‍🔥 Support
Did you find this project useful? Consider supporting me on ko-fi, or GitHub sponsors!  
  
<a href='https://ko-fi.com/U7U01VOM9P' target='_blank'><img height='36' style='border:0px;height:36px;' src='https://storage.ko-fi.com/cdn/kofi4.png?v=6' border='0' alt='Buy Me a Coffee at ko-fi.com' /></a>