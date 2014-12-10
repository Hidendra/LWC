There are several large differences from LWC v4. Alias commands (`/cprivate`, `/cremove`, etc) however are mostly the same. The following are the most noticeable differences:

* H2 is now the default database instead of SQLite. Available databases: `h2`, `sqlite`, `mysql`, `postgresql`, and `memory` (no persistence; protection data is lost after the server stops)
* Dynamic translations: LWC knows what language a player's client uses so if the language they are using is translated by LWC then it will send text for LWC to them in their client's language
* Commands (besides aliases) are very different for most commands. See [Commands](../user-guide/commands.md) for more info.
* Protections now have multiple access levels and each protection can have multiple owners; example access levels are:
    * `depositonly` (cannot withdraw items *if supported by the server*)
    * `member` (default; access the protection but not modify permissions on it)
    * `admin` (access & modify permissions except other admins)
    * `owner` (access & modify permissions for anyone incl. other owners)
* others (to-do add)
