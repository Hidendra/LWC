The file format for backup files is as follows. It is represented using C-structs for simplicity.

Backup files are represented using the `.lwc` or `.lwc.gz` extension, where the former is uncompressed and the latter is compressed using GZip.

By default, backup files are named using the naming format `MM-dd-yyyy-HHmm.lwc.gz` e.g `16-02-2012-1624.lwc.gz`

    struct BackupFile {
        short revision;
        long created; // epoch
        10byte reserved; // 10 bytes of reserved space
        RestorableEntity[] entities;
    };

    struct RESTORABLE {
        byte id; // identifies the type of entity
        RESTORABLE? payload; // either RestorableBlock or RestorableProtection
    };

    struct RestorableBlock {
        short id; // block id
        string world;
        int x;
        short y;
        int z;
        byte data;
        short item_count;
        RestorableItem[]; // Not present if item_count = 0
    };

    // Not present in RestorableEntity[], only in RestorableBlock
    struct RestorableItem {
        short slot;
        short id;
        short amount;
        short durability;
    };

    struct RestorableProtection {
        int id; // ID in the database, e.g mysql
        short blockId;
        string owner;
        string world;
        int x;
        short y;
        int z;
        string data; // acls, flags
        long created;
        long updated;
    };