// Production environment — URLs are injected at runtime by Railway
// via the ENV_CONFIG block in src/index.html (set by start.sh).
// Do NOT hardcode service URLs here — they change per Railway deployment.
declare global {
  interface Window {
    __env?: {
      userServiceUrl?: string;
      farmTwinServiceUrl?: string;
    };
  }
}

const w = typeof window !== 'undefined' ? window.__env ?? {} : {};

export const environment = {
  production: true,
  userServiceUrl: w.userServiceUrl ?? 'https://user-service.up.railway.app/api/v1',
  farmTwinServiceUrl: w.farmTwinServiceUrl ?? 'https://farm-twin-service.up.railway.app/api/v1',
};
