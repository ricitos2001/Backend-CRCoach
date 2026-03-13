Guía rápida para generar la documentación OpenAPI y publicarla en GitHub Pages

Resumen:
- Este proyecto usa springdoc (ya incluido en el `pom.xml`) para exponer la especificación OpenAPI en tiempo de ejecución.
- El archivo `docs/index.html` usa ReDoc y espera encontrar `docs/openapi.json` en la misma carpeta.

Opciones para publicar en GitHub Pages:
1) Publicar la carpeta `docs/` desde la rama principal (main/master).
2) Generar la especificación y desplegar una rama `gh-pages` con el contenido estático.

Pasos (método simple: generar openapi.json y subir a `docs/` en la rama principal):

1. Ejecutar la aplicación localmente y acceder al endpoint de openapi:

   ```bash
   # Construir y ejecutar (puedes usar tu IDE o maven):
   mvn spring-boot:run

   # En otra terminal, descargar la especificación OpenAPI (por defecto springdoc la sirve en /v3/api-docs):
   curl -sS http://localhost:8080/v3/api-docs -o docs/openapi.json
   ```

   Nota: si tu aplicación corre en otro puerto o con contexto, ajusta la URL.

2. Verifica que `docs/openapi.json` existe. Luego añade y commitea `docs/openapi.json` y `docs/index.html`:

   ```bash
   git add docs/openapi.json docs/index.html
   git commit -m "Add generated OpenAPI JSON and ReDoc viewer"
   git push
   ```

3. En GitHub, ve al repositorio -> Settings -> Pages y selecciona la rama `main` (o `master`) y la carpeta `/docs` como fuente. Guarda.

4. Tras unos segundos la página estará disponible en `https://<tu-usuario>.github.io/<tu-repo>/`.

Alternativa: publicar en la rama `gh-pages` (útil si no quieres commitear el JSON a main):

1. Genera `openapi.json` como antes.
2. Crea una carpeta temporal y copia `index.html` y `openapi.json`:

   ```bash
   mkdir /tmp/memoworks-docs
   cp docs/index.html docs/openapi.json /tmp/memoworks-docs/
   cd /tmp/memoworks-docs
   git init
   git remote add origin <url-del-repo>
   git checkout -b gh-pages
   git add .
   git commit -m "Publish API docs"
   git push --force origin gh-pages
   ```

3. En GitHub -> Settings -> Pages, selecciona la rama `gh-pages` como fuente.

Consejos:
- Si la API está protegida por seguridad, puedes habilitar temporalmente acceso anónimo al endpoint `/v3/api-docs` mientras generas el JSON, o arrancar la app con un profile que deshabilite seguridad.
- Para automatizarlo en CI, añade un job que arranque la app, haga curl al endpoint y publique a `gh-pages`.

