FROM docker-registry.wemove.com/ingrid-java:jdk-8
ENV INGRID_USER=root
ENV RUN_DIRECTLY=true

ADD ingrid-iplug-*-installer.jar /

RUN unzip ingrid-iplug-*-installer.jar -d /tmp \
    && mkdir -p /opt/ingrid/ingrid-iplug-blp \
    && cp -R /tmp/ingrid-iplug-blp-*/* /opt/ingrid/ingrid-iplug-blp/ \
    && rm -Rf /tmp/* \
    && sed -i -r 's/(<AppenderRef.*\/>)/\1<AppenderRef ref="Console" \/>/g' /opt/ingrid/ingrid-iplug-blp/conf/log4j2.xml \
    && rm ingrid-*.jar

RUN echo jetty.port=80 > /opt/ingrid/ingrid-iplug-blp/conf/config.override.properties

COPY ./files /

WORKDIR /opt/ingrid/ingrid-iplug-blp
EXPOSE 80

CMD /bin/sh start.sh start
