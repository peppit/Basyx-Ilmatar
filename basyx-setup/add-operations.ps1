#!/usr/bin/env pwsh
# Script to add crane operations to BaSyx AAS for testing Operation Delegation

$submodelId = "aHR0cHM6Ly9leGFtcGxlLmNvbS9pZHMvc20vNTAxMF81MTUwXzExNTJfMTEwMg"
$baseUrl = "http://localhost:8081"

function Add-CraneOperation {
    param(
        [string]$IdShort,
        [string]$Description,
        [string]$DelegationUrl
    )

    $operation = @{
        modelType = "Operation"
        idShort = $IdShort
        description = @(
            @{
                language = "en"
                text = $Description
            }
        )
        qualifiers = @(
            @{
                type = "invocationDelegation"
                value = $DelegationUrl
            }
        )
        inputVariables = @(
            @{
                value = @{
                    modelType = "Property"
                    idShort = "duration_ms"
                    valueType = "xs:long"
                    value = "5000"
                    description = @(
                        @{
                            language = "en"
                            text = "Pulse duration in milliseconds"
                        }
                    )
                }
            }
        )
        outputVariables = @(
            @{
                value = @{
                    modelType = "Property"
                    idShort = "status"
                    valueType = "xs:string"
                }
            },
            @{
                value = @{
                    modelType = "Property"
                    idShort = "message"
                    valueType = "xs:string"
                }
            }
        )
    } | ConvertTo-Json -Depth 10

    Write-Host "Adding operation: $IdShort" -ForegroundColor Cyan
    try {
        $response = Invoke-WebRequest `
            -Uri "$baseUrl/submodels/$submodelId/submodel-elements" `
            -Method Post `
            -Body $operation `
            -ContentType "application/json"
        Write-Host "  ✓ $IdShort added successfully (Status: $($response.StatusCode))" -ForegroundColor Green
    } catch {
        if ($_.Exception.Response.StatusCode -eq 409) {
            Write-Host "  ⚠ $IdShort already exists (skipping)" -ForegroundColor Yellow
        } else {
            Write-Host "  ✗ Failed to add $IdShort : $($_.Exception.Message)" -ForegroundColor Red
        }
    }
}

# Main script
Write-Host "`n============================================" -ForegroundColor Magenta
Write-Host "  BaSyx AAS - Crane Operations Setup" -ForegroundColor Magenta
Write-Host "============================================`n" -ForegroundColor Magenta

Write-Host "Checking BaSyx connection..." -ForegroundColor Cyan
try {
    $health = Invoke-WebRequest -Uri "$baseUrl/shells" -Method Get -TimeoutSec 5
    Write-Host "  ✓ BaSyx is reachable`n" -ForegroundColor Green
} catch {
    Write-Host "  ✗ Cannot reach BaSyx at $baseUrl" -ForegroundColor Red
    Write-Host "  Make sure Docker containers are running: docker-compose up -d`n" -ForegroundColor Yellow
    exit 1
}

Write-Host "Adding crane operation to AAS...`n" -ForegroundColor Cyan

Add-CraneOperation `
    -IdShort "HoistDown" `
    -Description "Lower the crane hoist" `
    -DelegationUrl "http://opcua-operation-service:8087/crane/hoist-down"

Write-Host "`n============================================" -ForegroundColor Magenta
Write-Host "  Setup Complete!" -ForegroundColor Magenta
Write-Host "============================================`n" -ForegroundColor Magenta

Write-Host "Next steps:" -ForegroundColor Cyan
Write-Host "  1. Open AAS Web UI: http://localhost:3000" -ForegroundColor White
Write-Host "  2. Navigate to your crane AAS" -ForegroundColor White
Write-Host "  3. Find the submodel and click 'HoistDown' operation" -ForegroundColor White
Write-Host "  4. Click 'Execute' or 'Invoke'" -ForegroundColor White
Write-Host "  5. Watch OPC UA node 'Hoist.Down' change to true for 5 seconds`n" -ForegroundColor White

Write-Host "Test operation directly:" -ForegroundColor Cyan
Write-Host "  Invoke-WebRequest -Uri 'http://localhost:8087/crane/hoist-down' -Method Post`n" -ForegroundColor Gray

Write-Host "View operation service logs:" -ForegroundColor Cyan
Write-Host "  docker logs -f opcua-operation-service`n" -ForegroundColor Gray
