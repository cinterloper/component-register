FROM cinterloper/lash
ARG PROJVER
ENV PROJVER ${PROJVER}
RUN apt-get update; apt -y upgrade; apt-get -y install wget unzip python python-pip git jq mbuffer nano ntp pigz pv python screen supervisor httpie
ENV CORNERSTONE_HOST='172.17.0.1'
ENV KVDN_START_TIME="5"
ENV MY_START_TIME="1"
ENV FAILURE_HOOK="echo FAILURE"
ADD Containers/default-startup.sh /startup.sh
ADD Containers/init.sh /init.sh
ENV STARTUP_HOOKS="/startup.sh"
RUN cd /opt/; wget https://github.com/cinterloper/kvdn/releases/download/3.3.2-1.0.9/clients-3.3.2-1.0.9.zip
RUN cd /opt; unzip clients-3.3.2-1.0.9.zip
RUN pip install pyInstaller httplib2
RUN cd /opt/clients-3.3.2-1.0.9/python; pyinstaller --onefile kvdn-cli.py
RUN cp /opt/clients-3.3.2-1.0.9/python/dist/kvdn-cli /usr/bin/
ADD Containers/_main_chain_dialback.sh /
ENV MAIN_LOOP=/_main_chain_dialback.sh
ADD build/libs/cornerstone-$PROJVER-fat.jar /opt/cornerstone.jar

CMD /init.sh
