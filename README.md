# feup-sdis

# Distributed backup service

## How to run

- To compile in LINUX:
	- using a terminal, navigate to the project's root folder and type "sh compile.sh"
	- after that run "sh rmi.sh" to start rmiregistry
	
- To run the peer:
	- open a terminal, navigate to the project's root folder and type "sh peer.sh <version> <id>"

- To run the TestApp:
	- open a terminal, navigate to the project's root folder and type "sh xxxx.sh" (xxxx -> backup or restore or delete or state)
	
- To clear the peers files:
	- open a terminal, navigate to the project's root folder and type "sh clearFileSystem.sh"
	
	
## How to compile

The project can be compiled using the *javac* command, using script compile.sh 


## How to run

### With scripts

- Into Proj1 folder:
  1. Run sh rmi.sh
  2. Run sh compile.sh
  3. Run sh peer.sh (see the usage of script)
  4. Run sh xxxx.sh (depending on invoking method of testApp)

- To reset the peers:
  - Run sh.clearFileSystem.sh

### Without scripts

  1. After having built the project with javac, open a *Terminal* in the **bin** folder resultant of the build process. 
  2. Need to start RMI Registry (See RMI registry section).
  3. After that you can then launch a peer or invoke the testApp (See Peer section and Test App section).


## RMI registry

As suggested, the interface implementation uses RMI. In order to interact with the service, an *rmiregistry* instance must be running inside **bin** folder.

The following command launches an *rmiregistry* instance in the background:
```rmiregistry &```


## Peer

If the user intends to interact with a peer, a name for the remote object must be specified, hence the alternative usages:

```
To run a peer:
Usage: java -classpath bin service.Peer <protocol_version> <server_id> <service_access_point> <mc:port> <mdb:port> <mdr:port>

Argument description:
		protocol_version - version of protocol used (1.0, 1.1, 1.2, 1.3 or 2.0)
		server_id - Integer representing the peer’s unique identifier
		service_access_point - host to access the rmi registry (format explained on RMI Registry section)
		mc:port - IP address:Port of the CONTROL channel
		mdb:port – IP address:Port of the BACKUP channel
		mdr:port – IP address:Port of the RESTORE channel
				
        Protocol version:                   service_access_point:
          1.0 -> Default                          - Formats on Peer: //host(:port)?/
          1.1 -> Backup Enhancement               - Formats on testApp: //host(:port)?/peerID
          1.2 -> Restore Enhancement
          1.3 -> Delete Enhancement
          2.0 -> All Enhancement
		
Eg. java -classpath bin service.Peer "$1" "$2" //localhost/ 224.0.0.0:8000 224.0.0.0:8001 224.0.0.0:8002

```

## Test App

```
To run test app
 Usage: java TestApp <peer_ap> <sub_protocol> <opnd_1> <opnd_2
 
 Argument description:
		peer_app – local peer access point
		sub-protocol – can be BACKUP, RESTORE, RECLAIM, DELETE or STATE
		opnd_1 – path name of the file or amount of space to be reclaim
		opnd_2 – specifies the desired replication degree. Applies only to backup sub-protocol
 
Eg.
 java -classpath bin service.TestApp //localhost/1 BACKUP "files/image1.png" 1
 java -classpath bin service.TestApp //localhost/1 DELETE "files/image1.png"
 java -classpath bin service.TestApp //localhost/2 RECLAIM 140000
 java -classpath bin service.TestApp //localhost/1 RESTORE "files/image1.png"
 java -classpath bin service.TestApp //localhost/1 STATE
 
```

### Defaults

Usages where the user does not specify the multicast addresses and ports will have the following defaults (on script):

|RMI              |MC            |MDB           |MDR           |
|-----------------|--------------|--------------|--------------|
|//localhost:1099/|224.0.0.0:8000|224.0.0.0:8001|224.0.0.0:8002|

The default size of a peer on system is 8MB.


### Local files and configuration

- The source files are under the project **src** folder.

- The class files are under the project **bin** folder.

- The files used to test are under the project **files** folder.

- The filesystem of each pear are under the project **fileSystem** folder, on each peer contains a **peerID** folder that contains **chunks** folder, **restore** folder and **db** file. Each **chunks** folder contains folders named by fileID that contains chunks backed up which belongs to that file.

You need not generate these files manually. Once the *Peer* is launched for the very first time, they will be automatically created.

You should not manually edit **db**. These files are managed by the service.

## Explanation os scripts

- compile.sh
  - Script that compile java classes into a bin folder.

- clearFileSystem.sh
  - Script that delete fileSystem of peers.

- peer.sh
  - Script that allows to run the peer with some parameters with default value.

    Usage: peer.sh <version> <peer_num>
      Eg. sh peer.sh 1.0 1

- Others scripts
  - The scripts like backup.sh, restore.sh, delete.sh run in peer 1 with default file as image1.png
  - The script reclaim.sh run in peer 2 with a size of 1400000.
  - And the script state.sh retrieve information about peer 1 and peer 2.
