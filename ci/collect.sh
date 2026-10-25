#!/usr/bin/env bash
# Sammelt Nachschlage-Material für die Entwicklung (nur auf dem temporären Branch ci/dev-tools).
set -u
OUT=devtools
mkdir -p "$OUT"
ART=build/moddev/artifacts
ls -la "$ART" | tee "$OUT/artifacts-listing.txt"
cp "$ART"/*-sources.jar "$OUT/" 2>/dev/null || true
cp "$ART"/*client-extra*.jar "$OUT/" 2>/dev/null || true

# Vanilla-Assets (Sounds-Liste, deutsche Sprachdatei) aus dem Asset-Index
PROPS=build/moddev/minecraft_assets.properties
cat "$PROPS" | tee "$OUT/assets.properties"
python3 - "$PROPS" "$OUT" <<'EOF'
import json, os, sys, shutil
props = {}
for line in open(sys.argv[1]):
    line = line.strip()
    if '=' in line and not line.startswith('#'):
        k, v = line.split('=', 1)
        props[k.strip()] = v.strip().replace('\\:', ':').replace('\\\\', '\\')
out = sys.argv[2]
root = props.get('assets_root')
index = props.get('asset_index')
print('assets_root', root, 'asset_index', index)
idx = json.load(open(os.path.join(root, 'indexes', index + '.json')))
objs = idx['objects']
def obj(name):
    h = objs[name]['hash']
    return os.path.join(root, 'objects', h[:2], h)
for name in ['minecraft/sounds.json', 'minecraft/lang/de_de.json']:
    if name in objs:
        shutil.copy(obj(name), os.path.join(out, name.replace('/', '_')))
with open(os.path.join(out, 'vanilla-sound-files.txt'), 'w') as f:
    for name in sorted(objs):
        if name.startswith('minecraft/sounds/'):
            f.write(name + '\n')
print('objects', len(objs))
EOF
ls -la "$OUT"
tar czf devtools.tar.gz "$OUT"
ls -la devtools.tar.gz
