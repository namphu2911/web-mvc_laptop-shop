package vn.hoidanit.laptopshop.controller.admin;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import vn.hoidanit.laptopshop.domain.User;
import vn.hoidanit.laptopshop.service.UploadService;
import vn.hoidanit.laptopshop.service.UserService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

@Controller
public class UserController {

    private final UserService userService;
    private final UploadService uploadService;
    private final PasswordEncoder passwordEncoder;

    public UserController(UserService userService, UploadService uploadService, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.uploadService = uploadService;
        this.passwordEncoder = passwordEncoder;
    }

    // Get user page (table info view)
    @RequestMapping("/admin/user")
    public String getUserPage(Model model) {
        List<User> users = this.userService.getAllUsers();
        model.addAttribute("users", users);
        return "admin/user/show";
    }

    @RequestMapping("/admin/user/{id}")
    public String getUserDetailPage(Model model, @PathVariable long id) {
        User user = this.userService.getUserById(id);
        model.addAttribute("id", id);
        model.addAttribute("userDetail", user);
        return "admin/user/detail";
    }

    // Get create user page (view)
    @GetMapping("/admin/user/create")
    public String getCreateUserPage(Model model) {
        model.addAttribute("newUser", new User());
        return "admin/user/create";
    }

    // Create user (create user by button confirm click)
    @PostMapping(value = "/admin/user/create")
    public String createUserPage(Model model, @ModelAttribute("newUser") User newUser,
            @RequestParam("imageFile") MultipartFile file) {
        String avatar = this.uploadService.handleSaveUploadFile(file, "avatar");
        String hashPassword = this.passwordEncoder.encode(newUser.getPassword());

        newUser.setAvatar(avatar);
        newUser.setPassword(hashPassword);
        var role = this.userService.getRoleByName(newUser.getRole().getName());
        newUser.setRole(role);

        this.userService.handelSaveUser(newUser);
        return "redirect:/admin/user"; // redirect to user page (table info)
    }

    // Get update user page (view)
    @RequestMapping("/admin/user/update/{id}")
    public String getUserUpdatePage(Model model, @PathVariable long id) {
        User currentUser = this.userService.getUserById(id);
        model.addAttribute("currentUser", currentUser);
        return "admin/user/update";
    }

    @PostMapping("/admin/user/update")
    public String postUpdateUser(Model model, @ModelAttribute("newUser") User updatedUser) {
        // String avatar = this.uploadService.handleSaveUploadFile(file, "avatar");
        User currentUser = this.userService.getUserById(updatedUser.getId());

        if (currentUser != null) {
            currentUser.setFullName(updatedUser.getFullName());
            currentUser.setAddress(updatedUser.getAddress());
            currentUser.setPhone(updatedUser.getPhone());
            var role = this.userService.getRoleByName(updatedUser.getRole().getName());
            currentUser.setRole(role);
            // currentUser.setAvatar(avatar);
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
