import socketserver
from DatabaseManager import *

def verify(value):
    if value == b"sbg1901":
        return True
    return False

class MyTCPHandler(socketserver.BaseRequestHandler):

    def handle(self):
        # self.request is the TCP socket connected to the client
        data = self.request.recv(1024).strip()
        print("{} wrote:".format(self.client_address[0]))
        print(data.decode())
        # just send back the same data, but upper-cased
        self.request.sendall(data.upper())


if __name__ == "__main__":
    HOST, PORT = "localhost", 9999
    print("made******")
    # Create the server, binding to localhost on port 9999
    server = socketserver.TCPServer((HOST, PORT), MyTCPHandler)

    # Activate the server; this will keep running until you
    # interrupt the program with Ctrl-C
    server.serve_forever()