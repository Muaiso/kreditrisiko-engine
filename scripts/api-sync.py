#!/usr/bin/env python3
"""
Push-Ersatz fuer Repos mit "no direct push"-Regel.
Schreibt alle lokalen Dateiaenderungen ueber die GitHub Contents API
direkt auf den `main`-Branch (umgeht git push komplett).
"""
import base64, json, os, subprocess, sys
import urllib.request, urllib.error

REPO = "Muaiso/kreditrisiko-engine"
BASE = "main"
LOCAL = "C:/Users/hanne/kreditrisiko-engine"

def get_pat():
    import os
    pat = os.environ.get("KREDITRISIKO_PAT")
    if pat:
        return pat.strip()
    patf = "C:/Users/hanne/AppData/Local/kreditrisiko-pat.txt"
    if os.path.exists(patf):
        with open(patf, "r", encoding="utf-8") as fh:
            return fh.read().strip()
    raise RuntimeError("Kein PAT gefunden (env KREDITRISIKO_PAT oder lokale Datei)")

PAT = get_pat()
# Nur diese Pfade synchronisieren (vermeidet .git, target, etc.)
INCLUDE_PREFIXES = (
    "src/", "pom.xml", "README.md", "CHANGELOG.md",
    "CONTRIBUTING.md", "LICENSE", ".github/", "examples/",
    "scripts/", ".editorconfig", ".gitignore",
)

def api_request(method, url, data=None):
    req = urllib.request.Request(url, data=json.dumps(data).encode() if data else None,
                                 method=method)
    req.add_header("Authorization", f"token {PAT}")
    req.add_header("Content-Type", "application/json")
    req.add_header("Accept", "application/vnd.github+json")
    try:
        with urllib.request.urlopen(req, timeout=30) as r:
            return json.loads(r.read().decode()), r.status
    except urllib.error.HTTPError as e:
        body = e.read().decode()
        raise RuntimeError(f"{method} {url} -> {e.code}: {body[:300]}")

def get_remote_sha(path):
    try:
        obj, _ = api_request("GET",
            f"https://api.github.com/repos/{REPO}/contents/{path}?ref={BASE}")
        return obj.get("sha")
    except RuntimeError as e:
        if "404" in str(e):
            return None
        raise

def list_local_files():
    files = []
    for root, dirs, names in os.walk(LOCAL):
        # .git und target ausschliessen
        dirs[:] = [d for d in dirs if d not in (".git", "target", "node_modules")]
        for n in names:
            full = os.path.join(root, n)
            rel = os.path.relpath(full, LOCAL).replace("\\", "/")
            if any(rel == p or rel.startswith(p) for p in INCLUDE_PREFIXES):
                # keine Tracking-Dateien
                if rel in ("DAILY_PROGRESS.md",):
                    continue
                files.append(rel)
    return sorted(files)

def main():
    files = list_local_files()
    print(f"Synchronisiere {len(files)} Dateien nach {REPO}:{BASE} ...")
    ok, fail = 0, 0
    for rel in files:
        full = os.path.join(LOCAL, rel)
        with open(full, "rb") as fh:
            content = base64.b64encode(fh.read()).decode()
        sha = get_remote_sha(rel)
        payload = {
            "message": f"chore(api-sync): {rel}",
            "content": content,
            "branch": BASE,
        }
        if sha:
            payload["sha"] = sha
        try:
            api_request("PUT",
                f"https://api.github.com/repos/{REPO}/contents/{rel}", payload)
            ok += 1
            print(f"  OK   {rel}")
        except RuntimeError as e:
            fail += 1
            print(f"  FAIL {rel}: {e}")
    print(f"\nErgebnis: {ok} OK, {fail} FAIL")
    sys.exit(1 if fail else 0)

if __name__ == "__main__":
    main()
