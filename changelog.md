### 4.00-alpha3
* The database is no longer used for LWC's in-memory database. It is now stored in-memory objects and even if the database connection is lost, LWC won't be totally unusable.
* Job scheduler. At the moment, it only supports Cleanup, but will support more in the future. It allows you to manually run specific jobs or automatically run them at a given time (they can be provided by outside sources, too.)
* `/lwc schedule create JOBNAME TYPE` - Creates a job, for example: `/lwc schedule create weekly cleanup`
* `/lwc schedule run JOBNAME` - Manually run a job, for example: `/lwc schedule run weekly`
* `/lwc schedule check JOBNAME` - Look up when the job will next run, if it's to be automatically ran. It will tell you how long until the job runs.
* `/lwc schedule autorun JOBNAME TIME` - Schedule a job to automatically run. For example, run the weekly job every week: `/lwc schedule autorun weekly 1 week` - you can combine times, e.g: `/lwc schedule autorun weekly 2 days 12 hours` = every 60 hours
* `/lwc schedule list` - List all of the known jobs. YELLOW represents a job that is not automatically scheduled and must be automatically ran. GREEN represents a job that is automatically scheduled but is not a candidate to be ran yet. RED represents a job that is waiting to be ran.
* `/lwc schedule arguments JOBNAME ARGUMENTS` - for example: `/lwc create JOBNAME expire` , `/lwc schedule arguments JOBNAME -remove 2 weeks` makes the job JOBNAME which removes protections + blocks of protections that have not be accessed in at least 2 weeks.
* `/lwc tasks` can be used instead of `/lwc schedule`

### 4.00-alpha2
* `/lwc admin report` has had a makeover and now also shows cache read/writes
* Protection rights have been inlined with the main protections table.
* Multi-group support has been added for Permissions 3.0+
* Permissions support has been modified to always depend on Superperms, while LWC's own implementations will only be used for groups. This **breaks Permissions 2/3 support** unless you have SuperpermsBridge!