#!/bin/bash

if [ -f "/root/jenkins/musicbotRest/RUNNING_PID" ]
then
	kill "$(cat /root/jenkins/musicbotRest/RUNNING_PID)"
	echo "Server is stopped."
	rm /root/jenkins/musicbotRest/RUNNING_PID
	sleep 5
else
	echo "Server is not running. New server is ready to start."
fi

nohup java -jar target/musicbot_rest-*.jar > logs.txt 2>&1 &

PID=$!

sleep 5

if ps -p $PID > /dev/null
then
  echo "Server is running."
  echo $PID > /root/jenkins/musicbotRest/RUNNING_PID
  exit 0
else
  echo "Server is not running."
  wait $PID
  exit $?
fi