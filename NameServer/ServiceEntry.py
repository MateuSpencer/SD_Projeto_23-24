from ServerEntry import ServerEntry

class ServiceEntry:
    def __init__(self, service_name):
        self.service_name = service_name
        self.server_entries = []

    def add_server_entry(self, server_entry):
        if isinstance(server_entry, ServerEntry):
            self.server_entries.append(server_entry)
        else:
            raise TypeError("Expected instance of type 'ServerEntry'")