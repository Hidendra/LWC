#LWC: Lightweight Inventory Protection and Management

LWC has a simple ideal: Allow the protection of our most important blocks individually. This includes chests, furnaces, dispensers, and even doors and signs (and just lately, trap doors.)

LWC does not use flat-file storage. By default, LWC uses SQLite, a stateless, serverless RDBMS (a fancy term for a database server.) It also has the ability to use MySQL!

What you can protect
----------------------
* Chests
* Dispensers
* Furnaces
* Doors
* Signs
* Trap doors
* Any other block you can imagine! Custom protections are totally possibly

Features
----------------------

* Plug 'n play! LWC handles all dependency resolution -- you just put LWC.jar in your plugins directory
* Has reached a level where its routinely safe and stable.
* Protections can be: Public, Password, and Private (by User and/or Groups)
* Limits of how many protections a user or group can lock
* Small memory footprint. No flat files.
* For chests, only 1 side of the chest needs to be protected. The plugin saves space by dynamically linking connected chests as protected
* Much, much more. This post would be extremely long if I were to post every feature LWC has to offer.

Converting
----------------------

Are you using ChestProtect or Chastity chests? LWC can seamlessly convert every chest from ChestProtect and all private chests from Chastity (only user permissions)
<b>In-game</b>: `/lwc admin convert <chestprotect|chastity>`

Commands
----------------------
You can view the latest commands by typing `/lwc` ingame, and then typing a command to view more information on it