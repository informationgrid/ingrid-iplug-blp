FROM alpine AS builder

ADD ingrid-iplug-*-installer.jar /
RUN unzip -q ingrid-iplug-*-installer.jar -d /tmp \
    && export INSTALL_DIR=`ls -d1 /tmp/ingrid-iplug-blp-*` \
    && sed -i -r 's/(<AppenderRef.*\/>)/\1<AppenderRef ref="Console" \/>/g' $INSTALL_DIR/conf/log4j2.xml \
    && echo jetty.port=8080 > $INSTALL_DIR/conf/config.override.properties


FROM bellsoft/liberica-openjre-alpine:25
ENV INGRID_USER=ingrid
ENV RUN_DIRECTLY=true

# user, group ingrid (1000:1000) is created in base image
COPY --chown=1000:1000 --from=builder /tmp/ingrid-iplug-blp-* /opt/ingrid/ingrid-iplug-blp/

RUN addgroup --gid 1000 ingrid \
  && adduser --gecos "" --disabled-password --uid 1000 --ingroup "ingrid" ingrid \
  && chown -R ingrid:ingrid /opt/ingrid \
  && chmod -R ug+rwX /opt/ingrid

USER ingrid

WORKDIR /opt/ingrid/ingrid-iplug-blp
EXPOSE 8080/tcp

CMD /bin/sh start.sh start
