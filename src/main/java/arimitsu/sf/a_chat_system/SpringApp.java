package arimitsu.sf.a_chat_system;

import akka.actor.ActorSystem;
import akka.actor.ActorSystem$;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@SpringBootApplication
public class SpringApp {
    public static void main(String... args) {
        // TODO init spring
        // TODO connect akka cluster
        SpringApplication.run(SpringApp.class, args);
    }

    private ChatSystem system = new ChatSystem();

    @RequestMapping(value = "/")
    public String index() {
        return "/index.html";
    }

    @RequestMapping(value = "/login")
    public String login(@RequestParam("id") String id) {
        system.login(id);
        // id session mapping.
        // generate id and set cookie
        return "redirect:/chat.html";
    }

}
