package com.pedidos360.orders;

import com.pedidos360.orders_service.config.SecurityConfig;
import com.pedidos360.orders_service.controller.OrderController;
import com.pedidos360.orders_service.repository.OrderRepository;
import com.pedidos360.orders_service.service.OrderService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.BadJwtException;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = OrderController.class)
@ContextConfiguration(classes = {
        OrderController.class,
        SecurityConfig.class
})
class SecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OrderRepository orderRepository;

    @MockBean
    private OrderService orderService;

    @MockBean
    private JwtDecoder jwtDecoder;

    // PRUEBA 1: Sin autenticación, debe responder HTTP 401.
    @Test
    void cuandoSinToken_entoncesRechazado() throws Exception {
        mockMvc.perform(get("/api/orders"))
                .andExpect(status().isUnauthorized());
    }

    // PRUEBA 2: Un administrador autenticado puede consultar los pedidos.
    @Test
    @WithMockUser(roles = "Admin")
    void cuandoAdministradorAutenticado_entoncesProcesado()
            throws Exception {

        when(orderRepository.findAll())
                .thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/orders"))
                .andExpect(status().isOk());
    }

    // PRUEBA 3: Un cliente no puede consultar todos los pedidos.
    @Test
    @WithMockUser(roles = "Cliente")
    void cuandoClienteIntentaAccesoAdmin_entoncesRechazadoPorPermisos()
            throws Exception {

        mockMvc.perform(get("/api/orders"))
                .andExpect(status().isForbidden());
    }

    
    // PRUEBA 4: Token inválido o expirado -> HTTP 401.
    @Test
    void cuandoTokenInvalidoOExpirado_entoncesRechazado()
            throws Exception {

        when(jwtDecoder.decode(anyString()))
                .thenThrow(new BadJwtException("Token inválido o expirado"));

        mockMvc.perform(get("/api/orders")
                .header("Authorization", "Bearer token_malo_o_vencido"))
                .andExpect(status().isUnauthorized());
    }

}
