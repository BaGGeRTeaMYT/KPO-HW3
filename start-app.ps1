# Script for starting microservices shop application
# Requires administrator privileges

param(
    [switch]$SkipBuild,
    [switch]$SkipTests,
    [switch]$Verbose
)

# Color output setup
$Host.UI.RawUI.ForegroundColor = "White"

function Write-Info {
    param([string]$Message)
    $Host.UI.RawUI.ForegroundColor = "Cyan"
    Write-Host "[INFO] $Message" -ForegroundColor Cyan
    $Host.UI.RawUI.ForegroundColor = "White"
}

function Write-Success {
    param([string]$Message)
    $Host.UI.RawUI.ForegroundColor = "Green"
    Write-Host "[SUCCESS] $Message" -ForegroundColor Green
    $Host.UI.RawUI.ForegroundColor = "White"
}

function Write-Warning {
    param([string]$Message)
    $Host.UI.RawUI.ForegroundColor = "Yellow"
    Write-Host "[WARNING] $Message" -ForegroundColor Yellow
    $Host.UI.RawUI.ForegroundColor = "White"
}

function Write-Error {
    param([string]$Message)
    $Host.UI.RawUI.ForegroundColor = "Red"
    Write-Host "[ERROR] $Message" -ForegroundColor Red
    $Host.UI.RawUI.ForegroundColor = "White"
}

function Test-Docker {
    try {
        $dockerVersion = docker --version
        Write-Success "Docker found: $dockerVersion"
        return $true
    }
    catch {
        Write-Error "Docker not found. Install Docker Desktop and restart PowerShell."
        return $false
    }
}

function Test-DockerRunning {
    try {
        docker info | Out-Null
        Write-Success "Docker Desktop is running"
        return $true
    }
    catch {
        Write-Error "Docker Desktop is not running. Start Docker Desktop and try again."
        return $false
    }
}

function Test-Ports {
    $ports = @(8080, 8081, 8082, 5432, 5672, 15672)
    $occupiedPorts = @()
    
    foreach ($port in $ports) {
        try {
            $connection = Test-NetConnection -ComputerName localhost -Port $port -WarningAction SilentlyContinue -InformationLevel Quiet
            if ($connection.TcpTestSucceeded) {
                $occupiedPorts += $port
            }
        }
        catch {
            # Port is free
        }
    }
    
    if ($occupiedPorts.Count -gt 0) {
        Write-Warning "Following ports are occupied: $($occupiedPorts -join ', ')"
        Write-Warning "Make sure application is not running in another terminal"
        $response = Read-Host "Continue? (y/N)"
        if ($response -ne "y" -and $response -ne "Y") {
            exit 1
        }
    }
    else {
        Write-Success "All required ports are free"
    }
}

function Build-Services {
    if ($SkipBuild) {
        Write-Info "Skipping service build"
        return
    }
    
    Write-Info "Starting service build..."
    
    # Build API Gateway
    Write-Info "Building API Gateway..."
    Set-Location "api-gateway"
    if (-not $SkipTests) {
        mvn clean package -DskipTests
    } else {
        mvn clean package
    }
    if ($LASTEXITCODE -ne 0) {
        Write-Error "API Gateway build failed"
        exit 1
    }
    Set-Location ".."
    
    # Build Orders Service
    Write-Info "Building Orders Service..."
    Set-Location "orders-service"
    if (-not $SkipTests) {
        mvn clean package -DskipTests
    } else {
        mvn clean package
    }
    if ($LASTEXITCODE -ne 0) {
        Write-Error "Orders Service build failed"
        exit 1
    }
    Set-Location ".."
    
    # Build Payments Service
    Write-Info "Building Payments Service..."
    Set-Location "payments-service"
    if (-not $SkipTests) {
        mvn clean package -DskipTests
    } else {
        mvn clean package
    }
    if ($LASTEXITCODE -ne 0) {
        Write-Error "Payments Service build failed"
        exit 1
    }
    Set-Location ".."
    
    Write-Success "All services built successfully"
}

function Start-Infrastructure {
    Write-Info "Starting infrastructure (PostgreSQL, RabbitMQ)..."
    
    docker-compose up postgres rabbitmq -d
    
    if ($LASTEXITCODE -ne 0) {
        Write-Error "Infrastructure startup failed"
        exit 1
    }
    
    # Wait for PostgreSQL readiness
    Write-Info "Waiting for PostgreSQL readiness..."
    $maxAttempts = 30
    $attempt = 0
    
    do {
        Start-Sleep -Seconds 2
        $attempt++
        try {
            $result = docker-compose exec -T postgres pg_isready -U postgres
            if ($result -like "*accepting connections*") {
                Write-Success "PostgreSQL is ready"
                break
            }
        }
        catch {
            # Ignore errors
        }
    } while ($attempt -lt $maxAttempts)
    
    if ($attempt -eq $maxAttempts) {
        Write-Warning "PostgreSQL not ready, but continuing..."
    }
    
    # Wait for RabbitMQ readiness
    Write-Info "Waiting for RabbitMQ readiness..."
    Start-Sleep -Seconds 10
    
    Write-Success "Infrastructure started"
}

function Start-Services {
    Write-Info "Starting microservices..."
    
    docker-compose up --build -d
    
    if ($LASTEXITCODE -ne 0) {
        Write-Error "Service startup failed"
        exit 1
    }
    
    Write-Success "Services started"
}

function Wait-ForServices {
    Write-Info "Waiting for service readiness..."
    
    $services = @(
        @{Name="API Gateway"; Url="http://localhost:8080/actuator/health"},
        @{Name="Orders Service"; Url="http://localhost:8081/actuator/health"},
        @{Name="Payments Service"; Url="http://localhost:8082/actuator/health"}
    )
    
    foreach ($service in $services) {
        Write-Info "Checking $($service.Name)..."
        $maxAttempts = 30
        $attempt = 0
        
        do {
            Start-Sleep -Seconds 2
            $attempt++
            try {
                $response = Invoke-WebRequest -Uri $service.Url -TimeoutSec 5 -UseBasicParsing
                if ($response.StatusCode -eq 200) {
                    Write-Success "$($service.Name) is ready"
                    break
                }
            }
            catch {
                if ($attempt -eq $maxAttempts) {
                    Write-Warning "$($service.Name) not ready, but continuing..."
                }
            }
        } while ($attempt -lt $maxAttempts)
    }
}

function Show-Status {
    Write-Host ""
    Write-Host "=" * 60
    Write-Host "APPLICATION STATUS" -ForegroundColor Green
    Write-Host "=" * 60
    
    docker-compose ps
    
    Write-Host ""
    Write-Host "=" * 60
    Write-Host "AVAILABLE SERVICES" -ForegroundColor Green
    Write-Host "=" * 60
    Write-Host "API Gateway:     http://localhost:8080" -ForegroundColor Cyan
    Write-Host "Orders Service:  http://localhost:8081" -ForegroundColor Cyan
    Write-Host "Payments Service: http://localhost:8082" -ForegroundColor Cyan
    Write-Host "RabbitMQ Admin:  http://localhost:15672" -ForegroundColor Cyan
    Write-Host "PostgreSQL:      localhost:5432" -ForegroundColor Cyan
    
    Write-Host ""
    Write-Host "=" * 60
    Write-Host "SWAGGER UI" -ForegroundColor Green
    Write-Host "=" * 60
    Write-Host "Orders Service:  http://localhost:8080/swagger-ui/index.html" -ForegroundColor Yellow
    Write-Host "Payments Service: http://localhost:8080/payments-swagger-ui/index.html" -ForegroundColor Yellow
    
    Write-Host ""
    Write-Host "=" * 60
    Write-Host "USEFUL COMMANDS" -ForegroundColor Green
    Write-Host "=" * 60
    Write-Host "View logs:       docker-compose logs -f [service-name]" -ForegroundColor Yellow
    Write-Host "Stop:            .\stop-app.ps1" -ForegroundColor Yellow
    Write-Host "Restart:         docker-compose restart [service-name]" -ForegroundColor Yellow
    
    Write-Host ""
    Write-Host "=" * 60
    Write-Host "API TESTING" -ForegroundColor Green
    Write-Host "=" * 60
    Write-Host "Create order:" -ForegroundColor Yellow
    Write-Host 'curl -X POST http://localhost:8080/api/orders -H "Content-Type: application/json" -H "X-User-ID: 1" -d "{\"amount\": 100.50}"' -ForegroundColor Gray
    
    Write-Host ""
    Write-Host "Create account:" -ForegroundColor Yellow
    Write-Host 'curl -X POST http://localhost:8080/api/payments/accounts -H "Content-Type: application/json" -H "X-User-ID: 1"' -ForegroundColor Gray
    
    Write-Host ""
    Write-Host "Deposit money:" -ForegroundColor Yellow
    Write-Host 'curl -X POST http://localhost:8080/api/payments/accounts/1/deposit -H "Content-Type: application/json" -H "X-User-ID: 1" -d "{\"amount\": 500.00}"' -ForegroundColor Gray
}

function Main {
    Write-Host ""
    Write-Host "=" * 60
    Write-Host "STARTING MICROSERVICES APPLICATION" -ForegroundColor Green
    Write-Host "=" * 60
    Write-Host ""
    
    # Check administrator privileges
    # if (-NOT ([Security.Principal.WindowsPrincipal] [Security.Principal.WindowsIdentity]::GetCurrent()).IsInRole([Security.Principal.WindowsBuiltInRole] "Administrator")) {
    #     Write-Error "Script must be run as administrator"
    #     exit 1
    # }
    
    # Check Docker
    if (-not (Test-Docker)) {
        exit 1
    }
    
    if (-not (Test-DockerRunning)) {
        exit 1
    }
    
    # Check ports
    Test-Ports
    
    # Stop existing containers
    Write-Info "Stopping existing containers..."
    docker-compose down 2>$null
    
    # Build services
    Build-Services
    
    # Start infrastructure
    Start-Infrastructure
    
    # Start services
    Start-Services
    
    # Wait for readiness
    Wait-ForServices
    
    # Show status
    Show-Status
    
    Write-Host ""
    Write-Success "Application started successfully!"
    Write-Host ""
}

# Run main script
Main 