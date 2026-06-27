#!/bin/sh
# Injects Railway runtime environment variables into index.html
# before Nginx serves the Angular app.
# Railway sets VITE_* or custom env vars at container start — we forward them here.

INDEX="/usr/share/nginx/html/index.html"

USER_SVC_URL="${USER_SERVICE_URL:-https://user-service.up.railway.app/api/v1}"
FARM_SVC_URL="${FARM_TWIN_SERVICE_URL:-https://farm-twin-service.up.railway.app/api/v1}"

# Inject __env block right after <head> tag
sed -i "s|<head>|<head><script>window.__env={userServiceUrl:'${USER_SVC_URL}',farmTwinServiceUrl:'${FARM_SVC_URL}'};</script>|" "$INDEX"

echo "Agri-Twin frontend starting with:"
echo "  userServiceUrl      = $USER_SVC_URL"
echo "  farmTwinServiceUrl  = $FARM_SVC_URL"

exec nginx -g 'daemon off;'
