**LWC-Economy 1.20**

* LWC-iConomy.jar has now become LWC-Economy.jar -- please update accordingly.
* BOSEconomy is now supported alongside iConomy. No extra setup is necessary for either!
* Money paid for protections can now be refunded when you destroy the protection, even if the person who paid (the owner) is offline.
* Discounts now give out the exact amount of protections for the discounted price. For example, if 'newCharge' is set to 0 and 'amount' is set to 5, a player will be able to create 5 protections for free after it takes effect.

**LWC 3.40**

* Almost all current LWC Module events have been deprecated. They have been replaced with events such as: `onProtectionInteract(LWCProtectionInteractEvent event)`
* Fixed PermissionsBukkit compatibility: some commands did not work correctly
* `/lwc admin cleanup` has been drastically sped up
* Servers without any Permissions plugin would find anyone would have LWC Admin access
* The mode "nospam" has been added to prevent protection creation messages from popping up (e.g "created protection successfully.") Use /cnospam or /lwc mode nospam, which toggles it on and off.
* Protection history. LWC now logs all protection creations/destructions -- in the future, this may log chest access, but if it happens it will be a separate plugin such as LWC-Economy
* -remove flags have been added to `/lwc admin expire` and `/lwc admin purge <player>` to remove the associated protection block. For example, if you use `/lwc admin purge -remove Hidendra`, it will remove all protections by Hidendra, and all of the protected blocks in the world, along with any chest contents.
* If LWC loses connection to MySQL, LWC will prevent access to chests until the connection is regained. This is to prevent stealing from chests if the connection is somehow lost and cannot be immediately regained.
* Pistons can no longer destroy protected doors and so on.
* Multitude of bug fixes and minor corrections
* Minor optimizations