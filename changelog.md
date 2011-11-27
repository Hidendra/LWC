### 4.0.0-alpha7
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