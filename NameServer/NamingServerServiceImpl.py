import sys
sys.path.insert(1, '../contract/target/generated-sources/protobuf/python')
import NameServer_pb2 as pb2
import NameServer_pb2_grpc as pb2_grpc
from NamingServer import NamingServer
from ServiceEntry import ServiceEntry
from ServerEntry import ServerEntry

class NamingServerServiceImpl (pb2_grpc.NamingServerServiceServicer):

    def __init__(self, debug=False, *args, **kwargs):
        self.naming_server = NamingServer()
        self.debug = debug

    def register(self, request, context):
        if self.debug:
            print("Received register request")
            print("Service name: " + request.serviceName)
            print("Address: " + request.address.host + ":" + str(request.address.port))
            print("Qualifier: " + request.qualifier)
        
        server_entry = ServerEntry(request.address.host, request.address.port, request.qualifier)
        if request.serviceName not in self.naming_server.service_map:
            self.naming_server.add_service_entry(request.serviceName, ServiceEntry(request.serviceName))
        else:
            for se in self.naming_server.service_map[request.serviceName].server_entries:
                if se.host == server_entry.host and se.port == server_entry.port:
                    raise Exception("Not possible to register the server")
        self.naming_server.service_map[request.serviceName].add_server_entry(server_entry)
        
        return pb2.registerResponse()
    
    def lookup(self, request, context):
        if self.debug:
            print("Received lookup request")
            print("Service name: " + request.serviceName)
            print("Qualifier: " + request.qualifier)
        
        if request.serviceName in self.naming_server.service_map:
            service_entry = self.naming_server.service_map[request.serviceName]
            if request.qualifier:
                server_entries = [se for se in service_entry.server_entries if se.qualifier == request.qualifier]
            else:
                server_entries = service_entry.server_entries
            return pb2.LookUpResponse(ServerEntry=server_entries)
        else:
            return pb2.LookUpResponse()
    
    def delete(self, request, context):
        if self.debug:
            print("Received delete request")
            print("Service name: " + request.serviceName)
            print("Address: " + request.address.host + ":" + str(request.address.port))
        
        if request.serviceName in self.naming_server.service_map:
            service_entry = self.naming_server.service_map[request.serviceName]
            server_entries = [se for se in service_entry.server_entries if se.host != request.address.host or se.port != request.address.port]
            service_entry.server_entries = server_entries
            return pb2.deleteResponse()
        else:
            raise Exception("Not possible to remove the server")