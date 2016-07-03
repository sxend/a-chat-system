package arimitsu.sf.a_chat_system;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.annotation.PostConstruct;

@Controller
@SpringBootApplication
public class SpringApp {
    public static void main(String... args) {
        // TODO connect akka cluster
        SpringApplication.run(SpringApp.class, args);
    }

    @Autowired
    private Components components;

    @PostConstruct
    public void initialize() {
        ChatSystem.startMember(this.components);
    }

    @RequestMapping(value = "/")
    public String index() {
        return "/index.html";
    }

    @RequestMapping(value = "/login")
    public String login(@RequestParam("room") String room, @RequestParam("name") String name) {
        return "redirect:/chat.html?room=" + room + "&name=" + name;
    }

}
