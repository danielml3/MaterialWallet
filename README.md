# MaterialWallet

MaterialWallet is a decentralized, SPV enabled Bitcoin wallet for the Android platform.

This project is released under the GNU General Public License v3.

## Index
- [1- Features](#features)
- [2.1- Building (debug variant)](#building-debug)
- [2.2- Building (release variant)](#building-release)
- [3- Third party libraries](#third-party-libraries)

<a id="features"></a>

## 1- Features
- Simplified Payment Verification (SPV)
- Fully decentralized
- MainNet and TestNet support (MainNet on release variant and TestNet on debug variant)
- Deterministic wallet
- Support for restoring wallets using a 12 word mnemonic (recovery phrase)
- Transaction list
- More to come with the time...

<a id="building-debug"></a>

## 2.1- Building (debug variant)
You can build the project with gradle as follows:
  - If on Linux, make `gradlew` executable: `chmod +x ./gradlew`
  - Start the gradle daemon: `./gradlew` or `gradlew.bat`
  - Build the debug variant: `./gradlew assembleDebug`
  - The debug package will be available at `./app/build/outputs/apk/debug/`

<a id="building-release"></a>

## 2.2- Building (release variant)
First, you must configure the credentials to sign the package with the following environment variables:
- RELEASE_STORE_FILE: Must contain the path to the keystore
- RELEASE_STORE_PASSWORD: Must contain the keystore password
- RELEASE_KEY_ALIAS: Must contain the key alias
- RELEASE_KEY_PASSWORD: Must contain the key password

Once configured, you can build the project with gradle as follows:
  - If on Linux, make `gradlew` executable: `chmod +x ./gradlew`
  - Start the gradle daemon: `./gradlew` or `gradlew.bat`
  - Build the release variant: `./gradlew assembleRelease`
  - The release package will be available at `./app/build/outputs/apk/release/`

<a id="third-party-libraries"></a>

## 3- Third party libraries
- AndroidX components
- Material 3 Components
- BitcoinJ