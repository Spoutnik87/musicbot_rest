#!/bin/bash

if [ -f "/root/musicbotRest/RUNNING_PID" ]
then
	kill "$(cat /root/musicbotRest/RUNNING_PID)"
	echo "Server is stopped."
	rm /root/musicbotRest/RUNNING_PID
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
  echo $PID > /root/musicbotRest/RUNNING_PID
  exit 0
else
  echo "Server is not running."
  wait $PID
  exit $?
fi