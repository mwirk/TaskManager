package com.taskmanager.org.mvcControllerTest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.taskmanager.org.controller.AuthController;
import com.taskmanager.org.dto.UserDTO;
import com.taskmanager.org.model.Role;
import com.taskmanager.org.model.User;
import com.taskmanager.org.repository.RoleRepository;
import com.taskmanager.org.service.*;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.servlet.ModelAndView;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TaskService taskService;

    @MockBean
    private UserService userService;

    @MockBean
    private CategoryService categoryService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private RoleRepository roleRepository;

    @MockBean
    private UserDetailsService userDetailsService;

    @MockBean
    private PasswordEncoder passwordEncoder;


    @Test
    @WithMockUser
    void shouldReturnRegisterViewWithUserModel() throws Exception {
        MvcResult mvcResult = mockMvc.perform(get("/register"))
                .andReturn();

        ModelAndView mav = mvcResult.getModelAndView();
        assertThat(mav).isNotNull();
        assertThat(mav.getViewName()).isEqualTo("register");
        assertThat(mav.getModel().get("user")).isInstanceOf(UserDTO.class);
    }

    @Test
    @WithMockUser
    void shouldRegisterNewUserAndRedirectToLogin() throws Exception {
        Role userRole = new Role();
        userRole.setName("ROLE_USER");
        when(roleRepository.findByName("ROLE_USER")).thenReturn(Optional.of(userRole));

        mockMvc.perform(post("/register")
                        .param("name", "John Doe")
                        .param("email", "john@example.com")
                        .param("password", "12345")
                        .with(csrf()))
                .andExpect(status().is2xxSuccessful());


    }
}
