from http.server import ThreadingHTTPServer, BaseHTTPRequestHandler

class MyServer(BaseHTTPRequestHandler):
    def do_GET(self):
        self.send_response(200)
        self.send_header('Content-Type', 'text.html')
        self.end_headers()


server = ThreadingHTTPServer(("0.0.0.0",8000), MyServer)
print("Server started at http://localhost:8000")
server.serve_forever()
server.server_close()