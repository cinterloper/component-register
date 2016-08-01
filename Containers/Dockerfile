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
RUN cd /opt/; wget https://github.com/cinterloper/kvdn/releases/download/3.3.2-1.0.6/clients-3.3.2-1.0.6.zip
RUN cd /opt; unzip clients-3.3.2-1.0.6.zip
RUN pip install pyInstaller
RUN cd /opt/clients-3.3.2-1.0.6/python; pyinstaller kvdn-cli.py
RUN echo 'export PATH=$PATH:/opt/clients-3.3.2-1.0.6/python/dist/kvdn-cli/' >> /etc/bash.bashrc
ADD _main_chain_dialback.sh /
ENV MAIN_LOOP=/_main_chain_dialback.sh
CMD /init.sh
