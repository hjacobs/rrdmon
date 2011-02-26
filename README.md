RRD Mon
=======

Graphs data from RRD files (collected by JMX collector or similar).

Requirements
------------

You need to download rrd4j from <http://code.google.com/p/rrd4j/> and install it into your local Maven repository.


Configuration
-------------

The easiest way to configure it is using a custom `datasources.cfg` file (must be in tomcat home), see example below:

    # very simple config for a RRD files filled with orders/s (e.g. from DB):
    db
        sales: Sales/s
            rrd=req:/data/rrd/db/sales.rrd:sales
            line=req:#000000:total:1
    

    # configuration for host web01 having two tomcat instances:
    web01
        requests: Requests/s
            rrd = req0:/data/rrd/%h/p8080/%n.rrd:%n
            rrd = req1:/data/rrd/%h/p8081/%n.rrd:%n
            expr = req:req0,req1,+
            line = req:#000000:total:1
            area = req0:#999999:p8080
            area = req1:#aaaaaa:p8081:stack

    # The following lines will use exactly the same config for web02-web04
    # (replacing %h in the web01 config with host names web02-04)
    web02 = web01
    web03 = web01
    web04 = web01


Building
--------

Building the WAR archive with maven is easy:

    mvn package


