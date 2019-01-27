# raspberry-sensors-pi4j
Raspberry Pi - Joy-It SensorKit X40 - Pi4J Sensor implementation

### Install Java sdk
```bash
sudo apt-get update 
sudo apt-get install oracle-java8-jdk
```

### Install wiring Pi native library

Install WiringPi Native library [Wiring Pi download and install](http://wiringpi.com/download-and-install/)

```bash
sudo apt-get update 
sudo apt-get install git
mkdir wiringPiSource
cd wiringPiSource
git clone git://git.drogon.net/wiringPi
cd wiringPi
build
```
test if install was successful
```bash
gpio -v
# or
gpio readall
```

### Install Pi4J Library 
#### This will install Version 1.1 -> for Model Raspberry PI 1, 2 and 3 (exclusive B+) 

```bash
curl -s get.pi4j.com | sudo bash
```

#### Install Pi4J Library -> Raspberry PI 1.2 Snapshot -> use for Raspberry pi 3 B+

```bash
mkdir pi4j && cd pi4j
wget http://get.pi4j.com/download/pi4j-1.2-SNAPSHOT.deb
sudo dpkg -i pi4j-1.2-SNAPSHOT.deb
pi4j -version
```

### Compile and run Software
```bash
cd ../raspberry-sensors-pi4j/src/
javac -classpath .:/opt/pi4j/lib/* -d . ./at/schinivision/Pi4jsensors.java
sudo java -classpath .:classes:/opt/pi4j/lib/'*' at.schinivision.Pi4jsensors
```
