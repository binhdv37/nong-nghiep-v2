#!/bin/bash
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


# export DB environment variables
if [ "$POSTGRES_SSL" = "enabled" ]; then
  mkdir -p /etc/psql-ssl-keys/
  openssl pkcs8 -topk8 -inform PEM -outform DER -in ${PG_SSL_KEY_FILE} -out /etc/psql-ssl-keys/client-key.pk8 -nocrypt
  cp ${PG_SSL_CERT_FILE} /etc/psql-ssl-keys/client-cert.pem
  cp ${PG_SSL_ROOTCERT_FILE} /etc/psql-ssl-keys/server-ca.pem
  chmod -R a+r /etc/psql-ssl-keys/
  
  export SPRING_DATASOURCE_URL="jdbc:postgresql://${PG_HOST}:${PG_PORT}/${PG_DATABASE}?sslmode=${PG_SSL_MODE}&sslrootcert=/etc/psql-ssl-keys/server-ca.pem&sslcert=/etc/psql-ssl-keys/client-cert.pem&sslkey=/etc/psql-ssl-keys/client-key.pk8"
else
  export SPRING_DATASOURCE_URL="jdbc:postgresql://${PG_HOST}:${PG_PORT}/${PG_DATABASE}"
fi
export SPRING_DATASOURCE_USERNAME=${PG_USER}
export SPRING_DATASOURCE_PASSWORD=${PG_PASS}

# print db config
echo "================================= Being DB config ===================================="
echo "Database URL: $SPRING_DATASOURCE_URL"
echo "Database user: $SPRING_DATASOURCE_USERNAME"
echo "================================= End DB config   ===================================="


CONF_FOLDER="${pkg.installFolder}/conf"
jarfile=${pkg.installFolder}/bin/${pkg.name}.jar
configfile=${pkg.name}.conf
firstlaunch=${DATA_FOLDER}/.firstlaunch

source "${CONF_FOLDER}/${configfile}"


if [ ! -f ${firstlaunch} ]; then
    echo "================================= Being DB installer ===================================="
    install-tb.sh --loadDemo
    touch ${firstlaunch}
fi

echo "Starting ThingsBoard ..."

java -cp ${jarfile} $JAVA_OPTS -Dloader.main=org.thingsboard.server.ThingsboardServerApplication \
                    -Dspring.jpa.hibernate.ddl-auto=none \
                    -Dlogging.config=${CONF_FOLDER}/logback.xml \
                    org.springframework.boot.loader.PropertiesLauncher

