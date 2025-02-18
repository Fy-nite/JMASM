#!/bin/env pwsh
# Script to generate documentation from .masm file comments
param(
    [Parameter(Mandatory=$true)]
    [string]$SourceDir,
    
    [Parameter(Mandatory=$true)]
    [string]$OutputDir
)


# Create docs directory if it doesn't exist
if (!(Test-Path $OutputDir)) {
    New-Item -ItemType Directory -Path $OutputDir
}

# Recursively get all .masm files
Get-ChildItem -Path $SourceDir -Filter "*.masm" -Recurse | ForEach-Object {
    $relativePath = $_.FullName.Replace($SourceDir, "").TrimStart("\")
    $folderPath = Split-Path $relativePath
    
    # Create output folder structure if it doesn't exist
    $targetFolder = Join-Path $OutputDir $folderPath
    if (!(Test-Path $targetFolder)) {
        New-Item -ItemType Directory -Path $targetFolder -Force
    }
    
    # Create markdown file for each .masm file
    $outputFile = Join-Path $OutputDir ($relativePath -replace '\.masm$', '.md')
    
    # Initialize the markdown file with a header
    @"
# $(Split-Path $_.Name -LeafBase) Documentation
Generated on $(Get-Date -Format "yyyy-MM-dd HH:mm:ss")

This documentation is automatically generated from comments in the MASM file.

"@ | Set-Content $outputFile
    
    # Add file content and comments
    Add-Content $outputFile "``````masm"
    
    $comments = @()
    $content = Get-Content $_.FullName
    
    foreach ($line in $content) {
        if ($line -match "^;\s*(.+)") {
            $comments += $matches[1].Trim()
        }
    }
    
    Add-Content $outputFile $content
    Add-Content $outputFile "``````"
    
    if ($comments.Count -gt 0) {
        Add-Content $outputFile "`nComments found in this file:"
        foreach ($comment in $comments) {
            Add-Content $outputFile "- $comment"
        }
    }
    
    Write-Host "Documentation generated for $relativePath"
}