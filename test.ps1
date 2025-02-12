$actions = @(
    { Write-Host "Random message: Hello, world!" },
    { Write-Host "Random number: $(Get-Random -Minimum 1 -Maximum 100)" },
    { Get-Date | Write-Host },
    { Get-Process | Sort-Object CPU -Descending | Select-Object -First 5 | Format-Table -AutoSize | Out-String | Write-Host },
    { "ASCII Art: \n  (\_/)", "  (o.o)", "  (> <)" | ForEach-Object { Write-Host $_ } },
    { Write-Host "System Uptime: $(Get-Uptime)" },
    { Write-Host "Random color text!" -ForegroundColor (Get-Random -InputObject @("Red", "Green", "Blue", "Yellow", "Cyan", "Magenta")) },
    { Write-Host "Random joke: Why did the PowerShell script cross the road? To execute on the other side!" }
)

$iterations = 10

for ($i = 0; $i -lt $iterations; $i++) {
    $randomAction = Get-Random -InputObject $actions
    & $randomAction
    Start-Sleep -Seconds 0.1
}
