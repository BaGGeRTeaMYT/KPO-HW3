param(
    [switch]$Force
)

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
        docker --version | Out-Null
        return $true
    }
    catch {
        Write-Error "Docker not found"
        return $false
    }
}

function Clean-KafkaData {
    Write-Info "Stopping all containers..."
    docker-compose down 2>$null
    
    Write-Info "Removing Kafka and ZooKeeper volumes..."
    
    $volumes = @(
        "kpo-hw3_kafka_data",
        "kpo-hw3_zookeeper_data", 
        "kpo-hw3_zookeeper_logs"
    )
    
    foreach ($volume in $volumes) {
        try {
            docker volume rm $volume 2>$null
            Write-Info "Volume $volume removed"
        }
        catch {
            Write-Warning "Volume $volume not found or already removed"
        }
    }
    
    Write-Success "Kafka and ZooKeeper data cleaned successfully"
}

function Main {
    Write-Host ""
    Write-Host ("=" * 60)
    Write-Host "CLEANING KAFKA AND ZOOKEEPER DATA" -ForegroundColor Green
    Write-Host ("=" * 60)
    Write-Host ""
    
    if (-not (Test-Docker)) {
        exit 1
    }
    
    if (-not $Force) {
        Write-Warning "This will remove ALL Kafka and ZooKeeper data!"
        Write-Warning "This action cannot be undone!"
        $response = Read-Host "Continue? (y/N)"
        if ($response -ne "y" -and $response -ne "Y") {
            Write-Info "Operation cancelled"
            exit 0
        }
    }
    
    Clean-KafkaData
    
    Write-Host ""
    Write-Success "Kafka and ZooKeeper data cleaned successfully!"
    Write-Host ""
    Write-Info "You can now restart the application with: .\start-app.ps1"
    Write-Host ""
}

Main 