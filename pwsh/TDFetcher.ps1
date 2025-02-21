#!/bin/env pwsh
param (
    [Parameter(Mandatory=$false)]
    [string]$SourceDirectory,
    [string]$OutputFile = "TODO-Report.md"
)

if (-not $SourceDirectory) {
    if ($Host.UI.SupportsVirtualTerminal) {
        Write-Host "`e[31mError: SourceDirectory is required.`e[0m"
    } else {
        Write-Host "Error: SourceDirectory is required." -ForegroundColor Red
    }
    Write-Host "Usage: TDFetcher.ps1 -SourceDirectory <path> [-OutputFile <path>]"
    exit
}

# Regular expression pattern for matching different TODO formats
$todoPattern = '(?m)(?://|/\*|\*)\s*(?:TODO|FIXME|@TODO|todo)[\s:]+(.*?)(?:\r?\n|$)'

# Initialize the markdown content
$markdownContent = "# TODO Items Report`n`n"

# Get all Java files recursively
$javaFiles = Get-ChildItem -Path $SourceDirectory -Filter "*.java" -Recurse
Write-Host "Found $($javaFiles.Count) Java files"

foreach ($file in $javaFiles) {
    Write-Host "Processing file: $($file.FullName)"
    $content = Get-Content $file.FullName
    $lineNumber = 1
    $hasTodos = $false
    
    $relativePath = $file.FullName.Replace($SourceDirectory, '').TrimStart('\')
    
    foreach ($line in $content) {
        $matches = [regex]::Matches($line, $todoPattern)
        
        if ($matches.Count -gt 0) {
            Write-Host "Found TODO at line $lineNumber"
            if (!$hasTodos) {
                # Add file header to markdown only if we haven't yet
                $markdownContent += "## $relativePath`n`n"
                $hasTodos = $true
            }
            
            foreach ($match in $matches) {
                # Extract the actual TODO message
                $todoMessage = $match.Groups[1].Value.Trim()
                Write-Host "TODO message: $todoMessage"
                # Add TODO item to markdown with line number
                $markdownContent += ("- Line {0}: {1}`n" -f $lineNumber, $todoMessage)
            }
        }
        $lineNumber++
    }
    
    if ($hasTodos) {
        $markdownContent += "`n"
    }
}

# Save the markdown file
$markdownContent | Out-File -FilePath $OutputFile -Encoding UTF8

Write-Host "TODO report has been generated at: $OutputFile"
