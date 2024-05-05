package com.isolutions4u.onlineshopping.controllers;

import com.isolutions4u.onlineshopping.model.Category;
import com.isolutions4u.onlineshopping.model.Product;
import com.isolutions4u.onlineshopping.service.CategoryService;
import com.isolutions4u.onlineshopping.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Controller
public class IndexController {

    @Autowired
    @Qualifier("categoryService")
    private CategoryService categoryService;

    @Autowired
    @Qualifier("productService")
    private ProductService productService;
    
    //Hiển thị trang chủ của ứng dụng, trong đó có thể hiển thị danh sách các danh mục hoặc thông tin khác tùy thuộc vào yêu cầu của ứng dụng
    @GetMapping(value = {"/", "/home"})
    public ModelAndView home() {
        ModelAndView modelAndView = new ModelAndView("page");
        modelAndView.addObject("userClickHome", true);
        modelAndView.addObject("title", "Home");

        modelAndView.addObject("categories", categoryService.findAllCategories());

        return modelAndView;
    }

    //Hiển thị trang liên hệ của ứng dụng, trong đó có thể chứa thông tin liên hệ hoặc một biểu mẫu liên hệ để người dùng có thể gửi thông điệp tới quản trị viên
    @GetMapping("/contact")
    public ModelAndView contact() {
        ModelAndView modelAndView = new ModelAndView("page");
        modelAndView.addObject("userClickContact", true);
        modelAndView.addObject("title", "Contact Us");

        return modelAndView;
    }
    
    //Hiển thị trang giới thiệu về ứng dụng hoặc về tổ chức, sản phẩm, dịch vụ của bạn
    @GetMapping("/about")
    public ModelAndView about() {
        ModelAndView modelAndView = new ModelAndView("page");
        modelAndView.addObject("userClickAbout", true);
        modelAndView.addObject("title", "About Us");

        return modelAndView;
    }
    
    //Hiển thị trang đăng nhập của ứng dụng
    @GetMapping("/login")
    public ModelAndView login(@RequestParam(name = "error", required = false) String error,
                              @RequestParam(name = "logout", required = false) String logout) {
        ModelAndView modelAndView = new ModelAndView("login");
        if (error != null) {
            modelAndView.addObject("message", "Invalid User Name or Password!");
        }
        if (logout != null) {
            modelAndView.addObject("logout", "User has Successfully Logged out!");
        }
        modelAndView.addObject("title", "Login");

        return modelAndView;
    }

    
    //Hiển thị tất cả các sản phẩm trên trang, cùng với danh sách các danh mục
    @GetMapping("/show/all/products")
    public ModelAndView showAllProducts() {
        ModelAndView modelAndView = new ModelAndView("page");
        modelAndView.addObject("userClickAllProducts", true);
        modelAndView.addObject("title", "All Products");

        // passing the list of categories

        modelAndView.addObject("categories", categoryService.findAllCategories());

        return modelAndView;
    }

    //Hiển thị tất cả các sản phẩm thuộc một danh mục cụ thể trên trang
    @GetMapping("/show/category/{id}/products")
    public ModelAndView showCategoryProducts(@PathVariable("id") int id) {
        ModelAndView modelAndView = new ModelAndView("page");

        Category category = null;
        category = categoryService.findCategoryById(id);

        modelAndView.addObject("userClickCategoryProducts", true);
        modelAndView.addObject("title", category.getName());

        // passing the list of categories

        modelAndView.addObject("categories", categoryService.findAllCategories());

        // passing the single category object
        modelAndView.addObject("category", category);

        return modelAndView;
    }

    //Hiển thị thông tin chi tiết của một sản phẩm cụ thể trên trang
    @GetMapping("/show/{id}/product")
    public ModelAndView showSingleProduct(@PathVariable("id") int id) throws ProductNotFoundExceptoion {

        ModelAndView modelAndView = new ModelAndView("page");

        Product product = productService.findProductById(id);

        if (product == null)
            throw new ProductNotFoundExceptoion();

        product.setViews(product.getViews() + 1);
        productService.updateProduct(product);
        modelAndView.addObject("title", product.getName());
        modelAndView.addObject("product", product);
        modelAndView.addObject("userClickShowProduct", true);

        return modelAndView;

    }

    //Hiển thị trang lỗi 403, chỉ ra rằng người dùng không được ủy quyền để truy cập trang đó
    @GetMapping("/access-denied")
    public ModelAndView accessDenied() {
        ModelAndView modelAndView = new ModelAndView("404");
        modelAndView.addObject("title", "403 - Access Denied");
        modelAndView.addObject("errorTitle", "Aha! Caught You");
        modelAndView.addObject("errorDescription", "You Are not authorized to Access this Page");
        return modelAndView;
    }

    //Xử lý việc đăng xuất người dùng khỏi hệ thống và chuyển hướng họ đến trang đăng nhập lại
    @GetMapping("/logout")
    public String logout(HttpServletRequest request, HttpServletResponse response) {

        // first we are going to fetch the authentication
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            new SecurityContextLogoutHandler().logout(request, response, authentication);
        }

        return "redirect:/login?logout";
    }

}
