# Matrix Client SDK for Java
[![Build Status](https://github.com/kamax-matrix/matrix-java-sdk/actions/workflows/build.yml/badge.svg)](https://github.com/kamax-matrix/matrix-java-sdk/actions)

---

**Matrix HTTP API v3 SDK for Java** - A pure Java implementation of the Matrix client-server API supporting all modern Matrix features (v1.17/v1.18).

---

## Purpose

Matrix SDK in Java 1.8 for:
- Client -> Homeserver
- Client -> Identity Server
- Application Server -> Homeserver

## Use
### Build from source
This SDK is not currently published to Maven Central. To use it in your project, you need to build it from source:

```bash
git clone https://github.com/kamax-matrix/matrix-java-sdk.git
cd matrix-java-sdk
./gradlew build
```

Then include the built JAR in your project or install it to your local Maven repository:

```bash
./gradlew publishToMavenLocal
```

### Add to your project
#### Gradle
```
dependencies {
    implementation 'io.kamax:matrix-java-sdk:0.1.0' // Check latest version in build.gradle
}
```

#### Maven
```
<dependencies>
  <dependency>
    <groupId>io.kamax</groupId>
    <artifactId>matrix-java-sdk</artifactId>
    <version>0.1.0</version> <!-- Check latest version in build.gradle -->
  </dependency>
</dependencies>
```
**Note:** This SDK was originally created to support Kamax.io projects and has been updated to provide comprehensive
Matrix HTTP API v3 support. It now includes all modern Matrix features (v1.17/v1.18) including spaces, threads,
end-to-end encryption, device management, and more.

### Getting started
#### Getting the client object
With .well-known auto-discovery:
```java
_MatrixClient client = new MatrixHttpClient("example.org");
client.discoverSettings();
```

With C2S API Base URL:
```java
URL baseUrl = new URL("https://example.org");
_MatrixClient client = new MatrixHttpClient(baseUrl);
```

#### Providing credentials
Access token:
```java
client.setAccessToken(accessToken);
```

Log in:
```java
client.login(new MatrixPasswordCredentials(username, password));
```

#### Sync
```java
// We will update this after each sync call
String syncToken = null;

// We sync until the process is interrupted via Ctrl+C or a signal
while (!Thread.currentThread().isInterrupted()) {
    
    // We provide the next batch token, or null if we don't have one yet
    _SyncData data = client.sync(SyncOptions.build().setSince(syncToken).get());
    
    // We check the joined rooms
    for (JoinedRoom joinedRoom : data.getRooms().getJoined()) {
        // We get the relevant room object to act on it while we process
        _Room room = client.getRoom(joinedRoom.getId());
        
        for (_MatrixEvent rawEv : joinedRoom.getTimeline()) {
            // We only want to act on room messages
            if ("m.room.message".contentEquals(rawEv.getType())) {
                MatrixJsonRoomMessageEvent msg = new MatrixJsonRoomMessageEvent(rawEv.getJson());
                
                // Ping?
                if (StringUtils.equals("ping", msgg.getBody())) {
                    // Pong!
                    room.sendText("pong");
                }
            }
        }
    }
    
    // We check the invited rooms
    for (InvitedRoom invitedRoom : data.getRooms().getInvited()) {
        // We auto-join rooms we are invited to
        client.getRoom(invitedRoom.getId()).join());
    }
    
    // Done processing sync data. We save the next batch token for the next loop execution
    syncToken = data.nextBatchToken();
}
```


#### As an Application Service
Use `MatrixApplicationServiceClient` instead of `MatrixHttpClient` when creating the main client object.

To talk to the API as a virtual user, use the method `createClient(localpart)` on MatrixApplicationServiceClient, then
processed normally.

### Real-world usage
#### As a regular client
You can check the [Send'n'Leave bot](https://github.com/kamax-matrix/matrix-send-n-leave-bot) which make uses of this SDK in a more realistic fashion.  
Direct link to the relevant code: [here](https://github.com/kamax-matrix/matrix-send-n-leave-bot/blob/master/src/main/java/io/kamax/matrix/bots/send_n_leave/SendNLeaveBot.java#L68)

#### As an Application Service
- mxasd-voip
  - [Project](https://github.com/kamax-matrix/matrix-appservice-voip)
  - [Relevant code](https://github.com/kamax-matrix/matrix-appservice-voip/blob/master/src/main/java/io/kamax/matrix/bridge/voip/matrix/MatrixManager.java)

- mxasd-email - **WARNING:** This project use a very old version of the SDK but is still relevant 
  - [Project](https://github.com/kamax-matrix/matrix-appservice-email)
  - [Relevant code](https://github.com/kamax-matrix/matrix-appservice-email/blob/master/src/main/java/io/kamax/matrix/bridge/email/model/matrix/MatrixApplicationService.java)

## Contribute
Contributions and PRs are welcome to turn this into a fully fledged Matrix Java SDK.  
Your code will be licensed under AGPLv3.

To ensure code formatting consistency, we use [Spotless](https://github.com/diffplug/spotless).  
Before opening any PR, make sure you format the code:
```bash
./gradlew spotlessApply
```

Your code must pass all existing tests with and must provide tests for any new method/class/feature.  
Make sure you run:
```bash
./gradlew test
```
