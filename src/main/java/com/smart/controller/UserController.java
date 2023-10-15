package com.smart.controller;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.smart.dao.ContactRepository;
import com.smart.dao.UserRepository;
import com.smart.entities.Contact;
import com.smart.entities.User;
import com.smart.helper.Message;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/user")
public class UserController {
	
	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;
	
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private ContactRepository contactRepository;
	
	@ModelAttribute
	public void addCommonData(Model model, Principal principal) {
		
		String userName= principal.getName();
		System.out.println(userName);
		
		//get the user detail using username("Email")
		User user = userRepository.getUserByUserName(userName);
		model.addAttribute("user",user);
		
		
	}
	
	
	//DashBoard Home
	@RequestMapping("/index")
	public String dashboard(Model model, Principal principal) {
		
		model.addAttribute("title","DASHBOARD");
		
		return "normal/user_dashboard";
	}
	
	
	//open add form handler
	@GetMapping("/add-contact")
	public String openAddContactForm(Model model) {
		
		
		model.addAttribute("title","Add Contact");
		model.addAttribute("contact",new Contact());
		
		return "normal/add_contact_form";
	}
	
	
	//processing Add Contact form
	@PostMapping("/process-contact")
	public String processContact(@ModelAttribute Contact contact, @RequestParam("profileImage") MultipartFile file, Principal principal, HttpSession session) {
		
		try {
		
		String name= principal.getName();
		User user = this.userRepository.getUserByUserName(name);
		
		//processing and uploading file
		
		if (file.isEmpty()) {
			System.out.println("Image Is not Selected");
			contact.setImage("contact.png");
		}else {
			
			contact.setImage(file.getOriginalFilename());
			File saveFile = new ClassPathResource("static/image").getFile();
			Path path = Paths.get(saveFile.getAbsoluteFile()+File.separator+file.getOriginalFilename());
			Files.copy(file.getInputStream(),path , StandardCopyOption.REPLACE_EXISTING);
			System.out.println("Image Uploaded Successfully");	
		}
		
		//bi-directional mapping
		contact.setUser(user);
		user.getContacts().add(contact);
		this.userRepository.save(user);
		
		System.out.println("Contact Added Successfully");
		
		session.setAttribute("message", new Message("Your Contact is added!! Add More..","success"));
		
		
		}catch (Exception e) {
			// TODO: handle exception
			System.out.println("ERROR "+e.getMessage());
			e.printStackTrace();
			
			session.setAttribute("message", new Message("Something went wrong..!! Try Again","danger"));
		}
		
		
		return "normal/add_contact_form";
	}
	
	//SHOW Contacts
	//per page =5 contacts
	//current page =0[page]
	@GetMapping("/show-contacts/{page}")
	public String showContacts(@PathVariable("page")Integer page,Model m, Principal principal) {
		
		m.addAttribute("title","VIEW CONTACTS");
		//contact List bhejni hai yaha
		String userName = principal.getName();
		User user = this.userRepository.getUserByUserName(userName);
		
		Pageable pageable = PageRequest.of(page, 5);
		
		Page<Contact> contacts = this.contactRepository.findContactByUser(user.getId(),pageable);
		m.addAttribute("contacts",contacts);
		m.addAttribute("currentPage", page);
		m.addAttribute("totalPages",contacts.getTotalPages());
		
		return "normal/show_contacts";
	}
	
	//showing particular contact details
	
	@GetMapping("/contact/{cId}")
	public String showContactDetails(@PathVariable("cId") Integer cId, Model model, Principal principal) {
		
		Optional<Contact> contactOptional = this.contactRepository.findById(cId);
		Contact contact = contactOptional.get();
		
		String Username = principal.getName();
		User user = this.userRepository.getUserByUserName(Username);
		
		if(user.getId()==contact.getUser().getId()) {
			
			model.addAttribute("contact", contact);
			model.addAttribute("title","Contact Detail");
		}else {	
			model.addAttribute("title","Access Denied!!!");	
		}		
		/* model.addAttribute("contact",contact); */
		return "normal/contact_detail";
	}

	//delete contact handler
		
	@GetMapping("/delete/{cId}")
	public String deleteContact(@PathVariable("cId") Integer cId, Model model, Principal principal, HttpSession session) {
		
		Optional<Contact> contactOptional = this.contactRepository.findById(cId);
		Contact contact = contactOptional.get();
		
		
		String Username = principal.getName();
		User user = this.userRepository.getUserByUserName(Username);
		
		if(user.getId()==contact.getUser().getId()) {
			
			user.getContacts().remove(contact);
			this.userRepository.save(user);
			
			session.setAttribute("message", new Message("Contact deleted successfully", "success"));
			
			return "redirect:/user/show-contacts/0";
			
		}else {	
			model.addAttribute("title","Access Denied!!!");	
			
			return "redirect:/user/show-contacts/0";
		}
		
	}
	 
	
	//OPEN UPDATE FORM HANDLER
	@PostMapping("/update-contact/{cid}")
	public String updateForm(@PathVariable("cid") Integer cid,Model m) {
		
		m.addAttribute("title","Update Contact");
		Contact contact = this.contactRepository.findById(cid).get();
		
		m.addAttribute("contact",contact);
		
		return "normal/update_form";
	}
	
	//UPDATE Contact handler
	@PostMapping("/process-update")
	public String updateHandler(@ModelAttribute Contact contact, @RequestParam("profileImage") MultipartFile file, Model m, HttpSession session, Principal principal) {
		
		try {
			//old contact details
			Contact oldContactDetail = this.contactRepository.findById(contact.getcId()).get();
			
			if(!file.isEmpty()) {
				
				//delete old image
				File deleteFile = new ClassPathResource("static/image").getFile();
				File file1= new File(deleteFile, oldContactDetail.getImage());
				file1.delete();
				
				//update new Image
				File saveFile = new ClassPathResource("static/image").getFile();
				Path path = Paths.get(saveFile.getAbsoluteFile()+File.separator+file.getOriginalFilename());
				Files.copy(file.getInputStream(),path , StandardCopyOption.REPLACE_EXISTING);
				
				contact.setImage(file.getOriginalFilename());
				
			}else {
				contact.setImage(oldContactDetail.getImage());
				
			}
			
			User user = this.userRepository.getUserByUserName(principal.getName());
			contact.setUser(user);
			
			this.contactRepository.save(contact);
			
			session.setAttribute("message", new Message("Your Contact is Updated..", "success"));
			
		}catch (Exception e) {
			// TODO: handle exception
		}
		
		
		return "redirect:/user/contact/"+contact.getcId();
	}
	
	
	//your profile handler
	@GetMapping("/profile")
	public String yourProfile(Model model) {
		
		model.addAttribute("title", "My Profile");
		return "normal/profile";
		
	}
	
	//Open Settings Handler
	@GetMapping("/settings")
	public String openSettings(Model model) {
		
		model.addAttribute("title","Settings");
		
		return "normal/settings";
	}
	
	//change password
	@PostMapping("/change-password")
	public String changePassword(@RequestParam("oldPassword") String oldPassword, @RequestParam("newPassword") String newPassword , Principal principal,HttpSession httpSession) {
		
		String name = principal.getName();
		User currentUser = this.userRepository.getUserByUserName(name);
		
		if(this.bCryptPasswordEncoder.matches(oldPassword, currentUser.getPassword())) {
			
			//change the password
			currentUser.setPassword(this.bCryptPasswordEncoder.encode(newPassword));
			this.userRepository.save(currentUser);
			httpSession.setAttribute("message", new Message("Password successfully changed", "success"));
		}else {
			httpSession.setAttribute("message", new Message("Wrong Old Password!!", "danger"));
			return "redirect:/user/settings";
		}
		
		return "redirect:/user/index";
	}
	
}
