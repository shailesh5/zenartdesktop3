/**
 * 
 */
package com.jio.asp.gstr1.v30.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Amit1.Dwivedi
 *
 */
@RestController
public class PingController {
	@RequestMapping(value = "/ping", method = RequestMethod.GET)
	String pingController(){
		return "pong";
	}
}
