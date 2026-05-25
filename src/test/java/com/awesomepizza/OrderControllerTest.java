package com.awesomepizza;

import com.awesomepizza.model.Order;
import com.awesomepizza.model.OrderItem;
import com.awesomepizza.model.OrderStatusEnum;
import com.awesomepizza.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private OrderRepository orderRepository;

    @BeforeEach
    void setUp() {
        // Clear the test database before each test to keep test cases isolated.
        orderRepository.deleteAll();
    }

    @Test
    void createOrder_shouldReturnCreatedForValidRequest() throws Exception {
        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "customerName": "Mario Rossi",
                                  "items": [
                                    {
                                      "pizzaName": "Margherita",
                                      "quantity": 2
                                    }
                                  ]
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").exists())
                .andExpect(jsonPath("$.status").value(OrderStatusEnum.RECEIVED.name()))
                .andExpect(jsonPath("$.customerName").value("Mario Rossi"))
                .andExpect(jsonPath("$.items[0].pizzaName").value("Margherita"))
                .andExpect(jsonPath("$.items[0].quantity").value(2));
    }

    @Test
    void createOrder_shouldReturnBadRequestForNestedValidationErrors() throws Exception {
        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "customerName": "Mario Rossi",
                                  "items": [
                                    {
                                      "pizzaName": "",
                                      "quantity": 0
                                    }
                                  ]
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Invalid request"))
                .andExpect(jsonPath("$.detail", containsString("items[0].pizzaName")))
                .andExpect(jsonPath("$.detail", containsString("items[0].quantity")));
    }

    @Test
    void getOrderByCode_shouldReturnNotFoundForUnknownCode() throws Exception {
        mockMvc.perform(get("/orders/AW-UNKNOWN"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Order not found"))
                .andExpect(jsonPath("$.detail").value("Order not found"));
    }

    @Test
    void takeNextOrder_shouldReturnConflictWhenOrderAlreadyInProgress() throws Exception {
        saveInProgressOrder();

        mockMvc.perform(patch("/orders/take-next"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Order conflict"))
                .andExpect(jsonPath("$.detail", containsString("already in progress")));
    }

    @Test
    void completeOrder_shouldReturnNotFoundForUnknownOrder() throws Exception {
        mockMvc.perform(patch("/orders/999/complete"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Order not found"));
    }

    private void saveInProgressOrder() {
        Order order = new Order("AW-INPROG", "Mario Rossi");
        order.addItem(new OrderItem("Margherita", 1));
        order.markAsInProgress();
        orderRepository.save(order);
    }
}
