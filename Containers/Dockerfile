FROM cinterloper/lash
ARG http_proxy
ENV http_proxy ${http_proxy}
RUN apt-get update; apt -y upgrade; apt-get -y install wget unzip python python-pip git jq mbuffer nano ntp pigz pv python screen supervisor httpie
ENV CORNERSTONE_HOST='172.17.0.1'
ENV KVDN_START_TIME="5"
ENV MY_START_TIME="1"
ENV FAILURE_HOOK="echo FAILURE"
ADD default-startup.sh /startup.sh
ADD init.sh /init.sh
ENV STARTUP_HOOKS="/startup.sh"
CMD /init.sh
