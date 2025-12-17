# OPC UA Operation Delegation Service

Spring Boot service that implements BaSyx Operation Delegation pattern to bridge AAS operations to OPC UA crane controls.

## Overview

This service acts as the **delegated operation handler** for BaSyx AAS operations. When an operation is invoked in the AAS Web UI or via API, BaSyx forwards the request to this service via HTTP, which then executes the corresponding OPC UA write operation.

## Architecture

```
AAS Web UI → BaSyx Environment → Operation Delegation → This Service → OPC UA Server
```

### How It Works

1. **AAS Operation Definition**: Operations in the AASX file have a `Qualifier` with:
   - `type`: `"invocationDelegation"`
   - `value`: `"http://opcua-operation-service:8087/crane/hoist-down"` (service URL)

2. **BaSyx Delegation**: When operation is invoked, BaSyx checks for the delegation qualifier and forwards the request

3. **This Service**: Receives the HTTP POST request and translates it to OPC UA write operations

4. **OPC UA**: Writes boolean pulse (true → 5 seconds → false) to crane control nodes

## Supported Operations

| Operation | Endpoint | OPC UA Node |
|-----------|----------|-------------|
| HoistDown | `/crane/hoist-down` | `ns=7;s=SCF.PLC.DX_Custom_V.Controls.Hoist.Down` |
| HoistUp | `/crane/hoist-up` | `ns=7;s=SCF.PLC.DX_Custom_V.Controls.Hoist.Up` |
| TrolleyLeft | `/crane/trolley-left` | `ns=7;s=SCF.PLC.DX_Custom_V.Controls.Trolley.Left` |
| TrolleyRight | `/crane/trolley-right` | `ns=7;s=SCF.PLC.DX_Custom_V.Controls.Trolley.Right` |

## Configuration

Edit `src/main/resources/application.yml`:

```yaml
opcua:
  endpoint: opc.tcp://host.docker.internal:4840  # OPC UA server address
```

**Note**: `host.docker.internal` is used to access the host machine from inside Docker.

## Building

### Standalone JAR
```bash
mvn clean package
java -jar target/opcua-operation-service-1.0.0.jar
```

### Docker Image
```bash
docker build -t opcua-operation-service:1.0.0 .
```

## Running

### With Docker Compose (Recommended)
The service is already configured in the main `docker-compose.yml`:

```yaml
opcua-operation-service:
  build:
    context: ./opcua-operation-service
  container_name: opcua-operation-service
  ports:
    - '8087:8087'
  restart: always
```

Start with:
```bash
cd ../  # Go to basyx-setup root
docker-compose up -d opcua-operation-service
```

### Standalone
```bash
mvn spring-boot:run
```

## API Examples

### Invoke HoistDown (default 5s duration)
```bash
curl -X POST http://localhost:8087/crane/hoist-down \
  -H "Content-Type: application/json"
```

### Invoke HoistDown with custom duration
```bash
curl -X POST http://localhost:8087/crane/hoist-down \
  -H "Content-Type: application/json" \
  -d '[
    {
      "value": {
        "modelType": "Property",
        "idShort": "duration",
        "value": "10000",
        "valueType": "xs:long"
      }
    }
  ]'
```

### Expected Response
```json
[
  {
    "value": {
      "modelType": "Property",
      "idShort": "status",
      "value": "SUCCESS",
      "valueType": "xs:string"
    }
  },
  {
    "value": {
      "modelType": "Property",
      "idShort": "message",
      "value": "HoistDown executed successfully",
      "valueType": "xs:string"
    }
  },
  {
    "value": {
      "modelType": "Property",
      "idShort": "duration_ms",
      "value": "5000",
      "valueType": "xs:long"
    }
  }
]
```

## Integration with BaSyx

To enable operation delegation in your AASX file, add operations with delegation qualifiers. Example JSON (to add via REST API or AASX editor):

```json
{
  "modelType": "Operation",
  "idShort": "HoistDown",
  "description": [
    {
      "language": "en",
      "text": "Lower the crane hoist"
    }
  ],
  "qualifiers": [
    {
      "type": "invocationDelegation",
      "value": "http://opcua-operation-service:8087/crane/hoist-down"
    }
  ],
  "inputVariables": [
    {
      "value": {
        "modelType": "Property",
        "idShort": "duration_ms",
        "valueType": "xs:long",
        "description": [{"language": "en", "text": "Pulse duration in milliseconds"}]
      }
    }
  ],
  "outputVariables": [
    {
      "value": {
        "modelType": "Property",
        "idShort": "status",
        "valueType": "xs:string"
      }
    },
    {
      "value": {
        "modelType": "Property",
        "idShort": "message",
        "valueType": "xs:string"
      }
    }
  ]
}
```

## Advantages Over Trigger Property Approach

✅ **Standard AAS Pattern**: Uses proper Operation elements  
✅ **Event-Driven**: No polling overhead  
✅ **Cleaner API**: Standard operation invocation via Web UI  
✅ **Parameters**: Can pass duration and other parameters  
✅ **Return Values**: Get execution status and results  
✅ **Web UI Support**: Operations show as clickable buttons  

## Troubleshooting

### Service can't connect to OPC UA
- Check OPC UA server is running on host
- Verify `host.docker.internal` resolves (Windows/Mac Docker Desktop feature)
- For Linux, use `--add-host=host.docker.internal:host-gateway` in docker run

### Operation not delegated
- Check BaSyx feature is enabled (enabled by default)
- Verify qualifier type is exactly `"invocationDelegation"`
- Check service URL is accessible from BaSyx container
- Review BaSyx logs for delegation errors

### Pulse not visible in OPC UA
- Increase `PULSE_DURATION_MS` in code
- Check OPC UA client refresh rate
- Verify node permissions allow writes

## Development

### Adding New Operations

1. Add OPC UA node constant
2. Create POST endpoint in `CraneOperationController`
3. Implement OPC UA write logic
4. Add operation to AASX with delegation qualifier
5. Test via Web UI or REST API

### Logging

Check logs:
```bash
docker logs opcua-operation-service
```

## Next Steps

1. Update your AASX file to include operations with `invocationDelegation` qualifiers
2. Restart BaSyx environment to load updated AASX
3. Test operations from AAS Web UI
4. Monitor OPC UA changes in UaExpert
