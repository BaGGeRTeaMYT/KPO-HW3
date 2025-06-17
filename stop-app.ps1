# Script for stopping microservices shop application

param(
    [switch]$RemoveVolumes,
    [switch]$RemoveImages,
    [switch]$Force
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
        docker --version | Out-Null
        return $true
    }
    catch {
        Write-Error "Docker not found"
        return $false
    }
}

function Stop-Services {
    Write-Info "Stopping microservices..."
    
    if ($RemoveVolumes) {
        docker-compose down -v
        Write-Info "Containers and volumes removed"
    } else {
        docker-compose down
        Write-Info "Containers stopped"
    }
    
    if ($LASTEXITCODE -ne 0) {
        Write-Warning "Containers might already be stopped"
    }
}

function Remove-Images {
    if ($RemoveImages) {
        Write-Info "Removing Docker images..."
        
        $images = @(
            "kpo-hw3-api-gateway",
            "kpo-hw3-orders-service", 
            "kpo-hw3-payments-service"
        )
        
        foreach ($image in $images) {
            try {
                docker rmi $image -f 2>$null
                Write-Info "Image $image removed"
            }
            catch {
                Write-Warning "Image $image not found or cannot be removed"
            }
        }
    }
}

function Cleanup-Resources {
    if ($Force) {
        Write-Info "Force cleaning resources..."
        
        # Stop all containers
        docker stop $(docker ps -q) 2>$null
        
        # Remove all containers
        docker rm $(docker ps -aq) 2>$null
        
        # Remove unused networks
        docker network prune -f 2>$null
        
        # Remove unused volumes
        docker volume prune -f 2>$null
        
        Write-Success "Resources cleaned"
    }
}

function Show-Status {
    Write-Host ""
    Write-Host "=" * 60
    Write-Host "STATUS AFTER STOPPING" -ForegroundColor Green
    Write-Host "=" * 60
    
    docker-compose ps 2>$null
    
    if ($LASTEXITCODE -ne 0) {
        Write-Success "All containers stopped"
    }
    
    Write-Host ""
    Write-Host "=" * 60
    Write-Host "INFORMATION" -ForegroundColor Green
    Write-Host "=" * 60
    
    if ($RemoveVolumes) {
        Write-Warning "All database data removed"
        Write-Info "Database will be reinitialized on next startup"
    } else {
        Write-Info "Database data preserved"
        Write-Info "Use -RemoveVolumes parameter for full cleanup"
    }
    
    if ($RemoveImages) {
        Write-Info "Docker images removed"
        Write-Info "Images will be rebuilt on next startup"
    }
    
    Write-Host ""
    Write-Host "To start application use: .\start-app.ps1" -ForegroundColor Cyan
}

function Main {
    Write-Host ""
    Write-Host "=" * 60
    Write-Host "STOPPING MICROSERVICES APPLICATION" -ForegroundColor Green
    Write-Host "=" * 60
    Write-Host ""
    
    # Check Docker
    if (-not (Test-Docker)) {
        exit 1
    }
    
    # Confirm volume removal
    if ($RemoveVolumes -and -not $Force) {
        Write-Warning "This will remove ALL database data!"
        $response = Read-Host "Continue? (y/N)"
        if ($response -ne "y" -and $response -ne "Y") {
            Write-Info "Operation cancelled"
            exit 0
        }
    }
    
    # Confirm image removal
    if ($RemoveImages -and -not $Force) {
        Write-Warning "This will remove Docker images!"
        $response = Read-Host "Continue? (y/N)"
        if ($response -ne "y" -and $response -ne "Y") {
            Write-Info "Operation cancelled"
            exit 0
        }
    }
    
    # Stop services
    Stop-Services
    
    # Remove images
    Remove-Images
    
    # Cleanup resources
    Cleanup-Resources
    
    # Show status
    Show-Status
    
    Write-Host ""
    Write-Success "Application stopped successfully!"
    Write-Host ""
}

# Run main script
Main 