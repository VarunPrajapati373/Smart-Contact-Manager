package com.smart.controller;

import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.smart.dao.UserRepository;
import com.smart.entities.User;
import com.smart.service.EmailService;

import jakarta.servlet.http.HttpSession;

@Controller
public class ForgotController {

	Random random = new Random(1000);
	
	@Autowired
	private EmailService emailservice;
	
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private BCryptPasswordEncoder bcrypt;
	
	@RequestMapping("/forgot")
	public String openEmailForm() {
		
	return "forgot_email_form";	
		
	}
	
	
	@PostMapping("/send-otp")
	public String sendOTP(@RequestParam("email") String email, HttpSession session) {
		
		//generating random no. of 4 digit
		int otp = random.nextInt(999999);
		
		//writing code for sending OTP
		
		String subject="OTP From SCM";
		String message = "<h1> OTP = "+otp+"</h1>";
		String to = email;
		
		
		boolean sendEmail = this.emailservice.sendEmail(subject, message,to);
		
		if(sendEmail) {
			
			//saving OTP in session to vrify if OTP is correct or not 
			session.setAttribute("myotp", otp);
			session.setAttribute("email", email);
			
			return "verify-otp";
			
			
		}else {
			
			session.setAttribute("message", "Check your email");
			
			return "forgot_email_form";
			
		}
		
	}
	
	//Verify OTP
	@PostMapping("/verify-otp")
	public String verifyOtp(@RequestParam("otp") int otp, HttpSession session) {
		
		int myOtp= (int) session.getAttribute("myotp");
		String email=(String) session.getAttribute("email");
		
		if(myOtp==otp) {
			
			//password change form
			User user = this.userRepository.getUserByUserName(email);
			
			if(user==null) {
				//send error message
				session.setAttribute("message", "User does not exist with this email");
				return "forgot_email_form";
				
			}else {
				//send change password form
					
			}
			return "password_change_form";
		
		}else {
			
			session.setAttribute("message", "You have entered wrong otp");
			return "verify-otp";
		}
		
	}
	
	
	@PostMapping("/change-password")
	public String changePassword(@RequestParam("newpassword") String newpassword, HttpSession session) {
		
		String email= (String)session.getAttribute("email");
		User user = this.userRepository.getUserByUserName(email);
		user.setPassword(this.bcrypt.encode(newpassword));
		this.userRepository.save(user);
		
		return "redirect:/signin?change=password changed successfully";
		
		
	}
	
}
