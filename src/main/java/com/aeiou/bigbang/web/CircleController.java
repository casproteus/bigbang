package com.aeiou.bigbang.web;

import com.aeiou.bigbang.domain.Circle;
import org.springframework.roo.addon.web.mvc.controller.json.RooWebJson;
import org.springframework.roo.addon.web.mvc.controller.scaffold.RooWebScaffold;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("/circles")
@Controller
@RooWebScaffold(path = "circles", formBackingObject = Circle.class)
@RooWebJson(jsonObject = Circle.class)
public class CircleController {
}
