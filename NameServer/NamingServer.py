from ServiceEntry import ServiceEntry

class NamingServer:
    def __init__(self):
        self.service_map = {}

    def add_service_entry(self, service_name, service_entry):
        if isinstance(service_entry, ServiceEntry):
            self.service_map[service_name] = service_entry
        else:
            raise TypeError("Expected instance of type 'ServiceEntry'")