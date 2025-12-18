package com.konecranes.opcua.controller;

import com.konecranes.opcua.service.OpcUaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * REST controller for crane operations delegated from AAS
 * Implements the BaSyx Operation Delegation pattern
 */
@RestController
public class CraneOperationController {

    private static final Logger logger = LoggerFactory.getLogger(CraneOperationController.class);

    // OPC UA Node IDs for crane controls
    private static final String HOIST_DOWN_NODE = "ns=7;s=SCF.PLC.DX_Custom_V.Controls.Hoist.Down";
    private static final String HOIST_UP_NODE = "ns=7;s=SCF.PLC.DX_Custom_V.Controls.Hoist.Up";
    private static final String TROLLEY_FORWARD_NODE = "ns=7;s=SCF.PLC.DX_Custom_V.Controls.Trolley.Forward";
    private static final String TROLLEY_BACKWARD_NODE = "ns=7;s=SCF.PLC.DX_Custom_V.Controls.Trolley.Backward";
    private static final String BRIDGE_FORWARD_NODE = "ns=7;s=SCF.PLC.DX_Custom_V.Controls.Bridge.Forward";
    private static final String BRIDGE_BACKWARD_NODE = "ns=7;s=SCF.PLC.DX_Custom_V.Controls.Bridge.Backward";
    
    private static final long PULSE_DURATION_MS = 10000; // 10 seconds

    @Autowired
    private OpcUaService opcUaService;

    /**
     * Hoist Down operation - activates the hoist down control
     * Input: Optional duration parameter (default 10000ms)
     * Output: Success status message
     */
    @PostMapping(value = "/crane/hoist-down", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> hoistDown(@RequestBody(required = false) String input) {
        logger.info("Executing HoistDown operation");
        
        try {
            opcUaService.writePulse(HOIST_DOWN_NODE, PULSE_DURATION_MS);
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "SUCCESS");
            response.put("message", "HoistDown executed successfully");
            response.put("duration_ms", PULSE_DURATION_MS);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error executing HoistDown", e);
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "ERROR");
            errorResponse.put("error", "HoistDown failed: " + e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Hoist Up operation - activates the hoist up control
     * Input: Optional duration parameter (default 10000ms)
     * Output: Success status message
     */
    @PostMapping(value = "/crane/hoist-up", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> hoistUp(@RequestBody(required = false) String input) {
        logger.info("Executing HoistUp operation");
        
        try {
            opcUaService.writePulse(HOIST_UP_NODE, PULSE_DURATION_MS);
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "SUCCESS");
            response.put("message", "HoistUp executed successfully");
            response.put("duration_ms", PULSE_DURATION_MS);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error executing HoistUp", e);
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "ERROR");
            errorResponse.put("error", "HoistUp failed: " + e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Trolley Forward operation - moves trolley forward
     * Input: Optional duration parameter (default 10000ms)
     * Output: Success status message
     */
    @PostMapping(value = "/crane/trolley-forward", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> trolleyForward(@RequestBody(required = false) String input) {
        logger.info("Executing TrolleyForward operation");
        
        try {
            opcUaService.writePulse(TROLLEY_FORWARD_NODE, PULSE_DURATION_MS);
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "SUCCESS");
            response.put("message", "TrolleyForward executed successfully");
            response.put("duration_ms", PULSE_DURATION_MS);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error executing TrolleyForward", e);
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "ERROR");
            errorResponse.put("error", "TrolleyForward failed: " + e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Trolley Backward operation - moves trolley backward
     * Input: Optional duration parameter (default 10000ms)
     * Output: Success status message
     */
    @PostMapping(value = "/crane/trolley-backward", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> trolleyBackward(@RequestBody(required = false) String input) {
        logger.info("Executing TrolleyBackward operation");
        
        try {
            opcUaService.writePulse(TROLLEY_BACKWARD_NODE, PULSE_DURATION_MS);
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "SUCCESS");
            response.put("message", "TrolleyBackward executed successfully");
            response.put("duration_ms", PULSE_DURATION_MS);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error executing TrolleyBackward", e);
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "ERROR");
            errorResponse.put("error", "TrolleyBackward failed: " + e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Bridge Forward operation - moves bridge forward
     * Input: Optional duration parameter (default 10000ms)
     * Output: Success status message
     */
    @PostMapping(value = "/crane/bridge-forward", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> bridgeForward(@RequestBody(required = false) String input) {
        logger.info("Executing BridgeForward operation");
        
        try {
            opcUaService.writePulse(BRIDGE_FORWARD_NODE, PULSE_DURATION_MS);
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "SUCCESS");
            response.put("message", "BridgeForward executed successfully");
            response.put("duration_ms", PULSE_DURATION_MS);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error executing BridgeForward", e);
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "ERROR");
            errorResponse.put("error", "BridgeForward failed: " + e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Bridge Backward operation - moves bridge backward
     * Input: Optional duration parameter (default 10000ms)
     * Output: Success status message
     */
    @PostMapping(value = "/crane/bridge-backward", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> bridgeBackward(@RequestBody(required = false) String input) {
        logger.info("Executing BridgeBackward operation");
        
        try {
            opcUaService.writePulse(BRIDGE_BACKWARD_NODE, PULSE_DURATION_MS);
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "SUCCESS");
            response.put("message", "BridgeBackward executed successfully");
            response.put("duration_ms", PULSE_DURATION_MS);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error executing BridgeBackward", e);
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "ERROR");
            errorResponse.put("error", "BridgeBackward failed: " + e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
}

