(function() {
  function initSwagger() {
    if (window.SwaggerUIBundle) {
      // Initialize Guardian API
      window.SwaggerUIBundle({
        url: '/docs/guardian.yaml',
        dom_id: '#swagger-guardian',
        deepLinking: true,
        presets: [
          window.SwaggerUIBundle.presets.apis,
          window.SwaggerUIBundle.presets.standalone
        ],
        layout: "StandaloneLayout"
      });

      // Initialize Integrations API
      window.SwaggerUIBundle({
        url: '/docs/integrations.yaml',
        dom_id: '#swagger-integrations',
        deepLinking: true,
        presets: [
          window.SwaggerUIBundle.presets.apis,
          window.SwaggerUIBundle.presets.standalone
        ],
        layout: "StandaloneLayout"
      });
    } else {
      // Retry if SwaggerUIBundle is not loaded yet
      setTimeout(initSwagger, 100);
    }
  }

  // Wait for DOM to be ready
  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', initSwagger);
  } else {
    // Check if SwaggerUIBundle is already loaded
    if (window.SwaggerUIBundle) {
      initSwagger();
    } else {
      // Wait for the script to load
      setTimeout(initSwagger, 500);
    }
  }
})();

