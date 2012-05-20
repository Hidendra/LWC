### 4.2.0
# LWC 4.2.0
* **Significant** performance increases, in some cases upwards of 90%. In most cases, ProtectionInteract was >100k nanoseconds. It is now 20k-ish. The Redstone event, which accounted for 50% of the cpu time, at 20k nanoseconds/event, is now around 2k-3k nanoseconds per event.
* Vault Economy & Permissions support
* Added fence gates to the default configuration
* Less quiet exceptions when the updater fails for any reason
* CreeperHeal support - do not remove protections if creeper heal is activated & enabled :)
* Rewrote double door logic to be more reliable. It also does not reopen doors that are already closed.
* Work around a difficult to reproduce exploit that appears to be caused by a Bukkit bug, exact cause unknown (not on LWC's end, but it prevents it now)
* A block's protection history can be cycled through if you use the command `/lwc info history`

### 4.1.1
# LWC 4.1.1
* Minor bug fix for metrics

### 4.1.0
# LWC 4.1.0
* **R6 and Minecraft 1.2 support**
* **The following has been *COMPLETELY* REMOVED:**
 * `/lwc schedule`;
 * `/lwc menu`;
 * `/lwc dev`;
 * `/lwc admin config`;
 * List support;
 * EasyNotify;
 * Job support (requirement for schedules);
 * Kick traps;
 * Ban traps;
 * Deprecated `ProtectionTypes` class (use the Protection.Type enum instead!)
* Full LWC backups can be created using `/lwc admin backup create` -- this creates a full backup for the database and also every block along with it including any contents.
 * Backups can be restored using `/lwc admin backup restore NAME` where NAME is the backup's file name without the extension (`.lwc` or `.lwc.gz`). The backups are stored in `plugins/LWC/backups/`
 * At this time, the double chest side of double chests are not stored.
* Any /lwc admin protectregion NAME to protect all protectable LWC blocks in a WorldGuard region to 'LWCWorldGuard', the owner can be changed via /lwc admin updateprotections set owner = 'NewOwner' where owner ='LWCWorldGuard' (sorry for being so long spunk!)
* **Fix:** /cunlock could be used even after the correct password was used.

### LWC 4.0.9
* /lwc admin rebuild: a command to rebuild a wiped database, barring some missing data such as ACLs which is less important and unrecoverable without backups. See http://forums.bukkit.org/threads/60178/

### LWC 4.0.8
* Fix an exploit that allows some very dangerous commands to be executed on the server

### LWC 4.0.7
* Maintenance release to fix an index. Most servers will not notice any difference.

### LWC 4.0.6
* bPermissions group support
* Allow the group prefix for matching groups via Permissions be changed in core.yml (and also change the default to group. instead of lwc.group.)
* Don't allow freshly placed pressure plates to open a door that has the redstone flag
* `/lwc admin purge` would interfere with LWC-Econ discount type IN_USE and not allow people to get discounted.

### LWC 4.0.5
* Fix gravity-matched blocks such as wooden sign, rails, lever, etc
* Prevent players from using /lwc fix on protections they cannot administrate

### LWC 4.0.4
* Incompatible classes when a method's return type was changed, so 4.0.4 is a complete rebuild of 4.0.3, fixing some bugs
* No source file changes except for plugin.yml

### LWC 4.0.3
* **New:** When removing one side of a protected double chest, it won't REMOVE the protection now.
* **New:** Support custom protections via the data value ( 'id:data' , e.g '181:8' )
 * This means Industrial Craft, etc blocks that use the data value for the block are now supported individually.
* Found another area where queries were being wasted when clicking on certain blocks
* Fixes an exploit detailed by `spunkiie` which allows you to arbitrarily take over certain protections
* Fix `plugins/LWC/locale/lwc.properties` - it was not being loaded properly
* Fix removal of double step protections when a step is placed and protected
* Fix automatic upgrade from sqlite->mysql
* Only perform rights migration from LWC3 -> LWC4 once even if the old rights table fails to drop
* Located and restored `/lwc admin purgeregion` - it went AWOL but has came back.
* Fail with a more descriptive failure when LWC cannot connect to the database at startup
* Fix an NPE in limitsv2 that is normally only found when using /climits
* soft defend iConomy to fix a bug when using the serverBankAccount feature of LWC-Economy
* Also soft depend Permissions & PermissionsBukkit again
 * **NOTE:** If you use LWC-Economy and/or LWC-Spout, you may need to upgrade them as well.
* Group limits in limitsv2.yml are no longer case sensitive
* Fix the SQLite libraries being placed in the wrong location - should be `plugins/LWC/lib` not `plugins/LWC/lib/lib` :)
* Fix worldguard.allowProtectionsOutsideRegions: it was working in reverse (true meant it was blocked, false meant it was allowed)

### LWC 4.0.2
* Optimized out 1 to 2 unneeded queries that were used when touching a protected or unprotected door (for either they were two different things)
* Fix a bug where converted rights from LWC 3 were 'not able to access protections'
* Fix an exploit where a chest protection could be overridden by another protection in two very specific cases
* Minor additions to `/lwc credits` -- upgrade MonsterTKE and add imaxorz

### LWC 4.0.1
* Fix the german locale's file encoding (latin-1 -> utf-8)
* Fix a Spout bug (both in its RB and dev builds) that allow a sticky piston to destroy chests by pulling a wooden door over it
* Fix a bug in converting rights from LWC3 -> 4 format when the table is a bit tainted

### LWC 4.0.0
#### Licensing
* LWC 4 is now licensed under the **2-clause BSD license**

#### Translations
* **New translation! Hungarian, courtesy of dretax**
* Most languages have been fully updated to the latest changes.

#### Functionality
* The following commands have been added: (see the wiki for more in depth info)
 * `/lwc history`
 * `/lwc details`
 * `/lwc schedule`
 * `/lwc setup`
 * `/lwc fix`
 * New sub commands for `/lwc admin`
* Towny integration. You can now allow members of a specific Town access your Private protection, simply: `/cmodify t:TownName` or when creating it: `/cprivate t:TownName`
* The WorldGuard feature has been rewritten to be easier to use and now includes a blacklist feature, so you can blacklist specific regions from having protections.
* Add a new flag: `AUTOCLOSE` which makes a door automatically close after the configured amount of time in `plugins/LWC/doors.yml`. Usage: `/cautoclose on|off`
* Allow Fence Gates to be automatically closed by the autoclose flag & `doors.yml`
* Pressure plates will now be protected if they are placed in front of a door. Only players who have access to the protection can use the pressure plate.
 * If you use `/credstone on` on the door, no redstone except the attached pressure plate will work on the door, meaning now only those who have access to the protection can open it.
* A protection's type can now be changed via `/cmodify type` e.g: `/cmodify private` will make a chest private. You can change a password (or change it TO password) via: `/cmodify password ThePassword`
* Limits V2. New limits system that is a lot easier to manage and use -- see `plugins/LWC/limitsv2.yml` Example commands:
 * **`/climits` now fully functions as it should! Give the new updated version a shot!**
 * `/lwc setup limits Hidendra 0` -- give hidendra 0 default protections
 * `/lwc setup limits Hidendra default unlimited chest 5` -- give hidendra 5 chests and unlimited everything else
 * `/lwc setup limits g:default 5` -- give the default group 5 protections
 * `/lwc setup limits default 0` -- set the default amount of protections to 0
 * `/lwc setup limits hidendra 54 10 96 1` -- example of using block ids instead of names (10 chests, 1 trap door)
* add `quiet` under protections that can be set to a block that hides creation messages and notices

#### Cosmetics
* `/lwc admin report` has been beautified
* `/cinfo` has been given a new look and is now more helpful. It will give you a shortlist of players who can access a private protection if you have appropriate access to it.

#### Database
* Changes to the database format that make it not possible to downgrade from LWC4 to LWC3. However, LWC will still upgrade from 3 to 4 ok.
* All existing LWC 3 indexes have been wiped and LWC is now better indexed
* Startup time has been dramatically reduced (to nil) for those with a huge protection set
* More optimal caching techniques to ensure duplicate cache entries aren't present or created

#### Internal
* The "bug 656 workaround" has been replaced with an automatic feature that does not need to be enabled, but is used when required
* `/lwc admin reload` will now also reload the loaded locale file (including the one in `plugins/LWC/locale/`)
* New & better updater. You can now subscribe to updates to the STABLE branch or BLEEDING_EDGE, which is the latest Jenkins builds.
* Locale messages defined as `null` will now not send the message to the player
* **FIX:** Protected blocks could be pulled with a sticky piston
* **FIX:** Doors could be destroyed by using a piston to push a dirt block towards it.
* **FIX:** Limits will now use the highest group limit instead of the first one found.
* **FIX:** The magnet module would sometimes not work as expected when used across multiple worlds.
* Metrics API for measuring plugin usage across the world using non-identifiable data.
 * Developed by myself, all implementation specific details and backends are open source.
 * View the data online: http://metrics.griefcraft.com/plugin/LWC
 * Backend source: https://github.com/Hidendra/metrics.griefcraft.com
* `loadProtection(int id)` now utilizes the protection cache

### 4.0.0-alpha7
* Added `worldguard.allowProtectionsOutsideRegions` to the WorldGuard portion of LWC to decide if you want to allow protections outside of WorldGuard regions.
* Added `optional.onlyProtectWhenOwnerIsOffline` and `optional.onlyProtectWhenOwnerIsOnline` config options to core.yml. They allow protections to only be "active" (protecting items) when the owner of the protection is offline or online respectively.
* API: Added `removeProtectionsByPlayer` to PhysDB
* Added the commands: `/lwc admin updateprotections` `/lwc admin deleteprotections` `/lwc admin selectprotections` Delete/Select take a where argument and must be specified; e.g `/lwc admin deleteprotections world='World1'` and Update takes a Where and/or Set statement: `/lwc admin updateprotections set world='World2' where world='World1'`
* On the fly database conversion using `/lwc setup database NewType` and does not require plugin reloads or server restarts. E.g if you're on MySQL and want to go back to SQLite: `/lwc setup database sqlite`
* **FIX:** `/lwc admin report` would throw an exception
* **FIX:** Internal database version was not updating
* Startup time has been dramatically increased for those with a very large amount of protections.
* The bug 656 workaround has been replaced with a feature that is automatically used when chunk bug are detected.
* It is now **no longer possible (!!)** to easily downgrade to 3.5x without losing some data.

### 4.0.0-alpha6
* LWC is now licensed under the **2-clause BSD license**. All code prior to this point is still GPL, however.

### 4.0.0-alpha5
* Add `/lwc details <HistoryId>` which allows you to view specific information on a history item.
* Add `/lwc history` - used to view history items for a specific player or all latest ones. To be used hand in hand with `/lwc details` which views specific information on any given history item. The arguments for `/lwc history` are dynamic and as such, all of the following uses are valid: `/lwc history` `/lwc history *` `/lwc history 2` (page 2) `/lwc history 2 *` `/lwc history * 2` `/lwc history Hidendra` `/lwc history Hidendra`  ... and so on!
* The way chests were facing were broken when upgrading from a pre-1.8 map to 1.8. The first time you click a protected chest you can access, it will reorient itself. Alternatively, you can use `/lwc fix` after this to manually orient a chest.
* **FIX:** Limits will now use the highest group limit instead of the first one found.
* **FIX:** The magnet module would sometimes not work as expected when used across multiple worlds.
* Added `t:TownName` and `town:TownName` to `/cmodify` and `/cprivate` for Towny integration. Allows residents of the town named to access that protection.
* Locale messages defined as `null` will now not send the message to the player
* New `AUTOCLOSE` flag, which allows doors to automatically close themselves after 3 seconds. Both sides of a double door must have this flag or doors may become out of sync.
* New LWC versions or builds can now be reloaded in without having to restart the server

### 4.0.0-alpha4
* `/lwc admin reload` will now also reload the loaded locale file (including the one in `plugins/LWC/locale/`)
* Added the `/lwc admin dump locale` utility - it dumps the current locale file into `plugins/LWC/locale/lwc.properties`
* Flags now support data. No utility to set data yet, however.
* The command `/lwc admin purgeregion <RegionName> [WorldName]` has been added to the WorldGuard module. If you are using it from the console, you must also specify the world that the region is in. If the region is in a different world than the player you use the command from, you must again also provide the world name.
* New updater + downloader. Currently, the default method of updating is via BLEEDING_EDGE. This is a build-by-build update and is currently set to manual by default. You can check your version with `/lwc admin version` and/or update to the latest with `/lwc admin update`. If you want automatic updating, in core.yml set core.updateMethod to: `updateMethod: AUTOMATIC`

### 4.0.0-alpha3
* **FIX:** Protected blocks could be pulled with a sticky piston
* **FIX:** Piston exploit that allowed protections such as Doors to be destroyed via:  PISTON -> BLOCK -> PROTECTED DOOR
* Towny integration. Protections cannot be made outside of Towns, e.g the wild. Set `core.townyBorders` to true
* Double wooden doors will now function properly
* The openAndClose feature of double doors has been fixed
* Out of sync double doors can be fixed with the `/lwc fix` or `/lwc fixdoor` commands
* Fixes a duplication exploit related to the Magnet feature and the Showcase plugin
* The database is no longer used for LWC's in-memory database. It is now stored in-memory objects and even if the database connection is lost, LWC won't be totally unusable.
* Added the Expire job. It allows you to automatically expire protections - equivilent to `/lwc admin expire`. If you want to expire protections every week, that haven't been accessed in at least 2 weeks, do this: `/lwc schedule create test expire` `/lwc schedule arguments test 2 weeks` `/lwc schedule autoRun test 1 week`  Done! If you want the block + inventory contents of the block to also be removed, you can add -remove after arguments, e.g: `/lwc schedule arguments test -remove 2 weeks`
* Added the Cleanup job. Allows you to automatically run `/lwc admin cleanup`
* Job scheduler. It allows you to manually run specific jobs or automatically run them at a given time (they can be provided by outside sources, too.)
* `/lwc schedule create JOBNAME TYPE` - Creates a job, for example: `/lwc schedule create weekly cleanup`
* `/lwc schedule run JOBNAME` - Manually run a job, for example: `/lwc schedule run weekly`
* `/lwc schedule check JOBNAME` - Look up when the job will next run, if it's to be automatically ran. It will tell you how long until the job runs.
* `/lwc schedule autorun JOBNAME TIME` - Schedule a job to automatically run. For example, run the weekly job every week: `/lwc schedule autorun weekly 1 week` - you can combine times, e.g: `/lwc schedule autorun weekly 2 days 12 hours` = every 60 hours
* `/lwc schedule list` - List all of the known jobs. YELLOW represents a job that is not automatically scheduled and must be automatically ran. GREEN represents a job that is automatically scheduled but is not a candidate to be ran yet. RED represents a job that is waiting to be ran.
* `/lwc schedule arguments JOBNAME ARGUMENTS` - for example: `/lwc create JOBNAME expire` , `/lwc schedule arguments JOBNAME -remove 2 weeks` makes the job JOBNAME which removes protections + blocks of protections that have not be accessed in at least 2 weeks.
* `/lwc tasks` can be used instead of `/lwc schedule`

### 4.0.0-alpha2
* `/lwc admin report` has had a makeover and now also shows cache read/writes
* Protection rights have been inlined with the main protections table
* Multi-group support has been added for Permissions 3.0+
* Permissions support has been modified to always depend on Superperms, while LWC's own implementations will only be used for groups. This **breaks Permissions 2/3 support** unless you have SuperpermsBridge!