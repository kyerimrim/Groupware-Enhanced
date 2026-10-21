package com.giga.gw.test;

import static org.junit.Assert.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import javax.imageio.ImageIO;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import com.giga.gw.controller.HRManagementController;
import com.giga.gw.dto.EmployeeDto;
import com.giga.gw.repository.IEmployeeDao;

public class ProfileImageRegistrationTest {
    private MockMvc mvc;
    private MockHttpSession session;
    private final AtomicReference<Map<String, Object>> inserted = new AtomicReference<>();

    @Before
    @SuppressWarnings("unchecked")
    public void setUp() {
        IEmployeeDao dao = (IEmployeeDao) Proxy.newProxyInstance(
                IEmployeeDao.class.getClassLoader(), new Class<?>[] {IEmployeeDao.class},
                (proxy, method, args) -> {
                    if ("insertEmployee".equals(method.getName())) {
                        inserted.set((Map<String, Object>) args[0]);
                        return 1;
                    }
                    throw new UnsupportedOperationException(method.getName());
                });
        mvc = MockMvcBuilders.standaloneSetup(new HRManagementController(null, dao, null)).build();
        session = new MockHttpSession();
        EmployeeDto login = new EmployeeDto();
        login.setEmpno("2601001");
        session.setAttribute("loginDto", login);
    }

    private MockMultipartFile employee() {
        return new MockMultipartFile("employee", "blob", "application/json",
                "{\"empno\":\"2610001\",\"name\":\"Test\",\"profileImage\":\"untrusted\"}".getBytes());
    }

    @Test
    public void registersMultipartImageWithEmployee() throws Exception {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        ImageIO.write(new BufferedImage(2, 2, BufferedImage.TYPE_INT_RGB), "png", bytes);
        assertEquals(200, mvc.perform(multipart("/hrManagement/employeeAdd.do")
                .file(employee()).file(new MockMultipartFile("profileImage", "photo.png",
                        "image/png", bytes.toByteArray())).session(session))
                .andReturn().getResponse().getStatus());
        assertTrue(inserted.get().get("profileImage").toString().startsWith("data:image/png;base64,"));
        assertEquals("2601001", inserted.get().get("create_emp"));
    }

    @Test
    public void ignoresClientSuppliedImageStringWhenNoFileSelected() throws Exception {
        assertEquals("true", mvc.perform(multipart("/hrManagement/employeeAdd.do")
                .file(employee()).session(session)).andReturn().getResponse().getContentAsString());
        assertNull(inserted.get().get("profileImage"));
    }

    @Test
    public void invalidImageDoesNotInsertEmployee() throws Exception {
        assertEquals(400, mvc.perform(multipart("/hrManagement/employeeAdd.do")
                .file(employee()).file(new MockMultipartFile("profileImage", new byte[] {1, 2, 3}))
                .session(session)).andReturn().getResponse().getStatus());
        assertNull(inserted.get());
    }
}
