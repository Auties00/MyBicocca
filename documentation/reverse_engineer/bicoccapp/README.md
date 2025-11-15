# BicoccApp Security Research

## Problem Statement

BicoccApp implements client-side security measures that prevent security researchers from analyzing the application's network traffic:
Android's security model blocks HTTP traffic interception through certificate pinning.
To analyze BicoccApp's API communications using tools like HTTPToolkit or Burp Suite, we need to bypass SSL certificate pinning.
SSL pinning bypass requires system-level modifications (root access), but BicoccApp detects rooted devices and refuses to run.

## Solution Architecture

- The [frida](./frida) directory includes the work that went into researching and developing a proof of concept to bypass the security measures.
- The [magisk](./magisk) directory contains a Magisk module that was developed based on the PoC to bypass the security measures.
- The [emulator](emulator) directory contains scripts to automatically set up and run an Android emulator with all necessary tools to start reverse engineering.