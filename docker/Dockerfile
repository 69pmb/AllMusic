FROM maven:3.5.4-jdk-8-slim
MAINTAINER pmb
RUN apt-get update
RUN apt-get install -y git
RUN git clone https://github.com/69pmb/AllMusic.git
RUN cd AllMusic && mvn clean install
WORKDIR AllMusic
RUN export DISPLAY=:0
RUN echo "" > /etc/java-8-openjdk/accessibility.properties
#RUN mvn exec:java -Djava.awt.headless=false
RUN mvn exec:java
ENTRYPOINT ["sh", "-c", "bash"]
#ENTRYPOINT ["mvn", "exec:java"]
