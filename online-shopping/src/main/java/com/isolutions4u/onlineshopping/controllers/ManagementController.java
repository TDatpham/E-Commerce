package com.isolutions4u.onlineshopping.controllers;

import com.isolutions4u.onlineshopping.model.Category;
import com.isolutions4u.onlineshopping.model.Product;
import com.isolutions4u.onlineshopping.service.CategoryService;
import com.isolutions4u.onlineshopping.service.ProductService;
import com.isolutions4u.onlineshopping.validator.ProductValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Controller
@RequestMapping("/manage")
public class ManagementController {

    @Qualifier("categoryService")
    @Autowired
    private CategoryService categoryService;

    @Qualifier("productService")
    @Autowired
    private ProductService productService;
    
    //Hiển thị trang quản lý sản phẩm, có thể bao gồm biểu mẫu để thêm hoặc chỉnh sửa sản phẩm và danh sách các sản phẩm hiện có
    @GetMapping(value = "/products")
    public ModelAndView showManageProducts(@RequestParam(name = "operation", required = false) String operation) {

        ModelAndView modelAndView = new ModelAndView("page");

        modelAndView.addObject("userClickManageProducts", true);
        modelAndView.addObject("title", "Manage Products");

        Product nProduct = new Product();
        nProduct.setSupplierId(1);
        nProduct.setActive(true);
        modelAndView.addObject("product", nProduct);
        if (operation != null && operation.equals("product")) {
            modelAndView.addObject("message", "Product Added Sucessfully!");
        } else if (operation != null && operation.equals("category")) {
            modelAndView.addObject("message", "Category Added Sucessfully!");
        } else if (operation != null && operation.equals("updated")) {
            modelAndView.addObject("message", "Product Updated Sucessfully!");
        }

        return modelAndView;
    }
    
    //Xử lý dữ liệu đầu vào từ biểu mẫu thêm hoặc cập nhật sản phẩm, bao gồm việc xử lý tệp tải lên
    @PostMapping(value = "/products")
    public String handleProductSubmission(@Valid @ModelAttribute("product") Product mProduct,
                                          BindingResult bindingResult, Model model, @RequestParam("file") MultipartFile file,
                                          HttpServletRequest request) {

        // check if there are any error

        if (mProduct.getId() == 0) {

            new ProductValidator().validate(mProduct, bindingResult);

            if (bindingResult.hasErrors()) {
                model.addAttribute("userClickManageProducts", true);
                model.addAttribute("title", "Manage Products");
                model.addAttribute("message", "Validation failed for Product Submission!");
                return "page";
            }

            try {

                // Get the file and save it somewhere
                fileSaveInFolder(mProduct, file, request);
                productService.saveProduct(mProduct);
                return "redirect:/manage/products?operation=product";
            } catch (IOException e) {
                e.printStackTrace();
                model.addAttribute("userClickManageProducts", true);
                model.addAttribute("title", "Manage Products");
                model.addAttribute("message", e.getMessage());
                return "page";
            }
        } else {
            if (!mProduct.getFile().getOriginalFilename().equals("")) {
                new ProductValidator().validate(mProduct, bindingResult);
                if (bindingResult.hasErrors()) {
                    model.addAttribute("userClickManageProducts", true);
                    model.addAttribute("title", "Manage Products");
                    model.addAttribute("message", "Validation failed for Product Submission!");
                    return "page";
                }

                try {

                    fileSaveInFolder(mProduct, file, request);
                    productService.updateProduct(mProduct);
                    return "redirect:/manage/products?operation=updated";
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                    model.addAttribute("userClickManageProducts", true);
                    model.addAttribute("title", "Manage Products");
                    model.addAttribute("message", e.getMessage());
                    return "page";
                }

            } else {
                productService.updateProduct(mProduct);
                return "redirect:/manage/products?operation=updated";
            }
        }
    }
    
    //Lưu tệp được tải lên vào thư mục /static/images/ trên máy chủ
    private void fileSaveInFolder(Product mProduct, MultipartFile file, HttpServletRequest request) throws IOException {
        byte[] bytes = file.getBytes();
        String p = request.getSession().getServletContext().getRealPath("/static/images/");
        System.out.println(p);
        Path path = Paths.get(p + mProduct.getCode() + ".jpg");
        Files.write(path, bytes);
    }

    
    //Xử lý việc kích hoạt hoặc vô hiệu hóa sản phẩm và trả về thông báo thành công tương ứng
    @PostMapping("/product/{id}/activation")
    @ResponseBody
    public String handleProductActivation(@PathVariable("id") int id) {

        Product product = productService.findProductByIdForAdmin(id);
        boolean isActive = product.isActive();

        product.setActive(!isActive);

        productService.updateProduct(product);

        return (isActive) ? "You have Successfully deactivated the Product with Id : " + product.getId()
                : "You have Successfully activated the Product with Id : " + product.getId();
    }
    
    //Hiển thị một trang cho phép quản trị viên chỉnh sửa thông tin của một sản phẩm cụ thể trong hệ thống
    @GetMapping("{id}/product")
    public ModelAndView showEditProducts(@PathVariable("id") int id) {

        ModelAndView modelAndView = new ModelAndView("page");

        modelAndView.addObject("userClickManageProducts", true);
        modelAndView.addObject("title", "Manage Products");

        // Fetching the product form database

        Product nProduct = productService.findProductByIdForAdmin(id);

        modelAndView.addObject("product", nProduct);

        return modelAndView;
    }
    
    //Xử lý dữ liệu đầu vào từ biểu mẫu thêm danh mục mới và lưu dữ liệu vào cơ sở dữ liệu
    @PostMapping("/category")
    public String handleCategorySubmission(@ModelAttribute("category") Category category) {

        categoryService.saveCategory(category);

        return "redirect:/manage/products?operation=category";
    }
    
    //Cung cấp danh sách các danh mục cho trang khi trang được tải lần đầu tiên hoặc mỗi khi trang được truy cập
    @ModelAttribute("categories")
    public List<Category> getCategories() {
        List<Category> categories = categoryService.findAllCategories();
        return categories;
    }
    
    //Cung cấp một đối tượng Category trống cho biểu mẫu thêm danh mục mới
    @ModelAttribute("category")
    public Category getCategory() {
        return new Category();
    }

}
