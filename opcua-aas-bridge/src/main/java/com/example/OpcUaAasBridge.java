package com.example;

import org.eclipse.milo.opcua.sdk.client.OpcUaClient;
import org.eclipse.milo.opcua.sdk.client.api.subscriptions.UaMonitoredItem;
import org.eclipse.milo.opcua.sdk.client.api.subscriptions.UaSubscription;
import org.eclipse.milo.opcua.stack.core.AttributeId;
import org.eclipse.milo.opcua.stack.core.types.builtin.DataValue;
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;
import org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.UInteger;
import org.eclipse.milo.opcua.stack.core.types.enumerated.MonitoringMode;
import org.eclipse.milo.opcua.stack.core.types.enumerated.TimestampsToReturn;
import org.eclipse.milo.opcua.stack.core.types.structured.MonitoredItemCreateRequest;
import org.eclipse.milo.opcua.stack.core.types.structured.MonitoringParameters;
import org.eclipse.milo.opcua.stack.core.types.structured.ReadValueId;

import okhttp3.*;
import com.google.gson.JsonObject;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class OpcUaAasBridge {
    
    private static final String OPC_UA_ENDPOINT = "opc.tcp://localhost:4840";
    private static final String OPC_UA_NODE_ID = "ns=7;s=SCF.PLC.DX_Custom_V.Controls.Hoist.Speed";
    
    // Base64 encoded submodel ID: aHR0cHM6Ly9leGFtcGxlLmNvbS9pZHMvc20vNTAxMF81MTUwXzExNTJfMTEwMg
    private static final String SUBMODEL_ID = "aHR0cHM6Ly9leGFtcGxlLmNvbS9pZHMvc20vNTAxMF81MTUwXzExNTJfMTEwMg";
    private static final String PROPERTY_ID_SHORT = "Hoist_Speed";
    private static final String AAS_ENDPOINT = "http://localhost:8081";
    
    private static final OkHttpClient httpClient = new OkHttpClient();

    public static void main(String[] args) throws Exception {
        System.out.println("Starting OPC UA to AAS Bridge...");
        
        // Create OPC UA client
        OpcUaClient client = OpcUaClient.create(OPC_UA_ENDPOINT);
        client.connect().get();
        System.out.println("Connected to OPC UA server: " + OPC_UA_ENDPOINT);

        // Create subscription
        UaSubscription subscription = client.getSubscriptionManager()
            .createSubscription(1000.0).get();
        System.out.println("Created OPC UA subscription");

        // Create monitored item for the node
        ReadValueId readValueId = new ReadValueId(
            NodeId.parse(OPC_UA_NODE_ID),
            AttributeId.Value.uid(),
            null,
            null
        );

        MonitoringParameters parameters = new MonitoringParameters(
            UInteger.valueOf(1),  // client handle
            1000.0,               // sampling interval
            null,                 // filter
            UInteger.valueOf(10), // queue size
            true                  // discard oldest
        );

        MonitoredItemCreateRequest request = new MonitoredItemCreateRequest(
            readValueId,
            MonitoringMode.Reporting,
            parameters
        );

        // Subscribe to value changes
        List<UaMonitoredItem> items = subscription
            .createMonitoredItems(
                TimestampsToReturn.Both,
                List.of(request),
                (item, id) -> item.setValueConsumer((monitoredItem, value) -> {
                    try {
                        updateAasProperty(value);
                    } catch (Exception e) {
                        System.err.println("Error updating AAS: " + e.getMessage());
                        e.printStackTrace();
                    }
                })
            ).get();

        System.out.println("Monitoring OPC UA node: " + OPC_UA_NODE_ID);
        System.out.println("Bridge is running. Press Ctrl+C to stop.");

        // Keep the application running
        CompletableFuture<Void> future = new CompletableFuture<>();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                client.disconnect().get();
                System.out.println("Disconnected from OPC UA server");
            } catch (Exception e) {
                e.printStackTrace();
            }
            future.complete(null);
        }));
        
        future.get();
    }

    private static void updateAasProperty(DataValue value) throws Exception {
        Object opcValue = value.getValue().getValue();
        System.out.println("OPC UA value changed: " + opcValue);

        // Build AAS property value update URL for BaSyx v2 using idShortPath
        String url = String.format("%s/submodels/%s/submodel-elements/%s",
            AAS_ENDPOINT, SUBMODEL_ID, PROPERTY_ID_SHORT);

        // Get current property and update just the value field
        Request getRequest = new Request.Builder()
            .url(url)
            .get()
            .build();

        String propertyJson;
        try (Response getResponse = httpClient.newCall(getRequest).execute()) {
            if (!getResponse.isSuccessful()) {
                System.err.println("Failed to get property. Status: " + getResponse.code());
                return;
            }
            propertyJson = getResponse.body().string();
        }

        // Parse and update the value
        JsonObject property = com.google.gson.JsonParser.parseString(propertyJson).getAsJsonObject();
        property.addProperty("value", opcValue.toString());

        // PUT the updated property back
        RequestBody body = RequestBody.create(
            property.toString(),
            MediaType.parse("application/json")
        );

        Request putRequest = new Request.Builder()
            .url(url)
            .put(body)
            .build();

        try (Response response = httpClient.newCall(putRequest).execute()) {
            if (response.isSuccessful()) {
                System.out.println("Successfully updated AAS property: " + opcValue);
            } else {
                System.err.println("Failed to update AAS. Status: " + response.code() + 
                    ", Body: " + response.body().string());
            }
        }
    }
}
