#!/bin/env pwsh
# tester.ps1

# Run maven test and capture output
$mavenOutput = & mvn test 2>&1 | Out-String

# Initialize variables for statistics
$errorCount = 0
$errorDetails = @()
$mdContent = "# Maven Test Results Report`n`n"

# Process the output line by line
$isError = $false
$currentError = ""

foreach ($line in $mavenOutput -split "`r?`n") {
    if ($line -match '^\[ERROR\]') {
        $isError = $true
        $errorCount++
        $currentError = "$line`n"
    }
    elseif ($isError -and $line -match '^\[INFO\]|^\[WARNING\]|^$') {
        $isError = $false
        $errorDetails += $currentError
        $currentError = ""
    }
    elseif ($isError) {
        $currentError += "$line`n"
    }
}

# Add any remaining error
if ($currentError) {
    $errorDetails += $currentError
}

# Generate markdown content
if ($errorCount -gt 0) {
    $mdContent += "## Errors Found ($errorCount)`n`n"
    foreach ($error in $errorDetails) {
        $mdContent += "### Error $($errorDetails.IndexOf($error) + 1)`n"
        $mdContent += "```text`n$error````n`n"
    }
} else {
    $mdContent += "## No Errors Found`n`n"
}

# Add statistics
$mdContent += "## Statistics`n"
$mdContent += "- Total Errors: $errorCount`n"
$mdContent += "- Test Run Date: $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')`n"

# Save to markdown file
$mdContent | Out-File -FilePath "test-results.md" -Encoding UTF8

Write-Host "Report generated in test-results.md"
