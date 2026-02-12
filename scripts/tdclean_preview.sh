#!/usr/bin/env bash
# Thin wrapper: POST /api/v1/admin/test-data-cleaner/preview
# Env: ADMIN_BEARER_TOKEN (required), TDCLEAN_BASE_URL (default http://localhost:8080)
# Args: --moduleIds <comma-separated>
# Exit: 0=success, 1=failed, 3=safety/400, 4=401/403. Always prints raw JSON to stdout.

set -e
BASE="${TDCLEAN_BASE_URL:-http://localhost:8080}"
if [ -z "${ADMIN_BEARER_TOKEN}" ]; then
  echo '{"error":"ADMIN_BEARER_TOKEN is required"}' >&2
  exit 1
fi

MODULE_IDS=""
while [ $# -gt 0 ]; do
  case "$1" in
    --moduleIds) MODULE_IDS="$2"; shift 2 ;;
    *) shift ;;
  esac
done
if [ -z "$MODULE_IDS" ]; then
  echo '{"error":"--moduleIds is required"}' >&2
  exit 1
fi

MODULES_JSON=$(node -e "console.log(JSON.stringify(process.argv[1].split(',').map(function(s){return s.trim();}).filter(Boolean)))" "$MODULE_IDS")
BODY=$(node -e "console.log(JSON.stringify({moduleIds:JSON.parse(process.argv[1]),includeAttachments:false}))" "$MODULES_JSON")

TMP=$(mktemp)
trap 'rm -f "$TMP"' EXIT
HTTP_CODE=$(curl -s -w "%{http_code}" -o "$TMP" -X POST "${BASE}/api/v1/admin/test-data-cleaner/preview" \
  -H "Authorization: Bearer ${ADMIN_BEARER_TOKEN}" \
  -H "Content-Type: application/json" \
  -d "$BODY")

RESP_BODY=$(cat "$TMP")
echo "$RESP_BODY"

if [ "$HTTP_CODE" = "401" ] || [ "$HTTP_CODE" = "403" ]; then
  exit 4
fi
if [ "$HTTP_CODE" = "400" ]; then
  MACHINE_CODE=$(echo "$RESP_BODY" | node -e "try{var d=JSON.parse(require('fs').readFileSync(0,'utf8')); process.stdout.write(d.machineCode||'');}catch(e){process.stdout.write('');}")
  if [ "$MACHINE_CODE" = "RESET_CONFIRM_REQUIRED" ] || [ "$MACHINE_CODE" = "ATTACHMENTS_NOT_SUPPORTED_YET" ]; then
    exit 3
  fi
  exit 1
fi
if [ "$HTTP_CODE" = "200" ]; then
  exit 0
fi
exit 1
