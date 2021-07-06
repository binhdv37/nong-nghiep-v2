#
# Copyright © 2016-2021 The Thingsboard Authors
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

FROM thingsboard/openjdk8

RUN apt-get update

COPY logback.xml ${pkg.name}.conf  start-tb-no-postgres.sh upgrade-tb.sh install-tb.sh ${pkg.name}.deb /tmp/

RUN chmod a+x /tmp/*.sh \
    && mv /tmp/start-tb-no-postgres.sh /usr/bin \
    && mv /tmp/upgrade-tb.sh /usr/bin \
    && mv /tmp/install-tb.sh /usr/bin

RUN dpkg -i /tmp/${pkg.name}.deb
RUN rm /tmp/${pkg.name}.deb

RUN systemctl --no-reload disable --now ${pkg.name}.service > /dev/null 2>&1 || :

RUN mv /tmp/logback.xml ${pkg.installFolder}/conf \
    && mv /tmp/${pkg.name}.conf ${pkg.installFolder}/conf


ENV HTTP_BIND_PORT=9090
ENV DATABASE_TS_TYPE=sql


ENV PG_HOST=postgresql \
    PG_PORT=5432 \ 
    PG_USER=postgres \
    PG_PASS=postgres \
    PG_DATABASE=thingsboard \
    POSTGRES_SSL=disabled

ENV ENABLE_UPGRADE=false \
    LOAD_DEMO=true \
    LOW_RAM_USAGE=false \
    DATA_FOLDER=/data \
    HTTP_BIND_PORT=9090 \
    DATABASE_TS_TYPE=sql \
    DATABASE_ENTITIES_TYPE=sql\
    SPRING_JPA_DATABASE_PLATFORM=org.hibernate.dialect.PostgreSQLDialect \
    SPRING_DRIVER_CLASS_NAME=org.postgresql.Driver 
    
ENV DATA_FOLDER=/data
RUN mkdir -p /data
RUN chown -R ${pkg.user}:${pkg.user} /data


RUN chown -R ${pkg.user}:${pkg.user} /var/log/${pkg.name}
RUN chmod 555 ${pkg.installFolder}/bin/${pkg.name}.jar

USER ${pkg.user}

EXPOSE 9090
EXPOSE 1883
EXPOSE 5683/udp
EXPOSE 5685/udp


CMD ["start-tb-no-postgres.sh"]
