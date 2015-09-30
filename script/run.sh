#!/bin/bash

javac ./Client.java
javac ./Server.java

echo "Hello, "$USER". This is Reservation System."
echo "If you are a server, please enter [0] and press [ENTER]: "
echo "If you are a client, please enter [1] and press [ENTER]: "

read choice

if [ $choice -eq 0 ]
then
    echo "If you want to run server #number, please enter [#number](0 - 2] and press [ENTER]: "
    read number
    echo "Got it! We start server "$number""
    java Server server $number
    
elif [ $choice -eq 1 ]
then
    echo "Load all servers' infomation from the table"
    echo "Hello customer! What to do you want?"
    java Client
else
    echo "Please follow the previous instruction"
fi