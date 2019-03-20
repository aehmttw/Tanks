# Tanks

Tanks is a game written in Java inspired by a game of the same name on the Wii.
There are 13 unique enemies you can fight, but you can add more of your own too.
The game works in Windows, Mac or Linux.

## How to play

Controls: 
Use the arrow keys or WASD to move 
Left click or click space to shoot
Right click or press enter to lay a mine
Press escape to pause the game

Level Editor controls:
Left click to place
Right click on an object to destroy it
Right click on nothing to rotate the object you are placing
Press up and down arrow keys or scroll to cycle enemy tanks
Press left and right arrow keys to cycle enemy tank, obstacle, player tank

The following are three screenshots of the game in action.  The colored squares are
tanks belonging to different teams, firing at each other.  The brown squares are
obstacles.  The user controls the cyan tank using the mouse or keyboard.

![Gameplay](screenshot1.PNG)
![Gameplay](screenshot2.PNG)
![Gameplay](screenshot3.PNG)

## Installation

To use the game you need to install Java.  For this purpose you may need
administrator privileges to install software on your machine.  You can install java 
from [java.com](https://java.com/download).

You can download the [jar file](https://1drv.ms/u/s!AnwBrt306BrJ6TlP255XMw8D8VGY) for tanks.
This comes as a zip, extract the zip file and you will have the jar file and a run command for mac.  
To run the jar file on Windows you can double-click on it. For Mac, you need to run the jar file with the
argument -XstartOnFirstThread, or by using the file whose name starts with RunTanks.

## Development

To develop the game we suggest using Eclipse or IntelliJ.  Eclipse can be installed from 
[eclipse.org](http://www.eclipse.org/downloads/).  IntelliJ can be installed from 
[jetbrains.com](https://www.jetbrains.com/idea/download/)

To access the source code you also need to install git.

You will need to add the following libraries to the build path of the project: 
PNGDecoder by Matthias Mann in TWL
All the jars and natives of the following LWJGL libraries:
LWJGL (core)
OpenGL
OpenAL
GLFW
Assimp
STB

## Credits:

Matei Budiu (Aehmttw)<br>
Karan Gurazada<br>
Hallowizer
