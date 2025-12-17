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
}

