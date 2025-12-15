# OPC UA to AAS Bridge

A Java application that bridges OPC UA data to BaSyx AAS (Asset Administration Shell) environment, enabling real-time synchronization of industrial control data.

## ✅ Status: WORKING

Successfully tested and operational! The bridge actively monitors OPC UA nodes and updates AAS properties in real-time through the BaSyx v2.0 REST API.

## Features

- ✅ **Real-time OPC UA Monitoring**: Connects to OPC UA server and subscribes to node value changes
- ✅ **Automatic AAS Updates**: Updates corresponding AAS property values via BaSyx v2 API
- ✅ **BaSyx v2 Compatible**: Uses correct `/$value` endpoint with PATCH method
- ✅ **Continuous Operation**: Runs as background service until manually stopped
- 📊 **Live Synchronization**: Currently mapping crane hoist speed from OPC UA to AAS Web UI

## Current Configuration

The bridge is configured to monitor:
- **OPC UA Node**: `ns=7;s=SCF.PLC.DX_Custom_V.Controls.Hoist.Speed` (Crane hoist speed control)
- **AAS Property**: `Hoist_Speed` in submodel `aHR0cHM6Ly9leGFtcGxlLmNvbS9pZHMvc20vNTAxMF81MTUwXzExNTJfMTEwMg`
- **Update Frequency**: Real-time on value change (1000ms sampling interval)

### Endpoints

- OPC UA Server: `opc.tcp://localhost:4840`
- BaSyx AAS Environment: `http://localhost:8081`
- BaSyx Web UI: `http://localhost:8082` (check updated values here!)

## Configuration

To monitor different OPC UA nodes or AAS properties, edit the constants in `OpcUaAasBridge.java`:

```java
private static final String OPC_UA_ENDPOINT = "opc.tcp://localhost:4840";
private static final String OPC_UA_NODE_ID = "ns=7;s=SCF.PLC.DX_Custom_V.Controls.Hoist.Speed";
private static final String SUBMODEL_ID = "aHR0cHM6Ly9leGFtcGxlLmNvbS9pZHMvc20vNTAxMF81MTUwXzExNTJfMTEwMg";
private static final String PROPERTY_ID_SHORT = "Hoist_Speed";
private static final String AAS_ENDPOINT = "http://localhost:8081";
```

## Build

```bash
mvn clean package
```

This creates a fat JAR with all dependencies at `target/opcua-aas-bridge-1.0-SNAPSHOT.jar`

## Run

### Windows (PowerShell)
```powershell
& "C:\Program Files\Eclipse Adoptium\jdk-25.0.1.8-hotspot\bin\java.exe" -jar target\opcua-aas-bridge-1.0-SNAPSHOT.jar
```

### Linux/Mac
```bash
java -jar target/opcua-aas-bridge-1.0-SNAPSHOT.jar
```

### Expected Output
```
Starting OPC UA to AAS Bridge...
Connected to OPC UA server: opc.tcp://localhost:4840
Created OPC UA subscription
Monitoring OPC UA node: ns=7;s=SCF.PLC.DX_Custom_V.Controls.Hoist.Speed
Bridge is running. Press Ctrl+C to stop.
OPC UA value changed: 20.0
Successfully updated AAS property: 20.0
```

## How It Works

1. **OPC UA Connection**: Establishes secure connection to OPC UA server using Eclipse Milo SDK
2. **Subscription Setup**: Creates monitored item subscription with 1000ms sampling interval
3. **Value Change Detection**: Listens for value change notifications from OPC UA server
4. **AAS Update**: When value changes, sends PATCH request to BaSyx v2 API endpoint:
   ```
   PATCH /submodels/{submodelId}/submodel-elements/{idShort}/$value
   Content-Type: application/json
   Body: "20.0"
   ```
5. **Continuous Operation**: Runs in background loop until stopped with Ctrl+C
6. **Graceful Shutdown**: Disconnects from OPC UA server on exit

## Technical Stack

- **Java 17+** (tested with JDK 25)
- **Eclipse Milo 0.6.8** - OPC UA client library
- **OkHttp 4.11.0** - HTTP client for REST API calls
- **Gson 2.10.1** - JSON serialization
- **Maven 3.9+** - Build tool with Shade plugin

## Requirements

### Runtime
- Java 17 or higher (Java 25 recommended)
- OPC UA server accessible at configured endpoint
- BaSyx AAS environment v2.0-SNAPSHOT running

### BaSyx Environment
The bridge requires a running BaSyx environment. Use the docker-compose setup in `../basyx-setup`:

```bash
cd ../basyx-setup
docker-compose up -d
```

This starts:
- AAS Environment (port 8081)
- AAS Registry (port 8082)
- Submodel Registry (port 8083)
- AAS Web UI (port 8085)
- MongoDB (backend storage)

## Verified Functionality

✅ **Tested on**: December 15, 2025  
✅ **BaSyx Version**: 2.0.0-SNAPSHOT  
✅ **OPC UA Server**: Custom crane control server  
✅ **Data Flow**: OPC UA → Bridge → BaSyx AAS → Web UI  
✅ **API Compatibility**: BaSyx v2 REST API `/$value` endpoint with PATCH

## Troubleshooting

### Bridge won't connect to OPC UA
- Verify OPC UA server is running: Check `opc.tcp://localhost:4840`
- Check firewall settings
- Verify node ID exists in OPC UA server

### AAS updates fail (404 error)
- Ensure BaSyx AAS environment is running
- Verify submodel ID is correct (Base64 encoded)
- Check that property `Hoist_Speed` exists in the submodel
- Restart AAS container to reload AASX file if property disappeared:
  ```bash
  docker-compose restart aas-env
  ```

### Property disappeared from Web UI
- BaSyx stores data in memory by default
- Restart AAS environment container to reload from AASX file
- Check logs: `docker logs aas-env`

## Future Enhancements

- 🔄 Bidirectional communication (AAS operations → OPC UA write)
- 📝 Configuration file support (JSON/YAML instead of hardcoded constants)
- 🔌 Multiple node mapping support
- 📊 Metrics and monitoring dashboard
- 🐳 Docker containerization
- 🔐 Security: OPC UA authentication and encryption

## License

This project uses Eclipse Milo (EPL-1.0) and other open-source libraries.
