package vn.hoidanit.laptopshop.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import java.util.List;
import vn.hoidanit.laptopshop.domain.User;
import vn.hoidanit.laptopshop.service.UserService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @RequestMapping("/")
    public String getHomePage(Model model) {
        List<User> users = this.userService.getAllUsersByEmail("np2911@gmail.com");
        System.out.println("Users: " + users);

        model.addAttribute("message", "message from controller");
        return "hello";
    }

    // Get user page (table info view)
    @RequestMapping("/admin/user")
    public String getUserPage(Model model) {
        List<User> users = this.userService.getAllUsers();
        model.addAttribute("users", users);
        return "admin/user/table-user";
    }

    @RequestMapping("/admin/user/{id}")
    public String getUserDetailPage(Model model, @PathVariable long id) {
        User user = this.userService.getUserById(id);
        model.addAttribute("id", id);
        model.addAttribute("userDetail", user);
        return "admin/user/show";
    }

    // Get create user page (view)
    @RequestMapping("/admin/user/create")
    public String getCreateUserPage(Model model) {
        model.addAttribute("newUser", new User());
        return "admin/user/create";
    }

    // Create user (create user by button confirm click)
    @RequestMapping(value = "/admin/user/create", method = RequestMethod.POST)
    public String createUserPage(Model model, @ModelAttribute("newUser") User newUser) {
        this.userService.handelSaveUser(newUser);
        return "redirect:/admin/user"; // redirect to user page (table info)
    }

    // Get update user page (view)
    @RequestMapping("/admin/user/update/{id}")
    public String getUserUpdatePage(Model model, @PathVariable long id) {
        User currentUser = this.userService.getUserById(id);
        model.addAttribute("newUser", currentUser);
        return "admin/user/update";
    }

    @PostMapping("/admin/user/update")
    public String postUpdateUser(Model model, @ModelAttribute("newUser") User updatedUser) {
        User currentUser = this.userService.getUserById(updatedUser.getId());
        if (currentUser != null) {
            currentUser.setFullName(updatedUser.getFullName());
            currentUser.setAddress(updatedUser.getAddress());
            currentUser.setPhone(updatedUser.getPhone());
            this.userService.handelSaveUser(currentUser);
        }
        return "redirect:/admin/user"; // redirect to user page (table info)
    }

    @GetMapping("/admin/user/delete/{id}")
    public String getUserDeletePage(Model model, @PathVariable long id) {
        model.addAttribute("id", id);
        return "admin/user/delete";
    }

    @PostMapping("/admin/user/delete")
    public String postDeleteUser(Model model, @RequestParam("id") long id) {
        this.userService.deleteUserById(id);
        return "redirect:/admin/user"; // redirect to user page (table info)
    }

}
