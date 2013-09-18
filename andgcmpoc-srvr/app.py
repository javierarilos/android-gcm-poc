#!/usr/bin/env python

#
# This file may be used instead of Apache mod_wsgi to run your python
# web application in a different framework.  A few examples are
# provided (cherrypi, gevent), but this file may be altered to run
# whatever framework is desired - or a completely customized service.
#
import os

try:
    zvirtenv = os.path.join(os.getenv('OPENSHIFT_PYTHON_DIR', '.'),
                            'virtenv', 'bin', 'activate_this.py')
    execfile(zvirtenv, dict(__file__=zvirtenv))
except IOError:
    pass

#
# IMPORTANT: Put any additional includes below this line.  If placed above this
# line, it's possible required libraries won't be in your searchable path
#

if __name__ == '__main__':
    ip = os.getenv('OPENSHIFT_PYTHON_IP', '0.0.0.0')
    port = int(os.getenv('OPENSHIFT_PYTHON_PORT', '9898'))

    from bottle import Bottle, run, request, response

    app = Bottle()
    # recvrs is a dictionary for receivers.
    # keys are: domain.recvr_id, values are dictionaries: {domain: <domain>, recvr_id: <recvr_id>, token: <token>}
    recvrs = {}
    SEPARATOR = '/'

    def key(domain, recvr_id):
        return domain + SEPARATOR + recvr_id

    # REGISTER RECVR:
    # curl -v -X POST http://androidgcmpoc-corralito.rhcloud.com/register/do1/re1/to1
    @app.post("/register/<domain>/<recvr_id>/<token>")
    def register(domain, recvr_id, token):
        recvrs[key(domain, recvr_id)] = {'domain': domain, 'recvr_id': recvr_id, 'token': token}
        return {"result": "OK"}

    # GET RECVR:
    # curl -v -X GET http://androidgcmpoc-corralito.rhcloud.com/register/do1/re1
    @app.get("/register/<domain>/<recvr_id>")
    def get_recvr(domain, recvr_id):
        try:
            return recvrs[key(domain, recvr_id)]
        except KeyError:
            response.status = 404
            return "Not registered: {}{}{}".format(domain, SEPARATOR, recvr_id)

    # SEND TO RECVR:
    # curl -v -X POST -d "pushing around the world" http://androidgcmpoc-corralito.rhcloud.com/send/do1/re1
    @app.post("/send/<domain>/<recvr_id>")
    def send(domain, recvr_id):
        body = "".join(request.body)
        return {"result": "OK", "recvr": get_recvr(domain, recvr_id), "body": body}

    @app.get("/time")
    def get_time():
        from time import time
        return str(time())

    @app.error(404)
    def not_found(err):
        return "(404) Url not found. {}".format(request.url)

    run(app, host=ip, port=port, debug=True, reloader=True)
