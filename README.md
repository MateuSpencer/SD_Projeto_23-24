# TupleSpaces

Distributed Systems Project 2024

**Group G65**

**Difficulty level: Bring 'em on!**


### Code Identification

In all source files (namely in the *groupId*s of the POMs), replace __GXX__ with your group identifier. The group
identifier consists of either A or T followed by the group number - always two digits. This change is important for 
code dependency management, to ensure your code runs using the correct components and not someone else's.

### Team Members

| Number | Name              | User                             | Email                                        |
|--------|-------------------|----------------------------------|----------------------------------------------|
| 100032 | Mateus Spencer    | <https://github.com/MateuSpencer>| <mailto:mateus.g.spencer@tecnico.ulisboa.pt> |
| 83897  | Goncalo Correia   | <https://github.com/layko88>     | <mailto:goncalo.t.correia@tecnico.ulisboa.pt>|
| 96656  | Joaquim Bação     | <https://github.com/joaquimlbacao-ist> | <mailto:joaquimluzbacao@tecnico.ulisboa.pt>  |

## Getting Started

The overall system is made up of several modules. The different types of servers are located in _ServerX_ (where X denotes stage 1, 2 or 3). 
The clients is in _Client_.
The definition of messages and services is in _Contract_. The future naming server
is in _NamingServer_.

See the [Project Statement](https://github.com/tecnico-distsys/TupleSpaces) for a complete domain and system description.

### Prerequisites

The Project is configured with Java 17 (which is only compatible with Maven >= 3.8), but if you want to use Java 11 you
can too -- just downgrade the version in the POMs.

To confirm that you have them installed and which versions they are, run in the terminal:

```s
javac -version
mvn -version
```

### Installation

To compile and install all modules:

```s
mvn clean install
```

## Usage

### Launching the Program

Each devivery has its tag: SD_F1, SD_F2 & SD_F3. Use chackout to open them. This usage if for the last delivery (SD_F3)

To launch the program, you need to first start the servers and then the client. Here's how you can do it:

0. To create the grpc python functions run this command

```
python -m grpc_tools.protoc -IContract\src\main\proto --python_out=Contract/target generated-sources/protobuf/python --grpc_python_out=Contract/target/generated-sources/protobuf python NameServer.proto
```

1. Navigate to the NameServer directory and run:

```
python server.py
```

2. Navigate to the directory of the server and run the following command:

```s
mvn exec:java -D exec.args="2003 C -debug"
```
Or withou the debug flag.

3. After starting the servers, navigate to the Client directory and run the following command:

```s
mvn exec:java -Dexec.args="-debug"
```
Or without the debug flag.

### Using the Program

Once the client is running, you can interact with the tuple space using the following commands:

- `put <tuple>`: This command puts a tuple into the tuple space.
- `read <pattern>`: This command reads a tuple from the tuple space that matches the given pattern.
- `take <pattern>`: This command removes a tuple from the tuple space that matches the given pattern.

Replace `<tuple>` and `<pattern>` with the actual tuple or pattern you want to use.

Write something else to show the usage of all the comands.

## Built With

* [Maven](https://maven.apache.org/) - Build and dependency management tool;
* [gRPC](https://grpc.io/) - RPC framework.
