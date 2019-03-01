# ml-eureka-example

Demonstrates how to use Netflix Eureka server with SPring Cloud.

Service `Demo1` calls instances of the `Demo2` service using an `Eureka` discovery service to locate the `Demo2` instances

# Download and build

	git clone git@github.com:callistaenterprise/ml-eureka-example.git
	cd ml-eureka-example
	./gradlew build
	
# Test

Start one instance of the `Demo1` service, two instances of the `Demo2` service and one `Eureka` server:

	java -jar demo1/build/libs/*.jar &
	java -jar demo2/build/libs/*.jar &
    java -Dserver.port=7003 -jar demo2/build/libs/*.jar &
	java -jar eureka-server/build/libs/*.jar &

Verify that the three service instances are registered with the Eureka server:

    curl -s -H "accept:application/json" localhost:8761/eureka/apps | jq -r .applications.application[].instance[].instanceId

Expect a response similar to (you will see another hostname...):

    magnus-mbp32.lan:demo1:7001
    magnus-mbp32.lan:demo2:7002
    magnus-mbp32.lan:demo2:7003

Try the DiscoveryClient in `demo1`:

    curl localhost:7001/demo2services -s | jq -r .[].instanceInfo.instanceId

Expect a response like:

    magnus-mbp32.lan:demo2:7002
    magnus-mbp32.lan:demo2:7003

Call the `Demo1` service from the `Demo2` service a couple of times using:

    curl localhost:7001/demo2ping

Expect responses from the two `Demo1` service instances in a round robin fashion:

    Magnus-MBP32.lan/192.168.1.128:7003
    Magnus-MBP32.lan/192.168.1.128:7002

Tear down

    kill $(jobs -p)