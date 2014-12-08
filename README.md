Ludum Dare 31 :: Unity Ascension
====================

A game made in 48 hours for the 31st [Ludum Dare](http://ludumdare.com/) 48 hour game competition using [Java 7](http://www.oracle.com/technetwork/java/javase/downloads/jdk7-downloads-1880260.html) and the [Lightweight Java Game Library 3](http://www.lwjgl.org). The theme was "Entire Game on One Screen" and my idea was to make a game that connected to my server so everyone that played would be playing on the same screen, and the entire game (including all instances) in some way only has one screen.
As for the gameplay I made a platform shooter (that easily fits on one screen), where players join teams and compete to try to earn points for their color. There was a planned ending where players from multiple teams would have to cooperate to defeat darkness and purify themselves. By casting away their inner darkness they would become more powerful and instead of a standard competition style scoring they would all share their points and get more point the more people they help to ascend. In the end I ran out of time, and only some core aspects got finished.
Game page: http://ludumdare.com/compo/ludum-dare-31/?action=preview&uid=21983

This will have a Master branch for the original game containing the 48 hour version plus any extra commits that fix portability or security bugs, and a second Post Competition branch that has the features I had hoped to add but ran out of time to add during the competition.

The network code was my first attempt at an online multiplayer game of this type, and probably took around 90% of the initial time I spent on this game. It is very poorly written so I apologize to anyone that would like to look through it. In all honesty by the end I was making enough last minute changes that I’m not entirely sure how it works, and would greatly appreciate if you’d point out any glaring mistakes or security concerns! Either submit a bug report here or tweet at me ([@HitchH1k3r](https://twitter.com/hitchh1k3r)).

Post Competition Goals
-----------

* Game improvements
  - [ ] Add jumping when you press W or the Up Arrow (not just spacebar)
  - [ ] Change the pickup sound when you get a health crystal (the sound was originally added but I forgot the event)
  - [ ] Improves splash screen to show a sample health bar, explain that you use health as ammo, and how to jump (I forgot jump instructions in the game… whoops)
  - [ ] Make the players heal slowly over time
  - [ ] Make players lose points when they die
* Improve network and server code
  - [ ] Add system to send me a text message and email on all server errors
  - [ ] Investigate and fix a bug I noticed where sometimes the server goes into a semi-working state until rebooted
  - [ ] Automatically scale back packet send interval if the buffer is backing up
  - [ ] Use some sort of compression (even delta compression) to decrease packet sizes
* Add ending to the game
  - [ ] Change the 5 score system to a different set of 7 scores
  - [ ] Improve the score display to show the players how to get different points a bit better (maybe highlight points when you get them)
  - [ ] Add an unlocked state to each of the 7 scores
  - [ ] Add the ascension boss battle, where two players on different teams can cooperate to get on the ascended team
  - [ ] ??? Possibly add a fallen state, where a player can have an opposite ending to the planned one, becoming one with darkness (still fits with the idea of unity ^_^)

License
-----------

I'm uncomfortable picking out a license for this right now, so this source code is presented for educational and security purposes. In the future I may put this under a proper open source license.

Build and Run
-----------

To build and run this you’ll need a java compiler (I used the eclipse IDE and JDK7), and LWJGL 3 (located here: http://www.lwjgl.org/download). There are two entry points **com.hitchh1k3rsguide.ld31.Main** for the game client and **com.hitchh1k3rsguide.utils.network.Server** for the game server. You’ll also have to edit the **HOST** and **PORT** variables in **Server.java** to match in both the client and server builds. This will be the port the server and client will use, and the host the client will attempt to connect to.