#!/bin/env pwsh
param(
    [string]$Product = ""
)

$products = @{
    "product1" = @{
        "url" = "https://example.com/downloads/product1.zip"
        "name" = "Product 1"
    }
    "product2" = @{
        "url" = "https://example.com/downloads/product2.zip"
        "name" = "Product 2"
    }
    "product3" = @{
        "url" = "https://example.com/downloads/product3.zip"
        "name" = "Product 3"
    }
}

function Show-Products {
    Write-Host "Available products:"
    foreach ($key in $products.Keys) {
        Write-Host "- $key : $($products[$key].name)"
    }
}

function Install-Product {
    param($ProductName)
    
    if (-not $products.ContainsKey($ProductName)) {
        Write-Host "Error: Product '$ProductName' not found!"
        Show-Products
        return
    }

    $productInfo = $products[$ProductName]
    Write-Host "Downloading $($productInfo.name) from: $($productInfo.url)"
    
    try {
        $outFile = Join-Path $env:TEMP "$ProductName.zip"
        Invoke-WebRequest -Uri $productInfo.url -OutFile $outFile
        Write-Host "Download complete! File saved to: $outFile"
        

        Write-Host "Installing $($productInfo.name)..."
        Expand-Archive -Path $outFile -DestinationPath .        
    } catch {
        Write-Host "Error downloading/installing product: $_"
    }
}

if ($Product -eq "") {
    Show-Products
} else {
    Install-Product -ProductName $Product
}