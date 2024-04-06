# Hamlib-JNI-Android


### Introduction

This project is trying to port the hamlib to Android using JNI bindings, currently is at early stage of development.
Currently, the idea is using "pseudoterminal master and slave" to bridge the I/O between Android's BluetoothSocket and tty to make serial port device controllable over Bluetooth SPP.

### Notes

A demo app is at "app" directory

The JNI related code is at "hamlib" directory

### TODO

- Implement a lot of JNI bindings
- Organize and refactor codes
- Support non-serial-port devices

### Build

```
git clone https://github.com/edward-p/Hamlib-JNI-Android
git submodule update --init
```


ndk version 25.1 is tested
