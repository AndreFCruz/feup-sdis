# feup-sdis

## TODO LIST

- [ ] Save and load database (Serializable?)
- [ ] Convert String to Integers on Maps
- [ ] Manage space on disk after doing things
- [ ] Update parameters of TestApp to allow specify protocol version, server id, service acess point, MC , MDB, MDR 

### Message
- [ ] On messages check if exists two CRLF to validate the message (Actually, it's assumed that is correct)
- [ ] Limit chunkNO to 1 million chunks bc this field should not be larger than 6 chars. (File max 64gb)
- [ ] Limit Replication Degree up to 9
- [ ] Check messages structure and update function convertHeaderToString

### Backup
- [ ] Furthermore, the initiator-peer needs to keep track of which peers have responded.
A peer should also count the number of confirmation messages for each of the chunks it has stored and keep that count in non-volatile memory. This information can be useful if the peer runs out of disk space: in that event, the peer may try to free some space by evicting chunks whose actual replication degree is higher than the desired replication degree.

- [ ] 

### Restore
- [ ] 
- [ ] 

### Delete
- [ ] 
- [ ] 

### 
- [ ] 
- [ ] 
- [ ] 
- [ ] 
- [ ] 

### Enhancements
- [ ] This scheme can deplete the backup space rather rapidly, and cause too much activity on the nodes once that space is full. Can you think of an alternative scheme that ensures the desired replication degree, avoids these problems, and, nevertheless, can interoperate with peers that execute the chunk backup protocol described above?

- [ ]
- [ ]
