<div id="top"></div>

<!-- PROJECT LOGO -->
<br />
<div align="center">
  <!-- <a href="https://github.com/kavindaperera/meshify-framework">
    <img src="https://firebasestorage.googleapis.com/v0/b/meshify-f206b.appspot.com/o/logos%2Fmeshify_text_logo_green.svg?alt=media&token=09cfa2c3-f8f1-4309-9bbc-87ab5cdc2b87" alt="Logo" width="500" height="100">
  </a> -->

  <h3 align="center">Meshify - Android Framework</h3>

  <p align="center">
    Adaptive Communication Framework for Android Devices using Bluetooth and Bluetooth Low Energy
    <br />
    <a href="http://ec2-3-20-225-140.us-east-2.compute.amazonaws.com/"><strong>Explore the docs »</strong></a>
    <br />
    <br />
    <a href="https://github.com/kavindaperera/meshify-framework">View Demo</a>
    ·
    <a href="https://github.com/kavindaperera/meshify-framework/issues">Report Bug</a>
    ·
    <a href="https://github.com/kavindaperera/meshify-framework/issues">Request Feature</a>
  </p>
</div>

<!-- PROJECT SHIELDS -->
<!--
*** I'm using markdown "reference style" links for readability.
*** Reference links are enclosed in brackets [ ] instead of parentheses ( ).
*** See the bottom of this document for the declaration of the reference variables
*** for contributors-url, forks-url, etc. This is an optional, concise syntax you may use.
*** https://www.markdownguide.org/basic-syntax/#reference-style-links
-->


[![forthebadge](https://forthebadge.com/images/badges/built-for-android.svg)](https://github.com/kavindaperera/meshify-framework)
[![forthebadge](https://forthebadge.com/images/badges/made-with-java.svg)](https://github.com/kavindaperera/meshify-framework)

[![Status](https://badgen.net/badge/status/development/green)](https://github.com/kavindaperera/meshify-framework)
[![Build Status](https://badgen.net/badge/build/pending/orange)](https://github.com/kavindaperera/meshify-framework)
[![LoC](https://badgen.net/badge/lines/10.27k/green)](https://github.com/kavindaperera/meshify-framework)
[![Repo](https://badgen.net/badge/icon/github?icon=github&label)](https://github.com/kavindaperera/meshify-framework)
[![License](https://badgen.net//badge/license/MIT/blue)](https://github.com/kavindaperera/meshify-framework/blob/development/LICENSE)

<!-- TABLE OF CONTENTS -->
<details>
  <summary>Table of Contents</summary>
  <ol>
    <li>
      <a href="#about-the-project">About The Project</a>
      <ul>
        <li><a href="#built-with">Built With</a></li>
      </ul>
    </li>
    <li>
      <a href="#getting-started">Getting Started</a>
      <ul>
        <li><a href="#prerequisites">Prerequisites</a></li>
        <li><a href="#installation">Installation</a></li>
      </ul>
    </li>
    <li><a href="#usage">Usage</a></li>
    <li><a href="#initialization">Initialization</a>
         <ul>
            <li><a href="#starting-meshify">Starting Meshify</a></li>
            <li><a href="#configuration">Configuration</a></li>
        </ul>
    </li>
    <li><a href="#roadmap">Roadmap</a></li>
    <li><a href="#contributing">Contributing</a></li>
    <li><a href="#license">License</a></li>
    <li><a href="#contact">Contact</a></li>
    <li><a href="#acknowledgments">Acknowledgments</a></li>
  </ol>
</details>

## About The Project



### Built With



## Getting Started

### Prerequisites

The first step is to add the following permissions in `AndroidManifest.xml`:

```xml
    <uses-permission android:name="android.permission.BLUETOOTH" />
    <uses-permission android:name="android.permission.BLUETOOTH_ADMIN" />
    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
    <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
    <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
```

Meshify supports both Bluetooth Classic and Bluetooth Low Energy (BLE) capable devices.

If you only planning to use BLE, you can optionally declare that your app uses BLE features on Android devices. By doing so, users on devices without BLE capabilities won’t see your app on the Google Play Store. If this behavior sounds good to you, add the following snippet below the `<uses-permission>` tags.

```xml
    <uses-feature
        android:name="android.hardware.bluetooth_le"
        android:required="true" />
```

## Usage

Add the dependency:

```javascript
implementation project(path: ':meshify')
```

## Initialization

You need to create a `UUID` include that in your `AndroidManifest.xml` file. This will be used to uniquely identify your application.

```xml
<meta-data
  android:name="com.codewizards.meshify.APP_KEY"
  android:value="xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx" />
```
### Starting Meshify

Call the `Meshify.initialize(...)` method of Meshify.

```java
Meshify.initialize(getApplicationContext());
```


### ConnectionListener

You can either extend `ConnectionListener` abstract class or create an [`Anonymous Class`](https://docs.oracle.com/javase/tutorial/java/javaOO/anonymousclasses.html) of `ConnectionListener`.

```java
// Anonymous Class
ConnectionListener connectionListener = new ConnectionListener() {...}
``` 

```java

        @override
        public void onDeviceDiscovered(Device device) {
            
        }

        @override
        public void onDeviceConnected(Device device, Session session) {
            
        }

        public void onIndirectDeviceFound(Device device){

        }

        @override
        public void onDeviceLost(Device device) {
            
        }
```

### MessageListener

You can either extend `MessageListener` abstract class or create an [`Anonymous Class`](https://docs.oracle.com/javase/tutorial/java/javaOO/anonymousclasses.html) of `MessageListener`.

```java
// Anonymous Class
MessageListener MessageListener = new MessageListener() {...}
```

`@override`

```java
        @override
        public void onMessageReceived(Message message) {
        
        }

        @override
        public void onBroadcastMessageReceived(Message message) {
        
        }
```

### Configuration

You can customize the `Meshify` framework according to your requirement using `Config` Builder.

```java
Config.Builder builder = new Config.Builder();
```

and finally call `Meshify.start()` to start the meshify service.

```java
Meshify.start(messageListener, connectionListener, builder.build());
```
