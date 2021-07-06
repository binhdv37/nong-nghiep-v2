#!/bin/sh
SERVICE_NAME=ThingsboardInstallApplication
CLASS=org.thingsboard.server.ThingsboardInstallApplication
PATH_TO_JAR=/opt/dam_tom/thingsboard-3.2.1.2-boot.jar
PID_PATH_NAME=/opt/ThingsboardInstallApplication-pid
PATH_TO_LOGBACK=/opt/dam_tom/logback.xml
DATA_DIR=/opt/dam_tom/data

checkstatus() {
  if ps ax | grep -v grep | grep $SERVICE_NAME >/dev/null; then
    echo "$SERVICE_NAME service running, everything is fine"
    return 0
  else
    echo "$SERVICE_NAME is not running"
    echo "$SERVICE_NAME is not running!" | mail -s "$SERVICE_NAME down" root
    return 3
  fi

}

startService() {
  echo "Starting $SERVICE_NAME ..."
  if [ ! -f $PID_PATH_NAME ]; then
    nohup java -cp $PATH_TO_JAR -Dloader.main=$CLASS \
    -Dlogging.config=$PATH_TO_LOGBACK \
    -Dinstall.data_dir=$DATA_DIR \
    org.springframework.boot.loader.PropertiesLauncher /tmp 2>>/dev/null >>/dev/null &
    echo $! >$PID_PATH_NAME
    echo "$SERVICE_NAME started ..."
    return 0
  else
    echo "$SERVICE_NAME is already running ..."
    return 0
  fi
}

stopService() {
  if [ -f $PID_PATH_NAME ]; then
    PID=$(cat $PID_PATH_NAME)
    echo "$SERVICE_NAME stoping ..."
    kill -9 $PID
    echo "$SERVICE_NAME stopped ..."
    rm $PID_PATH_NAME
    return 0
  else
    echo "$SERVICE_NAME is not running ..."
    return 0
  fi
}

case $1 in
start)
  startService
  ;;
stop)
  stopService

  ;;
restart)
  stopService
  startService

  ;;
status)
  checkstatus
  ;;
esac
