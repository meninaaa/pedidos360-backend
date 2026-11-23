package com.pedidos360.kafka_admin;

import com.pedidos360.kafkaadmin.KafkaAdminApplication;
import com.pedidos360.kafkaadmin.service.KafkaAdminService;
import org.apache.kafka.clients.admin.AdminClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = KafkaAdminApplication.class)
@AutoConfigureMockMvc
class KafkaAdminSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AdminClient adminClient;

    @MockBean
    private KafkaAdminService kafkaAdminService;

    @MockBean
    private JwtDecoder jwtDecoder;

    @Test
    void cuandoSinToken_entoncesRetornaUnauthorized() throws Exception {
        mockMvc.perform(get("/api/kafka-admin/topics"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void cuandoRolCliente_entoncesRetornaForbidden() throws Exception {
        mockMvc.perform(get("/api/kafka-admin/topics")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_Cliente"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void cuandoRolAdmin_entoncesPermiteListarTopicos() throws Exception {
        mockMvc.perform(get("/api/kafka-admin/topics")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_Admin"))))
                .andExpect(status().isOk());
    }
}