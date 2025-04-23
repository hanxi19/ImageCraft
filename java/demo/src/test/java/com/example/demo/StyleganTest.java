package com.example.demo;

import com.example.demo.controller.StyleganController;
import com.example.demo.pojo.StyleganBean;
import com.example.demo.service.styleganimpl.StyleganService1;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class StyleganTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean  // 由Spring注入Mock
    private StyleganService1 styleganService;

    @Test
    void testGenImage() throws Exception {
        when(styleganService.runStyleganScript(any(StyleganBean.class))).thenReturn(true);

        mockMvc.perform(post("/stylegan/gen")
                        .param("seed", "42")
                        .param("truc", "0.7")
                        .param("network", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.msg").value("图片生成成功"))
                .andExpect(jsonPath("$.imgUrl").exists());
    }
}
