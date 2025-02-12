# Configuration - Replace these with your values
$repoPath = "src/"
$forgejoApiUrl = "https://git.gay/api/v1"
$forgejoToken = $env:FORGEJO_TOKEN
$repoOwner = "Finite"
$repoName = "JMASM"

# Array to store found TODOs
$todos = @()

# Get all Java files recursively
Get-ChildItem -Path $repoPath -Filter "*.java" -Recurse | ForEach-Object {
    $fileName = $_.FullName
    $lineNumber = 0
    
    # Read file content line by line
    Get-Content $fileName | ForEach-Object {
        $lineNumber++
        $line = $_
        
        # Look for TODO/FIXME patterns
        if ($line -match "(TODO|FIXME|todo|fixme):\s*(.+)") {
            $todoType = $matches[1]
            $todoText = $matches[2].Trim()
            
            $todos += @{
                File = $fileName
                Line = $lineNumber
                Type = $todoType
                Text = $todoText
            }
        }
    }
}

# Create Forgejo issues for each TODO
foreach ($todo in $todos) {
    $issueTitle = "[$($todo.Type)] $($todo.Text)"
    $issueBody = @"
Found in file: $($todo.File)
Line: $($todo.Line)

Original comment: $($todo.Text)
"@

    $headers = @{
        "Authorization" = "token $forgejoToken"
        "Content-Type" = "application/json"
    }

    $body = @{
        title = $issueTitle
        body = $issueBody
    } | ConvertTo-Json

    try {
        $apiEndpoint = "$forgejoApiUrl/repos/$repoOwner/$repoName/issues"
        Invoke-RestMethod -Uri $apiEndpoint -Method Post -Headers $headers -Body $body
        Write-Host "Created issue: $issueTitle"
    }
    catch {
        Write-Error "Failed to create issue for: $issueTitle"
        Write-Error $_.Exception.Message
    }
}

Write-Host "Found and processed $($todos.Count) TODOs/FIXMEs"