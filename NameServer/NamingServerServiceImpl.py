import sys
sys.path.insert(1, '../contract/target/generated-sources/protobuf/python')
import NameServer_pb2 as pb2
import NameServer_pb2_grpc as pb2_grpc

class NamingServerServiceImpl (pb2_grpc.NamingServerServiceServicer):

    def __init__(self, *args, **kwargs):
        pass

    def register(self, request, context):
        # TODO: if debug is enabled
        print("Received register request")
        print("Service name: " + request.serviceName)
        print("Address: " + request.address.host + ":" + str(request.address.port))
        print("Qualifier: " + request.qualifier)
        
        # TODO: implement
        
        return pb2.registerResponse()
    
    def lookup(self, request, context):
        # TODO: if debug is enabled
        print("Received lookup request")
        print("Service name: " + request.serviceName)
        print("Qualifier: " + request.qualifier)
        
        # TODO: implement
        
        return pb2.lookupResponse()
    
    def delete(self, request, context):
        # TODO: if debug is enabled
        print("Received delete request")
        print("Service name: " + request.serviceName)
        print("Address: " + request.address.host + ":" + str(request.address.port))
        
        # TODO: implement
        
        return pb2.deleteResponse()
