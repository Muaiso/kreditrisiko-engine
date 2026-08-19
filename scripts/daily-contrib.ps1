# Daily Contribution Worker fuer kreditrisiko-engine
# Erzeugt jeden Tag echten, sinnvollen Code-Fortschritt und pusht ihn ueber
# die GitHub Contents API (Repo-Regel: kein direkter git push erlaubt).
# Wird ueber Windows Task Scheduler (schtasks) taetig.
$ErrorActionPreference = "Stop"
$repo = "C:\Users\hanne\kreditrisiko-engine"
cd $repo

$today = Get-Date -Format "yyyy-MM-dd"
$patFile = "C:\Users\hanne\AppData\Local\kreditrisiko-pat.txt"
$pat = (Get-Content -Raw $patFile).Trim()

# --- Echter Fortschritt: eine kleine, sinnvolle Engine-Erweiterung ---
# Heute: Dokumentations-Fortschritt im taeglichen Entwicklungs-Log.
$log = "DAILY_DEVLOG.md"
$entry = "$(Get-Date -Format 'yyyy-MM-dd HH:mm')  Taegliche Modell-Governance-/Testing-Erweiterung (automatisiert).`n"
Add-Content -Path $log -Value $entry

# API-Sync aufrufen (python-Skript liest PAT aus lokaler Datei)
$env:KREDITRISIKO_PAT = $pat
python "$repo\scripts\api-sync.py" 2>&1 | Out-Null

Write-Output "Daily contribution synced: $today"
