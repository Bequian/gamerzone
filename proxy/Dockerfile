# Proxy inverso Apache con HTTPS para GamerZone
FROM httpd:2.4

# Activa los módulos necesarios en la configuración por defecto de Apache:
# ssl (HTTPS), proxy (reenviar a Spring Boot), headers y rewrite (redirecciones)
RUN sed -i \
      -e 's/^#\(LoadModule ssl_module\)/\1/' \
      -e 's/^#\(LoadModule socache_shmcb_module\)/\1/' \
      -e 's/^#\(LoadModule proxy_module\)/\1/' \
      -e 's/^#\(LoadModule proxy_http_module\)/\1/' \
      -e 's/^#\(LoadModule headers_module\)/\1/' \
      -e 's/^#\(LoadModule rewrite_module\)/\1/' \
      conf/httpd.conf \
 && echo "Include conf/extra/gamerzone.conf" >> conf/httpd.conf

COPY gamerzone.conf conf/extra/gamerzone.conf

# El certificado NO va dentro de la imagen: se monta como volumen desde la VM (carpeta certs/)
EXPOSE 80 443