package com.actico.jax.reactive.controller;

import static org.springframework.web.bind.annotation.RequestMethod.*;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/rest")
@CrossOrigin("*")
public class TestRestController
{
   private static final Logger LOG = LoggerFactory.getLogger(TestRestController.class);


   @RequestMapping(method = { GET, POST, PATCH, TRACE, OPTIONS, PUT }, value = "/sink")
   public ResponseEntity<String> sink(@RequestHeader Map<String, Object> headers, @RequestBody String string)
   {
      LOG.info("Got Request: {}", string);
      LOG.info("---- Headers: {}", headers);
      return ResponseEntity.ok(string);
   }

}
