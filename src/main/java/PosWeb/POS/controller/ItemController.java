package PosWeb.POS.controller;

import PosWeb.POS.service.ItemService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@Slf4j
@RequiredArgsConstructor
public class ItemController {

    private final ItemService service;

    @GetMapping("items")
    public String items(Model model) {
        return "items/posweb";
    }
}
