# demonworld
A implementation of the demonworld tabletop beginner game

Starting the game:

- from source code: execute class Main.java
- use builded game: extract Demonworld.zip, then double click on Demonworld.jar
- start builded game with logs: extract Demonworld.zip, use commandline "java -jar Demonworld.jar" to start game and see the log output (e.g if the game doesnt start)

Single Player:
At every turn phase, use the blue chip symbol in task bar to let the computer player do his actions
Use the red dice in the task bar to evaluate the results of attacks

Multiplayer:
- Start Game (it will create a local Gameserver automatically)
- find out your ip adress in local network (e.g using ipconfig or ifconfig in commandline)
- configure your router to forward port 8087 to your local ip - see https://www.wikihow.com/Set-Up-Port-Forwarding-on-a-Router
- Find out the ip of your router on the internet (e.g. use https://www.wieistmeineip.de/)
- give the router ip to the second player

- second player edit the file resources/config.properties
- edit the setting for username, set prameter host to ip of the server
- start game, you should automatically connected to the external gameserver 
- cursorposition of other player is shown as a red dot on the map.
- i suggest to use teamspeak or videocall during game to communicate about gameplay, this is a virtual gameboard, not a full multiplayer game
  