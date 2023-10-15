package com.smart.controller;

import javax.naming.Binding;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.smart.dao.UserRepository;
import com.smart.entities.User;
import com.smart.helper.Message;

import jakarta.servlet.http.*;
import jakarta.validation.Valid;

@Controller
public class HomeController {

	@Autowired
	private BCryptPasswordEncoder passwordEncoder;
	
	@Autowired
	private UserRepository userRepository;
	
	@RequestMapping("/")
	public String home(Model model) {
		
		model.addAttribute("title", "HOME||Smart Contact Manager");
		
		return "home";
		
	}
	
	@RequestMapping("/about")
	public String about(Model model) {
		
		model.addAttribute("title", "ABOUT||Smart Contact Manager");
		
		return "about";
		
	}
	
	@RequestMapping("/signup")
	public String signup(Model model) {
		
		model.addAttribute("title", "REGISTER||Smart Contact Manager");
		
		model.addAttribute("user", new User());
		
		return "signup";
		
	}
	
	//handler for registering user
	@PostMapping("/do_register")
	public String registerUser(@Valid @ModelAttribute("user") User user, BindingResult result1, @RequestParam(value="agreement",defaultValue = "false")boolean agreement, Model model ,HttpSession session) {
		
		try {
			
			if(!agreement) {
				
				System.out.println("You have not agreed the terms and conditions.");
				throw new Exception("You have not agreed the terms and conditions.");
				
			}
			
			if(result1.hasErrors()) {
				
				System.out.println("ERROR "+result1.toString());
				
				model.addAttribute("user",user);
				return "signup";
			}
			
			user.setRole("ROLE_USER");
			user.setEnabled(true);
			user.setImageUrl("default.png");
			
			user.setPassword(passwordEncoder.encode(user.getPassword()));
			
			System.out.println("Agreement "+agreement);
			System.out.println("USER "+user);
		
			User result= this.userRepository.save(user);
			model.addAttribute("user", new User());
			
			session.setAttribute("message", new Message("User registered Successfully", "alert-success"));
			return "signup";
			
			
			
		}catch (Exception e) {
			// TODO: handle exception
			
			e.printStackTrace();
			model.addAttribute("user", user);
			session.setAttribute("message", new Message("Something went wrong !!!"+e.getMessage(), "alert-danger"));
			return "signup";
		}
		
		
	}

	//handler for custom login
	@GetMapping("/signin")
	public String customLogin(Model model) {
		
		model.addAttribute("title","LOGIN||Smart Contact Manager");
		return "login.html"; 
	}

}
