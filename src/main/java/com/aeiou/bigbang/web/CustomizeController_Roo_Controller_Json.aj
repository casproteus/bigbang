// WARNING: DO NOT EDIT THIS FILE. THIS FILE IS MANAGED BY SPRING ROO.
// You may push code into the target .java compilation unit if you wish to edit any member(s).

package com.aeiou.bigbang.web;

import com.aeiou.bigbang.domain.Customize;
import com.aeiou.bigbang.web.CustomizeController;
import java.util.List;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.util.UriComponentsBuilder;

privileged aspect CustomizeController_Roo_Controller_Json{

@RequestMapping(value="/{id}",method=RequestMethod.GET,headers="Accept=application/json")@ResponseBody public ResponseEntity<String>CustomizeController.showJson(@PathVariable("id")Long id){HttpHeaders headers=new HttpHeaders();headers.add("Content-Type","application/json; charset=utf-8");try{Customize customize=Customize.findCustomize(id);if(customize==null){return new ResponseEntity<String>(headers,HttpStatus.NOT_FOUND);}return new ResponseEntity<String>(customize.toJson(),headers,HttpStatus.OK);}catch(Exception e){return new ResponseEntity<String>("{\"ERROR\":"+e.getMessage()+"\"}",headers,HttpStatus.INTERNAL_SERVER_ERROR);}}

@RequestMapping(headers="Accept=application/json")@ResponseBody public ResponseEntity<String>CustomizeController.listJson(){HttpHeaders headers=new HttpHeaders();headers.add("Content-Type","application/json; charset=utf-8");try{List<Customize>result=Customize.findAllCustomizes();return new ResponseEntity<String>(Customize.toJsonArray(result),headers,HttpStatus.OK);}catch(Exception e){return new ResponseEntity<String>("{\"ERROR\":"+e.getMessage()+"\"}",headers,HttpStatus.INTERNAL_SERVER_ERROR);}}

@RequestMapping(method=RequestMethod.POST,headers="Accept=application/json")public ResponseEntity<String>CustomizeController.createFromJson(@RequestBody String json,UriComponentsBuilder uriBuilder){HttpHeaders headers=new HttpHeaders();headers.add("Content-Type","application/json");try{Customize customize=Customize.fromJsonToCustomize(json);customize.persist();RequestMapping a=(RequestMapping)getClass().getAnnotation(RequestMapping.class);headers.add("Location",uriBuilder.path(a.value()[0]+"/"+customize.getId().toString()).build().toUriString());return new ResponseEntity<String>(headers,HttpStatus.CREATED);}catch(Exception e){return new ResponseEntity<String>("{\"ERROR\":"+e.getMessage()+"\"}",headers,HttpStatus.INTERNAL_SERVER_ERROR);}}

@RequestMapping(value="/jsonArray",method=RequestMethod.POST,headers="Accept=application/json")public ResponseEntity<String>CustomizeController.createFromJsonArray(@RequestBody String json){HttpHeaders headers=new HttpHeaders();headers.add("Content-Type","application/json");try{for(Customize customize:Customize.fromJsonArrayToCustomizes(json)){customize.persist();}return new ResponseEntity<String>(headers,HttpStatus.CREATED);}catch(Exception e){return new ResponseEntity<String>("{\"ERROR\":"+e.getMessage()+"\"}",headers,HttpStatus.INTERNAL_SERVER_ERROR);}}

@RequestMapping(value="/{id}",method=RequestMethod.PUT,headers="Accept=application/json")public ResponseEntity<String>CustomizeController.updateFromJson(@RequestBody String json,@PathVariable("id")Long id){HttpHeaders headers=new HttpHeaders();headers.add("Content-Type","application/json");try{Customize customize=Customize.fromJsonToCustomize(json);customize.setId(id);if(customize.merge()==null){return new ResponseEntity<String>(headers,HttpStatus.NOT_FOUND);}return new ResponseEntity<String>(headers,HttpStatus.OK);}catch(Exception e){return new ResponseEntity<String>("{\"ERROR\":"+e.getMessage()+"\"}",headers,HttpStatus.INTERNAL_SERVER_ERROR);}}

@RequestMapping(value="/{id}",method=RequestMethod.DELETE,headers="Accept=application/json")public ResponseEntity<String>CustomizeController.deleteFromJson(@PathVariable("id")Long id){HttpHeaders headers=new HttpHeaders();headers.add("Content-Type","application/json");try{Customize customize=Customize.findCustomize(id);if(customize==null){return new ResponseEntity<String>(headers,HttpStatus.NOT_FOUND);}customize.remove();return new ResponseEntity<String>(headers,HttpStatus.OK);}catch(Exception e){return new ResponseEntity<String>("{\"ERROR\":"+e.getMessage()+"\"}",headers,HttpStatus.INTERNAL_SERVER_ERROR);}}

}
