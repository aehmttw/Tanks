# Tanks Multiplayer Modding API

This is a multiplayer modding API for Tanks. I'll be calling this project Mod API for short.

I've often wanted to use more than the default obstacles and tanks to make my level ideas come true.
I wanted to make modding easier, while also having support in the base game.
Thus, I created this modding API to allow others to create their own custom levels and games more easily.

The Mod API will eventually be added into the base game, too, so people can play custom levels without needing to download the Mod API client!

How it works
---
Many custom modding objects will be added per each version. However, these are only for use when modding, and will not be accessible otherwise.
Once a custom game has been made, if it only uses features from the Mod API, it will be fully functional, even if a server hosts and plays the game with the clients.
This is done by sending the objects through the network.

How to use
---

To play with others as a client, only the JAR file is needed.
To create a custom game or level, the source code is needed.

Installation
---

You can download the JAR file [here](https://onedrive.live.com/download?cid=1E1C6A69D73A57B9&resid=1E1C6A69D73A57B9%21123&authkey=ANYqlUYkDfJA3tA).

To create a project with the source code, just set it up like a normal Tanks modding project or extension-making project.

New Features:
---

**As of Mod API v1.1.0:**
- Now using Tanks v1.3.1!
- Added the `ModGame` class.
  - Not limited to one level.
  - Can change the condition in which the level ends.
  - Can disable shooting and laying mines.
- Added the `CustomMovable` class, which sends draw instructions through the network.
- The NPC tank can now forcibly display its message via a function call, and change its messages.
- Added kill messages in `ModGame`s and `ModLevel`s, only visible in multiplayer.
- Added the `CustomShape` class, which draws shapes on the screen
- Added the `TransitionEffect` class, which makes fading effects on the screen
- Added the `Transparent Tall Tiles` option to make obstacles that are above ground semi-transparent
- Added the `Colors` class, which contains commonly used colors in Tanks.
- Teams are now split evenly (can be disabled in a level's Team option)
- Options are now accessible within parties, including name and tank color changing
- Added custom zoom and auto zoom, along with messages that display when you change it.
- Added the _Events per Second_ setting to Party Host Options
- Added the ability to pause the game if the window loses focus
- Improved Tank AI
- Improved water

**As of Mod API v1.0.1:**
- Added light mode to minimap (toggle theme with the L key)
- Added panning to minimap. Default controls are the numpad 8, 4, 6, and 2.
- Added a Tanks mode to the minimap, toggle with the P key, which does not show tiles such as mud and ice.
- Added a clouds and sky for the game.
- NPC tanks now support a customizable shop, name tags, and animated text.
- Improved the paste function in the level editor
- Fixed actionbar text
- Added a lot more scoreboard objective types
- Changed the max height of obstacles to 8
- Fixed a bug where Medic Tanks do not heal you after you put on shields

**As of Mod API v1.0.0:**
- Extensions now support custom levels!
- A minimap with changeable zoom (use the - or = key or their equivalent keys on the numpad)
- Text boxes to search up the names of levels and crusades
- Changed the max height of obstacles to 6.0
- Changed the custom level a bit
- Published first person mode and immersive camera (go to Options - Graphics Options - view)
- or use F5 key to toggle perspective

**As of Mod API v0.2.0:**
- A scoreboard
- Title, subtitle, and actionbar text
- A new custom level instead of the tutorial
- Copy, cut, and paste in the level editor
- A confirm prompt of saving when you try to exit the game while in a level editor
- Other minor bug fixes and tweaks

**As of Mod API v0.1.0:**
- An NPC Tank (drive close to it and press e to talk with it)
- The ability to add a text obstacle
- Sand and Water obstacles because they seem to be popular modding ideas
- A guide to this Modding API that may instead be changed to be online
- A few minor tweaks to the code to allow them to support custom levels

---
Screenshots
---

The custom level included in the code
![Gameplay](screenshot1.PNG)

Copy, cut, and paste functions in the level editor
![Gameplay](screenshot2.PNG)
