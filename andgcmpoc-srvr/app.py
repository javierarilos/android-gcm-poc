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


""" you must fill this AUTHORIZATION_KEY constant with your authorization key in the form:
    'key=XXXXXXXXXXXX'
"""
AUTHORIZATION_KEY = ""


if __name__ == '__main__':

    if not AUTHORIZATION_KEY:
        print "AUTHORIZATION_KEY must be filled. Exiting"
        import sys
        sys.exit(1)

    ip = os.getenv('OPENSHIFT_PYTHON_IP', '0.0.0.0')
    port = int(os.getenv('OPENSHIFT_PYTHON_PORT', '9898'))

    from bottle import Bottle, run, request, response
    import requests, json

    session = requests.Session()
    app = Bottle()
    """ recvrs is a dictionary for receivers.
    keys are: domain.recvr_id, values are dictionaries: {domain: <domain>, recvr_id: <recvr_id>, token: <token>}"""
    recvrs = {}
    SEPARATOR = '/'

    def key(domain, recvr_id):
        return domain + SEPARATOR + recvr_id

    def _get_registration(domain, recvr_id):
        return recvrs[key(domain, recvr_id)]

    # REGISTER RECVR:
    # curl -v -X POST http://MYSERVER/register/do1/re1/to1
    @app.post("/register/<domain>/<recvr_id>/<token>")
    def post_register(domain, recvr_id, token):
        recvrs[key(domain, recvr_id)] = {'domain': domain, 'recvr_id': recvr_id, 'token': token}
        return {"result": "OK"}

    # GET RECVR:
    # curl -v -X GET http://MYSERVER/register/do1/re1
    @app.get("/register/<domain>/<recvr_id>")
    def get_recvr(domain, recvr_id):
        try:
            return _get_registration(domain, recvr_id)
        except KeyError:
            response.status = 404
            return "Not registered: {}{}{}".format(domain, SEPARATOR, recvr_id)

    # GET ALL RECVRS:
    # curl -v -X GET http://MYSERVER/register/
    @app.get("/register")
    def get_all_recvrs():
        return str(recvrs.values())

    # SEND TO RECVR:
    # curl -v -X POST -d "pushing around the world" http://MYSERVER/send/do1/re1
    @app.post("/send/<domain>/<recvr_id>")
    def post_send(domain, recvr_id):
        body = "".join(request.body)

        try:
            registration = _get_registration(domain, recvr_id)
            registration_ids = [registration["token"]]
            body_dict = json.loads(body)
            data = json.dumps({"data": body_dict, "registration_ids": registration_ids})
            r = session.post("https://android.googleapis.com/gcm/send",
                             headers={"Authorization": AUTHORIZATION_KEY,
                                      "Content-Type": "application/json"},
                             data=data)
            print("Response from google : status '{}', content '{}' ".format(r.status_code, r.content))
        except KeyError, ke:
            response.status = 404
            return {"result": "KO", "reason": "Not registered: {}{}{}".format(domain, SEPARATOR, recvr_id)}
        except ValueError, ve:
                response.status = 400
                return {"result": "KO",
                        "reason": "Expecting valid JSON, body: '{}' error: '{}' ".format(body, ve.message)}
        return {"result": "OK" if r.status_code == 200 else "KO",
                "recvr": registration,
                "response": str(r.content)}

    @app.get("/time")
    def get_time():
        from time import time
        return "current time is: "+str(time())

    @app.error(404)
    def not_found(err):
        return "(404) Url not found. {}".format(request.url)

    run(app, host=ip, port=port, debug=True, reloader=True)
