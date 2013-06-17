package com.aeiou.bigbang.web;

import com.aeiou.bigbang.domain.Customize;
import org.springframework.roo.addon.web.mvc.controller.json.RooWebJson;
import org.springframework.roo.addon.web.mvc.controller.scaffold.RooWebScaffold;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("/customizes")
@Controller
@RooWebScaffold(path = "customizes", formBackingObject = Customize.class)
@RooWebJson(jsonObject = Customize.class)
public class CustomizeController {
}
