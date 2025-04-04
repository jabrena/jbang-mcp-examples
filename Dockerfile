FROM eclipse-temurin:23-jdk

LABEL "org.opencontainers.image.title"="jbang"
LABEL "org.opencontainers.image.description"="Unleash the power of Java"
LABEL "org.opencontainers.image.url"="https://jbang.dev"
LABEL "org.opencontainers.image.licenses"="MIT"
LABEL "org.opencontainers.image.version"="0.124.0"
LABEL "org.opencontainers.image.revision"="44067df3442654cb0d612fa76b9032f06ca7a799"

COPY assembly/* /
COPY src/main/java/MCPStopWatch.java /scripts/
COPY src/main/java/application.properties /scripts/

## mkdir of .userPrefs is to fix https://github.com/jbangdev/jbang/issues/1831
RUN jar xf jbang-0.124.0.zip && \
    rm jbang-0.124.0.zip && \
    mv jbang-* jbang && \
    chmod +x jbang/bin/jbang && \
    mkdir -p $HOME/.java/.userPrefs

VOLUME /scripts

ENV PATH="${PATH}:/jbang/bin"
ENV SCRIPTS_HOME=/scripts
ENV JBANG_VERSION=0.124.0
ENV JBANG_PATH=/jbang/bin
ENV JBANG_DIR="/jbang/.jbang"

COPY entrypoint /bin/entrypoint
RUN chmod +x /bin/entrypoint

CMD ["/bin/entrypoint", "/scripts/MCPStopWatch.java"]