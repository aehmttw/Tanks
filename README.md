# Tanks: The Crusades

Tanks: The Crusades is a game written in Java inspired by Wii Play's Tanks.<br>
There are over 20 unique enemies you can fight, but you can add more of your own too.<br>
The game supports Windows, Mac and Linux.<br>
Tanks can be found on [Steam](https://store.steampowered.com/app/1660910/Tanks_The_Crusades/), [itch.io](https://aehmttw.itch.io/tanks), and the [iOS App Store](https://apps.apple.com/us/app/tanks-the-crusades/id1508772262)

## How to play

### Controls: <br>
Use the arrow keys or WASD to move <br>
Left click or click space to shoot<br>
Right click or press enter to lay a mine<br>
Press escape to pause the game<br>

### Level Editor controls:<br>
Left click to place<br>
Right click on an object to destroy it<br>
Right click on nothing to rotate the object you are placing<br>
Press up and down arrow keys or scroll to cycle enemy tanks<br>
Press left and right arrow keys to cycle enemy tank, obstacle, player tank<br>
Press space to access the object menu<br>
Press escape to change level settings<br>
Press enter to play your level<br>

The following are three screenshots of the game in action. The user controls the azure blue tank using the mouse or keyboard.

![Gameplay](screenshot1.PNG)
![Gameplay](screenshot2.PNG)
![Gameplay](screenshot3.PNG)

## Installation

To use the game you need to install Java. For this purpose you may need
administrator privileges to install software on your machine. You can install Java 
from [java.com](https://java.com/download). The minimum Java version is Java 8.<br>

You can download the [jar file](https://1drv.ms/u/s!AnwBrt306BrJ-ltbmsJuG2pZdfVk?e=fg7P0k) for Tanks.
To run the jar file you can double-click on it. 

## Development

To develop the game we suggest using Eclipse or IntelliJ. Eclipse can be installed from 
[eclipse.org](http://www.eclipse.org/downloads/). IntelliJ can be installed from 
[jetbrains.com](https://www.jetbrains.com/idea/download/). <br>

To access the source code you also need to install Git.<br>

You will need to add the following libraries to the build path of the project: <br>
PNGDecoder by Matthias Mann in TWL<br>
Netty<br>
All the jars and natives of the following LWJGL libraries:<br>
LWJGL (core)<br>
OpenGL<br>
OpenAL<br>
GLFW<br>
Assimp<br>
STB<br>
Commons IO<br>
Steamworks4j<br>

You can all download these libraries in zip format from [here](https://1drv.ms/f/s!AnwBrt306BrJgdAz1AzDfZuq79f8VQ?e=hemiis).

Please note - Tanks uses a custom version of Steamworks4j compiled with Mac OS arm64 natives. 
We recommend you use the libraries linked above, because they contain that modified Steamworks4j library. 

## Credits:

### Main Developer:
Matei Budiu (Aehmttw)<br>

### Code Contributors:
Karan Gurazada<br>
Hallowizer<br>
Grify<br>
Mihai Budiu<br>
Panadero1<br>
Arkar Tan<br>
Pythonmcpi<br>
Cool TM<br>
QazCetelic<br>
Lancelot<br>

### Supporters:
SapphireDrew<br>

### Special Thanks:
Everyone from the [Tanks Discord](https://discord.gg/aWPaJD3) who helped with ideas and bugs!
