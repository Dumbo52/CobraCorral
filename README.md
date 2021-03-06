CobraCorral
===========

A Horse locking/protection plugin for the Bukkit Minecraft server.

Configuration:
* **max-horses** - Specify the maximum amount of horses any player can lock. *Default: 2*
* **immortality** - If true, locked horses will be invulnerable to all damage when not being ridden. *Default: true*
* **auto-lock** - If true, horses will automatically lock when tamed if a lock spot is available. *Default true*
* **immortal-cooldown** - If true, horses will become invulnerable only after a delay. *Default: false*
* **cooldown-time** - The time, in seconds, that a horse will be vulnerable after the rider gets off. *Default: 5*
* **protect-chests** - If false, players cannot lock Mules/Donkeys with chests or place chests on locked Mules/Donkeys. *Default: true*
* **stop-pvp** - If true, players can not damage a locked horse if someone is riding it. *Default: false*
* **eject-on-logoff** - If true, any non-owner player on a locked horse (test-driving or on ACL) will be ejected on logoff. *Default: true*
* **backend** - Sets the database used to store locked horse information. *Default: sqlite, see settings below.*
  * **type:** sqlite
  * **file:** ccorral.db
  * **or**
  * **type:** mysql
  * **hostname:** localhost
  * **port:** 3306
  * **database:** ccorral
  * **username:** root
  * **password:** password
