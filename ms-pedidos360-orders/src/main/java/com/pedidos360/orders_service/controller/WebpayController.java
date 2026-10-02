package com.pedidos360.orders_service.controller;

import com.pedidos360.orders_service.entity.Order;
import com.pedidos360.orders_service.repository.OrderRepository;
import com.pedidos360.orders_service.service.OrderService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import jakarta.servlet.http.HttpServletResponse;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/payments")
public class WebpayController {

    private final RestTemplate restTemplate = new RestTemplate();
    private final OrderRepository orderRepository;
    private final OrderService orderService;

    private final String url = "https://webpay3gint.transbank.cl/rswebpaytransaction/api/webpay/v1.2/transactions";
    private final String commerceCode = "597055555532";
    private final String apiKey = "579B532A7440BB0C9079DED94D31EA1615BACEB56610332264630D42D0A36B1C";

    @Value("${PUBLIC_API_BASE_URL:http://localhost:8080/api/bff}")
    private String publicApiBaseUrl;

    @Value("${FRONTEND_BASE_URL:http://localhost:4200}")
    private String frontendBaseUrl;

    public WebpayController(OrderRepository orderRepository, OrderService orderService) {
        this.orderRepository = orderRepository;
        this.orderService = orderService;
    }

    @PostMapping("/create")
    public Map<String, String> createTransaction(@RequestParam double amount, @RequestParam String orderId) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Tbk-Api-Key-Id", commerceCode);
        headers.set("Tbk-Api-Key-Secret", apiKey);
        headers.set("Content-Type", "application/json");

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("buy_order", orderId);
        requestBody.put("session_id", "sesion-" + orderId);
        requestBody.put("amount", amount);
        requestBody.put("return_url", publicApiBaseUrl + "/payments/commit");

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, entity, Map.class);
        Map<String, Object> tbkResponse = response.getBody();

        Map<String, String> result = new HashMap<>();
        if (tbkResponse != null) {
            result.put("url", (String) tbkResponse.get("url"));
            result.put("token_ws", (String) tbkResponse.get("token"));
        }
        return result;
    }

    @GetMapping("/commit")
    public Map<String, String> commitTransaction(
            @RequestParam(value = "token_ws", required = false) String tokenWs,
            @RequestParam(value = "TBK_TOKEN", required = false) String tbkToken
    ) {
        Map<String, String> result = new HashMap<>();

        if (tokenWs == null && tbkToken != null) {
            result.put("url", frontendBaseUrl + "/payment-result?status=cancelled");
            return result;
        }

        HttpHeaders headers = new HttpHeaders();
        headers.set("Tbk-Api-Key-Id", commerceCode);
        headers.set("Tbk-Api-Key-Secret", apiKey);
        headers.set("Content-Type", "application/json");

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(url + "/" + tokenWs, HttpMethod.PUT, entity, Map.class);
            Map<String, Object> body = response.getBody();
            String status = body != null ? (String) body.get("status") : null;
            String buyOrder = body != null ? String.valueOf(body.get("buy_order")) : null;

            if ("AUTHORIZED".equals(status) && buyOrder != null) {
                try {
                    Long orderId = Long.parseLong(buyOrder);
                    orderService.updateOrderStatus(orderId, Order.OrderStatus.ACEPTADO);
                } catch (Exception e) {
                    System.err.println("⚠ [WEBPAY] Pago autorizado pero no se pudo actualizar el pedido " + buyOrder + ": " + e.getMessage());
                }
                result.put("url", frontendBaseUrl + "/payment-result?status=success&token=" + tokenWs);
            } else {
                result.put("url", frontendBaseUrl + "/payment-result?status=failed");
            }
        } catch (Exception e) {
            result.put("url", frontendBaseUrl + "/payment-result?status=failed");
        }
        return result;
    }
}