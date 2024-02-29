# syntax=docker/dockerfile-upstream:1.6.0-labs
FROM eclipse-temurin:11-jdk-jammy AS builder

RUN apt-get -qq -y update && \
    apt-get -qq -y install maven

WORKDIR /tmp

ADD https://github.com/69pmb/my-framework.git my-framework
ADD https://github.com/69pmb/AllMusic.git#feat/docker AllMusic

RUN cd my-framework && \
    mvn clean install -DskipTests -Dgcf.skip=true -q && \
    cd ../AllMusic && \
    mvn clean package -DskipTests -Dgcf.skip=true -q

FROM eclipse-temurin:11-jre-jammy

LABEL MAINTAINER 69pmb <pmbroca@gmail.com>

RUN apt-get -qq -y update && \
    apt-get -qq -y install libxext6 libxrender1 libxtst6 libxi6 && \
    rm -rf /var/lib/apt/lists/*

WORKDIR /opt/app

COPY --from=builder /tmp/AllMusic/shade/*.jar /opt/app/app.jar

CMD ["java", "-jar", "/opt/app/app.jar"]
