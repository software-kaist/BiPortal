'''
Created on 2015. 12. 6.

@author: SUNgHOOn
'''

import http.client
import urllib.parse
from epcis import Capture

epcis_host = "125.131.73.191"
epcis_port = 8080

def epcis_get(service):
    conn = http.client.HTTPConnection(epcis_host, epcis_port)
    conn.request("GET", service)
    res = conn.getresponse()
    print(res.status, res.reason)
    
    data = res.read()
    print(data.__str__())
#     print(data.title())# .lstrip())
#     print(data.upper())
    conn.close()
    
def epcis_post(url, params, headers):
    conn = http.client.HTTPConnection(epcis_host, epcis_port)
    conn.request("POST", url, params, headers)
    res = conn.getresponse()
    print(res.status, res.reason)

    data = res.read()
    print(data.__str__())

    conn.close()

if __name__ == '__main__':
#     epcis_get("/epcis/Service/GetStandardVersion")
#     epcis_get("/epcis/Service/GetQueryNames")

    url = "/epcis/Service/EventCapture"
    params = Capture.capture_str()
    headers = {"Content-type": "application/xml", "Accept": "text/plain"}
    print(params)
    epcis_post(url, params, headers)
    
#     conn = http.client.HTTPSConnection("localhost", 8080)
#     conn.set_tunnel("www.python.org")
#     conn.request("HEAD","/index.html")
    pass