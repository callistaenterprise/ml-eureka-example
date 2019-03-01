package com.example.demo2;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class Demo2Service {

    private static final Logger LOG = LoggerFactory.getLogger(Demo2Service.class);

    private final ServiceUtil serviceUtil;

    @Autowired
    public Demo2Service(ServiceUtil serviceUtil) {
        this.serviceUtil = serviceUtil;
    }

    @GetMapping(
        value    = "/ping",
        produces = "application/text")
    public String ping() {
        String serviceAddress = serviceUtil.getServiceAddress();
        LOG.debug("...pong from: {}", serviceAddress);
        return serviceAddress;
    }
}
