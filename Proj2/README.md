# Adversarial search task distribution via chord protocol

## How to run
From the Project's root folder:

1. To compile in LINUX/UNIX: using a terminal, navigate to the project's root folder and run the 'compile.sh' script:
  > sh compile.sh

2. To start the RMI registry run the 'rmi.sh' script:
  > sh rmi.sh  

3. To start a peer, use the 'peer.sh' script:
  > sh peer.sh <port> [<contact_ip:contact_port>]
  
If no contact provided, peer will create a new Chord ring.

(_e.g._ ```sh peer.sh 4000 localhost:4001```)

4. To run the ClientApp:
  - open a terminal, navigate to the project's root folder and type "sh query.sh <peer_address:port> <action> [< oper1 > < oper2 >]"
    - < action > is one of ```status```, ```get```, ```put``` or ```find_successor```.
	(_e.g._ ```sh query.sh localhost:4001 STATUS```)


## How to compile

The project can be compiled as usual with the ```javac``` command, or by running ```sh compile.sh``` from the project's root folder.

## RMI registry

As suggested, the interface implementation uses RMI. In order to interact with the service, an *rmiregistry* instance must be running inside **out/production/Proj2** folder.

The following command launches an *rmiregistry* instance in the background:
```rmiregistry &```


## Peer

```
To run a peer:
Usage: java -classpath out/production/Proj2 service.InitPeer <port> [<contact_ip:contact_port>]

Argument description:
		port - Port allocated to create the new peer
		contact_ip:contact_port - Address of peer to chord contact
				
Eg. java -classpath out/production/Proj2 service.InitPeer 4000 localhost:4001


```

## Client App

```
To run clent app
 Usage: java -classpath out/production/Proj2 service.InitClient <peer_address:port> <action> [<oper1> <oper2>]
 
 Argument description:
		<peer_address:port> – peer access point
		action – can be STATUS, LOOKUP, PUT, FIND_SUCCESSOR
		oper1 – 
		oper2 – 
Note: oper1, oper2,... depends on action

Eg.
 java -classpath out/production/Proj2 service.InitClient localhost:4000 STATUS
 java -classpath out/production/Proj2 service.InitClient localhost:4000 GET $1
 java -classpath out/production/Proj2 service.InitClient localhost:4000 PUT $1 $2
 (where $1 represents Key and $2 represent object)
```

### Defaults

|RMI              |
|-----------------|
|//localhost:1099/|


### Local files and configuration

- The source files are under the project **src** folder.

- The class files are under the project **out** folder.

- The reports are under the project **docs** folder.



## Scripts' Specification

- compile.sh
  - Script to compile java classes into a out folder.

- peer.sh
  - Script to launch an instance of a peer with some parameters.

    Usage: peer.sh <port> [<contact_ip:contact_port>]
      Eg. sh peer.sh 4000
      
 - query.sh
  - Script to launch an instance of a client to interact with peers.

    Usage: query.sh <peer_address:port> <action> [<oper1> <oper2>]
      Eg. sh query.sh localhost:4000 GET 12312412312     
      
