### 4.00-alpha2
* `/lwc admin report` has had a makeover and now also shows cache read/writes
* Protection rights have been inlined with the main protections table.
* Multi-group support has been added for Permissions 3.0+
* Permissions support has been modified to always depend on Superperms, while LWC's own implementations will only be used for groups. This **breaks Permissions 2/3 support** unless you have SuperpermsBridge!