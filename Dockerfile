FROM solsson/kafka-jre@sha256:06dabfc8cacd0687c8f52c52afd650444fb6d4a8e0b85f68557e6e7a5c71667c

COPY . /opt/src/kafka-test-failover

RUN set -e; \
  export DEBIAN_FRONTEND=noninteractive; \
  runDeps=''; \
  buildDeps='curl ca-certificates unzip'; \
  apt-get update && apt-get install -y $runDeps $buildDeps --no-install-recommends; \
  \
  cd /opt; \
  GRADLE_VERSION=4.2.1 PATH=$PATH:$(pwd)/gradle-$GRADLE_VERSION/bin; \
  curl -SLs -o gradle-$GRADLE_VERSION-bin.zip https://services.gradle.org/distributions/gradle-$GRADLE_VERSION-bin.zip; \
  unzip gradle-$GRADLE_VERSION-bin.zip; \
  rm gradle-$GRADLE_VERSION-bin.zip; \
  gradle -v; \
  \
  echo "temp layer";
  #\
  #rm gradle-$GRADLE_VERSION -Rf; \
  #rm ~/.gradle -Rf; \
  #apk del .build-deps; \

RUN set -e; \
  cd /opt; \
  GRADLE_VERSION=4.2.1 PATH=$PATH:$(pwd)/gradle-$GRADLE_VERSION/bin; \
  \
  cd /opt/src/kafka-test-failover; \
  gradle jar