package com.example.demo1;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.client.discovery.composite.CompositeDiscoveryClient;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.cloud.client.loadbalancer.reactive.LoadBalancerExchangeFilterFunction;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.lang.reflect.Field;
import java.util.List;

@RestController
public class Demo1Service {

    private static final Logger LOG = LoggerFactory.getLogger(Demo1Service.class);

    private final WebClient.Builder webClientBuilder;

    private final DiscoveryClient discoveryClient;

    @Autowired
    public Demo1Service(WebClient.Builder webClientBuilder, DiscoveryClient discoveryClient) {
        this.webClientBuilder = webClientBuilder;
        this.discoveryClient = discoveryClient;
        logDiscoveryClientDebugInfo(discoveryClient);
    }

    @GetMapping(
        value    = "/demo2ping",
        produces = "application/json")
    public Mono<String> demo2ping() {
        logLoadBalancerClientInfo(webClientBuilder);
        return webClientBuilder.build().get().uri("http://demo2/ping").retrieve().bodyToMono(String.class).log();
    }

    @GetMapping(
        value    = "/demo2services",
        produces = "application/json")
    public List<ServiceInstance> serviceInstancesByApplicationName() {
        return this.discoveryClient.getInstances("demo2");
    }

    private void logDiscoveryClientDebugInfo(DiscoveryClient discoveryClient) {
        LOG.debug("Got an discoveryClient of class: {}", discoveryClient.getClass().getName());

        if (discoveryClient instanceof CompositeDiscoveryClient)  {
            CompositeDiscoveryClient compositeDiscoveryClient = (CompositeDiscoveryClient)discoveryClient;
            LOG.debug("CompositeDiscoveryClient contains {} discoveryClients", compositeDiscoveryClient.getDiscoveryClients().size());
            compositeDiscoveryClient.getDiscoveryClients().forEach(dc -> LOG.debug("Child discoveryClient of class: {}", dc.getClass().getName()));
        }

        LOG.debug("DiscoveryClient currently list {} services", discoveryClient.getServices().size());
        discoveryClient.getServices().forEach(s -> LOG.debug("Service: {}", s));

        discoveryClient.getServices().stream().map(s -> discoveryClient.getInstances(s)).flatMap(il -> il.stream()).forEach(i -> LOG.debug("getInstanceId: {}", i.getInstanceId()));
    }

    private void logLoadBalancerClientInfo(WebClient.Builder webClientBuilder) {
        webClientBuilder.filters(l -> l.forEach(f -> {
            LOG.debug("Filter: {}", f.getClass().getName());

            if (f instanceof LoadBalancerExchangeFilterFunction) {
                LoadBalancerExchangeFilterFunction lbeff = (LoadBalancerExchangeFilterFunction)f;

                try {
                    Field privateField = lbeff.getClass().getDeclaredField("loadBalancerClient");
                    privateField.setAccessible(true);
                    LoadBalancerClient loadBalancerClient = (LoadBalancerClient) privateField.get(lbeff);
                    LOG.debug("LoadBalancerClient of class: {}", loadBalancerClient.getClass().getName());
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }));
    }
}